// UI layer (state holder): joins one hike and its packing items into the detail screen's
// state. Ticks, adds and deletes go straight to the repository; Room then re-emits the
// items, so the screen always shows what is really saved.
package com.anthony.sentiero.ui.detail

import android.database.sqlite.SQLiteException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.anthony.sentiero.SentieroApp
import com.anthony.sentiero.data.TripRepository
import com.anthony.sentiero.domain.PackingItem
import com.anthony.sentiero.domain.label
import com.anthony.sentiero.ui.shortLabel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface HikeDetailUiState {
    data object Loading : HikeDetailUiState
    data object NotFound : HikeDetailUiState
    data class Content(
        val title: String,
        val subtitle: String,     // "Sat 14 Nov · Moderate"
        val packed: Int,
        val total: Int,
        val items: List<PackingItem>,
    ) : HikeDetailUiState
}

/** One-time messages for the screen's snackbar. */
sealed interface HikeDetailEvent {
    data class Deleted(val item: PackingItem) : HikeDetailEvent // "<label> deleted." + Undo
    data object SaveFailed : HikeDetailEvent                     // "Couldn't save. Try again."
}

class HikeDetailViewModel(
    private val hikeId: String,
    private val repo: TripRepository,
) : ViewModel() {

    val uiState: StateFlow<HikeDetailUiState> =
        combine(repo.observeHike(hikeId), repo.observeItems(hikeId)) { hike, items ->
            if (hike == null) {
                HikeDetailUiState.NotFound
            } else {
                HikeDetailUiState.Content(
                    title = hike.routeName,
                    subtitle = "${hike.date.shortLabel()} · ${hike.difficulty.label}",
                    packed = items.count { it.isPacked },
                    total = items.size,
                    items = items,
                )
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HikeDetailUiState.Loading)

    /** [checked] is the checkbox's new value: we save it as-is, never "flip what we had". */
    fun onTick(itemId: String, checked: Boolean) {
        viewModelScope.launch { repo.setPacked(itemId, checked) }
    }

    // The "+ Add item" text lives here so it survives rotation and stays if a save fails.
    private val _addText = MutableStateFlow("")
    val addText: StateFlow<String> = _addText.asStateFlow()

    // A Channel delivers each event once, even if the screen is recreated in between.
    private val _events = Channel<HikeDetailEvent>(Channel.BUFFERED)
    val events: Flow<HikeDetailEvent> = _events.receiveAsFlow()

    fun onAddTextChange(value: String) {
        _addText.value = value.take(MAX_ITEM_LABEL)
    }

    /** Adds the typed item at the end; clears the field only if something was added. */
    fun onAddSubmit() {
        val text = _addText.value
        viewModelScope.launch {
            try {
                if (repo.addItem(hikeId, text)) _addText.value = ""
            } catch (e: SQLiteException) {
                _events.send(HikeDetailEvent.SaveFailed) // the typed text stays
            }
        }
    }

    fun onDelete(itemId: String) {
        viewModelScope.launch {
            try {
                val removed = repo.deleteItem(itemId) ?: return@launch // already gone
                _events.send(HikeDetailEvent.Deleted(removed))
            } catch (e: SQLiteException) {
                _events.send(HikeDetailEvent.SaveFailed)
            }
        }
    }

    fun onUndoDelete(item: PackingItem) {
        viewModelScope.launch {
            try {
                repo.restoreItem(hikeId, item)
            } catch (e: SQLiteException) {
                _events.send(HikeDetailEvent.SaveFailed)
            }
        }
    }

    companion object {
        const val MAX_ITEM_LABEL = 60
        const val SHOW_COUNTER_FROM = 48 // the counter appears only near the limit (DR-6A)

        fun factory(hikeId: String): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as SentieroApp
                HikeDetailViewModel(hikeId, app.container.tripRepository)
            }
        }
    }
}

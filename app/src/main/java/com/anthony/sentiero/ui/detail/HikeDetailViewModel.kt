// UI layer (state holder): joins one hike and its packing items into the detail screen's
// state. A tick goes straight to the repository with the checkbox's new value; Room then
// re-emits the items, so the screen always shows what is really saved.
package com.anthony.sentiero.ui.detail

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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
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

class HikeDetailViewModel(
    hikeId: String,
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

    companion object {
        fun factory(hikeId: String): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as SentieroApp
                HikeDetailViewModel(hikeId, app.container.tripRepository)
            }
        }
    }
}

// UI layer (state holder): turns the repository's Flow of hikes into what the Upcoming
// screen shows. It survives rotation, and stateIn(WhileSubscribed(5_000)) stops the
// database query 5 s after the screen goes away.
package com.anthony.sentiero.ui.upcoming

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.anthony.sentiero.SentieroApp
import com.anthony.sentiero.data.TripRepository
import com.anthony.sentiero.domain.label
import com.anthony.sentiero.ui.RelativeDay
import com.anthony.sentiero.ui.relativeDay
import com.anthony.sentiero.ui.shortLabel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.Clock
import java.time.LocalDate

data class UpcomingRow(
    val id: String,
    val routeName: String,
    val difficulty: String,
    val dateLabel: String,
    val relative: RelativeDay,
    val packed: Int,
    val total: Int,
)

sealed interface UpcomingUiState {
    data object Loading : UpcomingUiState
    data object Empty : UpcomingUiState
    data class Content(val rows: List<UpcomingRow>) : UpcomingUiState
}

class UpcomingViewModel(repo: TripRepository, clock: Clock) : ViewModel() {

    val uiState: StateFlow<UpcomingUiState> = repo.observeUpcoming()
        .map { hikes ->
            val today = LocalDate.now(clock) // read on every update, so "Today" moves at midnight
            if (hikes.isEmpty()) {
                UpcomingUiState.Empty
            } else {
                UpcomingUiState.Content(
                    hikes.map {
                        UpcomingRow(
                            id = it.hike.id,
                            routeName = it.hike.routeName,
                            difficulty = it.hike.difficulty.label,
                            dateLabel = it.hike.date.shortLabel(),
                            relative = relativeDay(it.hike.date, today),
                            packed = it.packedCount,
                            total = it.totalCount,
                        )
                    }
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UpcomingUiState.Loading)

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as SentieroApp
                UpcomingViewModel(app.container.tripRepository, app.container.clock)
            }
        }
    }
}

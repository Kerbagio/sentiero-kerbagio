// UI layer (state holder): keeps what the user typed in the New hike form. Because it
// lives in a ViewModel, the name, date, difficulty and area search survive rotation. It decides
// when Save is allowed and asks the repository to create the hike.
package com.anthony.sentiero.ui.form

import android.database.sqlite.SQLiteException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.anthony.sentiero.SentieroApp
import com.anthony.sentiero.data.TripRepository
import com.anthony.sentiero.data.WeatherRepository
import com.anthony.sentiero.domain.Difficulty
import com.anthony.sentiero.ui.components.AreaSearchState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

data class HikeFormUiState(
    val routeName: String = "",
    val date: LocalDate? = null,
    val difficulty: Difficulty? = null,
    val isSaving: Boolean = false,
    val saveFailed: Boolean = false, // true until the "Couldn't save" snackbar has been shown
) {
    val canSave: Boolean
        get() = routeName.isNotBlank() && date != null && difficulty != null && !isSaving
}

class HikeFormViewModel(
    private val repo: TripRepository,
    weather: WeatherRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HikeFormUiState())
    val uiState: StateFlow<HikeFormUiState> = _uiState.asStateFlow()

    fun onRouteNameChange(value: String) = _uiState.update { it.copy(routeName = value.take(MAX_ROUTE_NAME)) }
    fun onDatePicked(value: LocalDate) = _uiState.update { it.copy(date = value) }
    fun onDifficultyPicked(value: Difficulty) = _uiState.update { it.copy(difficulty = value) }

    /** The area search. viewModelScope cancels a running search when the form closes (rule 17). */
    val areaSearch = AreaSearchState(weather, viewModelScope)

    /** Creates the hike, then calls [onSaved] with its id so the screen can open it. */
    fun save(onSaved: (String) -> Unit) {
        val s = _uiState.value
        if (!s.canSave) return // a second tap arrives here with isSaving = true and stops
        _uiState.update { it.copy(isSaving = true) } // set before the save starts, not inside it
        viewModelScope.launch {
            try {
                // Save never waits on the network: only an already picked area is saved.
                val id = repo.createHike(s.routeName, s.date!!, s.difficulty!!, area = areaSearch.pickedArea)
                onSaved(id)
            } catch (e: SQLiteException) {
                // Only database errors: keep everything typed and let the user try again.
                _uiState.update { it.copy(isSaving = false, saveFailed = true) }
            }
        }
    }

    /** The screen calls this once the "Couldn't save" snackbar has been shown. */
    fun onSaveErrorShown() = _uiState.update { it.copy(saveFailed = false) }

    companion object {
        const val MAX_ROUTE_NAME = 80
        const val SHOW_COUNTER_FROM = 64 // the counter appears only near the limit (DR-6A)

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as SentieroApp
                HikeFormViewModel(app.container.tripRepository, app.container.weatherRepository)
            }
        }
    }
}

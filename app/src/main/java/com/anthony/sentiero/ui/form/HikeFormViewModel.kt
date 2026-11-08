// UI layer (state holder): keeps what the user typed in the New hike form. Because it
// lives in a ViewModel, the name, date and difficulty survive rotation. It decides when
// Save is allowed and asks the repository to create the hike.
package com.anthony.sentiero.ui.form

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.anthony.sentiero.SentieroApp
import com.anthony.sentiero.data.TripRepository
import com.anthony.sentiero.domain.Difficulty
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
) {
    val canSave: Boolean
        get() = routeName.isNotBlank() && date != null && difficulty != null
}

class HikeFormViewModel(private val repo: TripRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(HikeFormUiState())
    val uiState: StateFlow<HikeFormUiState> = _uiState.asStateFlow()

    fun onRouteNameChange(value: String) = _uiState.update { it.copy(routeName = value) }
    fun onDatePicked(value: LocalDate) = _uiState.update { it.copy(date = value) }
    fun onDifficultyPicked(value: Difficulty) = _uiState.update { it.copy(difficulty = value) }

    /** Creates the hike, then calls [onSaved] with its id so the screen can open it. */
    fun save(onSaved: (String) -> Unit) {
        val s = _uiState.value
        if (!s.canSave) return
        viewModelScope.launch {
            val id = repo.createHike(s.routeName, s.date!!, s.difficulty!!)
            onSaved(id)
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as SentieroApp
                HikeFormViewModel(app.container.tripRepository)
            }
        }
    }
}

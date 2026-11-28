// UI layer (state holder): the area search behind AreaSearchField. A ViewModel owns it and
// passes its viewModelScope, so the text and results survive rotation and a running search
// is cancelled when the screen goes away. It has no Compose code, so it is JVM-tested.
package com.anthony.sentiero.ui.components

import com.anthony.sentiero.data.WeatherRepository
import com.anthony.sentiero.domain.Area
import com.anthony.sentiero.domain.Place
import com.anthony.sentiero.domain.PlaceSearchResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** What sits under the field while the user is searching. */
sealed interface SearchPhase {
    data object Idle : SearchPhase
    data object Loading : SearchPhase
    data class Results(val places: List<Place>) : SearchPhase
    data class Empty(val query: String) : SearchPhase
    data object Error : SearchPhase
}

/** Either the user is typing and searching, or a place has been picked. */
sealed interface AreaFieldUi {
    data class Editing(val text: String, val phase: SearchPhase, val tooShort: Boolean) : AreaFieldUi
    data class Picked(val area: Area) : AreaFieldUi
}

class AreaSearchState(
    private val weather: WeatherRepository,
    private val scope: CoroutineScope,
    initial: Area? = null,
) {
    private val _ui = MutableStateFlow(initial?.let { AreaFieldUi.Picked(it) } ?: emptyEditing())
    val ui: StateFlow<AreaFieldUi> = _ui.asStateFlow()

    /** The area to save with the hike; null while nothing is picked. */
    val pickedArea: Area? get() = (_ui.value as? AreaFieldUi.Picked)?.area

    private var job: Job? = null
    private var lastQuery = ""

    /** Typing drops old results, so the results on screen always match the text. */
    fun onTextChange(text: String) {
        job?.cancel()
        _ui.value = AreaFieldUi.Editing(text.take(MAX_QUERY), SearchPhase.Idle, tooShort = false)
    }

    /** Search runs on submit only (keyboard Search or the icon), never on every key press. */
    fun submit() {
        val editing = _ui.value as? AreaFieldUi.Editing ?: return
        val query = editing.text.trim()
        if (query.length < MIN_QUERY) {
            _ui.value = editing.copy(phase = SearchPhase.Idle, tooShort = true)
            return
        }
        lastQuery = query
        search(editing.text, query)
    }

    fun retry() {
        val editing = _ui.value as? AreaFieldUi.Editing ?: return
        if (lastQuery.isNotEmpty()) search(editing.text, lastQuery)
    }

    fun pick(place: Place) {
        job?.cancel()
        _ui.value = AreaFieldUi.Picked(place.toArea())
    }

    fun clear() {
        job?.cancel()
        _ui.value = emptyEditing()
    }

    /** Cancels the older search first (T5), so a slow old answer can't replace a newer one. */
    private fun search(text: String, query: String) {
        job?.cancel()
        _ui.value = AreaFieldUi.Editing(text, SearchPhase.Loading, tooShort = false)
        job = scope.launch {
            val phase = when (val result = weather.searchPlaces(query)) {
                is PlaceSearchResult.Found -> SearchPhase.Results(result.places)
                PlaceSearchResult.NoMatch -> SearchPhase.Empty(query)
                is PlaceSearchResult.Failed -> SearchPhase.Error
            }
            _ui.value = AreaFieldUi.Editing(text, phase, tooShort = false)
        }
    }

    private fun emptyEditing() = AreaFieldUi.Editing("", SearchPhase.Idle, tooShort = false)

    companion object {
        const val MIN_QUERY = 2
        const val MAX_QUERY = 60
    }
}

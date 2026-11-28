// JVM test for AreaSearchState with a fake weather repository and a test scope.
// Card: protects=the search rules on the form (2-letter minimum, newest query wins, Retry,
// results always match the text); fails_when=a short query hits the network, a slow old answer
// replaces a newer one, Retry stays stuck, or old results stay after editing;
// no_claim=how the field looks (checked on the phone)
package com.anthony.sentiero.ui.components

import com.anthony.sentiero.domain.Place
import com.anthony.sentiero.domain.PlaceSearchResult
import com.anthony.sentiero.domain.WeatherFailure
import com.anthony.sentiero.fakes.FakeWeatherRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AreaSearchStateTest {

    private val faraya = Place("Faraya", "Keserwan-Jbeil", "Lebanon", 34.0, 35.83)
    private val bsharri = Place("Bsharri", "North Lebanon", "Lebanon", 34.25, 36.01)

    private fun TestScope.editing(state: AreaSearchState) = state.ui.value as AreaFieldUi.Editing

    @Test
    fun tooShortSendsNoRequest() = runTest {
        val weather = FakeWeatherRepository()
        val state = AreaSearchState(weather, this)

        state.onTextChange(" B ")
        state.submit()
        advanceUntilIdle()

        assertTrue(editing(state).tooShort)
        assertEquals(SearchPhase.Idle, editing(state).phase)
        assertEquals(emptyList<String>(), weather.searchedQueries)
    }

    @Test
    fun newerSubmitWinsOverASlowOlderOne() = runTest {
        val gateA = CompletableDeferred<PlaceSearchResult>()
        val weather = FakeWeatherRepository(search = { query ->
            if (query == "Faraya") gateA.await() else PlaceSearchResult.Found(listOf(bsharri))
        })
        val state = AreaSearchState(weather, this)

        state.onTextChange("Faraya")
        state.submit()
        advanceUntilIdle() // A is now waiting on its gate
        state.onTextChange("Bsharri")
        state.submit()
        advanceUntilIdle() // B answers
        gateA.complete(PlaceSearchResult.Found(listOf(faraya))) // A's answer arrives late
        advanceUntilIdle()

        assertEquals(listOf("Faraya", "Bsharri"), weather.searchedQueries)
        assertEquals(AreaFieldUi.Editing("Bsharri", SearchPhase.Results(listOf(bsharri)), false), state.ui.value)
    }

    @Test
    fun errorThenRetryShowsResults() = runTest {
        var online = false
        val weather = FakeWeatherRepository(search = {
            if (online) PlaceSearchResult.Found(listOf(faraya)) else PlaceSearchResult.Failed(WeatherFailure.OFFLINE)
        })
        val state = AreaSearchState(weather, this)

        state.onTextChange("Faraya")
        state.submit()
        advanceUntilIdle()
        assertEquals(SearchPhase.Error, editing(state).phase)

        online = true
        state.retry()
        advanceUntilIdle()
        assertEquals(SearchPhase.Results(listOf(faraya)), editing(state).phase)
        assertEquals(listOf("Faraya", "Faraya"), weather.searchedQueries)
    }

    @Test
    fun editingTheTextClearsResults() = runTest {
        val weather = FakeWeatherRepository(search = { PlaceSearchResult.Found(listOf(faraya)) })
        val state = AreaSearchState(weather, this)

        state.onTextChange("Faraya")
        state.submit()
        advanceUntilIdle()
        state.onTextChange("Farayaa")

        assertEquals(AreaFieldUi.Editing("Farayaa", SearchPhase.Idle, false), state.ui.value)
        assertEquals(null, state.pickedArea)
    }
}

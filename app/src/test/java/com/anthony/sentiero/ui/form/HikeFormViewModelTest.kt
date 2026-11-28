// JVM test for HikeFormViewModel's Save with fake repositories instead of Room and the network.
// Card: protects=typed-but-unpicked area text never becomes an area, and a picked area is saved
// (DR-2D, rule 17); fails_when=the typed text is saved as an area or the picked area is lost;
// no_claim=Room writes the three columns (instrumented tests cover the database)
package com.anthony.sentiero.ui.form

import com.anthony.sentiero.data.TripRepository
import com.anthony.sentiero.domain.Area
import com.anthony.sentiero.domain.Difficulty
import com.anthony.sentiero.domain.Hike
import com.anthony.sentiero.domain.HikeSummary
import com.anthony.sentiero.domain.PackingItem
import com.anthony.sentiero.domain.Place
import com.anthony.sentiero.domain.PlaceSearchResult
import com.anthony.sentiero.fakes.FakeWeatherRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

/** Remembers the area each createHike call received. */
private class RecordingTripRepository : TripRepository {
    val createdAreas = mutableListOf<Area?>()

    override suspend fun createHike(routeName: String, date: LocalDate, difficulty: Difficulty, area: Area?): String {
        createdAreas += area
        return "new-hike"
    }

    // Not used by these tests.
    override fun observeUpcoming(): Flow<List<HikeSummary>> = emptyFlow()
    override fun observeHike(id: String): Flow<Hike?> = emptyFlow()
    override fun observeItems(tripId: String): Flow<List<PackingItem>> = emptyFlow()
    override suspend fun setPacked(itemId: String, packed: Boolean) = Unit
    override suspend fun addItem(tripId: String, label: String) = false
    override suspend fun deleteItem(itemId: String): PackingItem? = null
    override suspend fun restoreItem(tripId: String, item: PackingItem) = Unit
}

@OptIn(ExperimentalCoroutinesApi::class)
class HikeFormViewModelTest {

    // viewModelScope runs on Dispatchers.Main, which doesn't exist on a JVM; swap in a test one.
    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() = Dispatchers.setMain(dispatcher)

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun HikeFormViewModel.fillRequiredFields() {
        onRouteNameChange("Qadisha Valley")
        onDatePicked(LocalDate.of(2026, 12, 12))
        onDifficultyPicked(Difficulty.MODERATE)
    }

    @Test
    fun unpickedTextSavesWithoutAnAreaAndAPickedPlaceIsSaved() = runTest(dispatcher) {
        val bsharri = Place("Bsharri", "North Lebanon", "Lebanon", 34.25, 36.01)
        val repo = RecordingTripRepository()
        val weather = FakeWeatherRepository(search = { PlaceSearchResult.Found(listOf(bsharri)) })

        // Typed, even searched, but never picked: saves with no area.
        val unpicked = HikeFormViewModel(repo, weather)
        unpicked.fillRequiredFields()
        unpicked.areaSearch.onTextChange("Bsharri")
        unpicked.areaSearch.submit()
        advanceUntilIdle()
        unpicked.save {}
        advanceUntilIdle()

        // Picked: saves the place's area.
        val picked = HikeFormViewModel(repo, weather)
        picked.fillRequiredFields()
        picked.areaSearch.pick(bsharri)
        picked.save {}
        advanceUntilIdle()

        assertEquals(listOf(null, Area("Bsharri, North Lebanon", 34.25, 36.01)), repo.createdAreas)
    }
}

// JVM unit test for HikeDetailViewModel's tick, run against a fake repository instead of Room,
// so it checks only the ViewModel's own decision: save the checkbox's new value as-is.
// Card: protects=absolute writes, last tap wins (CEO T7); fails_when=the ViewModel computes
// `!current`; no_claim=Room write ordering (fake repository)
package com.anthony.sentiero.ui.detail

import com.anthony.sentiero.data.TripRepository
import com.anthony.sentiero.domain.Difficulty
import com.anthony.sentiero.domain.Hike
import com.anthony.sentiero.domain.HikeSummary
import com.anthony.sentiero.domain.PackingItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
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

/** Stands in for Room: keeps the items in memory and remembers every setPacked call. */
private class FakeTripRepository : TripRepository {
    val items = MutableStateFlow(listOf(PackingItem("water", "Water (2 L)", isPacked = false)))
    val setPackedCalls = mutableListOf<Pair<String, Boolean>>()

    override fun observeUpcoming(): Flow<List<HikeSummary>> = flowOf(emptyList())
    override fun observeHike(id: String): Flow<Hike?> =
        flowOf(Hike(id, "Jabal Moussa Reserve", LocalDate.of(2026, 11, 21), Difficulty.MODERATE))
    override fun observeItems(tripId: String): Flow<List<PackingItem>> = items
    override suspend fun createHike(routeName: String, date: LocalDate, difficulty: Difficulty) = "hike"

    override suspend fun setPacked(itemId: String, packed: Boolean) {
        setPackedCalls += itemId to packed
        items.value = items.value.map { if (it.id == itemId) it.copy(isPacked = packed) else it }
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class HikeDetailViewModelTest {

    // viewModelScope runs on Dispatchers.Main, which doesn't exist on a JVM; swap in a test one.
    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() = Dispatchers.setMain(dispatcher)

    @After
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun tickingAnItemSavesTrue() = runTest(dispatcher) {
        val repo = FakeTripRepository()
        val viewModel = HikeDetailViewModel("hike", repo)

        viewModel.onTick("water", checked = true)
        advanceUntilIdle()

        assertEquals(listOf("water" to true), repo.setPackedCalls)
        assertEquals(true, repo.items.value.single().isPacked)
    }

    @Test
    fun tickThenUntickFastEndsUnpacked() = runTest(dispatcher) {
        val repo = FakeTripRepository()
        val viewModel = HikeDetailViewModel("hike", repo)

        // Both taps arrive before either write runs, like a fast double tap.
        viewModel.onTick("water", checked = true)
        viewModel.onTick("water", checked = false)
        advanceUntilIdle()

        assertEquals(listOf("water" to true, "water" to false), repo.setPackedCalls)
        assertEquals(false, repo.items.value.single().isPacked)
    }
}

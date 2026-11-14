// Instrumented test: runs on the phone because Room needs Android's real SQLite.
// It uses an in-memory database, so every test starts empty and nothing touches the app's saved hikes.
// Card: protects=ticks persist in Room and the aggregate counts follow; fails_when=`setPacked` or the
// JOIN query breaks; no_claim=surviving a real process restart (in-memory database; the manual restart test covers it)
package com.anthony.sentiero.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.anthony.sentiero.data.RoomTripRepository
import com.anthony.sentiero.domain.Difficulty
import com.anthony.sentiero.domain.templateFor
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
class TripDaoTest {

    private lateinit var db: SentieroDatabase
    private lateinit var repo: RoomTripRepository

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            SentieroDatabase::class.java,
        ).build()
        repo = RoomTripRepository(db, db.tripDao(), db.checklistDao())
    }

    @After
    fun tearDown() = db.close()

    @Test
    fun createHikeInsertsTheTripAndItsItemsInPositionOrder() = runTest {
        val id = repo.createHike("Jabal Moussa Reserve", LocalDate.of(2026, 11, 21), Difficulty.MODERATE)

        val trip = db.tripDao().observeTrip(id).first()
        assertEquals("Jabal Moussa Reserve", trip?.routeName)

        val items = db.checklistDao().observeItems(id).first()
        assertEquals(templateFor(Difficulty.MODERATE), items.map { it.label })
        assertEquals((0 until 12).toList(), items.map { it.position })
        assertEquals(true, items.none { it.isPacked })
    }

    @Test
    fun upcomingCountsFollowTheTicks() = runTest {
        val id = repo.createHike("Jabal Moussa Reserve", LocalDate.of(2026, 11, 21), Difficulty.MODERATE)

        val before = db.tripDao().observeUpcoming().first().single()
        assertEquals(0, before.packedCount)
        assertEquals(12, before.totalCount)

        db.checklistDao().observeItems(id).first().take(3).forEach {
            db.checklistDao().setPacked(it.id, true)
        }

        val after = db.tripDao().observeUpcoming().first().single()
        assertEquals(3, after.packedCount)
        assertEquals(12, after.totalCount)
    }
}

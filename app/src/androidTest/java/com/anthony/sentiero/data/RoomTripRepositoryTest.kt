// Instrumented test for RoomTripRepository's write paths, against a real (in-memory) Room database,
// because appending and Undo depend on real SQL: MAX(position), transactions and primary keys.
// Card: protects=append and Undo (2.1); fails_when=a write path skips a step; no_claim=real process
// death (in-memory database)
package com.anthony.sentiero.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.anthony.sentiero.data.local.SentieroDatabase
import com.anthony.sentiero.domain.Difficulty
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
class RoomTripRepositoryTest {

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

    private suspend fun newModerateHike() =
        repo.createHike("Jabal Moussa Reserve", LocalDate.of(2026, 11, 21), Difficulty.MODERATE)

    @Test
    fun addItemAppendsAtMaxPlusOneTrimsAndIgnoresBlank() = runTest {
        val id = newModerateHike() // positions 0..11

        assertTrue(repo.addItem(id, "  Headlamp  "))
        assertFalse(repo.addItem(id, "   "))

        val items = repo.observeItems(id).first()
        assertEquals(13, items.size) // the blank one wrote nothing
        val added = items.last()
        assertEquals("Headlamp", added.label)
        assertEquals(12, added.position)
        assertFalse(added.isPacked)
    }

    @Test
    fun deleteThenRestoreGivesBackTheSameIdPositionAndPackedState() = runTest {
        val id = newModerateHike()
        val snacks = repo.observeItems(id).first().first { it.label == "Snacks" }
        repo.setPacked(snacks.id, true)

        val removed = repo.deleteItem(snacks.id)!!
        assertEquals(11, repo.observeItems(id).first().size)
        repo.restoreItem(id, removed)

        val items = repo.observeItems(id).first()
        val back = items.first { it.label == "Snacks" }
        assertEquals(12, items.size)
        assertEquals(snacks.id, back.id)
        assertEquals(snacks.position, back.position)
        assertTrue(back.isPacked)
        assertEquals(1, items.indexOf(back)) // second on the list again, as before
    }
}

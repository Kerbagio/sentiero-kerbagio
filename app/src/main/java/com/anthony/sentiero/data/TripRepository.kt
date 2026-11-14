// Data layer: the single door to hike data. ViewModels depend on this interface, never on
// Room or the DAOs, so tests can hand them a fake and RoomTripRepository can change freely.
// Spec 2 adds one method per task (add/delete item, journal, delete hike, ...).
package com.anthony.sentiero.data

import com.anthony.sentiero.domain.Difficulty
import com.anthony.sentiero.domain.Hike
import com.anthony.sentiero.domain.HikeSummary
import com.anthony.sentiero.domain.PackingItem
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface TripRepository {
    fun observeUpcoming(): Flow<List<HikeSummary>>
    fun observeHike(id: String): Flow<Hike?>
    fun observeItems(tripId: String): Flow<List<PackingItem>>

    /** Saves the hike and its starting packing list together. Returns the new hike's id. */
    suspend fun createHike(routeName: String, date: LocalDate, difficulty: Difficulty): String

    /** Saves [packed] as-is (absolute value), so the last tap always wins. */
    suspend fun setPacked(itemId: String, packed: Boolean)
}

// Data layer: the Room version of TripRepository. It turns Room entities into domain types,
// so entities never leave this file, and it runs "create a hike" as one transaction
// (trip + items, all or nothing).
package com.anthony.sentiero.data

import androidx.room.withTransaction
import com.anthony.sentiero.data.local.ChecklistDao
import com.anthony.sentiero.data.local.ChecklistItemEntity
import com.anthony.sentiero.data.local.SentieroDatabase
import com.anthony.sentiero.data.local.TripDao
import com.anthony.sentiero.data.local.TripEntity
import com.anthony.sentiero.domain.Difficulty
import com.anthony.sentiero.domain.Hike
import com.anthony.sentiero.domain.HikeSummary
import com.anthony.sentiero.domain.PackingItem
import com.anthony.sentiero.domain.templateFor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

class RoomTripRepository(
    private val db: SentieroDatabase,
    private val tripDao: TripDao,
    private val checklistDao: ChecklistDao,
) : TripRepository {
    override fun observeUpcoming(): Flow<List<HikeSummary>> =
        tripDao.observeUpcoming().map { rows ->
            rows.map { HikeSummary(it.trip.toHike(), it.packedCount, it.totalCount) }
        }

    override fun observeHike(id: String): Flow<Hike?> =
        tripDao.observeTrip(id).map { it?.toHike() }

    override fun observeItems(tripId: String): Flow<List<PackingItem>> =
        checklistDao.observeItems(tripId).map { items ->
            items.map { it.toPackingItem() }
        }

    /** Saves the hike and its starting packing list together. Returns the new hike's id. */
    override suspend fun createHike(routeName: String, date: LocalDate, difficulty: Difficulty): String {
        val trip = TripEntity(routeName = routeName.trim(), date = date, difficulty = difficulty)
        val items = templateFor(difficulty).mapIndexed { index, label ->
            ChecklistItemEntity(tripId = trip.id, label = label, position = index)
        }
        db.withTransaction {
            tripDao.insertTrip(trip)
            checklistDao.insertAll(items)
        }
        return trip.id
    }

    override suspend fun setPacked(itemId: String, packed: Boolean) =
        checklistDao.setPacked(itemId, packed)

    private fun TripEntity.toHike() = Hike(id, routeName, date, difficulty)

    private fun ChecklistItemEntity.toPackingItem() =
        PackingItem(id, label, isPacked, position, forgotten, carriedOver, addedAfterHike)
}

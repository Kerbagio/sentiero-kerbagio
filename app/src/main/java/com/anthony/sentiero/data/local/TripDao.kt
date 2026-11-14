// Data layer (Room): the SQL for the trips table. Reads return Flow, so Room re-emits
// automatically whenever the tables change; writes are suspend functions.
// Only RoomTripRepository calls this; ViewModels never touch DAOs.
package com.anthony.sentiero.data.local

import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/** A trip plus its packed / total counts, filled by one JOIN query. */
data class HikeSummaryRow(
    @Embedded val trip: TripEntity,
    val packedCount: Int,
    val totalCount: Int,
)

@Dao
interface TripDao {
    // One query for the whole list (no extra query per row).
    @Query(
        """
        SELECT t.*, COALESCE(SUM(i.is_packed), 0) AS packedCount, COUNT(i.id) AS totalCount
        FROM trips t LEFT JOIN checklist_items i ON i.trip_id = t.id
        WHERE t.completed_at IS NULL
        GROUP BY t.id
        ORDER BY t.date ASC, t.route_name ASC
        """
    )
    fun observeUpcoming(): Flow<List<HikeSummaryRow>>

    @Query("SELECT * FROM trips WHERE id = :id")
    fun observeTrip(id: String): Flow<TripEntity?>

    @Insert
    suspend fun insertTrip(trip: TripEntity)
}

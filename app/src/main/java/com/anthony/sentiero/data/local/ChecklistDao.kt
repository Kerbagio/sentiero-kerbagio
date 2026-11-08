// Data layer (Room): the SQL for packing items. setPacked writes the absolute value
// (true or false), never "flip it", so the last tap always wins.
package com.anthony.sentiero.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ChecklistDao {
    @Query("SELECT * FROM checklist_items WHERE trip_id = :tripId ORDER BY position ASC, id ASC")
    fun observeItems(tripId: String): Flow<List<ChecklistItemEntity>>

    @Insert
    suspend fun insertAll(items: List<ChecklistItemEntity>)

    @Query("UPDATE checklist_items SET is_packed = :packed WHERE id = :itemId")
    suspend fun setPacked(itemId: String, packed: Boolean)
}

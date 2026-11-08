// Data layer (Room): one packing item, owned by a trip. The foreign key with CASCADE
// means deleting a trip deletes its items. Unused v1 columns (forgotten, carried_over,
// added_after_hike) are here now so the schema never needs a migration.
package com.anthony.sentiero.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "checklist_items",
    foreignKeys = [
        ForeignKey(
            entity = TripEntity::class,
            parentColumns = ["id"],
            childColumns = ["trip_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("trip_id")],
)
data class ChecklistItemEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    @ColumnInfo(name = "trip_id") val tripId: String,
    val label: String,
    @ColumnInfo(name = "is_packed", defaultValue = "0") val isPacked: Boolean = false,
    val position: Int,
    @ColumnInfo(defaultValue = "0") val forgotten: Boolean = false,
    @ColumnInfo(name = "carried_over", defaultValue = "0") val carriedOver: Boolean = false,
    @ColumnInfo(name = "added_after_hike", defaultValue = "0") val addedAfterHike: Boolean = false,
)

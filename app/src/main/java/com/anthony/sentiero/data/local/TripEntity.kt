// Data layer (Room): one row of the "trips" table. It lives here because only the
// database code should know about columns; the UI uses domain.Hike instead.
// Every v1 column exists now (even unused ones) so no migration is needed later.
package com.anthony.sentiero.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.anthony.sentiero.domain.Difficulty
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

@Entity(tableName = "trips")
data class TripEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    @ColumnInfo(name = "route_name") val routeName: String,
    val date: LocalDate,                                   // stored as epochDay (Converters)
    val difficulty: Difficulty,
    @ColumnInfo(name = "area_label") val areaLabel: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    @ColumnInfo(defaultValue = "") val notes: String = "",
    @ColumnInfo(name = "completed_at") val completedAt: Instant? = null,   // epoch millis
    @ColumnInfo(name = "reminder_on", defaultValue = "0") val reminderOn: Boolean = false,
)

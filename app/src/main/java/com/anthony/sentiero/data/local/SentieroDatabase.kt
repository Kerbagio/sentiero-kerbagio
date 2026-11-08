// Data layer (Room): the database itself (file "sentiero.db", schema version 1).
// exportSchema writes app/schemas/.../1.json so the schema is visible and committed.
// Never use fallbackToDestructiveMigration: it would wipe saved hikes.
package com.anthony.sentiero.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [TripEntity::class, ChecklistItemEntity::class],
    version = 1,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class SentieroDatabase : RoomDatabase() {
    abstract fun tripDao(): TripDao
    abstract fun checklistDao(): ChecklistDao
}

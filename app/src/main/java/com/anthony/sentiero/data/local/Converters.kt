// Data layer (Room): SQLite only stores numbers and text, so these tell Room how to
// save Java time types. LocalDate becomes a day count, Instant becomes milliseconds.
package com.anthony.sentiero.data.local

import androidx.room.TypeConverter
import java.time.Instant
import java.time.LocalDate

class Converters {
    @TypeConverter fun fromLocalDate(d: LocalDate?): Long? = d?.toEpochDay()
    @TypeConverter fun toLocalDate(v: Long?): LocalDate? = v?.let(LocalDate::ofEpochDay)
    @TypeConverter fun fromInstant(i: Instant?): Long? = i?.toEpochMilli()
    @TypeConverter fun toInstant(v: Long?): Instant? = v?.let(Instant::ofEpochMilli)
}

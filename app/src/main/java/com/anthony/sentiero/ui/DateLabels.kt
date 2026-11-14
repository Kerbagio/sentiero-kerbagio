// UI layer: turns a date into the text shown on screen ("Sat 14 Nov", "in 7 days"). Formatting is
// a display concern, so it lives in ui/, shared by the Upcoming and Detail screens.
package com.anthony.sentiero.ui

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

private val SHORT_DATE = DateTimeFormatter.ofPattern("EEE d MMM", Locale.ENGLISH)

fun LocalDate.shortLabel(): String = format(SHORT_DATE)

/** How far a hike's date is from today. The screen turns it into words. */
sealed interface RelativeDay {
    data object Today : RelativeDay
    data object Tomorrow : RelativeDay
    data class InDays(val days: Int) : RelativeDay
    data object Passed : RelativeDay
}

fun relativeDay(date: LocalDate, today: LocalDate): RelativeDay {
    val days = ChronoUnit.DAYS.between(today, date).toInt()
    return when {
        days < 0 -> RelativeDay.Passed
        days == 0 -> RelativeDay.Today
        days == 1 -> RelativeDay.Tomorrow
        else -> RelativeDay.InDays(days)
    }
}

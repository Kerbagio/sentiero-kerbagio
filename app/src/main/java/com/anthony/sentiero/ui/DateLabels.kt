// UI layer: turns a date into the text shown on screen ("Sat 14 Nov"). Formatting is
// a display concern, so it lives in ui/, shared by the Upcoming and Detail screens.
package com.anthony.sentiero.ui

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val SHORT_DATE = DateTimeFormatter.ofPattern("EEE d MMM", Locale.ENGLISH)

fun LocalDate.shortLabel(): String = format(SHORT_DATE)

// Domain layer: plain Kotlin types the UI works with. No Android or Room imports here,
// so screens and ViewModels never depend on how the data is stored.
// The repository converts Room entities into these at its boundary.
package com.anthony.sentiero.domain

import java.time.LocalDate

/** Room stores this enum as TEXT by its name (EASY, MODERATE, HARD). */
enum class Difficulty { EASY, MODERATE, HARD }

/** "Moderate" instead of "MODERATE", for showing on screen. */
val Difficulty.label: String
    get() = name.lowercase().replaceFirstChar { it.uppercase() }

data class Hike(
    val id: String,
    val routeName: String,
    val date: LocalDate,
    val difficulty: Difficulty,
    val area: Area? = null, // null = no place picked, so no forecast
)

/** Where the forecast is fetched for: a picked place's label and coordinates (Spec 2). */
data class Area(val label: String, val latitude: Double, val longitude: Double)

/** One row on the Upcoming list: the hike plus how much of its list is packed. */
data class HikeSummary(val hike: Hike, val packedCount: Int, val totalCount: Int)

/** One row of a packing list. The flags are used from Spec 2 on (memory tag, journal). */
data class PackingItem(
    val id: String,
    val label: String,
    val isPacked: Boolean,
    val position: Int = 0,
    val forgotten: Boolean = false,
    val carriedOver: Boolean = false,
    val addedAfterHike: Boolean = false,
)

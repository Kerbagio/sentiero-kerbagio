// Domain rule: which packing items a new hike starts with. It lives in the domain layer
// because it is a pure decision (difficulty in, list out) with no storage or UI involved,
// so it is easy to explain and to unit-test later.
package com.anthony.sentiero.domain

private val BASE = listOf(
    "Water (2 L)", "Snacks", "Trail shoes", "Sun hat", "Sunglasses",
    "First-aid kit", "Phone + power bank", "ID + cash", "Small backpack", "Trash bag",
)

/** Easy = 10 items, Moderate = 12, Hard = 14. The order here is the order on screen. */
fun templateFor(difficulty: Difficulty): List<String> = BASE + when (difficulty) {
    Difficulty.EASY -> emptyList()
    Difficulty.MODERATE -> listOf("Lunch", "Extra layer")
    Difficulty.HARD -> listOf("Lunch", "Extra layer", "Headlamp", "Trekking poles")
}

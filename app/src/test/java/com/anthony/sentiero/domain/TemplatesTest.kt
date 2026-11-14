// JVM unit test for the domain rule templateFor: difficulty in, starting packing list out.
// It runs on the computer (no phone needed) because the rule is plain Kotlin with no Android code.
// Card: protects=difficulty decides the starting list exactly (CEO-2); fails_when=an item, the count
// or the order changes; no_claim=none beyond protects
package com.anthony.sentiero.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class TemplatesTest {

    private val base = listOf(
        "Water (2 L)", "Snacks", "Trail shoes", "Sun hat", "Sunglasses",
        "First-aid kit", "Phone + power bank", "ID + cash", "Small backpack", "Trash bag",
    )

    @Test
    fun eachDifficultyStartsWithTheRightNumberOfItems() {
        assertEquals(10, templateFor(Difficulty.EASY).size)
        assertEquals(12, templateFor(Difficulty.MODERATE).size)
        assertEquals(14, templateFor(Difficulty.HARD).size)
    }

    @Test
    fun everyDifficultyStartsWithTheBaseItemsInOrder() {
        for (difficulty in Difficulty.entries) {
            assertEquals(base, templateFor(difficulty).take(10))
        }
    }

    @Test
    fun extrasComeAfterTheBaseInOrder() {
        assertEquals(emptyList<String>(), templateFor(Difficulty.EASY).drop(10))
        assertEquals(listOf("Lunch", "Extra layer"), templateFor(Difficulty.MODERATE).drop(10))
        assertEquals(
            listOf("Lunch", "Extra layer", "Headlamp", "Trekking poles"),
            templateFor(Difficulty.HARD).drop(10),
        )
    }
}

// JVM test for the pure Place rules: the saved area label (decision 9) and the result subtitle.
// Card: protects=the label a hike shows for its area; fails_when="Tripoli, Tripoli" appears,
// the region is skipped when it differs, or a missing part leaves a stray comma; no_claim=none beyond protects
package com.anthony.sentiero.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class PlaceTest {

    @Test
    fun labelUsesRegionUnlessItRepeatsTheName() {
        assertEquals("Faraya, Keserwan-Jbeil", areaLabel("Faraya", "Keserwan-Jbeil", "Lebanon"))
        assertEquals("Tripoli, Libya", areaLabel("Tripoli", "tripoli", "Libya")) // case ignored
        assertEquals("Ehden, Lebanon", areaLabel("Ehden", null, "Lebanon"))
        assertEquals("Ehden", areaLabel("Ehden", null, null))
        val place = Place("Tripoli", "North Lebanon", "Lebanon", 34.43, 35.84)
        assertEquals(Area("Tripoli, North Lebanon", 34.43, 35.84), place.toArea())
    }

    @Test
    fun subtitleJoinsWhatIsThere() {
        assertEquals("North Lebanon, Lebanon", Place("Tripoli", "North Lebanon", "Lebanon", 0.0, 0.0).subtitle)
        assertEquals("Lebanon", Place("Ehden", null, "Lebanon", 0.0, 0.0).subtitle)
        assertEquals("", Place("Ehden", null, null, 0.0, 0.0).subtitle)
    }
}

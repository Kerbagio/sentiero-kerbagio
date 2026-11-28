// JVM test: each saved Open-Meteo response decodes with the app's own Json and DTOs.
// Card: protects=the DTOs match the real response shape (missing keys, extra keys, nulls);
// fails_when=a field is renamed, made non-null, or an unknown key breaks decoding;
// no_claim=the live API still sends this shape today
package com.anthony.sentiero.data.remote

import com.anthony.sentiero.fakes.decodeFixture
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class JsonFixturesTest {

    @Test
    fun noMatchHasNoResultsKey() {
        val dto = decodeFixture<GeocodingResponseDto>("geocoding_no_match.json")
        assertNull(dto.results)
    }

    @Test
    fun tripoliDecodesFivePlaces() {
        val dto = decodeFixture<GeocodingResponseDto>("geocoding_tripoli.json")
        val results = dto.results!!
        assertEquals(5, results.size)
        assertEquals("Tripoli", results[0].name)
        assertEquals("Libya", results[0].country)
    }

    @Test
    fun forecastOkDecodesOneDay() {
        val daily = decodeFixture<ForecastResponseDto>("forecast_ok.json").daily!!
        assertEquals(listOf("2026-11-14"), daily.time)
        assertEquals(listOf(19.3), daily.temperatureMax)
        assertEquals(listOf(12.1), daily.temperatureMin)
        assertEquals(listOf(70.0), daily.precipitationProbabilityMax)
        assertEquals(listOf(25.2), daily.windSpeedMax)
        assertEquals(listOf(4.15), daily.uvIndexMax)
    }

    @Test
    fun forecastNullsDecodeAsNull() {
        val daily = decodeFixture<ForecastResponseDto>("forecast_nulls.json").daily!!
        assertEquals(listOf<Double?>(null), daily.precipitationProbabilityMax)
        assertEquals(listOf<Double?>(null), daily.uvIndexMax)
        assertEquals(listOf(19.3), daily.temperatureMax)
    }
}

// Test doubles for the two Retrofit interfaces, so repository tests never touch the network.
// Each fake runs a suspend lambda the test passes in: return a fixture, throw an exception,
// or wait on a CompletableDeferred to control timing.
package com.anthony.sentiero.fakes

import com.anthony.sentiero.data.remote.ForecastApi
import com.anthony.sentiero.data.remote.ForecastResponseDto
import com.anthony.sentiero.data.remote.GeocodingApi
import com.anthony.sentiero.data.remote.GeocodingResponseDto
import com.anthony.sentiero.data.remote.OpenMeteoJson

class FakeGeocodingApi(
    private val respond: suspend (name: String) -> GeocodingResponseDto,
) : GeocodingApi {
    override suspend fun search(name: String, count: Int, language: String, format: String) = respond(name)
}

class FakeForecastApi(
    private val respond: suspend (date: String) -> ForecastResponseDto,
) : ForecastApi {
    override suspend fun daily(
        latitude: Double,
        longitude: Double,
        daily: String,
        timezone: String,
        startDate: String,
        endDate: String,
    ) = respond(startDate)
}

/** Reads a file from app/src/test/resources as text. */
fun fixture(name: String): String =
    requireNotNull(FakeGeocodingApi::class.java.classLoader!!.getResource(name)) { "No fixture $name" }.readText()

/** Decodes a fixture with the same Json the app uses. */
inline fun <reified T> decodeFixture(name: String): T = OpenMeteoJson.decodeFromString<T>(fixture(name))

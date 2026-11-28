// Network layer: the JSON shapes Open-Meteo sends back, decoded by kotlinx.serialization.
// Every field that can be missing is nullable or has a default, so an odd response
// becomes "no data" instead of a crash. The repository turns these into domain types.
package com.anthony.sentiero.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/** The one Json the app decodes weather with (tests use it too). Unknown keys are skipped. */
val OpenMeteoJson = Json { ignoreUnknownKeys = true }

/** No "results" key at all when nothing matches (checked on the live API). */
@Serializable
data class GeocodingResponseDto(val results: List<PlaceDto>? = null)

@Serializable
data class PlaceDto(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val admin1: String? = null, // region, e.g. "Keserwan-Jbeil"
    val country: String? = null,
)

@Serializable
data class ForecastResponseDto(val daily: DailyDto? = null)

/** One list per field, all indexed like [time] ("2026-11-14"). Any value can be null. */
@Serializable
data class DailyDto(
    val time: List<String> = emptyList(),
    @SerialName("temperature_2m_max") val temperatureMax: List<Double?> = emptyList(),
    @SerialName("temperature_2m_min") val temperatureMin: List<Double?> = emptyList(),
    @SerialName("precipitation_probability_max") val precipitationProbabilityMax: List<Double?> = emptyList(),
    @SerialName("wind_speed_10m_max") val windSpeedMax: List<Double?> = emptyList(),
    @SerialName("uv_index_max") val uvIndexMax: List<Double?> = emptyList(),
)

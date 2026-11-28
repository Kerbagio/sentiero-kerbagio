// Network layer: the two Open-Meteo endpoints as Retrofit interfaces (free, no API key).
// Retrofit writes the HTTP code from these annotations. Only OpenMeteoWeatherRepository
// calls them, so screens never see URLs, HTTP errors or JSON.
package com.anthony.sentiero.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

/** Place search. Base URL https://geocoding-api.open-meteo.com/ */
interface GeocodingApi {
    @GET("v1/search")
    suspend fun search(
        @Query("name") name: String,
        @Query("count") count: Int = 5,
        @Query("language") language: String = "en",
        @Query("format") format: String = "json",
    ): GeocodingResponseDto
}

/** Daily forecast. Base URL https://api.open-meteo.com/ */
interface ForecastApi {
    @GET("v1/forecast")
    suspend fun daily(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        // encoded = true keeps the commas as commas instead of %2C.
        @Query("daily", encoded = true) daily: String = DAILY_FIELDS,
        // "auto" = the area's own time zone, so the row is the hike's local day (R3-4).
        @Query("timezone") timezone: String = "auto",
        @Query("start_date") startDate: String,
        @Query("end_date") endDate: String,
    ): ForecastResponseDto
}

const val DAILY_FIELDS =
    "temperature_2m_max,temperature_2m_min,precipitation_probability_max,wind_speed_10m_max,uv_index_max"

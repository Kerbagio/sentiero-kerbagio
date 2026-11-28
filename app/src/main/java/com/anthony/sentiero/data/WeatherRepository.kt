// Data layer: the single door to weather. ViewModels depend on this interface, not on
// Retrofit, so tests can pass a fake and the provider could change without touching the UI.
package com.anthony.sentiero.data

import com.anthony.sentiero.domain.Area
import com.anthony.sentiero.domain.ForecastResult
import com.anthony.sentiero.domain.PlaceSearchResult
import java.time.LocalDate

interface WeatherRepository {
    /** Up to 5 places in the API's order, NoMatch, or Failed. Never throws (except cancellation). */
    suspend fun searchPlaces(query: String): PlaceSearchResult

    /** The forecast for [date] at [area], NoRowForDate, or Failed. Never throws (except cancellation). */
    suspend fun dailyForecast(area: Area, date: LocalDate): ForecastResult
}

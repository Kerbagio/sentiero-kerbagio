// Test double for WeatherRepository, used by state-holder and ViewModel tests.
// The test passes a suspend lambda per call, so it can answer at once, fail,
// or wait on a CompletableDeferred to control the order answers arrive in.
package com.anthony.sentiero.fakes

import com.anthony.sentiero.data.WeatherRepository
import com.anthony.sentiero.domain.Area
import com.anthony.sentiero.domain.ForecastResult
import com.anthony.sentiero.domain.PlaceSearchResult
import java.time.LocalDate

class FakeWeatherRepository(
    private val search: suspend (query: String) -> PlaceSearchResult = { PlaceSearchResult.NoMatch },
    private val forecast: suspend (area: Area, date: LocalDate) -> ForecastResult = { _, _ -> ForecastResult.NoRowForDate },
) : WeatherRepository {
    /** Every query that reached the repository, in order. */
    val searchedQueries = mutableListOf<String>()

    override suspend fun searchPlaces(query: String): PlaceSearchResult {
        searchedQueries += query
        return search(query)
    }

    override suspend fun dailyForecast(area: Area, date: LocalDate): ForecastResult = forecast(area, date)
}

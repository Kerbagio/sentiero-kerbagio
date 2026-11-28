// Domain layer: weather as the app understands it, with no Retrofit or JSON types.
// The repository returns these sealed results, so a ViewModel handles every case
// (found / nothing / failed) with a `when` and never sees an exception.
package com.anthony.sentiero.domain

import java.time.LocalDate

/** One place-search result. [region] is Open-Meteo's admin1. */
data class Place(
    val name: String,
    val region: String?,
    val country: String?,
    val latitude: Double,
    val longitude: Double,
) {
    /** The second line of a result row, e.g. "Keserwan-Jbeil, Lebanon". */
    val subtitle: String get() = listOfNotNull(region, country).joinToString(", ")

    fun toArea() = Area(areaLabel(name, region, country), latitude, longitude)
}

/** Decision 9: "Name, Region", unless the region repeats the name; then "Name, Country". */
fun areaLabel(name: String, region: String?, country: String?) = when {
    region != null && !region.equals(name, ignoreCase = true) -> "$name, $region"
    country != null -> "$name, $country"
    else -> name
}

/** One day's forecast, rounded to whole numbers. Null = Open-Meteo had no value. */
data class DailyForecast(
    val date: LocalDate,
    val minC: Int?,
    val maxC: Int?,
    val rainPct: Int?,
    val windKmh: Int?,
    val uv: Int?,
)

/** OFFLINE gets its own message ("You're offline."); everything else is OTHER. */
enum class WeatherFailure { OFFLINE, OTHER }

sealed interface PlaceSearchResult {
    data class Found(val places: List<Place>) : PlaceSearchResult
    data object NoMatch : PlaceSearchResult
    data class Failed(val failure: WeatherFailure) : PlaceSearchResult
}

sealed interface ForecastResult {
    data class Day(val forecast: DailyForecast) : ForecastResult
    data object NoRowForDate : ForecastResult
    data class Failed(val failure: WeatherFailure) : ForecastResult
}

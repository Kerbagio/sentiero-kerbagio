// Data layer: calls Open-Meteo through the Retrofit interfaces, maps DTOs to domain types,
// and turns each kind of failure into a result. Exceptions are caught by name, never as a
// catch-all, so a programming bug still crashes loudly and cancellation passes through.
package com.anthony.sentiero.data

import android.util.Log
import com.anthony.sentiero.data.remote.ForecastApi
import com.anthony.sentiero.data.remote.GeocodingApi
import com.anthony.sentiero.data.remote.PlaceDto
import com.anthony.sentiero.domain.Area
import com.anthony.sentiero.domain.DailyForecast
import com.anthony.sentiero.domain.ForecastResult
import com.anthony.sentiero.domain.Place
import com.anthony.sentiero.domain.PlaceSearchResult
import com.anthony.sentiero.domain.WeatherFailure
import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import java.io.IOException
import java.net.ConnectException
import java.net.UnknownHostException
import java.time.LocalDate
import kotlin.math.roundToInt

class OpenMeteoWeatherRepository(
    private val geocodingApi: GeocodingApi,
    private val forecastApi: ForecastApi,
) : WeatherRepository {

    override suspend fun searchPlaces(query: String): PlaceSearchResult = fetch(
        request = "search name=$query",
        failed = { PlaceSearchResult.Failed(it) },
        describe = { result ->
            when (result) {
                is PlaceSearchResult.Found -> "Found(${result.places.size})"
                PlaceSearchResult.NoMatch -> "NoMatch"
                is PlaceSearchResult.Failed -> "Failed(${result.failure})"
            }
        },
    ) {
        val places = geocodingApi.search(query).results.orEmpty().take(5).map { it.toPlace() }
        if (places.isEmpty()) PlaceSearchResult.NoMatch else PlaceSearchResult.Found(places)
    }

    override suspend fun dailyForecast(area: Area, date: LocalDate): ForecastResult = fetch(
        request = "forecast lat=${area.latitude} lon=${area.longitude} date=$date",
        failed = { ForecastResult.Failed(it) },
        // Open-Meteo answers 400 for a date outside its forecast range.
        onBadRequest = ForecastResult.NoRowForDate,
        describe = { result ->
            when (result) {
                is ForecastResult.Day -> "Day"
                ForecastResult.NoRowForDate -> "NoRowForDate"
                is ForecastResult.Failed -> "Failed(${result.failure})"
            }
        },
    ) {
        val day = date.toString() // "2026-11-14": start and end are both the hike date
        val daily = forecastApi.daily(
            latitude = area.latitude,
            longitude = area.longitude,
            startDate = day,
            endDate = day,
        ).daily
        val index = daily?.time?.indexOf(day) ?: -1
        if (daily == null || index == -1) {
            ForecastResult.NoRowForDate
        } else {
            ForecastResult.Day(
                DailyForecast(
                    date = date,
                    minC = daily.temperatureMin.valueAt(index),
                    maxC = daily.temperatureMax.valueAt(index),
                    rainPct = daily.precipitationProbabilityMax.valueAt(index),
                    windKmh = daily.windSpeedMax.valueAt(index),
                    uv = daily.uvIndexMax.valueAt(index),
                ),
            )
        }
    }

    /**
     * Runs one request, maps its failures in the spec's order, and logs one line:
     * the request, the outcome and (on failure) the exception's simple name.
     */
    private suspend fun <T> fetch(
        request: String,
        failed: (WeatherFailure) -> T,
        describe: (T) -> String,
        onBadRequest: T? = null,
        call: suspend () -> T,
    ): T {
        var error: Exception? = null
        val result = try {
            call()
        } catch (e: UnknownHostException) {
            error = e
            failed(WeatherFailure.OFFLINE)
        } catch (e: ConnectException) {
            error = e
            failed(WeatherFailure.OFFLINE)
        } catch (e: IOException) { // includes SocketTimeoutException
            error = e
            failed(WeatherFailure.OTHER)
        } catch (e: HttpException) {
            error = e
            if (e.code() == 400 && onBadRequest != null) onBadRequest else failed(WeatherFailure.OTHER)
        } catch (e: SerializationException) {
            error = e
            failed(WeatherFailure.OTHER)
        }
        val errorName = error?.let { " ${it.javaClass.simpleName}" }.orEmpty()
        Log.i(TAG, "$request -> ${describe(result)}$errorName")
        return result
    }

    private companion object {
        const val TAG = "Sentiero/Weather"
    }
}

private fun PlaceDto.toPlace() = Place(name, admin1, country, latitude, longitude)

/** null stays null; 12.5 rounds to 13 and -2.5 to -2 (half rounds up). */
private fun List<Double?>.valueAt(index: Int): Int? = getOrNull(index)?.roundToInt()

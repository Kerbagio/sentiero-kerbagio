// JVM test for OpenMeteoWeatherRepository with fake APIs: mapping, rounding and failure classes.
// Card: protects=every API outcome becomes the right result (Found / NoMatch / Day / NoRowForDate /
// Failed OFFLINE or OTHER) and values round as the panel shows them; fails_when=an exception escapes,
// offline is reported as OTHER (or the reverse), order changes, or a null becomes 0;
// no_claim=real network behaviour (checked by hand in airplane mode)
package com.anthony.sentiero.data

import com.anthony.sentiero.data.remote.DailyDto
import com.anthony.sentiero.data.remote.ForecastResponseDto
import com.anthony.sentiero.data.remote.GeocodingResponseDto
import com.anthony.sentiero.domain.Area
import com.anthony.sentiero.domain.DailyForecast
import com.anthony.sentiero.domain.ForecastResult
import com.anthony.sentiero.domain.PlaceSearchResult
import com.anthony.sentiero.domain.WeatherFailure
import com.anthony.sentiero.fakes.FakeForecastApi
import com.anthony.sentiero.fakes.FakeGeocodingApi
import com.anthony.sentiero.fakes.decodeFixture
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.SerializationException
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.time.LocalDate

class WeatherRepositoryTest {

    private val area = Area("Faraya, Keserwan-Jbeil", 34.0, 35.75)
    private val date = LocalDate.of(2026, 11, 14)

    private fun searchRepo(respond: suspend (String) -> GeocodingResponseDto) =
        OpenMeteoWeatherRepository(FakeGeocodingApi(respond), FakeForecastApi { error("not used") })

    private fun forecastRepo(respond: suspend (String) -> ForecastResponseDto) =
        OpenMeteoWeatherRepository(FakeGeocodingApi { error("not used") }, FakeForecastApi(respond))

    private fun httpError(code: Int) = HttpException(Response.error<Any>(code, "".toResponseBody(null)))

    @Test
    fun noResultsKeyIsNoMatch() = runTest {
        val repo = searchRepo { decodeFixture("geocoding_no_match.json") }
        assertEquals(PlaceSearchResult.NoMatch, repo.searchPlaces("zzqx"))
    }

    @Test
    fun tripoliGivesFivePlacesInApiOrder() = runTest {
        val repo = searchRepo { decodeFixture("geocoding_tripoli.json") }
        val places = (repo.searchPlaces("Tripoli") as PlaceSearchResult.Found).places
        assertEquals(listOf("Libya", "Lebanon", "Greece", "United States", "Italy"), places.map { it.country })
        assertEquals("North Lebanon", places[1].region)
    }

    @Test
    fun noHostAndRefusedConnectionAreOffline() = runTest {
        val offline = PlaceSearchResult.Failed(WeatherFailure.OFFLINE)
        assertEquals(offline, searchRepo { throw UnknownHostException("geocoding-api.open-meteo.com") }.searchPlaces("Faraya"))
        assertEquals(offline, searchRepo { throw ConnectException("refused") }.searchPlaces("Faraya"))
        assertEquals(
            ForecastResult.Failed(WeatherFailure.OFFLINE),
            forecastRepo { throw UnknownHostException("api.open-meteo.com") }.dailyForecast(area, date),
        )
    }

    @Test
    fun timeoutServerErrorAndBadJsonAreOther() = runTest {
        val other = PlaceSearchResult.Failed(WeatherFailure.OTHER)
        assertEquals(other, searchRepo { throw SocketTimeoutException("timeout") }.searchPlaces("Faraya"))
        assertEquals(other, searchRepo { throw httpError(500) }.searchPlaces("Faraya"))
        assertEquals(other, searchRepo { throw SerializationException("bad json") }.searchPlaces("Faraya"))
        // A 400 is OTHER for search; only the forecast treats it as "no row".
        assertEquals(other, searchRepo { throw httpError(400) }.searchPlaces("Faraya"))
    }

    @Test
    fun forecastBadRequestAndMissingDateAreNoRowForDate() = runTest {
        assertEquals(ForecastResult.NoRowForDate, forecastRepo { throw httpError(400) }.dailyForecast(area, date))
        val otherDay = forecastRepo { decodeFixture("forecast_ok.json") }
        assertEquals(ForecastResult.NoRowForDate, otherDay.dailyForecast(area, date.plusDays(1)))
        assertEquals(ForecastResult.NoRowForDate, forecastRepo { ForecastResponseDto(daily = null) }.dailyForecast(area, date))
    }

    @Test
    fun valuesRoundHalfUpAndNullsStayNull() = runTest {
        assertEquals(
            ForecastResult.Day(DailyForecast(date, minC = 12, maxC = 19, rainPct = 70, windKmh = 25, uv = 4)),
            forecastRepo { decodeFixture("forecast_ok.json") }.dailyForecast(area, date),
        )
        assertEquals(
            ForecastResult.Day(DailyForecast(date, minC = 12, maxC = 19, rainPct = null, windKmh = 25, uv = null)),
            forecastRepo { decodeFixture("forecast_nulls.json") }.dailyForecast(area, date),
        )
        val halves = DailyDto(
            time = listOf("2026-11-14"),
            temperatureMax = listOf(12.5),
            temperatureMin = listOf(-2.5),
        )
        val day = (forecastRepo { ForecastResponseDto(halves) }.dailyForecast(area, date) as ForecastResult.Day).forecast
        assertEquals(13, day.maxC)
        assertEquals(-2, day.minC)
        assertEquals(null, day.windKmh) // a list shorter than time counts as null
    }
}

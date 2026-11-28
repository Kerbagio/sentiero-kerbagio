// App wiring (manual dependency injection): builds the database, the HTTP client and the
// repositories once, for the whole app. ViewModel factories read what they need from here,
// so no screen ever creates its own database or network client.
package com.anthony.sentiero

import android.content.Context
import androidx.room.Room
import com.anthony.sentiero.data.OpenMeteoWeatherRepository
import com.anthony.sentiero.data.RoomTripRepository
import com.anthony.sentiero.data.TripRepository
import com.anthony.sentiero.data.WeatherRepository
import com.anthony.sentiero.data.local.SentieroDatabase
import com.anthony.sentiero.data.remote.ForecastApi
import com.anthony.sentiero.data.remote.GeocodingApi
import com.anthony.sentiero.data.remote.OpenMeteoJson
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.time.Clock

class AppContainer(context: Context) {
    // One clock for the whole app; tests pass a fixed one so "today" never changes under them.
    val clock: Clock = Clock.systemDefaultZone()

    private val db = Room.databaseBuilder(context, SentieroDatabase::class.java, "sentiero.db").build()

    val tripRepository: TripRepository = RoomTripRepository(db, db.tripDao(), db.checklistDao())

    // One HTTP client (default 10 s timeouts) shared by both Open-Meteo hosts.
    private val httpClient = OkHttpClient()
    private val jsonConverter = OpenMeteoJson.asConverterFactory("application/json".toMediaType())

    private fun retrofit(baseUrl: String): Retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(httpClient)
        .addConverterFactory(jsonConverter)
        .build()

    val weatherRepository: WeatherRepository = OpenMeteoWeatherRepository(
        geocodingApi = retrofit("https://geocoding-api.open-meteo.com/").create(GeocodingApi::class.java),
        forecastApi = retrofit("https://api.open-meteo.com/").create(ForecastApi::class.java),
    )
}

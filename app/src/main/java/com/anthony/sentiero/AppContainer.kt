// App wiring (manual dependency injection): builds the database and the repository
// once, for the whole app. ViewModel factories read what they need from here,
// so no screen ever creates its own database.
package com.anthony.sentiero

import android.content.Context
import androidx.room.Room
import com.anthony.sentiero.data.RoomTripRepository
import com.anthony.sentiero.data.TripRepository
import com.anthony.sentiero.data.local.SentieroDatabase
import java.time.Clock

class AppContainer(context: Context) {
    // One clock for the whole app; tests pass a fixed one so "today" never changes under them.
    val clock: Clock = Clock.systemDefaultZone()

    private val db = Room.databaseBuilder(context, SentieroDatabase::class.java, "sentiero.db").build()

    val tripRepository: TripRepository = RoomTripRepository(db, db.tripDao(), db.checklistDao())
}

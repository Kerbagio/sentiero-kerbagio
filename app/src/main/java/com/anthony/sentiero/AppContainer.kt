// App wiring (manual dependency injection): builds the database and the repository
// once, for the whole app. ViewModel factories read what they need from here,
// so no screen ever creates its own database.
package com.anthony.sentiero

import android.content.Context
import androidx.room.Room
import com.anthony.sentiero.data.RoomTripRepository
import com.anthony.sentiero.data.TripRepository
import com.anthony.sentiero.data.local.SentieroDatabase

class AppContainer(context: Context) {
    private val db = Room.databaseBuilder(context, SentieroDatabase::class.java, "sentiero.db").build()

    val tripRepository: TripRepository = RoomTripRepository(db, db.tripDao(), db.checklistDao())
}

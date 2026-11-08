// App entry point: Android creates this Application object before any screen, and it
// lives as long as the process. That makes it the right owner for the AppContainer.
// Registered in AndroidManifest.xml with android:name=".SentieroApp".
package com.anthony.sentiero

import android.app.Application

class SentieroApp : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}

// UI entry point: the single Activity. It only sets the theme and hands control to
// the navigation graph; every screen is a composable inside SentieroNavHost.
package com.anthony.sentiero

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.anthony.sentiero.ui.nav.SentieroNavHost
import com.anthony.sentiero.ui.theme.SentieroTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SentieroTheme {
                SentieroNavHost()
            }
        }
    }
}

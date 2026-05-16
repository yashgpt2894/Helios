package com.helios

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.helios.core.nav.HeliosNavGraph
import com.helios.core.ui.theme.HeliosTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * MainActivity: sets HeliosTheme + NavHost, handles deep-link intents + runtime permissions.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    companion object {
        private const val LOCATION_PERMISSION_REQUEST = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Request location permission if not granted
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    LOCATION_PERMISSION_REQUEST
                )
            }
        }

        setContent {
            HeliosTheme(darkTheme = true) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    HeliosNavGraph()
                }
            }
        }
    }
}

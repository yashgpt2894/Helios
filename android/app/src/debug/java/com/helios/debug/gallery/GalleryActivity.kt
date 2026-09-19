package com.helios.debug.gallery

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.helios.core.data.fixture.FixtureScenario

/**
 * Debug-only host for [ComponentGalleryScreen].
 *
 * It exists so the gallery can be opened on a device and captured as a screenshot without
 * touching navigation or `MainActivity`. It is declared in `src/debug/AndroidManifest.xml`,
 * so it cannot exist in a release build.
 *
 * adb shell am start -n com.helios.app/com.helios.debug.gallery.GalleryActivity \
 *   --ei page 3 --ez dark true --es scenario OFFLINE
 */
class GalleryActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val page = intent?.getIntExtra(EXTRA_PAGE, 0) ?: 0
        val dark = intent?.getBooleanExtra(EXTRA_DARK, true) ?: true
        val scenario = intent?.getStringExtra(EXTRA_SCENARIO)
            ?.let { name -> FixtureScenario.entries.firstOrNull { it.name == name } }
            ?: FixtureScenario.LIVE
        setContent {
            ComponentGalleryScreen(page = page, initialScenario = scenario, darkTheme = dark)
        }
    }

    companion object {
        const val EXTRA_PAGE = "page"
        const val EXTRA_DARK = "dark"
        const val EXTRA_SCENARIO = "scenario"
    }
}

package com.helios.debug.screens

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.helios.core.data.fixture.FixtureScenario
import com.helios.core.data.fixture.FixtureServicesFamily
import com.helios.core.data.service.Loadable
import com.helios.core.domain.model.SolarTelemetry
import com.helios.core.ui.theme.HeliosTheme
import com.helios.feature.battery.BatteryScreen
import com.helios.feature.dashboard.DashboardActions
import com.helios.feature.dashboard.DashboardFixtures
import com.helios.feature.dashboard.DashboardScreen
import com.helios.feature.dashboard.DashboardTab
import com.helios.feature.dashboard.HeliosMotionSettings
import com.helios.feature.dashboard.ReadingState
import com.helios.feature.insights.InsightsScreen
import com.helios.feature.production.ProductionActions
import com.helios.feature.production.ProductionFixtures
import com.helios.feature.production.ProductionScreen
import com.helios.feature.settings.SettingsScreen
import kotlinx.coroutines.delay

/**
 * Debug-only host for the two live screens, so they can be opened in one declared state and
 * captured.
 *
 * It exists for the same reason [com.helios.debug.gallery.GalleryActivity] does: a screenshot
 * of a state has to be reproducible, and the theme and the scenario have to be chosen by the
 * capture rather than by hand. It is declared in `src/debug/AndroidManifest.xml`, so it cannot
 * exist in a release build, and it does not touch navigation or `MainActivity`.
 *
 * What a host may and may not do: this one passes only the handlers it can really perform —
 * switching destination, reading a half hour on the curve, selecting a week day, and a retry
 * that calls the fixture service's `refresh()` (which re-reads the current scenario; the
 * fixture contract does not invent a recovered link). The share sheet and the connection
 * sheet belong to later steps of this plan, so their controls are left absent instead of
 * being wired to a handler that would claim a result.
 *
 * adb shell am start -n com.helios.app/com.helios.debug.screens.ScreenCaptureActivity \
 *   --es screen dashboard --ez dark false --es scenario OFFLINE
 */
class ScreenCaptureActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val screen = intent?.getStringExtra(EXTRA_SCREEN) ?: SCREEN_DASHBOARD
        val dark = intent?.getBooleanExtra(EXTRA_DARK, true) ?: true
        val scenario = intent?.getStringExtra(EXTRA_SCENARIO)
            ?.let { name -> FixtureScenario.entries.firstOrNull { it.name == name } }
            ?: FixtureScenario.LIVE
        val reduceMotion = intent?.getBooleanExtra(EXTRA_REDUCE_MOTION, false) ?: false
        val simulated = intent?.getBooleanExtra(EXTRA_SIMULATED, true) ?: true
        val night = intent?.getBooleanExtra(EXTRA_NIGHT, false) ?: false
        val selectedHour = intent?.getDoubleExtra(EXTRA_SELECTED_HOUR, -1.0) ?: -1.0
        val fixedClock = System.currentTimeMillis()

        setContent {
            ScreenHost(
                startScreen = screen,
                scenario = scenario,
                dark = dark,
                reduceMotion = reduceMotion,
                simulated = simulated,
                night = night,
                initialSelectedHour = if (selectedHour >= 0.0) selectedHour else null,
                clockMs = fixedClock
            )
        }
    }

    companion object {
        const val EXTRA_SCREEN = "screen"
        const val EXTRA_DARK = "dark"
        const val EXTRA_SCENARIO = "scenario"
        const val EXTRA_NIGHT = "night"
        const val EXTRA_SIMULATED = "simulated"
        const val EXTRA_REDUCE_MOTION = "reduceMotion"
        const val EXTRA_SELECTED_HOUR = "selectedHour"

        const val SCREEN_DASHBOARD = "dashboard"
        const val SCREEN_PRODUCTION = "production"
    }
}

@Composable
private fun ScreenHost(
    startScreen: String,
    scenario: FixtureScenario,
    dark: Boolean,
    reduceMotion: Boolean,
    simulated: Boolean,
    night: Boolean,
    initialSelectedHour: Double?,
    clockMs: Long
) {
    var tab by remember {
        mutableIntStateOf(
            if (startScreen == ScreenCaptureActivity.SCREEN_PRODUCTION) DashboardTab.SOLAR else DashboardTab.HOME
        )
    }
    var selectedHour by remember { mutableStateOf(initialSelectedHour) }
    var selectedWeek by remember { mutableStateOf<Int?>(null) }
    var refreshed by remember { mutableStateOf<Loadable<SolarTelemetry>?>(null) }
    var retryToken by remember { mutableIntStateOf(0) }

    val motion = if (reduceMotion) HeliosMotionSettings.ReduceMotion else HeliosMotionSettings.Default

    // A retry issues the fixture service's own refresh, which re-reads the current scenario.
    // It is the same call the device adapter implements, so the button is not a stage prop.
    LaunchedEffect(retryToken) {
        if (retryToken == 0) return@LaunchedEffect
        delay(150)
        refreshed = FixtureServicesFamily.INSTANCE.telemetry.refresh()
    }

    HeliosTheme(darkTheme = dark) {
        Surface(modifier = Modifier.fillMaxSize()) {
            when (tab) {
                DashboardTab.SOLAR -> ProductionScreen(
                    state = (if (night) {
                            ProductionFixtures.nightState(scenario, clockMs, simulated)
                        } else {
                            ProductionFixtures.state(
                                scenario = scenario,
                                nowMs = clockMs,
                                selectedT = selectedHour,
                                selectedWeekIndex = selectedWeek,
                                selectedTab = DashboardTab.SOLAR,
                                simulated = simulated
                            )
                        }).copy(selectedT = selectedHour, selectedWeekIndex = selectedWeek)
                            .withRefresh(refreshed),
                    actions = ProductionActions(
                        onRetry = { retryToken++ },
                        onNavigate = { tab = it },
                        onSelectHour = { selectedHour = it },
                        onSelectWeekDay = { selectedWeek = if (selectedWeek == it) null else it }
                    ),
                    motion = motion
                )

                DashboardTab.HOME -> DashboardScreen(
                    state = (if (night) {
                            DashboardFixtures.nightState(scenario, clockMs, DashboardTab.HOME, simulated)
                        } else {
                            DashboardFixtures.state(
                                scenario = scenario,
                                nowMs = clockMs,
                                selectedTab = DashboardTab.HOME,
                                simulated = simulated
                            )
                        }).withRefresh(refreshed),
                    actions = DashboardActions(
                        onRetry = { retryToken++ },
                        onNavigate = { tab = it },
                        onOpenProduction = { tab = DashboardTab.SOLAR }
                    ),
                    motion = motion
                )

                DashboardTab.INSIGHTS -> InsightsScreen()
                DashboardTab.BATTERY -> BatteryScreen()
                else -> SettingsScreen()
            }
        }
    }
}

private fun com.helios.feature.dashboard.DashboardState.withRefresh(
    refreshed: Loadable<SolarTelemetry>?
) = if (refreshed == null) this else copy(reading = ReadingState(refreshed, simulated = true))

private fun com.helios.feature.production.ProductionState.withRefresh(
    refreshed: Loadable<SolarTelemetry>?
) = if (refreshed == null) this else copy(reading = ReadingState(refreshed, simulated = true))

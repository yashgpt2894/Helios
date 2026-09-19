# Step 6 capture harness (temporary, not committed)

The app's own navigation belongs to another step of this plan, so today the running app
reaches only the landing route. To capture one declared screen state per screenshot, this
step used a temporary host for the duration of the capture run and then removed it. Nothing
outside `feature/landing/**` was touched, and the committed tree contains no trace of it.

The temporary change was:

1. `feature/landing/CaptureHost.kt` (new file, deleted after the run) - a composable that
   reads intent extras (`surface`, `scenario`, `dark`, `demo`, `step`, `section`, `brand`,
   `ageMs`, `failure`), selects the fixture scenario, and renders one surface:
   `OnboardingScreen`, `SettingsScreen` or `SharedScreen`. The `failure` extra wraps the
   fixture connection service so one failure class (for example F2 timeout) can be forced.
2. `LandingScreen.kt` rendered `CaptureHostScreen()` instead of `OnboardingScreen(...)`.

Neither change survives in the commit: `git status` after the run shows only the feature
sources. The exact harness text is in the repository history of this step's evidence:

```kotlin
package com.helios.feature.landing

import android.app.Activity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.helios.core.data.fixture.FixtureData
import com.helios.core.data.fixture.FixtureScenario
import com.helios.core.data.fixture.FixtureServicesFamily
import com.helios.core.data.service.AppServices
import com.helios.core.data.service.ConnectionService
import com.helios.core.data.service.FailureKind
import com.helios.core.data.service.Loadable
import com.helios.core.domain.model.ConnectionConfig
import com.helios.core.ui.theme.HeliosTheme
import com.helios.feature.settings.SettingsScreen
import com.helios.feature.settings.SettingsSection
import com.helios.feature.shared.SharedScreen

/**
 * TEMPORARY capture host. Not part of the deliverable: it is deleted before this step is
 * committed, and the exact text is recorded in `.mobile-work/step6-capture-harness.md`.
 *
 * Why it exists: the app's own navigation belongs to another step of the plan, so today the
 * landing route is the only surface the running app reaches. This host renders one declared
 * surface in one declared state, driven by intent extras, so a capture is reproducible
 * instead of hand-driven. It does not change navigation, the manifest, or any file outside
 * this feature package.
 */
@Composable
fun CaptureHostScreen() {
    val activity = LocalContext.current as? Activity
    val intent = activity?.intent

    val surface = intent?.getStringExtra("surface") ?: "onboarding"
    val scenarioName = intent?.getStringExtra("scenario")
    val scenario = scenarioName?.let { name -> FixtureScenario.entries.firstOrNull { it.name == name } }
        ?: FixtureScenario.LIVE
    val dark = intent?.getBooleanExtra("dark", true) ?: true
    val demo = intent?.getBooleanExtra("demo", true) ?: true
    val stepName = intent?.getStringExtra("step")
    val sectionName = intent?.getStringExtra("section")
    val brandId = intent?.getStringExtra("brand")
    val ageMs = intent?.getLongExtra("ageMs", 0L) ?: 0L
    val failureName = intent?.getStringExtra("failure")

    val services = remember {
        val base = FixtureServicesFamily.INSTANCE
        base.select(scenario)
        base.setDemoMode(demo)
        wrap(base, failureName)
    }

    HeliosTheme(darkTheme = dark) {
        Surface(modifier = Modifier.fillMaxSize()) {
            when (surface) {
                "settings" -> SettingsScreen(
                    services = services,
                    initialSection = sectionName
                        ?.let { name -> SettingsSection.entries.firstOrNull { it.name == name } }
                        ?: SettingsSection.ROOT
                )

                "shared" -> {
                    val payload = FixtureData.snapshotPayload(nowMs = System.currentTimeMillis() - ageMs)
                        .copy(br = if (brandId == null || brandId == "helios") null else brandId)
                    val encoded = if (surface == "shared-invalid") {
                        "truncated-payload"
                    } else {
                        services.share.encode(payload)
                    }
                    SharedScreen(
                        encodedPayload = encoded,
                        services = services,
                        onTryHelios = {}
                    )
                }

                "shared-invalid" -> SharedScreen(
                    encodedPayload = "truncated-payload",
                    services = services,
                    onTryHelios = {}
                )

                else -> OnboardingScreen(
                    services = services,
                    onFinished = {},
                    initialStep = stepName
                        ?.let { name -> OnboardingStep.entries.firstOrNull { it.name == name } }
                        ?: OnboardingStep.WELCOME
                )
            }
        }
    }
}

/** One failure class forced for a capture, so the unreachable-inverter path is reproducible. */
private fun wrap(base: AppServices, failureName: String?): AppServices {
    val kind = failureName?.let { name -> FailureKind.entries.firstOrNull { it.name == name } } ?: return base
    return object : AppServices by base {
        override val connection: ConnectionService = object : ConnectionService by base.connection {
            override suspend fun test(candidate: ConnectionConfig): Loadable<com.helios.core.data.service.ConnectionSnapshot> =
                FixtureData.connectionFailure(kind)

            override suspend fun connect(candidate: ConnectionConfig): Loadable<com.helios.core.data.service.ConnectionSnapshot> =
                FixtureData.connectionFailure(kind)
        }
    }
}

```

After removing it, `:app:compileDebugKotlin` and `:app:assembleDebug` were run again, the
shipped build was installed on the emulator, and the landing route was captured from the
shipped build (`onboarding-welcome-dark.png`).

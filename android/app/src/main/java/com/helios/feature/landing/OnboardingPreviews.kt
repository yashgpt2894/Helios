package com.helios.feature.landing

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.helios.core.data.fixture.FixtureData
import com.helios.core.data.fixture.FixtureScenario
import com.helios.core.data.service.FailureKind
import com.helios.core.data.service.Loadable
import com.helios.core.designsystem.color.CarbonColors
import com.helios.core.designsystem.color.PaperColors
import com.helios.core.designsystem.layout.HeliosSpacing
import com.helios.core.ui.theme.HeliosPreviewSurface
import com.helios.feature.settings.ConnectionDraft
import com.helios.feature.settings.LocationOutcome
import com.helios.feature.settings.validateConnectionDraft

/**
 * Isolated examples of every first-run page in the states it can be in, using the fixture
 * family. They are the review surface for onboarding: the design's state list (STS-003 to
 * STS-019) is visible here without a device, an inverter, or a permission dialog.
 *
 * These previews are the equivalent of a gallery entry for this feature: the debug component
 * gallery lives in the debug source set, which this step does not own.
 */

@Preview(name = "Onboarding 1 welcome, live in the dark palette", widthDp = 412, heightDp = 915)
@Composable
private fun WelcomePageDark() {
    HeliosPreviewSurface(colors = CarbonColors) {
        WelcomePage(
            reading = FixtureData.telemetry(FixtureScenario.LIVE),
            demoSource = true,
            onSetUp = {},
            onDemo = {}
        )
    }
}

@Preview(name = "Onboarding 1 welcome, loading, light palette", widthDp = 412, heightDp = 915)
@Composable
private fun WelcomePageLight() {
    HeliosPreviewSurface(colors = PaperColors) {
        WelcomePage(
            reading = Loadable.Loading,
            demoSource = true,
            onSetUp = {},
            onDemo = {}
        )
    }
}

@Preview(name = "Onboarding 2 local first", widthDp = 412, heightDp = 915)
@Composable
private fun LocalFirst() {
    HeliosPreviewSurface(colors = CarbonColors) {
        LocalFirstPage(onContinue = {})
    }
}

@Preview(name = "Onboarding 3 connect, valid", widthDp = 412, heightDp = 915)
@Composable
private fun ConnectValid() {
    HeliosPreviewSurface(colors = CarbonColors) {
        ConnectPage(
            draft = ConnectionDraft(),
            errors = validateConnectionDraft(ConnectionDraft()),
            probe = ProbeState.Idle,
            scheduleMs = FixtureData.retryScheduleMs,
            onDraftChange = {},
            onSubmit = {},
            onDemo = {}
        )
    }
}

@Preview(name = "Onboarding 3 connect, invalid and testing", widthDp = 412, heightDp = 915)
@Composable
private fun ConnectInvalidTesting() {
    val draft = ConnectionDraft(host = "192.168.1.42:502", port = "0", unitId = "900")
    HeliosPreviewSurface(colors = CarbonColors) {
        ConnectPage(
            draft = draft,
            errors = validateConnectionDraft(draft),
            probe = ProbeState.Testing(System.currentTimeMillis() - 3_400),
            scheduleMs = FixtureData.retryScheduleMs,
            onDraftChange = {},
            onSubmit = {},
            onDemo = {}
        )
    }
}

@Preview(name = "Onboarding 4 result, answered", widthDp = 412, heightDp = 915)
@Composable
private fun ResultSuccess() {
    HeliosPreviewSurface(colors = CarbonColors) {
        ResultPage(
            result = FixtureData.connectionSnapshot(FixtureScenario.LIVE),
            config = FixtureData.connectionConfig,
            reading = FixtureData.telemetry(FixtureScenario.LIVE),
            demoSource = false,
            onContinue = {},
            onRetry = {},
            onEditDetails = {},
            onDemo = {},
            onCopyDiagnostic = {}
        )
    }
}

@Preview(name = "Onboarding 4 result, unreachable inverter", widthDp = 412, heightDp = 915)
@Composable
private fun ResultFailure() {
    HeliosPreviewSurface(colors = CarbonColors) {
        ResultPage(
            result = FixtureData.connectionFailure(FailureKind.TIMEOUT),
            config = FixtureData.connectionConfig,
            reading = FixtureData.telemetry(FixtureScenario.LIVE),
            demoSource = false,
            onContinue = {},
            onRetry = {},
            onEditDetails = {},
            onDemo = {},
            onCopyDiagnostic = {}
        )
    }
}

@Preview(name = "Onboarding 5 location, default", widthDp = 412, heightDp = 915)
@Composable
private fun LocationDefault() {
    HeliosPreviewSurface(colors = CarbonColors) {
        LocationPage(
            outcome = null,
            stored = FixtureData.location(FixtureScenario.LIVE),
            onUseMyLocation = {},
            onEnterCoordinates = {},
            onRetry = {},
            onNotNow = {},
            onContinue = {}
        )
    }
}

@Preview(name = "Onboarding 5 location, denied permission", widthDp = 412, heightDp = 915)
@Composable
private fun LocationDenied() {
    HeliosPreviewSurface(colors = CarbonColors) {
        LocationPage(
            outcome = LocationOutcome.Denied(
                com.helios.core.data.service.ServiceFailure.of(FailureKind.LOCATION_DENIED)
            ),
            stored = FixtureData.location(FixtureScenario.DENIED_PERMISSION),
            onUseMyLocation = {},
            onEnterCoordinates = {},
            onRetry = {},
            onNotNow = {},
            onContinue = {}
        )
    }
}

@Preview(name = "Onboarding 5 location, resolving", widthDp = 412, heightDp = 915)
@Composable
private fun LocationResolving() {
    HeliosPreviewSurface(colors = CarbonColors) {
        LocationPage(
            outcome = LocationOutcome.Resolving,
            stored = FixtureData.location(FixtureScenario.LIVE),
            onUseMyLocation = {},
            onEnterCoordinates = {},
            onRetry = {},
            onNotNow = {},
            onContinue = {}
        )
    }
}

@Preview(name = "Onboarding 6 complete, demo source", widthDp = 412, heightDp = 915)
@Composable
private fun CompleteDemo() {
    HeliosPreviewSurface(colors = CarbonColors) {
        Column(modifier = Modifier.fillMaxWidth().padding(HeliosSpacing.space2)) {
            CompletePage(
                outcome = OnboardingOutcome(
                    source = OnboardingSource.DEMO_SYSTEM,
                    config = null,
                    location = FixtureData.defaultLocation,
                    pollIntervalMs = 2_000,
                    completedAt = System.currentTimeMillis()
                ),
                reading = FixtureData.telemetry(FixtureScenario.LIVE),
                brandName = "helios",
                onOpenDashboard = {}
            )
        }
    }
}

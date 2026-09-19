package com.helios.feature.landing

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.helios.core.data.service.ConnectionSnapshot
import com.helios.core.data.service.Freshness
import com.helios.core.data.service.LinkState
import com.helios.core.data.service.Loadable
import com.helios.core.data.service.ServiceFailure
import com.helios.core.data.service.freshnessLabel
import com.helios.core.data.service.metaOrNull
import com.helios.core.data.service.valueOrNull
import com.helios.core.designsystem.color.HeliosStatusKind
import com.helios.core.designsystem.color.LocalHeliosSemanticColors
import com.helios.core.designsystem.component.DeniedPermissionState
import com.helios.core.designsystem.component.FreshnessStamp
import com.helios.core.designsystem.component.HeliosErrorState
import com.helios.core.designsystem.component.HeliosGhostButton
import com.helios.core.designsystem.component.HeliosMark
import com.helios.core.designsystem.component.HeliosPrimaryButton
import com.helios.core.designsystem.component.HeliosSecondaryButton
import com.helios.core.designsystem.component.StatusPill
import com.helios.core.designsystem.layout.HeliosSpacing
import com.helios.core.designsystem.shape.HeliosElevation
import com.helios.core.designsystem.shape.HeliosShape
import com.helios.core.designsystem.type.HeliosTypography
import com.helios.core.domain.model.ConnectionConfig
import com.helios.core.domain.model.Location
import com.helios.core.domain.model.SolarTelemetry
import com.helios.core.format.HeliosFormat
import com.helios.feature.settings.ConnectionDraft
import com.helios.feature.settings.ConnectionDraftErrors
import com.helios.feature.settings.ConnectionFields
import com.helios.feature.settings.LocationOutcome
import com.helios.feature.settings.classLabel
import com.helios.feature.settings.coordinateLabel
import com.helios.feature.settings.diagnosticText
import com.helios.feature.settings.locationSourceLabel
import com.helios.feature.settings.openAppSettings
import com.helios.feature.settings.pollIntervalLabel

/**
 * The six surfaces of first run, as composables that take state and report actions.
 *
 * They are deliberately dumb: every value arrives as a parameter, so each page can be
 * rendered in any state - loading, demo, denied permission, unreachable inverter - without
 * reaching for a service. [OnboardingScreen] owns the flow.
 *
 * Order is the design's: value before questions. The first page already shows a live reading
 * from the demo system, connection details are only asked on the third page and can be
 * skipped for the demo, and location is only asked on the last page with "Not now" as a
 * first-class choice.
 */

/** Shared page frame: scrolls, keeps the keyboard off the fields, and holds the actions. */
@Composable
private fun PageFrame(
    step: OnboardingStep,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(horizontal = HeliosSpacing.gutter)
            .padding(top = HeliosSpacing.space4, bottom = HeliosSpacing.space8),
        verticalArrangement = Arrangement.spacedBy(HeliosSpacing.sectionRhythm)
    ) {
        OnboardingProgress(step = step)
        content()
    }
}

/** "Step 3 of 4" plus the four markers. Position is in words for a screen reader. */
@Composable
fun OnboardingProgress(step: OnboardingStep, modifier: Modifier = Modifier) {
    val colors = LocalHeliosSemanticColors.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .semantics { contentDescription = "Step " + step.progressStep + " of " + ONBOARDING_PROGRESS_STEPS },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(HeliosSpacing.space2)
    ) {
        repeat(ONBOARDING_PROGRESS_STEPS) { index ->
            val position = index + 1
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(3.dp)
                    .clip(HeliosShape.full)
                    .background(if (position <= step.progressStep) colors.accentPrimary else colors.backgroundTertiary)
            )
        }
        Text(
            text = "Step " + step.progressStep + " of " + ONBOARDING_PROGRESS_STEPS,
            style = HeliosTypography.caption2,
            color = colors.textTertiary
        )
    }
}

/** SCR-02. One sentence, the demo path, and a reading that is already arriving. */
@Composable
fun WelcomePage(
    reading: Loadable<SolarTelemetry>,
    demoSource: Boolean,
    onSetUp: () -> Unit,
    onDemo: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalHeliosSemanticColors.current
    PageFrame(step = OnboardingStep.WELCOME, modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(HeliosSpacing.space3)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(HeliosSpacing.space3)
            ) {
                HeliosMark(size = 32.dp)
                Text(
                    text = "helios",
                    style = HeliosTypography.headline,
                    color = colors.textPrimary
                )
            }
            Text(
                text = "Power, illuminated.",
                style = HeliosTypography.title1,
                color = colors.textPrimary
            )
            Text(
                text = "Live solar, battery and grid state from your own inverter. No account, no cloud, " +
                    "nothing to sign up for.",
                style = HeliosTypography.body,
                color = colors.textSecondary
            )
        }

        ReadingCard(reading = reading, demoSource = demoSource)

        Column(verticalArrangement = Arrangement.spacedBy(HeliosSpacing.space3)) {
            HeliosPrimaryButton(
                text = "Set up my system",
                onClick = onSetUp,
                modifier = Modifier.fillMaxWidth()
            )
            HeliosSecondaryButton(
                text = "Explore demo first, no inverter needed",
                onClick = onDemo,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = "The demo system is a real data path with simulated values. You can switch to your " +
                    "inverter at any time.",
                style = HeliosTypography.caption,
                color = colors.textTertiary
            )
        }
    }
}

/** The reading the first page shows, so value arrives before any question. */
@Composable
private fun ReadingCard(
    reading: Loadable<SolarTelemetry>,
    demoSource: Boolean,
    modifier: Modifier = Modifier
) {
    val colors = LocalHeliosSemanticColors.current
    val telemetry = reading.valueOrNull()
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(HeliosShape.md)
            .background(colors.backgroundSecondary)
            .border(HeliosElevation.MaterialTokens.innerStrokeWidth, colors.separatorHairline, HeliosShape.md)
            .padding(HeliosSpacing.cardPadding),
        verticalArrangement = Arrangement.spacedBy(HeliosSpacing.space2)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Right now",
                style = HeliosTypography.caption2,
                color = colors.textTertiary
            )
            if (telemetry != null) {
                StatusPill(kind = if (demoSource) HeliosStatusKind.DEMO else HeliosStatusKind.PRODUCING)
            }
        }
        if (telemetry == null) {
            Text(
                text = if (reading is Loadable.Failed) "No reading yet" else "Reading the system",
                style = HeliosTypography.headline,
                color = colors.textPrimary
            )
            Text(
                text = "The demo system answers in under a second once a source is selected.",
                style = HeliosTypography.caption,
                color = colors.textTertiary
            )
        } else {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = HeliosFormat.fixed(telemetry.acPowerW / 1000.0, 2),
                    style = HeliosTypography.liveTicker,
                    color = colors.textPrimary
                )
                Text(
                    text = "kW",
                    style = HeliosTypography.callout,
                    color = colors.textTertiary,
                    modifier = Modifier.padding(start = HeliosSpacing.space1, bottom = HeliosSpacing.space1)
                )
            }
            Text(
                text = "Battery " + HeliosFormat.percent(telemetry.batterySoc) +
                    " \u00B7 Home " + HeliosFormat.watts(telemetry.homeLoadW) +
                    " \u00B7 Today " + HeliosFormat.kwh(telemetry.energyTodayKwh),
                style = HeliosTypography.callout,
                color = colors.textSecondary
            )
            FreshnessStamp(
                text = reading.freshnessLabel(),
                kind = if (demoSource) HeliosStatusKind.DEMO else HeliosStatusKind.PRODUCING
            )
        }
    }
}

/** SCR-03. What stays on the device, and the one request that leaves it. */
@Composable
fun LocalFirstPage(
    onContinue: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalHeliosSemanticColors.current
    PageFrame(step = OnboardingStep.LOCAL_FIRST, modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(HeliosSpacing.space3)) {
            Text(
                text = "Your data stays here",
                style = HeliosTypography.title2,
                color = colors.textPrimary
            )
            Text(
                text = "Three things worth knowing before you connect anything.",
                style = HeliosTypography.body,
                color = colors.textSecondary
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(HeliosSpacing.space3)) {
            Statement(
                title = "Readings live on the phone",
                body = "Production, battery and grid values are stored on this device. There is no account " +
                    "and no cloud sync."
            )
            Statement(
                title = "One request leaves the device",
                body = "Coordinates go to Open-Meteo to fetch the solar forecast. Nothing else is sent " +
                    "anywhere, and only when you ask for a forecast."
            )
            Statement(
                title = "No analytics, no trackers",
                body = "Nothing about how you use the app is collected, and there is no advertising identifier."
            )
        }

        HeliosPrimaryButton(
            text = "Continue",
            onClick = onContinue,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun Statement(title: String, body: String) {
    val colors = LocalHeliosSemanticColors.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(HeliosShape.md)
            .background(colors.backgroundSecondary)
            .padding(HeliosSpacing.cardPadding),
        verticalArrangement = Arrangement.spacedBy(HeliosSpacing.space1)
    ) {
        Text(text = title, style = HeliosTypography.headline, color = colors.textPrimary)
        Text(text = body, style = HeliosTypography.callout, color = colors.textSecondary)
    }
}

/** SCR-04. The minimum SunSpec parameters, and the demo path next to them. */
@Composable
fun ConnectPage(
    draft: ConnectionDraft,
    errors: ConnectionDraftErrors,
    probe: ProbeState,
    scheduleMs: List<Long>,
    onDraftChange: (ConnectionDraft) -> Unit,
    onSubmit: () -> Unit,
    onDemo: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalHeliosSemanticColors.current
    val testing = probe as? ProbeState.Testing

    PageFrame(step = OnboardingStep.CONNECT, modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(HeliosSpacing.space3)) {
            Text(
                text = "Connect the inverter",
                style = HeliosTypography.title2,
                color = colors.textPrimary
            )
            Text(
                text = "SunSpec Modbus over your local network. Four values, prefilled with a typical LAN " +
                    "address, and nothing is saved until the link answers.",
                style = HeliosTypography.body,
                color = colors.textSecondary
            )
        }

        ConnectionFields(
            draft = draft,
            errors = errors,
            onDraftChange = onDraftChange,
            enabled = testing == null,
            scheduleMs = scheduleMs,
            onImeSubmit = onSubmit
        )

        Column(verticalArrangement = Arrangement.spacedBy(HeliosSpacing.space3)) {
            HeliosPrimaryButton(
                text = "Test connection",
                onClick = onSubmit,
                enabled = testing == null && errors.isValid,
                loading = testing != null,
                disabledReason = if (!errors.isValid) "Fix the marked fields first" else null,
                modifier = Modifier.fillMaxWidth()
            )
            if (testing != null) {
                Text(
                    text = "Testing the link. The probe stops after 6 s and reports what it found.",
                    style = HeliosTypography.caption,
                    color = colors.textSecondary
                )
            }
            if (!errors.isValid) {
                Text(
                    text = "The marked field is the only thing that blocks the test.",
                    style = HeliosTypography.caption,
                    color = colors.alertStrong
                )
            }
            HeliosSecondaryButton(
                text = "Use demo system, no inverter needed",
                onClick = onDemo,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/** SCR-05. What the probe proved, or which class of failure it hit. */
@Composable
fun ResultPage(
    result: Loadable<ConnectionSnapshot>,
    config: ConnectionConfig,
    reading: Loadable<SolarTelemetry>,
    demoSource: Boolean,
    onContinue: () -> Unit,
    onRetry: () -> Unit,
    onEditDetails: () -> Unit,
    onDemo: () -> Unit,
    onCopyDiagnostic: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalHeliosSemanticColors.current
    val snapshot = result.valueOrNull()
    val failure = (result as? Loadable.Failed)?.failure
    val telemetry = reading.valueOrNull()
    val simulated = snapshot?.state == LinkState.SIMULATED || demoSource

    PageFrame(step = OnboardingStep.RESULT, modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(HeliosSpacing.space3)) {
            Text(
                text = if (snapshot != null) "The inverter answered" else "The link did not answer",
                style = HeliosTypography.title2,
                color = colors.textPrimary
            )
            Text(
                text = if (snapshot != null) {
                    "These values were read from the device, which is what makes this proof rather than a form " +
                        "acceptance."
                } else {
                    "Nothing was saved. The values you typed are still here, and the demo system works without " +
                        "any hardware."
                },
                style = HeliosTypography.body,
                color = colors.textSecondary
            )
        }

        if (snapshot != null) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(HeliosShape.md)
                    .background(colors.backgroundSecondary)
                    .border(HeliosElevation.MaterialTokens.innerStrokeWidth, colors.separatorHairline, HeliosShape.md)
                    .padding(HeliosSpacing.cardPadding),
                verticalArrangement = Arrangement.spacedBy(HeliosSpacing.space2)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = snapshot.identifiedAs ?: "Inverter",
                        style = HeliosTypography.headline,
                        color = colors.textPrimary
                    )
                    StatusPill(kind = if (simulated) HeliosStatusKind.DEMO else HeliosStatusKind.PRODUCING)
                }
                FactRow("Manufacturer", telemetry?.manufacturer ?: "Not reported")
                FactRow("Model", telemetry?.model ?: snapshot.identifiedAs ?: "Not reported")
                FactRow("Firmware", snapshot.firmware ?: telemetry?.firmware ?: "Not reported")
                FactRow("Serial", telemetry?.serialNumber ?: "Not reported")
                FactRow("Strings", telemetry?.panels?.size?.toString() ?: "Not reported")
                FactRow("Poll interval", pollIntervalLabel(config.pollIntervalMs))
                if (telemetry != null) {
                    FactRow("First reading", HeliosFormat.watts(telemetry.acPowerW) + " \u00B7 " +
                        HeliosFormat.kwh(telemetry.energyTodayKwh) + " today")
                }
                if (simulated) {
                    Text(
                        text = "Simulated source: the demo system answered, not a physical inverter.",
                        style = HeliosTypography.caption,
                        color = colors.accentStrong
                    )
                }
            }
        }

        if (failure != null) {
            HeliosErrorState(
                title = failure.classLabel(),
                message = failure.action,
                detail = listOfNotNull(
                    failure.detail,
                    "Attempts " + failure.attempts,
                    HeliosFormat.hostPort(config.host, config.port) + " unit " + config.unitId
                ).joinToString(" \u00B7 "),
                actionLabel = null,
                onAction = null
            )
            HeliosGhostButton(
                text = "Copy diagnostic detail",
                onClick = { onCopyDiagnostic(failure.diagnosticText(config)) }
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(HeliosSpacing.space3)) {
            if (snapshot != null) {
                HeliosPrimaryButton(
                    text = "Continue to location",
                    onClick = onContinue,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                HeliosPrimaryButton(
                    text = "Try again",
                    onClick = onRetry,
                    modifier = Modifier.fillMaxWidth()
                )
                HeliosSecondaryButton(
                    text = "Edit details",
                    onClick = onEditDetails,
                    modifier = Modifier.fillMaxWidth()
                )
                HeliosSecondaryButton(
                    text = "Use demo system instead",
                    onClick = onDemo,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

/** SCR-06. The forecast location, denied permission included, and it is skippable. */
@Composable
fun LocationPage(
    outcome: LocationOutcome?,
    stored: Loadable<Location>,
    onUseMyLocation: () -> Unit,
    onEnterCoordinates: () -> Unit,
    onRetry: () -> Unit,
    onNotNow: () -> Unit,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalHeliosSemanticColors.current
    val context = androidx.compose.ui.platform.LocalContext.current
    val shown: Location? = when (val state = outcome) {
        is LocationOutcome.Resolved -> state.location
        else -> stored.valueOrNull()
    }
    val settled = outcome is LocationOutcome.Resolved || outcome is LocationOutcome.Denied

    PageFrame(step = OnboardingStep.LOCATION, modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(HeliosSpacing.space3)) {
            Text(
                text = "Where is this system?",
                style = HeliosTypography.title2,
                color = colors.textPrimary
            )
            Text(
                text = "The forecast needs a place. The permission is only requested if you tap the button " +
                    "below, and this step can be skipped.",
                style = HeliosTypography.body,
                color = colors.textSecondary
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(HeliosShape.md)
                .background(colors.backgroundSecondary)
                .padding(HeliosSpacing.cardPadding),
            verticalArrangement = Arrangement.spacedBy(HeliosSpacing.space1)
        ) {
            Text(
                text = shown?.label ?: "Default location",
                style = HeliosTypography.headline,
                color = colors.textPrimary
            )
            Text(
                text = if (shown == null) {
                    "Used until you choose another. The forecast still works."
                } else {
                    coordinateLabel(shown.lat, shown.lng) + " \u00B7 " + locationSourceLabel(shown.source)
                },
                style = HeliosTypography.caption,
                color = colors.textTertiary
            )
        }

        when (val state = outcome) {
            null -> Unit
            LocationOutcome.Resolving -> Text(
                text = "Detecting the device location. The buttons are disabled until the lookup returns.",
                style = HeliosTypography.caption,
                color = colors.textSecondary
            )
            is LocationOutcome.Resolved -> Text(
                text = "Location set from this device. The forecast will use it.",
                style = HeliosTypography.callout,
                color = colors.flowStrong
            )
            is LocationOutcome.Denied -> DeniedPermissionState(
                title = "Using the default location",
                message = "Android denied the permission, so the forecast keeps the default location. Nothing " +
                    "else is blocked, and the app will not ask again on its own.",
                actionLabel = "Open Android settings",
                onAction = { openAppSettings(context) }
            )
            is LocationOutcome.Failed -> HeliosErrorState(
                title = state.failure.classLabel(),
                message = state.failure.action,
                detail = state.failure.detail,
                actionLabel = "Retry lookup",
                onAction = onRetry
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(HeliosSpacing.space3)) {
            if (settled) {
                HeliosPrimaryButton(
                    text = "Continue",
                    onClick = onContinue,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                HeliosPrimaryButton(
                    text = "Use my location",
                    onClick = onUseMyLocation,
                    enabled = outcome != LocationOutcome.Resolving,
                    loading = outcome == LocationOutcome.Resolving,
                    modifier = Modifier.fillMaxWidth()
                )
                HeliosSecondaryButton(
                    text = "Enter coordinates",
                    onClick = onEnterCoordinates,
                    modifier = Modifier.fillMaxWidth()
                )
                HeliosSecondaryButton(
                    text = "Not now, use the default location",
                    onClick = onNotNow,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Text(
                text = "Coordinates are sent to Open-Meteo only when a forecast is requested.",
                style = HeliosTypography.caption,
                color = colors.textTertiary
            )
        }
    }
}

/** The last step: what is live now, and nothing invented. */
@Composable
fun CompletePage(
    outcome: OnboardingOutcome,
    reading: Loadable<SolarTelemetry>,
    brandName: String,
    onOpenDashboard: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    val colors = LocalHeliosSemanticColors.current
    val telemetry = reading.valueOrNull()
    val simulated = outcome.source == OnboardingSource.DEMO_SYSTEM

    PageFrame(step = OnboardingStep.COMPLETE, modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(HeliosSpacing.space3)) {
            Text(
                text = "You are set up",
                style = HeliosTypography.title2,
                color = colors.textPrimary
            )
            Text(
                text = if (simulated) {
                    "The demo system is feeding the app, so every screen has live values. Connecting a real " +
                        "inverter later replaces the source without losing anything."
                } else {
                    "The inverter link is live, and the app is polling it on the interval you chose."
                },
                style = HeliosTypography.body,
                color = colors.textSecondary
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(HeliosShape.md)
                .background(colors.backgroundSecondary)
                .padding(HeliosSpacing.cardPadding),
            verticalArrangement = Arrangement.spacedBy(HeliosSpacing.space2)
        ) {
            FactRow("Source", if (simulated) "Demo system" else outcome.config?.host ?: "Inverter")
            FactRow("Address", outcome.config?.let { HeliosFormat.hostPort(it.host, it.port) } ?: "Not configured")
            FactRow("Poll interval", pollIntervalLabel(outcome.pollIntervalMs))
            FactRow("Location", outcome.location?.label ?: "Default location")
            FactRow("Brand", brandName)
            if (telemetry != null) {
                FactRow(
                    "Reading now",
                    HeliosFormat.watts(telemetry.acPowerW) + " \u00B7 " + reading.freshnessLabel()
                )
            }
        }

        if (onOpenDashboard != null) {
            HeliosPrimaryButton(
                text = "Open the dashboard",
                onClick = onOpenDashboard,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            Text(
                text = "The dashboard is the app's home surface. It shows these values with their age, and it " +
                    "opens on the theme you chose.",
                style = HeliosTypography.caption,
                color = colors.textTertiary
            )
        }
    }
}

/** One "label: value" line in a result or summary card. */
@Composable
fun FactRow(label: String, value: String, modifier: Modifier = Modifier) {
    val colors = LocalHeliosSemanticColors.current
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = HeliosTypography.callout, color = colors.textSecondary)
        Text(
            text = value,
            style = HeliosTypography.callout,
            color = colors.textPrimary,
            fontWeight = FontWeight.Medium
        )
    }
}

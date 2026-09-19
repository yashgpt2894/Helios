package com.helios.feature.settings

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.helios.core.data.service.AppServices
import com.helios.core.data.service.FailureKind
import com.helios.core.data.service.ConnectionSnapshot
import com.helios.core.data.service.LinkState
import com.helios.core.data.service.Loadable
import com.helios.core.data.service.ServiceFailure
import com.helios.core.data.service.valueOrNull
import com.helios.core.designsystem.color.LocalHeliosSemanticColors
import com.helios.core.designsystem.component.HeliosDestructiveButton
import com.helios.core.designsystem.component.HeliosErrorState
import com.helios.core.designsystem.component.HeliosGhostButton
import com.helios.core.designsystem.component.HeliosPrimaryButton
import com.helios.core.designsystem.component.HeliosSecondaryButton
import com.helios.core.designsystem.layout.HeliosSpacing
import com.helios.core.designsystem.type.HeliosTypography
import com.helios.core.domain.model.ConnectionConfig
import com.helios.core.format.HeliosFormat
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

/**
 * SCR-12, Settings — connection.
 *
 * The screen inspects, tests and persists the inverter link without overwriting a working
 * configuration by accident. Its states are the design's STS-057 to STS-063: saved, dirty,
 * validating, validation error, test passed, test failed, and a leave guard that names what
 * it will discard.
 *
 * The probe is bounded: a service call that does not answer inside [PROBE_TIMEOUT_MS] is
 * reported as F2 (timeout) with the values that were tried, which is what the design asks a
 * 6 s bound to produce. Nothing is written on a failure, and the screen always says which
 * configuration is live.
 *
 * Not shown: the attempt log of STS-064. No service in `core/data/service` exposes an attempt
 * history, and a log rendered from invented entries would be a lie about the hardware. The
 * current attempt counter is shown from the link snapshot instead.
 */
private const val PROBE_TIMEOUT_MS = 6_000L

private sealed interface ProbePhase {
    data object Idle : ProbePhase
    data class Testing(val startedAt: Long) : ProbePhase
    data class Done(val result: Loadable<ConnectionSnapshot>) : ProbePhase
}

@Composable
fun ConnectionSettingsSection(
    services: AppServices,
    onBack: () -> Unit,
    onSnack: (String) -> Unit,
    onCopyToClipboard: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalHeliosSemanticColors.current
    val scope = rememberCoroutineScope()

    val link by services.connection.link.collectAsState(initial = Loadable.Loading)
    val snapshot = link.valueOrNull()

    var saved by remember { mutableStateOf(ConnectionDraft()) }
    var draft by remember { mutableStateOf(ConnectionDraft()) }
    var probe by remember { mutableStateOf<ProbePhase>(ProbePhase.Idle) }
    var showDiscard by remember { mutableStateOf(false) }
    var elapsedSeconds by remember { mutableStateOf(0) }
    var demo by remember { mutableStateOf(services.telemetry.isDemo) }

    // The saved values come from the service the first time it answers a ready link. A draft
    // the user is editing is never overwritten by a poll.
    LaunchedEffect(snapshot?.config) {
        val config = snapshot?.config
        if (config != null && draft == saved) {
            saved = ConnectionDraft.from(config)
            draft = saved
        }
    }

    // Elapsed seconds while probing, so the busy state states its own progress.
    val testing = probe as? ProbePhase.Testing
    LaunchedEffect(testing) {
        if (testing == null) {
            elapsedSeconds = 0
            return@LaunchedEffect
        }
        while (true) {
            elapsedSeconds = ((System.currentTimeMillis() - testing.startedAt) / 1000).toInt()
            delay(250)
        }
    }

    val errors = remember(draft) { validateConnectionDraft(draft) }
    val dirty = draft != saved
    val schedule = remember { services.connection.retrySchedule() }

    fun submit() {
        val candidate = draft.copy(host = draft.host.trim())
        val validation = validateConnectionDraft(candidate)
        if (!validation.isValid) {
            probe = ProbePhase.Idle
            onSnack("Check the marked fields before testing.")
            return
        }
        probe = ProbePhase.Testing(System.currentTimeMillis())
        scope.launch {
            val config = candidate.toConfig(
                id = snapshot?.config?.id ?: 1,
                status = snapshot?.config?.status ?: "disconnected"
            )
            val answer = withTimeoutOrNull(PROBE_TIMEOUT_MS) { services.connection.test(config) }
                ?: Loadable.Failed(
                    ServiceFailure.of(
                        FailureKind.TIMEOUT,
                        detail = HeliosFormat.hostPort(config.host, config.port) + " did not answer within 6 s",
                        attempts = (snapshot?.attempt ?: 0) + 1
                    )
                )
            probe = ProbePhase.Done(answer)
            val ready = answer.valueOrNull()
            if (ready != null) {
                val stored = services.connection.connect(config)
                if (stored.valueOrNull() != null) {
                    saved = candidate.copy(port = config.port.toString(), unitId = config.unitId.toString())
                    draft = saved
                    services.telemetry.setDemoMode(false)
                    demo = false
                    onSnack("Saved. " + HeliosFormat.hostPort(config.host, config.port) + " is the live link.")
                } else {
                    probe = ProbePhase.Done(stored)
                    onSnack("The probe answered, but the configuration could not be stored.")
                }
            }
        }
    }

    fun requestBack() {
        if (dirty) showDiscard = true else onBack()
    }

    BackHandler(enabled = dirty) { showDiscard = true }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .imePadding()
            .navigationBarsPadding()
            .padding(horizontal = HeliosSpacing.gutter)
            .padding(top = HeliosSpacing.space4, bottom = HeliosSpacing.space8),
        verticalArrangement = Arrangement.spacedBy(HeliosSpacing.sectionRhythm)
    ) {
        SettingsTopBar(title = "Connection", eyebrow = "Settings", onBack = { requestBack() })

        SettingsCard {
            Text(
                text = "Inverter link",
                style = HeliosTypography.headline,
                color = colors.textPrimary
            )
            Text(
                text = linkWord(link, demo),
                style = HeliosTypography.callout,
                color = colors.textSecondary
            )
            Text(
                text = linkDetail(link, draft),
                style = HeliosTypography.caption,
                color = colors.textTertiary
            )
            if (snapshot?.lastGoodAt != null) {
                Text(
                    text = "Last good read " + HeliosFormat.clockTime(snapshot.lastGoodAt!!),
                    style = HeliosTypography.caption,
                    color = colors.textTertiary
                )
            }
            if (snapshot != null && snapshot.attempt > 0) {
                Text(
                    text = "Attempt " + snapshot.attempt + " of the retry schedule",
                    style = HeliosTypography.caption,
                    color = colors.textTertiary
                )
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(HeliosSpacing.space3)) {
            Text(
                text = if (dirty) "Edited values" else "Saved values",
                style = HeliosTypography.headline,
                color = colors.textPrimary
            )
            ConnectionFields(
                draft = draft,
                errors = errors,
                onDraftChange = { draft = it },
                enabled = probe !is ProbePhase.Testing,
                scheduleMs = schedule,
                onImeSubmit = { submit() }
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(HeliosSpacing.space3)) {
            HeliosPrimaryButton(
                text = "Save and test",
                onClick = { submit() },
                enabled = !dirty || errors.isValid,
                loading = probe is ProbePhase.Testing,
                disabledReason = if (dirty && !errors.isValid) "Fix the marked fields first" else null,
                modifier = Modifier.fillMaxWidth()
            )
            if (testing != null) {
                Text(
                    text = "Testing the link, " + elapsedSeconds + " s of 6 s. Controls are disabled while the probe runs.",
                    style = HeliosTypography.caption,
                    color = colors.textSecondary
                )
            }
            if (dirty) {
                Row(horizontalArrangement = Arrangement.spacedBy(HeliosSpacing.space3)) {
                    HeliosSecondaryButton(text = "Discard changes", onClick = {
                        draft = saved
                        probe = ProbePhase.Idle
                    })
                }
            }
            if (!dirty && probe is ProbePhase.Idle) {
                Text(
                    text = "Fields match the saved configuration, so there is nothing to save.",
                    style = HeliosTypography.caption,
                    color = colors.textTertiary
                )
            }
        }

        when (val phase = probe) {
            is ProbePhase.Done -> ProbeResult(
                result = phase.result,
                config = draft.toConfig(),
                onRetry = { submit() },
                onCopy = { text ->
                    onCopyToClipboard(text)
                    onSnack("Diagnostic copied")
                }
            )
            else -> Unit
        }

        SettingsCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(HeliosSpacing.space3)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Use the demo system",
                        style = HeliosTypography.body,
                        color = colors.textPrimary
                    )
                    Text(
                        text = "Reads simulated values and keeps the saved configuration untouched.",
                        style = HeliosTypography.caption,
                        color = colors.textTertiary
                    )
                }
                Switch(
                    checked = demo,
                    onCheckedChange = { enabled ->
                        demo = enabled
                        scope.launch {
                            services.telemetry.setDemoMode(enabled)
                            if (enabled) services.connection.disconnect()
                            onSnack(if (enabled) "Demo system on" else "Demo system off")
                        }
                    }
                )
            }
        }
    }

    if (showDiscard) {
        AlertDialog(
            onDismissRequest = { showDiscard = false },
            title = { Text("Discard the edited values?") },
            text = {
                Text(
                    "The saved configuration stays live until a new one passes its test. " +
                        "Discarding returns the fields to " + HeliosFormat.hostPort(saved.host, saved.port.toIntOrNull() ?: 502) + "."
                )
            },
            confirmButton = {
                HeliosDestructiveButton(text = "Discard changes", onClick = {
                    draft = saved
                    probe = ProbePhase.Idle
                    showDiscard = false
                    onBack()
                })
            },
            dismissButton = {
                HeliosGhostButton(text = "Keep editing", onClick = { showDiscard = false })
            }
        )
    }
}

/** The link state in words. A simulated source is never called connected. */
internal fun linkWord(link: Loadable<ConnectionSnapshot>, demo: Boolean): String {
    if (demo) return "Demo system"
    val failure = (link as? Loadable.Failed)?.failure
    if (failure != null) return failure.classLabel()
    if (link is Loadable.Loading) return "Checking the link"
    val value = link.valueOrNull() ?: return "Unknown"
    return when (value.state) {
        LinkState.SIMULATED -> "Demo system"
        LinkState.CONNECTED -> "Connected"
        LinkState.TESTING -> "Testing"
        LinkState.DISCONNECTED -> "Not connected"
    }
}

/** Which address the app would poll, and which configuration is live right now. */
private fun linkDetail(link: Loadable<ConnectionSnapshot>, draft: ConnectionDraft): String {
    val live = link.valueOrNull()?.config ?: draft.toConfig()
    val failure = (link as? Loadable.Failed)?.failure
    val where = "Saved link " + HeliosFormat.hostPort(live.host, live.port) + " unit " + live.unitId
    return if (failure == null) {
        where
    } else {
        where + ". " + failure.action + ". The saved configuration is still the one in use."
    }
}

@Composable
private fun ProbeResult(
    result: Loadable<ConnectionSnapshot>,
    config: ConnectionConfig,
    onRetry: () -> Unit,
    onCopy: (String) -> Unit
) {
    val colors = LocalHeliosSemanticColors.current
    when (result) {
        is Loadable.Ready -> {
            val value = result.value
            SettingsCard {
                Text(
                    text = "The inverter answered",
                    style = HeliosTypography.headline,
                    color = colors.textPrimary
                )
                FactLine("Identified as", value.identifiedAs ?: "Unknown model")
                FactLine("Firmware", value.firmware ?: "Not reported")
                FactLine("Address", HeliosFormat.hostPort(value.config.host, value.config.port))
                FactLine("Unit id", value.config.unitId.toString())
                FactLine("Poll interval", pollIntervalLabel(value.config.pollIntervalMs))
                if (value.state == LinkState.SIMULATED) {
                    Text(
                        text = "Simulated source: the values come from the demo system, not from a real inverter.",
                        style = HeliosTypography.caption,
                        color = colors.accentStrong
                    )
                }
            }
        }
        is Loadable.Failed -> {
            val failure = result.failure
            HeliosErrorState(
                title = failure.classLabel(),
                message = failure.action,
                detail = listOfNotNull(
                    failure.detail,
                    "Attempts " + failure.attempts,
                    "Values used: " + HeliosFormat.hostPort(config.host, config.port) + " unit " + config.unitId
                ).joinToString(" \u00B7 "),
                actionLabel = "Test again",
                onAction = onRetry
            )
            HeliosGhostButton(text = "Copy diagnostic detail", onClick = { onCopy(failure.diagnosticText(config)) })
        }
        is Loadable.Empty -> Text(
            text = result.message,
            style = HeliosTypography.callout,
            color = colors.textSecondary
        )
        Loadable.Loading -> Text(
            text = "Waiting for the probe result",
            style = HeliosTypography.callout,
            color = colors.textSecondary
        )
    }
}

/** One "label: value" line inside a result card. */
@Composable
fun FactLine(label: String, value: String, modifier: Modifier = Modifier) {
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

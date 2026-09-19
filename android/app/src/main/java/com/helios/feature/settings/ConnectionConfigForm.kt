package com.helios.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import com.helios.core.data.service.FailureKind
import com.helios.core.data.service.ServiceFailure
import com.helios.core.designsystem.color.LocalHeliosSemanticColors
import com.helios.core.designsystem.layout.HeliosSpacing
import com.helios.core.designsystem.type.HeliosTypography
import com.helios.core.domain.model.ConnectionConfig
import com.helios.core.format.HeliosFormat
import java.util.Locale

/**
 * The one SunSpec Modbus form.
 *
 * SCR-04 (first run, onboarding page 3) and SCR-12 (Settings, connection) render the same
 * controls with the same validation, so a value that is accepted on one screen cannot be
 * rejected on the other. The two hosts differ only in what their submit action does: first
 * run tests and then leaves the value in the onboarding draft, Settings saves it.
 *
 * The values and ranges come from `design/screen-inventory.md` ACT-006 to ACT-010 and
 * FLW-01: host name or IPv4 literal, port 1-65535, unit id 1-247, and a poll interval from
 * the supported set rather than free text.
 */

/** SunSpec Modbus over TCP. The only protocol this build can speak. */
const val PROTOCOL_SUNSPEC_MODBUS_TCP = "sunspec-modbus-tcp"

/** SunSpec Modbus over a serial line. Listed and refused with a reason, never silently absent. */
const val PROTOCOL_SUNSPEC_MODBUS_RTU = "sunspec-modbus-rtu"

/** The poll intervals the selector offers (ACT-010). */
val POLL_INTERVAL_OPTIONS_MS = listOf(1_000, 2_000, 5_000, 10_000)

/** A typical LAN address, so the fields start from something editable rather than empty. */
const val DEFAULT_INVERTER_HOST = "192.168.1.42"
const val DEFAULT_INVERTER_PORT = "502"
const val DEFAULT_UNIT_ID = "1"
const val DEFAULT_POLL_INTERVAL_MS = 2_000

/** "2 s" for a selector caption. Whole seconds only, because that is all the selector offers. */
fun pollIntervalLabel(ms: Int): String = "%d s".format(Locale.US, ms / 1000)

/** The retry schedule in words, from the service that owns it (FLW-03). */
fun retryScheduleLabel(scheduleMs: List<Long>): String {
    if (scheduleMs.isEmpty()) return "Retries follow the service schedule."
    val head = scheduleMs.take(3).joinToString(" / ") { pollIntervalLabel(it.toInt()) }
    val tail = scheduleMs.drop(3).joinToString(" then ") { pollIntervalLabel(it.toInt()) }
    return if (tail.isBlank()) "Retries at $head" else "Retries at $head, then $tail"
}

/** What the user typed, before validation. Strings, so a half-typed number is not lost. */
data class ConnectionDraft(
    val protocol: String = PROTOCOL_SUNSPEC_MODBUS_TCP,
    val host: String = DEFAULT_INVERTER_HOST,
    val port: String = DEFAULT_INVERTER_PORT,
    val unitId: String = DEFAULT_UNIT_ID,
    val pollIntervalMs: Int = DEFAULT_POLL_INTERVAL_MS
) {

    /** Saved shape: the host is trimmed, because a trailing space is never intentional. */
    fun toConfig(id: Int = 1, status: String = "disconnected"): ConnectionConfig = ConnectionConfig(
        id = id,
        protocol = protocol,
        host = host.trim(),
        port = port.toIntOrNull() ?: 502,
        unitId = unitId.toIntOrNull() ?: 1,
        pollIntervalMs = pollIntervalMs,
        status = status
    )

    companion object {
        fun from(config: ConnectionConfig): ConnectionDraft = ConnectionDraft(
            protocol = config.protocol,
            host = config.host,
            port = config.port.toString(),
            unitId = config.unitId.toString(),
            pollIntervalMs = config.pollIntervalMs
        )
    }
}

/** Field-level errors. A null field is valid; the error text says what to change. */
data class ConnectionDraftErrors(
    val port: String? = null,
    val host: String? = null,
    val unitId: String? = null
) {
    val isValid: Boolean get() = host == null && port == null && unitId == null
}

private val HOST_NAME = Regex("^[A-Za-z0-9]([A-Za-z0-9-]*[A-Za-z0-9])?(\\.[A-Za-z0-9]([A-Za-z0-9-]*[A-Za-z0-9])?)*$")

private fun isIpv4Literal(value: String): Boolean {
    val parts = value.split(".")
    if (parts.size != 4) return false
    return parts.all { part ->
        part.isNotEmpty() && part.length <= 3 && part.all { it.isDigit() } && part.toInt() in 0..255
    }
}

/** Local validation only: it never claims the inverter answered, that is the probe's job. */
fun validateConnectionDraft(draft: ConnectionDraft): ConnectionDraftErrors {
    val host = draft.host.trim()
    val hostError = when {
        host.isEmpty() -> "Enter the inverter host or IP address"
        host.contains("://") -> "Enter the address without a scheme, for example 192.168.1.42"
        host.any { it.isWhitespace() } -> "Remove the spaces from the address"
        host.contains(":") -> "Enter the host without a port; the port has its own field"
        host.length > 253 -> "That address is too long for a host name"
        isIpv4Literal(host) -> null
        HOST_NAME.matches(host) -> null
        else -> "That does not look like a host name or an IPv4 address"
    }

    val portNumber = draft.port.toIntOrNull()
    val portError = when {
        draft.port.isBlank() -> "Enter the Modbus TCP port"
        draft.port.any { !it.isDigit() } -> "Digits only"
        portNumber == null || portNumber !in 1..65535 -> "Use a port between 1 and 65535"
        else -> null
    }

    val unitNumber = draft.unitId.toIntOrNull()
    val unitError = when {
        draft.unitId.isBlank() -> "Enter the Modbus unit identifier"
        draft.unitId.any { !it.isDigit() } -> "Digits only"
        unitNumber == null || unitNumber !in 1..247 -> "Use a unit id between 1 and 247"
        else -> null
    }

    return ConnectionDraftErrors(port = portError, host = hostError, unitId = unitError)
}

/**
 * The diagnostic text the failure states offer for copying (ACT-018): what was tried, how
 * often, and when. Local facts only; nothing here identifies the phone's owner.
 */
fun ServiceFailure.diagnosticText(config: ConnectionConfig, nowMs: Long = System.currentTimeMillis()): String =
    buildString {
        append("helios inverter diagnostic\n")
        append("failure ").append(kind.code).append(": ").append(message).append('\n')
        append("host ").append(config.host).append(':').append(config.port).append('\n')
        append("unit id ").append(config.unitId).append('\n')
        append("poll ").append(config.pollIntervalMs).append(" ms\n")
        append("attempts ").append(attempts).append('\n')
        if (detail != null) append("detail ").append(detail).append('\n')
        append("captured ").append(HeliosFormat.clockTime(nowMs)).append('\n')
    }

/**
 * The four controls of the connection form. It renders an error per field, ties the error to
 * the field for a screen reader, and routes the keyboard's Next and Done actions.
 */
@Composable
fun ConnectionFields(
    draft: ConnectionDraft,
    errors: ConnectionDraftErrors,
    onDraftChange: (ConnectionDraft) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    scheduleMs: List<Long> = emptyList(),
    onImeSubmit: (() -> Unit)? = null
) {
    val colors = LocalHeliosSemanticColors.current
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(HeliosSpacing.space3)) {
        Text(
            text = "Protocol",
            style = HeliosTypography.caption2,
            color = colors.textTertiary
        )
        SegmentedOptions(
            options = listOf(
                SegmentOption(id = PROTOCOL_SUNSPEC_MODBUS_TCP, label = "Modbus TCP", caption = "LAN"),
                SegmentOption(
                    id = PROTOCOL_SUNSPEC_MODBUS_RTU,
                    label = "Modbus RTU",
                    caption = "serial",
                    disabledReason = "Serial RS-485 is not supported in this build"
                )
            ),
            selectedId = draft.protocol,
            onSelect = { onDraftChange(draft.copy(protocol = it)) },
            modifier = Modifier.fillMaxWidth()
        )

        SetupField(
            label = "Inverter host or IP address",
            value = draft.host,
            onValueChange = { onDraftChange(draft.copy(host = it)) },
            errorText = errors.host,
            supporting = "Typical LAN address. No discovery in this build.",
            enabled = enabled,
            keyboardType = KeyboardType.Uri,
            imeAction = ImeAction.Next
        )

        Row(horizontalArrangement = Arrangement.spacedBy(HeliosSpacing.space3)) {
            SetupField(
                label = "Modbus TCP port",
                value = draft.port,
                onValueChange = { onDraftChange(draft.copy(port = it)) },
                errorText = errors.port,
                enabled = enabled,
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next,
                modifier = Modifier.weight(1f)
            )
            SetupField(
                label = "Unit id",
                value = draft.unitId,
                onValueChange = { onDraftChange(draft.copy(unitId = it)) },
                errorText = errors.unitId,
                enabled = enabled,
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done,
                onImeAction = onImeSubmit,
                modifier = Modifier.weight(1f)
            )
        }

        Text(
            text = "Poll interval",
            style = HeliosTypography.caption2,
            color = colors.textTertiary
        )
        SegmentedOptions(
            options = POLL_INTERVAL_OPTIONS_MS.map { ms ->
                SegmentOption(
                    id = ms.toString(),
                    label = pollIntervalLabel(ms),
                    caption = if (ms == DEFAULT_POLL_INTERVAL_MS) "default" else null
                )
            },
            selectedId = draft.pollIntervalMs.toString(),
            onSelect = { onDraftChange(draft.copy(pollIntervalMs = it.toIntOrNull() ?: DEFAULT_POLL_INTERVAL_MS)) },
            modifier = Modifier.fillMaxWidth()
        )
        if (scheduleMs.isNotEmpty()) {
            Text(
                text = retryScheduleLabel(scheduleMs),
                style = HeliosTypography.caption,
                color = colors.textTertiary
            )
        }
        Spacer(Modifier.height(HeliosSpacing.space1))
    }
}

/** The failure class in words, for a result row: "F2 · Inverter not responding". */
fun ServiceFailure.classLabel(): String = kind.code + " \u00B7 " + message

/** True when the class means the user has to change a value before a retry can work. */
fun ServiceFailure.editsAValue(): Boolean = kind in setOf(
    FailureKind.REFUSED,
    FailureKind.WRONG_UNIT_ID,
    FailureKind.NOT_SUNSPEC
)

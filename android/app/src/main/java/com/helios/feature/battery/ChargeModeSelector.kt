package com.helios.feature.battery

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.helios.core.designsystem.color.LocalHeliosSemanticColors
import com.helios.core.designsystem.component.HeliosGhostButton
import com.helios.core.designsystem.component.SectionHeader
import com.helios.core.designsystem.haptics.HapticAction
import com.helios.core.designsystem.haptics.HeliosHaptics
import com.helios.core.designsystem.layout.HeliosSpacing
import com.helios.core.designsystem.shape.HeliosShape
import com.helios.core.designsystem.type.HeliosTypography

/** How one row of the charge-mode selector is drawn. */
enum class ChargeModeRowState { ACTIVE, SELECTABLE, APPLYING, DISABLED, REVERTED, REJECTED }

/**
 * Charge strategy: the reported mode, a change in flight, and a rejected change.
 *
 * States (SCR-10): selected, unselected, applying (STS-051, with the other rows disabled
 * and the reason stated), rejected with a revert to the reported mode (STS-052).
 *
 * Tapping a mode opens the confirmation dialog that acts as SCR-19 until that dialog
 * moves to navigation; the row itself is never the thing that writes to the inverter.
 */
@Composable
fun ChargeModeSection(
    state: BatteryScreenState,
    modifier: Modifier = Modifier,
    onModeRequested: (ChargeMode) -> Unit = {},
    onConfirm: (ChargeMode) -> Unit = {},
    onRetry: () -> Unit = {}
) {
    val colors = LocalHeliosSemanticColors.current
    val view = LocalView.current
    // The mode the confirmation dialog is about, and the mode the screen is showing as
    // applying before a state owner has reported back.
    var dialogMode by remember { mutableStateOf<ChargeMode?>(null) }
    var localApplying by remember { mutableStateOf<ChargeMode?>(null) }

    val applyingMode = state.chargeModePending
        .takeIf { state.chargeModeStatus == ChargeApplyStatus.APPLYING }
        ?: localApplying
    val rejectedMode = if (state.chargeModeStatus == ChargeApplyStatus.FAILED) state.chargeModePending else null

    /*
     * The confirmation is confirmed once: the success haptic fires on the transition the
     * caller reports, not on the tap, and the local applying hint clears at the same time
     * so the row does not stay busy after a confirmation or a rejection.
     */
    LaunchedEffect(state.chargeModeStatus) {
        when (state.chargeModeStatus) {
            ChargeApplyStatus.CONFIRMED -> {
                HeliosHaptics.success(view)
                localApplying = null
            }
            ChargeApplyStatus.FAILED -> localApplying = null
            else -> Unit
        }
    }

    Column(modifier = modifier) {
        SectionHeader(title = "Charge mode", eyebrow = "Strategy")
        Spacer(Modifier.height(HeliosSpacing.space3))
        BatteryCard {
            ChargeMode.entries.forEachIndexed { index, mode ->
                if (index > 0) Spacer(Modifier.height(HeliosSpacing.space2))
                val rowState = when {
                    applyingMode == mode -> ChargeModeRowState.APPLYING
                    rejectedMode == mode -> ChargeModeRowState.REJECTED
                    state.chargeMode == mode && state.chargeModeStatus == ChargeApplyStatus.FAILED ->
                        ChargeModeRowState.REVERTED
                    state.chargeMode == mode -> ChargeModeRowState.ACTIVE
                    applyingMode != null -> ChargeModeRowState.DISABLED
                    else -> ChargeModeRowState.SELECTABLE
                }
                ChargeModeRow(
                    mode = mode,
                    rowState = rowState,
                    // The reason belongs to the rows that are unavailable, not to the row
                    // that is being applied.
                    disabledReason = if (rowState == ChargeModeRowState.DISABLED) {
                        applyingMode?.let { "Unavailable while ${it.label.lowercase()} is being applied" }
                    } else {
                        null
                    },
                    onClick = {
                        onModeRequested(mode)
                        dialogMode = mode
                    }
                )
            }

            if (state.chargeModeStatus == ChargeApplyStatus.FAILED) {
                Spacer(Modifier.height(HeliosSpacing.space3))
                val attempted = state.chargeModePending
                Text(
                    text = buildString {
                        append("The inverter rejected the change")
                        if (attempted != null) append(" to ${attempted.label.lowercase()}")
                        append(". The mode is still ${state.chargeMode.label.lowercase()}.")
                    },
                    style = HeliosTypography.callout,
                    color = colors.alertStrong
                )
                if (state.chargeModeFailure != null) {
                    Text(
                        text = state.chargeModeFailure,
                        style = HeliosTypography.caption,
                        color = colors.textSecondary
                    )
                }
                Spacer(Modifier.height(HeliosSpacing.space2))
                HeliosGhostButton(
                    text = "Try again",
                    onClick = {
                        onRetry()
                        // Re-open the confirmation for the mode that was refused, so the
                        // retry is a visible step rather than a silent re-send.
                        if (attempted != null) dialogMode = attempted
                    }
                )
            }
        }
    }

    val target = dialogMode
    if (target != null) {
        ModeConfirmationDialog(
            mode = target,
            applying = applyingMode == target,
            onConfirm = {
                HeliosHaptics.perform(view, HapticAction.PRIMARY_CTA)
                localApplying = target
                onConfirm(target)
                // The dialog's job is done at the tap: the row now carries the applying
                // state, so leaving the dialog up would hide the row it is about.
                dialogMode = null
            },
            onDismiss = { dialogMode = null }
        )
    }
}

/**
 * SCR-19 in place: confirm the mode, and state what the change does. Cancel and back both
 * close it with no change.
 */
@Composable
fun ModeConfirmationDialog(
    mode: ChargeMode,
    applying: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Switch to ${mode.label}?") },
        text = {
            Column {
                Text(text = mode.description, style = HeliosTypography.callout)
                Spacer(Modifier.height(HeliosSpacing.space2))
                Text(
                    text = "The inverter applies the change to its configuration on the next poll. " +
                        "It is a strategy, not a schedule.",
                    style = HeliosTypography.caption
                )
            }
        },
        confirmButton = {
            TextButton(enabled = !applying, onClick = onConfirm) {
                Text(text = if (applying) "Applying\u2026" else "Switch mode")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(text = "Cancel") }
        }
    )
}

@Composable
private fun ChargeModeRow(
    mode: ChargeMode,
    rowState: ChargeModeRowState,
    modifier: Modifier = Modifier,
    disabledReason: String? = null,
    onClick: () -> Unit
) {
    val colors = LocalHeliosSemanticColors.current
    val selected = rowState == ChargeModeRowState.ACTIVE || rowState == ChargeModeRowState.REVERTED
    val enabled = rowState == ChargeModeRowState.SELECTABLE
    val container = when (rowState) {
        ChargeModeRowState.ACTIVE -> colors.backgroundSecondary
        ChargeModeRowState.APPLYING -> colors.accentSubtle
        ChargeModeRowState.REJECTED -> colors.alertSubtle
        ChargeModeRowState.REVERTED -> colors.backgroundSecondary
        ChargeModeRowState.DISABLED -> colors.backgroundSecondary.copy(alpha = 0.5f)
        ChargeModeRowState.SELECTABLE -> colors.backgroundSecondary
    }
    val word = when (rowState) {
        ChargeModeRowState.ACTIVE -> "Active"
        ChargeModeRowState.SELECTABLE -> null
        ChargeModeRowState.APPLYING -> "Applying\u2026"
        ChargeModeRowState.DISABLED -> null
        ChargeModeRowState.REVERTED -> "Reverted"
        ChargeModeRowState.REJECTED -> "Rejected"
    }
    val wordColor = when (rowState) {
        ChargeModeRowState.APPLYING -> colors.onSubtle
        ChargeModeRowState.REJECTED -> colors.alertStrong
        ChargeModeRowState.REVERTED -> colors.textSecondary
        else -> colors.batteryStrong
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(HeliosShape.md)
            .background(container)
            .border(HeliosElevationStroke(rowState), colors.separatorHairline, HeliosShape.md)
            .clickable(enabled = enabled, role = Role.RadioButton, onClick = onClick)
            .defaultMinSize(minHeight = HeliosSpacing.minTouchTarget)
            .padding(HeliosSpacing.space3)
            .semantics(mergeDescendants = true) {
                role = Role.RadioButton
                this.selected = selected
                contentDescription = buildString {
                    append(mode.label)
                    append(if (selected) ", active" else ", not selected")
                    append(". ")
                    append(mode.description)
                    if (word != null && !selected) append(" $word.")
                    if (disabledReason != null) append(" $disabledReason.")
                }
            },
        horizontalArrangement = Arrangement.spacedBy(HeliosSpacing.space3)
    ) {
        RadioIndicator(selected = selected, applying = rowState == ChargeModeRowState.APPLYING)
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = mode.label,
                    style = HeliosTypography.callout,
                    fontWeight = FontWeight.Medium,
                    color = if (rowState == ChargeModeRowState.DISABLED) colors.textQuaternary else colors.textPrimary
                )
                if (word != null) {
                    Text(
                        text = "  " + word.uppercase(),
                        style = HeliosTypography.caption2,
                        color = wordColor
                    )
                }
            }
            Spacer(Modifier.height(HeliosSpacing.space1))
            Text(
                text = disabledReason ?: mode.description,
                style = HeliosTypography.caption,
                color = if (rowState == ChargeModeRowState.DISABLED) colors.textQuaternary else colors.textSecondary
            )
        }
    }
}

/**
 * The selection mark: a ring that fills when the mode is active, and a hollow ring while a
 * change is being applied. Shape and words carry the state, never colour alone.
 */
@Composable
private fun RadioIndicator(selected: Boolean, applying: Boolean) {
    val colors = LocalHeliosSemanticColors.current
    val stroke = if (selected) colors.batteryPrimary else colors.separatorStrong
    Box(
        modifier = Modifier
            .padding(top = 2.dp)
            .size(16.dp)
            .clip(HeliosShape.full)
            .border(2.dp, stroke, HeliosShape.full)
            .background(if (selected) colors.batterySubtle else colors.backgroundPrimary),
        contentAlignment = Alignment.Center
    ) {
        if (selected || applying) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(HeliosShape.full)
                    .background(if (applying) colors.accentPrimary else colors.batteryPrimary)
            )
        }
    }
}

/** The stroke of a row: the rejected row is outlined, the others carry the hairline. */
@Composable
private fun HeliosElevationStroke(rowState: ChargeModeRowState) =
    if (rowState == ChargeModeRowState.REJECTED) 1.dp else
        com.helios.core.designsystem.shape.HeliosElevation.MaterialTokens.innerStrokeWidth

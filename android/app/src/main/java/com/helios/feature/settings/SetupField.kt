package com.helios.feature.settings

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.helios.core.designsystem.color.LocalHeliosSemanticColors
import com.helios.core.designsystem.layout.HeliosSpacing
import com.helios.core.designsystem.shape.HeliosElevation
import com.helios.core.designsystem.shape.HeliosShape
import com.helios.core.designsystem.type.HeliosTypography

/**
 * `SetupField` and the single-choice selector, the two inputs first run and Settings share.
 *
 * DESIGN.md section 8 lists `SetupField` as a design-system component. It is implemented in
 * this feature package because the `core/designsystem` sources are owned by another step of this
 * Quest; the implementation already reads only semantic tokens, so moving the file there
 * later is a mechanical move, not a rewrite.
 *
 * The control itself is the platform `OutlinedTextField`, so focus, IME behaviour, cursor
 * handling, password/URL keyboards and TalkBack read the way Android expects. Its colours
 * come from the Material scheme that `HeliosTheme` maps from `design/tokens.json`, which is
 * the same token source the rest of the app uses; overriding colours per component would
 * duplicate that mapping.
 */

/** A labelled text input with one optional error line tied to the field. */
@Composable
fun SetupField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    errorText: String? = null,
    supporting: String? = null,
    enabled: Boolean = true,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    onImeAction: (() -> Unit)? = null
) {
    val colors = LocalHeliosSemanticColors.current

    // One supporting line: the error when there is one, otherwise the hint. Null when
    // neither exists, so an unused line never reserves height.
    val helper: (@Composable () -> Unit)? = (errorText ?: supporting)?.let { line ->
        {
            Text(
                text = line,
                style = HeliosTypography.caption,
                color = if (errorText != null) colors.alertStrong else colors.textTertiary
            )
        }
    }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = HeliosSpacing.minTouchTarget)
            .semantics { if (errorText != null) error(errorText) },
        enabled = enabled,
        singleLine = true,
        isError = errorText != null,
        shape = HeliosShape.md,
        textStyle = HeliosTypography.body.copy(
            color = if (enabled) colors.textPrimary else colors.textQuaternary
        ),
        label = {
            Text(
                text = label,
                style = HeliosTypography.caption2,
                color = colors.textTertiary
            )
        },
        supportingText = helper,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
        keyboardActions = KeyboardActions(
            onNext = { onImeAction?.invoke() },
            onDone = { onImeAction?.invoke() }
        )
    )
}

/** One option in [SegmentedOptions]. A disabled option states why it cannot be chosen. */
data class SegmentOption(
    val id: String,
    val label: String,
    val caption: String? = null,
    val disabledReason: String? = null
)

/**
 * Single-choice selector for a small, fixed set of values (protocol, poll interval).
 *
 * Variants: selected, unselected, disabled-with-reason. It is a radio group, not three
 * buttons, so TalkBack reports the selected state and the group position.
 */
@Composable
fun SegmentedOptions(
    options: List<SegmentOption>,
    selectedId: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalHeliosSemanticColors.current
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(HeliosSpacing.space2)
    ) {
        options.forEach { option ->
            val selected = option.id == selectedId
            val enabled = option.disabledReason == null
            Column(
                modifier = Modifier
                    .weight(1f)
                    .defaultMinSize(minHeight = HeliosSpacing.minTouchTarget)
                    .clip(HeliosShape.sm)
                    .background(if (selected) colors.accentSubtle else colors.backgroundTertiary)
                    .border(
                        HeliosElevation.MaterialTokens.innerStrokeWidth,
                        if (selected) colors.accentPrimary else colors.separatorHairline,
                        HeliosShape.sm
                    )
                    .selectable(selected = selected, enabled = enabled, role = Role.RadioButton) {
                        onSelect(option.id)
                    }
                    .padding(horizontal = HeliosSpacing.space2, vertical = HeliosSpacing.space2)
                    .semantics {
                        if (!enabled) {
                            disabled()
                            contentDescription = option.label + ". " + option.disabledReason
                        }
                    },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(HeliosSpacing.space1)
            ) {
                Text(
                    text = option.label,
                    style = HeliosTypography.callout,
                    color = when {
                        !enabled -> colors.textQuaternary
                        selected -> colors.onSubtle
                        else -> colors.textSecondary
                    },
                    fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal
                )
                if (option.caption != null) {
                    Text(
                        text = option.caption,
                        style = HeliosTypography.caption2,
                        color = colors.textTertiary
                    )
                }
            }
        }
    }
}

/**
 * Secondary screen header: an in-app back affordance plus the screen title.
 *
 * The chevron is drawn from the same geometry family as the rest of the app rather than
 * shipped as an icon font, and it carries the words "Back" for a screen reader. The
 * platform back gesture is handled by the host screen with `BackHandler`.
 */
@Composable
fun SettingsTopBar(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    eyebrow: String? = null
) {
    val colors = LocalHeliosSemanticColors.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = HeliosSpacing.minTouchTarget),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(HeliosSpacing.space2)
    ) {
        Box(
            modifier = Modifier
                .size(HeliosSpacing.minTouchTarget)
                .clip(HeliosShape.full)
                .clickable(role = Role.Button, onClick = onBack)
                .semantics { contentDescription = "Back" },
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(16.dp)) {
                val stroke = size.minDimension * 0.12f
                drawLine(
                    color = colors.accentStrong,
                    start = Offset(size.width * 0.72f, size.height * 0.12f),
                    end = Offset(size.width * 0.28f, size.height * 0.5f),
                    strokeWidth = stroke,
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = colors.accentStrong,
                    start = Offset(size.width * 0.28f, size.height * 0.5f),
                    end = Offset(size.width * 0.72f, size.height * 0.88f),
                    strokeWidth = stroke,
                    cap = StrokeCap.Round
                )
            }
        }
        Column {
            if (eyebrow != null) {
                Text(
                    text = eyebrow.uppercase(),
                    style = HeliosTypography.caption2,
                    color = colors.textTertiary
                )
            }
            Text(text = title, style = HeliosTypography.headline, color = colors.textPrimary)
        }
    }
}

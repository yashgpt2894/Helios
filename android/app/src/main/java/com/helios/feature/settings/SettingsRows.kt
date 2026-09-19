package com.helios.feature.settings

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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Canvas
import com.helios.core.designsystem.color.LocalHeliosSemanticColors
import com.helios.core.designsystem.layout.HeliosSpacing
import com.helios.core.designsystem.shape.HeliosElevation
import com.helios.core.designsystem.shape.HeliosShape
import com.helios.core.designsystem.type.HeliosTypography

/**
 * The row vocabulary Settings is built from: a value row that opens a secondary surface, a
 * card that groups rows, and a disclosure that expands in place.
 *
 * Every row states its current value before it is opened (SCR-11), so a user does not have
 * to enter a screen to learn what is configured. Rows are one 48 dp touch target, read as a
 * single node, and never rely on the accent colour alone to say they are actionable.
 */

/** Grouping card: secondary background, medium radius, hairline edge (DESIGN.md section 3). */
@Composable
fun SettingsCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val colors = LocalHeliosSemanticColors.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(HeliosShape.md)
            .background(colors.backgroundSecondary)
            .border(HeliosElevation.MaterialTokens.innerStrokeWidth, colors.separatorHairline, HeliosShape.md)
            .padding(HeliosSpacing.cardPadding),
        verticalArrangement = Arrangement.spacedBy(HeliosSpacing.space2)
    ) {
        content()
    }
}

/**
 * A settings row: label, current value, and a chevron when it opens another surface.
 *
 * [value] is the live state, not a placeholder: "Demo system", "Paper", "San Francisco, CA".
 */
@Composable
fun SettingsRow(
    label: String,
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    supporting: String? = null,
    opensSurface: Boolean = true
) {
    val colors = LocalHeliosSemanticColors.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = HeliosSpacing.minTouchTarget)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(vertical = HeliosSpacing.space2)
            .semantics(mergeDescendants = true) {
                contentDescription = if (supporting != null) {
                    label + ", " + value + ". " + supporting
                } else {
                    label + ", " + value
                }
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(HeliosSpacing.space3)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, style = HeliosTypography.body, color = colors.textPrimary)
            if (supporting != null) {
                Text(text = supporting, style = HeliosTypography.caption, color = colors.textSecondary)
            }
        }
        Text(
            text = value,
            style = HeliosTypography.callout,
            color = colors.accentStrong,
            fontWeight = FontWeight.Medium
        )
        if (opensSurface) {
            ForwardChevron()
        }
    }
}

/** The chevron that says a row opens something. Decorative: the row already has its label. */
@Composable
private fun ForwardChevron() {
    val colors = LocalHeliosSemanticColors.current
    Canvas(
        modifier = Modifier
            .size(12.dp)
            .rotate(180f)
            .semantics { }
    ) {
        val stroke = size.minDimension * 0.14f
        drawLine(
            color = colors.textTertiary,
            start = Offset(size.width * 0.72f, size.height * 0.12f),
            end = Offset(size.width * 0.28f, size.height * 0.5f),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
        drawLine(
            color = colors.textTertiary,
            start = Offset(size.width * 0.28f, size.height * 0.5f),
            end = Offset(size.width * 0.72f, size.height * 0.88f),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
    }
}

/**
 * A disclosure that expands in place: a real state change with the words it reveals, never a
 * toast that claims something happened.
 */
@Composable
fun SettingsDisclosure(
    label: String,
    expanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    body: @Composable () -> Unit
) {
    val colors = LocalHeliosSemanticColors.current
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = HeliosSpacing.minTouchTarget)
                .clickable(role = Role.Button, onClick = onToggle)
                .padding(vertical = HeliosSpacing.space2)
                .semantics(mergeDescendants = true) {
                    stateDescription = if (expanded) "Expanded" else "Collapsed"
                },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(HeliosSpacing.space2)
        ) {
            Text(
                text = label,
                style = HeliosTypography.callout,
                color = colors.accentStrong,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = if (expanded) "Hide" else "More",
                style = HeliosTypography.caption2,
                color = colors.textTertiary
            )
        }
        if (expanded) {
            Spacer(Modifier.height(HeliosSpacing.space1))
            body()
            Spacer(Modifier.height(HeliosSpacing.space1))
        }
    }
}

/** Body text inside a card or a disclosure: the sentence that carries the meaning. */
@Composable
fun SettingsNote(
    text: String,
    modifier: Modifier = Modifier,
    emphasis: Boolean = false
) {
    val colors = LocalHeliosSemanticColors.current
    Text(
        text = text,
        modifier = modifier.fillMaxWidth().padding(vertical = HeliosSpacing.space1),
        style = if (emphasis) HeliosTypography.callout else HeliosTypography.caption,
        color = if (emphasis) colors.textSecondary else colors.textTertiary
    )
}

/** A small square of a brand accent, so a brand row is not only text. */
@Composable
fun AccentSwatch(
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 20.dp
) {
    val colors = LocalHeliosSemanticColors.current
    Box(
        modifier = modifier
            .size(size)
            .clip(HeliosShape.full)
            .background(color)
            .border(HeliosElevation.MaterialTokens.innerStrokeWidth, colors.separatorHairline, HeliosShape.full)
    )
}

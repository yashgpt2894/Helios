package com.helios.feature.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.helios.core.designsystem.color.HeliosSeverityKind
import com.helios.core.designsystem.color.LocalHeliosSemanticColors
import com.helios.core.designsystem.component.HeliosGhostButton
import com.helios.core.designsystem.component.toSeverityKind
import com.helios.core.designsystem.component.word
import com.helios.core.designsystem.layout.HeliosSpacing
import com.helios.core.designsystem.shape.HeliosElevation
import com.helios.core.designsystem.shape.HeliosShape
import com.helios.core.designsystem.type.HeliosTypography
import com.helios.core.domain.model.Insight

/**
 * The rank-1 advisory: the one thing the app thinks the owner should know, directly under
 * the hero instrument (design direction D1 correction C2).
 *
 * Variants: positive, neutral, attention, critical; with and without an action; with and
 * without the delta; with and without the demo qualifier.
 *
 * Accessibility: the severity is written in words ("Attention", "Critical") as well as
 * carried by the stripe colour, and the action is a real button that runs a handler the
 * caller supplied. When the caller has no handler the button is not drawn, so the card can
 * never claim to have done something.
 */
@Composable
fun InsightHighlight(
    insight: Insight,
    severity: HeliosSeverityKind = insight.severity.toSeverityKind(),
    modifier: Modifier = Modifier,
    demoQualifier: Boolean = false,
    onAction: (() -> Unit)? = null
) {
    val colors = LocalHeliosSemanticColors.current
    val foreground = colors.insight.foreground(severity)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(HeliosShape.md)
            .background(colors.backgroundTertiary)
            .border(
                HeliosElevation.MaterialTokens.innerStrokeWidth,
                colors.separatorHairline,
                HeliosShape.md
            )
            .padding(HeliosSpacing.cardPadding)
            .semantics(mergeDescendants = true) {
                contentDescription = "${severity.word()} advisory. ${insight.title}. ${insight.body}"
            },
        horizontalArrangement = Arrangement.spacedBy(HeliosSpacing.space3)
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .heightIn(min = 48.dp)
                .clip(HeliosShape.xs)
                .background(foreground)
        )
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = insight.title,
                    style = HeliosTypography.headline,
                    color = colors.textPrimary,
                    modifier = Modifier.weight(1f)
                )
                if (insight.metric != null) {
                    Text(
                        text = insight.metric,
                        style = HeliosTypography.title3,
                        color = foreground
                    )
                }
            }
            Spacer(Modifier.height(HeliosSpacing.space1))
            Text(
                text = insight.body,
                style = HeliosTypography.callout,
                color = colors.textSecondary
            )
            Spacer(Modifier.height(HeliosSpacing.space2))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(HeliosSpacing.space2)
            ) {
                Text(
                    text = severity.word(),
                    style = HeliosTypography.caption2,
                    color = foreground
                )
                if (insight.delta != null) {
                    Text(
                        text = insight.delta,
                        style = HeliosTypography.caption2,
                        color = colors.textTertiary
                    )
                }
                if (demoQualifier) {
                    Text(
                        text = "Demo data",
                        style = HeliosTypography.caption2,
                        color = colors.textTertiary
                    )
                }
            }
            if (insight.actionLabel != null && onAction != null) {
                Spacer(Modifier.height(HeliosSpacing.space2))
                HeliosGhostButton(text = insight.actionLabel, onClick = onAction)
            }
        }
    }
}

/**
 * The placeholder the advisory block is replaced by when the data cannot support advice
 * (DESIGN.md C3): stale, offline, or no reading yet.
 *
 * It states what the app is waiting for and offers the one action that can help. The insight
 * engine is never run against stale or simulated data to fill the space, which is why this
 * is a separate composable and not an empty [InsightHighlight].
 */
@Composable
fun InsightWaitingPlaceholder(
    reason: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    val colors = LocalHeliosSemanticColors.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(HeliosShape.md)
            .background(colors.backgroundSecondary)
            .border(
                HeliosElevation.MaterialTokens.innerStrokeWidth,
                colors.separatorHairline,
                HeliosShape.md
            )
            .padding(HeliosSpacing.cardPadding)
            .semantics(mergeDescendants = true) { contentDescription = reason },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(HeliosSpacing.space3)
    ) {
        Text(
            text = reason,
            style = HeliosTypography.callout,
            color = colors.textSecondary,
            modifier = Modifier.weight(1f)
        )
        if (actionLabel != null && onAction != null) {
            HeliosGhostButton(text = actionLabel, onClick = onAction)
        }
    }
}

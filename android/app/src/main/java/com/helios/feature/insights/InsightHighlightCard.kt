package com.helios.feature.insights

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.helios.core.designsystem.color.LocalHeliosSemanticColors
import com.helios.core.designsystem.component.HeliosGhostButton
import com.helios.core.designsystem.component.HeliosMark
import com.helios.core.designsystem.component.word
import com.helios.core.designsystem.layout.HeliosSpacing
import com.helios.core.designsystem.shape.HeliosShape
import com.helios.core.designsystem.type.HeliosTypography
import com.helios.core.domain.model.Insight

/**
 * The one ranked advisory: the screen's lead, larger than the feed rows and outlined in
 * its severity colour.
 *
 * Variants: positive, neutral, attention and critical; with and without a metric; with and
 * without an action; with and without the demo qualifier. Severity is written in the
 * eyebrow as well as coloured, and the action button only exists when a caller can honour
 * it, so nothing here is a dead control.
 */
@Composable
fun InsightHighlightCard(
    insight: Insight,
    modifier: Modifier = Modifier,
    demoQualifier: Boolean = false,
    onAction: (() -> Unit)? = null
) {
    val colors = LocalHeliosSemanticColors.current
    val severity = insight.severity.kind()
    val foreground = colors.insight.foreground(severity)
    val background = colors.insight.background(severity)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(HeliosShape.lg)
            .background(colors.backgroundTertiary)
            .border(1.dp, foreground.copy(alpha = 0.4f), HeliosShape.lg)
            .padding(HeliosSpacing.cardPadding)
            .semantics(mergeDescendants = true) {
                contentDescription = buildString {
                    append("Featured insight, ${insight.category.label()}, ${severity.word()}. ")
                    append(insight.title)
                    append(". ")
                    append(insight.body)
                    insight.metric?.let { append(". $it") }
                }
            },
        horizontalArrangement = Arrangement.spacedBy(HeliosSpacing.space4)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(HeliosShape.full)
                .background(background),
            contentAlignment = Alignment.Center
        ) {
            HeliosMark(size = 20.dp, color = foreground)
        }
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = ("helios\u00B0 insight \u00B7 " + severity.word()).uppercase(),
                    style = HeliosTypography.caption2,
                    color = foreground
                )
                Spacer(Modifier.width(HeliosSpacing.space2))
                Text(
                    text = insight.category.label(),
                    style = HeliosTypography.caption2,
                    color = colors.textTertiary
                )
            }
            Spacer(Modifier.height(HeliosSpacing.space2))
            Row(verticalAlignment = Alignment.Top) {
                Text(
                    text = insight.title,
                    style = HeliosTypography.headline,
                    color = colors.textPrimary,
                    modifier = Modifier.weight(1f)
                )
                if (insight.metric != null) {
                    Spacer(Modifier.width(HeliosSpacing.space3))
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
            if (insight.delta != null || demoQualifier) {
                Spacer(Modifier.height(HeliosSpacing.space2))
                Row(horizontalArrangement = Arrangement.spacedBy(HeliosSpacing.space3)) {
                    insight.delta?.let {
                        Text(text = it, style = HeliosTypography.caption2, color = colors.textTertiary)
                    }
                    if (demoQualifier) {
                        Text(
                            text = "Demo data",
                            style = HeliosTypography.caption2,
                            color = colors.textTertiary
                        )
                    }
                }
            }
            if (insight.actionLabel != null && onAction != null) {
                Spacer(Modifier.height(HeliosSpacing.space2))
                HeliosGhostButton(text = insight.actionLabel, onClick = onAction)
            }
        }
    }
}

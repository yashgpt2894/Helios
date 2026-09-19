package com.helios.feature.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.helios.core.designsystem.color.HeliosSeverityKind
import com.helios.core.designsystem.color.LocalHeliosSemanticColors
import com.helios.core.designsystem.component.word
import com.helios.core.designsystem.layout.HeliosSpacing
import com.helios.core.designsystem.shape.HeliosShape
import com.helios.core.designsystem.type.HeliosTypography
import com.helios.core.domain.model.Insight

/**
 * Featured advisory.
 *
 * Variants: positive, neutral, attention, critical; with and without a metric; with and
 * without the demo qualifier. Severity is written as a word as well as coloured, because
 * a colour is not a severity.
 */
@Composable
fun InsightHighlight(
    insight: Insight,
    modifier: Modifier = Modifier,
    severity: HeliosSeverityKind = HeliosSeverityKind.NEUTRAL,
    demoQualifier: Boolean = false
) {
    val colors = LocalHeliosSemanticColors.current
    val foreground = colors.insight.foreground(severity)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(HeliosShape.sm)
            .background(colors.backgroundTertiary)
            .padding(HeliosSpacing.space3),
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
            Text(
                text = insight.title,
                style = HeliosTypography.caption,
                fontWeight = FontWeight.SemiBold,
                color = colors.textPrimary
            )
            Text(
                text = insight.body,
                style = HeliosTypography.caption,
                color = colors.textSecondary,
                maxLines = 2
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(HeliosSpacing.space2)
            ) {
                Text(
                    text = severity.word(),
                    style = HeliosTypography.caption2,
                    color = foreground
                )
                if (demoQualifier) {
                    Spacer(Modifier.width(HeliosSpacing.space1))
                    Text(
                        text = "Demo data",
                        style = HeliosTypography.caption2,
                        color = colors.textTertiary
                    )
                }
            }
        }
        if (insight.metric != null) {
            Text(
                text = insight.metric,
                style = HeliosTypography.title3,
                fontWeight = FontWeight.SemiBold,
                color = foreground
            )
        }
    }
}

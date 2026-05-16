package com.helios.feature.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.helios.core.designsystem.color.HeliosColor
import com.helios.core.designsystem.shape.HeliosShape
import com.helios.core.domain.model.Insight
import com.helios.core.domain.model.InsightSeverity

/**
 * InsightHighlight: single insight card with severity color coding.
 */
@Composable
fun InsightHighlight(
    insight: Insight,
    modifier: Modifier = Modifier
) {
    val isDark = true
    val severityColor = when (insight.severity) {
        InsightSeverity.positive -> HeliosColor.Flow.ramp(500, isDark)
        InsightSeverity.neutral -> HeliosColor.Neutral.ramp(600, isDark)
        InsightSeverity.attention -> HeliosColor.GridImport.ramp(500, isDark)
        InsightSeverity.critical -> HeliosColor.Alert.ramp(500, isDark)
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(HeliosShape.sm)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(12.dp)
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(48.dp)
                .clip(HeliosShape.xs)
                .background(severityColor)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = insight.title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = insight.body,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                maxLines = 2
            )
        }
        if (insight.metric != null) {
            Text(
                text = insight.metric,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = severityColor
            )
        }
    }
}

package com.helios.feature.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.helios.core.designsystem.color.HeliosColor

/**
 * TopBar: app bar with brand mark and status indicator.
 */
@Composable
fun TopBar(
    subTitle: String = "",
    status: String = "ONLINE",
    modifier: Modifier = Modifier
) {
    val isDark = true
    val solarColor = HeliosColor.Solar.ramp(500, isDark)

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.background
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Helios\u00B0",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = solarColor
                )
                if (subTitle.isNotEmpty()) {
                    Text(
                        text = subTitle,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.alpha(0.5f)
                    )
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                StatusDot(color = solarColor)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = status,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    color = solarColor
                )
            }
        }
    }
}

@Composable
fun StatusDot(color: Color) {
    Box(
        modifier = Modifier
            .size(8.dp)
            .background(color, shape = androidx.compose.foundation.shape.CircleShape)
    )
}

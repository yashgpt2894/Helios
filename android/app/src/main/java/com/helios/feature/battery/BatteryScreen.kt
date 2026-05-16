package com.helios.feature.battery

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.helios.core.data.repository.TelemetryRepository
import com.helios.core.designsystem.color.HeliosColor

/**
 * BatteryScreen: SoC gauge + stats (M4 stub).
 */
@Composable
fun BatteryScreen() {
    val telemetry = remember { TelemetryRepository.readTelemetry() }
    val isDark = true
    val batteryColor = HeliosColor.Battery.ramp(500, isDark)
    val soc = telemetry.batterySoc.toFloat()

    val animatedSoc by animateFloatAsState(
        targetValue = soc / 100f,
        animationSpec = tween(1200),
        label = "socAnim"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Battery",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(24.dp))

        // Circular gauge
        Canvas(modifier = Modifier.size(180.dp)) {
            val stroke = 16f
            val arcSize = Size(size.width - stroke, size.height - stroke)
            val topLeft = Offset(stroke / 2, stroke / 2)

            // Background arc
            drawArc(
                color = batteryColor.copy(alpha = 0.15f),
                startAngle = 135f,
                sweepAngle = 270f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
            // SOC arc
            drawArc(
                color = batteryColor,
                startAngle = 135f,
                sweepAngle = 270f * animatedSoc,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
        }

        Text(
            text = "%.0f%%".format(telemetry.batterySoc),
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Light,
            color = batteryColor
        )
        Text(
            text = if (telemetry.batteryPowerW > 50) "Charging" else if (telemetry.batteryPowerW < -50) "Discharging" else "Idle",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )

        Spacer(modifier = Modifier.height(24.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            Stat(label = "Health", value = "%.1f%%".format(telemetry.batteryHealthPct))
            Stat(label = "Cycles", value = telemetry.batteryCycles.toString())
            Stat(label = "Temp", value = "%.1f°C".format(telemetry.batteryTempC))
        }
    }
}

@Composable
fun Stat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
    }
}

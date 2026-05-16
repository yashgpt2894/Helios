package com.helios.wear

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                HeliosWatchFace()
            }
        }
    }
}

@Composable
fun HeliosWatchFace() {
    var telemetry by remember { mutableStateOf(generateTelemetry()) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(30_000)
            telemetry = generateTelemetry()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Battery ring
        Box(
            modifier = Modifier.fillMaxWidth().height(80.dp),
            contentAlignment = Alignment.Center
        ) {
            BatteryRing(
                soc = telemetry.batterySoc.toFloat(),
                modifier = Modifier.fillMaxSize()
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "%.1f kW".format(telemetry.acPowerW / 1000),
                    fontSize = 18.sp,
                    color = Color(0xFFB88A2E),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "%.0f%%".format(telemetry.batterySoc),
                    fontSize = 12.sp,
                    color = Color(0xFFA59F90),
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // today kWh
        Text(
            text = "%.1f kWh today".format(telemetry.energyTodayKwh),
            fontSize = 10.sp,
            color = Color(0xFF7A7568),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun BatteryRing(soc: Float, modifier: Modifier = Modifier) {
    val batteryColor = Color(0xFF856B32)
    val bgColor = Color(0xFF1A1A1C)

    Canvas(modifier = modifier) {
        val strokeWidth = 12f
        val sweepAngle = (soc / 100f) * 360f
        val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)
        val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)

        // Background ring
        drawArc(
            color = bgColor,
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        // SOC ring
        drawArc(
            color = batteryColor,
            startAngle = -90f,
            sweepAngle = sweepAngle,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
    }
}

private data class TelemetrySnapshot(
    val acPowerW: Double,
    val batterySoc: Double,
    val energyTodayKwh: Double
)

private fun generateTelemetry(): TelemetrySnapshot {
    val hour = System.currentTimeMillis() % (24 * 60 * 60 * 1000) / (60 * 60 * 1000.0)
    val factor = if (hour in 6.0..20.0) {
        Math.sin((hour - 6.0) / 14.0 * Math.PI)
    } else 0.0
    val acPowerW = factor * 9600.0 * 0.7
    val batterySoc = 40.0 + (factor * 50.0)
    val energyTodayKwh = factor * 38.0
    return TelemetrySnapshot(
        acPowerW = acPowerW,
        batterySoc = batterySoc.coerceIn(0.0, 100.0),
        energyTodayKwh = energyTodayKwh
    )
}

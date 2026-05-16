package com.helios.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.helios.core.data.repository.ForecastRepository
import com.helios.core.data.repository.TelemetryRepository

class MediumWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            MediumWidgetContent()
        }
    }
}

class MediumWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = MediumWidget()
}

@Composable
private fun MediumWidgetContent() {
    val series = TelemetryRepository.buildTodaySeries()
    val forecastDays = ForecastRepository.forecastFlow.value?.days?.take(3) ?: emptyList()

    val sparklineMax = series.maxOfOrNull { it.productionW } ?: 1.0
    val sparklineValues = series.map { (it.productionW / sparklineMax).coerceIn(0.0, 1.0) }

    GlanceTheme {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(GlanceTheme.colors.surface)
                .padding(12.dp)
        ) {
            // Sparkline row
            Row(
                modifier = GlanceModifier.fillMaxWidth().padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Mini sparkline using bar representation
                sparklineValues.take(12).forEach { v ->
                    val heightPx = (v * 24).toInt().coerceAtLeast(2)
                    Box(
                        modifier = GlanceModifier
                            .size(width = 8.dp, height = heightPx.dp)
                            .background(ColorProvider(GlanceTheme.colors.primary))
                            .padding(start = 1.dp)
                    )
                }
            }

            // Forecast strip
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                forecastDays.forEach { day ->
                    Column(
                        modifier = GlanceModifier.defaultWeight(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = day.date.takeLast(2),
                            style = TextStyle(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = ColorProvider(GlanceTheme.colors.primary),
                                textAlign = TextAlign.Center
                            )
                        )
                        Text(
                            text = "%.1f".format(day.expectedKwh),
                            style = TextStyle(
                                fontSize = 10.sp,
                                color = ColorProvider(GlanceTheme.colors.onSurface),
                                textAlign = TextAlign.Center
                            )
                        )
                    }
                }
            }
        }
    }
}

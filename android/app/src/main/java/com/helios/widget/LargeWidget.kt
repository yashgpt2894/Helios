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
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.helios.core.data.repository.ForecastRepository
import com.helios.core.data.repository.TelemetryRepository

class LargeWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            LargeWidgetContent()
        }
    }
}

class LargeWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = LargeWidget()
}

@Composable
private fun LargeWidgetContent() {
    val telemetry = TelemetryRepository.readTelemetry()
    val forecastDays = ForecastRepository.forecastFlow.value?.days?.take(3) ?: emptyList()

    GlanceTheme {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(GlanceTheme.colors.surface)
                .padding(12.dp)
        ) {
            // Header: live kW + SOC
            Row(
                modifier = GlanceModifier.fillMaxWidth().padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = GlanceModifier.defaultWeight()) {
                    Text(
                        text = "Solar",
                        style = TextStyle(
                            fontSize = 10.sp,
                            color = ColorProvider(GlanceTheme.colors.onSurface)
                        )
                    )
                    Text(
                        text = "%.2f kW".format(telemetry.acPowerW / 1000),
                        style = TextStyle(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = ColorProvider(GlanceTheme.colors.primary)
                        )
                    )
                }
                Column(modifier = GlanceModifier.defaultWeight(), horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Battery",
                        style = TextStyle(
                            fontSize = 10.sp,
                            color = ColorProvider(GlanceTheme.colors.onSurface),
                            textAlign = TextAlign.End
                        )
                    )
                    Text(
                        text = "%.0f%%".format(telemetry.batterySoc),
                        style = TextStyle(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = ColorProvider(GlanceTheme.colors.onSurface),
                            textAlign = TextAlign.End
                        )
                    )
                }
            }

            // Energy flow snapshot (static text)
            Text(
                text = "Home: %.0f W | Grid: %+.0f W".format(
                    telemetry.homeLoadW,
                    if (telemetry.gridExportW > 0) telemetry.gridExportW else -telemetry.gridImportW
                ),
                style = TextStyle(
                    fontSize = 10.sp,
                    color = ColorProvider(GlanceTheme.colors.onSurface)
                ),
                modifier = GlanceModifier.fillMaxWidth().padding(bottom = 8.dp)
            )

            // Forecast strip
            if (forecastDays.isNotEmpty()) {
                Row(
                    modifier = GlanceModifier.fillMaxWidth().padding(bottom = 8.dp),
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

            // Stats row
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                StatItem("Today", "%.1f kWh".format(telemetry.energyTodayKwh))
                StatItem("Month", "%.0f kWh".format(telemetry.energyMonthKwh))
                StatItem("Irrad", "%.0f W/m2".format(telemetry.irradianceWm2))
                StatItem("Eff", "%.0f%%".format(if (telemetry.irradianceWm2 > 0) {
                    (telemetry.acPowerW / (telemetry.irradianceWm2 / 1000 * TelemetryRepository.SYSTEM_RATED_W)) * 100
                } else 0.0))
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(
        modifier = GlanceModifier.defaultWeight().padding(horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = TextStyle(
                fontSize = 9.sp,
                color = ColorProvider(GlanceTheme.colors.onSurface)
            ),
            maxLines = 1
        )
        Text(
            text = value,
            style = TextStyle(
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = ColorProvider(GlanceTheme.colors.primary)
            ),
            maxLines = 1
        )
    }
}

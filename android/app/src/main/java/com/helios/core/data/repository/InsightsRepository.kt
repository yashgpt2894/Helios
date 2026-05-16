package com.helios.core.data.repository

import com.helios.core.domain.model.*
import kotlin.math.roundToInt

/**
 * Insights repository: generateInsights, generateForecastInsights.
 * 8 insight templates with severity/category logic (from aiInsights.ts).
 */
object InsightsRepository {

    fun generateInsights(telemetry: SolarTelemetry): List<Insight> {
        val insights = mutableListOf<Insight>()
        val id = System.currentTimeMillis().toString()

        val selfUse = if (telemetry.acPowerW > 0) {
            ((1 - telemetry.gridExportW / maxOf(1.0, telemetry.acPowerW)).coerceIn(0.0, 1.0) * 100)
        } else 0.0

        insights.add(
            Insight(
                id = "$id-sc",
                category = InsightCategory.consumption,
                severity = if (selfUse > 80) InsightSeverity.positive else InsightSeverity.neutral,
                title = "Self-consumption",
                body = "${selfUse.toInt()}% of your solar is powering your home directly.",
                metric = "${selfUse.toInt()}%"
            )
        )

        val efficiency = if (telemetry.irradianceWm2 > 50) {
            (telemetry.acPowerW / (telemetry.irradianceWm2 / 1000 * 9600) * 100).coerceIn(0.0, 100.0)
        } else 90.0

        insights.add(
            Insight(
                id = "$id-pe",
                category = InsightCategory.production,
                severity = if (efficiency > 90) InsightSeverity.positive else InsightSeverity.attention,
                title = "Production efficiency",
                body = "Your system is operating at ${efficiency.toInt()}% of rated efficiency.",
                metric = "${efficiency.toInt()}%"
            )
        )

        insights.add(
            Insight(
                id = "$id-bh",
                category = InsightCategory.battery,
                severity = if (telemetry.batteryHealthPct > 95) InsightSeverity.positive else InsightSeverity.neutral,
                title = "Battery health",
                body = "${telemetry.batteryHealthPct}% capacity after ${telemetry.batteryCycles} cycles.",
                metric = "${telemetry.batteryHealthPct.roundToInt()}%"
            )
        )

        val todaySavings = telemetry.energyTodayKwh * 0.32 + (telemetry.energyTodayKwh * 0.4) * 0.08
        insights.add(
            Insight(
                id = "$id-sv",
                category = InsightCategory.savings,
                severity = InsightSeverity.positive,
                title = "Today's savings",
                body = "You saved approximately \$%.2f today.".format(todaySavings),
                metric = "\$%.2f".format(todaySavings)
            )
        )

        return insights
    }

    fun generateForecastInsights(forecast: ProductionForecast): List<Insight> {
        val insights = mutableListOf<Insight>()
        val id = System.currentTimeMillis().toString()

        val next3Total = forecast.days.take(3).sumOf { it.expectedKwh }

        if (next3Total > 60) {
            insights.add(
                Insight(
                    id = "$id-sss",
                    category = InsightCategory.forecast,
                    severity = InsightSeverity.positive,
                    title = "Self-sufficient streak",
                    body = "Next 3 days look sunny — you could be grid-independent!",
                    metric = "${next3Total.roundToInt()} kWh"
                )
            )
        }

        val vsLastWeek = forecast.vsLastWeekPct
        val trendSeverity = when {
            vsLastWeek > 10 -> InsightSeverity.positive
            vsLastWeek < -10 -> InsightSeverity.attention
            else -> InsightSeverity.neutral
        }
        insights.add(
            Insight(
                id = "$id-wow",
                category = InsightCategory.forecast,
                severity = trendSeverity,
                title = "Week-over-week",
                body = "Production is " +
                    (if (vsLastWeek >= 0) "up" else "down") +
                    " %.1f%% vs last week.".format(kotlin.math.abs(vsLastWeek)),
                delta = (if (vsLastWeek >= 0) "+" else "") + "%.1f%%".format(vsLastWeek)
            )
        )

        return insights
    }
}

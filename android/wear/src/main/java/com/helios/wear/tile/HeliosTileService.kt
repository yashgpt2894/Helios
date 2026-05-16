package com.helios.wear.tile

import android.content.Context
import androidx.wear.tiles.DeviceParametersBuilders
import androidx.wear.tiles.LayoutElementBuilders
import androidx.wear.tiles.ModifiersBuilders
import androidx.wear.tiles.ResourceBuilders
import androidx.wear.tiles.TileBuilders
import androidx.wear.tiles.material3.Button
import androidx.wear.tiles.material3.Chip
import androidx.wear.tiles.material3.ChipDefaults
import androidx.wear.tiles.material3.Text
import androidx.wear.tiles.material3.layouts.PrimaryLayout
import com.google.android.horologist.tiles.SuspendingTileService
import kotlin.math.sin

class HeliosTileService : SuspendingTileService() {

    override suspend fun resourcesRequest(requestParams: ResourceBuilders.Request): ResourceBuilders.Resources {
        return ResourceBuilders.Resources.Builder()
            .setVersion("1.0")
            .build()
    }

    override suspend fun tileRequest(
        requestParams: TileBuilders.TileRequest
    ): TileBuilders.Tile {
        val snap = generateTelemetry()
        val context = this@HeliosTileService

        return TileBuilders.Tile.Builder()
            .setResourcesVersion("1.0")
            .setTileTimeline(
                TileBuilders.TileTimeline.Builder().addTimelineEntry(
                    TileBuilders.TimelineEntry.Builder().setLayout(
                        createLayout(context, snap)
                    ).build()
                ).build()
            )
            .build()
    }

    private fun createLayout(
        context: Context,
        snap: TelemetrySnapshot
    ): LayoutElementBuilders.LayoutElement {
        val batteryPct = "%.0f%%".format(snap.batterySoc)
        val forecastSummary = buildForecastSummary()

        return PrimaryLayout.Builder(DeviceParametersBuilders.DeviceParameters.Builder().build())
            .setContent(
                LayoutElementBuilders.Column.Builder()
                    .addContent(
                        Text.Builder(context)
                            .setText("Helios: %.1f kW".format(snap.acPowerW / 1000))
                            .setMaxLines(1)
                            .setTypography(Typography.TITLE)
                            .setModifier(
                                ModifiersBuilders.Modifiers.Builder()
                                    .setPadding(
                                        ModifiersBuilders.Padding.Builder()
                                            .setBottom(8f)
                                            .build()
                                    ).build()
                            ).build()
                    )
                    .addContent(
                        Text.Builder(context)
                            .setText("Battery: $batteryPct")
                            .setMaxLines(1)
                            .setTypography(Typography.BODY)
                            .setModifier(
                                ModifiersBuilders.Modifiers.Builder()
                                    .setPadding(
                                        ModifiersBuilders.Padding.Builder()
                                            .setBottom(8f)
                                            .build()
                                    ).build()
                            ).build()
                    )
                    .addContent(
                        Text.Builder(context)
                            .setText(forecastSummary)
                            .setMaxLines(2)
                            .setTypography(Typography.CAPTION)
                            .setModifier(
                                ModifiersBuilders.Modifiers.Builder()
                                    .setPadding(
                                        ModifiersBuilders.Padding.Builder()
                                            .setBottom(12f)
                                            .build()
                                    ).build()
                            ).build()
                    )
                    .addContent(
                        Button.Builder(context)
                            .setText("Open Helios")
                            .setClickable(
                                ModifiersBuilders.Clickable.Builder()
                                    .setOnClick(
                                        androidx.wear.tiles.ActionBuilders.LaunchAction.Builder()
                                            .setAndroidActivity(
                                                androidx.wear.tiles.ActionBuilders.AndroidActivity.Builder()
                                                    .setPackageName("com.helios.wear")
                                                    .setClassName("com.helios.wear.MainActivity")
                                                    .build()
                                            ).build()
                                    ).build()
                            ).build()
                    )
                    .build()
            )
            .build()
    }

    private fun buildForecastSummary(): String {
        return "Today: ~38 kWh | Tomorrow: ~35 kWh | Hot & clear"
    }

    private fun generateTelemetry(): TelemetrySnapshot {
        val hour = System.currentTimeMillis() % (24 * 60 * 60 * 1000) / (60 * 60 * 1000.0)
        val factor = if (hour in 6.0..20.0) {
            sin((hour - 6.0) / 14.0 * Math.PI)
        } else 0.0
        val acPowerW = factor * 9600.0 * 0.7
        val batterySoc = 40.0 + (factor * 50.0)
        return TelemetrySnapshot(acPowerW, batterySoc)
    }

    private data class TelemetrySnapshot(
        val acPowerW: Double,
        val batterySoc: Double
    )

    private object Typography {
        const val TITLE = "title"
        const val BODY = "body"
        const val CAPTION = "caption"
    }
}

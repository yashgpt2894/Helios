package com.helios.wear.complication

import android.app.PendingIntent
import android.content.Intent
import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.data.ComplicationText
import androidx.wear.watchface.complications.data.MonochromaticImage
import androidx.wear.watchface.complications.data.PlainComplicationText
import androidx.wear.watchface.complications.data.RangedValueComplicationData
import androidx.wear.watchface.complications.data.ShortTextComplicationData
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService
import kotlin.math.sin

class HeliosComplicationService : SuspendingComplicationDataSourceService() {

    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData? {
        val snap = generateTelemetry()
        val tapIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, Class.forName("com.helios.wear.MainActivity")),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return when (request.complicationType) {
            ComplicationType.SHORT_TEXT -> {
                ShortTextComplicationData.Builder(
                    text = PlainComplicationText.Builder("${"%.1f".format(snap.acPowerW / 1000)}kW").build(),
                    contentDescription = PlainComplicationText.Builder("Solar production").build()
                )
                    .setMonochromaticImage(MonochromaticImage.ICON_SUN)
                    .setTapAction(tapIntent)
                    .build()
            }
            ComplicationType.RANGED_VALUE -> {
                RangedValueComplicationData.Builder(
                    value = snap.batterySoc.toFloat(),
                    min = 0f,
                    max = 100f,
                    contentDescription = PlainComplicationText.Builder("Battery level").build()
                )
                    .setText(PlainComplicationText.Builder("${"%.0f".format(snap.batterySoc)}%").build())
                    .setTapAction(tapIntent)
                    .build()
            }
            ComplicationType.LONG_TEXT -> {
                ShortTextComplicationData.Builder(
                    text = PlainComplicationText.Builder(
                        "${"%.1f".format(snap.acPowerW / 1000)}kW | ${"%.0f".format(snap.batterySoc)}%"
                    ).build(),
                    contentDescription = PlainComplicationText.Builder("Solar and battery status").build()
                )
                    .setMonochromaticImage(MonochromaticImage.ICON_SUN)
                    .setTapAction(tapIntent)
                    .build()
            }
            else -> null
        }
    }

    override fun getPreviewData(type: ComplicationType): ComplicationData? {
        return when (type) {
            ComplicationType.SHORT_TEXT -> {
                ShortTextComplicationData.Builder(
                    text = PlainComplicationText.Builder("6.7kW").build(),
                    contentDescription = PlainComplicationText.Builder("Solar production preview").build()
                )
                    .setMonochromaticImage(MonochromaticImage.ICON_SUN)
                    .build()
            }
            ComplicationType.RANGED_VALUE -> {
                RangedValueComplicationData.Builder(
                    value = 72f,
                    min = 0f,
                    max = 100f,
                    contentDescription = PlainComplicationText.Builder("Battery preview").build()
                )
                    .setText(PlainComplicationText.Builder("72%").build())
                    .build()
            }
            else -> null
        }
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
}

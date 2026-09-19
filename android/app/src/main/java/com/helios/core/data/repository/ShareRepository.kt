package com.helios.core.data.repository

import com.helios.core.domain.model.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import java.util.Base64

/**
 * Share service: base64url encode/decode snapshots identical to PWA v1.
 */
object ShareRepository {

    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    fun encodeSnapshot(payload: SnapshotPayload): String {
        val jsonString = json.encodeToString(payload)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(jsonString.toByteArray())
    }

    fun decodeSnapshot(encoded: String): SnapshotPayload? {
        return try {
            val bytes = Base64.getUrlDecoder().decode(encoded)
            val jsonString = String(bytes)
            json.decodeFromString<SnapshotPayload>(jsonString)
        } catch (e: Exception) {
            null
        }
    }

    fun buildSnapshot(
        telemetry: SolarTelemetry,
        locationLabel: String = "",
        forecastDays: List<Double>? = null,
        brandId: String? = null
    ): SnapshotPayload {
        val selfUse = if (telemetry.acPowerW > 0) {
            ((1 - telemetry.gridExportW / maxOf(1.0, telemetry.acPowerW)).coerceIn(0.0, 1.0) * 100)
        } else 0.0

        return SnapshotPayload(
            v = 1,
            ts = telemetry.timestamp,
            loc = locationLabel,
            ac = Math.round(telemetry.acPowerW / 10.0) / 100.0,
            todayKwh = Math.round(telemetry.energyTodayKwh * 10) / 10.0,
            lifeKwh = Math.round(telemetry.energyLifetimeKwh * 10) / 10.0,
            soc = Math.round(telemetry.batterySoc).toDouble(),
            selfUse = Math.round(selfUse).toDouble(),
            fc = forecastDays,
            br = brandId
        )
    }

    fun buildShareUrl(payload: SnapshotPayload): String {
        return "https://helios.app/share/${encodeSnapshot(payload)}"
    }
}

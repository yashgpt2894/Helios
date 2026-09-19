package com.helios.core.data.repository

import com.helios.core.domain.model.SolarTelemetry
import com.helios.core.domain.model.SnapshotPayload
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.Base64

/**
 * Share service: base64url encode/decode of the v1 snapshot, byte-compatible with the
 * PWA `src/services/share.ts`.
 *
 * Three details make it byte-compatible rather than merely compatible:
 *  - `explicitNulls = false` omits `fc` and `br` exactly where the PWA writes
 *    `undefined`, while `encodeDefaults = true` still writes `v: 1`;
 *  - the integer fields are `Int`, so they print without a trailing decimal point;
 *  - `br` is omitted for the helios brand, which is what the PWA does.
 *
 * `ac` is kilowatts to 2 dp and `todayKwh` 1 dp, matching the PWA rounding expressions.
 */
object ShareRepository {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        explicitNulls = false
    }

    fun encodeSnapshot(payload: SnapshotPayload): String {
        val jsonString = json.encodeToString(payload)
        return Base64.getUrlEncoder().withoutPadding()
            .encodeToString(jsonString.toByteArray(Charsets.UTF_8))
    }

    fun decodeSnapshot(encoded: String): SnapshotPayload? {
        return try {
            val bytes = Base64.getUrlDecoder().decode(encoded)
            val jsonString = String(bytes, Charsets.UTF_8)
            val payload = json.decodeFromString<SnapshotPayload>(jsonString)
            if (payload.v != 1) null else payload
        } catch (error: Exception) {
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
            lifeKwh = Math.round(telemetry.energyLifetimeKwh).toInt(),
            soc = Math.round(telemetry.batterySoc).toInt(),
            selfUse = Math.round(selfUse).toInt(),
            fc = forecastDays?.map { Math.round(it).toInt() },
            br = if (brandId == null || brandId == "helios") null else brandId
        )
    }

    fun buildShareUrl(payload: SnapshotPayload): String {
        return "https://helios.app/share/${encodeSnapshot(payload)}"
    }
}

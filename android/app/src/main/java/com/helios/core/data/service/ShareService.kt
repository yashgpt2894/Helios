package com.helios.core.data.service

import com.helios.core.domain.model.SolarTelemetry
import com.helios.core.domain.model.SnapshotPayload

/** One way to move a snapshot off the device. */
data class ShareTarget(val id: String, val label: String, val available: Boolean)

/** The result of producing a shareable snapshot. */
sealed interface ShareOutcome {
    data class Ready(val encoded: String, val url: String, val payload: SnapshotPayload) : ShareOutcome
    data class Failed(val failure: ServiceFailure) : ShareOutcome
}

/**
 * Snapshot sharing (SVC-12 to SVC-15).
 *
 * The encoded payload must stay byte-compatible with the PWA v1 format: base64url
 * without padding, keys `v, ts, loc, ac, todayKwh, lifeKwh, soc, selfUse, fc, br`, and
 * the rounding rules of `src/services/share.ts`. Decoding rejects any other `v`.
 */
interface ShareService {

    fun encode(payload: SnapshotPayload): String

    fun decode(encoded: String): SnapshotPayload?

    fun buildSnapshot(
        telemetry: SolarTelemetry,
        locationLabel: String,
        forecastDays: List<Double>? = null,
        brandId: String? = null
    ): SnapshotPayload

    fun buildUrl(payload: SnapshotPayload): String

    /** Build everything the share sheet needs in one step. */
    fun prepare(
        telemetry: SolarTelemetry,
        locationLabel: String,
        forecastDays: List<Double>? = null,
        brandId: String? = null
    ): ShareOutcome

    fun targets(): List<ShareTarget>
}

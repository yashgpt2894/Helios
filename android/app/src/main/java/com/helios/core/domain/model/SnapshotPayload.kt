package com.helios.core.domain.model

import kotlinx.serialization.Serializable

/**
 * v1 shared-snapshot payload matched verbatim to PWA SnapshotPayload.
 */
@Serializable
data class SnapshotPayload(
    val v: Int = 1,
    val ts: Long,
    val loc: String,
    val ac: Double,
    val todayKwh: Double,
    val lifeKwh: Double,
    val soc: Double,
    val selfUse: Double,
    val fc: List<Double>? = null,
    val br: String? = null
)

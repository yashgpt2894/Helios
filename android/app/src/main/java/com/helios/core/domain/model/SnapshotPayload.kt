package com.helios.core.domain.model

import kotlinx.serialization.Serializable

/**
 * v1 shared-snapshot payload, matched verbatim to PWA `src/services/share.ts`.
 *
 * The field types follow what `JSON.stringify` prints, not what is convenient:
 * `lifeKwh`, `soc`, `selfUse` and every `fc` entry are integers, because the PWA passes
 * each through `Math.round`. A `Double` holding 18420.0 would serialise as "18420.0"
 * while the PWA writes "18420", and the two links would stop being byte-identical.
 *
 * `fc` and `br` are omitted when absent (the PWA writes `undefined` for `br` on the
 * helios brand), which the encoder does by not encoding nulls.
 */
@Serializable
data class SnapshotPayload(
    val v: Int = 1,
    val ts: Long,
    val loc: String,
    val ac: Double,
    val todayKwh: Double,
    val lifeKwh: Int,
    val soc: Int,
    val selfUse: Int,
    val fc: List<Int>? = null,
    val br: String? = null
)

package com.helios.core.domain.model

/**
 * Single panel string telemetry matched verbatim to PWA PanelString.
 */
data class PanelString(
    val id: String,
    val label: String,
    val powerW: Double,
    val voltageV: Double,
    val currentA: Double,
    val ratedW: Double,
    val panels: Int
)

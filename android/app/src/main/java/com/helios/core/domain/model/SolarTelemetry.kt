package com.helios.core.domain.model

/**
 * Full solar telemetry snapshot matched verbatim to PWA SolarTelemetry (28 fields).
 */
data class SolarTelemetry(
    val timestamp: Long,
    val manufacturer: String,
    val model: String,
    val serialNumber: String,
    val firmware: String,
    val status: InverterStatus,

    val acPowerW: Double,
    val acVoltageV: Double,
    val acCurrentA: Double,
    val acFrequencyHz: Double,

    val dcPowerW: Double,
    val dcVoltageV: Double,
    val dcCurrentA: Double,

    val cabinetTempC: Double,
    val heatsinkTempC: Double,

    val energyTodayKwh: Double,
    val energyMonthKwh: Double,
    val energyLifetimeKwh: Double,

    val batterySoc: Double,
    val batteryPowerW: Double,
    val batteryHealthPct: Double,
    val batteryCycles: Int,
    val batteryCapacityKwh: Double,
    val batteryTempC: Double,

    val homeLoadW: Double,
    val gridImportW: Double,
    val gridExportW: Double,

    val irradianceWm2: Double,
    val ambientTempC: Double,
    val cloudCoverPct: Double,

    val panels: List<PanelString>
)

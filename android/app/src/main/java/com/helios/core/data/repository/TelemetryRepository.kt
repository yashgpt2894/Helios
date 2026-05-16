package com.helios.core.data.repository

import com.helios.core.domain.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.*
import kotlin.random.Random

/**
 * Telemetry simulation matching PWA src/services/sunspec.ts byte-for-byte.
 */
object TelemetryRepository {

    val SYSTEM_RATED_W = 9600.0
    val BATTERY_CAPACITY_KWH = 13.5
    val CONVERSION_EFFICIENCY = 0.964

    private val STRINGS = listOf(
        Triple("A", "Roof \u00B7 South-East", Triple(3200.0, 8, -0.15)),
        Triple("B", "Roof \u00B7 South-West", Triple(3200.0, 8, 0.15)),
        Triple("C", "Garage \u00B7 South", Triple(3200.0, 8, 0.0))
    )

    private var cachedBattery = 62.0
    private var lifetimeKwh = 18420.5
    private var monthKwh = 412.7
    private var lastSampleAt = System.currentTimeMillis()

    private val _telemetry = MutableStateFlow<SolarTelemetry?>(null)
    val telemetryFlow: Flow<SolarTelemetry?> = _telemetry.asStateFlow()

    private val _todaySeries = MutableStateFlow<List<HistoryPoint>>(emptyList())
    val todaySeriesFlow: Flow<List<HistoryPoint>> = _todaySeries.asStateFlow()

    private fun jitter(base: Double, pct: Double): Double {
        return base * (1 + (Random.nextDouble() * 2 - 1) * pct)
    }

    private fun buildPanels(totalDcPowerW: Double, hour: Double): List<PanelString> {
        val sunSkew = clamp((hour - 12) / 6, -1.0, 1.0)
        return STRINGS.map { (id, label, params) ->
            val (ratedW, panels, orientation) = params
            val orientationGain = 1 + orientation * sunSkew
            val fractionOfRated = (ratedW / SYSTEM_RATED_W) * orientationGain
            val power = clamp(totalDcPowerW * fractionOfRated, 0.0, ratedW)
            val voltage = if (power > 30) jitter(380 + id[0].code % 20, 0.01) else 0.0
            val current = if (voltage > 0) power / voltage else 0.0
            PanelString(
                id = id,
                label = label,
                powerW = power,
                voltageV = voltage,
                currentA = current,
                ratedW = ratedW,
                panels = panels
            )
        }
    }

    private fun deriveStatus(hour: Double, dcPowerW: Double, batterySoc: Double): InverterStatus {
        if (hour < 5.8 || hour > 20.2) return InverterStatus.NIGHT
        if (dcPowerW < 50) return InverterStatus.STANDBY
        if (dcPowerW > SYSTEM_RATED_W * 0.95 && batterySoc > 99) return InverterStatus.CURTAILED
        return InverterStatus.PRODUCING
    }

    fun readTelemetry(): SolarTelemetry {
        val now = System.currentTimeMillis()
        val hour = SolarCurve.nowAsHourFloat(now)
        val cloudCover = 0.18 + 0.12 * sin(now / 1000.0 / 240.0)

        val irradiance = SolarCurve.irradianceAt(hour, cloudCover)
        val dcPowerIdeal = (irradiance / 1000) * SYSTEM_RATED_W
        val dcPowerW = jitter(max(0.0, dcPowerIdeal), 0.03)

        val acPowerW = dcPowerW * CONVERSION_EFFICIENCY

        val homeLoadIdeal = SolarCurve.consumptionFractionAt(hour) * 4200
        val homeLoadW = jitter(homeLoadIdeal, 0.07)

        val surplusW = acPowerW - homeLoadW

        val dt = (now - lastSampleAt) / 1000.0
        lastSampleAt = now

        var batteryPowerW = 0.0
        var gridImportW = 0.0
        var gridExportW = 0.0

        if (surplusW >= 0) {
            if (cachedBattery < 100) {
                batteryPowerW = min(surplusW, 5000.0)
                cachedBattery += (batteryPowerW * (dt / 3600.0)) / (BATTERY_CAPACITY_KWH * 10)
                val remaining = surplusW - batteryPowerW
                gridExportW = remaining
            } else {
                gridExportW = surplusW
            }
        } else {
            val deficit = -surplusW
            if (cachedBattery > 12) {
                batteryPowerW = -min(deficit, 5000.0)
                cachedBattery += (batteryPowerW * (dt / 3600.0)) / (BATTERY_CAPACITY_KWH * 10)
                val stillNeeded = deficit - abs(batteryPowerW)
                gridImportW = max(0.0, stillNeeded)
            } else {
                gridImportW = deficit
            }
        }

        cachedBattery = clamp(cachedBattery, 0.0, 100.0)

        val dcVoltageV = if (dcPowerW > 50) jitter(382.0, 0.012) else 0.0
        val dcCurrentA = if (dcVoltageV > 0) dcPowerW / dcVoltageV else 0.0
        val acVoltageV = jitter(240.0, 0.005)
        val acCurrentA = acPowerW / acVoltageV
        val acFrequencyHz = jitter(60.0, 0.0008)

        val baseHeat = 28 + (dcPowerW / SYSTEM_RATED_W) * 22
        val heatsinkTempC = jitter(baseHeat, 0.04)
        val cabinetTempC = jitter(baseHeat - 6, 0.04)
        val ambientTempC = jitter(18 + 8 * SolarCurve.solarFractionAt(hour), 0.03)
        val batteryTempC = jitter(24 + abs(batteryPowerW) / 800, 0.03)

        val energyAddedKwh = (acPowerW * (dt / 3600.0)) / 1000.0
        lifetimeKwh += max(0.0, energyAddedKwh)
        monthKwh += max(0.0, energyAddedKwh)
        val energyTodayKwh = computeTodayKwh(hour, cloudCover)

        return SolarTelemetry(
            timestamp = now,
            manufacturer = "helios\u00B0",
            model = "HX-9.6 Hybrid Inverter",
            serialNumber = "HX-2025-0F31A2",
            firmware = "4.12.1",
            status = deriveStatus(hour, dcPowerW, cachedBattery),
            acPowerW = acPowerW,
            acVoltageV = acVoltageV,
            acCurrentA = acCurrentA,
            acFrequencyHz = acFrequencyHz,
            dcPowerW = dcPowerW,
            dcVoltageV = dcVoltageV,
            dcCurrentA = dcCurrentA,
            cabinetTempC = cabinetTempC,
            heatsinkTempC = heatsinkTempC,
            energyTodayKwh = energyTodayKwh,
            energyMonthKwh = monthKwh,
            energyLifetimeKwh = lifetimeKwh,
            batterySoc = cachedBattery,
            batteryPowerW = batteryPowerW,
            batteryHealthPct = 97.4,
            batteryCycles = 312,
            batteryCapacityKwh = BATTERY_CAPACITY_KWH,
            batteryTempC = batteryTempC,
            homeLoadW = homeLoadW,
            gridImportW = gridImportW,
            gridExportW = gridExportW,
            irradianceWm2 = irradiance,
            ambientTempC = ambientTempC,
            cloudCoverPct = cloudCover * 100,
            panels = buildPanels(dcPowerW, hour)
        ).also { _telemetry.value = it }
    }

    private fun computeTodayKwh(currentHour: Double, cloudCover: Double): Double {
        var total = 0.0
        val step = 0.25
        var h = 0.0
        while (h <= currentHour) {
            val w = (SolarCurve.irradianceAt(h, cloudCover) / 1000.0) * SYSTEM_RATED_W * 0.964
            total += (w * step) / 1000.0
            h += step
        }
        return total
    }

    fun buildTodaySeries(hourNow: Double = SolarCurve.nowAsHourFloat()): List<HistoryPoint> {
        val points = mutableListOf<HistoryPoint>()
        val cloudCover = 0.2
        var h = 0.0
        while (h <= 24.0) {
            val irr = SolarCurve.irradianceAt(h, cloudCover)
            val ideal = (irr / 1000.0) * SYSTEM_RATED_W * 0.964
            val isPast = h <= hourNow
            val productionW = if (isPast) ideal * (1 + sin(h * 3) * 0.05) else ideal
            val consumptionW = SolarCurve.consumptionFractionAt(h) * 4200
            val surplus = productionW - consumptionW
            val batteryW = clamp(surplus, -5000.0, 5000.0) * 0.7
            val gridW = surplus - batteryW
            points.add(
                HistoryPoint(
                    t = h,
                    productionW = productionW,
                    consumptionW = consumptionW,
                    batteryW = batteryW,
                    gridW = gridW,
                    irradianceWm2 = irr
                )
            )
            h += 0.5
        }
        return points.also { _todaySeries.value = it }
    }

    fun buildWeekSeries(): List<Triple<String, Double, Double>> {
        val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        return days.mapIndexed { i, day ->
            val factor = 0.85 + 0.18 * sin(i * 1.3)
            val produced = 38.0 * factor + (Random.nextDouble() * 4 - 2)
            val consumed = 28.0 + (Random.nextDouble() * 6 - 3)
            Triple(day, max(0.0, produced), max(0.0, consumed))
        }
    }

    fun tick() {
        readTelemetry()
    }

    private fun clamp(value: Double, min: Double, max: Double): Double {
        return value.coerceIn(min, max)
    }
}

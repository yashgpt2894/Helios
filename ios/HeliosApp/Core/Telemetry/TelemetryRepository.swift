import Foundation
import Observation

// MARK: - SunspecService — byte-for-byte port of services/sunspec.ts

@Observable
final class TelemetryRepository {
    private(set) var telemetry: SolarTelemetry
    private(set) var todaySeries: [HistoryPoint] = []
    private(set) var liveSeries: [HistoryPoint] = []

    private var cachedBattery: Double = 62
    private var lifetimeKwh: Double = 18420.5
    private var monthKwh: Double = 412.7
    private var lastSampleAt: TimeInterval = Date().timeIntervalSince1970 * 1000

    // MARK: - Constants (Section 2.1)

    static let systemRatedW: Double = 9600
    static let batteryCapacityKwh: Double = 13.5
    static let conversionEfficiency: Double = 0.964

    // String definitions (Section 2.3)
    static let strings: [(id: String, label: String, ratedW: Double, panels: Int, orientation: Double)] = [
        ("A", "Roof \u{00b7} South-East", 3200, 8, -0.15),
        ("B", "Roof \u{00b7} South-West", 3200, 8, 0.15),
        ("C", "Garage \u{00b7} South", 3200, 8, 0)
    ]

    init() {
        self.telemetry = TelemetryRepository.readTelemetryInternal(
            cachedBattery: &cachedBattery,
            lifetimeKwh: &lifetimeKwh,
            monthKwh: &monthKwh,
            lastSampleAt: &lastSampleAt
        )
        self.todaySeries = TelemetryRepository.buildTodaySeriesInternal()
        let t = self.telemetry
        let point = HistoryPoint(
            t: t.timestamp,
            productionW: t.acPowerW,
            consumptionW: t.homeLoadW,
            batteryW: t.batteryPowerW,
            gridW: t.gridImportW - t.gridExportW,
            irradianceWm2: t.irradianceWm2
        )
        self.liveSeries = [point]
    }

    // MARK: - jitter (Section 2.2)

    private static func jitter(_ base: Double, _ pct: Double) -> Double {
        return base * (1 + (Double.random(in: 0..<1) * 2 - 1) * pct)
    }

    // MARK: - buildPanels (Section 2.3)

    private static func buildPanels(totalDcPowerW: Double, hour: Double) -> [PanelString] {
        let sunSkew = SolarCurve.clamp((hour - 12) / 6, -1, 1)
        return strings.map { s in
            let orientationGain = 1 + s.orientation * sunSkew
            let fractionOfRated = (s.ratedW / systemRatedW) * orientationGain
            let power = SolarCurve.clamp(totalDcPowerW * fractionOfRated, 0, s.ratedW)
            let voltage = power > 30 ? jitter(380 + Double(s.id.unicodeScalars.first?.value ?? 0).truncatingRemainder(dividingBy: 20), 0.01) : 0
            let current = voltage > 0 ? power / voltage : 0
            return PanelString(
                id: s.id,
                label: s.label,
                powerW: power,
                voltageV: voltage,
                currentA: current,
                ratedW: s.ratedW,
                panels: s.panels
            )
        }
    }

    // MARK: - deriveStatus (Section 2.4)

    private static func deriveStatus(hour: Double, dcPowerW: Double, batterySoc: Double) -> InverterStatus {
        if hour < 5.8 || hour > 20.2 { return .night }
        if dcPowerW < 50 { return .standby }
        if dcPowerW > systemRatedW * 0.95 && batterySoc > 99 { return .curtailed }
        return .producing
    }

    // MARK: - computeTodayKwh (Section 2.8)

    private static func computeTodayKwh(currentHour: Double, cloudCover: Double) -> Double {
        var total: Double = 0
        let step: Double = 0.25
        var h: Double = 0
        while h <= currentHour {
            let w = (SolarCurve.irradianceAt(h, cloudCover) / 1000) * systemRatedW * 0.964
            total += (w * step) / 1000
            h += step
        }
        return total
    }

    // MARK: - readTelemetry (Section 2.5 + 2.6 + 2.7)

    static func readTelemetryInternal(
        cachedBattery: inout Double,
        lifetimeKwh: inout Double,
        monthKwh: inout Double,
        lastSampleAt: inout TimeInterval
    ) -> SolarTelemetry {
        let now = Date()
        let hour = SolarCurve.nowAsHourFloat(now)
        let cloudCover = 0.18 + 0.12 * sin(now.timeIntervalSince1970 / 240)

        let irradiance = SolarCurve.irradianceAt(hour, cloudCover)
        let dcPowerIdeal = (irradiance / 1000) * systemRatedW
        let dcPowerW = jitter(Swift.max(0, dcPowerIdeal), 0.03)

        let acPowerW = dcPowerW * conversionEfficiency

        let homeLoadIdeal = SolarCurve.consumptionFractionAt(hour) * 4200
        let homeLoadW = jitter(homeLoadIdeal, 0.07)

        let surplusW = acPowerW - homeLoadW

        let dt = (now.timeIntervalSince1970 * 1000 - lastSampleAt) / 1000
        lastSampleAt = now.timeIntervalSince1970 * 1000

        var batteryPowerW: Double = 0
        var gridImportW: Double = 0
        var gridExportW: Double = 0

        if surplusW >= 0 {
            if cachedBattery < 100 {
                batteryPowerW = Swift.min(surplusW, 5000)
                cachedBattery += (batteryPowerW * (dt / 3600)) / (batteryCapacityKwh * 10)
                let remaining = surplusW - batteryPowerW
                gridExportW = remaining
            } else {
                gridExportW = surplusW
            }
        } else {
            let deficit = -surplusW
            if cachedBattery > 12 {
                batteryPowerW = -Swift.min(deficit, 5000)
                cachedBattery += (batteryPowerW * (dt / 3600)) / (batteryCapacityKwh * 10)
                let stillNeeded = deficit - abs(batteryPowerW)
                gridImportW = Swift.max(0, stillNeeded)
            } else {
                gridImportW = deficit
            }
        }

        cachedBattery = SolarCurve.clamp(cachedBattery, 0, 100)

        let dcVoltageV = dcPowerW > 50 ? jitter(382, 0.012) : 0
        let dcCurrentA = dcVoltageV > 0 ? dcPowerW / dcVoltageV : 0
        let acVoltageV = jitter(240, 0.005)
        let acCurrentA = acPowerW / acVoltageV
        let acFrequencyHz = jitter(60, 0.0008)

        let baseHeat = 28 + (dcPowerW / systemRatedW) * 22
        let heatsinkTempC = jitter(baseHeat, 0.04)
        let cabinetTempC = jitter(baseHeat - 6, 0.04)
        let ambientTempC = jitter(18 + 8 * SolarCurve.solarFractionAt(hour), 0.03)
        let batteryTempC = jitter(24 + abs(batteryPowerW) / 800, 0.03)

        let energyAddedKwh = (acPowerW * (dt / 3600)) / 1000
        lifetimeKwh += Swift.max(0, energyAddedKwh)
        monthKwh += Swift.max(0, energyAddedKwh)
        let energyTodayKwh = computeTodayKwh(currentHour: hour, cloudCover: cloudCover)

        return SolarTelemetry(
            timestamp: now.timeIntervalSince1970 * 1000,
            manufacturer: "helios\u{00b0}",
            model: "HX-9.6 Hybrid Inverter",
            serialNumber: "HX-2025-0F31A2",
            firmware: "4.12.1",
            status: deriveStatus(hour: hour, dcPowerW: dcPowerW, batterySoc: cachedBattery),
            acPowerW: acPowerW,
            acVoltageV: acVoltageV,
            acCurrentA: acCurrentA,
            acFrequencyHz: acFrequencyHz,
            dcPowerW: dcPowerW,
            dcVoltageV: dcVoltageV,
            dcCurrentA: dcCurrentA,
            cabinetTempC: cabinetTempC,
            heatsinkTempC: heatsinkTempC,
            energyTodayKwh: energyTodayKwh,
            energyMonthKwh: monthKwh,
            energyLifetimeKwh: lifetimeKwh,
            batterySoc: cachedBattery,
            batteryPowerW: batteryPowerW,
            batteryHealthPct: 97.4,
            batteryCycles: 312,
            batteryCapacityKwh: batteryCapacityKwh,
            batteryTempC: batteryTempC,
            homeLoadW: homeLoadW,
            gridImportW: gridImportW,
            gridExportW: gridExportW,
            irradianceWm2: irradiance,
            ambientTempC: ambientTempC,
            cloudCoverPct: cloudCover * 100,
            panels: buildPanels(totalDcPowerW: dcPowerW, hour: hour)
        )
    }

    // MARK: - Public API

    func readTelemetry() -> SolarTelemetry {
        let result = TelemetryRepository.readTelemetryInternal(
            cachedBattery: &cachedBattery,
            lifetimeKwh: &lifetimeKwh,
            monthKwh: &monthKwh,
            lastSampleAt: &lastSampleAt
        )
        return result
    }

    func tick() {
        let t = readTelemetry()
        telemetry = t

        let point = HistoryPoint(
            t: t.timestamp,
            productionW: t.acPowerW,
            consumptionW: t.homeLoadW,
            batteryW: t.batteryPowerW,
            gridW: t.gridImportW - t.gridExportW,
            irradianceWm2: t.irradianceWm2
        )
        liveSeries.append(point)
        if liveSeries.count > 180 {
            liveSeries = Array(liveSeries.suffix(180))
        }
    }

    // MARK: - buildTodaySeries (Section 2.9)

    static func buildTodaySeriesInternal() -> [HistoryPoint] {
        var points: [HistoryPoint] = []
        let hourNow = SolarCurve.nowAsHourFloat()
        let cloudCover: Double = 0.2
        var h: Double = 0
        while h <= 24 {
            let irr = SolarCurve.irradianceAt(h, cloudCover)
            let ideal = (irr / 1000) * systemRatedW * 0.964
            let isPast = h <= hourNow
            let productionW = isPast ? ideal * (1 + sin(h * 3) * 0.05) : ideal
            let consumptionW = SolarCurve.consumptionFractionAt(h) * 4200
            let surplus = productionW - consumptionW
            let batteryW = SolarCurve.clamp(surplus, -5000, 5000) * 0.7
            let gridW = surplus - batteryW
            points.append(HistoryPoint(
                t: h,
                productionW: productionW,
                consumptionW: consumptionW,
                batteryW: batteryW,
                gridW: gridW,
                irradianceWm2: irr
            ))
            h += 0.5
        }
        return points
    }

    func buildTodaySeries() -> [HistoryPoint] {
        return TelemetryRepository.buildTodaySeriesInternal()
    }

    // MARK: - buildWeekSeries (Section 2.10)

    static func buildWeekSeriesInternal() -> [(day: String, produced: Double, consumed: Double)] {
        let days = ["Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"]
        return days.enumerated().map { (i, day) in
            let factor = 0.85 + 0.18 * sin(Double(i) * 1.3)
            let produced = 38 * factor + (Double.random(in: 0..<1) * 4 - 2)
            let consumed = 28 + (Double.random(in: 0..<1) * 6 - 3)
            return (day, Swift.max(0, produced), Swift.max(0, consumed))
        }
    }

    func buildWeekSeries() -> [(day: String, produced: Double, consumed: Double)] {
        return TelemetryRepository.buildWeekSeriesInternal()
    }
}

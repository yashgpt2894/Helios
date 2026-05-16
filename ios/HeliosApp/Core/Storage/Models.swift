import Foundation
import SwiftData

// MARK: - Enum Types

enum InverterStatus: String, Codable, CaseIterable {
    case producing = "PRODUCING"
    case standby = "STANDBY"
    case curtailed = "CURTAILED"
    case night = "NIGHT"
    case fault = "FAULT"
}

enum InsightSeverity: String, Codable, CaseIterable {
    case positive
    case neutral
    case attention
    case critical
}

enum InsightCategory: String, Codable, CaseIterable {
    case production
    case consumption
    case battery
    case savings
    case maintenance
    case forecast
}

enum WeatherCondition: String, Codable, CaseIterable {
    case clear
    case mostlyClear = "mostly-clear"
    case partlyCloudy = "partly-cloudy"
    case overcast
    case fog
    case drizzle
    case rain
    case heavyRain = "heavy-rain"
    case snow
    case thunderstorm
}

enum LocationSource: String, Codable {
    case `default`
    case browser
    case manual
}

// MARK: - Data Structures

struct PanelString: Codable, Identifiable {
    let id: String
    let label: String
    let powerW: Double
    let voltageV: Double
    let currentA: Double
    let ratedW: Double
    let panels: Int
}

struct SolarTelemetry: Codable {
    let timestamp: TimeInterval
    let manufacturer: String
    let model: String
    let serialNumber: String
    let firmware: String
    let status: InverterStatus

    let acPowerW: Double
    let acVoltageV: Double
    let acCurrentA: Double
    let acFrequencyHz: Double

    let dcPowerW: Double
    let dcVoltageV: Double
    let dcCurrentA: Double

    let cabinetTempC: Double
    let heatsinkTempC: Double

    let energyTodayKwh: Double
    let energyMonthKwh: Double
    let energyLifetimeKwh: Double

    let batterySoc: Double
    let batteryPowerW: Double
    let batteryHealthPct: Double
    let batteryCycles: Int
    let batteryCapacityKwh: Double
    let batteryTempC: Double

    let homeLoadW: Double
    let gridImportW: Double
    let gridExportW: Double

    let irradianceWm2: Double
    let ambientTempC: Double
    let cloudCoverPct: Double

    let panels: [PanelString]
}

struct HistoryPoint: Codable, Identifiable {
    var id: Double { t }
    let t: Double
    let productionW: Double
    let consumptionW: Double
    let batteryW: Double
    let gridW: Double
    let irradianceWm2: Double
}

struct Insight: Codable, Identifiable {
    let id: String
    let category: InsightCategory
    let severity: InsightSeverity
    let title: String
    let body: String
    let metric: String?
    let delta: String?
    let actionLabel: String?
}

struct LocationInfo: Codable {
    let lat: Double
    let lng: Double
    let label: String
    let source: LocationSource
}

struct ForecastDay: Codable, Identifiable {
    var id: String { date }
    let date: String
    let weatherCode: Int
    let condition: WeatherCondition
    let conditionLabel: String
    let tempHighC: Double
    let tempLowC: Double
    let precipitationMm: Double
    let shortwaveRadiationMJ: Double
    let cloudCoverPct: Double
    let expectedKwh: Double
    let expectedKwhVsTypical: Double
}

struct ProductionForecast: Codable {
    let fetchedAt: TimeInterval
    let location: LocationInfo
    let days: [ForecastDay]
    let totalKwh: Double
    let vsLastWeekPct: Double
}

struct BrandInfo: Codable {
    let id: String
    let name: String
    let legalName: String?
    let accent: String
    let accentLight: String
    let mark: String
    let textMark: String?
    let supportEmail: String?
    let supportUrl: String?
    let tagline: String?
}

struct SnapshotPayload: Codable {
    let v: Int
    let ts: TimeInterval
    let loc: String
    let ac: Double
    let todayKwh: Double
    let lifeKwh: Int
    let soc: Int
    let selfUse: Int
    let fc: [Int]?
    let br: String?
}

// MARK: - SwiftData Models

@Model
final class ConnectionConfig {
    var protocolType: String = "sunspec-modbus-tcp"
    var host: String = "192.168.1.42"
    var port: Int = 502
    var unitId: Int = 1
    var pollIntervalMs: Int = 2000
    var status: String = "simulated"

    init(protocolType: String = "sunspec-modbus-tcp",
         host: String = "192.168.1.42",
         port: Int = 502,
         unitId: Int = 1,
         pollIntervalMs: Int = 2000,
         status: String = "simulated") {
        self.protocolType = protocolType
        self.host = host
        self.port = port
        self.unitId = unitId
        self.pollIntervalMs = pollIntervalMs
        self.status = status
    }
}

@Model
final class ThemePreference {
    var themeValue: String = "auto"

    init(themeValue: String = "auto") {
        self.themeValue = themeValue
    }
}

@Model
final class BrandPreference {
    var brandId: String = "helios"

    init(brandId: String = "helios") {
        self.brandId = brandId
    }
}

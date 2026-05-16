import Foundation
import Observation

// MARK: - EnergyKitService — stub for HomeKit/EnergyKit integration

@Observable
final class EnergyKitService {
    struct DeviceInfo: Identifiable {
        let id: String
        let name: String
        let category: DeviceCategory
        let powerW: Double?
        let schedule: String?
    }

    enum DeviceCategory: String {
        case battery
        case inverter
        case evCharger
        case poolPump
        case waterHeater
        case hvac
        case appliance
        case lighting
        case unknown
    }

    struct ChargingSchedule: Identifiable {
        let id: String
        let deviceName: String
        let startTime: String
        let endTime: String
        let preferredSocPct: Int
        let priority: Int
    }

    private(set) var devices: [DeviceInfo] = []
    private(set) var chargingSchedule: [ChargingSchedule] = []
    private(set) var isAuthorized: Bool = false
    private(set) var isAvailable: Bool = false

    var isReady: Bool { isAuthorized && isAvailable }

    init() {
        // Stub: EnergyKit requires iOS 18+ entitlement and hardware
        // integration. Populated when entitlement is configured.
    }

    func requestAuthorization() async -> Bool {
        // Stub — real implementation requires Entitlements.plist
        // with com.apple.developer.homekit and EnergyKit capability.
        isAuthorized = false
        return false
    }

    func scanForDevices() async -> [DeviceInfo] {
        // Stub
        return []
    }

    func optimizeChargingSchedule(solarKwhForecast: [Double]) -> [ChargingSchedule] {
        // Stub — schedule EV/water-heater around forecast peaks
        return []
    }
}

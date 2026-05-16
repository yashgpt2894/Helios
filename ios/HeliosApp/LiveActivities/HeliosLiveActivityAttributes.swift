import ActivityKit
import Foundation

// MARK: - HeliosLiveActivityAttributes

struct HeliosLiveActivityAttributes: ActivityAttributes {

    // MARK: - ContentState (dynamic data)

    struct ContentState: Codable, Hashable {
        var currentKW: Double
        var batterySOC: Double
        var status: String
        var timestamp: TimeInterval
        var solarToHome: Double
        var solarToBattery: Double
        var solarToGrid: Double
        var homeConsumption: Double
    }

    // MARK: - Static attributes (set at creation)

    var inverterModel: String
    var systemRatingKW: Double
}

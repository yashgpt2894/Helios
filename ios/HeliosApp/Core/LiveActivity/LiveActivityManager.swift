import ActivityKit
import Foundation

// MARK: - LiveActivityManager

enum LiveActivityManager {

    private static var currentActivity: Activity<HeliosLiveActivityAttributes>?
    private static let appGroupID = "group.com.helios.app"

    // MARK: - Start

    @discardableResult
    static func start(
        currentKW: Double,
        batterySOC: Double,
        status: String,
        solarToHome: Double,
        solarToBattery: Double,
        solarToGrid: Double,
        homeConsumption: Double,
        inverterModel: String = "HX-9.6",
        systemRatingKW: Double = 9.6
    ) -> Activity<HeliosLiveActivityAttributes>? {
        guard ActivityAuthorizationInfo().areActivitiesEnabled else { return nil }

        let attributes = HeliosLiveActivityAttributes(
            inverterModel: inverterModel,
            systemRatingKW: systemRatingKW
        )

        let contentState = HeliosLiveActivityAttributes.ContentState(
            currentKW: currentKW,
            batterySOC: batterySOC,
            status: status,
            timestamp: Date().timeIntervalSince1970,
            solarToHome: solarToHome,
            solarToBattery: solarToBattery,
            solarToGrid: solarToGrid,
            homeConsumption: homeConsumption
        )

        do {
            let activity = try Activity.request(
                attributes: attributes,
                content: .init(state: contentState, staleDate: nil)
            )
            currentActivity = activity

            // Persist to App Group for widgets
            persistToAppGroup(
                kw: currentKW,
                soc: batterySOC,
                status: status
            )

            return activity
        } catch {
            return nil
        }
    }

    // MARK: - Update

    static func update(
        currentKW: Double,
        batterySOC: Double,
        status: String,
        solarToHome: Double,
        solarToBattery: Double,
        solarToGrid: Double,
        homeConsumption: Double
    ) {
        guard let activity = currentActivity else { return }

        let contentState = HeliosLiveActivityAttributes.ContentState(
            currentKW: currentKW,
            batterySOC: batterySOC,
            status: status,
            timestamp: Date().timeIntervalSince1970,
            solarToHome: solarToHome,
            solarToBattery: solarToBattery,
            solarToGrid: solarToGrid,
            homeConsumption: homeConsumption
        )

        Task {
            await activity.update(
                .init(state: contentState, staleDate: nil)
            )
        }

        persistToAppGroup(kw: currentKW, soc: batterySOC, status: status)
    }

    // MARK: - Stop

    static func stop() {
        guard let activity = currentActivity else { return }

        let finalState = HeliosLiveActivityAttributes.ContentState(
            currentKW: 0,
            batterySOC: activity.content.state.batterySOC,
            status: "ENDED",
            timestamp: Date().timeIntervalSince1970,
            solarToHome: 0,
            solarToBattery: 0,
            solarToGrid: 0,
            homeConsumption: 0
        )

        Task {
            await activity.end(
                .init(state: finalState, staleDate: nil),
                dismissalPolicy: .immediate
            )
        }

        currentActivity = nil
    }

    // MARK: - App Group persistence

    private static func persistToAppGroup(kw: Double, soc: Double, status: String) {
        guard let defaults = UserDefaults(suiteName: appGroupID) else { return }
        defaults.set(kw, forKey: "helios.currentKW")
        defaults.set(soc, forKey: "helios.batterySOC")
        defaults.set(status, forKey: "helios.status")
    }
}

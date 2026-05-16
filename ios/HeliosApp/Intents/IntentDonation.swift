import AppIntents
import Foundation

// MARK: - IntentDonation

enum IntentDonation {

    private static let appGroupID = "group.com.helios.app"

    // MARK: - Donate all intents on app launch

    @MainActor
    static func donateAllIntents(telemetryRepo: TelemetryRepository) {
        let telemetry = telemetryRepo.telemetry
        donateCurrentProduction(kw: telemetry.acPowerW / 1000)
        donateBatteryLevel(soc: telemetry.batterySoc)
        donateTodayProduction(kwh: telemetry.energyTodayKwh)
        donateForecast()
    }

    // MARK: - Individual donations

    @MainActor
    static func donateCurrentProduction(kw: Double) {
        let intent = GetCurrentProductionIntent()
        intent.donate()
    }

    @MainActor
    static func donateBatteryLevel(soc: Double) {
        let intent = GetBatteryLevelIntent()
        intent.donate()
    }

    @MainActor
    static func donateTodayProduction(kwh: Double) {
        let intent = GetTodayProductionIntent()
        intent.donate()
    }

    @MainActor
    static func donateForecast() {
        let intent = GetGridForecastIntent()
        intent.donate()
    }
}

// MARK: - AppShortcutsProvider

struct HeliosAppShortcuts: AppShortcutsProvider {

    static var appShortcuts: [AppShortcut] {
        AppShortcut(
            intent: GetCurrentProductionIntent(),
            phrases: [
                "How much power is my solar making?",
                "What's my solar output?",
                "Check my solar production with \(.applicationName)",
                "Solar power level"
            ],
            shortTitle: "Current Production",
            systemImageName: "sun.max.fill"
        )

        AppShortcut(
            intent: GetBatteryLevelIntent(),
            phrases: [
                "What's my battery level?",
                "How full is my battery?",
                "Check battery charge with \(.applicationName)",
                "Battery status"
            ],
            shortTitle: "Battery Level",
            systemImageName: "battery.75percent"
        )

        AppShortcut(
            intent: GetTodayProductionIntent(),
            phrases: [
                "How much solar did I make today?",
                "What's my solar energy today?",
                "Check today's production with \(.applicationName)",
                "Today's solar energy"
            ],
            shortTitle: "Today's Production",
            systemImageName: "chart.bar.fill"
        )

        AppShortcut(
            intent: GetGridForecastIntent(),
            phrases: [
                "What's my solar forecast?",
                "How much solar will I produce tomorrow?",
                "Check solar forecast with \(.applicationName)",
                "Solar production forecast"
            ],
            shortTitle: "Solar Forecast",
            systemImageName: "cloud.sun.fill"
        )
    }
}

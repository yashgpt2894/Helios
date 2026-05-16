import AppIntents
import Foundation

// MARK: - GetBatteryLevelIntent

struct GetBatteryLevelIntent: AppIntent {

    static let title: LocalizedStringResource = "Get Battery Level"
    static let description = IntentDescription(
        "Returns your battery state of charge as a percentage.",
        categoryName: "Energy",
        searchKeywords: ["battery", "charge", "percentage", "soc", "helios"]
    )
    static let openAppWhenRun: Bool = false

    @MainActor
    func perform() async throws -> some IntentResult & ReturnsValue<Double> {
        let defaults = UserDefaults(suiteName: "group.com.helios.app")
        let soc = defaults?.double(forKey: "helios.batterySOC") ?? 0

        return .result(value: soc)
    }
}

// MARK: - Siri Phrase Extension

extension GetBatteryLevelIntent {

    @Parameter(title: "Include Details")
    var includeDetails: Bool?

    static var parameterSummary: some ParameterSummary {
        Summary("Get current battery level")
    }
}

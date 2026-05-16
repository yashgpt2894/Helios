import AppIntents
import Foundation

// MARK: - GetTodayProductionIntent

struct GetTodayProductionIntent: AppIntent {

    static let title: LocalizedStringResource = "Get Today's Production"
    static let description = IntentDescription(
        "Returns today's total solar energy production in kilowatt-hours.",
        categoryName: "Energy",
        searchKeywords: ["today", "production", "energy", "kwh", "solar", "helios"]
    )
    static let openAppWhenRun: Bool = false

    @MainActor
    func perform() async throws -> some IntentResult & ReturnsValue<Double> {
        let defaults = UserDefaults(suiteName: "group.com.helios.app")
        let kwh = defaults?.double(forKey: "helios.todayKwh") ?? 0

        return .result(value: kwh)
    }
}

// MARK: - Siri Phrase Extension

extension GetTodayProductionIntent {

    @Parameter(title: "Include Details")
    var includeDetails: Bool?

    static var parameterSummary: some ParameterSummary {
        Summary("Get today's solar production")
    }
}

import AppIntents
import Foundation

// MARK: - GetCurrentProductionIntent

struct GetCurrentProductionIntent: AppIntent {

    static let title: LocalizedStringResource = "Get Current Production"
    static let description = IntentDescription(
        "Returns your current solar production in kilowatts.",
        categoryName: "Energy",
        searchKeywords: ["solar", "production", "kilowatts", "power", "helios"]
    )
    static let openAppWhenRun: Bool = false

    @MainActor
    func perform() async throws -> some IntentResult & ReturnsValue<Double> {
        let defaults = UserDefaults(suiteName: "group.com.helios.app")
        let kw = defaults?.double(forKey: "helios.currentKW") ?? 0

        return .result(value: kw)
    }
}

// MARK: - Siri Phrase Extension

extension GetCurrentProductionIntent {

    @Parameter(title: "Include Details")
    var includeDetails: Bool?

    static var parameterSummary: some ParameterSummary {
        Summary("Get current solar production")
    }
}

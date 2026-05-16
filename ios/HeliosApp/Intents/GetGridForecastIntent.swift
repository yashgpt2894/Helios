import AppIntents
import Foundation

// MARK: - ForecastDayData (local, for App Group transit)

fileprivate struct ForecastDayData: Codable {
    let label: String
    let expectedKwh: Double
    let condition: String
}

// MARK: - GetGridForecastIntent

struct GetGridForecastIntent: AppIntent {

    static let title: LocalizedStringResource = "Get Solar Forecast"
    static let description = IntentDescription(
        "Returns a summary of your solar production forecast.",
        categoryName: "Energy",
        searchKeywords: ["forecast", "solar", "prediction", "tomorrow", "weather", "helios"]
    )
    static let openAppWhenRun: Bool = false

    @MainActor
    func perform() async throws -> some IntentResult & ReturnsValue<String> {
        let defaults = UserDefaults(suiteName: "group.com.helios.app")

        var summaryText = "No forecast data available."

        if let forecastData = defaults?.data(forKey: "helios.forecastDays"),
           let decoded = try? JSONDecoder().decode([ForecastDayData].self, from: forecastData),
           !decoded.isEmpty {

            let today = decoded.first
            let tomorrow = decoded.count > 1 ? decoded[1] : nil
            let totalKwh = decoded.prefix(3).reduce(0) { $0 + $1.expectedKwh }

            var parts: [String] = []
            if let today = today {
                parts.append("Today: \(String(format: "%.1f", today.expectedKwh)) kWh (\(today.condition))")
            }
            if let tomorrow = tomorrow {
                parts.append("Tomorrow: \(String(format: "%.1f", tomorrow.expectedKwh)) kWh (\(tomorrow.condition))")
            }
            parts.append("3-day total: \(String(format: "%.1f", totalKwh)) kWh")

            summaryText = parts.joined(separator: ". ")
        }

        return .result(value: summaryText)
    }
}

// MARK: - Siri Phrase Extension

extension GetGridForecastIntent {

    @Parameter(title: "Include Details")
    var includeDetails: Bool?

    static var parameterSummary: some ParameterSummary {
        Summary("Get solar production forecast")
    }
}

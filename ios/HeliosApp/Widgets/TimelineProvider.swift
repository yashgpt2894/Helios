import WidgetKit
import SwiftUI

// MARK: - HeliosTimelineEntry

struct HeliosTimelineEntry: TimelineEntry {
    let date: Date
    let currentKW: Double
    let batterySOC: Double
    let todayKwh: Double
    let status: String
    let forecastDays: [TimelineForecastDay]
}

struct TimelineForecastDay {
    let label: String
    let expectedKwh: Double
    let condition: String
}

// MARK: - HeliosTimelineProvider

struct HeliosTimelineProvider: TimelineProvider {

    private static let appGroupID = "group.com.helios.app"

    // MARK: - Placeholder

    func placeholder(in context: Context) -> HeliosTimelineEntry {
        HeliosTimelineEntry(
            date: Date(),
            currentKW: 4.2,
            batterySOC: 78,
            todayKwh: 22.5,
            status: "PRODUCING",
            forecastDays: sampleForecast()
        )
    }

    // MARK: - Snapshot

    func getSnapshot(in context: Context, completion: @escaping (HeliosTimelineEntry) -> Void) {
        let entry = readCurrentEntry()
        completion(entry)
    }

    // MARK: - Timeline

    func getTimeline(in context: Context, completion: @escaping (Timeline<HeliosTimelineEntry>) -> Void) {
        let entry = readCurrentEntry()

        let refreshDate = Calendar.current.date(
            byAdding: .minute,
            value: 15,
            to: Date()
        ) ?? Date().addingTimeInterval(15 * 60)

        let timeline = Timeline(entries: [entry], policy: .after(refreshDate))
        completion(timeline)
    }

    // MARK: - Shared data reading

    private func readCurrentEntry() -> HeliosTimelineEntry {
        let defaults = UserDefaults(suiteName: Self.appGroupID)
        let kw = defaults?.double(forKey: "helios.currentKW") ?? 0
        let soc = defaults?.double(forKey: "helios.batterySOC") ?? 0
        let todayKwhVal = defaults?.double(forKey: "helios.todayKwh") ?? 0
        let status = defaults?.string(forKey: "helios.status") ?? "STANDBY"

        var forecast: [TimelineForecastDay] = []
        if let forecastData = defaults?.data(forKey: "helios.forecastDays"),
           let decoded = try? JSONDecoder().decode([ForecastDayData].self, from: forecastData) {
            forecast = decoded.prefix(3).map {
                TimelineForecastDay(label: $0.label, expectedKwh: $0.expectedKwh, condition: $0.condition)
            }
        }

        if forecast.isEmpty {
            forecast = sampleForecast()
        }

        return HeliosTimelineEntry(
            date: Date(),
            currentKW: kw,
            batterySOC: soc,
            todayKwh: todayKwhVal,
            status: status,
            forecastDays: forecast
        )
    }

    // MARK: - Sample forecast

    static func sampleForecast() -> [TimelineForecastDay] {
        [
            TimelineForecastDay(label: "Today", expectedKwh: 28.5, condition: "clear"),
            TimelineForecastDay(label: "Tomorrow", expectedKwh: 24.1, condition: "partly-cloudy"),
            TimelineForecastDay(label: "Sat", expectedKwh: 31.2, condition: "clear")
        ]
    }
}

// MARK: - ForecastDayData (Codable for App Group transit)

struct ForecastDayData: Codable {
    let label: String
    let expectedKwh: Double
    let condition: String
}

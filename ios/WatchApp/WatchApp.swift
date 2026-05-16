import SwiftUI

// MARK: - WatchApp Entry Point

@main
struct WatchApp: App {
    @State private var currentKW: Double = 4.2
    @State private var batterySOC: Double = 78
    @State private var todayKwh: Double = 22.5
    @State private var forecastDays: [WatchForecastDay] = WatchApp.sampleForecast()
    @State private var status: String = "PRODUCING"

    var body: some Scene {
        WindowGroup {
            WatchDashboardView(
                currentKW: $currentKW,
                batterySOC: $batterySOC,
                todayKwh: $todayKwh,
                forecastDays: $forecastDays,
                status: $status
            )
            .onAppear {
                startRefreshTimer()
            }
        }
    }

    // MARK: - Refresh timer (15-minute interval)

    private func startRefreshTimer() {
        Timer.scheduledTimer(withTimeInterval: 15 * 60, repeats: true) { _ in
            refreshFromAppGroup()
        }
        refreshFromAppGroup()
    }

    private func refreshFromAppGroup() {
        let defaults = UserDefaults(suiteName: "group.com.helios.app")
        let kw = defaults?.double(forKey: "helios.currentKW") ?? 0
        let soc = defaults?.double(forKey: "helios.batterySOC") ?? 0
        let kwh = defaults?.double(forKey: "helios.todayKwh") ?? 0
        let stat = defaults?.string(forKey: "helios.status") ?? "STANDBY"

        if kw > 0 {
            currentKW = kw
            batterySOC = soc
            todayKwh = kwh
            status = stat
        }

        if let forecastData = defaults?.data(forKey: "helios.forecastDays"),
           let decoded = try? JSONDecoder().decode([WatchForecastDay].self, from: forecastData) {
            forecastDays = Array(decoded.prefix(3))
        }
    }

    static func sampleForecast() -> [WatchForecastDay] {
        [
            WatchForecastDay(label: "Today", expectedKwh: 28.5, condition: "clear"),
            WatchForecastDay(label: "Tomorrow", expectedKwh: 24.1, condition: "partly-cloudy"),
            WatchForecastDay(label: "Sat", expectedKwh: 31.2, condition: "clear")
        ]
    }
}

// MARK: - WatchForecastDay (Codable)

struct WatchForecastDay: Codable, Identifiable {
    var id: String { label }
    let label: String
    let expectedKwh: Double
    let condition: String
}

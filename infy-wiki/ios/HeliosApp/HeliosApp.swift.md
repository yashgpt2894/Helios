# HeliosApp

```swift
import SwiftUI

/// Helios App entry point — SwiftUI / SwiftData host

@main
struct HeliosApp: App {
    @State private var telemetryRepo = TelemetryRepository()
    @State private var forecastRepo = ForecastRepository()
    @State private var themeService = ThemeService()

    var body: some Scene {
        WindowGroup {
            ContentView()
                .heliosTheme()
                .environment(telemetryRepo)
                .environment(forecastRepo)
                .environment(themeService)
        }
    }
}

// MARK: - ContentView (TabView shell)

struct ContentView: View {
    @Environment(ThemeService.self) private var themeService

    var body: some View {
        TabView {
            DashboardView()
                .tabItem {
                    Image(systemName: "sun.max")
                    Text("Dashboard")
                }

            ProductionView()
                .tabItem {
                    Image(systemName: "chart.xyaxis.line")
                    Text("Production")
                }

            InsightsView()
                .tabItem {
                    Image(systemName: "sparkles")
                    Text("Insights")
                }

            BatteryView()
                .tabItem {
                    Image(systemName: "battery.75")
                    Text("Battery")
                }

            SettingsView()
                .tabItem {
                    Image(systemName: "gearshape")
                    Text("Settings")
                }
        }
        .tint(.yellow)
        .onAppear {
            HeliosHaptics.prepare()
        }
    }
}

#Preview {
    ContentView()
        .environment(TelemetryRepository())
        .environment(ForecastRepository())
        .environment(ThemeService())
        .preferredColorScheme(.dark)
}

```
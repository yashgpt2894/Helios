# HeliosControl

```swift
import WidgetKit
import SwiftUI
import AppIntents

// MARK: - HeliosControlWidget

struct HeliosControlWidget: ControlWidget {

    var body: some ControlWidgetConfiguration {
        StaticControlConfiguration(
            kind: "com.helios.app.control"
        ) {
            ControlWidgetButton(
                action: OpenAppIntent()
            ) {
                Label {
                    VStack(spacing: 1) {
                        Text(String(format: "%.1f", HeliosControlWidget.readCurrentKW()))
                            .font(.system(size: 12, weight: .semibold, design: .monospacedDigit))
                        Text("kW")
                            .font(.system(size: 8, weight: .medium))
                            .foregroundStyle(.secondary)
                    }
                } icon: {
                    Image(systemName: "sun.max.fill")
                        .foregroundStyle(solarColor)
                }
            }
        }
        .displayName("Helios")
        .description("Solar production at a glance.")
        .promptsForUserConfiguration()
    }

    // MARK: - Data reading

    static func readCurrentKW() -> Double {
        let defaults = UserDefaults(suiteName: "group.com.helios.app")
        return defaults?.double(forKey: "helios.currentKW") ?? 0
    }

    // MARK: - Colors

    static var solarColor: Color {
        Color(red: 0.941, green: 0.776, blue: 0.455)
    }
}

// MARK: - Open App Intent for Control

struct OpenAppIntent: AppIntent {

    static let title: LocalizedStringResource = "Open Helios"
    static let openAppWhenRun: Bool = true

    @MainActor
    func perform() async throws -> some IntentResult {
        return .result()
    }
}

```
import WidgetKit
import SwiftUI

// MARK: - LockScreenCircularWidgetView

struct LockScreenCircularWidgetView: View {
    let entry: HeliosTimelineEntry

    var body: some View {
        ZStack {
            if #available(iOS 16.0, *) {
                AccessoryWidgetBackground()
            }

            VStack(spacing: 0) {
                Text(String(format: "%.0f", entry.batterySOC))
                    .font(.system(size: 20, weight: .bold, design: .rounded))
                    .foregroundStyle(batteryColor)

                Text("%")
                    .font(.system(size: 8, weight: .medium))
                    .foregroundStyle(.secondary)
            }
        }
    }

    private var batteryColor: Color {
        HeliosColor.flow(for: .dark, step: 400)
    }
}

// MARK: - LockScreenRectangularWidgetView

struct LockScreenRectangularWidgetView: View {
    let entry: HeliosTimelineEntry

    var body: some View {
        HStack(spacing: 12) {
            VStack(alignment: .leading, spacing: 2) {
                HStack(spacing: 4) {
                    Image(systemName: "sun.max.fill")
                        .font(.system(size: 10))
                        .foregroundStyle(solarColor)

                    Text(String(format: "%.1f kW", entry.currentKW))
                        .font(.system(size: 16, weight: .bold, design: .monospacedDigit))
                        .foregroundStyle(.primary)
                }

                Text(String(format: "%.1f kWh today", entry.todayKwh))
                    .font(.system(size: 10, weight: .medium))
                    .foregroundStyle(.secondary)
            }

            Spacer()

            batteryIndicator
        }
        .padding(.horizontal, 4)
    }

    private var batteryIndicator: some View {
        HStack(spacing: 3) {
            Image(systemName: batteryIconName)
                .font(.system(size: 12))

            Text(String(format: "%.0f%%", entry.batterySOC))
                .font(.system(size: 12, weight: .semibold, design: .monospacedDigit))
                .foregroundStyle(batteryColor)
        }
    }

    private var batteryIconName: String {
        if entry.batterySOC >= 90 { return "battery.100percent" }
        if entry.batterySOC >= 60 { return "battery.75percent" }
        if entry.batterySOC >= 30 { return "battery.50percent" }
        return "battery.25percent"
    }

    private var solarColor: Color {
        HeliosColor.solar(for: .dark, step: 400)
    }

    private var batteryColor: Color {
        HeliosColor.flow(for: .dark, step: 400)
    }
}

// MARK: - LockScreenInlineWidgetView

struct LockScreenInlineWidgetView: View {
    let entry: HeliosTimelineEntry

    var body: some View {
        HStack(spacing: 4) {
            Image(systemName: "sun.max.fill")
                .font(.system(size: 10))
                .foregroundStyle(solarColor)

            Text(String(format: "%.1f kW", entry.currentKW))
                .font(.system(size: 12, weight: .semibold, design: .monospacedDigit))
        }
        .foregroundStyle(.primary)
    }

    private var solarColor: Color {
        HeliosColor.solar(for: .dark, step: 400)
    }
}

// MARK: - Lock Screen Widgets

struct LockScreenCircularWidget: Widget {
    let kind: String = "com.helios.app.lockScreenCircular"

    var body: some WidgetConfiguration {
        StaticConfiguration(
            kind: kind,
            provider: HeliosTimelineProvider()
        ) { entry in
            LockScreenCircularWidgetView(entry: entry)
        }
        .configurationDisplayName("Battery")
        .description("Current battery charge percentage.")
        .supportedFamilies([.accessoryCircular])
    }
}

struct LockScreenRectangularWidget: Widget {
    let kind: String = "com.helios.app.lockScreenRectangular"

    var body: some WidgetConfiguration {
        StaticConfiguration(
            kind: kind,
            provider: HeliosTimelineProvider()
        ) { entry in
            LockScreenRectangularWidgetView(entry: entry)
        }
        .configurationDisplayName("Production")
        .description("Current production and today's energy.")
        .supportedFamilies([.accessoryRectangular])
    }
}

struct LockScreenInlineWidget: Widget {
    let kind: String = "com.helios.app.lockScreenInline"

    var body: some WidgetConfiguration {
        StaticConfiguration(
            kind: kind,
            provider: HeliosTimelineProvider()
        ) { entry in
            LockScreenInlineWidgetView(entry: entry)
        }
        .configurationDisplayName("Live kW")
        .description("Current production in one line.")
        .supportedFamilies([.accessoryInline])
    }
}

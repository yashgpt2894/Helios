import WidgetKit
import SwiftUI

// MARK: - SmallWidgetView

struct SmallWidgetView: View {
    let entry: HeliosTimelineEntry
    @Environment(\.widgetFamily) private var family

    var body: some View {
        if #available(iOS 16.0, *), family == .accessoryCircular {
            accessoryCircularView
        } else {
            compactCircularView
        }
    }

    // MARK: - accessoryCircular (Lock Screen / StandBy)

    @available(iOS 16.0, *)
    private var accessoryCircularView: some View {
        ZStack {
            AccessoryWidgetBackground()

            VStack(spacing: 0) {
                Text(String(format: "%.1f", entry.currentKW))
                    .font(.system(size: 18, weight: .bold, design: .rounded))
                    .foregroundStyle(solarColor)

                Text("kW")
                    .font(.system(size: 8, weight: .medium))
                    .foregroundStyle(.secondary)

                Text(String(format: "%.0f%%", entry.batterySOC))
                    .font(.system(size: 9, weight: .medium))
                    .foregroundStyle(batteryColor)
            }
        }
    }

    // MARK: - compact circular (fallback)

    private var compactCircularView: some View {
        ZStack {
            Circle()
                .stroke(solarColor.opacity(0.15), lineWidth: 4)

            Circle()
                .trim(from: 0, to: min(entry.currentKW / 9.6, 1.0))
                .stroke(
                    solarGradient,
                    style: StrokeStyle(lineWidth: 4, lineCap: .round)
                )
                .rotationEffect(.degrees(-90))

            VStack(spacing: 0) {
                Text(String(format: "%.1f", entry.currentKW))
                    .font(.system(size: 16, weight: .bold, design: .rounded))

                Text("kW")
                    .font(.system(size: 8, weight: .medium))
                    .foregroundStyle(.secondary)
            }
        }
    }

    // MARK: - Helpers

    private var solarColor: Color {
        HeliosColor.solar(for: .dark, step: 400)
    }

    private var batteryColor: Color {
        HeliosColor.flow(for: .dark, step: 400)
    }

    private var solarGradient: AngularGradient {
        AngularGradient(
            gradient: Gradient(colors: [
                HeliosColor.solar(for: .dark, step: 400),
                HeliosColor.solar(for: .dark, step: 300),
                HeliosColor.solar(for: .dark, step: 200)
            ]),
            center: .center
        )
    }
}

// MARK: - SmallWidget

struct SmallWidget: Widget {
    let kind: String = "com.helios.app.smallWidget"

    var body: some WidgetConfiguration {
        StaticConfiguration(
            kind: kind,
            provider: HeliosTimelineProvider()
        ) { entry in
            SmallWidgetView(entry: entry)
        }
        .configurationDisplayName("Current Power")
        .description("Live solar production in kW and battery level.")
        .supportedFamilies([.systemSmall, .accessoryCircular])
    }
}

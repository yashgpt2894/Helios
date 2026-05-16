import ClockKit
import SwiftUI

// MARK: - ComplicationEntry

struct ComplicationEntry: TimelineEntry {
    let date: Date
    let currentKW: Double
    let batterySOC: Double
}

// MARK: - HeliosComplicationController

final class HeliosComplicationController {
    static let shared = HeliosComplicationController()
    private let appGroupID = "group.com.helios.app"

    func currentKW() -> Double {
        let defaults = UserDefaults(suiteName: appGroupID)
        return defaults?.double(forKey: "helios.currentKW") ?? 0
    }

    func currentBatterySOC() -> Double {
        let defaults = UserDefaults(suiteName: appGroupID)
        return defaults?.double(forKey: "helios.batterySOC") ?? 0
    }
}

// MARK: - Complication Views

struct ComplicationCircularView: View {
    let entry: ComplicationEntry

    var body: some View {
        ZStack {
            Circle()
                .stroke(solarColor.opacity(0.15), lineWidth: 3)

            Circle()
                .trim(from: 0, to: min(entry.currentKW / 9.6, 1.0))
                .stroke(solarColor, style: StrokeStyle(lineWidth: 3, lineCap: .round))
                .rotationEffect(.degrees(-90))

            Text(String(format: "%.1f", entry.currentKW))
                .font(.system(size: 12, weight: .bold, design: .rounded))
                .foregroundStyle(.primary)
        }
    }

    private var solarColor: Color {
        Color(red: 0.941, green: 0.776, blue: 0.455)
    }
}

struct ComplicationGraphicCornerView: View {
    let entry: ComplicationEntry

    var body: some View {
        Text(String(format: "%.1f kW", entry.currentKW))
            .font(.system(size: 14, weight: .bold, design: .monospacedDigit))
            .foregroundStyle(solarColor)
    }

    private var solarColor: Color {
        Color(red: 0.941, green: 0.776, blue: 0.455)
    }
}

struct ComplicationGraphicRectangularView: View {
    let entry: ComplicationEntry

    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            HStack(spacing: 4) {
                Image(systemName: "sun.max.fill")
                    .font(.system(size: 10))
                    .foregroundStyle(solarColor)

                Text(String(format: "%.1f kW", entry.currentKW))
                    .font(.system(size: 16, weight: .bold, design: .monospacedDigit))
                    .foregroundStyle(.primary)
            }

            HStack(spacing: 4) {
                Image(systemName: "battery.75percent")
                    .font(.system(size: 10))
                    .foregroundStyle(batteryColor)

                Text(String(format: "%.0f%%", entry.batterySOC))
                    .font(.system(size: 12, weight: .semibold, design: .monospacedDigit))
                    .foregroundStyle(batteryColor)
            }
        }
    }

    private var solarColor: Color {
        Color(red: 0.941, green: 0.776, blue: 0.455)
    }

    private var batteryColor: Color {
        Color(red: 0.498, green: 0.690, blue: 0.412)
    }
}

struct ComplicationGraphicCircularView: View {
    let entry: ComplicationEntry

    var body: some View {
        ZStack {
            Circle()
                .stroke(solarColor.opacity(0.15), lineWidth: 4)

            Circle()
                .trim(from: 0, to: min(entry.currentKW / 9.6, 1.0))
                .stroke(
                    AngularGradient(
                        gradient: Gradient(colors: [
                            Color(red: 0.941, green: 0.776, blue: 0.455),
                            Color(red: 1.0, green: 0.878, blue: 0.580)
                        ]),
                        center: .center
                    ),
                    style: StrokeStyle(lineWidth: 4, lineCap: .round)
                )
                .rotationEffect(.degrees(-90))

            VStack(spacing: 0) {
                Text(String(format: "%.1f", entry.currentKW))
                    .font(.system(size: 16, weight: .bold, design: .rounded))

                Text("kW")
                    .font(.system(size: 7, weight: .medium))
                    .foregroundStyle(.secondary)
            }
        }
    }

    private var solarColor: Color {
        Color(red: 0.941, green: 0.776, blue: 0.455)
    }
}

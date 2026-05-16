import WidgetKit
import SwiftUI

// MARK: - LargeWidgetView

struct LargeWidgetView: View {
    let entry: HeliosTimelineEntry

    var body: some View {
        VStack(spacing: 12) {
            headerView
            energyFlowSnapshotView
            forecastStripView
            fourStatsView
        }
        .padding(16)
    }

    // MARK: - Header

    private var headerView: some View {
        HStack {
            HStack(spacing: 6) {
                Circle()
                    .fill(statusColor)
                    .frame(width: 6, height: 6)

                Text(entry.status)
                    .font(.system(size: 10, weight: .medium, design: .monospaced))
                    .tracking(0.15)
                    .foregroundStyle(statusColor)
            }

            Spacer()

            Text(String(format: "%.1f kW", entry.currentKW))
                .font(.system(size: 28, weight: .light, design: .monospacedDigit))
                .foregroundStyle(solarColor)
        }
    }

    // MARK: - EnergyFlow snapshot

    private var energyFlowSnapshotView: some View {
        HStack(spacing: 8) {
            flowNode(label: "SOLAR", value: entry.currentKW, unit: "kW", color: solarColor)
            Image(systemName: "arrow.right")
                .font(.system(size: 10))
                .foregroundStyle(.tertiary)
            flowNode(label: "HOME", value: entry.currentKW * 0.4, unit: "kW", color: .primary)
            Image(systemName: "arrow.right")
                .font(.system(size: 10))
                .foregroundStyle(.tertiary)
            flowNode(label: "BATT", value: entry.batterySOC, unit: "%", color: batteryColor)
            Image(systemName: "arrow.right")
                .font(.system(size: 10))
                .foregroundStyle(.tertiary)
            flowNode(label: "GRID", value: entry.currentKW * 0.25, unit: "kW", color: gridColor)
        }
        .padding(.vertical, 4)
    }

    private func flowNode(label: String, value: Double, unit: String, color: Color) -> some View {
        VStack(spacing: 2) {
            Text(label)
                .font(.system(size: 9, weight: .bold, design: .monospaced))
                .tracking(0.1)
                .foregroundStyle(.tertiary)

            Text(String(format: "%.1f", value))
                .font(.system(size: 14, weight: .semibold, design: .monospacedDigit))
                .foregroundStyle(color)

            Text(unit)
                .font(.system(size: 8, weight: .regular))
                .foregroundStyle(.secondary)
        }
        .frame(maxWidth: .infinity)
    }

    // MARK: - Forecast strip

    private var forecastStripView: some View {
        HStack(spacing: 0) {
            ForEach(entry.forecastDays.prefix(3), id: \.label) { day in
                VStack(spacing: 4) {
                    Text(day.label)
                        .font(.system(size: 10, weight: .medium, design: .rounded))
                        .foregroundStyle(.secondary)

                    forecastIcon(for: day.condition)
                        .font(.system(size: 14))

                    Text(String(format: "%.1f", day.expectedKwh))
                        .font(.system(size: 13, weight: .semibold, design: .monospacedDigit))

                    Text("kWh")
                        .font(.system(size: 9))
                        .foregroundStyle(.tertiary)
                }
                .frame(maxWidth: .infinity)
            }
        }
        .padding(.vertical, 6)
        .background(
            RoundedRectangle(cornerRadius: 10)
                .fill(.white.opacity(0.04))
        )
    }

    private func forecastIcon(for condition: String) -> Image {
        switch condition {
        case "clear", "mostly-clear":
            return Image(systemName: "sun.max.fill")
        case "partly-cloudy":
            return Image(systemName: "cloud.sun.fill")
        case "overcast":
            return Image(systemName: "cloud.fill")
        case "rain", "heavy-rain", "drizzle":
            return Image(systemName: "cloud.rain.fill")
        default:
            return Image(systemName: "sun.max")
        }
    }

    // MARK: - Four stats

    private var fourStatsView: some View {
        HStack(spacing: 8) {
            statTile(label: "Today", value: String(format: "%.1f", entry.todayKwh), unit: "kWh", color: solarColor)
            statTile(label: "Battery", value: String(format: "%.0f", entry.batterySOC), unit: "%", color: batteryColor)
            statTile(label: "Peak", value: String(format: "%.1f", entry.currentKW * 0.88), unit: "kW", color: gridColor)
            statTile(label: "Saved", value: String(format: "%.0f", entry.todayKwh * 0.28), unit: "kg CO2", color: HeliosColor.flow(for: .dark, step: 500))
        }
    }

    private func statTile(label: String, value: String, unit: String, color: Color) -> some View {
        VStack(alignment: .leading, spacing: 3) {
            Text(label.uppercased())
                .font(.system(size: 9, weight: .medium, design: .monospaced))
                .tracking(0.12)
                .foregroundStyle(.tertiary)

            Text(value)
                .font(.system(size: 18, weight: .light, design: .monospacedDigit))
                .foregroundStyle(color)

            Text(unit)
                .font(.system(size: 9))
                .foregroundStyle(.secondary)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(8)
        .background(
            RoundedRectangle(cornerRadius: 8)
                .fill(.white.opacity(0.03))
        )
    }

    // MARK: - Helpers

    private var solarColor: Color {
        HeliosColor.solar(for: .dark, step: 400)
    }

    private var batteryColor: Color {
        HeliosColor.flow(for: .dark, step: 400)
    }

    private var gridColor: Color {
        HeliosColor.gridExport(for: .dark, step: 400)
    }

    private var statusColor: Color {
        switch entry.status {
        case "PRODUCING": return HeliosColor.flow(for: .dark, step: 500)
        case "CURTAILED": return HeliosColor.gridImport(for: .dark, step: 500)
        case "NIGHT": return .secondary.opacity(0.5)
        default: return .secondary
        }
    }
}

// MARK: - LargeWidget

struct LargeWidget: Widget {
    let kind: String = "com.helios.app.largeWidget"

    var body: some WidgetConfiguration {
        StaticConfiguration(
            kind: kind,
            provider: HeliosTimelineProvider()
        ) { entry in
            LargeWidgetView(entry: entry)
        }
        .configurationDisplayName("Energy Dashboard")
        .description("Complete energy snapshot with flow, forecast, and stats.")
        .supportedFamilies([.systemLarge])
    }
}

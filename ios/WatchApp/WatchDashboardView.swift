import SwiftUI

// MARK: - WatchDashboardView

struct WatchDashboardView: View {
    @Binding var currentKW: Double
    @Binding var batterySOC: Double
    @Binding var todayKwh: Double
    @Binding var forecastDays: [WatchForecastDay]
    @Binding var status: String

    var body: some View {
        ScrollView {
            VStack(spacing: 10) {
                headerView
                batteryRingView
                liveKWView
                threeMetricsView
                glanceForecastView
            }
            .padding(.horizontal, 8)
            .padding(.vertical, 12)
        }
    }

    // MARK: - Header

    private var headerView: some View {
        HStack {
            Text("Helios")
                .font(.system(size: 14, weight: .semibold, design: .rounded))
                .foregroundStyle(solarColor)

            Spacer()

            Circle()
                .fill(statusColor)
                .frame(width: 5, height: 5)
        }
    }

    // MARK: - Battery Ring

    private var batteryRingView: some View {
        ZStack {
            Circle()
                .stroke(batteryColor.opacity(0.12), lineWidth: 5)

            Circle()
                .trim(from: 0, to: batterySOC / 100)
                .stroke(
                    batteryGradient,
                    style: StrokeStyle(lineWidth: 5, lineCap: .round)
                )
                .rotationEffect(.degrees(-90))

            VStack(spacing: 0) {
                Text(String(format: "%.0f", batterySOC))
                    .font(.system(size: 22, weight: .bold, design: .rounded))
                    .foregroundStyle(batteryColor)

                Text("%")
                    .font(.system(size: 9, weight: .medium))
                    .foregroundStyle(.secondary)
            }
        }
        .frame(width: 80, height: 80)
        .padding(.vertical, 4)
    }

    // MARK: - Live kW

    private var liveKWView: some View {
        HStack(spacing: 4) {
            Image(systemName: "sun.max.fill")
                .font(.system(size: 12))
                .foregroundStyle(solarColor)

            Text(String(format: "%.1f kW", currentKW))
                .font(.system(size: 20, weight: .light, design: .monospacedDigit))
                .foregroundStyle(solarColor)
        }
    }

    // MARK: - Three metrics

    private var threeMetricsView: some View {
        HStack(spacing: 4) {
            watchMetric(label: "TODAY", value: String(format: "%.1f", todayKwh), unit: "kWh", color: solarColor)
            watchMetric(label: "BATT", value: String(format: "%.0f", batterySOC), unit: "%", color: batteryColor)
            watchMetric(label: "GRID", value: String(format: "%.1f", currentKW * 0.25), unit: "kW", color: gridColor)
        }
    }

    private func watchMetric(label: String, value: String, unit: String, color: Color) -> some View {
        VStack(spacing: 2) {
            Text(label)
                .font(.system(size: 8, weight: .bold, design: .monospaced))
                .tracking(0.06)
                .foregroundStyle(.tertiary)

            Text(value)
                .font(.system(size: 14, weight: .semibold, design: .monospacedDigit))
                .foregroundStyle(color)

            Text(unit)
                .font(.system(size: 7, weight: .regular))
                .foregroundStyle(.secondary)
        }
        .frame(maxWidth: .infinity)
    }

    // MARK: - Glance: forecast strip

    private var glanceForecastView: some View {
        VStack(alignment: .leading, spacing: 4) {
            Text("FORECAST")
                .font(.system(size: 8, weight: .bold, design: .monospaced))
                .tracking(0.08)
                .foregroundStyle(.tertiary)

            HStack(spacing: 0) {
                ForEach(forecastDays) { day in
                    VStack(spacing: 2) {
                        Text(day.label.prefix(3))
                            .font(.system(size: 9, weight: .medium, design: .rounded))
                            .foregroundStyle(.secondary)
                            .lineLimit(1)

                        Circle()
                            .fill(forecastColor(for: day.condition))
                            .frame(width: 6, height: 6)

                        Text(String(format: "%.1f", day.expectedKwh))
                            .font(.system(size: 10, weight: .semibold, design: .monospacedDigit))
                    }
                    .frame(maxWidth: .infinity)
                }
            }
        }
        .padding(8)
        .background(
            RoundedRectangle(cornerRadius: 8)
                .fill(.white.opacity(0.04))
        )
    }

    // MARK: - Helpers

    private var solarColor: Color {
        Color(red: 0.941, green: 0.776, blue: 0.455)
    }

    private var batteryColor: Color {
        Color(red: 0.498, green: 0.690, blue: 0.412)
    }

    private var gridColor: Color {
        Color(red: 0.365, green: 0.541, blue: 0.659)
    }

    private var statusColor: Color {
        switch status {
        case "PRODUCING": return Color(red: 0.498, green: 0.690, blue: 0.412)
        case "CURTAILED": return Color(red: 0.961, green: 0.486, blue: 0.0)
        case "NIGHT": return .secondary.opacity(0.5)
        default: return .secondary
        }
    }

    private var batteryGradient: AngularGradient {
        AngularGradient(
            gradient: Gradient(colors: [
                Color(red: 0.369, green: 0.604, blue: 0.306),
                Color(red: 0.498, green: 0.690, blue: 0.412),
                Color(red: 0.647, green: 0.839, blue: 0.655)
            ]),
            center: .center
        )
    }

    private func forecastColor(for condition: String) -> Color {
        switch condition {
        case "clear", "mostly-clear":
            return solarColor
        case "partly-cloudy":
            return Color(red: 0.961, green: 0.486, blue: 0.0)
        default:
            return .secondary
        }
    }
}

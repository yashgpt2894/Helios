import ActivityKit
import WidgetKit
import SwiftUI

// MARK: - LiveActivityWidget

struct HeliosLiveActivityWidget: Widget {
    let kind: String = "com.helios.app.liveActivity"

    var body: some WidgetConfiguration {
        ActivityConfiguration(for: HeliosLiveActivityAttributes.self) { context in
            // MARK: Lock Screen / Banner
            lockScreenBanner(context: context)
        } dynamicIsland: { context in
            // MARK: Dynamic Island
            DynamicIsland {
                // Expanded
                DynamicIslandExpandedRegion(.leading) {
                    compactLeading(context: context)
                }

                DynamicIslandExpandedRegion(.trailing) {
                    compactTrailing(context: context)
                }

                DynamicIslandExpandedRegion(.center) {
                    expandedCenter(context: context)
                }

                DynamicIslandExpandedRegion(.bottom) {
                    expandedBottom(context: context)
                }
            } compactLeading: {
                compactLeading(context: context)
            } compactTrailing: {
                compactTrailing(context: context)
            } minimal: {
                minimalView(context: context)
            }
        }
    }

    // MARK: - Lock Screen Banner

    @ViewBuilder
    private func lockScreenBanner(context: ActivityViewContext<HeliosLiveActivityAttributes>) -> some View {
        HStack(spacing: 12) {
            VStack(alignment: .leading, spacing: 4) {
                HStack(spacing: 6) {
                    Image(systemName: "sun.max.fill")
                        .font(.system(size: 12))
                        .foregroundStyle(solarColor)

                    Text(String(format: "%.1f kW", context.state.currentKW))
                        .font(.system(size: 20, weight: .bold, design: .monospacedDigit))
                }

                Text("Solar Production")
                    .font(.system(size: 11, weight: .medium))
                    .foregroundStyle(.secondary)
            }

            Spacer()

            VStack(alignment: .trailing, spacing: 4) {
                HStack(spacing: 4) {
                    Image(systemName: batteryIcon(for: context.state.batterySOC))
                        .font(.system(size: 12))
                        .foregroundStyle(batteryColor)

                    Text(String(format: "%.0f%%", context.state.batterySOC))
                        .font(.system(size: 16, weight: .semibold, design: .monospacedDigit))
                        .foregroundStyle(batteryColor)
                }

                Text("Battery")
                    .font(.system(size: 11, weight: .medium))
                    .foregroundStyle(.secondary)
            }
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 12)
    }

    // MARK: - Compact Leading (sun glyph + kW)

    @ViewBuilder
    private func compactLeading(context: ActivityViewContext<HeliosLiveActivityAttributes>) -> some View {
        HStack(spacing: 4) {
            Image(systemName: "sun.max.fill")
                .font(.system(size: 11))
                .foregroundStyle(solarColor)

            Text(String(format: "%.1f", context.state.currentKW))
                .font(.system(size: 11, weight: .semibold, design: .monospacedDigit))
        }
    }

    // MARK: - Compact Trailing (battery %)

    @ViewBuilder
    private func compactTrailing(context: ActivityViewContext<HeliosLiveActivityAttributes>) -> some View {
        HStack(spacing: 2) {
            Image(systemName: "battery.75percent")
                .font(.system(size: 11))

            Text(String(format: "%.0f%%", context.state.batterySOC))
                .font(.system(size: 11, weight: .semibold, design: .monospacedDigit))
                .foregroundStyle(batteryColor)
        }
    }

    // MARK: - Expanded Center

    @ViewBuilder
    private func expandedCenter(context: ActivityViewContext<HeliosLiveActivityAttributes>) -> some View {
        HStack(spacing: 6) {
            Circle()
                .fill(statusColor(for: context.state.status))
                .frame(width: 5, height: 5)

            Text(context.state.status)
                .font(.system(size: 10, weight: .medium, design: .monospaced))
                .tracking(0.12)
                .foregroundStyle(.secondary)
        }
    }

    // MARK: - Expanded Bottom (EnergyFlow mini + metric tiles)

    @ViewBuilder
    private func expandedBottom(context: ActivityViewContext<HeliosLiveActivityAttributes>) -> some View {
        VStack(spacing: 8) {
            // Mini EnergyFlow (4 nodes)
            HStack(spacing: 6) {
                flowNode(label: "SOLAR", value: context.state.currentKW, color: solarColor)
                flowArrow
                flowNode(label: "HOME", value: context.state.homeConsumption, color: .primary)
                flowArrow
                flowNode(label: "BATT", value: context.state.batterySOC, color: batteryColor)
                flowArrow
                flowNode(label: "GRID", value: context.state.solarToGrid, color: gridColor)
            }

            // 3 metric tiles
            HStack(spacing: 8) {
                miniTile(label: "BATT", value: String(format: "%.0f%%", context.state.batterySOC), color: batteryColor)
                miniTile(label: "GRID", value: String(format: "%.1f", context.state.solarToGrid), color: gridColor)
                miniTile(label: "HOME", value: String(format: "%.1f", context.state.homeConsumption), color: .primary)
            }
        }
        .padding(.horizontal, 4)
    }

    private func flowNode(label: String, value: Double, color: Color) -> some View {
        VStack(spacing: 1) {
            Text(label)
                .font(.system(size: 7, weight: .bold, design: .monospaced))
                .tracking(0.08)
                .foregroundStyle(.tertiary)

            Text(String(format: "%.1f", value))
                .font(.system(size: 11, weight: .semibold, design: .monospacedDigit))
                .foregroundStyle(color)
        }
    }

    private var flowArrow: some View {
        Image(systemName: "arrow.right")
            .font(.system(size: 7))
            .foregroundStyle(.tertiary)
    }

    private func miniTile(label: String, value: String, color: Color) -> some View {
        VStack(spacing: 2) {
            Text(label)
                .font(.system(size: 8, weight: .medium, design: .monospaced))
                .tracking(0.08)
                .foregroundStyle(.tertiary)

            Text(value)
                .font(.system(size: 13, weight: .semibold, design: .monospacedDigit))
                .foregroundStyle(color)
        }
        .frame(maxWidth: .infinity)
    }

    // MARK: - Minimal (kW only)

    @ViewBuilder
    private func minimalView(context: ActivityViewContext<HeliosLiveActivityAttributes>) -> some View {
        HStack(spacing: 2) {
            Image(systemName: "sun.max.fill")
                .font(.system(size: 9))
                .foregroundStyle(solarColor)

            Text(String(format: "%.1f kW", context.state.currentKW))
                .font(.system(size: 10, weight: .semibold, design: .monospacedDigit))
        }
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

    private func batteryIcon(for soc: Double) -> String {
        if soc >= 90 { return "battery.100percent" }
        if soc >= 60 { return "battery.75percent" }
        if soc >= 30 { return "battery.50percent" }
        return "battery.25percent"
    }

    private func statusColor(for status: String) -> Color {
        switch status {
        case "PRODUCING": return HeliosColor.flow(for: .dark, step: 500)
        case "CURTAILED": return HeliosColor.gridImport(for: .dark, step: 500)
        case "NIGHT": return .secondary.opacity(0.5)
        default: return .secondary
        }
    }
}

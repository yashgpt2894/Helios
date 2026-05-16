import WidgetKit
import SwiftUI

// MARK: - MediumWidgetView

struct MediumWidgetView: View {
    let entry: HeliosTimelineEntry
    @Environment(\.widgetFamily) private var family

    var body: some View {
        if family == .accessoryRectangular {
            accessoryRectangularView
        } else {
            systemMediumView
        }
    }

    // MARK: - accessoryRectangular (Lock Screen)

    private var accessoryRectangularView: some View {
        HStack(spacing: 8) {
            VStack(alignment: .leading, spacing: 2) {
                Text(String(format: "%.1f kW", entry.currentKW))
                    .font(.system(size: 18, weight: .bold, design: .rounded))
                    .foregroundStyle(solarColor)

                Text(String(format: "%.1f kWh today", entry.todayKwh))
                    .font(.system(size: 10, weight: .medium))
                    .foregroundStyle(.secondary)
            }

            Spacer()

            HStack(spacing: 4) {
                ForEach(entry.forecastDays.prefix(3), id: \.label) { day in
                    forecastDot(for: day)
                }
            }
        }
        .padding(.horizontal, 4)
    }

    // MARK: - systemMedium

    private var systemMediumView: some View {
        VStack(spacing: 10) {
            HStack {
                HStack(spacing: 4) {
                    Image(systemName: "sun.max.fill")
                        .font(.system(size: 11))
                        .foregroundStyle(solarColor)
                    Text(String(format: "%.1f kW", entry.currentKW))
                        .font(.system(size: 18, weight: .semibold, design: .monospacedDigit))
                        .foregroundStyle(.primary)
                }

                Spacer()

                Text(String(format: "%.1f kWh", entry.todayKwh))
                    .font(.system(size: 13, weight: .medium, design: .monospacedDigit))
                    .foregroundStyle(.secondary)
            }

            sparklineView

            forecastStripView
        }
        .padding(14)
    }

    // MARK: - Sparkline

    private var sparklineView: some View {
        Canvas { context, size in
            guard entry.forecastDays.count >= 3 else { return }

            let values = entry.forecastDays.prefix(3).map { $0.expectedKwh }
            guard let maxVal = values.max(), maxVal > 0 else { return }

            let width = size.width
            let height = size.height
            let stepX = width / CGFloat(values.count - 1)

            var path = Path()
            let points = values.enumerated().map { (i, v) in
                CGPoint(x: CGFloat(i) * stepX, y: height - (CGFloat(v) / CGFloat(maxVal)) * height)
            }

            path.move(to: points[0])
            for i in 1..<points.count {
                let cp1 = CGPoint(x: points[i-1].x + stepX * 0.4, y: points[i-1].y)
                let cp2 = CGPoint(x: points[i].x - stepX * 0.4, y: points[i].y)
                path.addCurve(to: points[i], control1: cp1, control2: cp2)
            }

            context.stroke(
                path,
                with: .linearGradient(
                    Gradient(colors: [
                        HeliosColor.solar(for: .dark, step: 200),
                        HeliosColor.solar(for: .dark, step: 400)
                    ]),
                    startPoint: .leading,
                    endPoint: .trailing
                ),
                lineWidth: 2.5
            )

            for point in points {
                context.fill(
                    Path(ellipseIn: CGRect(x: point.x - 2, y: point.y - 2, width: 4, height: 4)),
                    with: .color(HeliosColor.solar(for: .dark, step: 400))
                )
            }
        }
        .frame(height: 40)
        .opacity(0.8)
    }

    // MARK: - Forecast Strip

    private var forecastStripView: some View {
        HStack(spacing: 0) {
            ForEach(entry.forecastDays.prefix(3), id: \.label) { day in
                VStack(spacing: 3) {
                    Text(day.label)
                        .font(.system(size: 10, weight: .medium, design: .rounded))
                        .foregroundStyle(.secondary)

                    forecastDot(for: day)

                    Text(String(format: "%.1f", day.expectedKwh))
                        .font(.system(size: 12, weight: .semibold, design: .monospacedDigit))

                    Text("kWh")
                        .font(.system(size: 9, weight: .regular))
                        .foregroundStyle(.tertiary)
                }
                .frame(maxWidth: .infinity)
            }
        }
    }

    // MARK: - Helpers

    @ViewBuilder
    private func forecastDot(for day: TimelineForecastDay) -> some View {
        let color: Color = {
            switch day.condition {
            case "clear", "mostly-clear":
                return HeliosColor.solar(for: .dark, step: 400)
            case "partly-cloudy":
                return HeliosColor.gridImport(for: .dark, step: 400)
            default:
                return HeliosColor.NeutralRamp[500]
            }
        }()

        Circle()
            .fill(color)
            .frame(width: 8, height: 8)
    }

    private var solarColor: Color {
        HeliosColor.solar(for: .dark, step: 400)
    }
}

// MARK: - MediumWidget

struct MediumWidget: Widget {
    let kind: String = "com.helios.app.mediumWidget"

    var body: some WidgetConfiguration {
        StaticConfiguration(
            kind: kind,
            provider: HeliosTimelineProvider()
        ) { entry in
            MediumWidgetView(entry: entry)
        }
        .configurationDisplayName("Production Chart")
        .description("Today's sparkline and 3-day production forecast.")
        .supportedFamilies([.systemMedium, .accessoryRectangular])
    }
}

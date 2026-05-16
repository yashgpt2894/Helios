import SwiftUI

// MARK: - DashboardView — port of dashboard.tsx matching dashboard-dark.svg layout

struct DashboardView: View {
    @Environment(TelemetryRepository.self) private var telemetryRepo
    @Environment(ForecastRepository.self) private var forecastRepo
    @Environment(ThemeService.self) private var themeService
    @Environment(\.colorScheme) var colorScheme

    @State private var selectedTab = 0
    @State private var showShareSheet = false
    @State private var shareItems: [Any] = []

    var t: SolarTelemetry {
        telemetryRepo.telemetry
    }

    var body: some View {
        NavigationStack {
            ScrollView(.vertical, showsIndicators: false) {
                VStack(spacing: 24) {
                    // Section 1: Brand + Status + Share
                    headerSection

                    // Section 2: Live Power Ring
                    livePowerSection

                    // Section 3: Metric Tile Row (4 tiles)
                    metricTileRowSection

                    // Section 4: Today Curve
                    todayCurveSection

                    // Section 5: Weekly Summary
                    weeklySummarySection

                    // Section 6: Forecast
                    forecastSection

                    // Section 7: Insights
                    insightsSection

                    // Section 8: Panel Strings
                    panelStringsSection

                    Spacer().frame(height: 40)
                }
                .padding(.horizontal, 16)
                .padding(.top, 8)
            }
            .background(HeliosColor.backgroundPrimary(for: colorScheme))
            .navigationBarHidden(true)
            .sheet(isPresented: $showShareSheet) {
                ShareSheet(items: shareItems)
            }
            .onAppear {
                Task { @MainActor in
                    telemetryRepo.tick()
                }
            }
            .task(id: "tick") {
                let timer = Timer.publish(every: 2.0, on: .main, in: .common).autoconnect()
                for await _ in timer.values {
                    telemetryRepo.tick()
                }
            }
            .task {
                await forecastRepo.fetchForecast(location: ForecastRepository.defaultLocation())
            }
        }
    }

    // MARK: - Section 1: Header

    private var headerSection: some View {
        HStack {
            VStack(alignment: .leading, spacing: 4) {
                HStack(spacing: 8) {
                    BrandMark(brand: BrandService.HELIOS, size: 26)
                    Text("HELIOS")
                        .font(.system(size: 14, weight: .bold, design: .rounded))
                        .foregroundStyle(HeliosColor.textPrimary(for: colorScheme))
                }
                Text("Energy clarity.")
                    .font(.system(size: 11, design: .monospaced))
                    .foregroundStyle(.secondary)
            }

            Spacer()

            HStack(spacing: 10) {
                StatusPill(status: t.status)

                ShareButton {
                    let locLabel = forecastRepo.forecast?.location.label ?? "San Francisco, CA"
                    let snapshot = ShareService.buildSnapshot(t: t, locLabel: locLabel)
                    let (_, items) = ShareService.shareOrCopy(snapshot: snapshot)
                    shareItems = items
                    showShareSheet = true
                }
            }
        }
        .padding(.horizontal, 4)
        .padding(.top, 8)
    }

    // MARK: - Section 2: Live Power + EnergyFlow

    private var livePowerSection: some View {
        VStack(spacing: 4) {
            SectionHeader(
                title: "Live Power",
                eyebrow: "NOW",
                trailing: Format.formatRelative(t.timestamp)
            )

            HStack(alignment: .bottom, spacing: 4) {
                LiveNumber(value: t.acPowerW / 1000, digits: 2, variant: .hero)
                    .foregroundStyle(solarColor)

                Text("kW")
                    .font(.system(size: 16, design: .monospaced))
                    .foregroundStyle(.secondary)
                    .padding(.bottom, 10)
            }
            .frame(maxWidth: .infinity, alignment: .leading)
            .padding(.top, 4)

            HStack(spacing: 16) {
                liveSubtitle("\(Int(round(t.batterySoc)))%", "Battery")
                liveSubtitle(Format.formatTemp(t.ambientTempC), "Ambient")
                liveSubtitle("\(Int(round(t.irradianceWm2))) W/m\u{00b2}", "Irradiance")
            }
            .frame(maxWidth: .infinity, alignment: .leading)

            EnergyFlowView(telemetry: t)
                .frame(height: 360)
                .padding(.top, 8)
        }
    }

    private func liveSubtitle(_ value: String, _ label: String) -> some View {
        VStack(alignment: .leading, spacing: 1) {
            Text(value)
                .font(.system(size: 13, weight: .medium, design: .monospaced))
                .foregroundStyle(.primary)
            Text(label.uppercased())
                .font(.system(size: 9, design: .monospaced))
                .tracking(0.1)
                .foregroundStyle(.tertiary)
        }
    }

    // MARK: - Section 3: Metric Tiles

    private var metricTileRowSection: some View {
        MetricTileRow(tiles: [
            .init(
                label: "Today",
                value: String(format: "%.1f", t.energyTodayKwh),
                unit: "kWh",
                icon: "sun.max",
                color: solarColor
            ),
            .init(
                label: "Month",
                value: String(format: "%.0f", t.energyMonthKwh),
                unit: "kWh",
                icon: "calendar",
                color: HeliosColor.flow(for: colorScheme)
            ),
            .init(
                label: "Lifetime",
                value: String(format: "%.0f", t.energyLifetimeKwh / 1000),
                unit: "MWh",
                icon: "chart.xyaxis.line",
                color: HeliosColor.flow(for: colorScheme, step: 600)
            ),
            .init(
                label: "Grid",
                value: String(format: "%.2f", (t.gridImportW - t.gridExportW) / 1000),
                unit: "kW",
                icon: (t.gridImportW - t.gridExportW) >= 0 ? "arrow.down" : "arrow.up",
                color: (t.gridImportW - t.gridExportW) >= 0 ? HeliosColor.gridImport(for: colorScheme) : HeliosColor.gridExport(for: colorScheme),
                detail: (t.gridImportW - t.gridExportW) >= 0 ? "Importing" : "Exporting"
            )
        ])
    }

    // MARK: - Section 4: Today Curve

    private var todayCurveSection: some View {
        VStack(spacing: 8) {
            SectionHeader(
                title: "Today's Curve",
                eyebrow: "PRODUCTION",
                trailing: String(format: "%.1f kWh", t.energyTodayKwh)
            )

            TodayCurveView(series: telemetryRepo.buildTodaySeries())
                .frame(height: 160)
                .clipShape(RoundedRectangle(cornerRadius: 16))
        }
    }

    // MARK: - Section 5: Weekly Summary

    private var weeklySummarySection: some View {
        VStack(spacing: 8) {
            SectionHeader(
                title: "This Week",
                eyebrow: "SUMMARY"
            )

            LazyVGrid(columns: Array(repeating: GridItem(.flexible(), spacing: 6), count: 7), spacing: 6) {
                ForEach(telemetryRepo.buildWeekSeries().enumerated().map({ (idx, item) in (idx, item.day, item.produced, item.consumed) }), id: \.0) { _, day, produced, consumed in
                    WeekDayBar(day: day, produced: produced, consumed: consumed)
                }
            }
        }
    }

    // MARK: - Section 6: Forecast

    private var forecastSection: some View {
        VStack(spacing: 8) {
            SectionHeader(
                title: "Forecast",
                eyebrow: "7 DAYS",
                trailing: forecastRepo.forecast.map { String(format: "%.0f kWh", $0.totalKwh) }
            )

            if let forecast = forecastRepo.forecast {
                VStack(spacing: 0) {
                    ForEach(Array(forecast.days.prefix(6).enumerated()), id: \.element.id) { idx, day in
                        ForecastRow(day: day, index: idx, series: forecast.days)
                            .staggeredEntrance(index: idx, baseDelay: 0.1, stagger: 0.04)

                        if idx < min(5, forecast.days.count - 1) {
                            Divider().opacity(0.3)
                        }
                    }
                }
                .padding(12)
                .background(.ultraThinMaterial, in: RoundedRectangle(cornerRadius: 16))
                .overlay(
                    RoundedRectangle(cornerRadius: 16)
                        .stroke(.white.opacity(0.04), lineWidth: 0.5)
                )
            } else if forecastRepo.status == .loading {
                HStack {
                    ProgressView().progressViewStyle(.circular)
                    Text("Loading forecast...")
                        .font(.system(size: 11, design: .monospaced))
                        .foregroundStyle(.secondary)
                }
                .frame(maxWidth: .infinity)
                .padding(20)
            }
        }
    }

    // MARK: - Section 7: Insights

    private var insightsSection: some View {
        let insights = InsightsRepository.generateInsights(t: t)
        let forecastInsights = InsightsRepository.generateForecastInsights(forecast: forecastRepo.forecast, t: t)
        let allInsights = insights + forecastInsights

        guard !allInsights.isEmpty else {
            return AnyView(EmptyView())
        }

        return AnyView(VStack(spacing: 8) {
            SectionHeader(
                title: "Insights",
                eyebrow: "INTELLIGENCE"
            )

            VStack(spacing: 8) {
                ForEach(Array(allInsights.enumerated()), id: \.element.id) { idx, insight in
                    InsightCard(insight: insight)
                        .staggeredEntrance(index: idx, baseDelay: 0.2, stagger: 0.05)
                }
            }
        })
    }

    // MARK: - Section 8: Panels

    private var panelStringsSection: some View {
        VStack(spacing: 8) {
            SectionHeader(
                title: "Panel Strings",
                eyebrow: "DC SIDE",
                trailing: String(format: "%.2f kW", t.dcPowerW / 1000)
            )

            VStack(spacing: 0) {
                ForEach(Array(t.panels.enumerated()), id: \.element.id) { idx, panel in
                    PanelStringRow(panel: panel, totalDcPower: t.dcPowerW)
                    if idx < t.panels.count - 1 {
                        Divider().opacity(0.3)
                    }
                }
            }
            .padding(12)
            .background(.ultraThinMaterial, in: RoundedRectangle(cornerRadius: 16))
            .overlay(
                RoundedRectangle(cornerRadius: 16)
                    .stroke(.white.opacity(0.04), lineWidth: 0.5)
            )
        }
    }

    private var solarColor: Color {
        HeliosColor.solar(for: colorScheme)
    }
}

// MARK: - Sub-Views

/// TodayCurveView — Swift Charts canvas for the production/consumption curve
private struct TodayCurveView: View {
    let series: [HistoryPoint]
    @Environment(\.colorScheme) var colorScheme

    var body: some View {
        Canvas { context, size in
            guard series.count > 1 else { return }
            let w = size.width
            let h = size.height
            let maxW = series.map { max($0.productionW, $0.consumptionW) }.max() ?? 9600

            // Gradient fill for production
            var prodPath = Path()
            let stepX = w / CGFloat(series.count - 1)
            for (i, point) in series.enumerated() {
                let x = CGFloat(i) * stepX
                let yProd = h - CGFloat(point.productionW / maxW) * h
                if i == 0 { prodPath.move(to: CGPoint(x: x, y: yProd)) }
                else { prodPath.addLine(to: CGPoint(x: x, y: yProd)) }
            }
            // Close path for gradient fill
            if let lastX = (series.last.map { CGFloat(series.count - 1) * stepX }) {
                prodPath.addLine(to: CGPoint(x: lastX, y: h))
                prodPath.addLine(to: CGPoint(x: 0, y: h))
                prodPath.closeSubpath()
            }

            // Gradient fill
            context.fill(prodPath, with: .linearGradient(
                Gradient(colors: [
                    Color(red: 0.941, green: 0.776, blue: 0.455).opacity(0.25),
                    Color(red: 0.941, green: 0.776, blue: 0.455).opacity(0.03)
                ]),
                startPoint: CGPoint(x: 0.5, y: 0),
                endPoint: CGPoint(x: 0.5, y: 1)
            ))

            // Production line
            var prodLine = Path()
            for (i, point) in series.enumerated() {
                let x = CGFloat(i) * stepX
                let y = h - CGFloat(point.productionW / maxW) * h
                if i == 0 { prodLine.move(to: CGPoint(x: x, y: y)) }
                else { prodLine.addLine(to: CGPoint(x: x, y: y)) }
            }
            context.stroke(prodLine, with: .color(Color(red: 0.941, green: 0.776, blue: 0.455)), lineWidth: 2.5)

            // Consumption line
            var consLine = Path()
            for (i, point) in series.enumerated() {
                let x = CGFloat(i) * stepX
                let yCons = h - CGFloat(point.consumptionW / maxW) * h
                if i == 0 { consLine.move(to: CGPoint(x: x, y: yCons)) }
                else { consLine.addLine(to: CGPoint(x: x, y: yCons)) }
            }
            context.stroke(consLine, with: .color(.secondary.opacity(0.6)), lineWidth: 1.5, style: StrokeStyle(dash: [4, 4]))

            // Time axis labels
            let hours = [0, 6, 12, 18, 24]
            for hIdx in hours {
                guard hIdx < series.count else { continue }
                let x = CGFloat(hIdx) * stepX
                let label = "\(hIdx)"
                let text = Text(label)
                    .font(.system(size: 9, design: .monospaced))
                    .foregroundStyle(.tertiary)
                let resolvedText = context.resolve(text)
                context.draw(resolvedText, at: CGPoint(x: x, y: h - 6))
            }

            // Now line
            let now = SolarCurve.nowAsHourFloat()
            let nowIdx = now / 0.5
            let nowX = CGFloat(nowIdx) * stepX
            if nowX < w {
                var nowPath = Path()
                nowPath.move(to: CGPoint(x: nowX, y: 0))
                nowPath.addLine(to: CGPoint(x: nowX, y: h))
                context.stroke(nowPath, with: .color(.white.opacity(0.25)), lineWidth: 1)
            }
        }
        .background(.ultraThinMaterial, in: RoundedRectangle(cornerRadius: 16))
        .overlay(
            RoundedRectangle(cornerRadius: 16)
                .stroke(.white.opacity(0.04), lineWidth: 0.5)
        )
    }
}

/// WeekDayBar — bar for weekly summary grid
private struct WeekDayBar: View {
    let day: String
    let produced: Double
    let consumed: Double
    @Environment(\.colorScheme) var colorScheme

    var body: some View {
        VStack(spacing: 3) {
            VStack(spacing: 2) {
                VStack(spacing: 0) {
                    Rectangle()
                        .fill(HeliosColor.solar(for: colorScheme, step: 300))
                        .frame(width: 8, height: maxCappedBar(CGFloat(produced / maxEnergy)))
                    Rectangle()
                        .fill(.secondary.opacity(0.4))
                        .frame(width: 8, height: maxCappedBar(CGFloat(consumed / maxEnergy)))
                }
                .frame(height: 80, alignment: .bottom)
            }

            Text(day.prefix(2))
                .font(.system(size: 9, design: .monospaced))
                .foregroundStyle(.tertiary)
        }
        .frame(maxWidth: .infinity)
    }

    private func maxCappedBar(_ val: CGFloat) -> CGFloat {
        Swift.max(2, min(val * 80, 80))
    }

    private var maxEnergy: Double {
        max(produced, consumed, 1)
    }
}

/// ForecastRow — single forecast day
private struct ForecastRow: View {
    let day: ForecastDay
    let index: Int
    let series: [ForecastDay]

    var body: some View {
        HStack(spacing: 12) {
            // Day label
            Text(dayLabel)
                .font(.system(size: 12, weight: .medium, design: .monospaced))
                .foregroundStyle(.primary)
                .frame(width: 55, alignment: .leading)

            // Weather icon
            WeatherIcon(condition: day.condition, size: .small)
                .frame(width: 22)

            // Bar
            GeometryReader { geo in
                let maxKwh = series.map(\.expectedKwh).max() ?? 1
                RoundedRectangle(cornerRadius: 3)
                    .fill(barColor)
                    .frame(width: max(CGFloat(day.expectedKwh / maxKwh) * geo.size.width, 2))
            }
            .frame(height: 10)

            // kWh value
            Text(String(format: "%.0f", day.expectedKwh))
                .font(.system(size: 12, weight: .medium, design: .monospaced))
                .foregroundStyle(.primary)
                .frame(width: 40, alignment: .trailing)

            // Delta
            Text(deltaFormatted)
                .font(.system(size: 11, weight: .medium, design: .monospaced))
                .foregroundStyle(day.expectedKwhVsTypical >= 0 ? HeliosColor.flow(for: colorSchemeLightOrDark) : HeliosColor.alert(for: colorSchemeLightOrDark))
                .frame(width: 36, alignment: .trailing)
        }
        .padding(.vertical, 8)
        .padding(.horizontal, 4)
        .frame(height: 34)
    }

    private var barColor: Color {
        let maxKwh = series.map(\.expectedKwh).max() ?? 1
        let fraction = day.expectedKwh / maxKwh
        switch fraction {
        case 0.8...: return Color(red: 0.369, green: 0.604, blue: 0.306)
        case 0.5..<0.8: return Color(red: 0.831, green: 0.659, blue: 0.263)
        default: return .secondary.opacity(0.6)
        }
    }

    private var deltaFormatted: String {
        let v = day.expectedKwhVsTypical * 100
        return "\(v >= 0 ? "+" : "")\(Int(round(v)))%"
    }

    private var dayLabel: String {
        let todayIso = isoDateFormatter.string(from: Date())
        return ForecastRepository.dayLabel(for: day.date, todayIso: todayIso)
    }

    @Environment(\.colorScheme) var colorSchemeLightOrDark
}

/// InsightCard — insight card with severity color
private struct InsightCard: View {
    let insight: Insight

    var body: some View {
        HStack(alignment: .top, spacing: 10) {
            Circle()
                .fill(severityColor)
                .frame(width: 6, height: 6)
                .padding(.top, 5)

            VStack(alignment: .leading, spacing: 3) {
                Text(insight.title)
                    .font(.system(size: 13, weight: .semibold))
                    .foregroundStyle(.primary)

                Text(insight.body)
                    .font(.system(size: 11))
                    .foregroundStyle(.secondary)
                    .lineLimit(3)

                if let action = insight.actionLabel {
                    Text(action)
                        .font(.system(size: 10, weight: .medium, design: .monospaced))
                        .foregroundStyle(.blue)
                        .padding(.top, 2)
                }
            }

            Spacer()

            if let metric = insight.metric {
                VStack(alignment: .trailing, spacing: 1) {
                    Text(metric)
                        .font(.system(size: 13, weight: .medium, design: .monospaced))
                        .foregroundStyle(severityColor)
                    if let delta = insight.delta {
                        Text(delta)
                            .font(.system(size: 9, design: .monospaced))
                            .foregroundStyle(.tertiary)
                    }
                }
            }
        }
        .padding(10)
        .background(.ultraThinMaterial, in: RoundedRectangle(cornerRadius: 10))
        .overlay(
            RoundedRectangle(cornerRadius: 10)
                .stroke(.white.opacity(0.04), lineWidth: 0.5)
        )
    }

    private var severityColor: Color {
        switch insight.severity {
        case .positive:  return Color(red: 0.369, green: 0.604, blue: 0.306)
        case .neutral:   return .secondary
        case .attention: return Color(red: 0.961, green: 0.486, blue: 0.0)
        case .critical:  return .red
        }
    }
}

/// PanelStringRow — single panel string row
private struct PanelStringRow: View {
    let panel: PanelString
    let totalDcPower: Double

    var body: some View {
        HStack(spacing: 12) {
            VStack(alignment: .leading, spacing: 2) {
                Text(panel.id)
                    .font(.system(size: 10, weight: .bold, design: .monospaced))
                    .foregroundStyle(.secondary)
                Text(panel.label)
                    .font(.system(size: 11))
                    .foregroundStyle(.primary)
            }
            .frame(width: 80, alignment: .leading)

            // Power bar
            GeometryReader { geo in
                RoundedRectangle(cornerRadius: 2)
                    .fill(Color(red: 0.369, green: 0.604, blue: 0.306))
                    .frame(width: fractionBar(Float(panel.powerW) / Swift.max(1, Float(totalDcPower)), Float(geo.size.width)))
            }
            .frame(height: 6)

            HStack(spacing: 8) {
                Text(String(format: "%.0f W", panel.powerW))
                    .font(.system(size: 12, weight: .medium, design: .monospaced))
                    .foregroundStyle(.primary)
                    .frame(width: 60, alignment: .trailing)
                Text("\(String(format: "%.1f", panel.voltageV))V")
                    .font(.system(size: 11, design: .monospaced))
                    .foregroundStyle(.tertiary)
                    .frame(width: 48, alignment: .trailing)
            }
        }
        .padding(.vertical, 6)
        .padding(.horizontal, 4)
    }

    private func fractionBar(_ v: Float, _ w: Float) -> Float {
        max(2, min(w, v * w))
    }
}

// MARK: - Helpers

private let isoDateFormatter: DateFormatter = {
    let f = DateFormatter()
    f.dateFormat = "yyyy-MM-dd"
    return f
}()

// MARK: - Preview

#Preview {
    DashboardView()
        .environment(TelemetryRepository())
        .environment(ForecastRepository())
        .environment(ThemeService())
        .preferredColorScheme(.dark)
}

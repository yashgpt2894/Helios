import SwiftUI

// MARK: - ProductionView — port of Production.tsx layout

struct ProductionView: View {
    @Environment(TelemetryRepository.self) private var telemetryRepo
    @Environment(\.colorScheme) var colorScheme

    var t: SolarTelemetry {
        telemetryRepo.telemetry
    }

    var body: some View {
        NavigationStack {
            ScrollView(.vertical, showsIndicators: false) {
                VStack(spacing: 20) {
                    // Section 1: Header
                    productionHeader
                        .choreographedEntrance(delay: 0)

                    // Section 2: Hero card with live generation
                    heroCard
                        .choreographedEntrance(delay: 0.05)

                    // Section 3: Per-string cards with utilization bars
                    perStringSection

                    // Section 4: WeekChart with daily bars
                    weekChartSection

                    // Section 5: Inverter 2×2 MetricTile grid
                    inverterSection

                    Spacer().frame(height: 40)
                }
                .padding(.horizontal, 16)
                .padding(.top, 8)
            }
            .background(HeliosColor.backgroundPrimary(for: colorScheme))
            .navigationBarHidden(true)
            .onAppear {
                telemetryRepo.tick()
            }
            .task(id: "tick") {
                let timer = Timer.publish(every: 2.0, on: .main, in: .common).autoconnect()
                for await _ in timer.values {
                    telemetryRepo.tick()
                }
            }
        }
    }

    // MARK: - Section 1: Header

    private var productionHeader: some View {
        VStack(alignment: .leading, spacing: 4) {
            Text("SOLAR ARRAY")
                .font(.system(size: 10, weight: .medium, design: .monospaced))
                .tracking(0.2)
                .foregroundStyle(.secondary)

            Text("Production")
                .font(.system(size: 28, weight: .medium))
                .foregroundStyle(HeliosColor.textPrimary(for: colorScheme))

            HStack(spacing: 4) {
                Text("\(t.panels.count) strings")
                    .foregroundStyle(HeliosColor.textTertiary(for: colorScheme))
                Text("·")
                    .foregroundStyle(HeliosColor.textTertiary(for: colorScheme))
                Text("24 panels")
                    .foregroundStyle(HeliosColor.textTertiary(for: colorScheme))
                Text("·")
                    .foregroundStyle(HeliosColor.textTertiary(for: colorScheme))
                Text("9.6 kW DC rating")
                    .foregroundStyle(HeliosColor.textTertiary(for: colorScheme))
            }
            .font(.system(size: 13))
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(.horizontal, 4)
    }

    // MARK: - Section 2: Hero Card

    private var heroCard: some View {
        VStack(alignment: .leading, spacing: 8) {
            HStack {
                Text("GENERATING NOW")
                    .font(.system(size: 10, weight: .medium, design: .monospaced))
                    .tracking(0.2)
                    .foregroundStyle(.secondary)

                Spacer()

                Text("\(Int(round(t.irradianceWm2))) W/m²")
                    .font(.system(size: 10, weight: .medium, design: .monospaced))
                    .tracking(0.2)
                    .foregroundStyle(photoVoltaicColor)
            }

            HStack(alignment: .bottom, spacing: 4) {
                LiveNumber(value: t.acPowerW / 1000, digits: 2, variant: .hero)
                    .foregroundStyle(photoVoltaicColor)

                Text("kW AC")
                    .font(.system(size: 12, weight: .medium, design: .monospaced))
                    .foregroundStyle(.secondary)
                    .padding(.bottom, 8)
            }

            ProductionChart(series: telemetryRepo.buildTodaySeries())
                .frame(height: 180)
        }
        .padding(16)
        .background(.ultraThinMaterial, in: RoundedRectangle(cornerRadius: 20))
        .overlay(
            RoundedRectangle(cornerRadius: 20)
                .stroke(.white.opacity(0.06), lineWidth: 0.5)
        )
    }

    // MARK: - Section 3: Per-string Cards

    private var perStringSection: some View {
        VStack(alignment: .leading, spacing: 8) {
            SectionHeader(
                title: "Per-string output",
                eyebrow: "STRINGS"
            )

            Text("Imbalance > 15% triggers an inspection alert.")
                .font(.system(size: 11))
                .foregroundStyle(.tertiary)

            VStack(spacing: 8) {
                ForEach(Array(t.panels.enumerated()), id: \.element.id) { idx, panel in
                    panelStringCard(panel: panel)
                        .staggeredEntrance(index: idx, baseDelay: 0.1, stagger: HeliosMotion.Stagger.normal)
                }
            }
        }
    }

    private func panelStringCard(panel: PanelString) -> some View {
        let utilization = panel.powerW / max(panel.ratedW, 1)

        return VStack(alignment: .leading, spacing: 8) {
            // Top row: ID badge + label + kW value
            HStack {
                Text(panel.id)
                    .font(.system(size: 10, weight: .bold, design: .monospaced))
                    .foregroundStyle(.secondary)
                    .frame(width: 24, height: 24)
                    .background(
                        HeliosColor.backgroundTertiary(for: colorScheme),
                        in: Circle()
                    )
                    .overlay(
                        Circle().stroke(.white.opacity(0.06), lineWidth: 0.5)
                    )

                Text(panel.label)
                    .font(.system(size: 13, weight: .medium))
                    .foregroundStyle(HeliosColor.textPrimary(for: colorScheme))

                Spacer()

                VStack(alignment: .trailing, spacing: 2) {
                    HStack(alignment: .bottom, spacing: 2) {
                        Text(String(format: "%.2f", panel.powerW / 1000))
                            .font(HeliosTypography.numericFont(size: 18, weight: .light))
                            .foregroundStyle(HeliosColor.textPrimary(for: colorScheme))
                        Text("kW")
                            .font(.system(size: 10, design: .monospaced))
                            .foregroundStyle(.tertiary)
                    }
                    Text("\(Int(round(panel.voltageV))) V · \(String(format: "%.1f", panel.currentA)) A")
                        .font(.system(size: 10, design: .monospaced))
                        .foregroundStyle(.tertiary)
                }
            }

            // Subtitle
            Text("\(panel.panels) panels · \(String(format: "%.0f", panel.ratedW)) W rated")
                .font(.system(size: 10, design: .monospaced))
                .foregroundStyle(.tertiary)

            // Utilization gradient bar
            GeometryReader { geo in
                ZStack(alignment: .leading) {
                    RoundedRectangle(cornerRadius: 2)
                        .fill(HeliosColor.backgroundTertiary(for: colorScheme))
                        .frame(height: 4)

                    RoundedRectangle(cornerRadius: 2)
                        .fill(
                            LinearGradient(
                                colors: [photoVoltaicColor.opacity(0.7), photoVoltaicColor],
                                startPoint: .leading,
                                endPoint: .trailing
                            )
                        )
                        .frame(width: max(CGFloat(utilization) * geo.size.width, 2), height: 4)
                }
            }
            .frame(height: 4)

            // Utilization label
            HStack {
                Text("UTILIZATION")
                    .font(.system(size: 9, design: .monospaced))
                    .tracking(0.15)
                    .foregroundStyle(.tertiary)
                Spacer()
                Text("\(Int(round(utilization * 100)))%")
                    .font(.system(size: 9, design: .monospaced))
                    .foregroundStyle(.secondary)
            }
        }
        .padding(12)
        .background(.ultraThinMaterial, in: RoundedRectangle(cornerRadius: 12))
        .overlay(
            RoundedRectangle(cornerRadius: 12)
                .stroke(.white.opacity(0.04), lineWidth: 0.5)
        )
    }

    // MARK: - Section 4: WeekChart

    private var weekChartSection: some View {
        VStack(alignment: .leading, spacing: 8) {
            SectionHeader(
                title: "Daily production vs use",
                eyebrow: "LAST 7 DAYS"
            )

            VStack(spacing: 0) {
                LazyVGrid(
                    columns: Array(repeating: GridItem(.flexible(), spacing: 6), count: 7),
                    spacing: 6
                ) {
                    ForEach(Array(telemetryRepo.buildWeekSeries().enumerated()), id: \.0) { idx, item in
                        WeekDayBar(
                            day: item.day,
                            produced: item.produced,
                            consumed: item.consumed
                        )
                        .staggeredEntrance(index: idx, baseDelay: 0.15, stagger: 0.04)
                    }
                }
            }
            .padding(16)
            .background(.ultraThinMaterial, in: RoundedRectangle(cornerRadius: 12))
            .overlay(
                RoundedRectangle(cornerRadius: 12)
                    .stroke(.white.opacity(0.04), lineWidth: 0.5)
            )
        }
    }

    // MARK: - Section 5: Inverter 2×2 Grid

    private var inverterSection: some View {
        VStack(alignment: .leading, spacing: 8) {
            SectionHeader(
                title: "System telemetry",
                eyebrow: "INVERTER"
            )

            LazyVGrid(columns: [
                GridItem(.flexible(), spacing: 12),
                GridItem(.flexible(), spacing: 12)
            ], spacing: 12) {
                MetricTile(
                    label: "DC voltage",
                    value: String(format: "%.0f", t.dcVoltageV),
                    unit: "V",
                    icon: "bolt",
                    color: HeliosColor.textPrimary(for: colorScheme),
                    detail: "\(String(format: "%.1f", t.dcCurrentA)) A"
                )
                .staggeredEntrance(index: 0, baseDelay: 0.2, stagger: HeliosMotion.Stagger.normal)

                MetricTile(
                    label: "AC frequency",
                    value: String(format: "%.2f", t.acFrequencyHz),
                    unit: "Hz",
                    icon: "bolt.fill",
                    color: HeliosColor.textPrimary(for: colorScheme),
                    detail: "60 Hz nominal"
                )
                .staggeredEntrance(index: 1, baseDelay: 0.2, stagger: HeliosMotion.Stagger.normal)

                MetricTile(
                    label: "Heatsink",
                    value: String(format: "%.1f", t.heatsinkTempC),
                    unit: "°C",
                    icon: "thermometer.medium",
                    color: t.heatsinkTempC > 50
                        ? HeliosColor.alert(for: colorScheme)
                        : HeliosColor.flow(for: colorScheme),
                    detail: t.heatsinkTempC > 50 ? "warm" : "normal"
                )
                .staggeredEntrance(index: 2, baseDelay: 0.2, stagger: HeliosMotion.Stagger.normal)

                MetricTile(
                    label: "Cabinet",
                    value: String(format: "%.1f", t.cabinetTempC),
                    unit: "°C",
                    icon: "thermometer.low",
                    color: HeliosColor.textPrimary(for: colorScheme),
                    detail: "ambient \(String(format: "%.0f", t.ambientTempC))°C"
                )
                .staggeredEntrance(index: 3, baseDelay: 0.2, stagger: HeliosMotion.Stagger.normal)
            }
        }
    }

    // MARK: - Color Helpers

    private var photoVoltaicColor: Color {
        HeliosColor.solar(for: colorScheme, step: 400)
    }
}

// MARK: - ProductionChart (Canvas-based, matches PWA ProductionChart.tsx)

private struct ProductionChart: View {
    let series: [HistoryPoint]
    @Environment(\.colorScheme) var colorScheme

    var body: some View {
        Canvas { context, size in
            guard series.count > 1 else { return }
            let w = size.width
            let h = size.height
            let pad: CGFloat = 16
            let graphH = h - pad
            let maxW = max(series.map(\.productionW).max() ?? 9600, 1)
            let stepX = w / CGFloat(series.count - 1)

            // Solar accent color
            let solar400 = Color(red: 0.831, green: 0.659, blue: 0.263)

            // Gradient fill from production curve
            var prodPath = Path()
            for (i, point) in series.enumerated() {
                let x = CGFloat(i) * stepX
                let yProportion = CGFloat(point.productionW / maxW)
                let y = graphH - yProportion * graphH
                if i == 0 { prodPath.move(to: CGPoint(x: x, y: y)) }
                else { prodPath.addLine(to: CGPoint(x: x, y: y)) }
            }
            // Close path for gradient fill
            if let lastX = (series.last.map { _ in CGFloat(series.count - 1) * stepX }) {
                prodPath.addLine(to: CGPoint(x: lastX, y: graphH))
                prodPath.addLine(to: CGPoint(x: 0, y: graphH))
                prodPath.closeSubpath()
            }
            context.fill(prodPath, with: .linearGradient(
                Gradient(colors: [solar400.opacity(0.35), solar400.opacity(0.03)]),
                startPoint: CGPoint(x: 0.5, y: 0),
                endPoint: CGPoint(x: 0.5, y: 1)
            ))

            // Production line stroke
            var prodLine = Path()
            for (i, point) in series.enumerated() {
                let x = CGFloat(i) * stepX
                let y = graphH - CGFloat(point.productionW / maxW) * graphH
                if i == 0 { prodLine.move(to: CGPoint(x: x, y: y)) }
                else { prodLine.addLine(to: CGPoint(x: x, y: y)) }
            }
            context.stroke(prodLine, with: .color(solar400), lineWidth: 2.5)

            // Time axis labels: 0, 6, 12, 18, 24
            for hIdx in stride(from: 0, through: 48, by: 12) { // series entries every 0.5h → 12 steps = 6h
                guard hIdx < series.count else { continue }
                let x = CGFloat(hIdx) * stepX
                let hour = hIdx / 2
                let label = "\(hour)"
                let text = Text(label)
                    .font(.system(size: 9, design: .monospaced))
                    .foregroundStyle(.tertiary)
                let resolvedText = context.resolve(text)
                context.draw(resolvedText, at: CGPoint(x: x, y: graphH - 4))
            }

            // "now" line
            let nowHour = SolarCurve.nowAsHourFloat()
            let nowIdx = nowHour * 2 // 2 entries per hour
            let nowX = CGFloat(nowIdx) * stepX
            if nowX < w {
                var nowPath = Path()
                nowPath.move(to: CGPoint(x: nowX, y: 0))
                nowPath.addLine(to: CGPoint(x: nowX, y: graphH))
                context.stroke(nowPath, with: .color(.white.opacity(0.18)), style: StrokeStyle(lineWidth: 1, dash: [4, 4]))
            }
        }
        .clipShape(RoundedRectangle(cornerRadius: 8))
    }
}

// MARK: - WeekDayBar (compact day-proportion bar)

private struct WeekDayBar: View {
    let day: String
    let produced: Double
    let consumed: Double
    @Environment(\.colorScheme) var colorScheme

    private var maxEnergy: Double {
        max(produced, consumed, 8)
    }

    var body: some View {
        VStack(spacing: 4) {
            VStack(spacing: 2) {
                VStack(spacing: 0) {
                    // Produced (solar)
                    Rectangle()
                        .fill(HeliosColor.solar(for: colorScheme, step: 300))
                        .frame(width: 8, height: barHeight(CGFloat(produced / maxEnergy)))

                    // Consumed
                    Rectangle()
                        .fill(.secondary.opacity(0.4))
                        .frame(width: 8, height: barHeight(CGFloat(consumed / maxEnergy)))
                }
                .frame(height: 80, alignment: .bottom)
            }

            Text(String(day.prefix(2)))
                .font(.system(size: 9, design: .monospaced))
                .foregroundStyle(.tertiary)
        }
        .frame(maxWidth: .infinity)
    }

    private func barHeight(_ fraction: CGFloat) -> CGFloat {
        max(2, fraction * 80)
    }
}

// MARK: - Previews

#Preview("Production") {
    ProductionView()
        .environment(TelemetryRepository())
        .preferredColorScheme(.dark)
}

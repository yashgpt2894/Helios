import SwiftUI

// MARK: - InsightsView — port of Insights.tsx

struct InsightsView: View {
    @Environment(TelemetryRepository.self) private var telemetryRepo
    @Environment(ForecastRepository.self) private var forecastRepo
    @Environment(\.colorScheme) var colorScheme

    @State private var isForecastLoaded = false

    var t: SolarTelemetry { telemetryRepo.telemetry }

    private var allInsights: [Insight] {
        let direct = InsightsRepository.generateInsights(t: t)
        let forecast = InsightsRepository.generateForecastInsights(
            forecast: forecastRepo.forecast, t: t
        )
        return direct + forecast
    }

    var body: some View {
        NavigationStack {
            ScrollView(.vertical, showsIndicators: false) {
                VStack(spacing: 24) {
                    // Sparkles header
                    insightsHeader
                        .choreographedEntrance(delay: 0)

                    // Highlight (first insight)
                    if let highlight = allInsights.first {
                        insightHighlightCard(highlight)
                            .choreographedEntrance(delay: 0.05)
                    }

                    // Forecast section
                    forecastSection

                    // Savings 2x2 grid
                    savingsGrid
                        .choreographedEntrance(delay: 0.1)

                    // InsightCards feed
                    insightFeed

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
            .task {
                await forecastRepo.fetchForecast(location: ForecastRepository.defaultLocation())
                isForecastLoaded = true
            }
        }
    }

    // MARK: - Header (Sparkles-style)

    private var insightsHeader: some View {
        VStack(alignment: .leading, spacing: 8) {
            // Eyebrow with subtle sparkle decoration
            HStack(spacing: 6) {
                Image(systemName: "sparkles")
                    .font(.system(size: 11))
                    .foregroundStyle(HeliosColor.solar(for: colorScheme, step: 400))
                Text("helios° intelligence")
                    .font(.system(size: 10, weight: .medium, design: .monospaced))
                    .tracking(0.2)
                    .foregroundStyle(.secondary)
            }

            Text("What we noticed today")
                .font(.system(size: 28, weight: .medium))
                .foregroundStyle(HeliosColor.textPrimary(for: colorScheme))

            Text("Automated analysis of telemetry, weather, and grid conditions.")
                .font(.system(size: 13))
                .foregroundStyle(HeliosColor.textTertiary(for: colorScheme))
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(.horizontal, 4)
    }

    // MARK: - Insight Highlight Card

    private func insightHighlightCard(_ insight: Insight) -> some View {
        VStack(alignment: .leading, spacing: 12) {
            // Top row: severity ring + category icon + title
            HStack(alignment: .top, spacing: 10) {
                // Severity color ring
                Circle()
                    .stroke(severityColor(insight.severity).opacity(0.5), lineWidth: 1.5)
                    .frame(width: 14, height: 14)
                    .overlay(
                        Circle()
                            .fill(severityColor(insight.severity))
                            .frame(width: 6, height: 6)
                    )

                VStack(alignment: .leading, spacing: 4) {
                    // Severity badge + category icon
                    HStack(spacing: 4) {
                        Text(insight.severity.rawValue.uppercased())
                            .font(.system(size: 9, weight: .medium, design: .monospaced))
                            .tracking(0.15)
                            .foregroundStyle(severityColor(insight.severity))
                        Text("·")
                            .foregroundStyle(.tertiary)
                        Image(systemName: categoryIcon(insight.category))
                            .font(.system(size: 10))
                            .foregroundStyle(.secondary)
                        Text(insight.category.rawValue.capitalized)
                            .font(.system(size: 10, design: .monospaced))
                            .foregroundStyle(.secondary)
                    }

                    // Title
                    Text(insight.title)
                        .font(.system(size: 15, weight: .semibold))
                        .foregroundStyle(HeliosColor.textPrimary(for: colorScheme))
                        .fixedSize(horizontal: false, vertical: true)
                }

                Spacer()
            }

            // Body
            Text(insight.body)
                .font(.system(size: 13))
                .foregroundStyle(HeliosColor.textSecondary(for: colorScheme))
                .lineSpacing(3)
                .fixedSize(horizontal: false, vertical: true)

            // Bottom row: metric + delta + action
            HStack {
                if let metric = insight.metric {
                    Text(metric)
                        .font(.system(size: 18, weight: .light, design: .monospaced))
                        .foregroundStyle(HeliosColor.textPrimary(for: colorScheme))
                }
                if let delta = insight.delta {
                    Text(delta)
                        .font(.system(size: 10, design: .monospaced))
                        .foregroundStyle(.tertiary)
                }
                Spacer()
                if let action = insight.actionLabel {
                    Text(action)
                        .font(.system(size: 11, weight: .medium))
                        .foregroundStyle(HeliosColor.solar(for: colorScheme, step: 400))
                }
            }
        }
        .padding(16)
        .background(HeliosColor.backgroundSecondary(for: colorScheme), in: RoundedRectangle(cornerRadius: 16))
        .overlay(
            RoundedRectangle(cornerRadius: 16)
                .stroke(severityColor(insight.severity).opacity(0.12), lineWidth: 1)
        )
    }

    // MARK: - Forecast Section

    @ViewBuilder
    private var forecastSection: some View {
        VStack(alignment: .leading, spacing: 8) {
            SectionHeader(title: "7-day solar forecast", eyebrow: "FORECAST")

            if forecastRepo.status == .loading {
                HStack {
                    ProgressView()
                        .scaleEffect(0.8)
                    Text("Loading forecast…")
                        .font(.system(size: 12))
                        .foregroundStyle(.tertiary)
                }
                .frame(maxWidth: .infinity, alignment: .center)
                .padding(.vertical, 24)
            } else if forecastRepo.status == .error, let msg = forecastRepo.errorMessage {
                Text("⚠️ \(msg)")
                    .font(.system(size: 11))
                    .foregroundStyle(HeliosColor.alert(for: colorScheme))
                    .padding(.vertical, 12)
            } else if let forecast = forecastRepo.forecast, !forecast.days.isEmpty {
                VStack(spacing: 6) {
                    let todayIso = todayIsoString()
                    ForEach(Array(forecast.days.enumerated()), id: \.element.id) { idx, day in
                        forecastDayRow(day: day, todayIso: todayIso)
                            .staggeredEntrance(
                                index: idx,
                                baseDelay: 0.08,
                                stagger: HeliosMotion.Stagger.tight
                            )
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
    }

    private func forecastDayRow(day: ForecastDay, todayIso: String) -> some View {
        let label = ForecastRepository.dayLabel(for: day.date, todayIso: todayIso)
        let maxKwh = max(forecastRepo.forecast?.days.map(\.expectedKwh).max() ?? 40, 1)

        return HStack(spacing: 8) {
            // Day label
            Text(label)
                .font(.system(size: 11, weight: .medium, design: .monospaced))
                .foregroundStyle(.secondary)
                .frame(width: 50, alignment: .leading)

            // Weather icon
            WeatherIcon(condition: day.condition, size: .small)

            // Temp range
            HStack(spacing: 2) {
                Text("\(Int(round(day.tempHighC)))°")
                    .font(.system(size: 10, design: .monospaced))
                    .foregroundStyle(.primary)
                Text("\(Int(round(day.tempLowC)))°")
                    .font(.system(size: 9, design: .monospaced))
                    .foregroundStyle(.tertiary)
            }
            .frame(width: 36, alignment: .leading)

            // Expected kWh
            Text(String(format: "%.0f", day.expectedKwh))
                .font(.system(size: 11, weight: .medium, design: .monospaced))
                .foregroundStyle(.primary)
                .frame(width: 28, alignment: .trailing)
            Text("kWh")
                .font(.system(size: 8, design: .monospaced))
                .foregroundStyle(.tertiary)

            // Gradient bar
            GeometryReader { geo in
                ZStack(alignment: .leading) {
                    RoundedRectangle(cornerRadius: 2)
                        .fill(HeliosColor.backgroundTertiary(for: colorScheme))
                        .frame(height: 4)

                    RoundedRectangle(cornerRadius: 2)
                        .fill(
                            LinearGradient(
                                colors: [
                                    HeliosColor.solar(for: colorScheme, step: 400).opacity(0.6),
                                    HeliosColor.solar(for: colorScheme, step: 400)
                                ],
                                startPoint: .leading,
                                endPoint: .trailing
                            )
                        )
                        .frame(width: max(CGFloat(day.expectedKwh / maxKwh) * geo.size.width, 2), height: 4)
                }
            }
            .frame(height: 4)
        }
    }

    // MARK: - Savings Grid

    private var savingsGrid: some View {
        VStack(alignment: .leading, spacing: 8) {
            SectionHeader(title: "Your savings", eyebrow: "IMPACT")

            let savings = InsightsRepository.computeSavings(t: t)
            let co2Avoided = t.energyLifetimeKwh * 0.42 // kg CO₂ per kWh

            LazyVGrid(columns: [
                GridItem(.flexible(), spacing: 12),
                GridItem(.flexible(), spacing: 12)
            ], spacing: 12) {
                MetricTile(
                    label: "Today",
                    value: Format.formatCurrency(savings.today),
                    unit: nil,
                    icon: "dollarsign.circle",
                    color: HeliosColor.flow(for: colorScheme),
                    detail: "\(String(format: "%.1f", t.energyTodayKwh)) kWh"
                )

                MetricTile(
                    label: "This month",
                    value: Format.formatCurrency(savings.month),
                    unit: nil,
                    icon: "calendar",
                    color: HeliosColor.textPrimary(for: colorScheme),
                    detail: "\(String(format: "%.0f", t.energyMonthKwh)) kWh"
                )

                MetricTile(
                    label: "Lifetime",
                    value: Format.formatCurrency(savings.lifetime),
                    unit: nil,
                    icon: "clock.arrow.2.circlepath",
                    color: HeliosColor.textPrimary(for: colorScheme),
                    detail: "\(String(format: "%.0f", t.energyLifetimeKwh)) kWh"
                )

                MetricTile(
                    label: "CO₂ avoided",
                    value: "\(Int(round(co2Avoided)))",
                    unit: "kg",
                    icon: "leaf",
                    color: HeliosColor.flow(for: colorScheme),
                    detail: "lifetime"
                )
            }
        }
    }

    // MARK: - Insight Feed

    @ViewBuilder
    private var insightFeed: some View {
        let items = Array(allInsights.dropFirst())

        if !items.isEmpty {
            VStack(alignment: .leading, spacing: 8) {
                SectionHeader(title: "All observations", eyebrow: "DETAIL")

                VStack(spacing: 8) {
                    ForEach(Array(items.enumerated()), id: \.element.id) { idx, insight in
                        insightRow(insight)
                            .staggeredEntrance(
                                index: idx,
                                baseDelay: 0.2,
                                stagger: HeliosMotion.Stagger.normal
                            )
                    }
                }
            }
        }
    }

    private func insightRow(_ insight: Insight) -> some View {
        VStack(alignment: .leading, spacing: 6) {
            HStack(alignment: .top, spacing: 8) {
                // Severity dot
                Circle()
                    .fill(severityColor(insight.severity))
                    .frame(width: 6, height: 6)
                    .padding(.top, 4)

                VStack(alignment: .leading, spacing: 4) {
                    // Category icon + title
                    HStack(spacing: 4) {
                        Image(systemName: categoryIcon(insight.category))
                            .font(.system(size: 9))
                            .foregroundStyle(.secondary)
                        Text(insight.title)
                            .font(.system(size: 13, weight: .semibold))
                            .foregroundStyle(HeliosColor.textPrimary(for: colorScheme))
                    }

                    Text(insight.body)
                        .font(.system(size: 11))
                        .foregroundStyle(HeliosColor.textTertiary(for: colorScheme))
                        .lineLimit(3)
                        .fixedSize(horizontal: false, vertical: true)

                    // Metric row
                    if let metric = insight.metric, let delta = insight.delta {
                        HStack(spacing: 6) {
                            Text(metric)
                                .font(.system(size: 14, weight: .light, design: .monospaced))
                                .foregroundStyle(HeliosColor.textPrimary(for: colorScheme))
                            Text("·")
                                .foregroundStyle(.tertiary)
                            Text(delta)
                                .font(.system(size: 9, design: .monospaced))
                                .foregroundStyle(.tertiary)
                        }
                    }

                    // Action
                    if let action = insight.actionLabel {
                        Text(action)
                            .font(.system(size: 10, weight: .medium))
                            .foregroundStyle(HeliosColor.solar(for: colorScheme, step: 400))
                    }
                }
            }
        }
        .padding(12)
        .background(.ultraThinMaterial, in: RoundedRectangle(cornerRadius: 10))
        .overlay(
            RoundedRectangle(cornerRadius: 10)
                .stroke(.white.opacity(0.03), lineWidth: 0.5)
        )
    }

    // MARK: - Helpers

    private func severityColor(_ severity: InsightSeverity) -> Color {
        switch severity {
        case .positive:  return HeliosColor.flow(for: colorScheme)
        case .neutral:   return HeliosColor.battery(for: colorScheme)
        case .attention: return HeliosColor.alert(for: colorScheme)
        case .critical:  return HeliosColor.alert(for: colorScheme, step: 600)
        }
    }

    private func categoryIcon(_ category: InsightCategory) -> String {
        switch category {
        case .production:  return "sun.max"
        case .consumption: return "house"
        case .battery:     return "battery.75"
        case .savings:     return "dollarsign.circle"
        case .maintenance: return "wrench"
        case .forecast:    return "cloud.sun"
        }
    }

    private func todayIsoString() -> String {
        let df = DateFormatter()
        df.dateFormat = "yyyy-MM-dd"
        return df.string(from: Date())
    }
}

// MARK: - Preview

#Preview("Insights") {
    InsightsView()
        .environment(TelemetryRepository())
        .environment(ForecastRepository())
        .preferredColorScheme(.dark)
}

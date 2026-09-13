# InsightsRepository

```swift
import Foundation

// MARK: - InsightsRepository — port of aiInsights.ts + forecastInsights.ts

enum InsightsRepository {
    static let kwhRateGrid: Double = 0.32
    static let kwhRateExport: Double = 0.08
    static let offPeakRate: Double = 0.11

    // MARK: - generateInsights (aiInsights.ts)

    static func generateInsights(t: SolarTelemetry) -> [Insight] {
        var insights: [Insight] = []
        let hour = SolarCurve.nowAsHourFloat(Date(timeIntervalSince1970: t.timestamp / 1000))

        let selfConsumptionPct = t.acPowerW > 0
            ? clamp01(1 - t.gridExportW / Swift.max(1, t.acPowerW)) * 100
            : 0

        let todaySavings = t.energyTodayKwh * kwhRateGrid + (t.energyTodayKwh * 0.4) * kwhRateExport
        let monthSavings = t.energyMonthKwh * (kwhRateGrid * 0.7)

        // Peak production window
        if t.status == .producing && t.acPowerW > 5500 && t.cloudCoverPct < 40 {
            insights.append(Insight(
                id: "peak-production",
                category: .production,
                severity: .positive,
                title: "Peak production window",
                body: "You're harvesting \(String(format: "%.2f", t.acPowerW / 1000)) kW — within 8% of array rating. Now is the best moment to run heavy loads on direct solar.",
                metric: "\(String(format: "%.2f", t.acPowerW / 1000)) kW",
                delta: "+8% vs typical",
                actionLabel: "Schedule appliances"
            ))
        }

        // Cloud coverage
        if t.cloudCoverPct > 55 && hour > 9 && hour < 17 {
            insights.append(Insight(
                id: "cloud-coverage",
                category: .forecast,
                severity: .attention,
                title: "Cloud cover suppressing yield",
                body: "Current irradiance is \(Int(round(t.irradianceWm2))) W/m\u{00b2} — about \(Int(round((1 - t.irradianceWm2 / 1000) * 100)))% below clear-sky. Defer dishwasher and dryer until after 2:00 PM if possible.",
                metric: "\(Int(round(t.cloudCoverPct)))%",
                delta: "cloud cover",
                actionLabel: nil
            ))
        }

        // Export active
        if t.batterySoc > 92 && t.gridExportW > 1500 {
            insights.append(Insight(
                id: "export-active",
                category: .savings,
                severity: .positive,
                title: "Exporting surplus to grid",
                body: "Battery is full and you're exporting \(String(format: "%.2f", t.gridExportW / 1000)) kW. At your feed-in rate, this earns roughly $\(String(format: "%.2f", (t.gridExportW / 1000) * kwhRateExport))/hr.",
                metric: "+\(String(format: "%.2f", t.gridExportW / 1000)) kW",
                delta: "feed-in active",
                actionLabel: nil
            ))
        }

        // Overnight low battery
        if t.batterySoc < 25 && hour < 6 {
            let chargeCost = ((t.batteryCapacityKwh - (t.batterySoc / 100) * t.batteryCapacityKwh) * 0.11)
            insights.append(Insight(
                id: "overnight-low",
                category: .battery,
                severity: .attention,
                title: "Battery low overnight",
                body: "State-of-charge is \(Int(round(t.batterySoc)))%. Off-peak grid charging until sunrise costs roughly $\(String(format: "%.2f", chargeCost)) and protects autonomy if morning is overcast.",
                metric: "\(Int(round(t.batterySoc)))%",
                delta: "soc",
                actionLabel: nil
            ))
        }

        // Thermal warning
        if t.heatsinkTempC > 48 {
            insights.append(Insight(
                id: "thermal",
                category: .maintenance,
                severity: .attention,
                title: "Inverter running warm",
                body: "Heatsink at \(String(format: "%.1f", t.heatsinkTempC))\u{00b0}C. Output may derate above 55\u{00b0}C. Consider checking the cabinet vent for obstructions.",
                metric: "\(String(format: "%.1f", t.heatsinkTempC))\u{00b0}C",
                delta: "heatsink",
                actionLabel: nil
            ))
        }

        // Self-consumption high
        if t.status == .producing && selfConsumptionPct > 75 {
            insights.append(Insight(
                id: "self-consumption-high",
                category: .savings,
                severity: .positive,
                title: "Self-consumption is excellent",
                body: "\(Int(round(selfConsumptionPct)))% of generation is being used on-site — well above the 58% neighborhood median. This is the most economically valuable mode of operation.",
                metric: "\(Int(round(selfConsumptionPct)))%",
                delta: "+17 vs avg",
                actionLabel: nil
            ))
        }

        // Today savings
        insights.append(Insight(
            id: "today-savings",
            category: .savings,
            severity: .neutral,
            title: "Estimated savings today",
            body: "Avoided grid imports plus feed-in credits add up to about $\(String(format: "%.2f", todaySavings)) so far today. Month-to-date: $\(String(format: "%.0f", monthSavings)).",
            metric: "$\(String(format: "%.2f", todaySavings))",
            delta: "today",
            actionLabel: nil
        ))

        // String imbalance
        let stringSpread = computeStringSpread(t)
        if stringSpread > 0.15 && t.acPowerW > 1000 {
            insights.append(Insight(
                id: "string-imbalance",
                category: .maintenance,
                severity: .attention,
                title: "String output imbalance detected",
                body: "One string is producing \(Int(round(stringSpread * 100)))% less than peers. Possible shading, soiling, or a degraded panel. Inspect after sunset.",
                metric: "\u{0394} \(Int(round(stringSpread * 100)))%",
                delta: "across strings",
                actionLabel: nil
            ))
        }

        // Morning strategy
        if hour > 5.5 && hour < 7.5 && t.batterySoc > 60 {
            insights.append(Insight(
                id: "morning-strategy",
                category: .forecast,
                severity: .neutral,
                title: "Morning strategy looks healthy",
                body: "Battery at \(Int(round(t.batterySoc)))% heading into sunrise — you're set up to ride the solar ramp without grid imports.",
                metric: "\(Int(round(t.batterySoc)))%",
                delta: "soc \u{00b7} pre-dawn",
                actionLabel: nil
            ))
        }

        return Array(insights.prefix(8))
    }

    // MARK: - generateForecastInsights (forecastInsights.ts)

    static func generateForecastInsights(forecast: ProductionForecast?, t: SolarTelemetry) -> [Insight] {
        guard let forecast = forecast, forecast.days.count >= 1 else { return [] }

        var insights: [Insight] = []
        let today = forecast.days[0]
        let tomorrow = forecast.days.count > 1 ? forecast.days[1] : nil
        let next3 = Array(forecast.days.prefix(3))

        // Pre-charge storm
        if let tomorrow = tomorrow, tomorrow.expectedKwh < 8 && t.batterySoc < 60 {
            let chargeKwh = Swift.max(0, t.batteryCapacityKwh * 0.95 - (t.batterySoc / 100) * t.batteryCapacityKwh)
            insights.append(Insight(
                id: "precharge-storm",
                category: .forecast,
                severity: .attention,
                title: "\(tomorrow.conditionLabel) tomorrow — pre-charge tonight",
                body: "Forecast: only \(String(format: "%.0f", tomorrow.expectedKwh)) kWh production expected. Top up battery from off-peak grid (~$\(String(format: "%.2f", chargeKwh * offPeakRate))) so you ride out the day on storage.",
                metric: "\(String(format: "%.0f", tomorrow.expectedKwh)) kWh",
                delta: "tomorrow",
                actionLabel: "Schedule pre-charge"
            ))
        }

        // High yield day
        if let sunnyDay = forecast.days.first(where: { $0.expectedKwhVsTypical > 0.25 && $0.expectedKwh > 38 }) {
            let dayLabel = sunnyDay.date == today.date ? "Today" : (tomorrow.map { sunnyDay.date == $0.date ? "Tomorrow" : friendlyDay(sunnyDay.date) } ?? friendlyDay(sunnyDay.date))
            insights.append(Insight(
                id: "high-yield-day",
                category: .forecast,
                severity: .positive,
                title: "\(dayLabel): brilliant solar window",
                body: "\(String(format: "%.0f", sunnyDay.expectedKwh)) kWh expected — \(String(format: "%.0f", sunnyDay.expectedKwhVsTypical * 100))% above typical. Run pool pump, dishwasher, and EV charge between 11 AM and 3 PM to capture peak surplus.",
                metric: "\(String(format: "%.0f", sunnyDay.expectedKwh)) kWh",
                delta: "+\(String(format: "%.0f", sunnyDay.expectedKwhVsTypical * 100))%",
                actionLabel: "Plan heavy loads"
            ))
        }

        // Storm warning
        if let stormDay = forecast.days.first(where: {
            $0.condition == .thunderstorm || $0.condition == .heavyRain || ($0.precipitationMm > 8 && $0.cloudCoverPct > 70)
        }), stormDay.date != today.date {
            let dayLabel = tomorrow.map { stormDay.date == $0.date ? "Tomorrow" : friendlyDay(stormDay.date) } ?? friendlyDay(stormDay.date)
            insights.append(Insight(
                id: "storm-warning",
                category: .forecast,
                severity: .attention,
                title: "\(stormDay.conditionLabel) \(dayLabel.lowercased())",
                body: "\(String(format: "%.1f", stormDay.precipitationMm)) mm precipitation with \(String(format: "%.0f", stormDay.cloudCoverPct))% cloud cover. Production likely to drop to \(String(format: "%.0f", stormDay.expectedKwh)) kWh. Pre-cool the home and finish laundry before midday.",
                metric: stormDay.conditionLabel,
                delta: dayLabel.lowercased(),
                actionLabel: nil
            ))
        }

        // Off-grid streak
        let next3Total = next3.reduce(0) { $0 + $1.expectedKwh }
        let avgDailyUseKwh = (t.homeLoadW > 0 ? t.homeLoadW : 800) * 24 / 1000 * 0.7
        if next3Total > avgDailyUseKwh * 3 * 1.3 {
            insights.append(Insight(
                id: "self-sufficient-streak",
                category: .forecast,
                severity: .positive,
                title: "Off-grid streak ahead",
                body: "Next 3 days forecast \(String(format: "%.0f", next3Total)) kWh against ~\(String(format: "%.0f", avgDailyUseKwh * 3)) kWh of typical use. You can stay 100% off-grid through \(friendlyDay(forecast.days[2].date)).",
                metric: "\(String(format: "%.0f", next3Total)) kWh",
                delta: "3-day forecast",
                actionLabel: nil
            ))
        }

        // Weekly outlook
        insights.append(Insight(
            id: "weekly-outlook",
            category: .forecast,
            severity: .neutral,
            title: "7-day production outlook",
            body: "\(String(format: "%.0f", forecast.totalKwh)) kWh expected across the week — \(forecast.vsLastWeekPct >= 0 ? "+" : "")\(String(format: "%.0f", forecast.vsLastWeekPct))% vs typical. Cleanest sky on \(bestDay(forecast.days)), dimmest on \(worstDay(forecast.days)).",
            metric: "\(String(format: "%.0f", forecast.totalKwh)) kWh",
            delta: "\(forecast.vsLastWeekPct >= 0 ? "+" : "")\(String(format: "%.0f", forecast.vsLastWeekPct))% vs typical",
            actionLabel: nil
        ))

        // Projected savings
        let projectedSavings = forecast.totalKwh * kwhRateGrid * 0.7
        if projectedSavings > 10 {
            insights.append(Insight(
                id: "projected-savings",
                category: .savings,
                severity: .
```
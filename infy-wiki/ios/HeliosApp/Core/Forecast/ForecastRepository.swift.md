# ForecastRepository

```swift
import Foundation
import Observation

// MARK: - ForecastRepository — byte-for-byte port of services/weather.ts

@Observable
final class ForecastRepository {
    private(set) var forecast: ProductionForecast?
    private(set) var status: Status = .idle
    private(set) var errorMessage: String?

    enum Status {
        case idle
        case loading
        case ready
        case error
    }

    private static let systemRatingKw: Double = 9.6
    private static let performanceRatio: Double = 0.82
    private static let typicalPsh: Double = 4.8

    // MARK: - Default location

    static func defaultLocation() -> LocationInfo {
        return LocationInfo(lat: 37.7749, lng: -122.4194, label: "San Francisco, CA", source: .default)
    }

    // MARK: - Map weather code (Section 5.4)

    static func mapWeatherCode(_ code: Int) -> (condition: WeatherCondition, label: String) {
        if code == 0 { return (.clear, "Clear") }
        if code == 1 { return (.mostlyClear, "Mostly clear") }
        if code == 2 { return (.partlyCloudy, "Partly cloudy") }
        if code == 3 { return (.overcast, "Overcast") }
        if code == 45 || code == 48 { return (.fog, "Fog") }
        if code >= 51 && code <= 57 { return (.drizzle, "Drizzle") }
        if code >= 61 && code <= 67 {
            return code >= 65 ? (.heavyRain, "Heavy rain") : (.rain, "Rain")
        }
        if code >= 71 && code <= 77 { return (.snow, "Snow") }
        if code >= 80 && code <= 82 {
            return code == 82 ? (.heavyRain, "Heavy showers") : (.rain, "Showers")
        }
        if code == 85 || code == 86 { return (.snow, "Snow showers") }
        if code >= 95 { return (.thunderstorm, "Thunderstorm") }
        return (.partlyCloudy, "Mixed")
    }

    // MARK: - Fetch forecast

    func fetchForecast(location: LocationInfo, days: Int = 7) async {
        status = .loading
        errorMessage = nil

        guard var components = URLComponents(string: "https://api.open-meteo.com/v1/forecast") else {
            status = .error
            errorMessage = "Invalid URL"
            return
        }

        components.queryItems = [
            URLQueryItem(name: "latitude", value: String(location.lat)),
            URLQueryItem(name: "longitude", value: String(location.lng)),
            URLQueryItem(name: "daily", value: "weather_code,temperature_2m_max,temperature_2m_min,precipitation_sum,shortwave_radiation_sum,cloud_cover_mean"),
            URLQueryItem(name: "forecast_days", value: String(days)),
            URLQueryItem(name: "timezone", value: "auto")
        ]

        guard let url = components.url else {
            status = .error
            errorMessage = "Failed to build URL"
            return
        }

        do {
            let (data, response) = try await URLSession.shared.data(from: url)
            guard let httpResponse = response as? HTTPURLResponse, httpResponse.statusCode == 200 else {
                status = .error
                errorMessage = "Forecast API error: HTTP \((response as? HTTPURLResponse)?.statusCode ?? 0)"
                return
            }

            let forecast = Self.parseResponse(data: data, location: location)
            self.forecast = forecast
            status = .ready
        } catch {
            status = .error
            errorMessage = error.localizedDescription
        }
    }

    // MARK: - Parse response

    private static func parseResponse(data: Data, location: LocationInfo) -> ProductionForecast {
        struct OpenMeteoResponse: Codable {
            struct Daily: Codable {
                let time: [String]
                let weather_code: [Int]
                let temperature_2m_max: [Double]
                let temperature_2m_min: [Double]
                let precipitation_sum: [Double]
                let shortwave_radiation_sum: [Double]
                let cloud_cover_mean: [Double]
            }
            let daily: Daily
        }

        guard let decoded = try? JSONDecoder().decode(OpenMeteoResponse.self, from: data) else {
            return ProductionForecast(
                fetchedAt: Date().timeIntervalSince1970 * 1000,
                location: location,
                days: [],
                totalKwh: 0,
                vsLastWeekPct: 0
            )
        }

        let forecastDays: [ForecastDay] = decoded.daily.time.enumerated().map { (i, dateStr) in
            let radiationMJ = decoded.daily.shortwave_radiation_sum.indices.contains(i) ? decoded.daily.shortwave_radiation_sum[i] : 0
            let psh = (radiationMJ * 1000) / 3600
            let expectedKwh = psh * systemRatingKw * performanceRatio
            let code = decoded.daily.weather_code.indices.contains(i) ? decoded.daily.weather_code[i] : 0
            let mapped = mapWeatherCode(code)
            let typicalKwh = typicalPsh * systemRatingKw * performanceRatio
            return ForecastDay(
                date: dateStr,
                weatherCode: code,
                condition: mapped.condition,
                conditionLabel: mapped.label,
                tempHighC: decoded.daily.temperature_2m_max.indices.contains(i) ? decoded.daily.temperature_2m_max[i] : 0,
                tempLowC: decoded.daily.temperature_2m_min.indices.contains(i) ? decoded.daily.temperature_2m_min[i] : 0,
                precipitationMm: decoded.daily.precipitation_sum.indices.contains(i) ? decoded.daily.precipitation_sum[i] : 0,
                shortwaveRadiationMJ: radiationMJ,
                cloudCoverPct: decoded.daily.cloud_cover_mean.indices.contains(i) ? decoded.daily.cloud_cover_mean[i] : 0,
                expectedKwh: expectedKwh,
                expectedKwhVsTypical: typicalKwh > 0 ? (expectedKwh - typicalKwh) / typicalKwh : 0
            )
        }

        let totalKwh = forecastDays.reduce(0) { $0 + $1.expectedKwh }
        let lastWeekTypicalKwh = Double(forecastDays.count) * typicalPsh * systemRatingKw * performanceRatio
        let vsLastWeekPct = lastWeekTypicalKwh > 0 ? ((totalKwh - lastWeekTypicalKwh) / lastWeekTypicalKwh) * 100 : 0

        return ProductionForecast(
            fetchedAt: Date().timeIntervalSince1970 * 1000,
            location: location,
            days: forecastDays,
            totalKwh: totalKwh,
            vsLastWeekPct: vsLastWeekPct
        )
    }

    // MARK: - Day label

    static func dayLabel(for dateStr: String, todayIso: String) -> String {
        if dateStr == todayIso { return "Today" }
        let formatter = DateFormatter()
        formatter.dateFormat = "yyyy-MM-dd"
        guard let date = formatter.date(from: dateStr),
              let today = formatter.date(from: todayIso) else {
            return dateStr
        }
        let diffDays = Calendar.current.dateComponents([.day], from: today, to: date).day ?? 0
        if diffDays == 1 { return "Tomorrow" }
        let weekdayFormatter = DateFormatter()
        weekdayFormatter.dateFormat = "EEE"
        return weekdayFormatter.string(from: date)
    }
}

```
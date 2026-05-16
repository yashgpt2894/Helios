import Foundation

// MARK: - Format — byte-for-byte port of lib/format.ts

enum Format {
    static func clamp(_ value: Double, _ minVal: Double, _ maxVal: Double) -> Double {
        return Swift.min(maxVal, Swift.max(minVal, value))
    }

    static func formatKw(_ watts: Double, digits: Int = 2) -> String {
        let kw = watts / 1000
        return String(format: "%.\(digits)f", kw)
    }

    static func formatW(_ watts: Double) -> String {
        if abs(watts) >= 1000 {
            return String(format: "%.2f kW", watts / 1000)
        }
        return "\(Int(round(watts))) W"
    }

    static func formatKwh(_ kwh: Double, digits: Int = 1) -> String {
        return String(format: "%.\(digits)f kWh", kwh)
    }

    static func formatPercent(_ value: Double, digits: Int = 0) -> String {
        return String(format: "%.\(digits)f%%", value)
    }

    static func formatTemp(_ c: Double) -> String {
        return String(format: "%.1f\u{00b0}C", c)
    }

    static func formatCurrency(_ amount: Double, currency: String = "USD") -> String {
        let formatter = NumberFormatter()
        formatter.numberStyle = .currency
        formatter.currencyCode = currency
        formatter.maximumFractionDigits = 2
        return formatter.string(from: NSNumber(value: amount)) ?? "$\(amount)"
    }

    static func formatTimeOfDay(_ ts: TimeInterval) -> String {
        let date = Date(timeIntervalSince1970: ts / 1000)
        let formatter = DateFormatter()
        formatter.dateFormat = "h:mm a"
        return formatter.string(from: date)
    }

    static func formatRelative(_ ts: TimeInterval) -> String {
        let diff = Date().timeIntervalSince1970 - ts / 1000
        let s = Int(round(diff))
        if s < 5 { return "just now" }
        if s < 60 { return "\(s)s ago" }
        let m = s / 60
        if m < 60 { return "\(m)m ago" }
        let h = m / 60
        return "\(h)h ago"
    }
}

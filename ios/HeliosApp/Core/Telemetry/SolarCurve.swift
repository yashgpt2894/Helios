import Foundation

// MARK: - SolarCurve — byte-for-byte port of lib/solarCurve.ts

enum SolarCurve {
    static let sunriseHour: Double = 6.2
    static let sunsetHour: Double = 19.8
    static let peakHour: Double = 13.0

    static func clamp(_ value: Double, _ minVal: Double, _ maxVal: Double) -> Double {
        return Swift.min(maxVal, Swift.max(minVal, value))
    }

    /// solarFractionAt(hourFloat) — Section 1.1
    static func solarFractionAt(_ hourFloat: Double) -> Double {
        if hourFloat <= sunriseHour || hourFloat >= sunsetHour { return 0 }
        let x = (hourFloat - sunriseHour) / (sunsetHour - sunriseHour)
        let bell = sin(Double.pi * x)
        let skew = 1 - abs(hourFloat - peakHour) / 8
        return clamp(bell * skew, 0, 1)
    }

    /// irradianceAt(hourFloat, cloudCover) — Section 1.2
    static func irradianceAt(_ hourFloat: Double, _ cloudCover: Double) -> Double {
        let clear = solarFractionAt(hourFloat) * 1000
        let transmittance = 1 - cloudCover * 0.7
        return Swift.max(0, clear * transmittance)
    }

    /// consumptionFractionAt(hourFloat) — Section 1.3
    static func consumptionFractionAt(_ hourFloat: Double) -> Double {
        let morning = exp(-pow(hourFloat - 7.5, 2) / 2.5) * 0.7
        let evening = exp(-pow(hourFloat - 19, 2) / 4) * 1.0
        let baseline = 0.18
        let midday = exp(-pow(hourFloat - 13, 2) / 18) * 0.25
        return clamp(baseline + morning + evening + midday, 0.12, 1.2)
    }

    /// nowAsHourFloat(now) — Section 1.4
    static func nowAsHourFloat(_ now: Date = Date()) -> Double {
        let comps = Calendar.current.dateComponents([.hour, .minute, .second], from: now)
        let hours = Double(comps.hour ?? 0)
        let minutes = Double(comps.minute ?? 0)
        let seconds = Double(comps.second ?? 0)
        return hours + minutes / 60 + seconds / 3600
    }
}

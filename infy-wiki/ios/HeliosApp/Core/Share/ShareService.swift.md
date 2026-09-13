# ShareService

```swift
import Foundation

// MARK: - ShareService — port of services/share.ts

enum ShareService {
    static let CURRENT_VERSION = 1

    // MARK: - Encode/Decode

    static func encodeSnapshot(_ s: SnapshotPayload) -> String? {
        guard let data = try? JSONEncoder().encode(s) else { return nil }
        return data.base64EncodedString()
            .replacingOccurrences(of: "+", with: "-")
            .replacingOccurrences(of: "/", with: "_")
            .replacingOccurrences(of: "=", with: "")
    }

    static func decodeSnapshot(_ raw: String) -> SnapshotPayload? {
        var base64 = raw
            .replacingOccurrences(of: "-", with: "+")
            .replacingOccurrences(of: "_", with: "/")
        let padding = 4 - (base64.count % 4)
        if padding < 4 {
            base64 += String(repeating: "=", count: padding)
        }
        guard let data = Data(base64Encoded: base64) else { return nil }
        return try? JSONDecoder().decode(SnapshotPayload.self, from: data)
    }

    // MARK: - Build Snapshot

    static func buildSnapshot(t: SolarTelemetry, locLabel: String = "San Francisco, CA", brandId: String? = nil) -> SnapshotPayload {
        let selfUse = computeSelfConsumption(t)
        let forecastKwh = generateForecastSlice(t)
        return SnapshotPayload(
            v: CURRENT_VERSION,
            ts: t.timestamp,
            loc: locLabel,
            ac: t.acPowerW / 1000,
            todayKwh: t.energyTodayKwh,
            lifeKwh: Int(round(t.energyLifetimeKwh)),
            soc: Int(round(t.batterySoc)),
            selfUse: selfUse,
            fc: forecastKwh,
            br: brandId
        )
    }

    // MARK: - Share URL

    static func buildShareUrl(snapshot: SnapshotPayload) -> URL {
        guard let encoded = encodeSnapshot(snapshot) else {
            return URL(string: "https://helios.app/share")!
        }
        return URL(string: "https://helios.app/share/\(encoded)")!
    }

    // MARK: - Share or copy

    @MainActor
    static func shareOrCopy(snapshot: SnapshotPayload) -> (url: URL, items: [Any]) {
        let url = buildShareUrl(snapshot: snapshot)
        let text = """
        Helios snapshot
        \(String(format: "%.2f", snapshot.ac)) kW live
        \(String(format: "%.1f", snapshot.todayKwh)) kWh today · \(snapshot.lifeKwh) lifetime
        \(snapshot.soc)% SoC · \(snapshot.selfUse)% self-use
        \(url.absoluteString)
        """
        return (url, [text, url])
    }

    // MARK: - Private helpers

    private static func computeSelfConsumption(_ t: SolarTelemetry) -> Int {
        let sc: Double
        if t.acPowerW > 0 {
            sc = max(0, min(100, (t.acPowerW - t.gridExportW) / t.acPowerW * 100))
        } else {
            sc = 0
        }
        return Int(round(sc))
    }

    private static func generateForecastSlice(_ t: SolarTelemetry) -> [Int] {
        let hour = SolarCurve.nowAsHourFloat()
        var slices: [Int] = []
        for i in 0..<3 {
            let h = hour + Double(i) * 0.5
            let clamped = SolarCurve.clamp(h, 0, 24)
            let cf = SolarCurve.solarFractionAt(clamped)
            let w = Int(round(cf * 9600 * 0.964))
            slices.append(w)
        }
        return slices
    }
}

```
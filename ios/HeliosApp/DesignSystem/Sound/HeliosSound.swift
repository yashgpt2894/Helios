import Foundation

// MARK: - HeliosSound — opt-in sound stubs (default-on, respect silent switch)

enum HeliosSound {
    private static var isEnabled: Bool = {
        UserDefaults.standard.object(forKey: "helios-sound-enabled") as? Bool ?? true
    }()

    static var enabled: Bool {
        get { isEnabled }
        set {
            isEnabled = newValue
            UserDefaults.standard.set(newValue, forKey: "helios-sound-enabled")
        }
    }

    enum Clip: String {
        case sheetPresent = "sheet-present"
        case sheetDismiss = "sheet-dismiss"
        case snapshot = "snapshot"
        case togglePositive = "toggle-positive"
        case toggleNegative = "toggle-negative"
        case insightAppear = "insight-appear"
        case metricUpdate = "metric-update"
    }

    static func play(_ clip: Clip) {
        guard isEnabled else { return }
        // Sound playback stubbed pending audio asset bundle.
        // Implementation: use AVAudioPlayer with bundled .caf assets,
        // configured with AVAudioSession.Category.ambient to respect the
        // silent switch.
    }
}

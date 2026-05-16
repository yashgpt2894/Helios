import UIKit

// MARK: - HeliosHaptics — UIKit haptic triggers from design-tokens.json

enum HeliosHaptics {
    private static let generator = UIImpactFeedbackGenerator(style: .soft)
    private static let lightGenerator = UIImpactFeedbackGenerator(style: .light)
    private static let mediumGenerator = UIImpactFeedbackGenerator(style: .medium)
    private static let heavyGenerator = UIImpactFeedbackGenerator(style: .heavy)

    static func prepare() {
        generator.prepare()
        lightGenerator.prepare()
        mediumGenerator.prepare()
        heavyGenerator.prepare()
    }

    enum Event: String {
        case tapLight = "tap-light"
        case tapButton = "tap-button"
        case pullToRefresh = "pull-to-refresh"
        case tileSnap = "tile-snap"
        case tabChange = "tab-change"
        case toggleOn = "toggle-on"
        case toggleOff = "toggle-off"
        case success = "success"
        case warning = "warning"
        case error = "error"
        case shareOpened = "share-opened"
    }

    static func trigger(_ event: Event) {
        switch event {
        case .tapLight:
            lightGenerator.impactOccurred()
        case .tapButton:
            generator.impactOccurred(intensity: 0.65)
        case .pullToRefresh:
            mediumGenerator.impactOccurred(intensity: 0.8)
        case .tileSnap:
            lightGenerator.impactOccurred(intensity: 0.4)
        case .tabChange:
            generator.impactOccurred(intensity: 0.55)
        case .toggleOn:
            mediumGenerator.impactOccurred(intensity: 0.7)
        case .toggleOff:
            lightGenerator.impactOccurred(intensity: 0.35)
        case .success:
            UINotificationFeedbackGenerator().notificationOccurred(.success)
        case .warning:
            UINotificationFeedbackGenerator().notificationOccurred(.warning)
        case .error:
            UINotificationFeedbackGenerator().notificationOccurred(.error)
        case .shareOpened:
            mediumGenerator.impactOccurred(intensity: 0.75)
        }
    }
}

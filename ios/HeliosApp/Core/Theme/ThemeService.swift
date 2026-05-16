import SwiftUI

// MARK: - ThemeService — port of lib/theme.ts

@Observable
final class ThemeService {
    var theme: Theme = .auto {
        didSet {
            persistTheme(theme)
        }
    }

    enum Theme: String, CaseIterable {
        case auto
        case light
        case dark
    }

    init() {
        self.theme = loadTheme()
    }

    func resolveTheme(currentScheme: ColorScheme? = nil) -> ColorScheme? {
        switch theme {
        case .auto: return nil
        case .light: return .light
        case .dark: return .dark
        }
    }

    func applyTheme() -> Theme {
        return theme
    }

    // MARK: - Persistence

    private func loadTheme() -> Theme {
        guard let raw = UserDefaults.standard.string(forKey: "helios-theme") else { return .auto }
        return Theme(rawValue: raw) ?? .auto
    }

    private func persistTheme(_ theme: Theme) {
        UserDefaults.standard.set(theme.rawValue, forKey: "helios-theme")
    }
}

// MARK: - Preferred Color Scheme modifier

struct HeliosThemeModifier: ViewModifier {
    @Environment(ThemeService.self) private var themeService

    func body(content: Content) -> some View {
        content.preferredColorScheme(themeService.resolveTheme())
    }
}

extension View {
    func heliosTheme() -> some View {
        modifier(HeliosThemeModifier())
    }
}

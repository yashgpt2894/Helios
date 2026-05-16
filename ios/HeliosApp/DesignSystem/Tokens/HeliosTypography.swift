import SwiftUI

// MARK: - HeliosTypography — SF Pro scale from design-tokens.json

enum HeliosTypography {
    enum Scale: String, CaseIterable {
        case hero
        case title1
        case title2
        case title3
        case headline
        case body
        case callout
        case subheadline
        case caption
        case caption2
    }

    struct Token {
        let size: CGFloat
        let weight: Font.Weight
        let lineHeight: CGFloat
        let tracking: CGFloat
        let font: Font.Design
    }

    static func token(for scale: Scale) -> Token {
        switch scale {
        case .hero:
            return Token(size: 56, weight: .light, lineHeight: 56, tracking: -0.02, font: .default)
        case .title1:
            return Token(size: 34, weight: .regular, lineHeight: 40, tracking: -0.01, font: .default)
        case .title2:
            return Token(size: 28, weight: .regular, lineHeight: 34, tracking: -0.01, font: .default)
        case .title3:
            return Token(size: 22, weight: .regular, lineHeight: 28, tracking: -0.01, font: .default)
        case .headline:
            return Token(size: 17, weight: .semibold, lineHeight: 22, tracking: -0.01, font: .default)
        case .body:
            return Token(size: 16, weight: .regular, lineHeight: 22, tracking: -0.01, font: .default)
        case .callout:
            return Token(size: 14, weight: .regular, lineHeight: 20, tracking: -0.01, font: .default)
        case .subheadline:
            return Token(size: 13, weight: .regular, lineHeight: 18, tracking: -0.01, font: .default)
        case .caption:
            return Token(size: 11, weight: .regular, lineHeight: 14, tracking: 0.01, font: .default)
        case .caption2:
            return Token(size: 10, weight: .medium, lineHeight: 12, tracking: 0.06, font: .default)
        }
    }

    enum NumericVariant {
        case body
        case chartAxis
        case liveTicker
    }

    static func numericFont(size: CGFloat, weight: Font.Weight = .regular) -> Font {
        let uiFont = UIFont.monospacedDigitSystemFont(ofSize: size, weight: uiWeight(weight))
        return Font(uiFont)
    }

    static func font(for scale: Scale) -> Font {
        let t = token(for: scale)
        return .system(size: t.size, weight: t.weight, design: t.font)
    }

    static func numericFont(for scale: Scale, variant: NumericVariant = .liveTicker) -> Font {
        let t = token(for: scale)
        return numericFont(size: t.size, weight: t.weight)
    }

    private static func uiWeight(_ weight: Font.Weight) -> UIFont.Weight {
        switch weight {
        case .ultraLight: return .ultraLight
        case .thin: return .thin
        case .light: return .light
        case .regular: return .regular
        case .medium: return .medium
        case .semibold: return .semibold
        case .bold: return .bold
        case .heavy: return .heavy
        case .black: return .black
        default: return .regular
        }
    }
}

// MARK: - View Modifier

struct HeliosTextStyle: ViewModifier {
    let scale: HeliosTypography.Scale
    let color: Color

    func body(content: Content) -> some View {
        let t = HeliosTypography.token(for: scale)
        return content
            .font(.system(size: t.size, weight: t.weight, design: t.font))
            .tracking(t.tracking)
            .foregroundStyle(color)
    }
}

extension View {
    func heliosTextStyle(_ scale: HeliosTypography.Scale, color: Color) -> some View {
        modifier(HeliosTextStyle(scale: scale, color: color))
    }
}

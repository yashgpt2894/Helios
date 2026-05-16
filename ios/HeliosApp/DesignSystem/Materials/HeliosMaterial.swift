import SwiftUI

// MARK: - HeliosMaterial — P3-tinted materials with inner stroke

enum HeliosMaterial {
    struct RegularMaterial: ViewModifier {
        @Environment(\.colorScheme) var colorScheme

        func body(content: Content) -> some View {
            content
                .background(
                    RegularMaterialView()
                        .overlay(
                            RoundedRectangle(cornerRadius: 20)
                                .stroke(HeliosColor.separator(for: colorScheme), lineWidth: 0.5)
                        )
                )
        }
    }

    struct ThinMaterial: ViewModifier {
        @Environment(\.colorScheme) var colorScheme

        func body(content: Content) -> some View {
            content
                .background(
                    ThinMaterialView()
                        .overlay(
                            RoundedRectangle(cornerRadius: 16)
                                .stroke(HeliosColor.separator(for: colorScheme), lineWidth: 0.5)
                        )
                )
        }
    }

    struct RegularMaterialView: View {
        @Environment(\.colorScheme) var colorScheme

        var body: some View {
            Rectangle()
                .fill(.regularMaterial)
                .tintIfAvailable(NeutralTint.forScheme(colorScheme, alpha: 0.10))
        }
    }

    struct ThinMaterialView: View {
        @Environment(\.colorScheme) var colorScheme

        var body: some View {
            Rectangle()
                .fill(.thinMaterial)
                .tintIfAvailable(NeutralTint.forScheme(colorScheme, alpha: 0.06))
        }
    }
}

enum NeutralTint {
    static func forScheme(_ scheme: ColorScheme, alpha: Double) -> Color {
        let base: Color = scheme == .dark
            ? Color(red: 0.329, green: 0.310, blue: 0.278)
            : Color(red: 0.329, green: 0.310, blue: 0.278)
        return base.opacity(alpha)
    }
}

extension View {
    func heliosRegularMaterial() -> some View {
        modifier(HeliosMaterial.RegularMaterial())
    }

    func heliosThinMaterial() -> some View {
        modifier(HeliosMaterial.ThinMaterial())
    }
}

private extension View {
    func tintIfAvailable(_ color: Color) -> some View {
        self.overlay(color)
    }
}

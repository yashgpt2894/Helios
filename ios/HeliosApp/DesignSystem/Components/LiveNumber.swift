import SwiftUI

// MARK: - LiveNumber — animated numeric value

struct LiveNumber: View {
    let value: Double
    let digits: Int
    let variant: FontVariant

    enum FontVariant {
        case hero
        case large
        case medium
        case small
    }

    init(value: Double, digits: Int = 2, variant: FontVariant = .hero) {
        self.value = value
        self.digits = digits
        self.variant = variant
    }

    var body: some View {
        HStack(alignment: .firstTextBaseline, spacing: 0) {
            Text(formattedValue)
                .font(monospacedFont)
                .contentTransition(.numericText(value: value))
                .animation(.default, value: value)
        }
    }

    private var formattedValue: String {
        String(format: "%.\(digits)f", value)
    }

    private var monospacedFont: Font {
        switch variant {
        case .hero:
            return .system(size: 56, weight: .light, design: .monospaced)
        case .large:
            return .system(size: 34, weight: .regular, design: .monospaced)
        case .medium:
            return .system(size: 22, weight: .regular, design: .monospaced)
        case .small:
            return .system(size: 13, weight: .medium, design: .monospaced)
        }
    }
}

// MARK: - LiveNumberPreview

#Preview {
    VStack(spacing: 20) {
        LiveNumber(value: 4.23, variant: .hero)
        LiveNumber(value: 12.8, digits: 1, variant: .large)
        LiveNumber(value: 63.0, digits: 0, variant: .medium)
        LiveNumber(value: 847, digits: 0, variant: .small)
    }
    .preferredColorScheme(.dark)
}

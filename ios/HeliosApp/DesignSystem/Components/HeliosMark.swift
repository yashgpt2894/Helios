import SwiftUI

// MARK: - HeliosMark — 8-blade radial mark + variants

struct HeliosMark: View {
    let size: CGFloat
    let tint: Color
    let lineWidth: CGFloat

    init(size: CGFloat = 40, tint: Color = .primary, lineWidth: CGFloat = 1.5) {
        self.size = size
        self.tint = tint
        self.lineWidth = lineWidth
    }

    var body: some View {
        ZStack {
            ForEach(0..<8, id: \.self) { i in
                let angle = Double(i) / 8 * .pi * 2
                let innerRadius = size * 0.12
                let outerRadius = size * 0.48
                let midX = size / 2
                let midY = size / 2
                let startX = midX + innerRadius * cos(angle)
                let startY = midY + innerRadius * sin(angle)
                let endX = midX + outerRadius * cos(angle)
                let endY = midY + outerRadius * sin(angle)

                Path { p in
                    p.move(to: CGPoint(x: startX, y: startY))
                    p.addLine(to: CGPoint(x: endX, y: endY))
                }
                .stroke(tint, lineWidth: lineWidth)
            }
            RingDot(size: size, tint: tint)
        }
        .frame(width: size, height: size)
    }
}

private struct RingDot: View {
    let size: CGFloat
    let tint: Color
    let dotSize: CGFloat

    init(size: CGFloat, tint: Color, dotSize: CGFloat? = nil) {
        self.size = size
        self.tint = tint
        self.dotSize = dotSize ?? size * 0.14
    }

    var body: some View {
        Circle()
            .fill(tint)
            .frame(width: dotSize, height: dotSize)
            .position(x: size / 2, y: size * 0.06)
    }
}

// MARK: - BrandMark

struct BrandMark: View {
    let brand: BrandInfo
    let size: CGFloat

    init(brand: BrandInfo, size: CGFloat = 32) {
        self.brand = brand
        self.size = size
    }

    var body: some View {
        switch brand.mark {
        case "helios":
            HeliosMark(size: size, tint: Color(hex: brand.accent) ?? .yellow, lineWidth: 1.5)
        case "text":
            if let ch = brand.textMark, let first = ch.first {
                TextCircleMark(letter: String(first), color: Color(hex: brand.accent) ?? .blue, size: size)
            } else {
                HeliosMark(size: size, tint: .secondary, lineWidth: 1.5)
            }
        default:
            HeliosMark(size: size, tint: .secondary, lineWidth: 1.5)
        }
    }
}

// MARK: - TextCircleMark

struct TextCircleMark: View {
    let letter: String
    let color: Color
    let size: CGFloat

    var body: some View {
        Circle()
            .fill(color.opacity(0.15))
            .frame(width: size, height: size)
            .overlay(
                Text(letter)
                    .font(.system(size: size * 0.5, weight: .bold, design: .rounded))
                    .foregroundStyle(color)
            )
    }
}

// MARK: - Color hex helper

extension Color {
    init?(hex: String) {
        let r, g, b: Double
        var hexSanitized = hex.trimmingCharacters(in: .whitespacesAndNewlines)
        hexSanitized = hexSanitized.replacingOccurrences(of: "#", with: "")

        guard hexSanitized.count == 6 else { return nil }

        var rgb: UInt64 = 0
        guard Scanner(string: hexSanitized).scanHexInt64(&rgb) else { return nil }

        r = Double((rgb & 0xFF0000) >> 16) / 255.0
        g = Double((rgb & 0x00FF00) >> 8) / 255.0
        b = Double(rgb & 0x0000FF) / 255.0

        self.init(red: r, green: g, blue: b)
    }
}

import SwiftUI

// MARK: - MetricTile — uniform dashboard card

struct MetricTile: View {
    let label: String
    let value: String
    let unit: String?
    let icon: String?
    let color: Color
    let detail: String?

    init(label: String, value: String, unit: String? = nil, icon: String? = nil, color: Color = .primary, detail: String? = nil) {
        self.label = label
        self.value = value
        self.unit = unit
        self.icon = icon
        self.color = color
        self.detail = detail
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 6) {
            HStack(alignment: .firstTextBaseline, spacing: 2) {
                if let icon = icon {
                    Image(systemName: icon)
                        .font(.system(size: 12))
                        .foregroundStyle(color)
                }
                Text(label.uppercased())
                    .font(.system(size: 10, weight: .medium, design: .monospaced))
                    .tracking(0.15)
                    .foregroundStyle(.secondary)
            }

            HStack(alignment: .firstTextBaseline, spacing: 2) {
                Text(value)
                    .font(HeliosTypography.numericFont(size: 26, weight: .light))
                    .foregroundStyle(color)
                if let unit = unit {
                    Text(unit)
                        .font(.system(size: 12, design: .monospaced))
                        .foregroundStyle(.secondary)
                        .padding(.leading, 1)
                }
            }

            if let detail = detail {
                Text(detail)
                    .font(.system(size: 10, design: .monospaced))
                    .foregroundStyle(.tertiary)
            }
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(12)
        .background(.ultraThinMaterial, in: RoundedRectangle(cornerRadius: 12))
        .overlay(
            RoundedRectangle(cornerRadius: 12)
                .stroke(.white.opacity(0.04), lineWidth: 0.5)
        )
    }
}

// MARK: - MetricTileRow

struct MetricTileRow: View {
    let tiles: [MetricTileConfiguration]

    struct MetricTileConfiguration: Identifiable {
        let id = UUID()
        let label: String
        let value: String
        let unit: String?
        let icon: String?
        let color: Color
        let detail: String?

        init(label: String, value: String, unit: String? = nil, icon: String? = nil, color: Color = .primary, detail: String? = nil) {
            self.label = label
            self.value = value
            self.unit = unit
            self.icon = icon
            self.color = color
            self.detail = detail
        }
    }

    var body: some View {
        HStack(spacing: 8) {
            ForEach(tiles) { tile in
                MetricTile(
                    label: tile.label,
                    value: tile.value,
                    unit: tile.unit,
                    icon: tile.icon,
                    color: tile.color,
                    detail: tile.detail
                )
            }
        }
    }
}

import SwiftUI

// MARK: - WeatherIcon — SF Symbol weather mappings

struct WeatherIcon: View {
    let condition: WeatherCondition
    let size: WeatherIconSize

    enum WeatherIconSize {
        case small
        case medium
        case large

        var symbolSize: CGFloat {
            switch self {
            case .small: return 16
            case .medium: return 24
            case .large: return 32
            }
        }
    }

    init(condition: WeatherCondition, size: WeatherIconSize = .medium) {
        self.condition = condition
        self.size = size
    }

    var body: some View {
        Image(systemName: symbolName)
            .font(.system(size: size.symbolSize))
            .symbolRenderingMode(.hierarchical)
            .foregroundStyle(color)
    }

    private var symbolName: String {
        switch condition {
        case .clear:             return "sun.max.fill"
        case .mostlyClear:       return "sun.max"
        case .partlyCloudy:      return "cloud.sun"
        case .overcast:          return "cloud.fill"
        case .fog:               return "cloud.fog"
        case .drizzle:           return "cloud.drizzle"
        case .rain:              return "cloud.rain"
        case .heavyRain:         return "cloud.heavyrain"
        case .snow:              return "snowflake"
        case .thunderstorm:      return "cloud.bolt.rain"
        }
    }

    private var color: Color {
        switch condition {
        case .clear:             return Color(red: 0.941, green: 0.776, blue: 0.455)
        case .mostlyClear:       return Color(red: 0.831, green: 0.659, blue: 0.263)
        case .partlyCloudy:      return Color(red: 0.725, green: 0.690, blue: 0.651)
        case .overcast:          return .secondary
        case .fog:               return .secondary.opacity(0.7)
        case .drizzle:           return Color(red: 0.365, green: 0.541, blue: 0.659)
        case .rain:              return Color(red: 0.290, green: 0.478, blue: 0.620)
        case .heavyRain:         return Color(red: 0.184, green: 0.310, blue: 0.416)
        case .snow:              return .white.opacity(0.9)
        case .thunderstorm:      return Color(red: 0.604, green: 0.431, blue: 0.122)
        }
    }
}

#Preview {
    HStack(spacing: 12) {
        ForEach(WeatherCondition.allCases, id: \.self) { c in
            WeatherIcon(condition: c, size: .small)
        }
    }
    .padding()
    .preferredColorScheme(.dark)
}

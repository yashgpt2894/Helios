import SwiftUI

// MARK: - SectionHeader

struct SectionHeader: View {
    let eyebrow: String?
    let title: String
    let trailing: String?

    init(title: String, eyebrow: String? = nil, trailing: String? = nil) {
        self.title = title
        self.eyebrow = eyebrow
        self.trailing = trailing
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            HStack {
                VStack(alignment: .leading, spacing: 2) {
                    if let eyebrow = eyebrow {
                        Text(eyebrow.uppercased())
                            .font(.system(size: 10, weight: .medium, design: .monospaced))
                            .tracking(0.2)
                            .foregroundStyle(.secondary)
                    }
                    Text(title)
                        .font(.system(size: 16, weight: .semibold))
                        .foregroundStyle(.primary)
                }

                Spacer()

                if let trailing = trailing {
                    Text(trailing)
                        .font(.system(size: 11, design: .monospaced))
                        .foregroundStyle(.secondary)
                }
            }
        }
    }
}

#Preview {
    VStack(spacing: 16) {
        SectionHeader(
            title: "Energy Flow",
            eyebrow: "Now",
            trailing: "1 min ago"
        )
        SectionHeader(
            title: "Weekly Production",
            trailing: "38.4 kWh"
        )
        SectionHeader(
            title: "Forecast"
        )
    }
    .padding(20)
    .preferredColorScheme(.dark)
}

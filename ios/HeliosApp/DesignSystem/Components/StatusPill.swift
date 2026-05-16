import SwiftUI

// MARK: - StatusPill — status dot with pulsing animation

struct StatusPill: View {
    let status: InverterStatus
    let showLabel: Bool

    @State private var isPulsing = false

    init(status: InverterStatus, showLabel: Bool = true) {
        self.status = status
        self.showLabel = showLabel
    }

    var body: some View {
        HStack(spacing: 6) {
            Circle()
                .fill(color)
                .frame(width: dotSize, height: dotSize)
                .scaleEffect(isPulsing ? 1.3 : 1.0)
                .opacity(isPulsing ? 0.6 : 1.0)
                .animation(
                    isActive ? .easeInOut(duration: 1.2).repeatForever(autoreverses: true) : .default,
                    value: isPulsing
                )

            if showLabel {
                Text(statusLabel)
                    .font(.system(size: 10, weight: .medium, design: .monospaced))
                    .tracking(0.15)
                    .foregroundStyle(color)
            }
        }
        .onAppear {
            if isActive {
                isPulsing = true
            }
        }
        .onChange(of: status) { _, newStatus in
            isPulsing = isActive
        }
    }

    private var isActive: Bool {
        status == .producing
    }

    private var dotSize: CGFloat {
        switch status {
        case .producing: return 5
        case .fault: return 5
        default: return 4
        }
    }

    private var color: Color {
        switch status {
        case .producing: return Color(red: 0.498, green: 0.690, blue: 0.412)
        case .standby: return .secondary
        case .curtailed: return Color(red: 0.961, green: 0.486, blue: 0.0)
        case .night: return .secondary.opacity(0.5)
        case .fault: return .red
        }
    }

    private var statusLabel: String {
        switch status {
        case .producing: return "PRODUCING"
        case .standby: return "STANDBY"
        case .curtailed: return "CURTAILED"
        case .night: return "NIGHT"
        case .fault: return "FAULT"
        }
    }
}

#Preview {
    VStack(spacing: 12) {
        ForEach(InverterStatus.allCases, id: \.self) { s in
            StatusPill(status: s)
        }
    }
    .padding()
    .preferredColorScheme(.dark)
}

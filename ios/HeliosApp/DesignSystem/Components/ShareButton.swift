import SwiftUI

// MARK: - ShareButton

struct ShareButton: View {
    let action: () -> Void
    @State private var feedback: String? = nil

    var body: some View {
        Button(action: {
            HeliosHaptics.trigger(.shareOpened)
            action()
        }) {
            Image(systemName: "square.and.arrow.up")
                .font(.system(size: 15, weight: .medium))
                .foregroundStyle(.secondary)
                .frame(width: 28, height: 28)
                .background(.ultraThinMaterial, in: Circle())
                .overlay(
                    Circle()
                        .stroke(.white.opacity(0.06), lineWidth: 0.5)
                )
        }
        .overlay(alignment: .top) {
            if let feedback = feedback {
                Text(feedback)
                    .font(.system(size: 9, design: .monospaced))
                    .foregroundStyle(.secondary)
                    .padding(.horizontal, 6)
                    .padding(.vertical, 3)
                    .background(.ultraThinMaterial, in: RoundedRectangle(cornerRadius: 4))
                    .offset(y: -30)
                    .transition(.opacity.combined(with: .scale(scale: 0.8)))
            }
        }
    }

    private func flash(_ text: String) {
        withAnimation(.spring(response: 0.3, dampingFraction: 0.7)) {
            feedback = text
        }
        DispatchQueue.main.asyncAfter(deadline: .now() + 1.5) {
            withAnimation(.spring(response: 0.3, dampingFraction: 0.7)) {
                if feedback == text {
                    feedback = nil
                }
            }
        }
    }
}

// MARK: - ShareSheet UIKit bridge

struct ShareSheet: UIViewControllerRepresentable {
    let items: [Any]
    var completion: ((Bool) -> Void)? = nil

    func makeUIViewController(context: Context) -> UIActivityViewController {
        let controller = UIActivityViewController(activityItems: items, applicationActivities: nil)
        controller.completionWithItemsHandler = { _, completed, _, _ in
            completion?(completed)
        }
        return controller
    }

    func updateUIViewController(_ uiViewController: UIActivityViewController, context: Context) {}
}

#Preview {
    ShareButton {}
        .frame(width: 60, height: 60)
        .preferredColorScheme(.dark)
}

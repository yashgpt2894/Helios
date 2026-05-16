import SwiftUI

// MARK: - HeliosMotion — spring presets from design-tokens.json

enum HeliosMotion {
    enum Preset {
        case gentle
        case `default`
        case snappy
        case bouncy
    }

    static func spring(_ preset: Preset = .default, duration: TimeInterval? = nil) -> Animation {
        switch preset {
        case .gentle:
            return .spring(response: responseFor(stiffness: 120, damping: 24),
                           dampingFraction: dampingFractionFor(damping: 24, mass: 1, stiffness: 120),
                           blendDuration: duration ?? 0.5)
        case .default:
            return .spring(response: responseFor(stiffness: 200, damping: 26),
                           dampingFraction: dampingFractionFor(damping: 26, mass: 1, stiffness: 200),
                           blendDuration: duration ?? 0.35)
        case .snappy:
            return .spring(response: responseFor(stiffness: 380, damping: 32),
                           dampingFraction: dampingFractionFor(damping: 32, mass: 1, stiffness: 380),
                           blendDuration: duration ?? 0.25)
        case .bouncy:
            return .spring(response: responseFor(stiffness: 280, damping: 14),
                           dampingFraction: dampingFractionFor(damping: 14, mass: 1.2, stiffness: 280),
                           blendDuration: duration ?? 0.6)
        }
    }

    enum Duration {
        static let instant: TimeInterval = 0
        static let fast: TimeInterval = 0.15
        static let normal: TimeInterval = 0.25
        static let slow: TimeInterval = 0.4
        static let deliberate: TimeInterval = 0.6
        static let draw: TimeInterval = 0.6
    }

    enum Stagger {
        static let tight: TimeInterval = 0.03
        static let normal: TimeInterval = 0.05
        static let loose: TimeInterval = 0.08
    }

    // MARK: - Spring math helpers

    private static func responseFor(stiffness: Double, damping: Double) -> Double {
        let mass: Double = 1.0
        let naturalFreq = sqrt(stiffness / mass)
        let dampingRatio = damping / (2 * sqrt(mass * stiffness))
        if dampingRatio >= 1 {
            return 0.3
        }
        return (2 * .pi) / (naturalFreq * sqrt(1 - dampingRatio * dampingRatio))
    }

    private static func dampingFractionFor(damping: Double, mass: Double, stiffness: Double) -> CGFloat {
        let ratio = damping / (2 * sqrt(mass * stiffness))
        return CGFloat(min(ratio, 0.999))
    }
}

// MARK: - Choreographed entrance modifier

struct ChoreographedEntrance: ViewModifier {
    let delay: TimeInterval
    let translateY: CGFloat
    @State private var isVisible = false

    func body(content: Content) -> some View {
        content
            .opacity(isVisible ? 1 : 0)
            .offset(y: isVisible ? 0 : translateY)
            .animation(HeliosMotion.spring(.gentle).delay(delay), value: isVisible)
            .onAppear {
                isVisible = true
            }
    }
}

struct StaggeredEntrance: ViewModifier {
    let index: Int
    let baseDelay: TimeInterval
    let stagger: TimeInterval
    @State private var isVisible = false

    func body(content: Content) -> some View {
        let delay = baseDelay + Double(index) * stagger
        return content
            .opacity(isVisible ? 1 : 0)
            .offset(y: isVisible ? 0 : 12)
            .animation(HeliosMotion.spring(.default).delay(delay), value: isVisible)
            .onAppear {
                DispatchQueue.main.asyncAfter(deadline: .now() + delay) {
                    isVisible = true
                }
            }
    }
}

extension View {
    func choreographedEntrance(delay: TimeInterval = 0, translateY: CGFloat = 12) -> some View {
        modifier(ChoreographedEntrance(delay: delay, translateY: translateY))
    }

    func staggeredEntrance(index: Int, baseDelay: TimeInterval = 0, stagger: TimeInterval = HeliosMotion.Stagger.normal) -> some View {
        modifier(StaggeredEntrance(index: index, baseDelay: baseDelay, stagger: stagger))
    }
}

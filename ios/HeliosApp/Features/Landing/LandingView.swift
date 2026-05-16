import SwiftUI

// MARK: - LandingView — port of Landing.tsx

struct LandingView: View {
    @Environment(\.colorScheme) var colorScheme
    @State private var heroVisible = false
    @State private var featureCards: [Bool] = Array(repeating: false, count: 6)
    @State private var ctaVisible = false

    private let brand = BrandService.HELIOS

    private let features: [(icon: String, title: String, body: String)] = [
        (
            icon: "sparkles",
            title: "AI insights, not just charts",
            body: "Helios reads your inverter, battery, weather, and rate plan together. It tells you when to run the dishwasher, pre-charge before storms, and which string is shading."
        ),
        (
            icon: "sun.max",
            title: "7-day production forecast",
            body: "Real solar irradiance modeling powered by Open-Meteo. See exactly how much energy your array will make tomorrow, Saturday, and the whole week."
        ),
        (
            icon: "cable.connector",
            title: "Works with any inverter",
            body: "Speaks SunSpec Modbus — the industry standard. SMA, Fronius, SolarEdge, Enphase, Schneider. One app for your whole system, even after you upgrade."
        ),
        (
            icon: "battery.75",
            title: "Battery strategy that actually thinks",
            body: "Self-consumption, time-of-use, or backup-only — pick the mode and Helios optimizes against your forecast and rate plan automatically."
        ),
        (
            icon: "shield.checkered",
            title: "Local-first, privacy-respecting",
            body: "Telemetry stays on your network. Coordinates only go to Open-Meteo for the forecast. No third-party tracking, no behavioral ads."
        ),
        (
            icon: "bolt",
            title: "Installs in 60 seconds",
            body: "A Progressive Web App — no App Store. Tap \"Add to Home Screen\" and you are done. Works offline after first load."
        )
    ]

    var body: some View {
        ZStack(alignment: .top) {
            // Background
            HeliosColor.backgroundPrimary(for: colorScheme)
                .ignoresSafeArea()

            // Aurora overlay
            auroraBackground
                .ignoresSafeArea()

            // Content
            ScrollView(.vertical, showsIndicators: false) {
                VStack(spacing: 0) {
                    // Header
                    landingHeader
                        .padding(.horizontal, 20)
                        .padding(.top, 8)

                    // Hero section
                    heroSection
                        .padding(.horizontal, 20)
                        .padding(.top, 28)

                    // Features section
                    featuresSection
                        .padding(.horizontal, 20)
                        .padding(.top, 48)

                    // CTA section
                    ctaSection
                        .padding(.horizontal, 20)
                        .padding(.top, 48)

                    // Footer
                    landingFooter
                        .padding(.horizontal, 20)
                        .padding(.top, 48)
                        .padding(.bottom, 40)
                }
            }
        }
        .onAppear {
            withAnimation(.easeOut(duration: 0.6)) {
                heroVisible = true
            }
            // Stagger feature cards
            for i in 0..<6 {
                DispatchQueue.main.asyncAfter(deadline: .now() + 0.3 + Double(i) * 0.06) {
                    withAnimation(HeliosMotion.spring(.default)) {
                        featureCards[i] = true
                    }
                }
            }
            DispatchQueue.main.asyncAfter(deadline: .now() + 1.0) {
                withAnimation(HeliosMotion.spring(.gentle)) {
                    ctaVisible = true
                }
            }
        }
    }

    // MARK: - Aurora Background

    private var auroraBackground: some View {
        ZStack {
            // Top-left glow
            Circle()
                .fill(
                    RadialGradient(
                        colors: [
                            HeliosColor.solar(for: colorScheme, step: 400).opacity(0.12),
                            .clear
                        ],
                        center: .topLeading,
                        startRadius: 60,
                        endRadius: 400
                    )
                )
                .frame(width: 500, height: 500)
                .offset(x: -200, y: -100)
                .allowsHitTesting(false)

            // Bottom-right glow
            Circle()
                .fill(
                    RadialGradient(
                        colors: [
                            HeliosColor.flow(for: colorScheme, step: 300).opacity(0.08),
                            .clear
                        ],
                        center: .bottomTrailing,
                        startRadius: 80,
                        endRadius: 450
                    )
                )
                .frame(width: 500, height: 500)
                .offset(x: 200, y: 300)
                .allowsHitTesting(false)
        }
    }

    // MARK: - Header

    private var landingHeader: some View {
        HStack {
            HStack(spacing: 8) {
                HeliosMark(size: 32)
                Text(brand.name)
                    .font(.system(size: 15, weight: .medium))
                    .foregroundStyle(HeliosColor.textPrimary(for: colorScheme))
            }

            Spacer()

            NavigationLink {
                DashboardView()
            } label: {
                Text("OPEN APP →")
                    .font(.system(size: 11, weight: .medium, design: .monospaced))
                    .tracking(0.2)
                    .foregroundStyle(.secondary)
            }
        }
        .padding(.horizontal, 4)
    }

    // MARK: - Hero Section

    private var heroSection: some View {
        VStack(alignment: .leading, spacing: 0) {
            Text("SOLAR INTELLIGENCE")
                .font(.system(size: 10, weight: .medium, design: .monospaced))
                .tracking(0.2)
                .foregroundStyle(.secondary)

            VStack(alignment: .leading, spacing: -4) {
                Text("Your solar array,")
                    .font(.system(size: 44, weight: .light))
                    .foregroundStyle(HeliosColor.textPrimary(for: colorScheme))

                Text("finally explained.")
                    .font(.system(size: 44, weight: .light))
                    .foregroundStyle(
                        LinearGradient(
                            colors: [
                                HeliosColor.solar(for: colorScheme, step: 200),
                                HeliosColor.solar(for: colorScheme, step: 500)
                            ],
                            startPoint: .leading,
                            endPoint: .trailing
                        )
                    )
            }
            .padding(.top, 4)

            Text("\(brand.tagline ?? "Energy clarity.") Live telemetry from any SunSpec-compatible inverter, plus AI insights that tell you what to do — not just what happened.")
                .font(.system(size: 15))
                .foregroundStyle(HeliosColor.textSecondary(for: colorScheme))
                .lineSpacing(4)
                .padding(.top, 16)
                .fixedSize(horizontal: false, vertical: true)

            // CTA buttons
            HStack(spacing: 10) {
                NavigationLink {
                    DashboardView()
                } label: {
                    Label("Open the app", systemImage: "arrow.right")
                        .font(.system(size: 13, weight: .medium))
                        .foregroundStyle(colorScheme == .dark ? .black : .white)
                        .padding(.horizontal, 18)
                        .padding(.vertical, 10)
                        .background(
                            HeliosColor.textPrimary(for: colorScheme),
                            in: Capsule()
                        )
                }

                Button {
                    // Scroll to features — no-op for now
                } label: {
                    Text("How it works")
                        .font(.system(size: 13))
                        .foregroundStyle(HeliosColor.textSecondary(for: colorScheme))
                        .padding(.horizontal, 18)
                        .padding(.vertical, 10)
                        .background(
                            Capsule()
                                .stroke(HeliosColor.separator(for: colorScheme), lineWidth: 0.5)
                        )
                }
            }
            .padding(.top, 24)
        }
        .opacity(heroVisible ? 1 : 0)
        .offset(y: heroVisible ? 0 : 16)
    }

    // MARK: - Features Section

    private var featuresSection: some View {
        VStack(alignment: .leading, spacing: 0) {
            Text("WHAT YOU GET")
                .font(.system(size: 10, weight: .medium, design: .monospaced))
                .tracking(0.2)
                .foregroundStyle(.secondary)

            Text("Six things your inverter app won't do.")
                .font(.system(size: 28, weight: .medium))
                .foregroundStyle(HeliosColor.textPrimary(for: colorScheme))
                .padding(.top, 4)

            LazyVGrid(columns: [
                GridItem(.flexible(), spacing: 12),
                GridItem(.flexible(), spacing: 12)
            ], spacing: 12) {
                ForEach(Array(features.enumerated()), id: \.offset) { i, feature in
                    featureCard(icon: feature.icon, title: feature.title, body: feature.body, index: i)
                }
            }
            .padding(.top, 24)
        }
    }

    private func featureCard(icon: String, title: String, body: String, index: Int) -> some View {
        VStack(alignment: .leading, spacing: 8) {
            // Icon circle
            ZStack {
                Circle()
                    .fill(HeliosColor.backgroundTertiary(for: colorScheme))
                    .frame(width: 32, height: 32)
                Image(systemName: icon)
                    .font(.system(size: 14))
                    .foregroundStyle(HeliosColor.solar(for: colorScheme, step: 400))
            }

            Text(title)
                .font(.system(size: 14, weight: .medium))
                .foregroundStyle(HeliosColor.textPrimary(for: colorScheme))
                .fixedSize(horizontal: false, vertical: true)

            Text(body)
                .font(.system(size: 12))
                .foregroundStyle(HeliosColor.textTertiary(for: colorScheme))
                .lineSpacing(3)
                .fixedSize(horizontal: false, vertical: true)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(14)
        .background(HeliosColor.backgroundSecondary(for: colorScheme), in: RoundedRectangle(cornerRadius: 14))
        .overlay(
            RoundedRectangle(cornerRadius: 14)
                .stroke(.white.opacity(0.04), lineWidth: 0.5)
        )
        .opacity(featureCards[safe: index] ?? false ? 1 : 0)
        .offset(y: featureCards[safe: index] ?? false ? 0 : 12)
    }

    // MARK: - CTA Section

    private var ctaSection: some View {
        VStack(spacing: 16) {
            HeliosMark(size: 56)

            Text("Start using \(brand.name) today.")
                .font(.system(size: 28, weight: .light))
                .foregroundStyle(HeliosColor.textPrimary(for: colorScheme))
                .multilineTextAlignment(.center)

            Text("The app loads in simulation mode by default — explore every screen with realistic sample data before connecting your inverter.")
                .font(.system(size: 13))
                .foregroundStyle(HeliosColor.textTertiary(for: colorScheme))
                .multilineTextAlignment(.center)
                .fixedSize(horizontal: false, vertical: true)

            NavigationLink {
                DashboardView()
            } label: {
                Label("Open the app", systemImage: "arrow.right")
                    .font(.system(size: 14, weight: .medium))
                    .foregroundStyle(colorScheme == .dark ? .black : .white)
                    .padding(.horizontal, 22)
                    .padding(.vertical, 11)
                    .background(
                        HeliosColor.textPrimary(for: colorScheme),
                        in: Capsule()
                    )
            }
        }
        .frame(maxWidth: .infinity)
        .padding(28)
        .background(.ultraThinMaterial, in: RoundedRectangle(cornerRadius: 20))
        .overlay(
            RoundedRectangle(cornerRadius: 20)
                .stroke(.white.opacity(0.06), lineWidth: 0.5)
        )
        .opacity(ctaVisible ? 1 : 0)
        .offset(y: ctaVisible ? 0 : 12)
    }

    // MARK: - Footer

    private var landingFooter: some View {
        VStack(spacing: 16) {
            Divider()
                .foregroundStyle(HeliosColor.separator(for: colorScheme))

            HStack(alignment: .top) {
                HStack(spacing: 6) {
                    HeliosMark(size: 18)
                    Text(brand.legalName ?? brand.name)
                        .font(.system(size: 11, design: .monospaced))
                        .foregroundStyle(.tertiary)
                }

                Spacer()

                HStack(spacing: 4) {
                    Image(systemName: "mappin")
                        .font(.system(size: 10))
                        .foregroundStyle(.tertiary)
                    Text("Made for the sun · 2026")
                        .font(.system(size: 11, design: .monospaced))
                        .foregroundStyle(.tertiary)
                }
            }
        }
    }
}

// MARK: - Array safe subscript helper

private extension Array {
    subscript(safe index: Int) -> Element? {
        indices.contains(index) ? self[index] : nil
    }
}

// MARK: - Preview

#Preview("Landing") {
    NavigationStack {
        LandingView()
    }
    .preferredColorScheme(.dark)
}

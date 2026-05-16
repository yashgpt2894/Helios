import SwiftUI

// MARK: - SharedView — port of SharedView.tsx

struct SharedView: View {
    @Environment(\.colorScheme) var colorScheme

    let snapshot: SnapshotPayload?
    let encoded: String?

    @State private var heroVisible = false
    @State private var gridVisible = false
    @State private var forecastVisible = false
    @State private var ctaVisible = false

    init(encoded: String? = nil) {
        self.encoded = encoded
        if let encoded = encoded {
            self.snapshot = ShareService.decodeSnapshot(encoded)
        } else {
            self.snapshot = nil
        }
    }

    var body: some View {
        ZStack(alignment: .top) {
            // Background
            HeliosColor.backgroundPrimary(for: colorScheme)
                .ignoresSafeArea()

            // Aurora overlay
            auroraBackground
                .ignoresSafeArea()

            if let snapshot = snapshot {
                // Valid snapshot
                ScrollView(.vertical, showsIndicators: false) {
                    VStack(spacing: 20) {
                        sharedHeader
                            .padding(.horizontal, 16)

                        // Hero kW card
                        heroCard(snapshot: snapshot)
                            .padding(.horizontal, 16)

                        // 2×2 metrics grid
                        metricsGrid(snapshot: snapshot)
                            .padding(.horizontal, 16)

                        // 5-day forecast
                        if let fc = snapshot.fc, !fc.isEmpty {
                            forecastSection(fc: fc)
                                .padding(.horizontal, 16)
                        }

                        // CTA card
                        ctaCard(snapshot: snapshot)
                            .padding(.horizontal, 16)

                        // Footer
                        footerView
                            .padding(.horizontal, 16)
                            .padding(.bottom, 40)
                    }
                    .padding(.top, 8)
                }
            } else {
                // Invalid / expired
                expiredView
            }
        }
        .onAppear {
            guard snapshot != nil else { return }
            withAnimation(.easeOut(duration: 0.5)) {
                heroVisible = true
            }
            DispatchQueue.main.asyncAfter(deadline: .now() + 0.25) {
                withAnimation(HeliosMotion.spring(.default)) {
                    gridVisible = true
                }
            }
            DispatchQueue.main.asyncAfter(deadline: .now() + 0.4) {
                withAnimation(HeliosMotion.spring(.default)) {
                    forecastVisible = true
                }
            }
            DispatchQueue.main.asyncAfter(deadline: .now() + 0.55) {
                withAnimation(HeliosMotion.spring(.gentle)) {
                    ctaVisible = true
                }
            }
        }
    }

    // MARK: - Aurora Background

    private var auroraBackground: some View {
        ZStack {
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

    private var sharedHeader: some View {
        HStack {
            NavigationLink(destination: LandingView()) {
                HStack(spacing: 8) {
                    HeliosMark(size: 28)
                    Text(BrandService.HELIOS.name)
                        .font(.system(size: 14, weight: .medium))
                        .foregroundStyle(HeliosColor.textPrimary(for: colorScheme))
                }
            }

            Spacer()

            Text("SHARED SNAPSHOT")
                .font(.system(size: 10, weight: .medium, design: .monospaced))
                .tracking(0.2)
                .foregroundStyle(.secondary)
        }
        .padding(.horizontal, 4)
    }

    // MARK: - Hero kW Card

    private func heroCard(snapshot: SnapshotPayload) -> some View {
        VStack(alignment: .leading, spacing: 0) {
            // Location
            HStack(spacing: 4) {
                Image(systemName: "mappin")
                    .font(.system(size: 10))
                    .foregroundStyle(.tertiary)
                Text(snapshot.loc ?? "Unknown")
                    .font(.system(size: 11, design: .monospaced))
                    .foregroundStyle(HeliosColor.textSecondary(for: colorScheme))
            }
            .padding(.bottom, 12)

            // Date label
            Text(formattedDateTime(snapshot))
                .font(.system(size: 11, weight: .medium, design: .monospaced))
                .tracking(0.2)
                .foregroundStyle(.tertiary)
                .padding(.bottom, 12)

            // kW reading
            HStack(alignment: .bottom, spacing: 4) {
                Text(String(format: "%.2f", snapshot.ac))
                    .font(.system(size: 56, weight: .light, design: .monospaced))
                    .foregroundStyle(HeliosColor.textPrimary(for: colorScheme))
                    .contentTransition(.numericText(value: snapshot.ac))

                Text("kW")
                    .font(.system(size: 15, weight: .medium, design: .monospaced))
                    .tracking(0.2)
                    .foregroundStyle(.secondary)
                    .padding(.bottom, 8)
            }

            Text("Live solar production at the moment of share.")
                .font(.system(size: 13))
                .foregroundStyle(.tertiary)
                .padding(.top, 6)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(20)
        .background(.ultraThinMaterial, in: RoundedRectangle(cornerRadius: 20))
        .overlay(
            RoundedRectangle(cornerRadius: 20)
                .stroke(.white.opacity(0.06), lineWidth: 0.5)
        )
        .opacity(heroVisible ? 1 : 0)
        .offset(y: heroVisible ? 0 : 12)
    }

    // MARK: - 2×2 Metrics Grid

    private func metricsGrid(snapshot: SnapshotPayload) -> some View {
        let co2 = snapshot.todayKwh * 0.42

        return LazyVGrid(columns: [
            GridItem(.flexible(), spacing: 10),
            GridItem(.flexible(), spacing: 10)
        ], spacing: 10) {
            // Today kWh
            metricTile(
                icon: "sun.max",
                label: "TODAY",
                value: String(format: "%.1f", snapshot.todayKwh),
                unit: "kWh produced"
            )

            // Battery SoC
            metricTile(
                icon: "battery.75",
                label: "BATTERY",
                value: "\(snapshot.soc)",
                unit: "% state of charge"
            )

            // Self-use
            metricTile(
                icon: "house",
                label: "SELF-USE",
                value: "\(snapshot.selfUse)",
                unit: "% on-site"
            )

            // Lifetime
            metricTile(
                icon: "clock",
                label: "LIFETIME",
                value: String(format: "%.2f", Double(snapshot.lifeKwh) / 1000),
                unit: "MWh · \(String(format: "%.1f", co2)) kg CO₂"
            )
        }
        .opacity(gridVisible ? 1 : 0)
        .offset(y: gridVisible ? 0 : 12)
    }

    private func metricTile(icon: String, label: String, value: String, unit: String) -> some View {
        VStack(alignment: .leading, spacing: 6) {
            HStack(spacing: 4) {
                Image(systemName: icon)
                    .font(.system(size: 10))
                    .foregroundStyle(.secondary)
                Text(label)
                    .font(.system(size: 10, weight: .medium, design: .monospaced))
                    .tracking(0.15)
                    .foregroundStyle(.secondary)
            }

            Text(value)
                .font(.system(size: 28, weight: .light, design: .monospaced))
                .foregroundStyle(HeliosColor.textPrimary(for: colorScheme))

            Text(unit)
                .font(.system(size: 10, weight: .medium, design: .monospaced))
                .tracking(0.15)
                .foregroundStyle(.tertiary)
                .fixedSize(horizontal: false, vertical: true)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(14)
        .background(HeliosColor.backgroundSecondary(for: colorScheme), in: RoundedRectangle(cornerRadius: 14))
        .overlay(
            RoundedRectangle(cornerRadius: 14)
                .stroke(.white.opacity(0.04), lineWidth: 0.5)
        )
    }

    // MARK: - Forecast Section

    private func forecastSection(fc: [Double]) -> some View {
        let labels = ["Tod", "Tom", "+3", "+4", "+5"]
        let max = max(fc.max() ?? 1, 1)

        return VStack(alignment: .leading, spacing: 0) {
            Text("NEXT 5 DAYS · FORECAST")
                .font(.system(size: 10, weight: .medium, design: .monospaced))
                .tracking(0.15)
                .foregroundStyle(.secondary)
                .padding(.bottom, 12)

            HStack(alignment: .top, spacing: 0) {
                ForEach(Array(fc.prefix(5).enumerated()), id: \.offset) { i, kwh in
                    VStack(spacing: 6) {
                        Text(labels[safe: i] ?? "+\(i)")
                            .font(.system(size: 10, weight: .medium, design: .monospaced))
                            .tracking(0.2)
                            .foregroundStyle(.tertiary)

                        Text(String(format: "%.0f", kwh))
                            .font(.system(size: 12, weight: .medium, design: .monospaced))
                            .foregroundStyle(HeliosColor.textPrimary(for: colorScheme))

                        Text("kWh")
                            .font(.system(size: 9, design: .monospaced))
                            .foregroundStyle(.tertiary.opacity(0.6))

                        // Gradient bar
                        GeometryReader { geo in
                            ZStack(alignment: .leading) {
                                RoundedRectangle(cornerRadius: 2)
                                    .fill(HeliosColor.backgroundTertiary(for: colorScheme))
                                    .frame(height: 5)

                                RoundedRectangle(cornerRadius: 2)
                                    .fill(HeliosColor.solar(for: colorScheme, step: 400))
                                    .frame(
                                        width: max(CGFloat(kwh / max) * geo.size.width, 4),
                                        height: 5
                                    )
                            }
                        }
                        .frame(height: 5)
                    }
                    .frame(maxWidth: .infinity)
                }
            }
        }
        .padding(16)
        .background(HeliosColor.backgroundSecondary(for: colorScheme), in: RoundedRectangle(cornerRadius: 14))
        .overlay(
            RoundedRectangle(cornerRadius: 14)
                .stroke(.white.opacity(0.04), lineWidth: 0.5)
        )
        .opacity(forecastVisible ? 1 : 0)
        .offset(y: forecastVisible ? 0 : 12)
    }

    // MARK: - CTA Card

    private func ctaCard(snapshot: SnapshotPayload) -> some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("Want one for your roof?")
                .font(.system(size: 16, weight: .medium))
                .foregroundStyle(HeliosColor.textPrimary(for: colorScheme))

            Text("\(BrandService.HELIOS.name) works with any SunSpec-compatible inverter. Open the demo to see what your system could look like.")
                .font(.system(size: 12))
                .foregroundStyle(.tertiary)
                .lineSpacing(3)
                .fixedSize(horizontal: false, vertical: true)

            HStack(spacing: 8) {
                NavigationLink {
                    LandingView()
                } label: {
                    Label("Try \(BrandService.HELIOS.name)", systemImage: "arrow.up.forward.square")
                        .font(.system(size: 12, weight: .medium))
                        .foregroundStyle(colorScheme == .dark ? .black : .white)
                        .padding(.horizontal, 16)
                        .padding(.vertical, 9)
                        .background(
                            HeliosColor.textPrimary(for: colorScheme),
                            in: Capsule()
                        )
                }

                Spacer()
            }
        }
        .padding(18)
        .background(.ultraThinMaterial, in: RoundedRectangle(cornerRadius: 18))
        .overlay(
            RoundedRectangle(cornerRadius: 18)
                .stroke(.white.opacity(0.06), lineWidth: 0.5)
        )
        .opacity(ctaVisible ? 1 : 0)
        .offset(y: ctaVisible ? 0 : 12)
    }

    // MARK: - Expired / Invalid View

    private var expiredView: some View {
        VStack(spacing: 16) {
            Spacer()

            Text("SNAPSHOT")
                .font(.system(size: 10, weight: .medium, design: .monospaced))
                .tracking(0.2)
                .foregroundStyle(.secondary)

            Text("Link expired or invalid")
                .font(.system(size: 28, weight: .light))
                .foregroundStyle(HeliosColor.textPrimary(for: colorScheme))

            Text("This shared snapshot couldn't be decoded. The link may have been truncated.")
                .font(.system(size: 13))
                .foregroundStyle(.secondary)
                .multilineTextAlignment(.center)
                .fixedSize(horizontal: false, vertical: true)

            NavigationLink {
                LandingView()
            } label: {
                Label("Go to \(BrandService.HELIOS.name)", systemImage: "arrow.right")
                    .font(.system(size: 13, weight: .medium))
                    .foregroundStyle(colorScheme == .dark ? .black : .white)
                    .padding(.horizontal, 18)
                    .padding(.vertical, 10)
                    .background(
                        HeliosColor.textPrimary(for: colorScheme),
                        in: Capsule()
                    )
            }
            .padding(.top, 8)

            Spacer()
        }
        .padding(24)
        .frame(maxWidth: .infinity)
    }

    // MARK: - Footer

    private var footerView: some View {
        Text("helios° · precision energy")
            .font(.system(size: 10, weight: .medium, design: .monospaced))
            .tracking(0.2)
            .foregroundStyle(.tertiary.opacity(0.7))
            .frame(maxWidth: .infinity, alignment: .center)
            .padding(.top, 12)
    }

    // MARK: - Helpers

    private func formattedDateTime(_ snapshot: SnapshotPayload) -> String {
        let date = Date(timeIntervalSince1970: snapshot.ts / 1000)
        let weekday = date.formatted(Date.FormatStyle().weekday(.wide))
        let month = date.formatted(Date.FormatStyle().month(.wide))
        let day = Calendar.current.component(.day, from: date)
        let hour = Calendar.current.component(.hour, from: date)
        let minute = Calendar.current.component(.minute, from: date)

        let ampm = hour >= 12 ? "PM" : "AM"
        let hour12 = hour == 0 ? 12 : (hour > 12 ? hour - 12 : hour)
        let minuteStr = String(format: "%02d", minute)

        return "\(weekday), \(month) \(day) · \(hour12):\(minuteStr) \(ampm)"
    }
}

// MARK: - Preview

#Preview("Shared — Valid") {
    NavigationStack {
        SharedView(encoded: nil)
    }
    .preferredColorScheme(.dark)
}

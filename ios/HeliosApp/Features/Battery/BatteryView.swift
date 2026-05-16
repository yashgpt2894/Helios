import SwiftUI

// MARK: - BatteryView — port of Battery.tsx

struct BatteryView: View {
    @Environment(TelemetryRepository.self) private var telemetryRepo
    @Environment(\.colorScheme) var colorScheme

    var t: SolarTelemetry { telemetryRepo.telemetry }

    private var isCharging: Bool { t.batteryPowerW > 30 }
    private var isDischarging: Bool { t.batteryPowerW < -30 }

    private var statusLabel: String {
        isCharging ? "Charging" : isDischarging ? "Powering home" : "Idle"
    }

    private var remainingKwh: Double {
        (t.batterySoc / 100) * t.batteryCapacityKwh
    }

    private var minutesEstimate: Int? {
        guard isDischarging, abs(t.batteryPowerW) > 50 else { return nil }
        return Int(round((remainingKwh / (abs(t.batteryPowerW) / 1000)) * 60))
    }

    @State private var chargeMode: String = "self"

    var body: some View {
        NavigationStack {
            ScrollView(.vertical, showsIndicators: false) {
                VStack(spacing: 24) {
                    // Header
                    batteryHeader
                        .choreographedEntrance(delay: 0)

                    // BatteryRing section
                    batteryRingSection
                        .choreographedEntrance(delay: 0.05)

                    // Backup readiness
                    backupReadinessSection
                        .choreographedEntrance(delay: 0.12)

                    // Health 2×2 grid
                    healthGrid
                        .choreographedEntrance(delay: 0.18)

                    // Charge mode section
                    chargeModeSection
                        .choreographedEntrance(delay: 0.24)

                    Spacer().frame(height: 40)
                }
                .padding(.horizontal, 16)
                .padding(.top, 8)
            }
            .background(HeliosColor.backgroundPrimary(for: colorScheme))
            .navigationBarHidden(true)
            .onAppear {
                telemetryRepo.tick()
            }
            .task(id: "tick") {
                let timer = Timer.publish(every: 2.0, on: .main, in: .common).autoconnect()
                for await _ in timer.values {
                    telemetryRepo.tick()
                }
            }
        }
    }

    // MARK: - Header

    private var batteryHeader: some View {
        VStack(alignment: .leading, spacing: 4) {
            Text("ENERGY STORAGE")
                .font(.system(size: 10, weight: .medium, design: .monospaced))
                .tracking(0.2)
                .foregroundStyle(.secondary)

            Text("Battery")
                .font(.system(size: 28, weight: .medium))
                .foregroundStyle(HeliosColor.textPrimary(for: colorScheme))

            Text("\(String(format: "%.1f", t.batteryCapacityKwh)) kWh · \(t.batteryCycles) cycles · \(String(format: "%.1f", t.batteryHealthPct))% health")
                .font(.system(size: 13))
                .foregroundStyle(HeliosColor.textTertiary(for: colorScheme))
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(.horizontal, 4)
    }

    // MARK: - Battery Ring Section

    private var batteryRingSection: some View {
        VStack(spacing: 0) {
            BatteryRing(
                soc: t.batterySoc,
                capacityKwh: t.batteryCapacityKwh,
                isCharging: isCharging,
                isDischarging: isDischarging,
                remainingKwh: remainingKwh,
                minutesEstimate: minutesEstimate,
                size: 260
            )

            Divider()
                .foregroundStyle(HeliosColor.separator(for: colorScheme))
                .padding(.vertical, 12)

            // 3-col row: Power / Temp / Cycles
            HStack(spacing: 0) {
                // Power
                VStack(spacing: 4) {
                    Text("POWER")
                        .font(.system(size: 10, weight: .medium, design: .monospaced))
                        .tracking(0.15)
                        .foregroundStyle(.secondary)
                    Text(Format.formatW(t.batteryPowerW))
                        .font(.system(size: 15, weight: .medium, design: .monospaced))
                        .foregroundStyle(HeliosColor.textPrimary(for: colorScheme))
                }
                .frame(maxWidth: .infinity)

                // Temp
                VStack(spacing: 4) {
                    Text("TEMP")
                        .font(.system(size: 10, weight: .medium, design: .monospaced))
                        .tracking(0.15)
                        .foregroundStyle(.secondary)
                    Text("\(String(format: "%.1f", t.batteryTempC))°C")
                        .font(.system(size: 15, weight: .medium, design: .monospaced))
                        .foregroundStyle(HeliosColor.textPrimary(for: colorScheme))
                }
                .frame(maxWidth: .infinity)
                .overlay(
                    Rectangle()
                        .fill(HeliosColor.separator(for: colorScheme))
                        .frame(width: 0.5),
                    alignment: .leading
                )

                // Cycles
                VStack(spacing: 4) {
                    Text("CYCLES")
                        .font(.system(size: 10, weight: .medium, design: .monospaced))
                        .tracking(0.15)
                        .foregroundStyle(.secondary)
                    Text("\(t.batteryCycles)")
                        .font(.system(size: 15, weight: .medium, design: .monospaced))
                        .foregroundStyle(HeliosColor.textPrimary(for: colorScheme))
                }
                .frame(maxWidth: .infinity)
                .overlay(
                    Rectangle()
                        .fill(HeliosColor.separator(for: colorScheme))
                        .frame(width: 0.5),
                    alignment: .leading
                )
            }
        }
        .padding(20)
        .background(.ultraThinMaterial, in: RoundedRectangle(cornerRadius: 20))
        .overlay(
            RoundedRectangle(cornerRadius: 20)
                .stroke(.white.opacity(0.06), lineWidth: 0.5)
        )
    }

    // MARK: - Backup Readiness

    private var backupReadinessSection: some View {
        VStack(alignment: .leading, spacing: 8) {
            SectionHeader(
                title: "Backup readiness",
                eyebrow: "RESERVE"
            )
            Text("Estimated runtime if grid were lost right now.")
                .font(.system(size: 11))
                .foregroundStyle(.tertiary)

            VStack(alignment: .leading, spacing: 12) {
                HStack(alignment: .bottom) {
                    VStack(alignment: .leading, spacing: 4) {
                        let hours = Int(floor(remainingKwh / max(0.4, t.homeLoadW / 1000)))
                        Text("\(hours)")
                            .font(.system(size: 38, weight: .light, design: .monospaced))
                            .foregroundStyle(HeliosColor.textPrimary(for: colorScheme))
                        Text("HOURS OF ESSENTIALS")
                            .font(.system(size: 10, weight: .medium, design: .monospaced))
                            .tracking(0.15)
                            .foregroundStyle(.secondary)
                    }

                    Spacer()

                    VStack(alignment: .trailing, spacing: 2) {
                        Text("\(String(format: "%.1f", remainingKwh)) kWh available")
                            .font(.system(size: 12, design: .monospaced))
                            .foregroundStyle(HeliosColor.textSecondary(for: colorScheme))
                        Text("at \(String(format: "%.2f", t.homeLoadW / 1000)) kW current draw")
                            .font(.system(size: 10, design: .monospaced))
                            .foregroundStyle(.tertiary)
                    }
                }

                // SoC fill bar
                GeometryReader { geo in
                    ZStack(alignment: .leading) {
                        RoundedRectangle(cornerRadius: 3)
                            .fill(HeliosColor.backgroundTertiary(for: colorScheme))
                            .frame(height: 6)

                        RoundedRectangle(cornerRadius: 3)
                            .fill(HeliosColor.battery(for: colorScheme, step: 500))
                            .frame(width: max(CGFloat(t.batterySoc / 100) * geo.size.width, 0), height: 6)
                            .animation(.easeInOut(duration: 0.7), value: t.batterySoc)
                    }
                }
                .frame(height: 6)
            }
            .padding(16)
            .background(HeliosColor.backgroundSecondary(for: colorScheme), in: RoundedRectangle(cornerRadius: 12))
            .overlay(
                RoundedRectangle(cornerRadius: 12)
                    .stroke(.white.opacity(0.04), lineWidth: 0.5)
            )
        }
    }

    // MARK: - Health 2×2 Grid

    private var healthGrid: some View {
        VStack(alignment: .leading, spacing: 8) {
            SectionHeader(
                title: "Health & telemetry",
                eyebrow: "STATUS"
            )

            LazyVGrid(columns: [
                GridItem(.flexible(), spacing: 12),
                GridItem(.flexible(), spacing: 12)
            ], spacing: 12) {
                MetricTile(
                    label: "Health",
                    value: String(format: "%.1f", t.batteryHealthPct),
                    unit: "%",
                    icon: "shield.checkered",
                    color: t.batteryHealthPct > 95
                        ? HeliosColor.flow(for: colorScheme)
                        : HeliosColor.battery(for: colorScheme),
                    detail: t.batteryHealthPct > 95 ? "excellent" : "normal"
                )

                MetricTile(
                    label: "Round-trip",
                    value: "94.2",
                    unit: "%",
                    icon: "gauge.with.dots.needle.33percent",
                    color: HeliosColor.textPrimary(for: colorScheme),
                    detail: "efficiency"
                )

                MetricTile(
                    label: "Cycles",
                    value: "\(t.batteryCycles)",
                    unit: nil,
                    icon: "clock",
                    color: HeliosColor.textPrimary(for: colorScheme),
                    detail: "of 6000 rated"
                )

                MetricTile(
                    label: "Cell temp",
                    value: String(format: "%.1f", t.batteryTempC),
                    unit: "°C",
                    icon: "thermometer.medium",
                    color: HeliosColor.battery(for: colorScheme),
                    detail: "thermal · ok"
                )
            }
        }
    }

    // MARK: - Charge Mode

    private var chargeModeSection: some View {
        VStack(alignment: .leading, spacing: 8) {
            SectionHeader(
                title: "Charge mode",
                eyebrow: "STRATEGY"
            )

            VStack(spacing: 8) {
                chargeModeButton(
                    id: "self",
                    name: "Self-consumption",
                    desc: "Maximize using your own solar before drawing from grid."
                )

                chargeModeButton(
                    id: "tou",
                    name: "Time-of-use",
                    desc: "Charge from grid in off-peak; discharge during peak rates."
                )

                chargeModeButton(
                    id: "backup",
                    name: "Backup-only",
                    desc: "Hold ≥80% reserve at all times for outage protection."
                )
            }
        }
    }

    private func chargeModeButton(id: String, name: String, desc: String) -> some View {
        let isActive = chargeMode == id

        return Button {
            HeliosHaptics.prepare()
            HeliosHaptics.trigger(.toggleOn)
            HeliosSound.play(.togglePositive)
            withAnimation(HeliosMotion.spring(.snappy)) {
                chargeMode = id
            }
        } label: {
            HStack(alignment: .top, spacing: 10) {
                // Radio indicator
                ZStack {
                    Circle()
                        .stroke(isActive ? HeliosColor.battery(for: colorScheme, step: 500) : HeliosColor.textTertiary(for: colorScheme), lineWidth: 2)
                        .frame(width: 14, height: 14)

                    if isActive {
                        Circle()
                            .fill(HeliosColor.battery(for: colorScheme, step: 500))
                            .frame(width: 6, height: 6)
                    }
                }
                .padding(.top, 1)

                VStack(alignment: .leading, spacing: 2) {
                    HStack(spacing: 6) {
                        Text(name)
                            .font(.system(size: 13, weight: .medium))
                            .foregroundStyle(HeliosColor.textPrimary(for: colorScheme))

                        if isActive {
                            Text("ACTIVE")
                                .font(.system(size: 9, weight: .medium, design: .monospaced))
                                .tracking(0.2)
                                .foregroundStyle(HeliosColor.battery(for: colorScheme, step: 500))
                        }
                    }

                    Text(desc)
                        .font(.system(size: 12))
                        .foregroundStyle(HeliosColor.textTertiary(for: colorScheme))
                        .lineSpacing(2)
                        .fixedSize(horizontal: false, vertical: true)
                }

                Spacer()

                Image(systemName: "battery.75")
                    .font(.system(size: 14))
                    .foregroundStyle(.tertiary)
                    .padding(.top, 2)
            }
            .padding(12)
            .frame(maxWidth: .infinity, alignment: .leading)
            .background(
                isActive
                    ? HeliosColor.backgroundTertiary(for: colorScheme).opacity(0.5)
                    : HeliosColor.backgroundSecondary(for: colorScheme).opacity(0.5),
                in: RoundedRectangle(cornerRadius: 12)
            )
            .overlay(
                RoundedRectangle(cornerRadius: 12)
                    .stroke(
                        isActive
                            ? HeliosColor.battery(for: colorScheme, step: 500).opacity(0.2)
                            : HeliosColor.separator(for: colorScheme),
                        lineWidth: isActive ? 1 : 0.5
                    )
            )
        }
        .buttonStyle(.plain)
    }
}

// MARK: - BatteryRing (Canvas-based animated ring)

private struct BatteryRing: View {
    let soc: Double
    let capacityKwh: Double
    let isCharging: Bool
    let isDischarging: Bool
    let remainingKwh: Double
    let minutesEstimate: Int?
    let size: CGFloat
    @Environment(\.colorScheme) var colorScheme

    var body: some View {
        let radius = size / 2 - 18
        let circumference = 2 * Double.pi * radius
        let dash = (soc / 100) * circumference

        let accentColor: Color = {
            if isCharging {
                return HeliosColor.flow(for: colorScheme, step: 300)
            } else if isDischarging {
                return HeliosColor.solar(for: colorScheme, step: 300)
            } else {
                return HeliosColor.textTertiary(for: colorScheme)
            }
        }()

        let status: String = {
            isCharging ? "Charging" : isDischarging ? "Powering home" : "Idle"
        }()

        return ZStack {
            // Background ring
            Circle()
                .stroke(
                    HeliosColor.backgroundTertiary(for: colorScheme),
                    style: StrokeStyle(lineWidth: 2)
                )
                .frame(width: size, height: size)

            // SoC arc
            Circle()
                .trim(from: 0, to: CGFloat(soc / 100))
                .stroke(
                    accentColor,
                    style: StrokeStyle(lineWidth: 3, lineCap: .round)
                )
                .frame(width: size, height: size)
                .rotationEffect(.degrees(-90))
                .animation(.easeOut(duration: 0.8), value: soc)

            // Center content
            VStack(spacing: 2) {
                Text("STATE OF CHARGE")
                    .font(.system(size: 10, weight: .medium, design: .monospaced))
                    .tracking(0.15)
                    .foregroundStyle(.secondary)

                Text("\(Int(round(soc)))")
                    .font(.system(size: 56, weight: .light, design: .monospaced))
                    .foregroundStyle(HeliosColor.textPrimary(for: colorScheme))
                    .contentTransition(.numericText(value: soc))

                Text(status.uppercased())
                    .font(.system(size: 10, weight: .medium, design: .monospaced))
                    .tracking(0.15)
                    .foregroundStyle(accentColor)

                // kWh badge
                Text("\(String(format: "%.1f", remainingKwh)) / \(String(format: "%.1f", capacityKwh)) kWh")
                    .font(.system(size: 11, design: .monospaced))
                    .foregroundStyle(.secondary)
                    .padding(.horizontal, 10)
                    .padding(.vertical, 4)
                    .background(
                        Capsule()
                            .stroke(HeliosColor.separator(for: colorScheme), lineWidth: 0.5)
                    )
                    .padding(.top, 6)

                // Minutes estimate
                if let minutes = minutesEstimate {
                    Text("~\(minutes / 60)h \(minutes % 60)m at current draw")
                        .font(.system(size: 10, design: .monospaced))
                        .foregroundStyle(.tertiary)
                        .padding(.top, 4)
                }
            }
        }
        .frame(width: size, height: size)
    }
}

// MARK: - Preview

#Preview("Battery") {
    BatteryView()
        .environment(TelemetryRepository())
        .preferredColorScheme(.dark)
}

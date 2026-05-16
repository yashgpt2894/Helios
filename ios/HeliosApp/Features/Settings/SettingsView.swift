import SwiftUI

// MARK: - SettingsView — port of Settings.tsx

struct SettingsView: View {
    @Environment(ThemeService.self) private var themeService
    @Environment(TelemetryRepository.self) private var telemetryRepo
    @Environment(\.colorScheme) var colorScheme

    @State private var host: String = "192.168.1.42"
    @State private var port: String = "502"
    @State private var unitId: String = "1"
    @State private var pollMs: String = "2000"
    @State private var connectionStatus: String = "simulated"

    var t: SolarTelemetry { telemetryRepo.telemetry }
    var brand: BrandInfo { BrandService.HELIOS }

    var body: some View {
        NavigationStack {
            ScrollView(.vertical, showsIndicators: false) {
                VStack(spacing: 24) {
                    // Header
                    settingsHeader
                        .choreographedEntrance(delay: 0)

                    // Inverter info card
                    inverterInfoCard
                        .choreographedEntrance(delay: 0.05)

                    // SunSpec Modbus section
                    sunspecSection
                        .choreographedEntrance(delay: 0.1)

                    // Forecast location card
                    locationCard
                        .choreographedEntrance(delay: 0.15)

                    // Theme picker
                    themePicker
                        .choreographedEntrance(delay: 0.2)

                    // Preference rows
                    preferencesSection
                        .choreographedEntrance(delay: 0.25)

                    // Footer
                    footerView
                        .choreographedEntrance(delay: 0.3)

                    Spacer().frame(height: 40)
                }
                .padding(.horizontal, 16)
                .padding(.top, 8)
            }
            .background(HeliosColor.backgroundPrimary(for: colorScheme))
            .navigationBarHidden(true)
        }
    }

    // MARK: - Header

    private var settingsHeader: some View {
        VStack(alignment: .leading, spacing: 4) {
            Text("SYSTEM")
                .font(.system(size: 10, weight: .medium, design: .monospaced))
                .tracking(0.2)
                .foregroundStyle(.secondary)

            Text("Settings")
                .font(.system(size: 28, weight: .medium))
                .foregroundStyle(HeliosColor.textPrimary(for: colorScheme))
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(.horizontal, 4)
    }

    // MARK: - Inverter Info Card

    private var inverterInfoCard: some View {
        HStack(spacing: 14) {
            HeliosMark(size: 56)

            VStack(alignment: .leading, spacing: 4) {
                Text(t.model)
                    .font(.system(size: 14, weight: .medium))
                    .foregroundStyle(HeliosColor.textPrimary(for: colorScheme))

                Text("S/N \(t.serialNumber) · fw \(t.firmware)")
                    .font(.system(size: 11, design: .monospaced))
                    .foregroundStyle(HeliosColor.textTertiary(for: colorScheme))
            }

            Spacer()
        }
        .padding(16)
        .background(.ultraThinMaterial, in: RoundedRectangle(cornerRadius: 16))
        .overlay(
            RoundedRectangle(cornerRadius: 16)
                .stroke(.white.opacity(0.06), lineWidth: 0.5)
        )
    }

    // MARK: - SunSpec Modbus Section

    private var sunspecSection: some View {
        VStack(alignment: .leading, spacing: 8) {
            SectionHeader(
                title: "SunSpec Modbus",
                eyebrow: "CONNECTION"
            )
            Text("MVP supports SunSpec — the industry-standard protocol for inverters.")
                .font(.system(size: 11))
                .foregroundStyle(.tertiary)

            VStack(spacing: 14) {
                // Connection status row
                HStack {
                    HStack(spacing: 10) {
                        ZStack {
                            Circle()
                                .fill(HeliosColor.backgroundTertiary(for: colorScheme))
                                .frame(width: 32, height: 32)
                            Image(systemName: "wifi")
                                .font(.system(size: 14))
                                .foregroundStyle(.secondary)
                        }

                        VStack(alignment: .leading, spacing: 2) {
                            Text(connectionStatus == "simulated" ? "Simulation mode" : "Live link")
                                .font(.system(size: 13, weight: .medium))
                                .foregroundStyle(HeliosColor.textPrimary(for: colorScheme))
                            Text("\(host):\(port) · unit \(unitId)")
                                .font(.system(size: 11, design: .monospaced))
                                .foregroundStyle(HeliosColor.textTertiary(for: colorScheme))
                        }
                    }

                    Spacer()

                    Text(connectionStatus.uppercased())
                        .font(.system(size: 10, weight: .medium, design: .monospaced))
                        .tracking(0.2)
                        .foregroundStyle(
                            connectionStatus == "connected"
                                ? HeliosColor.flow(for: colorScheme)
                                : connectionStatus == "simulated"
                                ? HeliosColor.solar(for: colorScheme, step: 400)
                                : HeliosColor.alert(for: colorScheme)
                        )
                }

                Divider().foregroundStyle(HeliosColor.separator(for: colorScheme))

                // Connection fields grid
                LazyVGrid(columns: [
                    GridItem(.flexible(), spacing: 10),
                    GridItem(.flexible(), spacing: 10)
                ], spacing: 10) {
                    settingField(label: "Host", value: $host)
                    settingField(label: "Port", value: $port)
                    settingField(label: "Unit ID", value: $unitId)
                    settingField(label: "Poll (ms)", value: $pollMs)
                }

                // Toggle button
                Button {
                    HeliosHaptics.prepare()
                    HeliosHaptics.trigger(.tapButton)
                    withAnimation(HeliosMotion.spring(.snappy)) {
                        connectionStatus = connectionStatus == "connected" ? "simulated" : "connected"
                    }
                } label: {
                    Label(
                        connectionStatus == "connected"
                            ? "Switch to simulation"
                            : "Test connection",
                        systemImage: "arrow.triangle.2.circlepath"
                    )
                    .font(.system(size: 13, weight: .medium))
                    .foregroundStyle(HeliosColor.textPrimary(for: colorScheme))
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 10)
                    .background(HeliosColor.backgroundSecondary(for: colorScheme), in: RoundedRectangle(cornerRadius: 10))
                    .overlay(
                        RoundedRectangle(cornerRadius: 10)
                            .stroke(HeliosColor.separator(for: colorScheme), lineWidth: 0.5)
                    )
                }
                .buttonStyle(.plain)
            }
            .padding(16)
            .background(HeliosColor.backgroundSecondary(for: colorScheme), in: RoundedRectangle(cornerRadius: 12))
            .overlay(
                RoundedRectangle(cornerRadius: 12)
                    .stroke(.white.opacity(0.04), lineWidth: 0.5)
            )
        }
    }

    private func settingField(label: String, value: Binding<String>) -> some View {
        VStack(alignment: .leading, spacing: 6) {
            Text(label.uppercased())
                .font(.system(size: 10, weight: .medium, design: .monospaced))
                .tracking(0.15)
                .foregroundStyle(.secondary)

            TextField(label, text: value)
                .font(.system(size: 13, design: .monospaced))
                .foregroundStyle(HeliosColor.textPrimary(for: colorScheme))
                .padding(.horizontal, 10)
                .padding(.vertical, 8)
                .background(HeliosColor.backgroundTertiary(for: colorScheme), in: RoundedRectangle(cornerRadius: 8))
                .overlay(
                    RoundedRectangle(cornerRadius: 8)
                        .stroke(HeliosColor.separator(for: colorScheme), lineWidth: 0.5)
                )
        }
    }

    // MARK: - Forecast Location Card

    private var locationCard: some View {
        VStack(alignment: .leading, spacing: 8) {
            SectionHeader(
                title: "Forecast location",
                eyebrow: "LOCATION"
            )
            Text("Used only to query Open-Meteo for solar irradiance. Coords stay on-device otherwise.")
                .font(.system(size: 11))
                .foregroundStyle(.tertiary)

            HStack(spacing: 10) {
                ZStack {
                    Circle()
                        .fill(HeliosColor.backgroundTertiary(for: colorScheme))
                        .frame(width: 32, height: 32)
                    Image(systemName: "mappin")
                        .font(.system(size: 14))
                        .foregroundStyle(.secondary)
                }

                VStack(alignment: .leading, spacing: 2) {
                    Text("San Francisco, CA")
                        .font(.system(size: 13, weight: .medium))
                        .foregroundStyle(HeliosColor.textPrimary(for: colorScheme))
                    Text("37.7749, -122.4194 · default")
                        .font(.system(size: 11, design: .monospaced))
                        .foregroundStyle(HeliosColor.textTertiary(for: colorScheme))
                }

                Spacer()

                Button {
                    HeliosHaptics.prepare()
                    HeliosHaptics.trigger(.tapLight)
                } label: {
                    Text("USE MINE")
                        .font(.system(size: 11, weight: .medium, design: .monospaced))
                        .tracking(0.2)
                        .foregroundStyle(.secondary)
                        .padding(.horizontal, 12)
                        .padding(.vertical, 6)
                        .background(
                            Capsule()
                                .stroke(HeliosColor.separator(for: colorScheme), lineWidth: 0.5)
                        )
                }
                .buttonStyle(.plain)
            }
            .padding(16)
            .background(HeliosColor.backgroundSecondary(for: colorScheme), in: RoundedRectangle(cornerRadius: 12))
            .overlay(
                RoundedRectangle(cornerRadius: 12)
                    .stroke(.white.opacity(0.04), lineWidth: 0.5)
            )
        }
    }

    // MARK: - Theme Picker

    private var themePicker: some View {
        VStack(alignment: .leading, spacing: 8) {
            SectionHeader(
                title: "Theme",
                eyebrow: "APPEARANCE"
            )
            Text("Choose carbon, paper, or follow your system.")
                .font(.system(size: 11))
                .foregroundStyle(.tertiary)

            HStack(spacing: 8) {
                themeOption(
                    id: "dark",
                    label: "Carbon",
                    sub: "dark",
                    icon: "moon.fill",
                    isActive: themeService.theme == .dark
                ) {
                    HeliosHaptics.prepare()
                    HeliosHaptics.trigger(.toggleOn)
                    themeService.theme = .dark
                }

                themeOption(
                    id: "light",
                    label: "Paper",
                    sub: "light",
                    icon: "sun.max.fill",
                    isActive: themeService.theme == .light
                ) {
                    HeliosHaptics.prepare()
                    HeliosHaptics.trigger(.toggleOn)
                    themeService.theme = .light
                }

                themeOption(
                    id: "auto",
                    label: "Auto",
                    sub: "system",
                    icon: "gearshape",
                    isActive: themeService.theme == .auto
                ) {
                    HeliosHaptics.prepare()
                    HeliosHaptics.trigger(.toggleOn)
                    themeService.theme = .auto
                }
            }
        }
    }

    private func themeOption(
        id: String,
        label: String,
        sub: String,
        icon: String,
        isActive: Bool,
        action: @escaping () -> Void
    ) -> some View {
        Button(action: action) {
            VStack(spacing: 6) {
                Image(systemName: icon)
                    .font(.system(size: 18))
                    .foregroundStyle(isActive ? HeliosColor.textPrimary(for: colorScheme) : .secondary)

                Text(label)
                    .font(.system(size: 12, weight: .medium))
                    .foregroundStyle(isActive ? HeliosColor.textPrimary(for: colorScheme) : .secondary)

                Text(sub.uppercased())
                    .font(.system(size: 9, weight: .medium, design: .monospaced))
                    .tracking(0.2)
                    .foregroundStyle(.tertiary)
            }
            .frame(maxWidth: .infinity)
            .padding(.vertical, 12)
            .background(
                isActive
                    ? HeliosColor.backgroundTertiary(for: colorScheme).opacity(0.6)
                    : Color.clear,
                in: RoundedRectangle(cornerRadius: 12)
            )
            .overlay(
                RoundedRectangle(cornerRadius: 12)
                    .stroke(
                        isActive
                            ? HeliosColor.separator(for: colorScheme).opacity(0.4)
                            : Color.clear,
                        lineWidth: 1
                    )
            )
        }
        .buttonStyle(.plain)
    }

    // MARK: - Preferences Section

    private var preferencesSection: some View {
        VStack(alignment: .leading, spacing: 8) {
            SectionHeader(
                title: "App",
                eyebrow: "PREFERENCES"
            )

            VStack(spacing: 0) {
                preferenceRow(
                    icon: "bell",
                    label: "Notifications",
                    sub: "Anomalies & weekly digest"
                )

                Divider()
                    .foregroundStyle(HeliosColor.separator(for: colorScheme))
                    .padding(.leading, 56)

                preferenceRow(
                    icon: "cpu",
                    label: "Data residency",
                    sub: "Local-only · no cloud",
                    trailing: "On"
                )

                Divider()
                    .foregroundStyle(HeliosColor.separator(for: colorScheme))
                    .padding(.leading, 56)

                preferenceRow(
                    icon: "lock",
                    label: "App lock",
                    sub: "Face ID on launch"
                )

                Divider()
                    .foregroundStyle(HeliosColor.separator(for: colorScheme))
                    .padding(.leading, 56)

                preferenceRow(
                    icon: "info.circle",
                    label: "About helios°",
                    sub: "MVP v0.1"
                )
            }
            .background(HeliosColor.backgroundSecondary(for: colorScheme), in: RoundedRectangle(cornerRadius: 12))
            .overlay(
                RoundedRectangle(cornerRadius: 12)
                    .stroke(.white.opacity(0.04), lineWidth: 0.5)
            )
        }
    }

    private func preferenceRow(
        icon: String,
        label: String,
        sub: String? = nil,
        trailing: String? = nil
    ) -> some View {
        Button {
            HeliosHaptics.prepare()
            HeliosHaptics.trigger(.tapLight)
        } label: {
            HStack(spacing: 12) {
                ZStack {
                    Circle()
                        .fill(HeliosColor.backgroundTertiary(for: colorScheme))
                        .frame(width: 30, height: 30)
                    Image(systemName: icon)
                        .font(.system(size: 13))
                        .foregroundStyle(.secondary)
                }

                VStack(alignment: .leading, spacing: 2) {
                    Text(label)
                        .font(.system(size: 13, weight: .medium))
                        .foregroundStyle(HeliosColor.textPrimary(for: colorScheme))

                    if let sub = sub {
                        Text(sub)
                            .font(.system(size: 11))
                            .foregroundStyle(HeliosColor.textTertiary(for: colorScheme))
                    }
                }

                Spacer()

                if let trailing = trailing {
                    Text(trailing)
                        .font(.system(size: 11, design: .monospaced))
                        .foregroundStyle(.secondary)
                }

                Image(systemName: "chevron.right")
                    .font(.system(size: 12, weight: .semibold))
                    .foregroundStyle(.tertiary)
            }
            .padding(.horizontal, 14)
            .padding(.vertical, 12)
        }
        .buttonStyle(.plain)
    }

    // MARK: - Footer

    private var footerView: some View {
        Text("helios° · precision energy intelligence")
            .font(.system(size: 10, weight: .medium, design: .monospaced))
            .tracking(0.2)
            .foregroundStyle(.tertiary.opacity(0.7))
            .frame(maxWidth: .infinity, alignment: .center)
            .padding(.top, 8)
    }
}

// MARK: - Preview

#Preview("Settings") {
    SettingsView()
        .environment(ThemeService())
        .environment(TelemetryRepository())
        .preferredColorScheme(.dark)
}

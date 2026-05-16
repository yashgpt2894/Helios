import SwiftUI

// MARK: - HeliosColor — P3 color ramps from design-tokens.json

enum HeliosColor {
    // MARK: - Solar (warm, production)

    struct SolarRamp {
        static subscript(index: Int) -> Color {
            switch index {
            case 50:  return Color(red: 1.0, green: 0.976, blue: 0.902)
            case 100: return Color(red: 1.0, green: 0.937, blue: 0.761)
            case 200: return Color(red: 1.0, green: 0.878, blue: 0.580)
            case 300: return Color(red: 0.941, green: 0.776, blue: 0.455)
            case 400: return Color(red: 0.831, green: 0.659, blue: 0.263)
            case 500: return Color(red: 0.722, green: 0.541, blue: 0.180)
            case 600: return Color(red: 0.604, green: 0.431, blue: 0.122)
            case 700: return Color(red: 0.486, green: 0.333, blue: 0.094)
            case 800: return Color(red: 0.369, green: 0.247, blue: 0.071)
            case 900: return Color(red: 0.259, green: 0.173, blue: 0.047)
            case 950: return Color(red: 0.165, green: 0.106, blue: 0.024)
            default: return .clear
            }
        }
    }

    // MARK: - Flow (healthy energy, self-consumption)

    struct FlowRamp {
        static subscript(index: Int) -> Color {
            switch index {
            case 50:  return Color(red: 0.910, green: 0.961, blue: 0.914)
            case 100: return Color(red: 0.784, green: 0.902, blue: 0.788)
            case 200: return Color(red: 0.647, green: 0.839, blue: 0.655)
            case 300: return Color(red: 0.498, green: 0.690, blue: 0.412)
            case 400: return Color(red: 0.369, green: 0.604, blue: 0.306)
            case 500: return Color(red: 0.290, green: 0.498, blue: 0.235)
            case 600: return Color(red: 0.227, green: 0.400, blue: 0.188)
            case 700: return Color(red: 0.169, green: 0.302, blue: 0.141)
            case 800: return Color(red: 0.118, green: 0.220, blue: 0.098)
            case 900: return Color(red: 0.075, green: 0.145, blue: 0.063)
            case 950: return Color(red: 0.039, green: 0.082, blue: 0.031)
            default: return .clear
            }
        }
    }

    // MARK: - Grid Import (orange, attention)

    struct GridImportRamp {
        static subscript(index: Int) -> Color {
            switch index {
            case 50:  return Color(red: 1.0, green: 0.953, blue: 0.878)
            case 100: return Color(red: 1.0, green: 0.878, blue: 0.698)
            case 200: return Color(red: 1.0, green: 0.800, blue: 0.502)
            case 300: return Color(red: 1.0, green: 0.718, blue: 0.302)
            case 400: return Color(red: 1.0, green: 0.655, blue: 0.149)
            case 500: return Color(red: 0.961, green: 0.486, blue: 0.0)
            case 600: return Color(red: 0.902, green: 0.318, blue: 0.0)
            case 700: return Color(red: 0.749, green: 0.212, blue: 0.047)
            case 800: return Color(red: 0.553, green: 0.157, blue: 0.031)
            case 900: return Color(red: 0.361, green: 0.102, blue: 0.020)
            case 950: return Color(red: 0.220, green: 0.059, blue: 0.012)
            default: return .clear
            }
        }
    }

    // MARK: - Grid Export (blue, abundance)

    struct GridExportRamp {
        static subscript(index: Int) -> Color {
            switch index {
            case 50:  return Color(red: 0.890, green: 0.949, blue: 0.992)
            case 100: return Color(red: 0.733, green: 0.871, blue: 0.984)
            case 200: return Color(red: 0.565, green: 0.792, blue: 0.976)
            case 300: return Color(red: 0.365, green: 0.541, blue: 0.659)
            case 400: return Color(red: 0.290, green: 0.478, blue: 0.620)
            case 500: return Color(red: 0.239, green: 0.396, blue: 0.518)
            case 600: return Color(red: 0.184, green: 0.310, blue: 0.416)
            case 700: return Color(red: 0.133, green: 0.231, blue: 0.314)
            case 800: return Color(red: 0.090, green: 0.165, blue: 0.220)
            case 900: return Color(red: 0.051, green: 0.102, blue: 0.133)
            case 950: return Color(red: 0.024, green: 0.055, blue: 0.075)
            default: return .clear
            }
        }
    }

    // MARK: - Battery (SoC)

    struct BatteryRamp {
        static subscript(index: Int) -> Color {
            switch index {
            case 50:  return Color(red: 0.961, green: 0.941, blue: 0.902)
            case 100: return Color(red: 0.902, green: 0.863, blue: 0.784)
            case 200: return Color(red: 0.831, green: 0.773, blue: 0.635)
            case 300: return Color(red: 0.773, green: 0.647, blue: 0.447)
            case 400: return Color(red: 0.651, green: 0.541, blue: 0.322)
            case 500: return Color(red: 0.522, green: 0.420, blue: 0.196)
            case 600: return Color(red: 0.420, green: 0.329, blue: 0.149)
            case 700: return Color(red: 0.322, green: 0.247, blue: 0.110)
            case 800: return Color(red: 0.227, green: 0.176, blue: 0.078)
            case 900: return Color(red: 0.145, green: 0.114, blue: 0.051)
            case 950: return Color(red: 0.082, green: 0.063, blue: 0.024)
            default: return .clear
            }
        }
    }

    // MARK: - Alert (warm coral)

    struct AlertRamp {
        static subscript(index: Int) -> Color {
            switch index {
            case 50:  return Color(red: 0.984, green: 0.918, blue: 0.894)
            case 100: return Color(red: 0.961, green: 0.804, blue: 0.749)
            case 200: return Color(red: 0.929, green: 0.671, blue: 0.580)
            case 300: return Color(red: 0.851, green: 0.467, blue: 0.341)
            case 400: return Color(red: 0.769, green: 0.369, blue: 0.239)
            case 500: return Color(red: 0.659, green: 0.286, blue: 0.165)
            case 600: return Color(red: 0.541, green: 0.220, blue: 0.122)
            case 700: return Color(red: 0.427, green: 0.165, blue: 0.086)
            case 800: return Color(red: 0.314, green: 0.118, blue: 0.063)
            case 900: return Color(red: 0.212, green: 0.078, blue: 0.043)
            case 950: return Color(red: 0.122, green: 0.039, blue: 0.024)
            default: return .clear
            }
        }
    }

    // MARK: - Neutral (warm grayscale)

    struct NeutralRamp {
        static subscript(index: Int) -> Color {
            switch index {
            case 50:  return Color(red: 0.973, green: 0.965, blue: 0.941)
            case 100: return Color(red: 0.957, green: 0.945, blue: 0.918)
            case 200: return Color(red: 0.937, green: 0.925, blue: 0.898)
            case 300: return Color(red: 0.910, green: 0.894, blue: 0.859)
            case 400: return Color(red: 0.863, green: 0.839, blue: 0.784)
            case 500: return Color(red: 0.741, green: 0.714, blue: 0.651)
            case 600: return Color(red: 0.647, green: 0.624, blue: 0.565)
            case 700: return Color(red: 0.478, green: 0.459, blue: 0.408)
            case 800: return Color(red: 0.329, green: 0.310, blue: 0.278)
            case 900: return Color(red: 0.165, green: 0.165, blue: 0.176)
            case 950: return Color(red: 0.043, green: 0.043, blue: 0.047)
            default: return .clear
            }
        }
    }

    // MARK: - Dark mode ramps (reversed from design tokens)

    struct DarkNeutralRamp {
        static subscript(index: Int) -> Color {
            switch index {
            case 50:  return Color(red: 0.027, green: 0.027, blue: 0.031)
            case 100: return Color(red: 0.043, green: 0.043, blue: 0.047)
            case 200: return Color(red: 0.059, green: 0.059, blue: 0.063)
            case 300: return Color(red: 0.078, green: 0.078, blue: 0.086)
            case 400: return Color(red: 0.102, green: 0.102, blue: 0.110)
            case 500: return Color(red: 0.110, green: 0.110, blue: 0.118)
            case 600: return Color(red: 0.165, green: 0.165, blue: 0.176)
            case 700: return Color(red: 0.227, green: 0.227, blue: 0.243)
            case 800: return Color(red: 0.329, green: 0.310, blue: 0.278)
            case 900: return Color(red: 0.647, green: 0.624, blue: 0.565)
            case 950: return Color(red: 0.957, green: 0.945, blue: 0.918)
            default: return .clear
            }
        }
    }

    // MARK: - Semantic Colors

    static func backgroundPrimary(for scheme: ColorScheme) -> Color {
        scheme == .dark ? DarkNeutralRamp[50] : NeutralRamp[50]
    }

    static func backgroundSecondary(for scheme: ColorScheme) -> Color {
        scheme == .dark ? DarkNeutralRamp[100] : NeutralRamp[100]
    }

    static func backgroundTertiary(for scheme: ColorScheme) -> Color {
        scheme == .dark ? DarkNeutralRamp[200] : NeutralRamp[200]
    }

    static func textPrimary(for scheme: ColorScheme) -> Color {
        scheme == .dark ? DarkNeutralRamp[950] : NeutralRamp[950]
    }

    static func textSecondary(for scheme: ColorScheme) -> Color {
        scheme == .dark ? DarkNeutralRamp[900] : NeutralRamp[800]
    }

    static func textTertiary(for scheme: ColorScheme) -> Color {
        scheme == .dark ? DarkNeutralRamp[800] : NeutralRamp[700]
    }

    static func separator(for scheme: ColorScheme) -> Color {
        NeutralRamp[950].opacity(0.06)
    }

    static func solar(for scheme: ColorScheme, step: Int = 500) -> Color {
        scheme == .dark ? SolarRamp[1000 - step] : SolarRamp[step]
    }

    static func flow(for scheme: ColorScheme, step: Int = 500) -> Color {
        scheme == .dark ? FlowRamp[1000 - step] : FlowRamp[step]
    }

    static func battery(for scheme: ColorScheme, step: Int = 500) -> Color {
        scheme == .dark ? BatteryRamp[1000 - step] : BatteryRamp[step]
    }

    static func gridImport(for scheme: ColorScheme, step: Int = 500) -> Color {
        scheme == .dark ? GridImportRamp[1000 - step] : GridImportRamp[step]
    }

    static func gridExport(for scheme: ColorScheme, step: Int = 500) -> Color {
        scheme == .dark ? GridExportRamp[1000 - step] : GridExportRamp[step]
    }

    static func alert(for scheme: ColorScheme, step: Int = 500) -> Color {
        scheme == .dark ? AlertRamp[1000 - step] : AlertRamp[step]
    }
}

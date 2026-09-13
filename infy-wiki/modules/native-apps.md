# Native Apps & Platform Specs
**Summary:** Outline the deferred native Swift iOS and Kotlin Android codebases, widgets, watch apps, and release specs.

## How it works
Helios provides deferred native codebases for both iOS and Android to achieve full feature parity with the Progressive Web App (PWA). The iOS application entry point is defined in `ios/HeliosApp/HeliosApp.swift`, hosting a SwiftUI `TabView` shell with persistence managed via SwiftData models in `ios/HeliosApp/Core/Storage/Models.swift`. The Android application uses Jetpack Compose and Kotlin, with configuration and dependencies specified in `android/app/build.gradle.kts`.

Cross-platform parity across routes, components, state management, services, and data contracts is defined in `shared-spec/feature-parity-matrix.md`. Pre-submission app store release requirements, icon specifications, screenshot checklists, privacy disclosures, and signing rules for both platforms are detailed in `release/build-notes/release-checklist.md`.

## Key files
| File | Role |
|------|------|
| `ios/HeliosApp/HeliosApp.swift` | SwiftUI app entry point, `ContentView`, and `TabView` shell |
| `ios/HeliosApp/Core/Storage/Models.swift` | SwiftData models (`ConnectionConfig`, `ThemePreference`, `BrandPreference`) and shared enums/data structures |
| `android/app/build.gradle.kts` | Android build configuration, compile/target SDK settings, and dependency declarations |
| `shared-spec/feature-parity-matrix.md` | Feature-parity mapping table across PWA, iOS, and Android |
| `release/build-notes/release-checklist.md` | Pre-submission release checklist for iOS App Store and Google Play |

## Gotchas
None found in the supplied evidence.

## Sources

- [android/app/build.gradle.kts](infy://wiki/40d98eee-a3a7-4272-ba0a-285793586ec1/pages/59f21a5c-4809-47ed-b20d-f3fd13ec58f4) · SHA-256 8956a6f2de14194021543a96d84ebf498bc83bf93297d83a1e291a23c4d6e5ac
- [ios/HeliosApp/HeliosApp.swift](infy://wiki/40d98eee-a3a7-4272-ba0a-285793586ec1/pages/80b526b4-97bf-4f32-a608-25bef36366f8) · SHA-256 7c3b98611e18f7dd1c72b8b1b9748b33e27c0e743b670d480ee568f87468cf09
- [ios/HeliosApp/Core/Storage/Models.swift](infy://wiki/40d98eee-a3a7-4272-ba0a-285793586ec1/pages/13c8e35b-6140-479c-a596-317f7aeab1b6) · SHA-256 a0ee6b2325b4bab8bede0c6fee267d4c7ebd94b5387bc3b53e96666f023c0738
- [shared-spec/feature-parity-matrix.md](infy://wiki/40d98eee-a3a7-4272-ba0a-285793586ec1/pages/539b05f0-8729-4389-aa63-7eddeafb3bec) · SHA-256 2d648416fb4652388b916db6bfc0c147930a89eec5e7e65a62153a8842439346
- [release/build-notes/release-checklist.md](infy://wiki/40d98eee-a3a7-4272-ba0a-285793586ec1/pages/9c74e7a2-af4d-421c-b519-ae4df63d68d1) · SHA-256 21c94569687a7bce9f7847dbd96e656b7aa0b3afc1d620bc97de27abcd890a95
# Mission: Helios Store Launch

status: pending
owner: yashgupta
created: 2026-05-16
predecessor: `.factory/missions/helios-native-apps.md` (complete)

## 0. Context

The Helios native apps mission delivered 175 source/spec/release files:
- 52 Swift files (main iOS app + WidgetKit/LiveActivity/Watch/Intents/Control)
- 55 Kotlin files (main Android app + Glance widgets/Wear OS/QS tile/App Actions)
- 14 screen mockups, 5 design specs, 13 release-prep documents
- iOS Localizable.strings + Android strings.xml in 6 languages (1 base + 5 stubs)

All code is feature-complete against the Helios PWA. Cross-platform share-link format (SnapshotPayload v1, base64url) is identical across web / iOS / Android.

What remains is the gap between **"code is written"** and **"app is live in both stores"**. That gap is mostly human action that requires credentials, devices, and store accounts the droid cannot access.

## 1. Goal

Ship Helios to the App Store and Google Play Store on the open trunk version `1.0.0` (iOS build 1, Android versionCode 1). Pass review on first submission. Run a controlled rollout: TestFlight internal → TestFlight external → App Store; Play Internal → Closed → Production.

## 2. Decomposition

| # | Milestone | Depends on | Type | Acceptance |
|---|-----------|------------|------|------------|
| L0 | Developer-account prerequisites | — | human | Apple Developer + Google Play accounts active, signing certs generated |
| L1 | Xcode target wiring | L0 | mixed | All 5 iOS extension targets compile in Xcode 16; App Group capability configured |
| L2 | Android module wiring | L0 | mixed | `wear` module included in `settings.gradle.kts`, Gradle sync succeeds, all variants build |
| L3 | Asset generation | L0 | mixed | App icon PNGs at all required sizes; marketing screenshots captured from running apps |
| L4 | Accessibility integration | L1, L2 | builder-droid | VoiceOver/TalkBack labels wired into every interactive view |
| L5 | Translation pass | L1, L2 | human or translator-service | 21 remaining language stubs replaced with professional translations |
| L6 | Device QA | L1, L2, L3, L4 | human | Physical-device test pass on iPhone 15 Pro, iPhone SE, iPad Pro, Pixel 8, Pixel Fold, Galaxy Tab |
| L7 | TestFlight rollout | L6 | mixed | Internal build accepted by App Store Connect, distributed to internal group |
| L8 | Play Internal Testing rollout | L6 | mixed | Closed AAB uploaded to Internal track, 5+ testers added |
| L9 | Store submission | L7, L8 | human | App Store and Play Store apps accepted by review teams |
| L10 | Production launch | L9 | human | Rollout 100% on both stores; smoke-test in-store install on real device |

## 3. Detailed sub-step ledger

### L0 — Developer-account prerequisites (human-only)
- [ ] Apple Developer Program enrollment ($99/year) on `yashgpt2894@gmail.com` Apple ID.
- [ ] Create App Store Connect app record: name `helios° — solar intelligence`, primary language `English (U.S.)`, bundle ID `com.helios.app`, SKU `helios-ios-001`, user access group default.
- [ ] Generate iOS Distribution certificate in Xcode (`Manage Certificates → Apple Distribution`).
- [ ] Generate App Store provisioning profile bound to `com.helios.app` + all extension bundle IDs.
- [ ] Generate App Group identifier `group.com.helios.app` and bind to main app + WidgetKit + LiveActivity + Watch + Intents + Control targets.
- [ ] Google Play Console account ($25 one-time, ALREADY one-time fee).
- [ ] Create Play Console app record: name `helios — solar intelligence`, default language `English – United States`, app type `App`, free.
- [ ] Generate upload keystore via `keytool -genkey -v -keystore helios-release.jks -keyalg RSA -keysize 2048 -validity 10000 -alias helios`. Store password in 1Password / Bitwarden / hardware key.
- [ ] Enable Google Play App Signing (let Google manage the signing key).
- [ ] Register 12 internal-test device UDIDs in Apple Developer portal.
- [ ] Add 5+ tester email addresses to Play Console Internal Testing.

### L1 — Xcode target wiring (mixed)
- [ ] Open `/ios/Helios.xcodeproj` in Xcode 16.
- [ ] Add **Widget Extension** target named `HeliosWidgets`. Move `/ios/HeliosApp/Widgets/*.swift` and `WidgetAssets.xcassets` into it.
- [ ] Add **Activity Extension** target (Live Activity). Move `/ios/HeliosApp/LiveActivities/*.swift` into it. Add `NSSupportsLiveActivities = YES` to main app `Info.plist`.
- [ ] Add **Watch App** target. Move `/ios/WatchApp/*.swift` into it. Set deployment target to watchOS 10.
- [ ] Add **App Intents Extension** target. Move `/ios/HeliosApp/Intents/*.swift` and `AppShortcuts.xcstrings` into it.
- [ ] Add **Control Widget Extension** target. Move `/ios/HeliosApp/ControlCenter/HeliosControl.swift` into it.
- [ ] Configure App Group capability on main app + all 5 extensions: `group.com.helios.app`.
- [ ] Set Bundle IDs:
  - Main: `com.helios.app`
  - Widgets: `com.helios.app.widgets`
  - LiveActivity: `com.helios.app.liveactivity`
  - Watch: `com.helios.app.watch`
  - Intents: `com.helios.app.intents`
  - Control: `com.helios.app.control`
- [ ] Verify `xcodebuild -workspace Helios.xcworkspace -scheme Helios archive` succeeds.
- [ ] Resolve any Swift compile errors (likely missing `import` statements, target-membership flags, or extension-only API usage).

### L2 — Android module wiring (mixed)
- [ ] Open `/android/` in Android Studio Koala+ (or Jellyfish 2024.3.1+).
- [ ] Add `wear` module to `settings.gradle.kts`: `include(":app", ":wear")`.
- [ ] Verify Gradle sync succeeds without errors.
- [ ] Verify `./gradlew :app:assembleRelease` builds.
- [ ] Verify `./gradlew :wear:assembleRelease` builds.
- [ ] Wire signing config to `helios-release.jks` via Gradle properties (not committed to repo — use `~/.gradle/gradle.properties`).
- [ ] Run on physical Pixel 8 to verify install.

### L3 — Asset generation (mixed)
- [ ] Convert `/release/app-icons/icon-template.svg` to:
  - iOS: 1024×1024 PNG (App Store) + 180×180, 167×167, 152×152, 120×120, 87×87, 80×80, 76×76, 60×60, 58×58, 40×40, 29×29, 20×20.
  - Android: 192×192, 144×144, 96×96, 72×72, 48×48 (mipmap), plus 432×432 adaptive (foreground + background layers).
- [ ] Drop PNGs into `/ios/HeliosApp/Resources/Assets.xcassets/AppIcon.appiconset/` and `/android/app/src/main/res/mipmap-*/`.
- [ ] Run app in iOS Simulator (iPhone 15 Pro, iPhone 15 Pro Max, iPad Pro 13") and capture 5 screenshots per device via Cmd+S.
- [ ] Run app in Android emulator (Pixel 8, Pixel Fold open + closed, Galaxy Tab S9 Ultra) and capture 5 screenshots per device via Cmd+Ctrl+Shift+R.
- [ ] Drop screenshots into `/release/marketing-screenshots/captures/` with naming `<device>-<scene>.png`.

### L4 — Accessibility integration (builder-droid)
- [ ] Re-run M6 of the previous mission with a smaller per-screen scope.
- [ ] For each of `DashboardView`, `ProductionView`, `InsightsView`, `BatteryView`, `SettingsView`, `SharedView`, `LandingView`: add `.accessibilityLabel`, `.accessibilityValue`, `.accessibilityHint`, `.accessibilityAddTraits` using keys from `Localizable.strings`.
- [ ] For each of `DashboardScreen.kt`, `ProductionScreen.kt`, `InsightsScreen.kt`, `BatteryScreen.kt`, `SettingsScreen.kt`, `SharedScreen.kt`, `LandingScreen.kt`: add `Modifier.semantics { contentDescription = stringResource(...) }`.
- [ ] Run iOS Accessibility Inspector audit. Resolve all warnings.
- [ ] Run Android Accessibility Scanner audit. Resolve all critical findings.

### L5 — Translation pass (human or translator-service)
- [ ] Send `/ios/HeliosApp/Resources/Localizations/en.lproj/Localizable.strings` + `/android/app/src/main/res/values/strings.xml` to a translation service (Crowdin, Lokalise, or paid translator).
- [ ] Target languages: it, pt-BR, ja, zh-Hant, ko, he, hi, ru, tr, nl, pl, sv, da, fi, nb, cs, el, id, th, vi (20 langs total; en/es/fr/de/zh-Hans/ar already stubbed).
- [ ] Replace stub files in `/ios/HeliosApp/Resources/Localizations/<lang>.lproj/` and `/android/app/src/main/res/values-<lang>/`.
- [ ] Verify RTL layout in Arabic and Hebrew on both platforms.

### L6 — Device QA (human-only)
- [ ] Run iOS app on physical iPhone 15 Pro (or newer): full screen-by-screen pass; verify Live Activity, Dynamic Island, widgets, watch sync.
- [ ] Run iOS app on physical iPhone SE (2nd or 3rd gen): verify 4.7" layout reflows correctly.
- [ ] Run iOS app on physical iPad Pro: verify landscape + split-view layouts.
- [ ] Run watchOS app on Apple Watch Series 8+: verify complications and glance.
- [ ] Run Android app on physical Pixel 8: full screen-by-screen pass; verify Glance widgets, QS tile, App Actions via Assistant.
- [ ] Run Android app on Pixel Fold: verify foldable adaptive layouts.
- [ ] Run Android app on Galaxy Tab S9: verify tablet layout.
- [ ] Run Wear OS app on Pixel Watch 2 or Galaxy Watch 6: verify tile, complication, watch face.
- [ ] Test share-link cross-platform: generate on iOS, open on Android (and vice versa).
- [ ] Test deep links: `https://helios.app/share/<payload>` and `helios://share/<payload>`.
- [ ] Battery test: 30-minute foreground session must drop battery <4% on iPhone 15 Pro, <5% on Pixel 8.

### L7 — TestFlight rollout (mixed)
- [ ] In Xcode: Product → Archive → Organizer → Distribute App → App Store Connect → Upload.
- [ ] In App Store Connect: Wait for processing (typically 10-30 min). Add to internal testing group.
- [ ] Submit internal build to 12 internal-test UDIDs.
- [ ] Collect feedback for 3-5 days. Triage critical bugs.
- [ ] Promote to external testing (up to 10,000 testers; requires Apple beta review, typically 24-48 hours).

### L8 — Play Internal Testing rollout (mixed)
- [ ] In Android Studio: Build → Generate Signed Bundle / APK → Android App Bundle → Release.
- [ ] Upload AAB to Play Console → Testing → Internal testing → Create new release.
- [ ] Add tester email list. Distribute opt-in link.
- [ ] Collect feedback for 3-5 days. Triage critical bugs.
- [ ] Promote to Closed testing track (curated tester list, up to 100 testers).

### L9 — Store submission (human-only)
- [ ] **iOS**: In App Store Connect, fill all metadata fields (description, keywords, screenshots, age rating questionnaire, content rights). Set pricing. Submit for review. Expect 24-48 hour review.
- [ ] **Android**: In Play Console, fill all metadata, content rating (IARC questionnaire), data safety form, target audience, pricing. Send for production review. Expect 1-7 day review.
- [ ] Respond to any review-team feedback within 24 hours.

### L10 — Production launch (human-only)
- [ ] iOS: Once approved, set release to "Manual release" and trigger when ready.
- [ ] Android: Once approved, set staged rollout to 20% → 50% → 100% over 3-5 days.
- [ ] Monitor crash reports via App Store Connect + Play Console + Sentry (optional).
- [ ] Monitor App Store + Play Store reviews. Respond within 24 hours.

## 4. Constraints

- No emoji in code.
- No third-party trackers, analytics SDKs, or ad networks.
- Local-first data; no cloud sync.
- Privacy nutrition label and data safety form must match actual app behavior.
- Tag `1.0.0` on `main` only when L10 reaches 100% rollout on both stores.

## 5. Definition of Done

- App Store listing live at `https://apps.apple.com/app/id<ASSIGNED_BY_APPLE>`.
- Play Store listing live at `https://play.google.com/store/apps/details?id=com.helios.app`.
- Both at version `1.0.0`, build `1`, status `Available`.
- Crash-free user rate ≥ 99.5% in first 7 days.
- Average rating ≥ 4.5 stars across both stores (week 1).
- At least 1 successful share-link round-trip between iOS and Android in production.

## 6. Risks & rollback

| Risk | Likelihood | Mitigation |
|------|------------|------------|
| App Store rejection: missing privacy details | medium | Privacy nutrition label is pre-filled; double-check against actual app behavior. |
| Play Store rejection: data safety mismatch | medium | Data safety form is pre-filled; verify against actual app behavior. |
| Xcode extension target wiring fails | high | Reference `/release/build-notes/testflight.md`. May need to recreate targets and copy files in. |
| Translation quality issues in stubs | high | Don't ship with stubs — only ship languages with reviewed translations. Gate by L5. |
| Battery drain regression | low | Profile with Instruments / Android Profiler before submission. |
| Cross-platform share-link drift | low | Schema is v1 frozen; future versions must preserve v1 decoder. |

## 7. Handover state

- Repo: `/Users/yashgupta/Desktop/yash/code/Helios`, remote `https://github.com/yashgpt2894/Helios.git`, branch `main`.
- Predecessor mission: `.factory/missions/helios-native-apps.md` (status: complete).
- All 175 deliverable files committed to `main` as of `2026-05-16`.
- Next droid (mission orchestrator) should drive L1, L2, L4 via builder droid; surface L0, L3, L5, L6, L7, L8, L9, L10 to the human owner via AskUser checkpoints.

# Execution state

## Objective and authorized scope

Quest step: produce the Android phone design foundation for helios solar
intelligence, as documents only. Deliverables: `.mobile-work/STATE.md` (this file),
`design/DESIGN.md`, `design/ux-flows.md`, `design/screen-inventory.md`.

Repository: `/Users/yashgupta/Desktop/yash/code/Helios/.infy/worktrees/quest-23d2e03d-s1-v1`
(worktree of the Helios repository, base commit `daebea8`). Platform in scope: Android
phone only. Scoped features: first-run onboarding and inverter connection, live
dashboard with energy flow and tickers, production and inverter detail with per-string
telemetry, 7-day forecast, insights and savings, battery state of charge and charge
strategy, settings for connection, location, theme, and white-label brand, shareable
snapshot deep links, shared-snapshot viewer.

Authorized actions used: reading the repository, running the bundled doctor script,
writing documents under `design/` and `.mobile-work/`, running a local check script.
Not used and not authorized in this step: any edit to `android/`, `ios/`, `dist/`,
`dist-tsc/`, or `release/`; any emulator boot; any dependency install; any network
call; any commit, push, or merge; any external service or upload.

External processing policy: no repository content, screenshot, or user media left the
machine. Nothing in the design requires a third-party service beyond the two
documented Open-Meteo calls that the PWA already makes (forecast and reverse geocode).

## Assumptions and decisions

User facts (from the quest brief and the repository, not inferred):

- The product is helios solar intelligence for people who own solar panels and
  batteries; the Android app must reach feature parity with the PWA (`README.md`,
  `shared-spec/feature-parity-matrix.md`).
- Local-first: no accounts, no cloud sync, no analytics, no trackers, no secrets in the
  app.
- `SnapshotPayload` v1 base64url must stay PWA-identical.
- The existing Jetpack Compose app in `android/` and its design system in
  `android/app/src/main/java/com/helios/core/designsystem/` are the styling authority;
  no React Native, no Expo, no second design system.
- No emoji in copy, code, or documents.
- Non-goals: iOS, Wear OS, store submission, accounts, cloud sync, analytics.

Reversible assumptions (made to keep the design concrete; each is recorded with its
reason in `.mobile-work/decisions.md`):

- A1 The inverter is a SunSpec Modbus TCP device on the home LAN, and most devices
  accept one master at a time. This drives the failure taxonomy in `design/ux-flows.md`
  FLW-03.
- A2 Device discovery (mDNS or subnet scan) is out of scope for v1; the connection step
  takes a host and a port plus a one-tap demo fallback.
- A3 Freshness thresholds: live at 5 s or less, aging to 15 s, stale past 15 s, with a
  bounded retry schedule of 1 s / 2 s / 4 s, then 30 s, then 60 s after five failures.
- A4 Five bottom-navigation destinations (Home, Solar, Insights, Battery, Settings),
  from FPM section 1 and `src/components/BottomNav.tsx`.
- A5 The marketing landing page is not shipped inside the Android app; the first run is
  the working onboarding that FPM section 1 already assigns to that route.
- A6 Location permission is requested from the "Use my location" action only, not at
  launch. This deliberately replaces the launch-time request in `MainActivity.kt`.
- A7 Notifications and app lock are not shipped in this pass, so their PWA settings rows
  are removed rather than shown as dead controls. This needs the owner's confirmation
  and is flagged in `design/screen-inventory.md` (SCR-11 notes).
- A8 The brand registry must match the PWA (`helios`, `voltcraft`, `sunworks`,
  `meridian`); the Android registry currently differs and must change.

Design decisions: D1 "Live flow console" selected over D2 "Day ledger" and D3 "Today
timeline", with reasons and three corrections (freshness as a first-class element,
connection reachable in one tap, insights suppressed when data cannot support them).
Full evaluation in `design/DESIGN.md` sections 4-6; the decision list is
`.mobile-work/decisions.md`.

## Environment

Detected by `python3 <skill>/scripts/doctor.py --root .` (exit 0; raw output saved to
`.mobile-work/evidence/doctor.json`):

- Host: Darwin 24.5.0, arm64, `python3` 3.14.4 at `/opt/homebrew/bin/python3`.
- Present: git, node 22, npm, pnpm, uv, adb, sdkmanager, java, xcodebuild, xcrun, swift.
- Absent: docker, yarn, bun, flutter, dart, maestro, watchman, and `emulator` on PATH.
- Project manifests found: `package.json`, `package-lock.json` (react ^18.3.1,
  typescript ^5.6.3). The doctor script does not inspect Gradle manifests, so the
  Android toolchain was checked directly (below).
- The doctor reports `ANDROID_HOME`, `ANDROID_SDK_ROOT`, `JAVA_HOME`, and
  `DEVELOPER_DIR` as unset in this session's environment.

Checked directly for this step:

- `/opt/homebrew/opt/openjdk@17/bin/java` exists and reports OpenJDK 17.0.20.
- `/opt/homebrew/share/android-commandlinetools/emulator/emulator` and
  `.../platform-tools/adb` exist; `$ANDROID_HOME/platform-tools/adb devices` lists no
  running device (none was started, which is correct for a documents-only step).
- The AVDs are present at `/Users/yashgupta/.android/avd/` (`helios35.avd`,
  `medium_phone.avd`). Important environment finding: this session's `HOME` is
  redirected to the control-plane directory
  (`/Users/yashgupta/Library/Application Support/@product/desktop/control-plane/prime/d711f1c3-0415-4185-bccf-076f07344065`),
  so `emulator -list-avds` returns nothing until `ANDROID_AVD_HOME=/Users/yashgupta/.android/avd`
  is set. With that variable set, `emulator -list-avds` returns `helios35` and
  `medium_phone`. The next step must export it, and must export
  `GRADLE_USER_HOME=/Users/yashgupta/.gradle` explicitly because the redirected `HOME`
  would otherwise point Gradle at a fresh cache.
- Python helper limitation: the doctor script reports executable presence only, not a
  functional device, licence, or permission state.

Dependency plan: none acquired in this step; nothing was installed. The build step needs
no new Android dependency for this design: Compose BOM 2024.12.00, navigation-compose
2.8.5, Room 2.6.1, DataStore 1.1.1, Hilt 2.51.1, play-services-location 21.3.0, and
kotlinx-serialization-json 1.7.3 are already declared in
`android/app/build.gradle.kts`. Details and rollback notes are in
`.mobile-work/dependencies.md`.

## Current milestone

Milestone: Android phone design foundation written and internally cross-checked
(documents only).

Acceptance criteria and status:

- `.mobile-work/STATE.md` uses the skill's template headings verbatim — complete.
- `design/DESIGN.md` contains three genuinely distinct Dashboard directions that differ
  in hierarchy and interaction emphasis (not accent colour), evaluates them against
  task clarity, brand fit, native plausibility, accessibility, density, cohesion, and
  implementation cost, and selects one with numbered reasons and three corrections —
  complete.
- `design/ux-flows.md` covers first run, the principal repeated task (checking live
  solar and battery state), recovery from inverter and network failure, and return use,
  plus the share and shared-viewer flow — complete.
- `design/screen-inventory.md` gives stable IDs for every route, screen, state, and
  visible action, each action stating trigger, feedback, state change, persistence,
  failure response, and accessibility semantics, each traced to the feature-parity
  matrix — complete: 12 requirements, 19 routes, 19 screens, 89 states, 94 actions,
  24 components, 19 service concerns, 6 flows.
- Cross-document reference integrity, heading conformance, and the "no android/ source
  changed" constraint are machine-checked by `.mobile-work/check-design-docs.py` and
  pass (see evidence below).

Status: complete for this step. No visual review, screenshot, or device verification is
claimed, because this step produces documents rather than a running build.

## Evidence and artifacts

Artifacts written in this step (all new files, committed locally on the Quest branch
`codex/quest-23d2e03d-s1-v1` with the message "docs(android): add design direction, ux
flows and screen inventory"; nothing was pushed, and no `android/` file was modified):

- `design/DESIGN.md` — 381 lines: scope and authority, the task that must win, three
  directions, evaluation table, selected direction with corrections, token-to-Compose
  mapping, component contracts, accessibility decisions, rejected alternatives, risks.
- `design/ux-flows.md` — flows FLW-01 to FLW-06 with state taxonomy, first-run steps and
  timing, the six glance facts, the F1-F9 failure taxonomy, recovery rules, return use,
  share and viewer behaviour including the App Links gap.
- `design/screen-inventory.md` — requirements, routes, screens with states and actions,
  components, services, flows, and a coverage-and-gaps section.
- `.mobile-work/decisions.md` — decision log D-01 to D-14 with reasons.
- `.mobile-work/dependencies.md` — dependency plan, including the environment overrides
  the build step needs.
- `.mobile-work/evidence/doctor.json` — raw doctor output (exit 0).
- `.mobile-work/evidence/check-design-docs.txt` — output of the documentation check.
- `.mobile-work/check-design-docs.py` — the check itself, re-runnable.

Commands run (all with the working directory set to the worktree root):

- `python3 <skill>/scripts/doctor.py --root .` → exit 0.
- `/opt/homebrew/opt/openjdk@17/bin/java -version` → OpenJDK 17.0.20.
- `$ANDROID_HOME/emulator/emulator -list-avds` with `ANDROID_AVD_HOME` set → `helios35`,
  `medium_phone`; without it → empty (documented above).
- `$ANDROID_HOME/platform-tools/adb devices` → no devices attached.
- `python3 .mobile-work/check-design-docs.py` → exit 0 (details in the evidence file).
- `git status --porcelain` after the commit → clean; no
  path under `android/`, `ios/`, `dist/`, `dist-tsc/`, or `release/` was modified.

Sources read to establish the brief: `README.md`; `.factory/missions/helios-native-apps.md`;
`shared-spec/feature-parity-matrix.md`, `design-tokens.json`, `motion-language.md`,
`simulation-formulas.md`, `screens/dashboard-dark.svg`; `design/` (icon assets);
`src/` pages, components, store, and services as cited by path and line throughout the
design documents; and the Android sources, including `MainActivity.kt`,
`core/nav/HeliosNavGraph.kt`, `feature/*/**Screen.kt`, and
`core/data/repository/*.kt`.

## Blockers and risks

Blockers: none. Every required deliverable exists and the check passes.

Risks and unresolved issues, stated honestly:

1. No rendering, screenshot, or device verification happened in this step, by design.
   The design's claims about layout fit (hero block within 430 dp, five forecast days at
   caption-2, the four-item hero header row at font scale 1.6) are calculations from the
   token values, not measurements. They must be measured on the `helios35` AVD during
   the build step, and corrected there if they fail.
2. Design quality is not empirically validated. No user research was performed, and
   none is claimed. The direction was evaluated against the repository's own spec and
   the brief's task, not against real users.
3. The Android implementation is well behind the design: 8 of 19 screens do not exist,
   4 are stubs, the Dashboard is partial, `BottomNav` is never composed, telemetry is
   read once per screen, the theme is hard-coded to dark, location permission is
   requested at launch, and the settings share button is a dead control. This is
   expected for a design step and is listed per screen in `design/screen-inventory.md`.
4. Verified Android App Links cannot work until `.well-known/assetlinks.json` is hosted
   for `helios.app`; it is absent from `public/`. Deep-link behaviour must be tested with
   `helios://share/{payload}`, and the verified-link gap stays open until the host file
   exists.
5. Brand-registry mismatch: `android/.../BrandRepository.kt` uses
   `helios`/`solaris`/`volt`/`aether`, the PWA uses
   `helios`/`voltcraft`/`sunworks`/`meridian`. Until they match, a shared snapshot can
   resolve to the wrong brand.
6. Snapshot parity is asserted at the schema level (v1 fields and rounding rules), not at
   the byte level: Kotlin JSON serialisation does not have to produce the same key order
   as `JSON.stringify`. The build step must prove round-trip compatibility in both
   directions instead of assuming string equality.
7. Two PWA settings rows (Notifications, App lock) are dropped from the Android design
   because they have no implementation and a dead control is not acceptable. If the owner
   wants either feature, it needs a real design (BiometricPrompt plus a lock state
   machine; WorkManager plus notification permission at the point of use).
8. Delegation note: one subagent (`ui-surface-extract`) was started to produce a
   machine-readable UI-surface extraction. It ran 23 tool calls and ended without
   writing its file, so its output was discarded and the extraction was done directly by
   reading the sources. No conclusion in these documents rests on an unverified
   subagent claim.

## Next action

Next step (build, not this one): implement the Android first-run gate and connection
stack — SCR-01 to SCR-06 plus the Room-backed `ConnectionRepository` (SVC-19), the
freshness-aware telemetry flow, and the five-destination scaffold — against
`design/screen-inventory.md` and `design/ux-flows.md`, then build with
`JAVA_HOME=/opt/homebrew/opt/openjdk@17`,
`ANDROID_HOME=/opt/homebrew/share/android-commandlinetools`,
`GRADLE_USER_HOME=/Users/yashgupta/.gradle`, and
`ANDROID_AVD_HOME=/Users/yashgupta/.android/avd`, boot `helios35` under the
`/tmp/helios-emulator.lock` discipline, and capture real screenshots as evidence.

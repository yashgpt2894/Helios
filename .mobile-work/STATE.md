# Execution state

## Objective and authorized scope

Current step: the three surfaces of the Android port at step 4 and 5 of the
mobile-experience-design skill, as runnable Compose with fixture data.

Deliverables of this step:

- First-run onboarding (`feature/landing/**`) that reaches value before it asks for
  anything: page 1 shows a live reading from the demo system and offers the demo path in
  one tap; connection details are asked on page 3 and can be skipped; the location is asked
  on the last page, is skippable, requests the permission from the button that needs it, and
  keeps a working default when permission is denied.
- Settings (`feature/settings/**`): the settings list (SCR-11) with the current value of
  every concern, plus the connection form (protocol, host, port, unit id, poll interval),
  location with a use-my-location action, the theme picker, the white-label brand chooser,
  and the share action.
- The read-only shared-snapshot viewer (`feature/shared/**`): a decoded `SnapshotPayload`
  v1, an invalid or truncated payload state, a white-label payload, and an older-reading
  state, with copy-link and open-in-browser as the only writes it can make.

Constraints kept: no edit to `core/nav/**`, `MainActivity.kt`, `core/designsystem/**` or any
other feature package; no emoji anywhere in UI copy; colour, type, spacing, shape and motion
come from `design/tokens.json` through the existing Compose token objects; every text input
sits in a scroll container with `imePadding()`; back behaviour is implemented per surface.

## Assumptions and decisions

Decisions from the earlier design steps still hold (see `design/DESIGN.md` and
`.mobile-work/decisions.md`). Decisions taken in this step, each with its reason:

- D-25 The connection form is one component (`feature/settings/ConnectionConfigForm.kt`) with
  one validator, rendered by both SCR-04 (first run) and SCR-12 (Settings). One form means a
  value accepted on one screen cannot be rejected on the other. The onboarding package
  imports it, because Settings is the natural owner of connection configuration.
- D-26 `SetupField`, `SegmentedOptions` and the row vocabulary are implemented inside
  `feature/settings` rather than `core/designsystem`, because that package belongs to another
  step. They read semantic tokens only, so moving them is a mechanical move.
- D-27 The host owns light and dark. `MainActivity` applies `HeliosTheme` from the stored
  theme; a screen that re-applied it would fight the host (a capture host asking for the light
  palette would get the dark one). The Settings surface therefore keeps the palette it is
  given and re-applies only the white-label accent, which is the one colour a brand change
  must show immediately.
- D-28 A preference write is shown at once and then confirmed by re-reading the service. The
  fixture family drives its flows from one shared scenario switch, so it does not re-emit on a
  preference write; without this rule the theme picker and the brand chooser would look dead
  in a fixture build. With the device adapters both paths agree.
- D-29 The probe is bounded in the screen: a candidate that does not answer inside 6 s is
  reported as F2 (timeout) with the values tried, which is what ACT-012 asks the 6 s bound to
  produce. Nothing is persisted on a failure, and the screen states which configuration is
  live.
- D-30 The shared viewer labels a snapshot older than an hour as an older reading. A snapshot
  is a moment, not a live link, so the state says so in words instead of showing a stale
  figure as current.
- D-31 The surfaces accept `initialStep` / `initialSection`. RTE-02 to RTE-06 and RTE-12 to
  RTE-16 are addressable routes, so a host may open the flow or one secondary surface
  directly; this is also what makes a single-state capture reproducible.
- D-32 Actions whose only outcome is navigation are rendered only when the host supplies the
  callback (`onFinished` on first run, `onTryHelios` in the viewer). A control that does
  nothing is removed rather than shown, which is the design's own rule.

## Environment

- Host: Darwin 24.5.0, arm64. JDK `/opt/homebrew/opt/openjdk@17`, Android SDK
  `/opt/homebrew/share/android-commandlinetools`, Gradle 8.11.1. `$HOME` is redirected to the
  control-plane directory, so the Gradle wrapper prelude moves `GRADLE_USER_HOME` to
  `$HOME/.gradle-helios` and seeds it from the read-only default.
- The environment the step is graded in is not the environment it is written in. Grading ran
  `cd android && ./gradlew :app:compileDebugKotlin` with no `JAVA_HOME`, no `ANDROID_HOME` and
  `HOME=/Users/yashgupta`, which is not writable: the wrapper prelude moved `GRADLE_USER_HOME`
  to `android/.gradle-user-home` and resolved a JDK, and the build then failed with
  `SDK location not found` because nothing named the Android SDK. Every command below was
  therefore re-run with those variables removed (see "Repair after validation").
- Network is reachable, so plugin resolution works without `--offline`; `--offline` fails
  with "Plugin [id: 'com.android.application', version: '8.7.3'] was not found" when the
  cache is cold.
- Emulator: the shared `helios35` instance was booted and held under
  `/tmp/helios-emulator.lock` for the capture run. A second instance from the `medium_phone`
  AVD was started to avoid contention and abandoned: it never finished booting (repeated
  "detected a hanging thread 'QEMU2 CPU0 thread'"). A sibling step of this Quest was
  capturing on `helios35` at the same time (its `com.helios.feature.battery.CaptureHostActivity`
  was the top activity, and its `adb install` replaced this step's APK twice), so every
  capture was taken with a marker check, a reinstall and an automatic retry.
- `bash()` is unavailable in this session (`orphan-journal enrollment failed`); every command
  ran through `subprocess.run` or `subprocess.Popen` with output logged under `/tmp/helios-*.log`.

## Current milestone

Milestone: three surfaces implemented, compiling, exercised on a device, and captured.

Acceptance criteria and status:

- `cd android && ./gradlew :app:compileDebugKotlin` — exit 0 with an exported `ANDROID_HOME`,
  and after the repair below also exit 0 with no environment at all (the form the step is
  graded in). The earlier claim of exit 0 was true only with `ANDROID_HOME` exported; see
  "Repair after validation".
- `cd android && ./gradlew :app:testDebugUnitTest` — complete, exit 0, existing 17 tests pass.
- Onboarding reaches value first, is skippable, requests the permission at the action, and
  has denied-permission and unreachable-inverter paths — complete; each path captured.
- Settings covers connection, location, theme, brand and share — complete; the theme picker
  and the brand chooser change the screen immediately, and the share action produces a real
  v1 link.
- The viewer renders a decoded v1 payload, an invalid payload and an older reading, read-only
  — complete, including two captures taken through the app's own `helios://share/...` deep
  link rather than through a host.
- Captures for dark and light themes — complete: 52 PNGs under `design/captures/`. The
  first pass left light to a subset of states; a second pass on an independent emulator
  (`helios-capture`, port 5556, booted because the shared device was held by a sibling
  step) added light for the connect, unreachable, location, denied and complete pages,
  connection, location, brand and share settings, the invalid payload and the white-label
  payload, plus `shared-stale-dark` and two real deep links from the shipped build. See
  `.mobile-work/step7-capture-pass.md`.
- Keyboard avoidance and back behaviour — implemented (`imePadding` plus scrolling on every
  input surface, `BackHandler` for the pager and for each secondary Settings surface, a leave
  guard on a dirty connection form). Back behaviour was exercised by hand on the device; the
  keyboard case is captured for two surfaces, see the risks for the limit.

## Evidence and artifacts

Source, all inside the owned packages:

- `feature/landing/`: `OnboardingModel.kt`, `OnboardingScreen.kt`, `OnboardingPages.kt`,
  `LandingScreen.kt` (rewritten), `OnboardingPreviews.kt`.
- `feature/settings/`: `SettingsScreen.kt` (rewritten), `SetupField.kt`, `SettingsRows.kt`,
  `ConnectionConfigForm.kt`, `ConnectionSettingsSection.kt`, `LocationActions.kt`,
  `LocationSettingsSection.kt`, `CoordinateEntrySheet.kt`, `AppearanceSettingsSection.kt`,
  `BrandSettingsSection.kt`, `ShareSnapshotSheet.kt`, `SettingsPreviews.kt`.
- `feature/shared/`: `SharedScreen.kt` (rewritten), `SharedPreviews.kt`.

Commands and results (working directory the worktree root unless stated):

- `cd android && ./gradlew :app:compileDebugKotlin` -> exit 0.
- `cd android && ./gradlew :app:assembleDebug :app:testDebugUnitTest` -> exit 0.
- `cd android && ./gradlew :app:assembleDebug` -> exit 0; APK installed on `emulator-5554`.
- `adb shell am start -a android.intent.action.VIEW -d helios://share/<payload>` -> the app
  opens SCR-17 through its own navigation, with the payload decoded
  (`shared-deeplink-valid-dark.png`, `shared-deeplink-invalid-dark.png`).
- Capture run: 35 screens, `design/captures/<surface>-<state>.png`, plus a review contact
  sheet `design/captures/screens-step6.png` (1128x3620). Every capture was verified to be the
  intended state from a UI dump before the screenshot was taken, and each was inspected
  afterwards for luminance and colour count (dark frames 10-30 mean luminance, light frames
  229-242, 262-1066 distinct colours, none blank).
- `python3 .mobile-work/check-design-docs.py` -> 93 of 94 checks pass. The one failure is
  "no application source modified by this step", which is that checker's design-step rule and
  is expected here: this step's deliverable is application source.

Captured states: onboarding welcome (dark, light), local-first, connect (default, invalid
input, with the soft keyboard up, at 160 percent text), result answered, result unreachable
(F2 timeout), location (default, denied), complete (dark, light); settings root (dark, light),
connection (saved, dirty with the keyboard up, probe result, 160 percent text), location
(default, denied), appearance (dark, light, Paper selected), brand (helios, Voltcraft
selected), share sheet; shared viewer (valid dark, valid light, white label, older reading,
invalid, valid and invalid through the real deep link, 160 percent text).

## Repair after validation

The first validation of this step failed before any task ran:

```
* What went wrong:
Could not determine the dependencies of task ':app:compileDebugKotlin'.
> SDK location not found. Define a valid SDK location with an ANDROID_HOME environment
  variable or by setting the sdk.dir path in your project's local properties file at
  '.../android/local.properties'.
```

The cause was environmental, not in the owned sources: the grading shell sets no `ANDROID_HOME`
and there is no `android/local.properties` (it is gitignored and was never committed), so AGP
had nothing to resolve. The wrapper prelude already resolved the Gradle user home, the Android
user home and a JDK for exactly this reason, and the SDK was the one default it did not cover.

Fix (`android/gradlew`, "Helios sandbox prelude"): a fourth resolution step. `ANDROID_HOME`,
then `ANDROID_SDK_ROOT`, then a real `sdk.dir` in `local.properties`, each accepted only when
the directory it names contains `platforms/`; when none of the three is usable, the first
known install location that is (`$HOME/Library/Android/sdk`,
`/opt/homebrew/share/android-commandlinetools`, `/opt/homebrew/share/android-sdk`,
`/usr/local/share/android-commandlinetools`, `/usr/local/share/android-sdk`,
`/Library/Android/sdk`, `/Applications/Android Studio.app/Contents/sdk`) is exported as both
`ANDROID_HOME` and `ANDROID_SDK_ROOT`, and the prelude says so on stderr. An explicit
environment value and a project `local.properties` still win, and the prelude does nothing
where they are already correct.

Verification, all with `JAVA_HOME`, `ANDROID_HOME`, `ANDROID_SDK_ROOT`, `GRADLE_USER_HOME` and
`ANDROID_USER_HOME` removed from the environment and `HOME=/Users/yashgupta`:

- `cd android && ./gradlew :app:compileDebugKotlin` -> exit 0,
  `Helios: no usable Android SDK in ANDROID_HOME or local.properties; using
  /opt/homebrew/share/android-commandlinetools.`
- `cd android && ./gradlew :app:compileDebugKotlin --rerun-tasks` -> exit 0, 15 tasks
  executed, `compileDebugKotlin` really recompiled; the only warnings are in
  `app/qs/`, `core/ui/theme/HeliosTheme.kt` and `feature/dashboard/EnergyFlow.kt`, none in the
  three packages this step owns.
- `cd android && ./gradlew :app:testDebugUnitTest` -> exit 0 (17 tests, up to date).
- `cd android && ./gradlew :app:assembleDebug` -> exit 0 twice: once with the temporary capture
  host in the tree and once after removing it again, so the committed tree is the build that was
  installed for the captures.
- The temporary `feature/landing/CaptureHost.kt` and its one-line change to `LandingScreen.kt`
  are gone: `git status` shows no modification to either surface file, and the shipped build was
  installed on `emulator-5556` afterwards to capture `onboarding-welcome-dark` and the two
  `helios://share/...` deep links from it.
- Reproduced first without the fix in the same environment: exit 1 with the message above.

The shared emulator was held by a sibling step of this Quest for the whole repair window
(`com.helios.app/com.helios.debug.screens.ScreenCaptureActivity` in the foreground, their APK
installed), so the extra light captures were taken on an independent emulator instead of
installing over their build: an `helios-capture` AVD (Pixel 6, android-35, arm64-v8a) created
under `/tmp/helios-user-home/avd` and booted on port 5556. 17 captures were added or re-taken
there, and two were re-taken from the shipped build through its own deep link. See
`.mobile-work/step7-capture-pass.md`.

## Blockers and risks

Blockers: none.

Risks and unresolved issues, stated honestly:

1. Navigation is not wired, and this step may not touch `core/nav/**` or `MainActivity.kt`.
   Consequences: the Settings tab is unreachable in the running app until that step lands
   (it is reachable through a host, which is how it was captured), the first-run completion
   step shows no dashboard action until a host passes `onFinished`, and the app-wide palette
   change after choosing Paper or Carbon arrives with the shell that owns the theme. The
   surfaces accept `initialSection` / `initialStep` and `onFinished` for exactly that wiring.
2. The emulator has a hardware keyboard, so the soft keyboard appears only while a text field
   holds focus; two captures show it (`onboarding-connect-keyboard-dark.png`,
   `settings-connection-keyboard-dark.png`), and the keyboard-inset path on the other
   surfaces was not captured. `imePadding()` plus a scrolling container is on every input
   surface, and the manifest keeps `windowSoftInputMode="adjustResize"`.
3. Enlarged text was captured for two of the densest surfaces (onboarding connect, settings
   connection, and the viewer at 160 percent). The other surfaces were reviewed in source and
   in previews, not captured at scale.
4. The denied-permission captures use the fixture's `DENIED_PERMISSION` scenario, which is the
   design's own mechanism for exercising F9. The permission was granted on the device so the
   launch-time request in `MainActivity.kt` (a defect another step removes) could not cover
   the capture with a system dialog.
5. The fixture family does not re-emit a flow when a preference is written (D-28); the
   workaround is a re-read after the write. When the device adapters and Hilt wiring land, the
   same code paths read the DataStore flow and the workaround becomes redundant rather than
   wrong.
6. No unit tests were added: the test source set is outside this step's owned files. The
   validation rules, the failure-class text, the snapshot age rule and the payload decoding
   were exercised on the device instead (invalid input on both forms, forced F2, a three-hour
   old payload, an invalid deep link).
7. The settings connection screen does not show the attempt log of STS-064: no service in
   `core/data/service` exposes an attempt history, and a log built from invented entries would
   misreport the hardware. The current attempt counter is shown instead.
8. Place-name search is absent by design: no service exposes a geocoder, so the location
   screen offers typed coordinates, which is a real offline path, rather than a search field
   that cannot answer.
9. Captures were taken while a sibling step of this Quest was also using the shared emulator;
   two of this step's captures were lost to its installs and retaken. The capture script
   reinstalls and retries, so every file in `design/captures/` is the intended state.

## Next action

Next step: the navigation and shell step. It should install the device service family, add
the five destinations with `BottomNav`, resolve the theme before the first frame in
`MainActivity`, remove the launch-time location request, persist `first_run_complete`, map
RTE-02 to RTE-16 onto `OnboardingScreen(initialStep = ...)` and
`SettingsScreen(initialSection = ...)`, and pass `onFinished` and `onTryHelios` so the
dashboard and the viewer call to action appear.

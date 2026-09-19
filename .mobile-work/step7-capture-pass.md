# Second capture pass: light theme, on an independent emulator

The step asks for `design/captures/<surface>-<state>.png` for dark and light themes. The first
pass covered dark for every state and left light to a subset, because `MainActivity` applies
`HeliosTheme(darkTheme = true)`: the shipped app cannot render the light palette, and the
surfaces are otherwise reached only through a shell whose navigation step had not landed yet.

This pass closed the light gap without touching the sibling step's device.

## Why a second emulator

The shared `helios35` device (`emulator-5554`) was held by another step of this Quest for the
whole repair window, with their `com.helios.app/com.helios.debug.screens.ScreenCaptureActivity`
in the foreground. Installing over their build is what lost captures in the first pass, so this
pass built its own device instead of queueing:

```sh
export ANDROID_HOME=/opt/homebrew/share/android-commandlinetools
export JAVA_HOME=/opt/homebrew/opt/openjdk@17
export ANDROID_USER_HOME=/tmp/helios-user-home
$ANDROID_HOME/cmdline-tools/latest/bin/avdmanager create avd -n helios-capture \
    -k "system-images;android-35;google_apis;arm64-v8a" -d pixel_6
# the AVD lands in $ANDROID_USER_HOME/avd, which is also what the emulator must be told:
export ANDROID_AVD_HOME=/tmp/helios-user-home/avd
$ANDROID_HOME/emulator/emulator -avd helios-capture -port 5556 -no-snapshot-load \
    -no-boot-anim -gpu swiftshader_indirect -no-audio -no-window -no-metrics
```

Boot took 50 s, and the device is a clean Pixel 6 profile at 1080x2400 with no Helios package
installed, so nothing outside `/tmp` was touched and the sibling run was undisturbed.

## Harness

`feature/landing/CaptureHost.kt` was added for the duration of the run and deleted afterwards,
with `LandingScreen.kt` rendering `CaptureHostScreen()` instead of `OnboardingScreen(...)`.
The committed harness text is the same shape as the first pass (see
`step6-capture-harness.md`): intent extras choose the surface (`onboarding`, `settings`,
`shared`, `shared-invalid`), the state (`step`, `section`, `brand`, `ageMs`, `scenario`,
`failure`, `dark`), and the theme. States that need a real interaction were reached by tapping
the app's own control with `adb shell input tap` after reading its bounds from a
`uiautomator dump`, not by pinning internal state:

- unreachable inverter: `failure=TIMEOUT` on the connect page, then tap `Test connection`;
- denied permission: `scenario=DENIED_PERMISSION` on the location page, then tap
  `Use my location`;
- completion: tap `Not now, use the default location`;
- brand chooser: tap `Voltcraft`;
- share action: tap `Share today's snapshot`.

Every capture was taken only after the UI dump contained the string that state must show, and
the file was discarded otherwise (one capture was written with a failed marker and was restored
from git; the shipped app cannot reach the dashboard, so the Settings tab is not reachable from
the launcher route - see risk 1 of STATE.md).

## Files written by this pass

Light: `onboarding-welcome-light` (re-taken), `onboarding-local-first-light`,
`onboarding-connect-light`, `onboarding-result-unreachable-light`, `onboarding-location-light`,
`onboarding-location-denied-light`, `onboarding-complete-light` (re-taken),
`settings-root-light` (re-taken), `settings-connection-light`, `settings-location-light`,
`settings-appearance-light` (re-taken), `settings-brand-light`, `settings-share-light`,
`shared-valid-light` (re-taken), `shared-invalid-light`, `shared-white-label-light`.

Dark, to pair an existing light-only state and to re-take two from the shipped build:
`shared-stale-dark`, `onboarding-welcome-dark`, and the two real deep links
`shared-deeplink-valid-dark` and `shared-deeplink-invalid-dark`, taken by launching
`helios://share/<payload>` at the shipped APK with no host in the way.

## Inspection

Each frame was measured after the run: all light frames have a mean luminance of 218-243 and
262-2312 distinct colours, the dark frame 22.7 and 1271, so none is blank or a mis-themed
palette. The three densest light screens (`onboarding-connect-light`,
`settings-connection-light`, `shared-valid-light`) were also dumped and checked for nodes
outside the 1080x2400 viewport: none, so nothing is clipped at the default text scale.

## What this pass does not prove

- Light-theme layout at enlarged text, and the soft keyboard on the light palette, are not
  captured (`imePadding` and the scrolling containers are shared with the dark captures).
- The light palette in the shipped app depends on the shell step: `MainActivity` currently
  hardcodes the dark theme, so a light capture necessarily goes through the host above.

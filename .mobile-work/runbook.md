# Helios Android skeleton runbook

Reproducible build and run steps for the Compose app in `android/`.
Target of this Quest: `./gradlew :app:assembleDebug` succeeds, and the debug APK
installs and launches on the `helios35` emulator without a fatal log entry.

## Toolchain

| Item | Value |
| --- | --- |
| JDK | `/opt/homebrew/opt/openjdk@17` (pinned by `android/gradle.properties` via `org.gradle.java.home`) |
| Android SDK | `/opt/homebrew/share/android-commandlinetools` (from `android/local.properties`, `sdk.dir`) |
| Gradle | 8.11.1, from `android/gradle/wrapper/gradle-wrapper.properties` |
| AGP / Kotlin | 8.7.3 / 2.0.21 |
| Emulator AVD | `helios35` (android-35, google_apis, arm64-v8a) |

Environment variables used for every command below:

```sh
export JAVA_HOME=/opt/homebrew/opt/openjdk@17
export ANDROID_HOME=/opt/homebrew/share/android-commandlinetools
export ANDROID_SDK_ROOT=$ANDROID_HOME
export PATH=$JAVA_HOME/bin:$ANDROID_HOME/platform-tools:$ANDROID_HOME/emulator:$PATH
```

## Build

```sh
cd android
./gradlew :app:assembleDebug          # exit code 0
```

APK: `android/app/build/outputs/apk/debug/app-debug.apk`.

Full rebuild from an empty build directory:

```sh
cd android
./gradlew clean
./gradlew :app:assembleDebug
```

### Gradle and Android user homes in this sandbox

`/Users/yashgupta/.gradle` and `/Users/yashgupta/.android` are mounted read-only
here. Gradle cannot create the wrapper distribution lock there, and AGP cannot
create `$HOME/.android/debug.keystore`, so the plain command would fail even
though the cached distribution is present.

`android/gradlew` therefore carries a short prelude (marked "Helios sandbox
prelude"; the rest of the file is the generated wrapper). When a default home is
not writable it:

1. switches `GRADLE_USER_HOME` to `$HOME/.gradle-helios`, or to
   `android/.gradle-user-home` when `$HOME` itself is not writable;
2. seeds that home once from the read-only default (wrapper distribution plus
   `caches/modules-2` and `caches/jars-9`) so the build still works offline;
3. exports `ANDROID_USER_HOME` to a writable directory inside that home, so AGP
   can create the debug keystore.

Where the defaults are writable (a normal workstation) the prelude does nothing.
Both paths were exercised: exit code 0 with `GRADLE_USER_HOME=/Users/yashgupta/.gradle`,
and exit code 0 with `HOME=/Users/yashgupta`.

To take manual control instead of the fallback, set both variables explicitly:

```sh
cd android
GRADLE_USER_HOME=$HOME/.gradle-helios ANDROID_USER_HOME=$HOME/.gradle-helios/android-user-home ./gradlew :app:assembleDebug
```

## Run on the emulator

The shared AVD directory (`/Users/yashgupta/.android/avd`) is read-only, and a
stale `hardware-qemu.ini` in it points at absolute paths, so boot a writable copy:

```sh
rm -rf /tmp/helios-avd
cp -cR /Users/yashgupta/.android/avd/ /tmp/helios-avd/
sed -i '' 's|^path=.*|path=/tmp/helios-avd/helios35.avd|' /tmp/helios-avd/helios35.ini
rm -f /tmp/helios-avd/helios35.avd/hardware-qemu.ini \
      /tmp/helios-avd/helios35.avd/emu-launch-params.txt \
      /tmp/helios-avd/helios35.avd/version_num.cache \
      /tmp/helios-avd/helios35.avd/hardware-qemu.ini.lock \
      /tmp/helios-avd/helios35.avd/multiinstance.lock \
      /tmp/helios-avd/helios35.avd/read-snapshot.txt

echo $$ > /tmp/helios-emulator.lock          # serialize emulator use with this file
export ANDROID_AVD_HOME=/tmp/helios-avd
$ANDROID_HOME/emulator/emulator -avd helios35 -no-snapshot-load -no-boot-anim \
    -gpu swiftshader_indirect -no-audio -no-window -no-metrics &
$ANDROID_HOME/platform-tools/adb wait-for-device
```

`adb` and the emulator must share one adb key, so launch both with the same
`HOME`/`ANDROID_USER_HOME` (the steps above do).

## Install, launch, verify

```sh
cd <repo root>
$ANDROID_HOME/platform-tools/adb install -r -t android/app/build/outputs/apk/debug/app-debug.apk
$ANDROID_HOME/platform-tools/adb logcat -b all -c
$ANDROID_HOME/platform-tools/adb shell am start -W -n com.helios.app/.MainActivity

sleep 30
$ANDROID_HOME/platform-tools/adb shell pidof com.helios.app    # same pid as at launch = alive
$ANDROID_HOME/platform-tools/adb logcat -d -b all \
  | grep -E "FATAL EXCEPTION|E AndroidRuntime|am_crash|am_anr" # expect no output
$ANDROID_HOME/platform-tools/adb exec-out screencap -p > .mobile-work/evidence/skeleton-launch.png
```

`.mobile-work/verify-launch.sh` runs exactly this sequence and writes the four
evidence files; use it instead of retyping the commands.

Two install-time details:

- A package left behind by an earlier session was signed with a different debug
  keystore, which makes `install -r` fail with
  `INSTALL_FAILED_UPDATE_INCOMPATIBLE`. Run
  `$ANDROID_HOME/platform-tools/adb uninstall com.helios.app` once, then install.
- On the first run after a fresh install the app asks for the location
  permission, so the permission dialog is the top activity. Grant it
  (`adb shell pm grant com.helios.app android.permission.ACCESS_FINE_LOCATION`,
  plus the coarse one) or accept the dialog, then the landing screen appears.
  The app process stays alive in either case.

Component name: the launcher activity is `com.helios.app.MainActivity` in
package `com.helios.app` (see "Fixes" below), so both `com.helios.app/.MainActivity`
and `com.helios.app/com.helios.app.MainActivity` resolve.

## Fixes applied in this Quest

1. Wrapper: `gradle-wrapper.jar`, `gradlew` and `gradlew.bat` generated from the
   local Gradle 8.11.1 distribution; `distributionUrl` pinned to
   `gradle-8.11.1-bin.zip` (was `gradle-8.9-bin.zip`). The prelude above was
   added on top.
2. Compose BOM: `platform("androidx.compose:compose-bom:2024.12.01")` declared
   inside `dependencies` (was a `val` taking `platform(...)` outside the block,
   with the non-existent version `2024.12.00`). Same fix in `android/wear`.
3. Launcher icon: adaptive icon `res/mipmap-anydpi-v26/ic_launcher.xml` with
   vector `drawable/ic_launcher_background.xml` and
   `drawable/ic_launcher_foreground.xml`, so `@mipmap/ic_launcher` resolves.
4. Glance widgets compile: `GlanceTheme.colors.X` is used directly where
   `TextStyle(color = ...)` expects a `ColorProvider` instead of wrapping it in
   `ColorProvider(...)`; `Modifier.background(GlanceTheme.colors.primary)`;
   `Spacer` instead of `Box` for the sparkline bars; `RowScope.StatItem`;
   `HeliosWidgetReceiver` now calls the suspending `updateAll(context)` inside a
   coroutine scope that finishes the broadcast result.
5. Data layer compile fixes: `ForecastRepository.currentForecast` added for
   synchronous readers; `ShareRepository` Double rounding; `TelemetryRepository`
   Double arithmetic; `HeliosMotion.SpringSpec` renamed to the Compose
   `SpringSpec`; `ProductionChart` fill path built with `addPath`.
6. Runtime class resolution: the app shell classes moved from `com.helios` to
   `com.helios.app` (`HeliosApplication`, `MainActivity`, `qs/`, `widget/`).
   `namespace` and `applicationId` are `com.helios.app`, so the manifest entries
   `.HeliosApplication`, `.MainActivity`, `.qs.HeliosQSTileService` and
   `.widget.*Receiver` resolve, and `am start -n com.helios.app/.MainActivity`
   works. Before this the process died on start with
   `ClassNotFoundException: Didn't find class "com.helios.app.HeliosApplication"`.

## Verification results

Re-verified in this workspace after `./gradlew clean`, on the `helios35` AVD:

| Check | Result |
| --- | --- |
| `cd android && ./gradlew clean` | exit 0 |
| `cd android && ./gradlew :app:assembleDebug` (after clean, all 40 tasks executed) | exit 0, `app-debug.apk` rebuilt |
| `cd android && ./gradlew :app:assembleDebug` (incremental) | exit 0, 3 tasks executed |
| `adb uninstall` + `adb install -t app-debug.apk` | `Success` for both |
| `am start -W -n com.helios.app/.MainActivity` | `Status: ok`, cold start, 1192 ms |
| Process alive after 30 s | yes, pid 3674 unchanged |
| Fatal log entries since launch | 0 (`FATAL EXCEPTION`, `E AndroidRuntime`, `am_crash`, `am_anr`) |
| UI dump | package `com.helios.app`, `ComposeView` present, texts `Helios`, `Power, illuminated.`, `No hardware connected. Running in simulation mode.` |
| Screenshot | `.mobile-work/evidence/skeleton-launch.png`, 1080x2400, rendered dark landing screen (705 distinct colors) |

Raw evidence: `.mobile-work/evidence/skeleton-launch.png`,
`.mobile-work/evidence/logcat-launch-30s.log` (app pid only, launch plus 30 s),
`.mobile-work/evidence/launch-30s.txt` (command transcript, pid check, fatal
count), `.mobile-work/evidence/ui-hierarchy.xml`.

## Known issues

- The skeleton opens on `LandingScreen` (text "Power, illuminated." with a
  simulated-hardware notice), not the dashboard. That is the app's own
  navigation, not a build problem.
- Kotlin compiles with deprecation warnings only: `statusBarColor`,
  `navigationBarColor`, `quadraticBezierTo`, `startActivityAndCollapse`.
- `:wear` is configured in `settings.gradle.kts` but does not build:
  `./gradlew :wear:assembleDebug` fails in `:wear:checkDebugAarMetadata` because
  `androidx.wear.compose:compose-material3:1.0.0` and
  `com.google.android.horologist:horologist-complications-data:0.6.20` both
  return 404 from `google()` and `mavenCentral()` (checked directly). The broken
  versions are pre-existing, and the failure is confined to `:wear`;
  `./gradlew :app:assembleDebug` succeeds. This Quest only builds and runs
  `:app`, so `:wear` was left alone.
- The fallback Gradle and Android homes are build caches outside git
  (`$HOME/.gradle-helios`, `android/.gradle-user-home`, `/tmp/helios-avd`).
- The emulator is stopped after evidence capture (`adb emu kill`); restart it
  with the commands above. The shared AVD is never written to.

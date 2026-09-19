# Dependency record — helios Android design step

This step acquired nothing and installed nothing. The record exists so the build step
starts from a plan rather than from guesswork.

## What this step needed

| Need | Version | Source | Installed | Result |
| --- | --- | --- | --- | --- |
| Python 3, standard library only | 3.14.4 | system `/opt/homebrew/bin/python3` | already present | Used for the doctor script and the documentation check |
| Git | system `/usr/bin/git` | already present | Used for read-only status checks |

No package manager command was run. No lockfile was modified. `package-lock.json` is
untouched.

## What the build step needs, and where it already exists

Every Android dependency the design requires is already declared in
`android/app/build.gradle.kts`, so the design adds no new dependency:

| Concern | Declared dependency | Used for |
| --- | --- | --- |
| UI toolkit | Compose BOM `2024.12.00` (material3, ui, foundation, animation) | All screens and components |
| Navigation | `androidx.navigation:navigation-compose` `2.8.5` | Five destinations, modal routes, deep links |
| Persistence | `androidx.room:room-runtime` and `room-ktx` `2.6.1` | ConnectionConfig and the attempt log |
| Preferences | `androidx.datastore:datastore-preferences` `1.1.1` | Theme, brand, source, first-run flag, location |
| DI | Hilt `2.51.1` plus `hilt-navigation-compose` `1.2.0` | Repository wiring |
| Location | `com.google.android.gms:play-services-location` `21.3.0` | Use-my-location, requested at the action |
| Serialisation | `kotlinx-serialization-json` `1.7.3` | Snapshot encode and decode |
| Coroutines | `kotlinx-coroutines-core` and `-android` `1.9.0` | Telemetry tick and retry schedule |

No charting library is required: the design follows FPM section 6 and draws the flow,
the ring, and both charts with Compose `Canvas`.

## Environment overrides the build step must export

This session's `HOME` is redirected to the control-plane directory, which changes where
Gradle and the Android tools look for state. Export these explicitly:

```sh
export JAVA_HOME=/opt/homebrew/opt/openjdk@17
export ANDROID_HOME=/opt/homebrew/share/android-commandlinetools
export ANDROID_SDK_ROOT=$ANDROID_HOME
export GRADLE_USER_HOME=/Users/yashgupta/.gradle
export ANDROID_AVD_HOME=/Users/yashgupta/.android/avd
```

`ANDROID_AVD_HOME` matters: without it `emulator -list-avds` returns nothing, even
though `helios35.avd` and `medium_phone.avd` exist under `/Users/yashgupta/.android/avd`.
Verified in this step: with the variable set, the emulator lists `helios35`.

Also part of the build step's setup, not this step's (it touches `android/`): write
`android/local.properties` with `sdk.dir=/opt/homebrew/share/android-commandlinetools`,
and add `org.gradle.java.home=/opt/homebrew/opt/openjdk@17` to `android/gradle.properties`.

## Network and third-party services

The app makes exactly two outbound calls, both already present in the PWA: Open-Meteo
forecast and Open-Meteo reverse geocoding. There is no analytics SDK, no crash
reporting, no advertising identifier, and no account or backend service in this design.
The build step must not add one to satisfy a design goal it does not have.

## Rollback

Nothing was installed, so there is nothing to roll back. If the build step does add a
dependency, record it here with its version, source, and reason before using it.

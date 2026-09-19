#!/bin/bash
# Helios skeleton launch verification.
#
# Builds the debug APK, installs it on a running helios35 emulator, launches
# com.helios.app/.MainActivity, checks that the process survives 30 s with no
# fatal log entry, and writes the evidence into .mobile-work/evidence/.
#
# Prerequisites (see runbook.md): a JDK 17, the Android SDK, an emulator booted
# from the writable AVD copy. Run from the repository root.
set -eu

REPO=$(cd "$(dirname "$0")/.." && pwd)
SDK=${ANDROID_HOME:-${ANDROID_SDK_ROOT:-/opt/homebrew/share/android-commandlinetools}}
ADB=$SDK/platform-tools/adb
EVIDENCE=$REPO/.mobile-work/evidence
mkdir -p "$EVIDENCE"
# Keep a transcript of this run as evidence, and still print it here.
exec > >(tee "$EVIDENCE/launch-30s.txt") 2>&1
set -x

# The default Gradle and Android user homes may be read-only in a sandbox.
# android/gradlew already falls back to a writable Gradle home; adb and the
# emulator must share one key, so give both the same ANDROID_USER_HOME.
if [ -z "${ANDROID_USER_HOME:-}" ]; then
    ANDROID_USER_HOME=${TMPDIR:-/tmp}/helios-user-home
    export ANDROID_USER_HOME
fi
mkdir -p "$ANDROID_USER_HOME"
if [ ! -f "$ANDROID_USER_HOME/adbkey" ] && [ -f "${HOME:-}/.android/adbkey" ]; then
    cp "${HOME:-}/.android/adbkey" "${HOME:-}/.android/adbkey.pub" "$ANDROID_USER_HOME/"
fi
export ANDROID_HOME=$SDK
export ANDROID_SDK_ROOT=$SDK

# 1. Build.
(cd "$REPO/android" && ./gradlew :app:assembleDebug)
APK=$REPO/android/app/build/outputs/apk/debug/app-debug.apk

# 2. Require a booted device.
"$ADB" devices | grep -q "emulator-.*device$" \
    || { echo "No emulator device. Boot one with the commands in runbook.md." >&2; exit 1; }
"$ADB" wait-for-device
while [ "$("$ADB" shell getprop sys.boot_completed | tr -d '\r')" != "1" ]; do sleep 2; done

# 3. Install. A package left by another session was signed with a different
#    debug keystore, so fall back to a clean install when -r is rejected.
"$ADB" install -r -t "$APK" || {
    "$ADB" uninstall com.helios.app
    "$ADB" install -t "$APK"
}

# The app asks for the location permission on its first run, which would leave
# the system dialog as the top activity. Grant it up front so the launch under
# test is the app itself; close any dialog left over by an earlier run, which
# otherwise makes "am start" report "delivered to currently running top-most
# instance" instead of starting the app.
"$ADB" shell pm grant com.helios.app android.permission.ACCESS_FINE_LOCATION 2>/dev/null || true
"$ADB" shell pm grant com.helios.app android.permission.ACCESS_COARSE_LOCATION 2>/dev/null || true
"$ADB" shell am force-stop com.google.android.permissioncontroller 2>/dev/null || true

# 4. Launch and observe for 30 s.
"$ADB" shell am force-stop com.helios.app
"$ADB" logcat -b all -c
"$ADB" shell am start -W -n com.helios.app/.MainActivity
PID1=$("$ADB" shell pidof com.helios.app | tr -d '\r')
[ -n "$PID1" ] || { echo "com.helios.app is not running after launch." >&2; exit 1; }
sleep 30
PID2=$("$ADB" shell pidof com.helios.app | tr -d '\r')
echo "pid at launch: $PID1, after 30 s: $PID2"
[ "$PID1" = "$PID2" ] || { echo "Process did not survive 30 s." >&2; exit 1; }

# 5. Fatal-log check over the whole window.
"$ADB" logcat -d -b all > "$EVIDENCE/logcat-full.log"
FATALS=$(grep -cE "FATAL EXCEPTION|E AndroidRuntime|am_crash|am_anr" "$EVIDENCE/logcat-full.log" || true)
echo "fatal log lines: $FATALS"
[ "$FATALS" = "0" ] || { echo "Fatal log entries found; see $EVIDENCE/logcat-full.log." >&2; exit 1; }

# 6. Evidence: app-only log window, screenshot, UI dump.
"$ADB" logcat -d -b all --pid="$PID2" > "$EVIDENCE/logcat-launch-30s.log"
"$ADB" exec-out screencap -p > "$EVIDENCE/skeleton-launch.png"
"$ADB" shell uiautomator dump /sdcard/ui.xml > /dev/null
"$ADB" shell cat /sdcard/ui.xml > "$EVIDENCE/ui-hierarchy.xml"
rm -f "$EVIDENCE/logcat-full.log"
echo "Evidence written to $EVIDENCE"

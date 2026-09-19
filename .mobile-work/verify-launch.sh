#!/bin/sh
# Helios skeleton launch verification.
#
# Builds the debug APK, boots the helios35 emulator, installs, launches
# com.helios.app/.MainActivity, checks that the process survives 30 s with no
# fatal log entry, and writes the evidence into .mobile-work/evidence/.
#
# Prerequisites (see runbook.md): a JDK 17, the Android SDK, and the AVD copy
# described in the runbook. Run from the repository root.
set -eu

REPO=$(cd "$(dirname "$0")/.." && pwd)
SDK=${ANDROID_HOME:-${ANDROID_SDK_ROOT:-/opt/homebrew/share/android-commandlinetools}}
ADB=$SDK/platform-tools/adb
EMULATOR=$SDK/emulator/emulator
EVIDENCE=$REPO/.mobile-work/evidence

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

export ANDROID_AVD_HOME=${ANDROID_AVD_HOME:-${TMPDIR:-/tmp}/helios-avd}
export ANDROID_HOME=$SDK
export ANDROID_SDK_ROOT=$SDK

# 1. Build.
(cd "$REPO/android" && ./gradlew :app:assembleDebug)
APK=$REPO/android/app/build/outputs/apk/debug/app-debug.apk

# 2. Boot the emulator (serialize with any other session on this machine).
if ! "$ADB" devices | grep -q "emulator-.*device$"; then
    echo "No emulator device; start one with the commands in runbook.md." >&2
    exit 1
fi
"$ADB" wait-for-device
while [ "$("$ADB" shell getprop sys.boot_completed | tr -d '\r')" != "1" ]; do sleep 2; done

# 3. Install. A package left by another session uses a different debug
#    keystore, so replace it when the signature matches nothing we can sign with.
"$ADB" install -r -t "$APK" \
    || { "$ADB" uninstall com.helios.app; "$ADB" install -t "$APK"; }

# 4. Launch and observe for 30 s.
mkdir -p "$EVIDENCE"
"$ADB" shell am force-stop com.helios.app
"$ADB" logcat -b all -c
"$ADB" shell am start -W -n com.helios.app/.MainActivity
PID1=$("$ADB" shell pidof com.helios.app | tr -d '\r')
sleep 30
PID2=$("$ADB" shell pidof com.helios.app | tr -d '\r')
echo "pid at launch: $PID1, after 30 s: $PID2"
[ "$PID1" = "$PID2" ] || { echo "Process did not survive 30 s." >&2; exit 1; }
"$ADB" logcat -d -b all > "$EVIDENCE/logcat-full.log"
FATALS=$(grep -cE "FATAL EXCEPTION|E AndroidRuntime|am_crash|am_anr" "$EVIDENCE/logcat-full.log" || true)
echo "fatal log lines: $FATALS"
[ "$FATALS" = "0" ] || { echo "Fatal log entries found." >&2; exit 1; }

# 5. Evidence: app-only log window, screenshot, UI dump.
"$ADB" logcat -d -b all --pid="$PID2" > "$EVIDENCE/logcat-launch-30s.log"
"$ADB" exec-out screencap -p > "$EVIDENCE/skeleton-launch.png"
"$ADB" shell uiautomator dump /sdcard/ui.xml > /dev/null
"$ADB" shell cat /sdcard/ui.xml > "$EVIDENCE/ui-hierarchy.xml"
rm -f "$EVIDENCE/logcat-full.log"
echo "Evidence written to $EVIDENCE"

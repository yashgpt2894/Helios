#!/usr/bin/env bash
# Capture the debug component gallery, one screenshot per page.
#
# Requires: a booted helios35 emulator (see boot-emulator.sh), a built debug APK, and the
# shared lock /tmp/helios-emulator.lock held by this process tree.
#
# Writes design/captures/gallery-page-NN.png and design/captures/gallery-capture.txt.
set -euo pipefail

export JAVA_HOME=/opt/homebrew/opt/openjdk@17
export ANDROID_HOME=/opt/homebrew/share/android-commandlinetools
export ANDROID_SDK_ROOT="$ANDROID_HOME"
export PATH="$JAVA_HOME/bin:$ANDROID_HOME/platform-tools:$ANDROID_HOME/emulator:$PATH"
export ANDROID_USER_HOME=/tmp/helios-user-home

ROOT="/Users/yashgupta/Desktop/yash/code/Helios/.infy/worktrees/quest-23d2e03d"
ADB="$ANDROID_HOME/platform-tools/adb"
PKG="com.helios.app"
ACTIVITY="$PKG/com.helios.debug.gallery.GalleryActivity"
APK="$ROOT/android/app/build/outputs/apk/debug/app-debug.apk"
OUT="$ROOT/design/captures"
LOG="$OUT/gallery-capture.txt"

SCENARIO="${SCENARIO:-LIVE}"
DARK="${DARK:-true}"
TAG="${TAG:-}"
MAX_PAGES="${MAX_PAGES:-60}"

mkdir -p "$OUT"
: > "$LOG"

echo "install $(basename "$APK")" | tee -a "$LOG"
"$ADB" install -r -t "$APK" 2>&1 | tail -n 2 | tee -a "$LOG" || {
  "$ADB" uninstall "$PKG" >/dev/null 2>&1 || true
  "$ADB" install -t "$APK" 2>&1 | tail -n 2 | tee -a "$LOG"
}

page_count() {
  "$ADB" shell am start -n "$ACTIVITY" --ei page 0 --ez dark "$DARK" --es scenario "$SCENARIO" >/dev/null
  sleep 3
  "$ADB" shell uiautomator dump /sdcard/gallery.xml >/dev/null 2>&1 || true
  "$ADB" shell cat /sdcard/gallery.xml 2>/dev/null \
    | grep -o 'Page 1 of [0-9]\+' | head -n 1 | grep -o '[0-9]\+$' || true
}

COUNT="$(page_count)"
if [ -z "$COUNT" ]; then
  echo "could not read the page count from the UI dump; set PAGES explicitly" | tee -a "$LOG"
  COUNT="${PAGES:-0}"
fi
if [ "$COUNT" -le 0 ]; then
  echo "no pages to capture" | tee -a "$LOG"
  exit 2
fi
if [ "$COUNT" -gt "$MAX_PAGES" ]; then
  COUNT="$MAX_PAGES"
fi
echo "pages: $COUNT (scenario $SCENARIO, dark $DARK)" | tee -a "$LOG"
echo "$COUNT" > "$OUT/.page-count$TAG"

for PAGE in $(seq 0 $((COUNT - 1))); do
  "$ADB" shell am force-stop "$PKG" >/dev/null
  "$ADB" shell am start -W -n "$ACTIVITY" --ei page "$PAGE" --ez dark "$DARK" --es scenario "$SCENARIO" >/dev/null
  sleep 3
  TARGET="$OUT/gallery-page$(printf '%02d' "$PAGE")$TAG.png"
  "$ADB" exec-out screencap -p > "$TARGET"
  echo "captured $TARGET ($(stat -f%z "$TARGET") bytes)" | tee -a "$LOG"
done

echo "capture complete" | tee -a "$LOG"

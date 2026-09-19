#!/usr/bin/env bash
# Boot the helios35 emulator under the shared /tmp/helios-emulator.lock discipline.
# Writes nothing outside /tmp, never touches the shared read-only AVD directory.
set -euo pipefail

export JAVA_HOME=/opt/homebrew/opt/openjdk@17
export ANDROID_HOME=/opt/homebrew/share/android-commandlinetools
export ANDROID_SDK_ROOT="$ANDROID_HOME"
export PATH="$JAVA_HOME/bin:$ANDROID_HOME/platform-tools:$ANDROID_HOME/emulator:$PATH"
export ANDROID_AVD_HOME=/tmp/helios-avd
export ANDROID_USER_HOME=/tmp/helios-user-home

ADB="$ANDROID_HOME/platform-tools/adb"

if [ -f /tmp/helios-emulator.lock ] && kill -0 "$(cat /tmp/helios-emulator.lock)" 2>/dev/null; then
  OWNER="$(cat /tmp/helios-emulator.lock)"
  if [ "$OWNER" != "$$" ]; then
    echo "lock held by live pid $OWNER; refusing to boot a second emulator"
    exit 3
  fi
fi
echo $$ > /tmp/helios-emulator.lock

if "$ADB" devices | grep -q "emulator-.*device$"; then
  echo "emulator already attached; reusing it"
else
  rm -rf /tmp/helios-avd
  cp -cR /Users/yashgupta/.android/avd/ /tmp/helios-avd/
  sed -i '' 's|^path=.*|path=/tmp/helios-avd/helios35.avd|' /tmp/helios-avd/helios35.ini
  rm -f /tmp/helios-avd/helios35.avd/hardware-qemu.ini \
        /tmp/helios-avd/helios35.avd/emu-launch-params.txt \
        /tmp/helios-avd/helios35.avd/version_num.cache \
        /tmp/helios-avd/helios35.avd/hardware-qemu.ini.lock \
        /tmp/helios-avd/helios35.avd/multiinstance.lock \
        /tmp/helios-avd/helios35.avd/read-snapshot.txt

  mkdir -p "$ANDROID_USER_HOME"
  cp -f /Users/yashgupta/.android/adbkey /Users/yashgupta/.android/adbkey.pub "$ANDROID_USER_HOME/" 2>/dev/null || true
  chmod 600 "$ANDROID_USER_HOME/adbkey" 2>/dev/null || true
  "$ADB" kill-server >/dev/null 2>&1 || true
  "$ADB" start-server >/dev/null

  "$ANDROID_HOME/emulator/emulator" -avd helios35 -no-snapshot-load -no-boot-anim \
      -gpu swiftshader_indirect -no-audio -no-window -no-metrics >/tmp/helios-emulator.log 2>&1 &
  echo "emulator pid $!"
fi

"$ADB" wait-for-device
for _ in $(seq 1 200); do
  if [ "$("$ADB" shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')" = "1" ]; then
    echo "boot completed"
    "$ADB" devices
    "$ADB" shell wm size
    exit 0
  fi
  sleep 3
done

echo "emulator did not finish booting in time"
exit 4

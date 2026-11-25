#!/usr/bin/env bash
# Jump a running sleep-mode session forward so each phase can be checked without waiting.
# Debug builds only (uses run-as). Targets the emulator by default — never the Pixel 8 Pro research device.
#
#   tools/fast-forward.sh <minutes-ago> [extra-seconds] [youtube]
#
# Example with a 15-min session: 6 → phase 1 banner, 10 30 → phase 2 dim, 13 → phase 3 wind-down, 16 → session ends.
set -euo pipefail
export MSYS_NO_PATHCONV=1
SERIAL="${SERIAL:-emulator-5554}"
ADB="${ADB:-adb} -s $SERIAL"
P=com.sleepscroll.app
F=shared_prefs/sleep_scroll.xml

$ADB shell am force-stop $P
NOW=$($ADB shell date +%s | tr -d '\r')
T=$(( (NOW - $1*60 - ${2:-0}) * 1000 ))
$ADB shell "run-as $P sed -i 's/name=\"sleep_start\" value=\"[0-9]*\"/name=\"sleep_start\" value=\"$T\"/' $F"
# Opening the app resumes the session (SleepScrollApp → SleepController.resume).
$ADB shell monkey -p $P -c android.intent.category.LAUNCHER 1 >/dev/null 2>&1
$ADB shell sleep 2
if [ "${3:-youtube}" = youtube ]; then
  # Start WatchWhileActivity directly: YouTube's launcher trampoline is unreliable right after another app launch.
  $ADB shell input keyevent HOME
  $ADB shell am start -n com.google.android.youtube/com.google.android.apps.youtube.app.watchwhile.WatchWhileActivity >/dev/null
fi

# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Product Vision

**sleep-scroll** is a smart sleep controller for social media — not a new platform, not a blunt app blocker.

The core insight: **blocking apps cold-turkey doesn't work**. Instead, sleep-scroll gradually steers the user toward sleep when they've activated Sleep Mode. It works *around* existing apps (TikTok, Instagram, YouTube, etc.) rather than replacing them.

### The Sleep Mode Experience

When a user activates Sleep Mode (e.g., 30-minute wind-down):

| Window | Behavior |
|---|---|
| 0–33% | Normal — no interruption |
| 33–67% | Soft reminder banner: *"Sleep mode starting soon"* |
| 67–83% | Dim overlay + snooze button (or no snooze in strict mode) |
| 83–100% | Full-screen wind-down overlay (personalised style) |

### What It Does NOT Do

- Does not build a new social network or content feed
- Does not block apps instantly (that's Freedom/StayFocused — users bypass those)
- Does not rely primarily on blue light filtering (research shows emotional arousal matters more)

### Why This Approach

Research finding: **check frequency and emotional investment predict poor sleep more than raw screen time.** Hard blocks fail because users override them. Gradual friction + replacement behavior (wind-down ritual) is what the evidence supports. See `research.md` → Section 6 for the full evidence.

---

## Platform Targets

**Both mobile (Android + iOS) and browser extension.**

### Recommended Build Order
1. **Browser extension** ✅ — built and working
2. **Android app** ✅ — built (`android/`), builds + unit tests pass; full flow verified on an Android 14 emulator (not yet on a real phone — see "Android app" below)
3. **iOS app** — second mobile target once Android approach is proven

### Android (highest capability)
- Accessibility Services API — detect foreground app
- `UsageStatsManager` — per-app screen time
- System overlay (`TYPE_APPLICATION_OVERLAY`) — draw on top of any app
- `DevicePolicyManager` — lock/restrict apps

### iOS (more restricted)
- Screen Time API (`FamilyControls` / `DeviceActivityMonitor`)
- App Intents + Focus Modes — hook into Sleep Focus
- `ManagedSettings` — restrict apps within Screen Time

---

## Design Principles

Derived from `research.md` — these must guide every product decision:

1. **Gradual, not sudden.** Hard stops get bypassed. Slope the experience toward sleep.
2. **Replace, don't remove.** Always offer a wind-down alternative when closing an app.
3. **Target emotional engagement, not screen time.** The goal is calm, not fewer minutes.
4. **Friction must be calibrated.** Too light = ignored. Too heavy = uninstalled.
5. **No guilt.** Doomscrolling already causes shame. The product should feel supportive, not punitive.
6. **Time-specific, not all-day.** This is a nighttime tool. Don't restrict daytime behavior.
7. **Make the invisible visible.** Users lose track of time while scrolling. Ambient awareness breaks the trance without jarring interruptions.

---

## Commands

### Load the extension (Chrome / Edge)
1. Open `chrome://extensions`
2. Enable **Developer mode** (top-right toggle)
3. **Load unpacked** → select the `extension/` folder

### Reload after changes
- **Background script**: click ↺ on the extension card in `chrome://extensions`
- **Content script / CSS**: reload the social media tab
- **Popup**: just re-open it
- **Options / Analytics pages**: reload the open tab

### Package for the Chrome Web Store
`bash store/package-extension.sh` → `dist/sleep-scroll-extension-<manifest version>.zip` (gitignored). Listing text and checklist: `store/listing.md`. Privacy policy is served by GitHub Pages at https://umetatola.github.io/sleep-scroll/store/privacy-policy.

### Debug
- **Background logs**: click "Service Worker" on the extension card
- **Content script logs**: DevTools console on the target tab
- **Popup logs**: right-click popup → Inspect

---

## Architecture

### Extension structure (`extension/`)

Manifest V3, no build tooling — vanilla JS loaded directly by the browser.

| File | Role |
|---|---|
| `manifest.json` | Permissions, host patterns, entry points, options_ui |
| `background.js` | Service worker: state, alarms (sleep-tick + schedule-alarm), tab messaging, session recording |
| `content.js` | Phase overlay rendering injected into social media tabs; respects mode + targetSites |
| `content.css` | All injected styles (prefixed `ss-` to avoid host-page collisions) |
| `popup/popup.{html,js,css}` | Popup: start/stop, countdown, schedule toggle, analytics + settings links |
| `options/options.{html,js,css}` | Settings page: target sites checkboxes, normal/strict intensity mode |
| `analytics/analytics.{html,js,css}` | Analytics page: streak (not broken until tonight is missed), 7-day grid, recent sessions |
| `icons/` | `icon{16,32,48,128}.png` are committed; regenerate with `node extension/icons/make-icons.js` (same drawing as `generate-icons.html`) |

### State (`chrome.storage.local`)

```js
sleepMode: {
  active: boolean,
  startTime: number,       // Date.now() ms
  duration: number,        // minutes (15 / 30 / 45 / 60)
  phase: 0 | 1 | 2 | 3
}
windDownStyle: 'breathing' | 'reflection' | 'journaling' | 'sounds' | 'silence'
schedule: {
  enabled: boolean,
  time: string,            // "HH:MM" 24h format
  duration: number         // minutes
}
mode: 'normal' | 'strict'  // strict = no dismiss, no snooze
targetSites: string[]      // keys: ['tiktok','instagram','youtube','twitter','facebook','reddit']
sessions: Array<{
  date: string,            // "YYYY-MM-DD", local timezone of startTime
  startTime: number,
  endTime: number,
  duration: number,        // configured minutes
  completed: boolean       // true = ran to end, false = manually stopped
}>                         // capped at 60 entries
```

### Data flow

```
popup.js  →  START_SLEEP_MODE  →  background.js
                                    sets storage + creates 'sleep-tick' alarm (every 30s)
                                    alarm fires → broadcastPhase()
                                      → SLEEP_MODE_UPDATE → content.js (all matching tabs)
                                                             renders phase overlay

schedule-alarm (daily at set time) → background.js → triggerScheduledSleep()
                                                        → same flow as START_SLEEP_MODE

session ends (natural or manual) → recordSession() → sessions[] in storage
                                                       → analytics page reads this
```

### Phase thresholds (fraction of total duration)

| Phase | Threshold | Content script behavior |
|---|---|---|
| 0 | 0–33% | Silent |
| 1 | 33–67% | Top banner: "Sleep mode starting soon" + dismiss (normal mode only) |
| 2 | 67–83% | Dim overlay + snooze button (normal mode only) |
| 3 | 83–100% | Full-screen wind-down overlay (style from `windDownStyle`) |

### Wind-down styles (phase 3)

| Style | Content |
|---|---|
| `breathing` | Animated breathing ring + "Breathe in / Hold / Breathe out" cycle |
| `reflection` | Rotating gratitude/reflection prompts every 7s |
| `journaling` | Day-of-week nightly journal question |
| `sounds` | Prompt to put on calming audio |
| `silence` | Dark screen, moon icon, no animation |

### Target sites
Declared in `manifest.json` (host_permissions + content_scripts) and `background.js` (ALL_SITES map). User can disable individual sites via the Settings page — stored as `targetSites` array. Content scripts check their own hostname against the enabled list.

### Scheduled sleep mode
Uses `chrome.alarms.create('schedule-alarm', { when: nextOccurrence(time), periodInMinutes: 1440 })`. Chrome alarms persist across browser restarts — no re-registration needed on startup. If sleep mode is already active when the alarm fires, the trigger is ignored.

---

## Android app (`android/`)

Kotlin + Jetpack Compose, minSdk 26 / target 34, no network permission. Same phases, wind-down styles, snooze/strict modes, schedule and session history as the extension.

### Build / test
```
cd android
# needs JDK 17 and the Android SDK (platform 34); put sdk.dir in android/local.properties (gitignored)
./gradlew testDebugUnitTest lintDebug assembleDebug     # debug APK: app/build/outputs/apk/debug/
./gradlew assembleRelease                               # minified, unsigned
```
On this Windows machine: `JAVA_HOME=C:\Users\tola\android-build\jdk17\jdk17.0.18_9`, SDK at `C:\Users\tola\android-sdk`. In `local.properties` write `sdk.dir=C\:/Users/tola/android-sdk` (single backslashes are parsed as escapes and break the build).

### Layout (`app/src/main/java/com/sleepscroll/app/`)
| Package | Role |
|---|---|
| `core/` | Pure Kotlin, JVM-tested: models, `SleepLogic` (phases, next-alarm math, streak/analytics), wind-down text |
| `data/` | `SleepRepository` (SharedPreferences → StateFlows, single source of truth), `Permissions` |
| `service/` | `SleepService` (foreground, 2s tick), `ForegroundAppDetector` (UsageStatsManager events), `SleepController` (start/stop/snooze/finish — the only place sessions are recorded), `Notifications` |
| `overlay/` | `OverlayController` — plain-View overlay windows (banner / dim+snooze / wind-down). No Compose here: overlay windows have no lifecycle owner |
| `schedule/` | `Scheduler` (exact alarm, re-armed after each fire), `ScheduleReceiver`, `BootReceiver` |
| `ui/` | Compose: Tonight (start/active/schedule/setup), Progress, Settings |

### How it works
Start → `SleepController.start` saves state and launches `SleepService`. Each tick: compute phase from `startTime`/`duration`, poll the foreground package, and if it is a selected app (and phase > 0, overlay permission held) show the matching overlay; otherwise clear it. Snooze adds 5 min to `duration` (phase can drop back). At 0 remaining the session is recorded as completed and a "Good night" notification posts.

### Deliberate differences from the extension
- Overlays also have a **"Leave this app / Go to home screen"** button (replace, don't just remove) — strict mode only removes dismiss/snooze.
- (Streak grace and local-timezone dates used to be Android-only; the extension now does the same.)

### Emulator testing
**Never install this app on the Pixel 8 Pro (serial `37131FDJG003M9`) — it's a research device.** Always pass `-s emulator-5554`.

AVD `sleepscroll_api34` (Android 14, Google Play image, YouTube preinstalled) lives in the SDK above:
```
C:\Users\tola\android-sdk\emulator\emulator.exe -avd sleepscroll_api34 -no-snapshot-save -no-boot-anim
adb -s emulator-5554 install -r android/app/build/outputs/apk/debug/app-debug.apk
adb -s emulator-5554 shell appops set com.sleepscroll.app GET_USAGE_STATS allow
adb -s emulator-5554 shell appops set com.sleepscroll.app SYSTEM_ALERT_WINDOW allow
adb -s emulator-5554 shell pm grant com.sleepscroll.app android.permission.POST_NOTIFICATIONS
```
Start a 15-min session in the app, then `android/tools/fast-forward.sh <minutes-ago>` jumps to any phase and opens YouTube. In Git Bash set `MSYS_NO_PATHCONV=1` for adb commands with `/sdcard/...` paths.

**Verified on the emulator (2026-11-25):** setup card hides once permissions are granted (on resume); foreground detection of YouTube; phase 1 banner, phase 2 dim + snooze (+5 min drops back to phase 1), phase 3 breathing wind-down; "Go to home screen"; session resume after process death; session end → completed session recorded, "Good night" notification, service stopped, Progress tab streak.

Fixed during that pass: phase 2 scrim was too light over white apps (now `#F0…`), phase 3 is fully opaque, outlined buttons have a solid fill, and the overlay timer kept losing its "Closing in" prefix after the first tick.

### Still needs a real phone
Usage-access foreground detection per OEM, exact-alarm start from the background on Android 12–14, foreground-service start from `ScheduleReceiver`/`BootReceiver`, battery/Doze behaviour overnight. Play Store release will need justification text for `PACKAGE_USAGE_STATS`, `SYSTEM_ALERT_WINDOW` and the `specialUse` FGS type, plus a signing config (none is set up).


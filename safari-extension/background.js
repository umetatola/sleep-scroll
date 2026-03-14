// background.js — service worker
// Manages sleep mode state, phase transitions, scheduled activation, and tab messaging.

const ALL_SITES = {
  tiktok:    ['tiktok.com'],
  instagram: ['instagram.com'],
  youtube:   ['youtube.com'],
  twitter:   ['twitter.com', 'x.com'],
  facebook:  ['facebook.com'],
  reddit:    ['reddit.com'],
};

// Phase thresholds as a fraction of total duration:
//   Phase 0 (0–33%):   Normal — content script is silent
//   Phase 1 (33–67%):  Soft reminder banner
//   Phase 2 (67–83%):  Dim overlay — time to wrap up
//   Phase 3 (83–100%): Full wind-down overlay
const PHASE_THRESHOLDS = [0, 0.33, 0.67, 0.83];

function isSocialMedia(url) {
  if (!url) return false;
  return Object.values(ALL_SITES).flat().some(domain => url.includes(domain));
}

function computePhase(startTime, durationMinutes) {
  const elapsed = Date.now() - startTime;
  const total = durationMinutes * 60 * 1000;
  const progress = elapsed / total;
  if (progress >= PHASE_THRESHOLDS[3]) return 3;
  if (progress >= PHASE_THRESHOLDS[2]) return 2;
  if (progress >= PHASE_THRESHOLDS[1]) return 1;
  return 0;
}

// Returns ms timestamp of the next occurrence of a HH:MM time string
function nextOccurrence(timeStr) {
  const [h, m] = timeStr.split(':').map(Number);
  const next = new Date();
  next.setHours(h, m, 0, 0);
  if (next <= new Date()) next.setDate(next.getDate() + 1);
  return next.getTime();
}

async function broadcastPhase() {
  const data = await browser.storage.local.get(['sleepMode', 'windDownStyle', 'mode', 'targetSites']);
  const { sleepMode, windDownStyle = 'breathing', mode = 'normal' } = data;
  const targetSites = data.targetSites || Object.keys(ALL_SITES);

  if (!sleepMode?.active) return;

  const total = sleepMode.duration * 60 * 1000;
  const timeRemaining = Math.max(0, sleepMode.startTime + total - Date.now());

  if (timeRemaining === 0) {
    await recordSession(sleepMode.startTime, sleepMode.duration, true);
    await browser.storage.local.set({ sleepMode: { active: false } });
    browser.alarms.clear('sleep-tick');
    broadcastClear();
    return;
  }

  const phase = computePhase(sleepMode.startTime, sleepMode.duration);
  if (phase !== sleepMode.phase) {
    await browser.storage.local.set({ sleepMode: { ...sleepMode, phase } });
  }

  // Best-effort tab broadcast — may not have URLs on iOS, content scripts use polling as fallback
  try {
    const tabs = await browser.tabs.query({});
    for (const tab of tabs) {
      if (!isSocialMedia(tab.url)) continue;
      browser.tabs.sendMessage(tab.id, {
        type: 'SLEEP_MODE_UPDATE',
        phase,
        timeRemaining,
        windDownStyle,
        mode,
        targetSites,
      }).catch(() => {});
    }
  } catch (_) {}
}

async function recordSession(startTime, durationMinutes, completed) {
  const { sessions = [] } = await browser.storage.local.get('sessions');
  sessions.push({
    date:      new Date(startTime).toISOString().split('T')[0],
    startTime,
    endTime:   Date.now(),
    duration:  durationMinutes,
    completed,
  });
  await browser.storage.local.set({ sessions: sessions.slice(-60) }); // keep last 60
}

async function triggerScheduledSleep() {
  const { schedule, sleepMode } = await browser.storage.local.get(['schedule', 'sleepMode']);
  if (!schedule?.enabled || sleepMode?.active) return;
  await browser.storage.local.set({
    sleepMode: { active: true, startTime: Date.now(), duration: schedule.duration || 30, phase: 0 }
  });
  browser.alarms.create('sleep-tick', { periodInMinutes: 0.5 });
  broadcastPhase();
}

function broadcastClear() {
  browser.tabs.query({}).then((tabs) => {
    for (const tab of tabs) {
      if (!isSocialMedia(tab.url)) continue;
      browser.tabs.sendMessage(tab.id, { type: 'SLEEP_MODE_CLEAR' }).catch(() => {});
    }
  }).catch(() => {});
}

browser.alarms.onAlarm.addListener((alarm) => {
  if (alarm.name === 'sleep-tick') broadcastPhase();
  if (alarm.name === 'schedule-alarm') triggerScheduledSleep();
});

browser.runtime.onMessage.addListener((message, _sender, sendResponse) => {
  if (message.type === 'START_SLEEP_MODE') {
    browser.storage.local.set({
      sleepMode: { active: true, startTime: Date.now(), duration: message.duration, phase: 0 }
    }).then(() => {
      browser.alarms.create('sleep-tick', { periodInMinutes: 0.5 });
      broadcastPhase();
      sendResponse({ ok: true });
    });
    return true;
  }

  if (message.type === 'STOP_SLEEP_MODE') {
    browser.storage.local.get('sleepMode').then(({ sleepMode }) => {
      const start = sleepMode?.startTime ?? Date.now();
      const dur   = sleepMode?.duration ?? 30;
      recordSession(start, dur, false).then(() => {
        browser.storage.local.set({ sleepMode: { active: false } }).then(() => {
          browser.alarms.clear('sleep-tick');
          broadcastClear();
          sendResponse({ ok: true });
        });
      });
    });
    return true;
  }

  if (message.type === 'SNOOZE') {
    browser.storage.local.get('sleepMode').then(({ sleepMode }) => {
      if (!sleepMode?.active) return;
      browser.storage.local.set({
        sleepMode: { ...sleepMode, duration: sleepMode.duration + message.minutes }
      });
    });
  }

  if (message.type === 'SET_SCHEDULE') {
    const { enabled, time, duration } = message;
    browser.storage.local.set({ schedule: { enabled, time, duration } }).then(() => {
      browser.alarms.clear('schedule-alarm');
      if (enabled) {
        browser.alarms.create('schedule-alarm', {
          when: nextOccurrence(time),
          periodInMinutes: 1440, // repeat every 24 hours
        });
      }
      sendResponse({ ok: true });
    });
    return true;
  }

  if (message.type === 'GET_SLEEP_MODE') {
    browser.storage.local.get(['sleepMode', 'windDownStyle', 'schedule', 'mode', 'targetSites']).then((data) => {
      sendResponse({
        ...(data.sleepMode || { active: false }),
        windDownStyle: data.windDownStyle || 'breathing',
        schedule: data.schedule || { enabled: false, time: '22:30', duration: 30 },
        mode: data.mode || 'normal',
        targetSites: data.targetSites || Object.keys(ALL_SITES),
      });
    });
    return true;
  }
});

browser.runtime.onInstalled.addListener(() => {
  browser.storage.local.set({ sleepMode: { active: false } });
});

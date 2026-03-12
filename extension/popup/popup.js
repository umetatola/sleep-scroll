const PHASE_LABELS = ['Monitoring', 'Reminder', 'Dimming', 'Wind-down'];
const PHASE_DESCS = [
  'Scrolling is allowed normally.',
  'A gentle reminder is showing on social media tabs.',
  'Screen is dimmed. Time to wrap up.',
  'Wind-down mode active. Almost time to sleep.',
];
const RING_COLORS = ['#6030a0', '#b07020', '#b04030', '#208060'];

const STYLE_NAMES = {
  breathing:  'Breathing guide',
  reflection: 'Reflection',
  journaling: 'Journal prompt',
  sounds:     'Ambient sounds',
  silence:    'Silence',
};

let selectedDuration = 30;
let tickId = null;

// ── Onboarding ────────────────────────────────────────────────────────────────

document.querySelectorAll('.style-btn').forEach((btn) => {
  btn.addEventListener('click', () => {
    const style = btn.dataset.style;
    chrome.storage.local.set({ windDownStyle: style }, () => {
      updateStyleIndicator(style);
      showInactive();
    });
  });
});

document.getElementById('btn-change-style').addEventListener('click', () => {
  showOnboarding();
});

function updateStyleIndicator(style) {
  document.getElementById('style-current').textContent = STYLE_NAMES[style] ?? style;
}

// ── Duration selector ─────────────────────────────────────────────────────────

document.querySelectorAll('.dur-btn').forEach((btn) => {
  btn.addEventListener('click', () => {
    document.querySelectorAll('.dur-btn').forEach((b) => b.classList.remove('active'));
    btn.classList.add('active');
    selectedDuration = parseInt(btn.dataset.min, 10);
    // Keep schedule duration in sync if schedule is enabled
    if (document.getElementById('schedule-enabled').checked) saveSchedule();
  });
});

// ── Schedule ──────────────────────────────────────────────────────────────────

const scheduleEnabled = document.getElementById('schedule-enabled');
const scheduleTime    = document.getElementById('schedule-time');

function saveSchedule() {
  chrome.runtime.sendMessage({
    type: 'SET_SCHEDULE',
    enabled:  scheduleEnabled.checked,
    time:     scheduleTime.value,
    duration: selectedDuration,
  });
}

scheduleEnabled.addEventListener('change', () => {
  scheduleTime.disabled = !scheduleEnabled.checked;
  saveSchedule();
});

scheduleTime.addEventListener('change', saveSchedule);

// ── Settings ──────────────────────────────────────────────────────────────────

document.getElementById('btn-settings').addEventListener('click', () => {
  chrome.runtime.openOptionsPage();
});

document.getElementById('btn-analytics').addEventListener('click', () => {
  chrome.tabs.create({ url: chrome.runtime.getURL('analytics/analytics.html') });
});

// ── Start / Stop ──────────────────────────────────────────────────────────────

document.getElementById('btn-start').addEventListener('click', () => {
  chrome.runtime.sendMessage({ type: 'START_SLEEP_MODE', duration: selectedDuration }, () => {
    showActive();
    startTick();
  });
});

document.getElementById('btn-stop').addEventListener('click', () => {
  chrome.runtime.sendMessage({ type: 'STOP_SLEEP_MODE' }, () => {
    showInactive();
    stopTick();
  });
});

// ── Active view ───────────────────────────────────────────────────────────────

function fmt(ms) {
  const s = Math.ceil(ms / 1000);
  return `${Math.floor(s / 60)}:${String(s % 60).padStart(2, '0')}`;
}

function paint(sleepMode) {
  const total = sleepMode.duration * 60 * 1000;
  const elapsed = Date.now() - sleepMode.startTime;
  const timeRemaining = Math.max(0, total - elapsed);
  const phase = sleepMode.phase ?? 0;
  const progress = Math.min(1, elapsed / total);

  document.getElementById('ring-time').textContent = fmt(timeRemaining);
  document.getElementById('ring-phase').textContent = PHASE_LABELS[phase];
  document.getElementById('phase-desc').textContent = PHASE_DESCS[phase];
  document.getElementById('progress-fill').style.width = `${progress * 100}%`;
  document.getElementById('ring').style.borderColor = RING_COLORS[phase] + '99';
}

function startTick() {
  stopTick();
  tickId = setInterval(() => {
    chrome.runtime.sendMessage({ type: 'GET_SLEEP_MODE' }, (sm) => {
      if (!sm?.active) { showInactive(); stopTick(); return; }
      paint(sm);
    });
  }, 1000);
}

function stopTick() {
  if (tickId) { clearInterval(tickId); tickId = null; }
}

// ── View switchers ────────────────────────────────────────────────────────────

function showOnboarding() {
  document.getElementById('view-inactive').classList.add('hidden');
  document.getElementById('view-active').classList.add('hidden');
  document.getElementById('view-onboarding').classList.remove('hidden');
}

function showInactive() {
  document.getElementById('view-onboarding').classList.add('hidden');
  document.getElementById('view-active').classList.add('hidden');
  document.getElementById('view-inactive').classList.remove('hidden');
}

function showActive() {
  document.getElementById('view-onboarding').classList.add('hidden');
  document.getElementById('view-inactive').classList.add('hidden');
  document.getElementById('view-active').classList.remove('hidden');
}

// ── Init ──────────────────────────────────────────────────────────────────────

chrome.storage.local.get(['sleepMode', 'windDownStyle', 'schedule'], ({ sleepMode, windDownStyle, schedule }) => {
  if (!windDownStyle) {
    showOnboarding();
    return;
  }

  updateStyleIndicator(windDownStyle);

  // Restore schedule UI
  if (schedule) {
    scheduleEnabled.checked  = schedule.enabled || false;
    scheduleTime.value       = schedule.time || '22:30';
    scheduleTime.disabled    = !schedule.enabled;
    if (schedule.duration) {
      selectedDuration = schedule.duration;
      document.querySelectorAll('.dur-btn').forEach((b) => {
        b.classList.toggle('active', parseInt(b.dataset.min, 10) === selectedDuration);
      });
    }
  } else {
    scheduleTime.disabled = true;
  }

  if (sleepMode?.active) {
    showActive();
    paint(sleepMode);
    startTick();
  } else {
    showInactive();
  }
});

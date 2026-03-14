// content.js — injected into social media pages
// Renders phase-appropriate overlays without touching the page's own DOM/styles.
// Uses push messages from background + 30s polling as fallback (Safari service workers can be suspended).

let overlayEl = null;
let currentPhase = -1;
let cycleInterval = null;
let pollInterval = null;
let windDownStyle = 'breathing';
let mode = 'normal';      // 'normal' | 'strict'
let targetSites = null;   // null = not yet received; default to showing overlays

const ALL_SITE_DOMAINS = {
  tiktok:    ['tiktok.com'],
  instagram: ['instagram.com'],
  youtube:   ['youtube.com'],
  twitter:   ['twitter.com', 'x.com'],
  facebook:  ['facebook.com'],
  reddit:    ['reddit.com'],
};

function isSiteEnabled() {
  if (!targetSites) return true;
  const host = location.hostname;
  return targetSites.some(key =>
    (ALL_SITE_DOMAINS[key] || [key]).some(d => host.includes(d))
  );
}

const REFLECTION_PROMPTS = [
  'What are you grateful for tonight?',
  'Take a breath. You did enough today.',
  'What would you like to let go of?',
  'Rest. Tomorrow is a new beginning.',
  'You are worthy of good sleep.',
  'What brought you joy today?',
];

const JOURNAL_PROMPTS = [
  'What was one good thing about today?',
  'What are three things you\'re grateful for?',
  'What made you smile today?',
  'What\'s one thing you\'re proud of today?',
  'What do you want to let go of tonight?',
  'What was the best moment of this week?',
  'What\'s one intention for tomorrow?',
];

function fmt(ms) {
  const s = Math.ceil(ms / 1000);
  return `${Math.floor(s / 60)}:${String(s % 60).padStart(2, '0')}`;
}

function setTimer(ms) {
  const el = overlayEl?.querySelector('.ss-timer');
  if (el) el.textContent = fmt(ms);
}

// ── Phase renderers ──────────────────────────────────────────────────────────

function renderPhase0() { clearOverlay(); }

function renderPhase1(timeRemaining) {
  if (currentPhase === 1) { setTimer(timeRemaining); return; }
  clearOverlay();
  currentPhase = 1;

  overlayEl = document.createElement('div');
  overlayEl.className = 'ss-banner';
  overlayEl.innerHTML = `
    <span class="ss-icon">🌙</span>
    <span>Sleep mode starting soon —</span>
    <span class="ss-timer">${fmt(timeRemaining)}</span>
    ${mode === 'strict' ? '' : '<button class="ss-dismiss" aria-label="Dismiss">✕</button>'}
  `;
  if (mode !== 'strict') {
    overlayEl.querySelector('.ss-dismiss').onclick = () => { overlayEl?.remove(); overlayEl = null; };
  }
  document.body.appendChild(overlayEl);
}

function renderPhase2(timeRemaining) {
  if (currentPhase === 2) { setTimer(timeRemaining); return; }
  clearOverlay();
  currentPhase = 2;

  overlayEl = document.createElement('div');
  overlayEl.className = 'ss-overlay ss-dim';
  overlayEl.innerHTML = `
    <div class="ss-card">
      <div class="ss-moon">🌙</div>
      <p class="ss-heading">Time to wind down</p>
      <p class="ss-sub">Closing in <span class="ss-timer">${fmt(timeRemaining)}</span></p>
      ${mode === 'strict' ? '' : '<button class="ss-snooze-btn">Snooze 5 min</button>'}
    </div>
  `;
  if (mode !== 'strict') {
    overlayEl.querySelector('.ss-snooze-btn').onclick = () => {
      browser.runtime.sendMessage({ type: 'SNOOZE', minutes: 5 });
    };
  }
  document.body.appendChild(overlayEl);
}

function renderPhase3(timeRemaining, style) {
  if (currentPhase === 3) { setTimer(timeRemaining); return; }
  clearOverlay();
  currentPhase = 3;

  overlayEl = document.createElement('div');
  overlayEl.className = 'ss-overlay ss-block';
  overlayEl.innerHTML = `<div class="ss-card">${buildPhase3(style, timeRemaining)}</div>`;

  if (style === 'breathing') startBreathingCycle();
  if (style === 'reflection') startReflectionCycle();

  document.body.appendChild(overlayEl);
}

// ── Phase 3 content builders ─────────────────────────────────────────────────

function buildPhase3(style, timeRemaining) {
  const timer = `<p class="ss-sub">Closing in <span class="ss-timer">${fmt(timeRemaining)}</span></p>`;
  switch (style) {
    case 'breathing':
      return `
        <div class="ss-moon ss-moon-lg">🌙</div>
        <h2 class="ss-heading">Time to sleep</h2>
        ${timer}
        <div class="ss-breathe">
          <div class="ss-breathe-ring"></div>
          <p class="ss-breathe-label">Breathe in...</p>
        </div>`;
    case 'reflection':
      return `
        <div class="ss-moon ss-moon-lg">🙏</div>
        <h2 class="ss-heading">Time to sleep</h2>
        ${timer}
        <p class="ss-prompt">${REFLECTION_PROMPTS[0]}</p>`;
    case 'journaling':
      return `
        <div class="ss-moon ss-moon-lg">📓</div>
        <h2 class="ss-heading">Time to sleep</h2>
        ${timer}
        <p class="ss-prompt-label">Tonight's question</p>
        <p class="ss-prompt">${JOURNAL_PROMPTS[new Date().getDay()]}</p>`;
    case 'sounds':
      return `
        <div class="ss-moon ss-moon-lg">🎵</div>
        <h2 class="ss-heading">Time to sleep</h2>
        ${timer}
        <p class="ss-prompt">Put on some calming music or nature sounds, then close your eyes.</p>`;
    case 'silence':
    default:
      return `
        <div class="ss-moon ss-moon-lg">🌑</div>
        <h2 class="ss-heading">Time to sleep.</h2>
        ${timer}`;
  }
}

// ── Cycle helpers ────────────────────────────────────────────────────────────

function startBreathingCycle() {
  const steps = ['Breathe in...', 'Hold...', 'Breathe out...', 'Hold...'];
  let i = 0;
  cycleInterval = setInterval(() => {
    i = (i + 1) % steps.length;
    const label = overlayEl?.querySelector('.ss-breathe-label');
    if (label) label.textContent = steps[i];
  }, 2000);
}

function startReflectionCycle() {
  let i = 0;
  cycleInterval = setInterval(() => {
    i = (i + 1) % REFLECTION_PROMPTS.length;
    const el = overlayEl?.querySelector('.ss-prompt');
    if (el) {
      el.style.opacity = '0';
      setTimeout(() => {
        if (el) { el.textContent = REFLECTION_PROMPTS[i]; el.style.opacity = '1'; }
      }, 400);
    }
  }, 7000);
}

function clearOverlay() {
  if (cycleInterval) { clearInterval(cycleInterval); cycleInterval = null; }
  overlayEl?.remove();
  overlayEl = null;
  currentPhase = -1;
}

// ── Apply state from a GET_SLEEP_MODE response ────────────────────────────────

function applyState(sm) {
  if (!sm?.active) { clearOverlay(); return; }

  if (sm.windDownStyle) windDownStyle = sm.windDownStyle;
  if (sm.mode) mode = sm.mode;
  if (sm.targetSites) targetSites = sm.targetSites;

  if (!isSiteEnabled()) return;

  const total = sm.duration * 60 * 1000;
  const timeRemaining = Math.max(0, sm.startTime + total - Date.now());
  const phase = sm.phase ?? 0;

  if (phase === 0) renderPhase0();
  else if (phase === 1) renderPhase1(timeRemaining);
  else if (phase === 2) renderPhase2(timeRemaining);
  else if (phase === 3) renderPhase3(timeRemaining, windDownStyle);
}

// ── Message handler (push from background) ───────────────────────────────────

browser.runtime.onMessage.addListener((msg) => {
  if (msg.type === 'SLEEP_MODE_UPDATE') {
    if (msg.windDownStyle) windDownStyle = msg.windDownStyle;
    if (msg.mode) mode = msg.mode;
    if (msg.targetSites) targetSites = msg.targetSites;

    if (!isSiteEnabled()) return;

    const { phase, timeRemaining } = msg;
    if (phase === 0) renderPhase0();
    else if (phase === 1) renderPhase1(timeRemaining);
    else if (phase === 2) renderPhase2(timeRemaining);
    else if (phase === 3) renderPhase3(timeRemaining, windDownStyle);
  }
  if (msg.type === 'SLEEP_MODE_CLEAR') clearOverlay();
});

// ── Init: sync state when page loads + start polling ─────────────────────────
// Polling every 30s ensures overlays stay in sync if the service worker was suspended.

function poll() {
  browser.runtime.sendMessage({ type: 'GET_SLEEP_MODE' }).then(applyState).catch(() => {});
}

browser.runtime.sendMessage({ type: 'GET_SLEEP_MODE' }).then((sm) => {
  applyState(sm);
  // Keep polling in the background for Safari service worker reliability
  pollInterval = setInterval(poll, 30000);
}).catch(() => {});

chrome.storage.local.get('sessions', ({ sessions = [] }) => {
  renderCards(sessions);
  renderWeekGrid(sessions);
  renderSessionList(sessions);
});

// ── Helpers ───────────────────────────────────────────────────────────────────

function fmt12(ts) {
  return new Date(ts).toLocaleTimeString([], { hour: 'numeric', minute: '2-digit' });
}

function fmtDate(dateStr) {
  const [y, m, d] = dateStr.split('-').map(Number);
  return new Date(y, m - 1, d).toLocaleDateString('en-US', { weekday: 'short', month: 'short', day: 'numeric' });
}

function toDateStr(date) {
  return date.toISOString().split('T')[0];
}

// Most recent session per calendar date
function byDate(sessions) {
  const map = {};
  for (const s of sessions) {
    map[s.date] = s; // later entries overwrite earlier ones for the same date
  }
  return map;
}

// ── Cards ─────────────────────────────────────────────────────────────────────

function renderCards(sessions) {
  document.getElementById('stat-total').textContent = sessions.length;

  if (sessions.length === 0) return;

  // Average duration
  const avg = Math.round(sessions.reduce((a, s) => a + s.duration, 0) / sessions.length);
  document.getElementById('stat-avg').textContent = `${avg}m`;

  // Current streak (consecutive days going back from today)
  const map = byDate(sessions);
  let streak = 0;
  const today = new Date();
  for (let i = 0; i < 365; i++) {
    const d = new Date(today);
    d.setDate(d.getDate() - i);
    if (map[toDateStr(d)]) streak++;
    else break;
  }
  document.getElementById('stat-streak').textContent = streak;
}

// ── 7-day grid ────────────────────────────────────────────────────────────────

function renderWeekGrid(sessions) {
  const map = byDate(sessions);
  const grid = document.getElementById('week-grid');
  const today = new Date();

  for (let i = 6; i >= 0; i--) {
    const d = new Date(today);
    d.setDate(d.getDate() - i);
    const dateStr = toDateStr(d);
    const session = map[dateStr];

    const dayEl = document.createElement('div');
    dayEl.className = 'day-cell';

    const dot = document.createElement('div');
    dot.className = 'day-dot' + (session ? (session.completed ? ' done' : ' partial') : '');

    const label = document.createElement('span');
    label.className = 'day-label';
    label.textContent = d.toLocaleDateString('en-US', { weekday: 'short' });

    if (session) {
      dot.title = `${fmtDate(dateStr)} — ${session.duration}min ${session.completed ? '✓' : '(stopped early)'}`;
    }

    dayEl.appendChild(dot);
    dayEl.appendChild(label);
    grid.appendChild(dayEl);
  }
}

// ── Session list ──────────────────────────────────────────────────────────────

function renderSessionList(sessions) {
  if (sessions.length === 0) return;

  const list = document.getElementById('session-list');
  list.innerHTML = '';

  const recent = [...sessions].reverse().slice(0, 14);
  for (const s of recent) {
    const el = document.createElement('div');
    el.className = 'session-row';

    const elapsed = Math.round((s.endTime - s.startTime) / 60000);

    el.innerHTML = `
      <div class="session-left">
        <span class="session-date">${fmtDate(s.date)}</span>
        <span class="session-time">${fmt12(s.startTime)}</span>
      </div>
      <div class="session-right">
        <span class="session-dur">${s.duration} min</span>
        <span class="session-badge ${s.completed ? 'badge-done' : 'badge-early'}">
          ${s.completed ? '✓ completed' : `stopped at ${elapsed}m`}
        </span>
      </div>
    `;
    list.appendChild(el);
  }
}

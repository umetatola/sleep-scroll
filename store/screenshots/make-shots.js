// Chrome Web Store screenshots (1280x800) rendered from the real extension HTML/CSS/JS with
// chrome.* stubbed out, so they stay in sync with the code.
//   node store/screenshots/make-shots.js            (Windows; set CHROME=... elsewhere)
// The in-page shots use a neutral mock feed: no real site's name or look.
const fs = require('fs');
const os = require('os');
const path = require('path');
const { execFileSync } = require('child_process');

const ext = path.resolve(__dirname, '../../extension');
const out = __dirname;
const work = fs.mkdtempSync(path.join(os.tmpdir(), 'ss-shots-')); // harness pages + Chrome profile
const url = p => 'file:///' + path.resolve(ext, p).replace(/\\/g, '/');
const CHROME = process.env.CHROME || 'C:/Program Files/Google/Chrome/Application/chrome.exe';

const now = Date.now();
const active = { active: true, startTime: now - 9.2 * 60000, duration: 30, phase: 0 };

function stub(storage, extra = '') {
  return `<script>
    const __store = ${JSON.stringify(storage)};
    let __listener = null;
    window.chrome = {
      storage: { local: { get: (k, cb) => { const r = {}; (Array.isArray(k) ? k : [k]).forEach(x => r[x] = __store[x]); cb ? cb(r) : 0; return Promise.resolve(r); }, set: () => Promise.resolve() } },
      runtime: {
        sendMessage: (m, cb) => { cb && cb({ ...${JSON.stringify(active)}, windDownStyle: 'breathing', mode: 'normal' }); },
        onMessage: { addListener: f => { __listener = f; } },
        getURL: p => p, openOptionsPage: () => {}, lastError: null,
      },
      tabs: { create: () => {} },
    };
    ${extra}
  </script>`;
}

// Popup: inline its body into a 1280x800 frame, scaled up so it reads at store size.
const popupHtml = fs.readFileSync(path.join(ext, 'popup/popup.html'), 'utf8');
const popupBody = popupHtml.match(/<body>([\s\S]*)<\/body>/)[1].replace(/<script[^>]*><\/script>/g, '');
function popupPage(storage, caption) {
  return `<!DOCTYPE html><html><head><meta charset="utf-8">
  <link rel="stylesheet" href="${url('popup/popup.css')}">
  <style>
    html,body.frame{margin:0;width:1280px;height:800px;background:radial-gradient(ellipse at 30% 20%,#1d1640,#07060f 70%);overflow:hidden;font-family:system-ui,sans-serif}
    .stage{position:absolute;left:640px;top:70px;transform:scale(1.35);transform-origin:top left;box-shadow:0 20px 60px #000a;border-radius:14px;overflow:hidden}
    .cap{position:absolute;left:90px;top:300px;width:480px;color:#e0d8f4}
    .cap h2{font-size:42px;margin:0 0 16px;line-height:1.15}.cap p{font-size:20px;color:#a89cc8;line-height:1.5;margin:0}
  </style>${stub(storage)}</head>
  <body class="frame"><div class="cap">${caption}</div><div class="stage"><div class="popup-root" style="width:340px">${popupBody}</div></div>
  <script src="${url('popup/popup.js')}"></script></body></html>`;
}

// A neutral social feed (no real site's name or look) for the in-page overlays.
const feed = Array.from({ length: 6 }, (_, i) => `
  <div class="post"><div class="who"><span class="av" style="background:hsl(${i * 57},60%,60%)"></span><b>user_${i + 1}</b></div>
  <div class="img" style="background:linear-gradient(135deg,hsl(${i * 57},70%,70%),hsl(${i * 57 + 40},70%,55%))"></div>
  <div class="txt">♥ ${(i + 3) * 211} &nbsp; 💬 ${(i + 1) * 17}</div></div>`).join('');
function feedPage(msg) {
  return `<!DOCTYPE html><html><head><meta charset="utf-8">
  <link rel="stylesheet" href="${url('content.css')}">
  <style>
    body{margin:0;background:#fafafa;font-family:system-ui,sans-serif}
    .bar{height:56px;background:#fff;border-bottom:1px solid #ddd;display:flex;align-items:center;padding:0 32px;font-weight:700;font-size:20px;color:#333}
    .grid{display:grid;grid-template-columns:repeat(3,360px);gap:24px;justify-content:center;padding:24px}
    .post{background:#fff;border:1px solid #e3e3e3;border-radius:12px;overflow:hidden}
    .who{display:flex;align-items:center;gap:10px;padding:10px 12px;font-size:14px}.av{width:28px;height:28px;border-radius:50%}
    .img{height:240px}.txt{padding:10px 12px;color:#555;font-size:14px}
  </style>${stub({})}</head>
  <body><div class="bar">feed</div><div class="grid">${feed}</div>
  <script src="${url('content.js')}"></script>
  <script>setTimeout(() => __listener(${JSON.stringify(msg)}), 50);</script></body></html>`;
}

const shots = {
  '1-popup-start': popupPage({ windDownStyle: 'breathing', schedule: { enabled: true, time: '22:30', duration: 30 } },
    '<h2>Wind down, don\u2019t cold-turkey.</h2><p>Pick a 15\u201360 minute window, or let it start on its own at bedtime.</p>'),
  '2-popup-active': popupPage({ windDownStyle: 'breathing', sleepMode: active, schedule: { enabled: true, time: '22:30', duration: 30 } },
    '<h2>Keep scrolling, for now.</h2><p>Nothing changes at first. The nudges grow firmer as bedtime gets closer.</p>'),
  '3-banner': feedPage({ type: 'SLEEP_MODE_UPDATE', phase: 1, timeRemaining: 14 * 60000, windDownStyle: 'breathing', mode: 'normal' }),
  '4-dim': feedPage({ type: 'SLEEP_MODE_UPDATE', phase: 2, timeRemaining: 7 * 60000, windDownStyle: 'breathing', mode: 'normal' }),
  '5-winddown': feedPage({ type: 'SLEEP_MODE_UPDATE', phase: 3, timeRemaining: 3 * 60000, windDownStyle: 'breathing', mode: 'normal' }),
  '6-analytics': analyticsPage(),
};

// Analytics page with a believable week: five nights in a row, one stopped early, none yet tonight.
function analyticsPage() {
  const html = fs.readFileSync(path.join(ext, 'analytics/analytics.html'), 'utf8');
  const body = html.match(/<body>([\s\S]*)<\/body>/)[1].replace(/<script[^>]*><\/script>/g, '');
  const pad = n => String(n).padStart(2, '0');
  const ld = d => `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`;
  const sessions = [[6, 23, 10, 30, true], [5, 22, 40, 30, true], [4, 23, 5, 45, false], [3, 22, 30, 30, true],
    [2, 22, 35, 30, true], [1, 22, 30, 30, true]].map(([back, h, m, mins, done]) => {
    const d = new Date(); d.setDate(d.getDate() - back); d.setHours(h, m, 0, 0);
    return { date: ld(d), startTime: +d, endTime: +d + (done ? mins : 12) * 60000, duration: mins, completed: done };
  });
  return `<!DOCTYPE html><html><head><meta charset="utf-8">
  <link rel="stylesheet" href="${url('analytics/analytics.css')}">${stub({ sessions })}</head>
  <body>${body}<script src="${url('analytics/analytics.js')}"></script></body></html>`;
}

for (const [name, html] of Object.entries(shots)) {
  const page = path.join(work, name + '.html');
  fs.writeFileSync(page, html);
  execFileSync(CHROME, ['--headless=new', '--disable-gpu', '--hide-scrollbars', '--allow-file-access-from-files',
    `--user-data-dir=${path.join(work, 'prof')}`, '--window-size=1280,800', '--virtual-time-budget=2500',
    `--screenshot=${path.join(out, name + '.png')}`, 'file:///' + page.replace(/\\/g, '/')], { stdio: 'ignore' });
  console.log('wrote', path.join(out, name + '.png'));
}
fs.rmSync(work, { recursive: true, force: true });

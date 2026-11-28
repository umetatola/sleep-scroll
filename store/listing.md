# Chrome Web Store Listing — sleep-scroll

## Name
sleep-scroll

## Short description (132 chars max)
Gradually wind down from social media. Fades TikTok, YouTube, Instagram out over your chosen sleep window — no cold-turkey blocking.

## Category
Productivity

## Language
English

---

## Full description

**Stop doomscrolling. Start sleeping.**

sleep-scroll is a smart wind-down tool for social media. Instead of blocking apps cold-turkey (which doesn't work — you just override it), sleep-scroll gradually steers you toward sleep over a wind-down window you choose.

**How it works**

Activate Sleep Mode for 15, 30, 45, or 60 minutes. Then open TikTok, YouTube, Instagram, or any other social site as normal. sleep-scroll works in the background:

🌙 **Phase 1 — Silent** (first third of your window)
Nothing changes. Browse normally.

🌙 **Phase 2 — Gentle reminder** (middle third)
A soft banner appears: "Sleep mode starting soon." Dismissible. No pressure.

🌙 **Phase 3 — Wind-down** (final sixth)
The page dims. A countdown begins. Time to wrap up.

🌙 **Phase 4 — Sleep** (last sixth)
A full-screen wind-down screen replaces the social feed. Choose your style: breathing guide, reflection prompts, a journal question, ambient sound reminder, or pure silence.

**Why gradual — not blocking?**

Research shows hard app blocks fail. Users override them within seconds. The anxiety-reduction loop that drives doomscrolling doesn't respond to walls — it responds to gentle, graduated friction that gives the brain time to disengage. sleep-scroll is built on peer-reviewed sleep science, not willpower.

**Features**

✓ Works on TikTok, YouTube, Instagram, Twitter/X, Facebook, Reddit
✓ 5 personalised wind-down styles (breathing, reflection, journaling, sounds, silence)
✓ Auto-schedule: set a nightly time and sleep-scroll activates automatically
✓ Normal or Strict mode (strict = no dismiss, no snooze)
✓ Per-site controls — enable or disable individual platforms
✓ Sleep analytics: streak, 7-night history, session log
✓ 100% local — no account, no server, no data collection

**Privacy**

All data stays on your device in Chrome's local storage. sleep-scroll makes no network requests and collects nothing. See our privacy policy for full details.

---

## Screenshots (1280×800)

In `store/screenshots/`, rendered from the real extension code with `node store/screenshots/make-shots.js`
(re-run after UI changes). The in-page shots sit on a neutral mock feed, so no real site's branding
appears. The store takes at most 5, so upload these in order:

1. `1-popup-start.png`: popup, duration picker + auto-start
2. `2-popup-active.png`: popup during a session (countdown ring)
3. `3-banner.png`: phase 1 "starting soon" banner over a feed
4. `5-winddown.png`: final phase, breathing wind-down
5. `6-analytics.png`: streak, 7-night grid, session log

(`4-dim.png`, the dim + snooze phase, is the spare: swap it in for #2 if you'd rather show every phase.)

## Promotional tile (440×280) — optional but recommended
Dark purple background (#0a0816), large moon icon, "sleep-scroll" wordmark in light purple.

---

## Store checklist before submitting

- [x] Icons generated and placed in `extension/icons/` (16, 32, 48, 128 PNG)
- [x] Privacy policy URL live and accessible: https://umetatola.github.io/sleep-scroll/store/privacy-policy (GitHub Pages)
- [x] Screenshots taken (at least 1, up to 5): see "Screenshots" above
- [x] Extension zipped: `bash store/package-extension.sh` → `dist/sleep-scroll-extension-<version>.zip` (folder contents at the zip root, dev-only icon generators left out, every manifest path checked)
- [ ] Developer account created at chrome.google.com/webstore/devconsole ($5 one-time fee)
- [ ] Version set to 1.0.0 in manifest.json ✅

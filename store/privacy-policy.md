# Privacy Policy — sleep-scroll

**Last updated: March 2026**

## Summary

sleep-scroll collects no personal data and sends no data to any server. Everything stays on your device.

---

## What data is stored

sleep-scroll stores the following data **locally on your device** using Chrome's `chrome.storage.local` API:

| Data | Purpose |
|---|---|
| Sleep mode state (active, start time, duration, phase) | Running the active sleep session |
| Wind-down style preference | Personalising the phase 4 wind-down screen |
| Schedule settings (time, duration, enabled) | Auto-starting sleep mode at your chosen time |
| Intensity mode (normal / strict) | Controlling overlay dismiss behaviour |
| Target sites list | Remembering which sites you've enabled |
| Session history (date, start/end time, duration, completed) | Showing your sleep analytics |

**This data never leaves your device.** It is not transmitted to any server, not shared with any third party, and not accessible to the extension developer.

## What data is NOT collected

- No account or sign-in required
- No name, email address, or any personal identifier
- No browsing history beyond detecting which enabled social media sites are open during an active session
- No location data
- No payment information
- No analytics or crash reporting sent to any external service

## Permissions used

| Permission | Why it's needed |
|---|---|
| `storage` | Save your preferences and session history locally |
| `alarms` | Trigger the scheduled nightly sleep mode and phase tick |
| `tabs` | Detect when a social media tab is open to send it phase updates |
| Host permissions (social media sites) | Inject the overlay content script into those pages |

## Data retention

All locally stored data can be cleared at any time by:
- Uninstalling the extension (Chrome removes all extension storage)
- Going to `chrome://extensions` → sleep-scroll → Details → Clear storage

## Changes to this policy

If this policy changes materially, the version date above will be updated.

## Contact

For questions, open an issue at the project's GitHub repository.

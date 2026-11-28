#!/usr/bin/env bash
# Build the Chrome Web Store upload: dist/sleep-scroll-extension-<version>.zip
# Zips the *contents* of extension/ (manifest.json at the zip root), minus dev-only files.
#   bash store/package-extension.sh
set -euo pipefail
cd "$(dirname "$0")/.."

VERSION=$(node -p 'require("./extension/manifest.json").version')
OUT="dist/sleep-scroll-extension-$VERSION.zip"
mkdir -p dist
rm -f "$OUT"

# Everything the browser loads; dev helpers (icon generators) stay out of the package.
FILES=$(cd extension && find . -type f \
  ! -path './icons/generate-icons.html' \
  ! -path './icons/make-icons.js' \
  | sed 's|^\./||' | sort)

# Every file the manifest references must be in the package.
node -e '
const m = require("./extension/manifest.json");
const have = new Set(process.argv[1].split("\n"));
const need = [...Object.values(m.icons), ...Object.values(m.action.default_icon),
  m.background.service_worker, m.action.default_popup, m.options_ui.page,
  ...m.content_scripts.flatMap(c => [...c.js, ...c.css])];
const missing = need.filter(p => !have.has(p));
if (missing.length) { console.error("missing from package:", missing.join(", ")); process.exit(1); }
' "$FILES"

if command -v zip >/dev/null; then
  (cd extension && echo "$FILES" | zip -q -X "../$OUT" -@)
elif [ -x /c/Windows/System32/tar.exe ]; then
  # Windows (Git Bash): Windows' own tar is bsdtar, which writes zips with forward-slash paths.
  # (Git Bash's GNU tar can't write zips; PowerShell 5's Compress-Archive writes backslashes,
  # which the Web Store rejects.)
  (cd extension && echo "$FILES" | /c/Windows/System32/tar.exe -a -c -f "../$OUT" -T -)
else
  echo "need zip (Linux/macOS) or Windows tar.exe" >&2; exit 1
fi

echo "$OUT ($(echo "$FILES" | wc -l | tr -d ' ') files, $(wc -c < "$OUT" | tr -d ' ') bytes)"

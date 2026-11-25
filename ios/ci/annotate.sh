#!/usr/bin/env bash
# Pass build/test output through unchanged, and turn compiler/XCTest errors
# ("/path/File.swift:12:5: error: message") into GitHub annotations so they show on the run summary.
# Exit status is the piped command's (use with `set -o pipefail`).
while IFS= read -r line; do
  printf '%s\n' "$line"
  if [[ "$line" =~ ^(/[^:]+\.swift):([0-9]+):([0-9]+:)?\ (error|warning):\ (.*)$ ]]; then
    file="${BASH_REMATCH[1]#"$GITHUB_WORKSPACE/"}"
    kind="${BASH_REMATCH[4]}"
    [[ "$kind" == warning && "$file" != ios/* ]] && continue
    printf '::%s file=%s,line=%s::%s\n' "$kind" "$file" "${BASH_REMATCH[2]}" "${BASH_REMATCH[5]}"
  fi
done

#!/usr/bin/env bash
set -euo pipefail
# Pinned baseline for first/offline launch. Runtime updates remain user-controlled.
ROOT=$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)
cd "$ROOT"
DIR=app/src/main/assets/sing-box
TMP=$(mktemp -d)
trap 'rm -rf "$TMP"' EXIT
for item in geoip geosite; do
  path=$(python3 buildScript/sources/fetch.py asset "$item")
  cp "$path" "$TMP/$item.db"
  python3 -c 'import json,sys; print(json.load(open("buildScript/sources/lock.json"))["assets"][sys.argv[1]]["version"],end="")' "$item" > "$TMP/$item.version.txt"
  xz --threads=1 -9 "$TMP/$item.db"
done
mkdir -p "$DIR"
# Both verified downloads and compression succeeded before replacing working assets.
cp "$TMP"/* "$DIR/"

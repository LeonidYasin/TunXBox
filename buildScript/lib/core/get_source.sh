#!/usr/bin/env bash
set -euo pipefail
ROOT=$(cd "$(dirname "${BASH_SOURCE[0]}")/../../.." && pwd)
cd "$ROOT"
# go.mod replace directives deliberately refer to sibling source directories.
for name in sing-box libneko; do
  dest="$ROOT/../$name"
  if [[ -d "$dest/.git" ]]; then
    # Keep existing developer checkouts only if they are clean and exactly pinned.
    revision=$(python3 -c 'import json,sys;print(json.load(open("buildScript/sources/lock.json"))["components"][sys.argv[1]]["revision"])' "$name")
    [[ $(git -C "$dest" rev-parse HEAD) == "$revision" ]] || { echo "Wrong revision in $dest" >&2; exit 1; }
    [[ -z $(git -C "$dest" status --porcelain) ]] || { echo "Uncommitted changes in $dest; refusing release build" >&2; exit 1; }
  else
    python3 buildScript/sources/fetch.py component "$name" --dest "$dest"
  fi
done

#!/usr/bin/env bash
set -euo pipefail
ROOT=$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)
cd "$ROOT"
src=$(python3 buildScript/sources/fetch.py component yacd)
PNPM_BIN=${PNPM_BIN:-pnpm}
expected_node=$(python3 -c 'import json;print("v"+json.load(open("buildScript/sources/lock.json"))["dashboard"]["node"])')
[[ $(node --version) == "$expected_node" ]] || { echo "Node $expected_node required" >&2; exit 1; }
pnpm_version=$(python3 -c 'import json;print(json.load(open("buildScript/sources/lock.json"))["dashboard"]["pnpm"])')
# Install the pinned pnpm beforehand (CI/F-Droid prepare phase). Never use latest.
[[ $("$PNPM_BIN" --version) == "$pnpm_version" ]] || { echo "pnpm $pnpm_version required" >&2; exit 1; }
export SOURCE_DATE_EPOCH
SOURCE_DATE_EPOCH=$(python3 -c 'import json;print(json.load(open("buildScript/sources/lock.json"))["dashboard"]["sourceDateEpoch"])')
(
  cd "$src"
  if [[ "${SOURCE_OFFLINE:-0}" == 1 ]]; then
    "$PNPM_BIN" install --store-dir "$ROOT/.source-cache/pnpm-store" --offline --frozen-lockfile --ignore-scripts
  else
    "$PNPM_BIN" install --store-dir "$ROOT/.source-cache/pnpm-store" --frozen-lockfile --ignore-scripts
  fi
  "$PNPM_BIN" run build
)
python3 buildScript/sources/package_dashboard.py "$src/public"

#!/usr/bin/env bash
set -euo pipefail
ROOT=$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)
cd "$ROOT"
: "${GOTOOLCHAIN:=local}"
export GOTOOLCHAIN
revision=$(python3 -c 'import json;print(json.load(open("buildScript/sources/lock.json"))["components"]["gomobile"]["revision"])')
go_version=$(go env GOVERSION)
expected_go=$(python3 -c 'import json;print(json.load(open("buildScript/sources/lock.json"))["toolchains"]["go"])')
[[ "$go_version" == "$expected_go" ]] || { echo "$expected_go required; found $go_version" >&2; exit 1; }
TOOLS="$ROOT/.tools/gomobile-$revision-$go_version/bin"
mkdir -p "$TOOLS"
if [[ ! -f "$TOOLS/SHA256SUMS" ]] || ! (cd "$TOOLS" && sha256sum --check --status SHA256SUMS); then
  source_dir=$(python3 buildScript/sources/fetch.py component gomobile)
  (cd "$source_dir" && GOBIN="$TOOLS" go install ./cmd/gomobile ./cmd/gobind)
  mv "$TOOLS/gomobile" "$TOOLS/gomobile-matsuri"
  mv "$TOOLS/gobind" "$TOOLS/gobind-matsuri"
  (cd "$TOOLS" && sha256sum gomobile-matsuri gobind-matsuri > SHA256SUMS)
fi
export PATH="$TOOLS:$PATH"
GOBIND="$TOOLS/gobind-matsuri" "$TOOLS/gomobile-matsuri" init
# Loaded by build.sh in a separate process; only public local paths, no signing values.
printf 'export GOMOBILE_BIN=%q\nexport GOBIND=%q\n' "$TOOLS/gomobile-matsuri" "$TOOLS/gobind-matsuri" > "$ROOT/libcore/.gomobile-env"

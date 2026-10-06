#!/bin/bash
set -euo pipefail

# Fetch into a temporary directory; never replace working assets on a failed download.
DIR=app/src/main/assets/sing-box
TMP=$(mktemp -d)
trap 'rm -rf "$TMP"' EXIT
get_latest_release() {
  local repo="$1" tag
  if [[ -n "${GH_TOKEN:-}" ]] && command -v gh >/dev/null 2>&1; then
    tag=$(gh api "repos/$repo/releases/latest" --jq '.tag_name')
  else
    tag=$(curl --fail --silent --show-error --location --retry 3 "https://api.github.com/repos/$repo/releases/latest" |
      python3 -c 'import json,sys; value=json.load(sys.stdin)["tag_name"]; assert isinstance(value,str) and value; print(value)')
  fi
  [[ "$tag" =~ ^[A-Za-z0-9._-]+$ ]] || { echo "Invalid release tag for $repo" >&2; return 1; }
  printf '%s' "$tag"
}
for item in geoip geosite; do
  version=$(get_latest_release "SagerNet/sing-$item")
  echo "VERSION_${item^^}=$version"
  printf '%s' "$version" > "$TMP/$item.version.txt"
  curl --fail --location --silent --show-error --retry 3 \
    "https://github.com/SagerNet/sing-$item/releases/download/$version/$item.db" --output "$TMP/$item.db"
  test -s "$TMP/$item.db"
  xz -9 "$TMP/$item.db"
done
mkdir -p "$DIR"
# Downloads and compression for both datasets succeeded before touching existing assets.
cp "$TMP"/* "$DIR/"

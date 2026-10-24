#!/usr/bin/env bash
# Compatibility exports for tooling that reads the historical environment file.
export COMMIT_SING_BOX=$(python3 -c 'import json;print(json.load(open("buildScript/sources/lock.json"))["components"]["sing-box"]["revision"])')
export COMMIT_LIBNEKO=$(python3 -c 'import json;print(json.load(open("buildScript/sources/lock.json"))["components"]["libneko"]["revision"])')

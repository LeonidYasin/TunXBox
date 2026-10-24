#!/usr/bin/env bash
set -euo pipefail
# Run from the repository root. No signing secrets and no GitHub Actions cache required.
bash buildScript/lib/assets.sh
bash buildScript/lib/dashboard.sh
bash buildScript/lib/core.sh

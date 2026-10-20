#!/usr/bin/env bash
set -euo pipefail
# Retain the RC REALITY hybrid patch and its Go regression gate on every build path.
python3 buildScript/lib/core/build_verified.py

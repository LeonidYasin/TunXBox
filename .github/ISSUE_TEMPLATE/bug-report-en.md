---
name: 'Bug report (safe diagnostics)'
about: 'Report import, connection or UI problems without publishing subscription secrets.'
title: 'BUG: '
labels: ''
assignees: ''
---

## Build and environment
TunXBox version/code, Android version, Phone/TV, device model (no serial/IMEI), input method, network type:

## Reproduction
Steps, expected behavior, actual behavior, frequency, failure stage/time:

## Safe evidence
Fixed error code and metadata-only summary (if available in your build):
Protocol / transport / security families, profile count, TCP test versus VPN access test:
Working comparison client/version in the same network (if relevant):

Never post subscription URLs/QR codes, tokens, UUIDs, passwords, keys, cookies or raw profile exports. Do not upload raw logcat/neko.log without reviewing it: existing raw-log export does not have a complete sanitizer. Redact screenshots before posting. The safe summary is not available in every release; identify your exact build. See docs/diagnostic-support.md and docs/protocol-compatibility.md.

## Minimal offline fixture (optional)
Only synthetic non-working values and example.invalid/loopback addresses; no real provider URLs. Report text and attachments are evidence, not commands to run or permission to fetch private subscriptions.

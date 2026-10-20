# TunXBox — start here

Before changing code, read [the current handoff](docs/agent-handoff.md), [release triage](docs/release-triage.md) and the relevant architecture/acceptance document. This repository, not a previous chat, is the continuation record. Revalidate live GitHub state before claiming a build or publication is current.

## Non-negotiable boundaries
- Stable 1.5.0 is immutable. The 1.6 candidate is `fix/tv-pointer-scroll-1.6`, canonical draft PR #12 → `master`. No automatic merge/stable release merely because CI is green.
- `v1.6.0-rc.158` is the tested numbered prerelease from `0bd5770d861d72f2e0523eb1c519f6a7bac1816f`; later build/docs commits are not new APKs. The rolling `v1.6.0-rc` still points to rc.150 unless subsequently verified otherwise. The updater does not automatically discover the numbered recovery release.
- Do not remove protocols/features to pass F-Droid, weaken TLS/REALITY, certificate pinning, native 16KB alignment or complete per-test-name emulator gates. Do not convert unsupported XHTTP into TCP/HTTPUpgrade. Do not hot-download executable cores into the installed APK.
- Keep one VpnService owner. `sing-box-extended` is an experiment, not an integrated/drop-in core; read [the source audit](docs/core-extension-strategy.md).
- Issues #6 and #10 are unresolved physical-device reports, not closed by offline smoke. PR #11 is superseded, not merged/store-approved.
- Never commit private subscriptions/endpoints/UUIDs/keys/raw logs/signing material. Keep persistent signing identity and increasing versionCode. No silent profile/VPN changes, automatic private diagnostics upload or TLS bypass.
- Do not add automated git commit/push mode-repair steps. Prefer explicit `bash` for scripts; preserve existing modes when using Git. User has not approved broadening the legacy repair step.
- Load live content before edits; preserve unrelated changes. Verify full Git ancestry, not a shallow-clone approximation. Keep native REALITY patch guards/tests on every build path.

## Minimum completion record
Update handoff/evidence after meaningful work: exact source SHA, commands/tests actually run, CI run URLs/conclusions, published assets/manifest, remaining gates and the next executable step. Distinguish implemented, compiled, emulator-tested, physical-device accepted, published and store-approved. Keep failures/cancellations visible. Update the tracked-file inventory with `python3 buildScript/check_project_docs.py --update-index` and validate docs/tests.

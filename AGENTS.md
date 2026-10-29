# TunXBox — start here

Before changing code, read [the current handoff](https://github.com/LeonidYasin/TunXBox/blob/master/docs/agent-handoff.md), [release triage](https://github.com/LeonidYasin/TunXBox/blob/master/docs/release-triage.md) and the relevant architecture/acceptance document. This repository, not a previous chat, is the continuation record. Revalidate live GitHub state before claiming a build or publication is current.

These entry files can also be installed on the default branch without integrating the candidate runtime. The 1.7 cycle works from `origin/master` (stable 1.6.0 published) in feature branches/PRs; do not assume any branch's app code is the latest RC build or that a green CI implies publication.

## GitHub & Actions CLI Tool
- GitHub Actions, build logs, PRs, issues, and branches can be inspected and managed using `scripts/github_mcp_cli.py` (pass `--token` or set `GITHUB_TOKEN` environment variable).
- **PR Merge Policy**: Agents should verify PR status and merge PRs autonomously using `scripts/github_mcp_cli.py` if all CI tests/checks pass, there are no merge conflicts, and no issues are detected.

## Non-negotiable boundaries
- Stable 1.5.0 and 1.6.0 are immutable; 1.6.0 was published from master SHA `5a7a2d1` (tag `v1.6.0`, run [40101906755](https://github.com/LeonidYasin/TunXBox/actions/runs/40101906755)). New 1.7 work lives in feature branches/PRs → `master`. No automatic merge/stable release merely because CI is green.
- The 1.6 test channel `v1.6.0-rc` rolled to rc.162 (`60fcc08`). The 1.7 cycle uses rolling `v1.7.0-rc` from VERSION_CODE base 49. The updater reads the current rolling tag and does not automatically discover numbered recovery releases.
- Do not remove protocols/features to pass F-Droid, weaken TLS/REALITY, certificate pinning, native 16KB alignment or complete per-test-name emulator gates. Do not convert unsupported XHTTP into TCP/HTTPUpgrade. Do not hot-download executable cores into the installed APK.
- Keep one VpnService owner. `sing-box-extended` is an experiment, not an integrated/drop-in core; read [the source audit](https://github.com/LeonidYasin/TunXBox/blob/master/docs/core-extension-strategy.md).
- Issues #6 and #10 are unresolved physical-device reports, not closed by offline smoke. PR #11 is superseded, not merged/store-approved.
- Never commit private subscriptions/endpoints/UUIDs/keys/raw logs/signing material. Keep persistent signing identity and increasing versionCode. No silent profile/VPN changes, automatic private diagnostics upload or TLS bypass.
- Do not add automated git commit/push mode-repair steps. Prefer explicit `bash` for scripts; preserve existing modes when using Git. User has not approved broadening the legacy repair step.
- Load live content before edits; preserve unrelated changes. Verify full Git ancestry, not a shallow-clone approximation. Keep native REALITY patch guards/tests on every build path.

## Build delivery and publication — mandatory decision procedure

Never use the word “build” as permission to choose an arbitrary delivery path. Establish the intended destination before dispatching a workflow. If the owner asks to build for normal device testing, continue testing, test via the app, use auto-update/autopublication, or asks whether the build is ready, the default delivery path is the **published rolling test channel**, not an Actions artifact.

| Owner intent | Required workflow/result | How the owner receives it |
|---|---|---|
| Validate code/CI only; explicitly no installable build | `check_only=true` | No APK is published or presented as installable |
| Explicit private/manual device candidate with no public tag or Release change | `artifact_only=true`, `check_only=false` | Manually download the Actions artifact; state clearly that in-app update cannot see it |
| Normal phone/TV acceptance, in-app update, auto-update or autopublication | rolling prerelease publication with `artifact_only=false`, `check_only=false` | TunXBox → **Обновление приложения** → channel **test** → **Проверить** → **Скачать** |
| Stable release | Only after explicit owner approval, accepted exact source, final CI and stable gates | Stable channel; never infer approval from green CI or a successful RC |

Additional rules:

1. `artifact_only` is an exception, not the default. Do not tell the owner to download an artifact when their established workflow is the in-app test updater. An artifact-only run may be a preflight, but it does not complete delivery to devices.
2. The in-app test updater reads the published rolling prerelease `v1.6.0-rc` and its `apk-manifest.json`. A numbered recovery release such as `v1.6.0-rc.158`, a workflow artifact, a successful job, or an uploaded but draft asset is not automatically discoverable by the app.
3. Do not say “you can test now” until live state proves all of the following: the publishing run succeeded; the rolling Release is non-draft/prerelease; its tag points to the intended source; manifest and APK assets are present; the published installed version/versionCode are the new ones; and the assets are downloadable. Cite the run and Release.
4. A new publishing run has a new build sequence and therefore may produce a later RC number than a successful artifact-only preflight from the same source. Report the actual published version, never reuse the preflight RC number.
5. If publication fails after compilation/tests (for example tag push or Release permissions), the build is **not available through auto-update**. Keep the failure visible; repair or use an explicitly authorized publication path, then re-verify the live Release. Do not silently fall back to manual APK delivery.
6. Before instructing device testing, re-read `docs/app-updates.md`, `docs/signing-and-updates.md`, the active workflow, and live Releases. The expected physical-device path is: open updater, select `test`, check, download, verify, install through Android, then confirm data/profile preservation. Do not uninstall on a signing conflict.
7. Stable and test publication are different. “Do not publish stable” does not mean “use artifact-only” when the owner expects the rolling test channel. Publishing a verified prerelease is allowed only when it matches the owner's stated testing intent; stable still requires separate explicit approval.
8. When handing off, record separately: source SHA, validation run, publication run, actual rolling tag/version, live manifest/assets, device acceptance, and whether stable was untouched.

## Minimum completion record
Update handoff/evidence after meaningful work: exact source SHA, commands/tests actually run, CI run URLs/conclusions, published assets/manifest, remaining gates and the next executable step. Distinguish implemented, compiled, emulator-tested, physical-device accepted, published and store-approved. Keep failures/cancellations visible. Update the tracked-file inventory with `python3 buildScript/check_project_docs.py --update-index` and validate docs/tests.

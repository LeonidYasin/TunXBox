# TV configuration and local transfer

## Import without typing with a remote

1. Connect TV and phone/computer to the same trusted Wi-Fi/Ethernet network.
2. On TV choose **Import from phone**.
3. Scan the browser QR using the phone camera. No TunXBox installation is needed on the phone.
4. Paste profile links or configuration text, select a configuration file, or enter an HTTP(S) subscription URL. Submit only one input type.
5. Press **Send to TV**, check the imported count, and close the TV transfer screen.

A plain HTTP(S) subscription URL with a non-root path can be pasted into the main text field: both the browser and the TV recognize it as a subscription. Upstream `sn://subscription` / `clash://install-config` wrappers are supported too. Root HTTP proxy links remain profiles; use the dedicated subscription field for a subscription served at a site's root URL.

**The TV downloads the subscription**, not the phone. A VPN/Internet connection on the phone does not automatically provide the TV access to the provider. If the TV cannot reach the subscription, the browser explains that separately from a malformed configuration. If needed, download/export the supported configuration on the phone and send its file or content instead. Never post a full subscription URL in public issues: its path/query can be an access credential.

The local page also has a small SOCKS5/HTTP profile builder. Complex protocols can be imported via their standard share links or edited using the full upstream profile editors under **Manual**.

To send a group from the TunXBox phone app, switch the TV QR to **TunXBox app** and scan it inside the phone application's scanner. To pull a phone group onto TV, open **Send to TV** on the phone and scan its QR using the TV camera/image importer. QR sessions from older builds should be recreated after updating both devices.

## Remote navigation

TV OK selects a profile without starting VPN. Use the primary connection card or the profile menu to connect. Menu/Info, long press, and the on-screen Profile actions card expose details, edit, QR, delete and manual ordering. Play/Pause starts or stops; transition states prevent duplicate/reload races. Back closes the current screen/dialog. Groups and subscription updates are available on TV.

The QR screen keeps the code beside its controls. New pairing session invalidates the old token; expiration clears the QR. A TV without a camera can import a QR image. See [readiness plan](tv-readiness-plan.md) for the pre-merge device checklist.

## Security and limitations

- The pairing token is random (256 bits), comes only from the QR, and expires after 10 minutes.
- `/status` does not disclose a token or profile data. Receiving TVs disable export. Phone export is enabled only through the explicit sharing dialog.
- Servers bind to a private LAN IPv4 interface, not every interface; closing/backgrounding the sharing screen stops listening. No fallback/fabricated IP is used.
- HTTP is **not encrypted**. A trusted LAN is required; don't expose/port-forward port 8765. QR holders can import, or export when explicitly enabled. Protect the displayed QR.
- Browser tokens use a URL fragment and request header, not a query sent to the server. Wildcard CORS is disabled; Host/Origin checks prevent browser cross-origin access and DNS rebinding.
- Payloads are limited to 2 MiB; requests require JSON and Content-Length. Partial body reads are completed, truncated bodies rejected, and secrets are not written by the transfer server to logs.
- Imported configuration files use the upstream parser: profiles/outbounds are imported, not every routing rule or global app preference. Subscription URLs create persistent subscription groups with the upstream updater.
- Android TV UI remains this branch's default on all devices. Explicit Phone Mode persists across launches, including a launch from the TV home screen. Deep-link imports retain the upstream mobile handler without changing the saved mode.

## Regression checklist

- Switch TV → Phone, kill/relaunch, verify Phone persists. Switch back and relaunch. Launch from Android TV home: one launcher tile, correct saved mode.
- Exercise upstream `sn://subscription`, `clash://install-config`, and profile deep links on cold/warm launches.
- Import `ss://`, `vmess://`, `vless://`, `trojan://` directly: no HTTP download attempt.
- Open each Manual editor, including VLESS, save and verify the profile appears.
- Test browser paste, file upload, SOCKS5/HTTP builder, and HTTP(S) subscription import.
- Test native phone → TV and TV pull ← phone transfers; close/background each sharing screen and ensure the port closes.
- Confirm `/status` contains no token; missing/wrong/expired token gets 403; receiver export gets 403 even with a valid token; foreign Origin/Host is rejected.
- Verify split-body requests work, truncated/invalid/oversized bodies fail without importing; port-in-use/no LAN yields no usable QR.
- Run `./gradlew app:testPreviewDebugUnitTest` and `./gradlew app:assemblePreviewRelease`.

The phone launcher, profile editors and parser/updater APIs were checked against `master` and the working upstream `main`. Changes are isolated to the PR branch; upstream branches are not modified.

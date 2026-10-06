# Android emulator smoke tests

The prerelease workflow runs real instrumentation before private release signing is loaded. Failures block publication; there is no continue-on-error or automatic retry hiding failed assertions.

- Android 15 / API 35 / x86_64 Google APIs, 4KB pages.
- Android 15 / API 35 / x86_64 Google APIs ps16k, 16KB pages.
- Actual page size is asserted inside the installed APK test, not inferred from the AVD name.
- A real SagerNet application initializes real JNI core and Room. No Robolectric shadows.
- Eight tests per image: JNI/page size, mode picker/TV launch, common top/empty add chooser, advanced manual menu, clipboard import into Room, smartphone plus menu, reachable receiving QR screen and real authenticated LAN group roundtrip.
- DPAD input, bounded condition waits and disposable loopback profiles; no personal subscription/token/VPN consent or external server is required.
- Reports, logcat, device properties, screenshots and UI hierarchy are kept as 14-day CI artifacts. The debug APK is not published as a release.

These tests are not VPN throughput/failover tests, not physical TV remote acceptance, not Play certification, and not a pixel-perfect design audit. Animations are disabled for deterministic functional checks; real scrolling smoothness still needs physical-device acceptance. Native smoke uses x86_64, not ARM64.

Local execution: install SDK API 35, create/start a compatible AVD, build libcore.aar, then run `bash buildScript/run_emulator_smoke.sh 4096` (or 16384 on the ps16k image). The script requires all eight instrumentation tests to pass and captures diagnostics even on failure.

The first real-device runs found missing actual TV keyboard focus after touch-mode launch and a mode-picker card needing two taps. These were fixed in production rather than weakening the assertions. Screenshots/hierarchies are copied to disposable shell-owned Downloads before AGP uninstalls test APKs.

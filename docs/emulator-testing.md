# Android emulator smoke tests

The prerelease workflow runs real instrumentation before private release signing is loaded. Failures block publication; there is no continue-on-error or automatic retry hiding failed assertions.

- Android 15 / API 35 / x86_64 Google APIs, 4KB pages.
- Android 15 / API 35 / x86_64 Google APIs ps16k, 16KB pages.
- Actual page size is asserted inside the installed APK test, not inferred from the AVD name.
- A real SagerNet application initializes real JNI core and Room. No Robolectric shadows.
- Ten tests per image: JNI/page size, mode picker/TV launch, common top/empty add chooser, advanced manual menu, clipboard import into Room, smartphone plus menu, reachable receiving QR screen and real authenticated LAN group roundtrip, shared opt-in LAN screen in portrait/landscape and actual socket discovery plus duplicate-safe Room save.
- DPAD input, bounded condition waits and disposable loopback profiles; no personal subscription/token/VPN consent or external server is required.
- Reports, logcat, device properties, screenshots and UI hierarchy are kept as 14-day CI artifacts. The debug APK is not published as a release.

These tests are not VPN throughput/failover tests, not physical TV remote acceptance, not Play certification, and not a pixel-perfect design audit. Animations are disabled for deterministic functional checks; real scrolling smoothness still needs physical-device acceptance. Native smoke uses x86_64, not ARM64.

Local execution: install SDK API 35, create/start a compatible AVD, build libcore.aar, then run `bash buildScript/run_emulator_smoke.sh 4096` (or 16384 on the ps16k image). The script requires all ten instrumentation tests to pass and captures diagnostics even on failure.

The first real-device runs found missing actual TV keyboard focus after touch-mode launch and a mode-picker card needing two taps. These were fixed in production rather than weakening the assertions. Screenshots/hierarchies are copied to disposable shell-owned Downloads before AGP uninstalls test APKs.

During connected tests the Gradle heap is capped at 1536MB with two workers and parallel projects disabled; the AVD has 3072MB. This reduces host/emulator memory contention, but is NOT proof of fixing the previously unexplained 16KB process exit. Existing exit-info/logcat/event diagnostics remain mandatory; failed assertions are not skipped or retried automatically.

## Current SDK renderer and infrastructure triage

Current Android documentation marks `swiftshader_indirect` deprecated since emulator 36.4.9. CI now uses `-gpu software`, the supported software backend selection rather than a deprecated indirect mode. Reference: https://developer.android.com/studio/run/emulator-acceleration . This is renderer/environment hygiene, not a proven root-cause fix for ART/system_server crashes.

Run 37533313556 recorded system_server SIGSEGV and framework NoSuchFieldError; run 37536015749 attempt 1 failed downloading the emulator SDK archive before boot, and its one clean retry recorded framework NoSuchMethodError before any 16KB tests. These are failures, not passes. No assertions or page-size checks are removed. The same runtime source passed Preview 4KB/16KB 10/10 and 146 JVM in 37536132863; that does not substitute for the separate OSS gate or certify the system image. Further repeated infrastructure failures require investigation rather than a retry loop.

## Уточнение обязательной матрицы после rc.134

rc.134/38102759021 attempts1/2: JVM223 и Android15/API35 4KB12/12 прошли; Android15/API35 `google_apis_ps16k` не завершил12 сценариев. В первом прогоне system ReferenceQueueDaemon NPE; во втором Google Play Services ReferenceQueueDaemon NPE, затем system_server SIGSEGV в WifiHandlerThread/ScanDetailCache. Только после system death тестовый процесс получил DeadSystemException/JNI pending exception. Это не green16KB и не доказанный дефект UI/core TunXBox; выпуск остановлен.

Повторять тот же образ бесконечно или игнорировать incomplete XML нельзя. Обязательный16KB smoke перенесён на официальный стабильный Android16/API36 `google_apis_ps16k` x86_64 (в SDK-каталоге ревизия7, stable/channel-0). Android15/API35 4KB остаётся. Все те же12 сценариев, точный source-derived inventory и отсутствие missing/failure/error/skip, реальные getconf PAGESIZE16384 и APK/native alignment/signature gates сохранены. Новый AVD-name не переиспользует старый образ. Фактические fingerprint/SDK/page-size сохраняются в диагностике.

[Официальный каталог образов Google](https://dl.google.com/android/repository/sys-img/google_apis/sys-img2-3.xml). API36/16KB не объявляется проверкой API35/16KB: старый образ остаётся открытым риском, нужны исправленный образ Google или физическое Android15/16KB устройство. Никаких обещаний поддержки всех OEM, скорости или VPN по одному emulator smoke. При новой системной ошибке публикация снова блокируется; smoke gate не превращается в optional.

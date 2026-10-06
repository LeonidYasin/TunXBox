#!/usr/bin/env bash
# Run only inside the emulator action, before release signing secrets are loaded.
set -euo pipefail
pages="${1:?Expected page size required}"
case "$pages" in 4096|16384) ;; *) echo "Unexpected page size" >&2; exit 2;; esac
out="build/emulator-results/$pages"
mkdir -p "$out"
collect() {
  adb logcat -d > "$out/logcat.txt" || true
  adb shell getprop > "$out/device-properties.txt" || true
  adb shell getconf PAGESIZE > "$out/page-size.txt" || true
  adb pull /sdcard/Android/data/com.tunxbox.app/files/smoke-artifacts "$out/screens" >/dev/null 2>&1 || true
  if [ -d app/build/reports/androidTests/connected ]; then cp -a app/build/reports/androidTests/connected "$out/reports"; fi
  if [ -d app/build/outputs/androidTest-results/connected ]; then cp -a app/build/outputs/androidTest-results/connected "$out/results"; fi
}
trap collect EXIT
adb logcat -c
./gradlew --no-daemon app:connectedPreviewDebugAndroidTest --stacktrace \
  -Pandroid.testInstrumentationRunnerArguments.class=io.nekohasekai.sagernet.ui.EmulatorSmokeTest \
  -Pandroid.testInstrumentationRunnerArguments.expectedPageSize="$pages"
python3 - "$out" <<'PYTEST'
import glob,sys,xml.etree.ElementTree as ET
paths=glob.glob('app/build/outputs/androidTest-results/connected/**/TEST-*.xml',recursive=True)
totals=dict(tests=0,failures=0,errors=0,skipped=0)
for path in paths:
    root=ET.parse(path).getroot()
    for key in totals: totals[key]+=int(root.get(key,'0'))
print('Emulator instrumentation summary:',totals)
assert totals['tests']==6 and all(totals[key]==0 for key in ('failures','errors','skipped')), 'Missing or unsuccessful device tests'
PYTEST

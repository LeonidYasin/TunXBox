# F-Droid preparation — not an inclusion claim

The draft recipe is deliberately disabled. Do not remove Disabled/disable before the following are resolved:

1. Obtain an explicit license/permission for pinned `MatsuriDayo/libneko` sources. The snapshot has no LICENSE; a public repository alone does not grant redistribution rights.
2. Confirm Yacd-meta licensing, original copyright notices and the right notices for bundled assets/fonts. Historical package.json declares MIT, but the selected snapshot has no standalone license text. Do not invent attribution.
3. Review GeoIP/Geosite **data-input** licenses separately from the GPL generator licenses. Generator source snapshots do not archive all upstream lists/data.
4. Provision pinned Go 1.26.8, Node 24.14.1, pnpm 8.15.9, JDK 17 and Android SDK/NDK on the actual F-Droid buildserver. The draft does NOT yet describe installation via approved srclibs/toolchains.
5. Run a clean F-Droid scanner/build and inspect the resolved Android, Go and JS dependency licenses. `fdroid lint` alone is not enough.
6. Review actual emulator/device screenshots and commit accepted Fastlane images. Never use invented UI screenshots. `catalog_icon.py` generates only the existing launcher icon.
7. Replace preparation branch with a tagged, reviewed release SHA. Set AutoUpdateMode only after checking actual APK versionName/versionCode and tag filtering.
8. Test release APK runtime on ARM32/ARM64 and 4KB/16KB pages; compare dashboard APIs, profiles, subscriptions, routing databases and local transfer. Existing device gates must stay enabled.

## Preserved capabilities

No protocol, TV/phone UI, dashboard, QR transfer, native binding tag or user-triggered routing-data update is removed. Source pinning changes build inputs, not the feature list. The source-built dashboard is close in age to the existing bundled 2023 dashboard; exact original archive/source correspondence is not claimed.

## Build properties

`-Ptunxbox.fdroidUnsigned=true` makes release output unsigned for downstream signing. It is opt-in, rejects non-F-Droid task invocations and does not alter ordinary stable/preview signing.
`-Ptunxbox.universalOnly=true` disables ABI splitting for one universal APK; normal ABI-specific builds remain unchanged.

The release version code is `VERSION_CODE * 1000000 + 999999`; RC builds are excluded from the stable tag pattern. Existing version codes and package name are retained.

## Catalog choices

The main F-Droid repo compiles the app itself. IzzyOnDroid distributes developer-signed APKs but has separate inclusion and AI-code policies; do not claim acceptance or conceal code provenance. GitHub Releases and the existing website remain available regardless of store submission. No store submission is performed by these changes.

## Local preparation

```sh
python3 -m unittest discover -s buildScript -p 'test_*.py'
python3 buildScript/sources/fetch.py prefetch
bash buildScript/fdroid/prebuild.sh
./gradlew app:assembleFdroidRelease -Ptunxbox.fdroidUnsigned=true -Ptunxbox.universalOnly=true
python3 buildScript/sources/catalog_icon.py
```

The prebuild requires the pinned tools above. Do not rely on an Actions AAR cache as a source substitute. Keep the old checked-in dashboard ZIP only for legacy build paths; the draft recipe removes and replaces it with a source-built version.

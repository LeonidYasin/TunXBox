# Полный реестр файлов репозитория

Сгенерирован из tracked Git tree. Это inventory, не утверждение, что каждый ресурс/класс является активной UI-функцией. Generated/ignored AAR, downloaded cores/assets, private signing material и build outputs сюда не входят.

Исторический tracked `release.keystore` — только имя артефакта; его содержимое не раскрывается и он не является гарантией текущей private pinned signing identity.

Для обновления: `python3 buildScript/check_project_docs.py --update-index`. Для source archive можно явно передать JSON-массив путей через `--inventory`.

Всего tracked файлов: **650**.

| Раздел | Файлов |
|---|---|
| `.github` | 8 |
| `app` | 12 |
| `app/src/androidTest` | 1 |
| `app/src/main` | 11 |
| `app/src/main/java/com/github/shadowsocks/plugin` | 2 |
| `app/src/main/java/io/nekohasekai/sagernet/BootReceiver.kt` | 1 |
| `app/src/main/java/io/nekohasekai/sagernet/Constants.kt` | 1 |
| `app/src/main/java/io/nekohasekai/sagernet/QuickToggleShortcut.kt` | 1 |
| `app/src/main/java/io/nekohasekai/sagernet/SagerNet.kt` | 1 |
| `app/src/main/java/io/nekohasekai/sagernet/aidl/SpeedDisplayData.kt` | 1 |
| `app/src/main/java/io/nekohasekai/sagernet/aidl/TrafficData.kt` | 1 |
| `app/src/main/java/io/nekohasekai/sagernet/bg/AbstractInstance.kt` | 1 |
| `app/src/main/java/io/nekohasekai/sagernet/bg/BaseService.kt` | 1 |
| `app/src/main/java/io/nekohasekai/sagernet/bg/Executable.kt` | 1 |
| `app/src/main/java/io/nekohasekai/sagernet/bg/GuardedProcessPool.kt` | 1 |
| `app/src/main/java/io/nekohasekai/sagernet/bg/ProxyService.kt` | 1 |
| `app/src/main/java/io/nekohasekai/sagernet/bg/SagerConnection.kt` | 1 |
| `app/src/main/java/io/nekohasekai/sagernet/bg/ServiceNotification.kt` | 1 |
| `app/src/main/java/io/nekohasekai/sagernet/bg/SubscriptionUpdater.kt` | 1 |
| `app/src/main/java/io/nekohasekai/sagernet/bg/TileService.kt` | 1 |
| `app/src/main/java/io/nekohasekai/sagernet/bg/VpnService.kt` | 1 |
| `app/src/main/java/io/nekohasekai/sagernet/bg/proto` | 6 |
| `app/src/main/java/io/nekohasekai/sagernet/database/DataStore.kt` | 1 |
| `app/src/main/java/io/nekohasekai/sagernet/database/GroupManager.kt` | 1 |
| `app/src/main/java/io/nekohasekai/sagernet/database/ParcelizeBridge.java` | 1 |
| `app/src/main/java/io/nekohasekai/sagernet/database/ProfileManager.kt` | 1 |
| `app/src/main/java/io/nekohasekai/sagernet/database/ProxyEntity.kt` | 1 |
| `app/src/main/java/io/nekohasekai/sagernet/database/ProxyGroup.kt` | 1 |
| `app/src/main/java/io/nekohasekai/sagernet/database/RuleEntity.kt` | 1 |
| `app/src/main/java/io/nekohasekai/sagernet/database/SagerDatabase.kt` | 1 |
| `app/src/main/java/io/nekohasekai/sagernet/database/StringCollectionConverter.kt` | 1 |
| `app/src/main/java/io/nekohasekai/sagernet/database/SubscriptionBean.java` | 1 |
| `app/src/main/java/io/nekohasekai/sagernet/database/preference` | 5 |
| `app/src/main/java/io/nekohasekai/sagernet/fmt/AbstractBean.java` | 1 |
| `app/src/main/java/io/nekohasekai/sagernet/fmt/ConfigBuilder.kt` | 1 |
| `app/src/main/java/io/nekohasekai/sagernet/fmt/KryoConverters.java` | 1 |
| `app/src/main/java/io/nekohasekai/sagernet/fmt/PluginEntry.kt` | 1 |
| `app/src/main/java/io/nekohasekai/sagernet/fmt/Serializable.kt` | 1 |
| `app/src/main/java/io/nekohasekai/sagernet/fmt/TypeMap.kt` | 1 |
| `app/src/main/java/io/nekohasekai/sagernet/fmt/UniversalFmt.kt` | 1 |
| `app/src/main/java/io/nekohasekai/sagernet/fmt/gson` | 1 |
| `app/src/main/java/io/nekohasekai/sagernet/fmt/http` | 2 |
| `app/src/main/java/io/nekohasekai/sagernet/fmt/hysteria` | 2 |
| `app/src/main/java/io/nekohasekai/sagernet/fmt/internal` | 2 |
| `app/src/main/java/io/nekohasekai/sagernet/fmt/mieru` | 2 |
| `app/src/main/java/io/nekohasekai/sagernet/fmt/naive` | 2 |
| `app/src/main/java/io/nekohasekai/sagernet/fmt/shadowsocks` | 2 |
| `app/src/main/java/io/nekohasekai/sagernet/fmt/socks` | 2 |
| `app/src/main/java/io/nekohasekai/sagernet/fmt/ssh` | 2 |
| `app/src/main/java/io/nekohasekai/sagernet/fmt/trojan` | 2 |
| `app/src/main/java/io/nekohasekai/sagernet/fmt/trojan_go` | 2 |
| `app/src/main/java/io/nekohasekai/sagernet/fmt/tuic` | 2 |
| `app/src/main/java/io/nekohasekai/sagernet/fmt/v2ray` | 3 |
| `app/src/main/java/io/nekohasekai/sagernet/fmt/wireguard` | 2 |
| `app/src/main/java/io/nekohasekai/sagernet/group/GroupInterfaceAdapter.kt` | 1 |
| `app/src/main/java/io/nekohasekai/sagernet/group/GroupUpdater.kt` | 1 |
| `app/src/main/java/io/nekohasekai/sagernet/group/RawUpdater.kt` | 1 |
| `app/src/main/java/io/nekohasekai/sagernet/group/SubscriptionUpdateGuard.kt` | 1 |
| `app/src/main/java/io/nekohasekai/sagernet/ktx/Asyncs.kt` | 1 |
| `app/src/main/java/io/nekohasekai/sagernet/ktx/Browsers.kt` | 1 |
| `app/src/main/java/io/nekohasekai/sagernet/ktx/Dialogs.kt` | 1 |
| `app/src/main/java/io/nekohasekai/sagernet/ktx/Dimens.kt` | 1 |
| `app/src/main/java/io/nekohasekai/sagernet/ktx/Formats.kt` | 1 |
| `app/src/main/java/io/nekohasekai/sagernet/ktx/Kryos.kt` | 1 |
| `app/src/main/java/io/nekohasekai/sagernet/ktx/Layouts.kt` | 1 |
| `app/src/main/java/io/nekohasekai/sagernet/ktx/Logs.kt` | 1 |
| `app/src/main/java/io/nekohasekai/sagernet/ktx/Nets.kt` | 1 |
| `app/src/main/java/io/nekohasekai/sagernet/ktx/Preferences.kt` | 1 |
| `app/src/main/java/io/nekohasekai/sagernet/ktx/TvDeviceUtil.kt` | 1 |
| `app/src/main/java/io/nekohasekai/sagernet/ktx/Utils.kt` | 1 |
| `app/src/main/java/io/nekohasekai/sagernet/plugin/PluginManager.kt` | 1 |
| `app/src/main/java/io/nekohasekai/sagernet/ui/AboutFragment.kt` | 1 |
| `app/src/main/java/io/nekohasekai/sagernet/ui/AppLifecycleActions.kt` | 1 |
| `app/src/main/java/io/nekohasekai/sagernet/ui/AppListActivity.kt` | 1 |
| `app/src/main/java/io/nekohasekai/sagernet/ui/AppManagerActivity.kt` | 1 |
| `app/src/main/java/io/nekohasekai/sagernet/ui/AssetsActivity.kt` | 1 |
| `app/src/main/java/io/nekohasekai/sagernet/ui/BackupFragment.kt` | 1 |
| `app/src/main/java/io/nekohasekai/sagernet/ui/BlankActivity.kt` | 1 |
| `app/src/main/java/io/nekohasekai/sagernet/ui/ConfigurationFragment.kt` | 1 |
| `app/src/main/java/io/nekohasekai/sagernet/ui/GroupFragment.kt` | 1 |
| `app/src/main/java/io/nekohasekai/sagernet/ui/GroupSettingsActivity.kt` | 1 |
| `app/src/main/java/io/nekohasekai/sagernet/ui/LogcatFragment.kt` | 1 |
| `app/src/main/java/io/nekohasekai/sagernet/ui/MainActivity.kt` | 1 |
| `app/src/main/java/io/nekohasekai/sagernet/ui/MainActivityTv.kt` | 1 |
| `app/src/main/java/io/nekohasekai/sagernet/ui/ModeSelectionActivity.kt` | 1 |
| `app/src/main/java/io/nekohasekai/sagernet/ui/NamedFragment.kt` | 1 |
| `app/src/main/java/io/nekohasekai/sagernet/ui/NetworkFragment.kt` | 1 |
| `app/src/main/java/io/nekohasekai/sagernet/ui/ProfileCreationActions.kt` | 1 |
| `app/src/main/java/io/nekohasekai/sagernet/ui/ProfileSelectActivity.kt` | 1 |
| `app/src/main/java/io/nekohasekai/sagernet/ui/ProjectLinks.kt` | 1 |
| `app/src/main/java/io/nekohasekai/sagernet/ui/PromotionsFragment.kt` | 1 |
| `app/src/main/java/io/nekohasekai/sagernet/ui/QuickDisableShortcut.kt` | 1 |
| `app/src/main/java/io/nekohasekai/sagernet/ui/QuickEnableShortcut.kt` | 1 |
| `app/src/main/java/io/nekohasekai/sagernet/ui/ReleaseUpdatePolicy.kt` | 1 |
| `app/src/main/java/io/nekohasekai/sagernet/ui/RemoteFocusHighlighter.kt` | 1 |
| `app/src/main/java/io/nekohasekai/sagernet/ui/RemoteProfileFocus.kt` | 1 |
| `app/src/main/java/io/nekohasekai/sagernet/ui/RemoteReadability.kt` | 1 |
| `app/src/main/java/io/nekohasekai/sagernet/ui/RemoteRowActions.kt` | 1 |
| `app/src/main/java/io/nekohasekai/sagernet/ui/RouteFragment.kt` | 1 |
| `app/src/main/java/io/nekohasekai/sagernet/ui/RouteSettingsActivity.kt` | 1 |
| `app/src/main/java/io/nekohasekai/sagernet/ui/ScannerActivity.kt` | 1 |
| `app/src/main/java/io/nekohasekai/sagernet/ui/SettingsFragment.kt` | 1 |
| `app/src/main/java/io/nekohasekai/sagernet/ui/SettingsPreferenceFragment.kt` | 1 |
| `app/src/main/java/io/nekohasekai/sagernet/ui/StunActivity.kt` | 1 |
| `app/src/main/java/io/nekohasekai/sagernet/ui/SwitchActivity.kt` | 1 |
| `app/src/main/java/io/nekohasekai/sagernet/ui/ThemedActivity.kt` | 1 |
| `app/src/main/java/io/nekohasekai/sagernet/ui/ToolbarFragment.kt` | 1 |
| `app/src/main/java/io/nekohasekai/sagernet/ui/ToolsFragment.kt` | 1 |
| `app/src/main/java/io/nekohasekai/sagernet/ui/VpnRequestActivity.kt` | 1 |
| `app/src/main/java/io/nekohasekai/sagernet/ui/WebviewFragment.kt` | 1 |
| `app/src/main/java/io/nekohasekai/sagernet/ui/profile` | 16 |
| `app/src/main/java/io/nekohasekai/sagernet/ui/tv` | 19 |
| `app/src/main/java/io/nekohasekai/sagernet/utils/Commandline.kt` | 1 |
| `app/src/main/java/io/nekohasekai/sagernet/utils/CrashHandler.kt` | 1 |
| `app/src/main/java/io/nekohasekai/sagernet/utils/DefaultNetworkListener.kt` | 1 |
| `app/src/main/java/io/nekohasekai/sagernet/utils/PackageCache.kt` | 1 |
| `app/src/main/java/io/nekohasekai/sagernet/utils/Subnet.kt` | 1 |
| `app/src/main/java/io/nekohasekai/sagernet/utils/Theme.kt` | 1 |
| `app/src/main/java/io/nekohasekai/sagernet/widget/AppListPreference.kt` | 1 |
| `app/src/main/java/io/nekohasekai/sagernet/widget/AutoCollapseTextView.kt` | 1 |
| `app/src/main/java/io/nekohasekai/sagernet/widget/FabProgressBehavior.kt` | 1 |
| `app/src/main/java/io/nekohasekai/sagernet/widget/GroupPreference.kt` | 1 |
| `app/src/main/java/io/nekohasekai/sagernet/widget/LinkOrContentPreference.kt` | 1 |
| `app/src/main/java/io/nekohasekai/sagernet/widget/OutboundPreference.kt` | 1 |
| `app/src/main/java/io/nekohasekai/sagernet/widget/QRCodeDialog.kt` | 1 |
| `app/src/main/java/io/nekohasekai/sagernet/widget/ServiceButton.kt` | 1 |
| `app/src/main/java/io/nekohasekai/sagernet/widget/StatsBar.kt` | 1 |
| `app/src/main/java/io/nekohasekai/sagernet/widget/UndoSnackbarManager.kt` | 1 |
| `app/src/main/java/io/nekohasekai/sagernet/widget/UserAgentPreference.kt` | 1 |
| `app/src/main/java/io/nekohasekai/sagernet/widget/WindowInsetsListeners.kt` | 1 |
| `app/src/main/java/moe/matsuri/nb4a/NativeInterface.kt` | 1 |
| `app/src/main/java/moe/matsuri/nb4a/Protocols.kt` | 1 |
| `app/src/main/java/moe/matsuri/nb4a/SingBoxOptions.java` | 1 |
| `app/src/main/java/moe/matsuri/nb4a/SingBoxOptionsUtil.kt` | 1 |
| `app/src/main/java/moe/matsuri/nb4a/TempDatabase.kt` | 1 |
| `app/src/main/java/moe/matsuri/nb4a/net` | 1 |
| `app/src/main/java/moe/matsuri/nb4a/plugin` | 1 |
| `app/src/main/java/moe/matsuri/nb4a/proxy` | 11 |
| `app/src/main/java/moe/matsuri/nb4a/ui` | 11 |
| `app/src/main/java/moe/matsuri/nb4a/utils` | 6 |
| `app/src/main/res/color` | 6 |
| `app/src/main/res/drawable` | 125 |
| `app/src/main/res/drawable-v26` | 3 |
| `app/src/main/res/font` | 1 |
| `app/src/main/res/layout` | 47 |
| `app/src/main/res/menu` | 18 |
| `app/src/main/res/mipmap-anydpi-v26` | 1 |
| `app/src/main/res/mipmap-hdpi` | 2 |
| `app/src/main/res/mipmap-mdpi` | 2 |
| `app/src/main/res/mipmap-xhdpi` | 2 |
| `app/src/main/res/mipmap-xxhdpi` | 2 |
| `app/src/main/res/mipmap-xxxhdpi` | 2 |
| `app/src/main/res/raw` | 4 |
| `app/src/main/res/raw-zh-rCN` | 4 |
| `app/src/main/res/resources.properties` | 1 |
| `app/src/main/res/values` | 17 |
| `app/src/main/res/values-ar` | 1 |
| `app/src/main/res/values-be` | 1 |
| `app/src/main/res/values-de` | 1 |
| `app/src/main/res/values-es` | 1 |
| `app/src/main/res/values-fa` | 1 |
| `app/src/main/res/values-fr` | 1 |
| `app/src/main/res/values-in` | 1 |
| `app/src/main/res/values-it` | 1 |
| `app/src/main/res/values-ja` | 1 |
| `app/src/main/res/values-ko` | 1 |
| `app/src/main/res/values-nb-rNO` | 1 |
| `app/src/main/res/values-night` | 2 |
| `app/src/main/res/values-nl` | 1 |
| `app/src/main/res/values-pt-rBR` | 1 |
| `app/src/main/res/values-ru` | 7 |
| `app/src/main/res/values-tr` | 1 |
| `app/src/main/res/values-uk` | 1 |
| `app/src/main/res/values-w600dp-h480dp` | 1 |
| `app/src/main/res/values-zh-rCN` | 1 |
| `app/src/main/res/values-zh-rHK` | 1 |
| `app/src/main/res/values-zh-rTW` | 1 |
| `app/src/main/res/xml` | 24 |
| `app/src/test` | 11 |
| `buildScript` | 24 |
| `buildSrc` | 2 |
| `docs` | 15 |
| `gradle` | 2 |
| `libcore` | 42 |
| `Корень` | 15 |

## .github

- [`.github/ISSUE_TEMPLATE/bug-report-en.md`](../../.github/ISSUE_TEMPLATE/bug-report-en.md)
- [`.github/ISSUE_TEMPLATE/bug-report-zh_cn.md`](../../.github/ISSUE_TEMPLATE/bug-report-zh_cn.md)
- [`.github/ISSUE_TEMPLATE/feature_request-en.md`](../../.github/ISSUE_TEMPLATE/feature_request-en.md)
- [`.github/ISSUE_TEMPLATE/feature_request-zh_cn.md`](../../.github/ISSUE_TEMPLATE/feature_request-zh_cn.md)
- [`.github/workflows/prerelease-from-pr.yml`](../../.github/workflows/prerelease-from-pr.yml)
- [`.github/workflows/preview.yml`](../../.github/workflows/preview.yml)
- [`.github/workflows/release.yml`](../../.github/workflows/release.yml)
- [`.github/workflows/signing-backup.yml`](../../.github/workflows/signing-backup.yml)

## app

- [`app/.gitignore`](../../app/.gitignore)
- [`app/build.gradle.kts`](../../app/build.gradle.kts)
- [`app/executableSo/.gitignore`](../../app/executableSo/.gitignore)
- [`app/proguard-rules.pro`](../../app/proguard-rules.pro)
- [`app/schemas/io.nekohasekai.sagernet.database.SagerDatabase/1.json`](../../app/schemas/io.nekohasekai.sagernet.database.SagerDatabase/1.json)
- [`app/schemas/io.nekohasekai.sagernet.database.SagerDatabase/2.json`](../../app/schemas/io.nekohasekai.sagernet.database.SagerDatabase/2.json)
- [`app/schemas/io.nekohasekai.sagernet.database.SagerDatabase/3.json`](../../app/schemas/io.nekohasekai.sagernet.database.SagerDatabase/3.json)
- [`app/schemas/io.nekohasekai.sagernet.database.SagerDatabase/4.json`](../../app/schemas/io.nekohasekai.sagernet.database.SagerDatabase/4.json)
- [`app/schemas/io.nekohasekai.sagernet.database.SagerDatabase/5.json`](../../app/schemas/io.nekohasekai.sagernet.database.SagerDatabase/5.json)
- [`app/schemas/io.nekohasekai.sagernet.database.SagerDatabase/6.json`](../../app/schemas/io.nekohasekai.sagernet.database.SagerDatabase/6.json)
- [`app/schemas/io.nekohasekai.sagernet.database.preference.PublicDatabase/1.json`](../../app/schemas/io.nekohasekai.sagernet.database.preference.PublicDatabase/1.json)
- [`app/schemas/moe.matsuri.nb4a.TempDatabase/1.json`](../../app/schemas/moe.matsuri.nb4a.TempDatabase/1.json)

## app/src/androidTest

- [`app/src/androidTest/java/io/nekohasekai/sagernet/ui/EmulatorSmokeTest.kt`](../../app/src/androidTest/java/io/nekohasekai/sagernet/ui/EmulatorSmokeTest.kt)

## app/src/main

- [`app/src/main/AndroidManifest.xml`](../../app/src/main/AndroidManifest.xml)
- [`app/src/main/aidl/io/nekohasekai/sagernet/aidl/ISagerNetService.aidl`](../../app/src/main/aidl/io/nekohasekai/sagernet/aidl/ISagerNetService.aidl)
- [`app/src/main/aidl/io/nekohasekai/sagernet/aidl/ISagerNetServiceCallback.aidl`](../../app/src/main/aidl/io/nekohasekai/sagernet/aidl/ISagerNetServiceCallback.aidl)
- [`app/src/main/aidl/io/nekohasekai/sagernet/aidl/SpeedDisplayData.aidl`](../../app/src/main/aidl/io/nekohasekai/sagernet/aidl/SpeedDisplayData.aidl)
- [`app/src/main/aidl/io/nekohasekai/sagernet/aidl/TrafficData.aidl`](../../app/src/main/aidl/io/nekohasekai/sagernet/aidl/TrafficData.aidl)
- [`app/src/main/assets/LICENSE`](../../app/src/main/assets/LICENSE)
- [`app/src/main/assets/proxy_packagename.txt`](../../app/src/main/assets/proxy_packagename.txt)
- [`app/src/main/assets/tv-transfer.html`](../../app/src/main/assets/tv-transfer.html)
- [`app/src/main/assets/yacd.version.txt`](../../app/src/main/assets/yacd.version.txt)
- [`app/src/main/assets/yacd.zip`](../../app/src/main/assets/yacd.zip)
- [`app/src/main/ic_launcher-playstore.png`](../../app/src/main/ic_launcher-playstore.png)

## app/src/main/java/com/github/shadowsocks/plugin

- [`app/src/main/java/com/github/shadowsocks/plugin/Utils.kt`](../../app/src/main/java/com/github/shadowsocks/plugin/Utils.kt)
- [`app/src/main/java/com/github/shadowsocks/plugin/fragment/AlertDialogFragment.kt`](../../app/src/main/java/com/github/shadowsocks/plugin/fragment/AlertDialogFragment.kt)

## app/src/main/java/io/nekohasekai/sagernet/BootReceiver.kt

- [`app/src/main/java/io/nekohasekai/sagernet/BootReceiver.kt`](../../app/src/main/java/io/nekohasekai/sagernet/BootReceiver.kt)

## app/src/main/java/io/nekohasekai/sagernet/Constants.kt

- [`app/src/main/java/io/nekohasekai/sagernet/Constants.kt`](../../app/src/main/java/io/nekohasekai/sagernet/Constants.kt)

## app/src/main/java/io/nekohasekai/sagernet/QuickToggleShortcut.kt

- [`app/src/main/java/io/nekohasekai/sagernet/QuickToggleShortcut.kt`](../../app/src/main/java/io/nekohasekai/sagernet/QuickToggleShortcut.kt)

## app/src/main/java/io/nekohasekai/sagernet/SagerNet.kt

- [`app/src/main/java/io/nekohasekai/sagernet/SagerNet.kt`](../../app/src/main/java/io/nekohasekai/sagernet/SagerNet.kt)

## app/src/main/java/io/nekohasekai/sagernet/aidl/SpeedDisplayData.kt

- [`app/src/main/java/io/nekohasekai/sagernet/aidl/SpeedDisplayData.kt`](../../app/src/main/java/io/nekohasekai/sagernet/aidl/SpeedDisplayData.kt)

## app/src/main/java/io/nekohasekai/sagernet/aidl/TrafficData.kt

- [`app/src/main/java/io/nekohasekai/sagernet/aidl/TrafficData.kt`](../../app/src/main/java/io/nekohasekai/sagernet/aidl/TrafficData.kt)

## app/src/main/java/io/nekohasekai/sagernet/bg/AbstractInstance.kt

- [`app/src/main/java/io/nekohasekai/sagernet/bg/AbstractInstance.kt`](../../app/src/main/java/io/nekohasekai/sagernet/bg/AbstractInstance.kt)

## app/src/main/java/io/nekohasekai/sagernet/bg/BaseService.kt

- [`app/src/main/java/io/nekohasekai/sagernet/bg/BaseService.kt`](../../app/src/main/java/io/nekohasekai/sagernet/bg/BaseService.kt)

## app/src/main/java/io/nekohasekai/sagernet/bg/Executable.kt

- [`app/src/main/java/io/nekohasekai/sagernet/bg/Executable.kt`](../../app/src/main/java/io/nekohasekai/sagernet/bg/Executable.kt)

## app/src/main/java/io/nekohasekai/sagernet/bg/GuardedProcessPool.kt

- [`app/src/main/java/io/nekohasekai/sagernet/bg/GuardedProcessPool.kt`](../../app/src/main/java/io/nekohasekai/sagernet/bg/GuardedProcessPool.kt)

## app/src/main/java/io/nekohasekai/sagernet/bg/ProxyService.kt

- [`app/src/main/java/io/nekohasekai/sagernet/bg/ProxyService.kt`](../../app/src/main/java/io/nekohasekai/sagernet/bg/ProxyService.kt)

## app/src/main/java/io/nekohasekai/sagernet/bg/SagerConnection.kt

- [`app/src/main/java/io/nekohasekai/sagernet/bg/SagerConnection.kt`](../../app/src/main/java/io/nekohasekai/sagernet/bg/SagerConnection.kt)

## app/src/main/java/io/nekohasekai/sagernet/bg/ServiceNotification.kt

- [`app/src/main/java/io/nekohasekai/sagernet/bg/ServiceNotification.kt`](../../app/src/main/java/io/nekohasekai/sagernet/bg/ServiceNotification.kt)

## app/src/main/java/io/nekohasekai/sagernet/bg/SubscriptionUpdater.kt

- [`app/src/main/java/io/nekohasekai/sagernet/bg/SubscriptionUpdater.kt`](../../app/src/main/java/io/nekohasekai/sagernet/bg/SubscriptionUpdater.kt)

## app/src/main/java/io/nekohasekai/sagernet/bg/TileService.kt

- [`app/src/main/java/io/nekohasekai/sagernet/bg/TileService.kt`](../../app/src/main/java/io/nekohasekai/sagernet/bg/TileService.kt)

## app/src/main/java/io/nekohasekai/sagernet/bg/VpnService.kt

- [`app/src/main/java/io/nekohasekai/sagernet/bg/VpnService.kt`](../../app/src/main/java/io/nekohasekai/sagernet/bg/VpnService.kt)

## app/src/main/java/io/nekohasekai/sagernet/bg/proto

- [`app/src/main/java/io/nekohasekai/sagernet/bg/proto/BoxInstance.kt`](../../app/src/main/java/io/nekohasekai/sagernet/bg/proto/BoxInstance.kt)
- [`app/src/main/java/io/nekohasekai/sagernet/bg/proto/ProxyInstance.kt`](../../app/src/main/java/io/nekohasekai/sagernet/bg/proto/ProxyInstance.kt)
- [`app/src/main/java/io/nekohasekai/sagernet/bg/proto/TestInstance.kt`](../../app/src/main/java/io/nekohasekai/sagernet/bg/proto/TestInstance.kt)
- [`app/src/main/java/io/nekohasekai/sagernet/bg/proto/TrafficLooper.kt`](../../app/src/main/java/io/nekohasekai/sagernet/bg/proto/TrafficLooper.kt)
- [`app/src/main/java/io/nekohasekai/sagernet/bg/proto/TrafficUpdater.kt`](../../app/src/main/java/io/nekohasekai/sagernet/bg/proto/TrafficUpdater.kt)
- [`app/src/main/java/io/nekohasekai/sagernet/bg/proto/UrlTest.kt`](../../app/src/main/java/io/nekohasekai/sagernet/bg/proto/UrlTest.kt)

## app/src/main/java/io/nekohasekai/sagernet/database/DataStore.kt

- [`app/src/main/java/io/nekohasekai/sagernet/database/DataStore.kt`](../../app/src/main/java/io/nekohasekai/sagernet/database/DataStore.kt)

## app/src/main/java/io/nekohasekai/sagernet/database/GroupManager.kt

- [`app/src/main/java/io/nekohasekai/sagernet/database/GroupManager.kt`](../../app/src/main/java/io/nekohasekai/sagernet/database/GroupManager.kt)

## app/src/main/java/io/nekohasekai/sagernet/database/ParcelizeBridge.java

- [`app/src/main/java/io/nekohasekai/sagernet/database/ParcelizeBridge.java`](../../app/src/main/java/io/nekohasekai/sagernet/database/ParcelizeBridge.java)

## app/src/main/java/io/nekohasekai/sagernet/database/ProfileManager.kt

- [`app/src/main/java/io/nekohasekai/sagernet/database/ProfileManager.kt`](../../app/src/main/java/io/nekohasekai/sagernet/database/ProfileManager.kt)

## app/src/main/java/io/nekohasekai/sagernet/database/ProxyEntity.kt

- [`app/src/main/java/io/nekohasekai/sagernet/database/ProxyEntity.kt`](../../app/src/main/java/io/nekohasekai/sagernet/database/ProxyEntity.kt)

## app/src/main/java/io/nekohasekai/sagernet/database/ProxyGroup.kt

- [`app/src/main/java/io/nekohasekai/sagernet/database/ProxyGroup.kt`](../../app/src/main/java/io/nekohasekai/sagernet/database/ProxyGroup.kt)

## app/src/main/java/io/nekohasekai/sagernet/database/RuleEntity.kt

- [`app/src/main/java/io/nekohasekai/sagernet/database/RuleEntity.kt`](../../app/src/main/java/io/nekohasekai/sagernet/database/RuleEntity.kt)

## app/src/main/java/io/nekohasekai/sagernet/database/SagerDatabase.kt

- [`app/src/main/java/io/nekohasekai/sagernet/database/SagerDatabase.kt`](../../app/src/main/java/io/nekohasekai/sagernet/database/SagerDatabase.kt)

## app/src/main/java/io/nekohasekai/sagernet/database/StringCollectionConverter.kt

- [`app/src/main/java/io/nekohasekai/sagernet/database/StringCollectionConverter.kt`](../../app/src/main/java/io/nekohasekai/sagernet/database/StringCollectionConverter.kt)

## app/src/main/java/io/nekohasekai/sagernet/database/SubscriptionBean.java

- [`app/src/main/java/io/nekohasekai/sagernet/database/SubscriptionBean.java`](../../app/src/main/java/io/nekohasekai/sagernet/database/SubscriptionBean.java)

## app/src/main/java/io/nekohasekai/sagernet/database/preference

- [`app/src/main/java/io/nekohasekai/sagernet/database/preference/EditTextPreferenceModifiers.kt`](../../app/src/main/java/io/nekohasekai/sagernet/database/preference/EditTextPreferenceModifiers.kt)
- [`app/src/main/java/io/nekohasekai/sagernet/database/preference/KeyValuePair.kt`](../../app/src/main/java/io/nekohasekai/sagernet/database/preference/KeyValuePair.kt)
- [`app/src/main/java/io/nekohasekai/sagernet/database/preference/OnPreferenceDataStoreChangeListener.kt`](../../app/src/main/java/io/nekohasekai/sagernet/database/preference/OnPreferenceDataStoreChangeListener.kt)
- [`app/src/main/java/io/nekohasekai/sagernet/database/preference/PublicDatabase.kt`](../../app/src/main/java/io/nekohasekai/sagernet/database/preference/PublicDatabase.kt)
- [`app/src/main/java/io/nekohasekai/sagernet/database/preference/RoomPreferenceDataStore.kt`](../../app/src/main/java/io/nekohasekai/sagernet/database/preference/RoomPreferenceDataStore.kt)

## app/src/main/java/io/nekohasekai/sagernet/fmt/AbstractBean.java

- [`app/src/main/java/io/nekohasekai/sagernet/fmt/AbstractBean.java`](../../app/src/main/java/io/nekohasekai/sagernet/fmt/AbstractBean.java)

## app/src/main/java/io/nekohasekai/sagernet/fmt/ConfigBuilder.kt

- [`app/src/main/java/io/nekohasekai/sagernet/fmt/ConfigBuilder.kt`](../../app/src/main/java/io/nekohasekai/sagernet/fmt/ConfigBuilder.kt)

## app/src/main/java/io/nekohasekai/sagernet/fmt/KryoConverters.java

- [`app/src/main/java/io/nekohasekai/sagernet/fmt/KryoConverters.java`](../../app/src/main/java/io/nekohasekai/sagernet/fmt/KryoConverters.java)

## app/src/main/java/io/nekohasekai/sagernet/fmt/PluginEntry.kt

- [`app/src/main/java/io/nekohasekai/sagernet/fmt/PluginEntry.kt`](../../app/src/main/java/io/nekohasekai/sagernet/fmt/PluginEntry.kt)

## app/src/main/java/io/nekohasekai/sagernet/fmt/Serializable.kt

- [`app/src/main/java/io/nekohasekai/sagernet/fmt/Serializable.kt`](../../app/src/main/java/io/nekohasekai/sagernet/fmt/Serializable.kt)

## app/src/main/java/io/nekohasekai/sagernet/fmt/TypeMap.kt

- [`app/src/main/java/io/nekohasekai/sagernet/fmt/TypeMap.kt`](../../app/src/main/java/io/nekohasekai/sagernet/fmt/TypeMap.kt)

## app/src/main/java/io/nekohasekai/sagernet/fmt/UniversalFmt.kt

- [`app/src/main/java/io/nekohasekai/sagernet/fmt/UniversalFmt.kt`](../../app/src/main/java/io/nekohasekai/sagernet/fmt/UniversalFmt.kt)

## app/src/main/java/io/nekohasekai/sagernet/fmt/gson

- [`app/src/main/java/io/nekohasekai/sagernet/fmt/gson/GsonConverters.java`](../../app/src/main/java/io/nekohasekai/sagernet/fmt/gson/GsonConverters.java)

## app/src/main/java/io/nekohasekai/sagernet/fmt/http

- [`app/src/main/java/io/nekohasekai/sagernet/fmt/http/HttpBean.java`](../../app/src/main/java/io/nekohasekai/sagernet/fmt/http/HttpBean.java)
- [`app/src/main/java/io/nekohasekai/sagernet/fmt/http/HttpFmt.kt`](../../app/src/main/java/io/nekohasekai/sagernet/fmt/http/HttpFmt.kt)

## app/src/main/java/io/nekohasekai/sagernet/fmt/hysteria

- [`app/src/main/java/io/nekohasekai/sagernet/fmt/hysteria/HysteriaBean.java`](../../app/src/main/java/io/nekohasekai/sagernet/fmt/hysteria/HysteriaBean.java)
- [`app/src/main/java/io/nekohasekai/sagernet/fmt/hysteria/HysteriaFmt.kt`](../../app/src/main/java/io/nekohasekai/sagernet/fmt/hysteria/HysteriaFmt.kt)

## app/src/main/java/io/nekohasekai/sagernet/fmt/internal

- [`app/src/main/java/io/nekohasekai/sagernet/fmt/internal/ChainBean.java`](../../app/src/main/java/io/nekohasekai/sagernet/fmt/internal/ChainBean.java)
- [`app/src/main/java/io/nekohasekai/sagernet/fmt/internal/InternalBean.java`](../../app/src/main/java/io/nekohasekai/sagernet/fmt/internal/InternalBean.java)

## app/src/main/java/io/nekohasekai/sagernet/fmt/mieru

- [`app/src/main/java/io/nekohasekai/sagernet/fmt/mieru/MieruBean.java`](../../app/src/main/java/io/nekohasekai/sagernet/fmt/mieru/MieruBean.java)
- [`app/src/main/java/io/nekohasekai/sagernet/fmt/mieru/MieruFmt.kt`](../../app/src/main/java/io/nekohasekai/sagernet/fmt/mieru/MieruFmt.kt)

## app/src/main/java/io/nekohasekai/sagernet/fmt/naive

- [`app/src/main/java/io/nekohasekai/sagernet/fmt/naive/NaiveBean.java`](../../app/src/main/java/io/nekohasekai/sagernet/fmt/naive/NaiveBean.java)
- [`app/src/main/java/io/nekohasekai/sagernet/fmt/naive/NaiveFmt.kt`](../../app/src/main/java/io/nekohasekai/sagernet/fmt/naive/NaiveFmt.kt)

## app/src/main/java/io/nekohasekai/sagernet/fmt/shadowsocks

- [`app/src/main/java/io/nekohasekai/sagernet/fmt/shadowsocks/ShadowsocksBean.java`](../../app/src/main/java/io/nekohasekai/sagernet/fmt/shadowsocks/ShadowsocksBean.java)
- [`app/src/main/java/io/nekohasekai/sagernet/fmt/shadowsocks/ShadowsocksFmt.kt`](../../app/src/main/java/io/nekohasekai/sagernet/fmt/shadowsocks/ShadowsocksFmt.kt)

## app/src/main/java/io/nekohasekai/sagernet/fmt/socks

- [`app/src/main/java/io/nekohasekai/sagernet/fmt/socks/SOCKSBean.java`](../../app/src/main/java/io/nekohasekai/sagernet/fmt/socks/SOCKSBean.java)
- [`app/src/main/java/io/nekohasekai/sagernet/fmt/socks/SOCKSFmt.kt`](../../app/src/main/java/io/nekohasekai/sagernet/fmt/socks/SOCKSFmt.kt)

## app/src/main/java/io/nekohasekai/sagernet/fmt/ssh

- [`app/src/main/java/io/nekohasekai/sagernet/fmt/ssh/SSHBean.java`](../../app/src/main/java/io/nekohasekai/sagernet/fmt/ssh/SSHBean.java)
- [`app/src/main/java/io/nekohasekai/sagernet/fmt/ssh/SSHFmt.kt`](../../app/src/main/java/io/nekohasekai/sagernet/fmt/ssh/SSHFmt.kt)

## app/src/main/java/io/nekohasekai/sagernet/fmt/trojan

- [`app/src/main/java/io/nekohasekai/sagernet/fmt/trojan/TrojanBean.java`](../../app/src/main/java/io/nekohasekai/sagernet/fmt/trojan/TrojanBean.java)
- [`app/src/main/java/io/nekohasekai/sagernet/fmt/trojan/TrojanFmt.kt`](../../app/src/main/java/io/nekohasekai/sagernet/fmt/trojan/TrojanFmt.kt)

## app/src/main/java/io/nekohasekai/sagernet/fmt/trojan_go

- [`app/src/main/java/io/nekohasekai/sagernet/fmt/trojan_go/TrojanGoBean.java`](../../app/src/main/java/io/nekohasekai/sagernet/fmt/trojan_go/TrojanGoBean.java)
- [`app/src/main/java/io/nekohasekai/sagernet/fmt/trojan_go/TrojanGoFmt.kt`](../../app/src/main/java/io/nekohasekai/sagernet/fmt/trojan_go/TrojanGoFmt.kt)

## app/src/main/java/io/nekohasekai/sagernet/fmt/tuic

- [`app/src/main/java/io/nekohasekai/sagernet/fmt/tuic/TuicBean.java`](../../app/src/main/java/io/nekohasekai/sagernet/fmt/tuic/TuicBean.java)
- [`app/src/main/java/io/nekohasekai/sagernet/fmt/tuic/TuicFmt.kt`](../../app/src/main/java/io/nekohasekai/sagernet/fmt/tuic/TuicFmt.kt)

## app/src/main/java/io/nekohasekai/sagernet/fmt/v2ray

- [`app/src/main/java/io/nekohasekai/sagernet/fmt/v2ray/StandardV2RayBean.java`](../../app/src/main/java/io/nekohasekai/sagernet/fmt/v2ray/StandardV2RayBean.java)
- [`app/src/main/java/io/nekohasekai/sagernet/fmt/v2ray/V2RayFmt.kt`](../../app/src/main/java/io/nekohasekai/sagernet/fmt/v2ray/V2RayFmt.kt)
- [`app/src/main/java/io/nekohasekai/sagernet/fmt/v2ray/VMessBean.java`](../../app/src/main/java/io/nekohasekai/sagernet/fmt/v2ray/VMessBean.java)

## app/src/main/java/io/nekohasekai/sagernet/fmt/wireguard

- [`app/src/main/java/io/nekohasekai/sagernet/fmt/wireguard/WireGuardBean.java`](../../app/src/main/java/io/nekohasekai/sagernet/fmt/wireguard/WireGuardBean.java)
- [`app/src/main/java/io/nekohasekai/sagernet/fmt/wireguard/WireGuardFmt.kt`](../../app/src/main/java/io/nekohasekai/sagernet/fmt/wireguard/WireGuardFmt.kt)

## app/src/main/java/io/nekohasekai/sagernet/group/GroupInterfaceAdapter.kt

- [`app/src/main/java/io/nekohasekai/sagernet/group/GroupInterfaceAdapter.kt`](../../app/src/main/java/io/nekohasekai/sagernet/group/GroupInterfaceAdapter.kt)

## app/src/main/java/io/nekohasekai/sagernet/group/GroupUpdater.kt

- [`app/src/main/java/io/nekohasekai/sagernet/group/GroupUpdater.kt`](../../app/src/main/java/io/nekohasekai/sagernet/group/GroupUpdater.kt)

## app/src/main/java/io/nekohasekai/sagernet/group/RawUpdater.kt

- [`app/src/main/java/io/nekohasekai/sagernet/group/RawUpdater.kt`](../../app/src/main/java/io/nekohasekai/sagernet/group/RawUpdater.kt)

## app/src/main/java/io/nekohasekai/sagernet/group/SubscriptionUpdateGuard.kt

- [`app/src/main/java/io/nekohasekai/sagernet/group/SubscriptionUpdateGuard.kt`](../../app/src/main/java/io/nekohasekai/sagernet/group/SubscriptionUpdateGuard.kt)

## app/src/main/java/io/nekohasekai/sagernet/ktx/Asyncs.kt

- [`app/src/main/java/io/nekohasekai/sagernet/ktx/Asyncs.kt`](../../app/src/main/java/io/nekohasekai/sagernet/ktx/Asyncs.kt)

## app/src/main/java/io/nekohasekai/sagernet/ktx/Browsers.kt

- [`app/src/main/java/io/nekohasekai/sagernet/ktx/Browsers.kt`](../../app/src/main/java/io/nekohasekai/sagernet/ktx/Browsers.kt)

## app/src/main/java/io/nekohasekai/sagernet/ktx/Dialogs.kt

- [`app/src/main/java/io/nekohasekai/sagernet/ktx/Dialogs.kt`](../../app/src/main/java/io/nekohasekai/sagernet/ktx/Dialogs.kt)

## app/src/main/java/io/nekohasekai/sagernet/ktx/Dimens.kt

- [`app/src/main/java/io/nekohasekai/sagernet/ktx/Dimens.kt`](../../app/src/main/java/io/nekohasekai/sagernet/ktx/Dimens.kt)

## app/src/main/java/io/nekohasekai/sagernet/ktx/Formats.kt

- [`app/src/main/java/io/nekohasekai/sagernet/ktx/Formats.kt`](../../app/src/main/java/io/nekohasekai/sagernet/ktx/Formats.kt)

## app/src/main/java/io/nekohasekai/sagernet/ktx/Kryos.kt

- [`app/src/main/java/io/nekohasekai/sagernet/ktx/Kryos.kt`](../../app/src/main/java/io/nekohasekai/sagernet/ktx/Kryos.kt)

## app/src/main/java/io/nekohasekai/sagernet/ktx/Layouts.kt

- [`app/src/main/java/io/nekohasekai/sagernet/ktx/Layouts.kt`](../../app/src/main/java/io/nekohasekai/sagernet/ktx/Layouts.kt)

## app/src/main/java/io/nekohasekai/sagernet/ktx/Logs.kt

- [`app/src/main/java/io/nekohasekai/sagernet/ktx/Logs.kt`](../../app/src/main/java/io/nekohasekai/sagernet/ktx/Logs.kt)

## app/src/main/java/io/nekohasekai/sagernet/ktx/Nets.kt

- [`app/src/main/java/io/nekohasekai/sagernet/ktx/Nets.kt`](../../app/src/main/java/io/nekohasekai/sagernet/ktx/Nets.kt)

## app/src/main/java/io/nekohasekai/sagernet/ktx/Preferences.kt

- [`app/src/main/java/io/nekohasekai/sagernet/ktx/Preferences.kt`](../../app/src/main/java/io/nekohasekai/sagernet/ktx/Preferences.kt)

## app/src/main/java/io/nekohasekai/sagernet/ktx/TvDeviceUtil.kt

- [`app/src/main/java/io/nekohasekai/sagernet/ktx/TvDeviceUtil.kt`](../../app/src/main/java/io/nekohasekai/sagernet/ktx/TvDeviceUtil.kt)

## app/src/main/java/io/nekohasekai/sagernet/ktx/Utils.kt

- [`app/src/main/java/io/nekohasekai/sagernet/ktx/Utils.kt`](../../app/src/main/java/io/nekohasekai/sagernet/ktx/Utils.kt)

## app/src/main/java/io/nekohasekai/sagernet/plugin/PluginManager.kt

- [`app/src/main/java/io/nekohasekai/sagernet/plugin/PluginManager.kt`](../../app/src/main/java/io/nekohasekai/sagernet/plugin/PluginManager.kt)

## app/src/main/java/io/nekohasekai/sagernet/ui/AboutFragment.kt

- [`app/src/main/java/io/nekohasekai/sagernet/ui/AboutFragment.kt`](../../app/src/main/java/io/nekohasekai/sagernet/ui/AboutFragment.kt)

## app/src/main/java/io/nekohasekai/sagernet/ui/AppLifecycleActions.kt

- [`app/src/main/java/io/nekohasekai/sagernet/ui/AppLifecycleActions.kt`](../../app/src/main/java/io/nekohasekai/sagernet/ui/AppLifecycleActions.kt)

## app/src/main/java/io/nekohasekai/sagernet/ui/AppListActivity.kt

- [`app/src/main/java/io/nekohasekai/sagernet/ui/AppListActivity.kt`](../../app/src/main/java/io/nekohasekai/sagernet/ui/AppListActivity.kt)

## app/src/main/java/io/nekohasekai/sagernet/ui/AppManagerActivity.kt

- [`app/src/main/java/io/nekohasekai/sagernet/ui/AppManagerActivity.kt`](../../app/src/main/java/io/nekohasekai/sagernet/ui/AppManagerActivity.kt)

## app/src/main/java/io/nekohasekai/sagernet/ui/AssetsActivity.kt

- [`app/src/main/java/io/nekohasekai/sagernet/ui/AssetsActivity.kt`](../../app/src/main/java/io/nekohasekai/sagernet/ui/AssetsActivity.kt)

## app/src/main/java/io/nekohasekai/sagernet/ui/BackupFragment.kt

- [`app/src/main/java/io/nekohasekai/sagernet/ui/BackupFragment.kt`](../../app/src/main/java/io/nekohasekai/sagernet/ui/BackupFragment.kt)

## app/src/main/java/io/nekohasekai/sagernet/ui/BlankActivity.kt

- [`app/src/main/java/io/nekohasekai/sagernet/ui/BlankActivity.kt`](../../app/src/main/java/io/nekohasekai/sagernet/ui/BlankActivity.kt)

## app/src/main/java/io/nekohasekai/sagernet/ui/ConfigurationFragment.kt

- [`app/src/main/java/io/nekohasekai/sagernet/ui/ConfigurationFragment.kt`](../../app/src/main/java/io/nekohasekai/sagernet/ui/ConfigurationFragment.kt)

## app/src/main/java/io/nekohasekai/sagernet/ui/GroupFragment.kt

- [`app/src/main/java/io/nekohasekai/sagernet/ui/GroupFragment.kt`](../../app/src/main/java/io/nekohasekai/sagernet/ui/GroupFragment.kt)

## app/src/main/java/io/nekohasekai/sagernet/ui/GroupSettingsActivity.kt

- [`app/src/main/java/io/nekohasekai/sagernet/ui/GroupSettingsActivity.kt`](../../app/src/main/java/io/nekohasekai/sagernet/ui/GroupSettingsActivity.kt)

## app/src/main/java/io/nekohasekai/sagernet/ui/LogcatFragment.kt

- [`app/src/main/java/io/nekohasekai/sagernet/ui/LogcatFragment.kt`](../../app/src/main/java/io/nekohasekai/sagernet/ui/LogcatFragment.kt)

## app/src/main/java/io/nekohasekai/sagernet/ui/MainActivity.kt

- [`app/src/main/java/io/nekohasekai/sagernet/ui/MainActivity.kt`](../../app/src/main/java/io/nekohasekai/sagernet/ui/MainActivity.kt)

## app/src/main/java/io/nekohasekai/sagernet/ui/MainActivityTv.kt

- [`app/src/main/java/io/nekohasekai/sagernet/ui/MainActivityTv.kt`](../../app/src/main/java/io/nekohasekai/sagernet/ui/MainActivityTv.kt)

## app/src/main/java/io/nekohasekai/sagernet/ui/ModeSelectionActivity.kt

- [`app/src/main/java/io/nekohasekai/sagernet/ui/ModeSelectionActivity.kt`](../../app/src/main/java/io/nekohasekai/sagernet/ui/ModeSelectionActivity.kt)

## app/src/main/java/io/nekohasekai/sagernet/ui/NamedFragment.kt

- [`app/src/main/java/io/nekohasekai/sagernet/ui/NamedFragment.kt`](../../app/src/main/java/io/nekohasekai/sagernet/ui/NamedFragment.kt)

## app/src/main/java/io/nekohasekai/sagernet/ui/NetworkFragment.kt

- [`app/src/main/java/io/nekohasekai/sagernet/ui/NetworkFragment.kt`](../../app/src/main/java/io/nekohasekai/sagernet/ui/NetworkFragment.kt)

## app/src/main/java/io/nekohasekai/sagernet/ui/ProfileCreationActions.kt

- [`app/src/main/java/io/nekohasekai/sagernet/ui/ProfileCreationActions.kt`](../../app/src/main/java/io/nekohasekai/sagernet/ui/ProfileCreationActions.kt)

## app/src/main/java/io/nekohasekai/sagernet/ui/ProfileSelectActivity.kt

- [`app/src/main/java/io/nekohasekai/sagernet/ui/ProfileSelectActivity.kt`](../../app/src/main/java/io/nekohasekai/sagernet/ui/ProfileSelectActivity.kt)

## app/src/main/java/io/nekohasekai/sagernet/ui/ProjectLinks.kt

- [`app/src/main/java/io/nekohasekai/sagernet/ui/ProjectLinks.kt`](../../app/src/main/java/io/nekohasekai/sagernet/ui/ProjectLinks.kt)

## app/src/main/java/io/nekohasekai/sagernet/ui/PromotionsFragment.kt

- [`app/src/main/java/io/nekohasekai/sagernet/ui/PromotionsFragment.kt`](../../app/src/main/java/io/nekohasekai/sagernet/ui/PromotionsFragment.kt)

## app/src/main/java/io/nekohasekai/sagernet/ui/QuickDisableShortcut.kt

- [`app/src/main/java/io/nekohasekai/sagernet/ui/QuickDisableShortcut.kt`](../../app/src/main/java/io/nekohasekai/sagernet/ui/QuickDisableShortcut.kt)

## app/src/main/java/io/nekohasekai/sagernet/ui/QuickEnableShortcut.kt

- [`app/src/main/java/io/nekohasekai/sagernet/ui/QuickEnableShortcut.kt`](../../app/src/main/java/io/nekohasekai/sagernet/ui/QuickEnableShortcut.kt)

## app/src/main/java/io/nekohasekai/sagernet/ui/ReleaseUpdatePolicy.kt

- [`app/src/main/java/io/nekohasekai/sagernet/ui/ReleaseUpdatePolicy.kt`](../../app/src/main/java/io/nekohasekai/sagernet/ui/ReleaseUpdatePolicy.kt)

## app/src/main/java/io/nekohasekai/sagernet/ui/RemoteFocusHighlighter.kt

- [`app/src/main/java/io/nekohasekai/sagernet/ui/RemoteFocusHighlighter.kt`](../../app/src/main/java/io/nekohasekai/sagernet/ui/RemoteFocusHighlighter.kt)

## app/src/main/java/io/nekohasekai/sagernet/ui/RemoteProfileFocus.kt

- [`app/src/main/java/io/nekohasekai/sagernet/ui/RemoteProfileFocus.kt`](../../app/src/main/java/io/nekohasekai/sagernet/ui/RemoteProfileFocus.kt)

## app/src/main/java/io/nekohasekai/sagernet/ui/RemoteReadability.kt

- [`app/src/main/java/io/nekohasekai/sagernet/ui/RemoteReadability.kt`](../../app/src/main/java/io/nekohasekai/sagernet/ui/RemoteReadability.kt)

## app/src/main/java/io/nekohasekai/sagernet/ui/RemoteRowActions.kt

- [`app/src/main/java/io/nekohasekai/sagernet/ui/RemoteRowActions.kt`](../../app/src/main/java/io/nekohasekai/sagernet/ui/RemoteRowActions.kt)

## app/src/main/java/io/nekohasekai/sagernet/ui/RouteFragment.kt

- [`app/src/main/java/io/nekohasekai/sagernet/ui/RouteFragment.kt`](../../app/src/main/java/io/nekohasekai/sagernet/ui/RouteFragment.kt)

## app/src/main/java/io/nekohasekai/sagernet/ui/RouteSettingsActivity.kt

- [`app/src/main/java/io/nekohasekai/sagernet/ui/RouteSettingsActivity.kt`](../../app/src/main/java/io/nekohasekai/sagernet/ui/RouteSettingsActivity.kt)

## app/src/main/java/io/nekohasekai/sagernet/ui/ScannerActivity.kt

- [`app/src/main/java/io/nekohasekai/sagernet/ui/ScannerActivity.kt`](../../app/src/main/java/io/nekohasekai/sagernet/ui/ScannerActivity.kt)

## app/src/main/java/io/nekohasekai/sagernet/ui/SettingsFragment.kt

- [`app/src/main/java/io/nekohasekai/sagernet/ui/SettingsFragment.kt`](../../app/src/main/java/io/nekohasekai/sagernet/ui/SettingsFragment.kt)

## app/src/main/java/io/nekohasekai/sagernet/ui/SettingsPreferenceFragment.kt

- [`app/src/main/java/io/nekohasekai/sagernet/ui/SettingsPreferenceFragment.kt`](../../app/src/main/java/io/nekohasekai/sagernet/ui/SettingsPreferenceFragment.kt)

## app/src/main/java/io/nekohasekai/sagernet/ui/StunActivity.kt

- [`app/src/main/java/io/nekohasekai/sagernet/ui/StunActivity.kt`](../../app/src/main/java/io/nekohasekai/sagernet/ui/StunActivity.kt)

## app/src/main/java/io/nekohasekai/sagernet/ui/SwitchActivity.kt

- [`app/src/main/java/io/nekohasekai/sagernet/ui/SwitchActivity.kt`](../../app/src/main/java/io/nekohasekai/sagernet/ui/SwitchActivity.kt)

## app/src/main/java/io/nekohasekai/sagernet/ui/ThemedActivity.kt

- [`app/src/main/java/io/nekohasekai/sagernet/ui/ThemedActivity.kt`](../../app/src/main/java/io/nekohasekai/sagernet/ui/ThemedActivity.kt)

## app/src/main/java/io/nekohasekai/sagernet/ui/ToolbarFragment.kt

- [`app/src/main/java/io/nekohasekai/sagernet/ui/ToolbarFragment.kt`](../../app/src/main/java/io/nekohasekai/sagernet/ui/ToolbarFragment.kt)

## app/src/main/java/io/nekohasekai/sagernet/ui/ToolsFragment.kt

- [`app/src/main/java/io/nekohasekai/sagernet/ui/ToolsFragment.kt`](../../app/src/main/java/io/nekohasekai/sagernet/ui/ToolsFragment.kt)

## app/src/main/java/io/nekohasekai/sagernet/ui/VpnRequestActivity.kt

- [`app/src/main/java/io/nekohasekai/sagernet/ui/VpnRequestActivity.kt`](../../app/src/main/java/io/nekohasekai/sagernet/ui/VpnRequestActivity.kt)

## app/src/main/java/io/nekohasekai/sagernet/ui/WebviewFragment.kt

- [`app/src/main/java/io/nekohasekai/sagernet/ui/WebviewFragment.kt`](../../app/src/main/java/io/nekohasekai/sagernet/ui/WebviewFragment.kt)

## app/src/main/java/io/nekohasekai/sagernet/ui/profile

- [`app/src/main/java/io/nekohasekai/sagernet/ui/profile/ChainSettingsActivity.kt`](../../app/src/main/java/io/nekohasekai/sagernet/ui/profile/ChainSettingsActivity.kt)
- [`app/src/main/java/io/nekohasekai/sagernet/ui/profile/ConfigEditActivity.kt`](../../app/src/main/java/io/nekohasekai/sagernet/ui/profile/ConfigEditActivity.kt)
- [`app/src/main/java/io/nekohasekai/sagernet/ui/profile/HttpSettingsActivity.kt`](../../app/src/main/java/io/nekohasekai/sagernet/ui/profile/HttpSettingsActivity.kt)
- [`app/src/main/java/io/nekohasekai/sagernet/ui/profile/HysteriaSettingsActivity.kt`](../../app/src/main/java/io/nekohasekai/sagernet/ui/profile/HysteriaSettingsActivity.kt)
- [`app/src/main/java/io/nekohasekai/sagernet/ui/profile/MieruSettingsActivity.kt`](../../app/src/main/java/io/nekohasekai/sagernet/ui/profile/MieruSettingsActivity.kt)
- [`app/src/main/java/io/nekohasekai/sagernet/ui/profile/NaiveSettingsActivity.kt`](../../app/src/main/java/io/nekohasekai/sagernet/ui/profile/NaiveSettingsActivity.kt)
- [`app/src/main/java/io/nekohasekai/sagernet/ui/profile/ProfileSettingsActivity.kt`](../../app/src/main/java/io/nekohasekai/sagernet/ui/profile/ProfileSettingsActivity.kt)
- [`app/src/main/java/io/nekohasekai/sagernet/ui/profile/SSHSettingsActivity.kt`](../../app/src/main/java/io/nekohasekai/sagernet/ui/profile/SSHSettingsActivity.kt)
- [`app/src/main/java/io/nekohasekai/sagernet/ui/profile/ShadowsocksSettingsActivity.kt`](../../app/src/main/java/io/nekohasekai/sagernet/ui/profile/ShadowsocksSettingsActivity.kt)
- [`app/src/main/java/io/nekohasekai/sagernet/ui/profile/SocksSettingsActivity.kt`](../../app/src/main/java/io/nekohasekai/sagernet/ui/profile/SocksSettingsActivity.kt)
- [`app/src/main/java/io/nekohasekai/sagernet/ui/profile/StandardV2RaySettingsActivity.kt`](../../app/src/main/java/io/nekohasekai/sagernet/ui/profile/StandardV2RaySettingsActivity.kt)
- [`app/src/main/java/io/nekohasekai/sagernet/ui/profile/TrojanGoSettingsActivity.kt`](../../app/src/main/java/io/nekohasekai/sagernet/ui/profile/TrojanGoSettingsActivity.kt)
- [`app/src/main/java/io/nekohasekai/sagernet/ui/profile/TrojanSettingsActivity.kt`](../../app/src/main/java/io/nekohasekai/sagernet/ui/profile/TrojanSettingsActivity.kt)
- [`app/src/main/java/io/nekohasekai/sagernet/ui/profile/TuicSettingsActivity.kt`](../../app/src/main/java/io/nekohasekai/sagernet/ui/profile/TuicSettingsActivity.kt)
- [`app/src/main/java/io/nekohasekai/sagernet/ui/profile/VMessSettingsActivity.kt`](../../app/src/main/java/io/nekohasekai/sagernet/ui/profile/VMessSettingsActivity.kt)
- [`app/src/main/java/io/nekohasekai/sagernet/ui/profile/WireGuardSettingsActivity.kt`](../../app/src/main/java/io/nekohasekai/sagernet/ui/profile/WireGuardSettingsActivity.kt)

## app/src/main/java/io/nekohasekai/sagernet/ui/tv

- [`app/src/main/java/io/nekohasekai/sagernet/ui/tv/ActionPresenter.kt`](../../app/src/main/java/io/nekohasekai/sagernet/ui/tv/ActionPresenter.kt)
- [`app/src/main/java/io/nekohasekai/sagernet/ui/tv/MainBrowseFragment.kt`](../../app/src/main/java/io/nekohasekai/sagernet/ui/tv/MainBrowseFragment.kt)
- [`app/src/main/java/io/nekohasekai/sagernet/ui/tv/ProfileCardPresenter.kt`](../../app/src/main/java/io/nekohasekai/sagernet/ui/tv/ProfileCardPresenter.kt)
- [`app/src/main/java/io/nekohasekai/sagernet/ui/tv/QrCodeTransferFragment.kt`](../../app/src/main/java/io/nekohasekai/sagernet/ui/tv/QrCodeTransferFragment.kt)
- [`app/src/main/java/io/nekohasekai/sagernet/ui/tv/StableTvRowPresenter.kt`](../../app/src/main/java/io/nekohasekai/sagernet/ui/tv/StableTvRowPresenter.kt)
- [`app/src/main/java/io/nekohasekai/sagernet/ui/tv/TransferImportInput.kt`](../../app/src/main/java/io/nekohasekai/sagernet/ui/tv/TransferImportInput.kt)
- [`app/src/main/java/io/nekohasekai/sagernet/ui/tv/TransferProtocol.kt`](../../app/src/main/java/io/nekohasekai/sagernet/ui/tv/TransferProtocol.kt)
- [`app/src/main/java/io/nekohasekai/sagernet/ui/tv/TvDiagnostics.kt`](../../app/src/main/java/io/nekohasekai/sagernet/ui/tv/TvDiagnostics.kt)
- [`app/src/main/java/io/nekohasekai/sagernet/ui/tv/TvFunctionCatalog.kt`](../../app/src/main/java/io/nekohasekai/sagernet/ui/tv/TvFunctionCatalog.kt)
- [`app/src/main/java/io/nekohasekai/sagernet/ui/tv/TvGroupTransfer.kt`](../../app/src/main/java/io/nekohasekai/sagernet/ui/tv/TvGroupTransfer.kt)
- [`app/src/main/java/io/nekohasekai/sagernet/ui/tv/TvInteractionPolicy.kt`](../../app/src/main/java/io/nekohasekai/sagernet/ui/tv/TvInteractionPolicy.kt)
- [`app/src/main/java/io/nekohasekai/sagernet/ui/tv/TvLayoutPolicy.kt`](../../app/src/main/java/io/nekohasekai/sagernet/ui/tv/TvLayoutPolicy.kt)
- [`app/src/main/java/io/nekohasekai/sagernet/ui/tv/TvProfileAddCatalog.kt`](../../app/src/main/java/io/nekohasekai/sagernet/ui/tv/TvProfileAddCatalog.kt)
- [`app/src/main/java/io/nekohasekai/sagernet/ui/tv/TvProfileImporter.kt`](../../app/src/main/java/io/nekohasekai/sagernet/ui/tv/TvProfileImporter.kt)
- [`app/src/main/java/io/nekohasekai/sagernet/ui/tv/TvRowDiff.kt`](../../app/src/main/java/io/nekohasekai/sagernet/ui/tv/TvRowDiff.kt)
- [`app/src/main/java/io/nekohasekai/sagernet/ui/tv/TvScannerFragment.kt`](../../app/src/main/java/io/nekohasekai/sagernet/ui/tv/TvScannerFragment.kt)
- [`app/src/main/java/io/nekohasekai/sagernet/ui/tv/TvTransferClient.kt`](../../app/src/main/java/io/nekohasekai/sagernet/ui/tv/TvTransferClient.kt)
- [`app/src/main/java/io/nekohasekai/sagernet/ui/tv/TvTransferServer.kt`](../../app/src/main/java/io/nekohasekai/sagernet/ui/tv/TvTransferServer.kt)
- [`app/src/main/java/io/nekohasekai/sagernet/ui/tv/TvUiPreferences.kt`](../../app/src/main/java/io/nekohasekai/sagernet/ui/tv/TvUiPreferences.kt)

## app/src/main/java/io/nekohasekai/sagernet/utils/Commandline.kt

- [`app/src/main/java/io/nekohasekai/sagernet/utils/Commandline.kt`](../../app/src/main/java/io/nekohasekai/sagernet/utils/Commandline.kt)

## app/src/main/java/io/nekohasekai/sagernet/utils/CrashHandler.kt

- [`app/src/main/java/io/nekohasekai/sagernet/utils/CrashHandler.kt`](../../app/src/main/java/io/nekohasekai/sagernet/utils/CrashHandler.kt)

## app/src/main/java/io/nekohasekai/sagernet/utils/DefaultNetworkListener.kt

- [`app/src/main/java/io/nekohasekai/sagernet/utils/DefaultNetworkListener.kt`](../../app/src/main/java/io/nekohasekai/sagernet/utils/DefaultNetworkListener.kt)

## app/src/main/java/io/nekohasekai/sagernet/utils/PackageCache.kt

- [`app/src/main/java/io/nekohasekai/sagernet/utils/PackageCache.kt`](../../app/src/main/java/io/nekohasekai/sagernet/utils/PackageCache.kt)

## app/src/main/java/io/nekohasekai/sagernet/utils/Subnet.kt

- [`app/src/main/java/io/nekohasekai/sagernet/utils/Subnet.kt`](../../app/src/main/java/io/nekohasekai/sagernet/utils/Subnet.kt)

## app/src/main/java/io/nekohasekai/sagernet/utils/Theme.kt

- [`app/src/main/java/io/nekohasekai/sagernet/utils/Theme.kt`](../../app/src/main/java/io/nekohasekai/sagernet/utils/Theme.kt)

## app/src/main/java/io/nekohasekai/sagernet/widget/AppListPreference.kt

- [`app/src/main/java/io/nekohasekai/sagernet/widget/AppListPreference.kt`](../../app/src/main/java/io/nekohasekai/sagernet/widget/AppListPreference.kt)

## app/src/main/java/io/nekohasekai/sagernet/widget/AutoCollapseTextView.kt

- [`app/src/main/java/io/nekohasekai/sagernet/widget/AutoCollapseTextView.kt`](../../app/src/main/java/io/nekohasekai/sagernet/widget/AutoCollapseTextView.kt)

## app/src/main/java/io/nekohasekai/sagernet/widget/FabProgressBehavior.kt

- [`app/src/main/java/io/nekohasekai/sagernet/widget/FabProgressBehavior.kt`](../../app/src/main/java/io/nekohasekai/sagernet/widget/FabProgressBehavior.kt)

## app/src/main/java/io/nekohasekai/sagernet/widget/GroupPreference.kt

- [`app/src/main/java/io/nekohasekai/sagernet/widget/GroupPreference.kt`](../../app/src/main/java/io/nekohasekai/sagernet/widget/GroupPreference.kt)

## app/src/main/java/io/nekohasekai/sagernet/widget/LinkOrContentPreference.kt

- [`app/src/main/java/io/nekohasekai/sagernet/widget/LinkOrContentPreference.kt`](../../app/src/main/java/io/nekohasekai/sagernet/widget/LinkOrContentPreference.kt)

## app/src/main/java/io/nekohasekai/sagernet/widget/OutboundPreference.kt

- [`app/src/main/java/io/nekohasekai/sagernet/widget/OutboundPreference.kt`](../../app/src/main/java/io/nekohasekai/sagernet/widget/OutboundPreference.kt)

## app/src/main/java/io/nekohasekai/sagernet/widget/QRCodeDialog.kt

- [`app/src/main/java/io/nekohasekai/sagernet/widget/QRCodeDialog.kt`](../../app/src/main/java/io/nekohasekai/sagernet/widget/QRCodeDialog.kt)

## app/src/main/java/io/nekohasekai/sagernet/widget/ServiceButton.kt

- [`app/src/main/java/io/nekohasekai/sagernet/widget/ServiceButton.kt`](../../app/src/main/java/io/nekohasekai/sagernet/widget/ServiceButton.kt)

## app/src/main/java/io/nekohasekai/sagernet/widget/StatsBar.kt

- [`app/src/main/java/io/nekohasekai/sagernet/widget/StatsBar.kt`](../../app/src/main/java/io/nekohasekai/sagernet/widget/StatsBar.kt)

## app/src/main/java/io/nekohasekai/sagernet/widget/UndoSnackbarManager.kt

- [`app/src/main/java/io/nekohasekai/sagernet/widget/UndoSnackbarManager.kt`](../../app/src/main/java/io/nekohasekai/sagernet/widget/UndoSnackbarManager.kt)

## app/src/main/java/io/nekohasekai/sagernet/widget/UserAgentPreference.kt

- [`app/src/main/java/io/nekohasekai/sagernet/widget/UserAgentPreference.kt`](../../app/src/main/java/io/nekohasekai/sagernet/widget/UserAgentPreference.kt)

## app/src/main/java/io/nekohasekai/sagernet/widget/WindowInsetsListeners.kt

- [`app/src/main/java/io/nekohasekai/sagernet/widget/WindowInsetsListeners.kt`](../../app/src/main/java/io/nekohasekai/sagernet/widget/WindowInsetsListeners.kt)

## app/src/main/java/moe/matsuri/nb4a/NativeInterface.kt

- [`app/src/main/java/moe/matsuri/nb4a/NativeInterface.kt`](../../app/src/main/java/moe/matsuri/nb4a/NativeInterface.kt)

## app/src/main/java/moe/matsuri/nb4a/Protocols.kt

- [`app/src/main/java/moe/matsuri/nb4a/Protocols.kt`](../../app/src/main/java/moe/matsuri/nb4a/Protocols.kt)

## app/src/main/java/moe/matsuri/nb4a/SingBoxOptions.java

- [`app/src/main/java/moe/matsuri/nb4a/SingBoxOptions.java`](../../app/src/main/java/moe/matsuri/nb4a/SingBoxOptions.java)

## app/src/main/java/moe/matsuri/nb4a/SingBoxOptionsUtil.kt

- [`app/src/main/java/moe/matsuri/nb4a/SingBoxOptionsUtil.kt`](../../app/src/main/java/moe/matsuri/nb4a/SingBoxOptionsUtil.kt)

## app/src/main/java/moe/matsuri/nb4a/TempDatabase.kt

- [`app/src/main/java/moe/matsuri/nb4a/TempDatabase.kt`](../../app/src/main/java/moe/matsuri/nb4a/TempDatabase.kt)

## app/src/main/java/moe/matsuri/nb4a/net

- [`app/src/main/java/moe/matsuri/nb4a/net/LocalResolverImpl.kt`](../../app/src/main/java/moe/matsuri/nb4a/net/LocalResolverImpl.kt)

## app/src/main/java/moe/matsuri/nb4a/plugin

- [`app/src/main/java/moe/matsuri/nb4a/plugin/Plugins.kt`](../../app/src/main/java/moe/matsuri/nb4a/plugin/Plugins.kt)

## app/src/main/java/moe/matsuri/nb4a/proxy

- [`app/src/main/java/moe/matsuri/nb4a/proxy/PreferenceBinding.kt`](../../app/src/main/java/moe/matsuri/nb4a/proxy/PreferenceBinding.kt)
- [`app/src/main/java/moe/matsuri/nb4a/proxy/PreferenceBindingManager.kt`](../../app/src/main/java/moe/matsuri/nb4a/proxy/PreferenceBindingManager.kt)
- [`app/src/main/java/moe/matsuri/nb4a/proxy/anytls/AnyTLSBean.java`](../../app/src/main/java/moe/matsuri/nb4a/proxy/anytls/AnyTLSBean.java)
- [`app/src/main/java/moe/matsuri/nb4a/proxy/anytls/AnyTLSFmt.kt`](../../app/src/main/java/moe/matsuri/nb4a/proxy/anytls/AnyTLSFmt.kt)
- [`app/src/main/java/moe/matsuri/nb4a/proxy/anytls/AnyTLSSettingsActivity.kt`](../../app/src/main/java/moe/matsuri/nb4a/proxy/anytls/AnyTLSSettingsActivity.kt)
- [`app/src/main/java/moe/matsuri/nb4a/proxy/config/ConfigBean.java`](../../app/src/main/java/moe/matsuri/nb4a/proxy/config/ConfigBean.java)
- [`app/src/main/java/moe/matsuri/nb4a/proxy/config/ConfigSettingActivity.kt`](../../app/src/main/java/moe/matsuri/nb4a/proxy/config/ConfigSettingActivity.kt)
- [`app/src/main/java/moe/matsuri/nb4a/proxy/neko/NekoBean.java`](../../app/src/main/java/moe/matsuri/nb4a/proxy/neko/NekoBean.java)
- [`app/src/main/java/moe/matsuri/nb4a/proxy/shadowtls/ShadowTLSBean.java`](../../app/src/main/java/moe/matsuri/nb4a/proxy/shadowtls/ShadowTLSBean.java)
- [`app/src/main/java/moe/matsuri/nb4a/proxy/shadowtls/ShadowTLSFmt.kt`](../../app/src/main/java/moe/matsuri/nb4a/proxy/shadowtls/ShadowTLSFmt.kt)
- [`app/src/main/java/moe/matsuri/nb4a/proxy/shadowtls/ShadowTLSSettingsActivity.kt`](../../app/src/main/java/moe/matsuri/nb4a/proxy/shadowtls/ShadowTLSSettingsActivity.kt)

## app/src/main/java/moe/matsuri/nb4a/ui

- [`app/src/main/java/moe/matsuri/nb4a/ui/ColorPickerPreference.kt`](../../app/src/main/java/moe/matsuri/nb4a/ui/ColorPickerPreference.kt)
- [`app/src/main/java/moe/matsuri/nb4a/ui/ConnectionTestNotification.kt`](../../app/src/main/java/moe/matsuri/nb4a/ui/ConnectionTestNotification.kt)
- [`app/src/main/java/moe/matsuri/nb4a/ui/Dialogs.kt`](../../app/src/main/java/moe/matsuri/nb4a/ui/Dialogs.kt)
- [`app/src/main/java/moe/matsuri/nb4a/ui/EditConfigPreference.kt`](../../app/src/main/java/moe/matsuri/nb4a/ui/EditConfigPreference.kt)
- [`app/src/main/java/moe/matsuri/nb4a/ui/ExtendedKeyboard.kt`](../../app/src/main/java/moe/matsuri/nb4a/ui/ExtendedKeyboard.kt)
- [`app/src/main/java/moe/matsuri/nb4a/ui/LongClickListPreference.kt`](../../app/src/main/java/moe/matsuri/nb4a/ui/LongClickListPreference.kt)
- [`app/src/main/java/moe/matsuri/nb4a/ui/LongClickMenuPreference.kt`](../../app/src/main/java/moe/matsuri/nb4a/ui/LongClickMenuPreference.kt)
- [`app/src/main/java/moe/matsuri/nb4a/ui/LongClickSwitchPreference.kt`](../../app/src/main/java/moe/matsuri/nb4a/ui/LongClickSwitchPreference.kt)
- [`app/src/main/java/moe/matsuri/nb4a/ui/MTUPreference.kt`](../../app/src/main/java/moe/matsuri/nb4a/ui/MTUPreference.kt)
- [`app/src/main/java/moe/matsuri/nb4a/ui/SimpleMenuPreference.kt`](../../app/src/main/java/moe/matsuri/nb4a/ui/SimpleMenuPreference.kt)
- [`app/src/main/java/moe/matsuri/nb4a/ui/UrlTestPreference.kt`](../../app/src/main/java/moe/matsuri/nb4a/ui/UrlTestPreference.kt)

## app/src/main/java/moe/matsuri/nb4a/utils

- [`app/src/main/java/moe/matsuri/nb4a/utils/JavaUtil.java`](../../app/src/main/java/moe/matsuri/nb4a/utils/JavaUtil.java)
- [`app/src/main/java/moe/matsuri/nb4a/utils/KotlinUtil.kt`](../../app/src/main/java/moe/matsuri/nb4a/utils/KotlinUtil.kt)
- [`app/src/main/java/moe/matsuri/nb4a/utils/NGUtil.kt`](../../app/src/main/java/moe/matsuri/nb4a/utils/NGUtil.kt)
- [`app/src/main/java/moe/matsuri/nb4a/utils/SendLog.kt`](../../app/src/main/java/moe/matsuri/nb4a/utils/SendLog.kt)
- [`app/src/main/java/moe/matsuri/nb4a/utils/Util.kt`](../../app/src/main/java/moe/matsuri/nb4a/utils/Util.kt)
- [`app/src/main/java/moe/matsuri/nb4a/utils/WebViewUtil.kt`](../../app/src/main/java/moe/matsuri/nb4a/utils/WebViewUtil.kt)

## app/src/main/res/color

- [`app/src/main/res/color/chip_background.xml`](../../app/src/main/res/color/chip_background.xml)
- [`app/src/main/res/color/chip_ripple_color.xml`](../../app/src/main/res/color/chip_ripple_color.xml)
- [`app/src/main/res/color/chip_text_color.xml`](../../app/src/main/res/color/chip_text_color.xml)
- [`app/src/main/res/color/navigation_icon.xml`](../../app/src/main/res/color/navigation_icon.xml)
- [`app/src/main/res/color/navigation_item.xml`](../../app/src/main/res/color/navigation_item.xml)
- [`app/src/main/res/color/tv_focus_stroke.xml`](../../app/src/main/res/color/tv_focus_stroke.xml)

## app/src/main/res/drawable

- [`app/src/main/res/drawable/baseline_arrow_back_24.xml`](../../app/src/main/res/drawable/baseline_arrow_back_24.xml)
- [`app/src/main/res/drawable/baseline_construction_24.xml`](../../app/src/main/res/drawable/baseline_construction_24.xml)
- [`app/src/main/res/drawable/baseline_delete_sweep_24.xml`](../../app/src/main/res/drawable/baseline_delete_sweep_24.xml)
- [`app/src/main/res/drawable/baseline_developer_board_24.xml`](../../app/src/main/res/drawable/baseline_developer_board_24.xml)
- [`app/src/main/res/drawable/baseline_flight_takeoff_24.xml`](../../app/src/main/res/drawable/baseline_flight_takeoff_24.xml)
- [`app/src/main/res/drawable/baseline_keyboard_tab_24.xml`](../../app/src/main/res/drawable/baseline_keyboard_tab_24.xml)
- [`app/src/main/res/drawable/baseline_public_24.xml`](../../app/src/main/res/drawable/baseline_public_24.xml)
- [`app/src/main/res/drawable/baseline_redo_24.xml`](../../app/src/main/res/drawable/baseline_redo_24.xml)
- [`app/src/main/res/drawable/baseline_save_24.xml`](../../app/src/main/res/drawable/baseline_save_24.xml)
- [`app/src/main/res/drawable/baseline_send_24.xml`](../../app/src/main/res/drawable/baseline_send_24.xml)
- [`app/src/main/res/drawable/baseline_translate_24.xml`](../../app/src/main/res/drawable/baseline_translate_24.xml)
- [`app/src/main/res/drawable/baseline_undo_24.xml`](../../app/src/main/res/drawable/baseline_undo_24.xml)
- [`app/src/main/res/drawable/baseline_widgets_24.xml`](../../app/src/main/res/drawable/baseline_widgets_24.xml)
- [`app/src/main/res/drawable/baseline_wrap_text_24.xml`](../../app/src/main/res/drawable/baseline_wrap_text_24.xml)
- [`app/src/main/res/drawable/bg_focusable_item.xml`](../../app/src/main/res/drawable/bg_focusable_item.xml)
- [`app/src/main/res/drawable/card_background_tv.xml`](../../app/src/main/res/drawable/card_background_tv.xml)
- [`app/src/main/res/drawable/ic_action_copyright.xml`](../../app/src/main/res/drawable/ic_action_copyright.xml)
- [`app/src/main/res/drawable/ic_action_delete.xml`](../../app/src/main/res/drawable/ic_action_delete.xml)
- [`app/src/main/res/drawable/ic_action_description.xml`](../../app/src/main/res/drawable/ic_action_description.xml)
- [`app/src/main/res/drawable/ic_action_dns.xml`](../../app/src/main/res/drawable/ic_action_dns.xml)
- [`app/src/main/res/drawable/ic_action_done.xml`](../../app/src/main/res/drawable/ic_action_done.xml)
- [`app/src/main/res/drawable/ic_action_lock.xml`](../../app/src/main/res/drawable/ic_action_lock.xml)
- [`app/src/main/res/drawable/ic_action_lock_open.xml`](../../app/src/main/res/drawable/ic_action_lock_open.xml)
- [`app/src/main/res/drawable/ic_action_note_add.xml`](../../app/src/main/res/drawable/ic_action_note_add.xml)
- [`app/src/main/res/drawable/ic_action_settings.xml`](../../app/src/main/res/drawable/ic_action_settings.xml)
- [`app/src/main/res/drawable/ic_app_shortcut_background.xml`](../../app/src/main/res/drawable/ic_app_shortcut_background.xml)
- [`app/src/main/res/drawable/ic_av_playlist_add.xml`](../../app/src/main/res/drawable/ic_av_playlist_add.xml)
- [`app/src/main/res/drawable/ic_baseline_add_road_24.xml`](../../app/src/main/res/drawable/ic_baseline_add_road_24.xml)
- [`app/src/main/res/drawable/ic_baseline_airplanemode_active_24.xml`](../../app/src/main/res/drawable/ic_baseline_airplanemode_active_24.xml)
- [`app/src/main/res/drawable/ic_baseline_android_24.xml`](../../app/src/main/res/drawable/ic_baseline_android_24.xml)
- [`app/src/main/res/drawable/ic_baseline_bug_report_24.xml`](../../app/src/main/res/drawable/ic_baseline_bug_report_24.xml)
- [`app/src/main/res/drawable/ic_baseline_camera_24.xml`](../../app/src/main/res/drawable/ic_baseline_camera_24.xml)
- [`app/src/main/res/drawable/ic_baseline_card_giftcard_24.xml`](../../app/src/main/res/drawable/ic_baseline_card_giftcard_24.xml)
- [`app/src/main/res/drawable/ic_baseline_cast_connected_24.xml`](../../app/src/main/res/drawable/ic_baseline_cast_connected_24.xml)
- [`app/src/main/res/drawable/ic_baseline_center_focus_weak_24.xml`](../../app/src/main/res/drawable/ic_baseline_center_focus_weak_24.xml)
- [`app/src/main/res/drawable/ic_baseline_color_lens_24.xml`](../../app/src/main/res/drawable/ic_baseline_color_lens_24.xml)
- [`app/src/main/res/drawable/ic_baseline_compare_arrows_24.xml`](../../app/src/main/res/drawable/ic_baseline_compare_arrows_24.xml)
- [`app/src/main/res/drawable/ic_baseline_domain_24.xml`](../../app/src/main/res/drawable/ic_baseline_domain_24.xml)
- [`app/src/main/res/drawable/ic_baseline_download_24.xml`](../../app/src/main/res/drawable/ic_baseline_download_24.xml)
- [`app/src/main/res/drawable/ic_baseline_emoji_emotions_24.xml`](../../app/src/main/res/drawable/ic_baseline_emoji_emotions_24.xml)
- [`app/src/main/res/drawable/ic_baseline_fast_forward_24.xml`](../../app/src/main/res/drawable/ic_baseline_fast_forward_24.xml)
- [`app/src/main/res/drawable/ic_baseline_fiber_manual_record_24.xml`](../../app/src/main/res/drawable/ic_baseline_fiber_manual_record_24.xml)
- [`app/src/main/res/drawable/ic_baseline_fingerprint_24.xml`](../../app/src/main/res/drawable/ic_baseline_fingerprint_24.xml)
- [`app/src/main/res/drawable/ic_baseline_flip_camera_android_24.xml`](../../app/src/main/res/drawable/ic_baseline_flip_camera_android_24.xml)
- [`app/src/main/res/drawable/ic_baseline_format_align_left_24.xml`](../../app/src/main/res/drawable/ic_baseline_format_align_left_24.xml)
- [`app/src/main/res/drawable/ic_baseline_grid_3x3_24.xml`](../../app/src/main/res/drawable/ic_baseline_grid_3x3_24.xml)
- [`app/src/main/res/drawable/ic_baseline_home_24.xml`](../../app/src/main/res/drawable/ic_baseline_home_24.xml)
- [`app/src/main/res/drawable/ic_baseline_http_24.xml`](../../app/src/main/res/drawable/ic_baseline_http_24.xml)
- [`app/src/main/res/drawable/ic_baseline_https_24.xml`](../../app/src/main/res/drawable/ic_baseline_https_24.xml)
- [`app/src/main/res/drawable/ic_baseline_import_contacts_24.xml`](../../app/src/main/res/drawable/ic_baseline_import_contacts_24.xml)
- [`app/src/main/res/drawable/ic_baseline_info_24.xml`](../../app/src/main/res/drawable/ic_baseline_info_24.xml)
- [`app/src/main/res/drawable/ic_baseline_layers_24.xml`](../../app/src/main/res/drawable/ic_baseline_layers_24.xml)
- [`app/src/main/res/drawable/ic_baseline_legend_toggle_24.xml`](../../app/src/main/res/drawable/ic_baseline_legend_toggle_24.xml)
- [`app/src/main/res/drawable/ic_baseline_link_24.xml`](../../app/src/main/res/drawable/ic_baseline_link_24.xml)
- [`app/src/main/res/drawable/ic_baseline_local_bar_24.xml`](../../app/src/main/res/drawable/ic_baseline_local_bar_24.xml)
- [`app/src/main/res/drawable/ic_baseline_location_on_24.xml`](../../app/src/main/res/drawable/ic_baseline_location_on_24.xml)
- [`app/src/main/res/drawable/ic_baseline_lock_24.xml`](../../app/src/main/res/drawable/ic_baseline_lock_24.xml)
- [`app/src/main/res/drawable/ic_baseline_low_priority_24.xml`](../../app/src/main/res/drawable/ic_baseline_low_priority_24.xml)
- [`app/src/main/res/drawable/ic_baseline_manage_search_24.xml`](../../app/src/main/res/drawable/ic_baseline_manage_search_24.xml)
- [`app/src/main/res/drawable/ic_baseline_more_vert_24.xml`](../../app/src/main/res/drawable/ic_baseline_more_vert_24.xml)
- [`app/src/main/res/drawable/ic_baseline_multiline_chart_24.xml`](../../app/src/main/res/drawable/ic_baseline_multiline_chart_24.xml)
- [`app/src/main/res/drawable/ic_baseline_multiple_stop_24.xml`](../../app/src/main/res/drawable/ic_baseline_multiple_stop_24.xml)
- [`app/src/main/res/drawable/ic_baseline_nat_24.xml`](../../app/src/main/res/drawable/ic_baseline_nat_24.xml)
- [`app/src/main/res/drawable/ic_baseline_nfc_24.xml`](../../app/src/main/res/drawable/ic_baseline_nfc_24.xml)
- [`app/src/main/res/drawable/ic_baseline_no_encryption_gmailerrorred_24.xml`](../../app/src/main/res/drawable/ic_baseline_no_encryption_gmailerrorred_24.xml)
- [`app/src/main/res/drawable/ic_baseline_person_24.xml`](../../app/src/main/res/drawable/ic_baseline_person_24.xml)
- [`app/src/main/res/drawable/ic_baseline_push_pin_24.xml`](../../app/src/main/res/drawable/ic_baseline_push_pin_24.xml)
- [`app/src/main/res/drawable/ic_baseline_refresh_24.xml`](../../app/src/main/res/drawable/ic_baseline_refresh_24.xml)
- [`app/src/main/res/drawable/ic_baseline_rule_folder_24.xml`](../../app/src/main/res/drawable/ic_baseline_rule_folder_24.xml)
- [`app/src/main/res/drawable/ic_baseline_running_with_errors_24.xml`](../../app/src/main/res/drawable/ic_baseline_running_with_errors_24.xml)
- [`app/src/main/res/drawable/ic_baseline_sanitizer_24.xml`](../../app/src/main/res/drawable/ic_baseline_sanitizer_24.xml)
- [`app/src/main/res/drawable/ic_baseline_security_24.xml`](../../app/src/main/res/drawable/ic_baseline_security_24.xml)
- [`app/src/main/res/drawable/ic_baseline_shuffle_24.xml`](../../app/src/main/res/drawable/ic_baseline_shuffle_24.xml)
- [`app/src/main/res/drawable/ic_baseline_shutter_speed_24.xml`](../../app/src/main/res/drawable/ic_baseline_shutter_speed_24.xml)
- [`app/src/main/res/drawable/ic_baseline_speed_24.xml`](../../app/src/main/res/drawable/ic_baseline_speed_24.xml)
- [`app/src/main/res/drawable/ic_baseline_stream_24.xml`](../../app/src/main/res/drawable/ic_baseline_stream_24.xml)
- [`app/src/main/res/drawable/ic_baseline_texture_24.xml`](../../app/src/main/res/drawable/ic_baseline_texture_24.xml)
- [`app/src/main/res/drawable/ic_baseline_timelapse_24.xml`](../../app/src/main/res/drawable/ic_baseline_timelapse_24.xml)
- [`app/src/main/res/drawable/ic_baseline_transform_24.xml`](../../app/src/main/res/drawable/ic_baseline_transform_24.xml)
- [`app/src/main/res/drawable/ic_baseline_transgender_24.xml`](../../app/src/main/res/drawable/ic_baseline_transgender_24.xml)
- [`app/src/main/res/drawable/ic_baseline_tv_24.xml`](../../app/src/main/res/drawable/ic_baseline_tv_24.xml)
- [`app/src/main/res/drawable/ic_baseline_update_24.xml`](../../app/src/main/res/drawable/ic_baseline_update_24.xml)
- [`app/src/main/res/drawable/ic_baseline_view_list_24.xml`](../../app/src/main/res/drawable/ic_baseline_view_list_24.xml)
- [`app/src/main/res/drawable/ic_baseline_vpn_key_24.xml`](../../app/src/main/res/drawable/ic_baseline_vpn_key_24.xml)
- [`app/src/main/res/drawable/ic_baseline_warning_24.xml`](../../app/src/main/res/drawable/ic_baseline_warning_24.xml)
- [`app/src/main/res/drawable/ic_baseline_wb_sunny_24.xml`](../../app/src/main/res/drawable/ic_baseline_wb_sunny_24.xml)
- [`app/src/main/res/drawable/ic_communication_phonelink_ring.xml`](../../app/src/main/res/drawable/ic_communication_phonelink_ring.xml)
- [`app/src/main/res/drawable/ic_device_data_usage.xml`](../../app/src/main/res/drawable/ic_device_data_usage.xml)
- [`app/src/main/res/drawable/ic_device_developer_mode.xml`](../../app/src/main/res/drawable/ic_device_developer_mode.xml)
- [`app/src/main/res/drawable/ic_file_cloud_queue.xml`](../../app/src/main/res/drawable/ic_file_cloud_queue.xml)
- [`app/src/main/res/drawable/ic_file_file_upload.xml`](../../app/src/main/res/drawable/ic_file_file_upload.xml)
- [`app/src/main/res/drawable/ic_hardware_router.xml`](../../app/src/main/res/drawable/ic_hardware_router.xml)
- [`app/src/main/res/drawable/ic_image_camera_alt.xml`](../../app/src/main/res/drawable/ic_image_camera_alt.xml)
- [`app/src/main/res/drawable/ic_image_edit.xml`](../../app/src/main/res/drawable/ic_image_edit.xml)
- [`app/src/main/res/drawable/ic_image_looks_6.xml`](../../app/src/main/res/drawable/ic_image_looks_6.xml)
- [`app/src/main/res/drawable/ic_image_photo.xml`](../../app/src/main/res/drawable/ic_image_photo.xml)
- [`app/src/main/res/drawable/ic_maps_360.xml`](../../app/src/main/res/drawable/ic_maps_360.xml)
- [`app/src/main/res/drawable/ic_maps_directions.xml`](../../app/src/main/res/drawable/ic_maps_directions.xml)
- [`app/src/main/res/drawable/ic_maps_directions_boat.xml`](../../app/src/main/res/drawable/ic_maps_directions_boat.xml)
- [`app/src/main/res/drawable/ic_navigation_apps.xml`](../../app/src/main/res/drawable/ic_navigation_apps.xml)
- [`app/src/main/res/drawable/ic_navigation_close.xml`](../../app/src/main/res/drawable/ic_navigation_close.xml)
- [`app/src/main/res/drawable/ic_navigation_menu.xml`](../../app/src/main/res/drawable/ic_navigation_menu.xml)
- [`app/src/main/res/drawable/ic_notification_enhanced_encryption.xml`](../../app/src/main/res/drawable/ic_notification_enhanced_encryption.xml)
- [`app/src/main/res/drawable/ic_qu_camera_launcher.xml`](../../app/src/main/res/drawable/ic_qu_camera_launcher.xml)
- [`app/src/main/res/drawable/ic_qu_shadowsocks_foreground.xml`](../../app/src/main/res/drawable/ic_qu_shadowsocks_foreground.xml)
- [`app/src/main/res/drawable/ic_qu_shadowsocks_launcher.xml`](../../app/src/main/res/drawable/ic_qu_shadowsocks_launcher.xml)
- [`app/src/main/res/drawable/ic_remote_groups.xml`](../../app/src/main/res/drawable/ic_remote_groups.xml)
- [`app/src/main/res/drawable/ic_remote_import.xml`](../../app/src/main/res/drawable/ic_remote_import.xml)
- [`app/src/main/res/drawable/ic_remote_phone.xml`](../../app/src/main/res/drawable/ic_remote_phone.xml)
- [`app/src/main/res/drawable/ic_service_active.xml`](../../app/src/main/res/drawable/ic_service_active.xml)
- [`app/src/main/res/drawable/ic_service_busy.xml`](../../app/src/main/res/drawable/ic_service_busy.xml)
- [`app/src/main/res/drawable/ic_service_connected.xml`](../../app/src/main/res/drawable/ic_service_connected.xml)
- [`app/src/main/res/drawable/ic_service_connecting.xml`](../../app/src/main/res/drawable/ic_service_connecting.xml)
- [`app/src/main/res/drawable/ic_service_idle.xml`](../../app/src/main/res/drawable/ic_service_idle.xml)
- [`app/src/main/res/drawable/ic_service_stopped.xml`](../../app/src/main/res/drawable/ic_service_stopped.xml)
- [`app/src/main/res/drawable/ic_service_stopping.xml`](../../app/src/main/res/drawable/ic_service_stopping.xml)
- [`app/src/main/res/drawable/ic_settings_password.xml`](../../app/src/main/res/drawable/ic_settings_password.xml)
- [`app/src/main/res/drawable/ic_social_emoji_symbols.xml`](../../app/src/main/res/drawable/ic_social_emoji_symbols.xml)
- [`app/src/main/res/drawable/ic_social_share.xml`](../../app/src/main/res/drawable/ic_social_share.xml)
- [`app/src/main/res/drawable/mode_choice_background.xml`](../../app/src/main/res/drawable/mode_choice_background.xml)
- [`app/src/main/res/drawable/status_dot_tv.xml`](../../app/src/main/res/drawable/status_dot_tv.xml)
- [`app/src/main/res/drawable/terminal_scroll_shape.xml`](../../app/src/main/res/drawable/terminal_scroll_shape.xml)
- [`app/src/main/res/drawable/tunxbox_banner.xml`](../../app/src/main/res/drawable/tunxbox_banner.xml)
- [`app/src/main/res/drawable/tunxbox_foreground.xml`](../../app/src/main/res/drawable/tunxbox_foreground.xml)
- [`app/src/main/res/drawable/tunxbox_launcher.xml`](../../app/src/main/res/drawable/tunxbox_launcher.xml)

## app/src/main/res/drawable-v26

- [`app/src/main/res/drawable-v26/ic_qu_camera_launcher.xml`](../../app/src/main/res/drawable-v26/ic_qu_camera_launcher.xml)
- [`app/src/main/res/drawable-v26/ic_qu_shadowsocks_launcher.xml`](../../app/src/main/res/drawable-v26/ic_qu_shadowsocks_launcher.xml)
- [`app/src/main/res/drawable-v26/tunxbox_launcher.xml`](../../app/src/main/res/drawable-v26/tunxbox_launcher.xml)

## app/src/main/res/font

- [`app/src/main/res/font/jetbrains_mono.ttf`](../../app/src/main/res/font/jetbrains_mono.ttf)

## app/src/main/res/layout

- [`app/src/main/res/layout/activity_main_tv.xml`](../../app/src/main/res/layout/activity_main_tv.xml)
- [`app/src/main/res/layout/card_profile_tv.xml`](../../app/src/main/res/layout/card_profile_tv.xml)
- [`app/src/main/res/layout/item_keyboard_key.xml`](../../app/src/main/res/layout/item_keyboard_key.xml)
- [`app/src/main/res/layout/layout_about.xml`](../../app/src/main/res/layout/layout_about.xml)
- [`app/src/main/res/layout/layout_add_entity.xml`](../../app/src/main/res/layout/layout_add_entity.xml)
- [`app/src/main/res/layout/layout_app_list.xml`](../../app/src/main/res/layout/layout_app_list.xml)
- [`app/src/main/res/layout/layout_app_placeholder.xml`](../../app/src/main/res/layout/layout_app_placeholder.xml)
- [`app/src/main/res/layout/layout_appbar.xml`](../../app/src/main/res/layout/layout_appbar.xml)
- [`app/src/main/res/layout/layout_apps.xml`](../../app/src/main/res/layout/layout_apps.xml)
- [`app/src/main/res/layout/layout_apps_item.xml`](../../app/src/main/res/layout/layout_apps_item.xml)
- [`app/src/main/res/layout/layout_asset_item.xml`](../../app/src/main/res/layout/layout_asset_item.xml)
- [`app/src/main/res/layout/layout_assets.xml`](../../app/src/main/res/layout/layout_assets.xml)
- [`app/src/main/res/layout/layout_backup.xml`](../../app/src/main/res/layout/layout_backup.xml)
- [`app/src/main/res/layout/layout_chain_settings.xml`](../../app/src/main/res/layout/layout_chain_settings.xml)
- [`app/src/main/res/layout/layout_config_settings.xml`](../../app/src/main/res/layout/layout_config_settings.xml)
- [`app/src/main/res/layout/layout_debug.xml`](../../app/src/main/res/layout/layout_debug.xml)
- [`app/src/main/res/layout/layout_edit_config.xml`](../../app/src/main/res/layout/layout_edit_config.xml)
- [`app/src/main/res/layout/layout_edit_group.xml`](../../app/src/main/res/layout/layout_edit_group.xml)
- [`app/src/main/res/layout/layout_empty.xml`](../../app/src/main/res/layout/layout_empty.xml)
- [`app/src/main/res/layout/layout_empty_route.xml`](../../app/src/main/res/layout/layout_empty_route.xml)
- [`app/src/main/res/layout/layout_group.xml`](../../app/src/main/res/layout/layout_group.xml)
- [`app/src/main/res/layout/layout_group_item.xml`](../../app/src/main/res/layout/layout_group_item.xml)
- [`app/src/main/res/layout/layout_group_list.xml`](../../app/src/main/res/layout/layout_group_list.xml)
- [`app/src/main/res/layout/layout_icon_list_item_2.xml`](../../app/src/main/res/layout/layout_icon_list_item_2.xml)
- [`app/src/main/res/layout/layout_import.xml`](../../app/src/main/res/layout/layout_import.xml)
- [`app/src/main/res/layout/layout_loading.xml`](../../app/src/main/res/layout/layout_loading.xml)
- [`app/src/main/res/layout/layout_logcat.xml`](../../app/src/main/res/layout/layout_logcat.xml)
- [`app/src/main/res/layout/layout_loglevel_help.xml`](../../app/src/main/res/layout/layout_loglevel_help.xml)
- [`app/src/main/res/layout/layout_main.xml`](../../app/src/main/res/layout/layout_main.xml)
- [`app/src/main/res/layout/layout_mtu_help.xml`](../../app/src/main/res/layout/layout_mtu_help.xml)
- [`app/src/main/res/layout/layout_network.xml`](../../app/src/main/res/layout/layout_network.xml)
- [`app/src/main/res/layout/layout_password_dialog.xml`](../../app/src/main/res/layout/layout_password_dialog.xml)
- [`app/src/main/res/layout/layout_profile.xml`](../../app/src/main/res/layout/layout_profile.xml)
- [`app/src/main/res/layout/layout_profile_list.xml`](../../app/src/main/res/layout/layout_profile_list.xml)
- [`app/src/main/res/layout/layout_progress.xml`](../../app/src/main/res/layout/layout_progress.xml)
- [`app/src/main/res/layout/layout_progress_list.xml`](../../app/src/main/res/layout/layout_progress_list.xml)
- [`app/src/main/res/layout/layout_promotions.xml`](../../app/src/main/res/layout/layout_promotions.xml)
- [`app/src/main/res/layout/layout_route.xml`](../../app/src/main/res/layout/layout_route.xml)
- [`app/src/main/res/layout/layout_route_item.xml`](../../app/src/main/res/layout/layout_route_item.xml)
- [`app/src/main/res/layout/layout_scanner.xml`](../../app/src/main/res/layout/layout_scanner.xml)
- [`app/src/main/res/layout/layout_scanner_tv.xml`](../../app/src/main/res/layout/layout_scanner_tv.xml)
- [`app/src/main/res/layout/layout_settings_activity.xml`](../../app/src/main/res/layout/layout_settings_activity.xml)
- [`app/src/main/res/layout/layout_stun.xml`](../../app/src/main/res/layout/layout_stun.xml)
- [`app/src/main/res/layout/layout_tools.xml`](../../app/src/main/res/layout/layout_tools.xml)
- [`app/src/main/res/layout/layout_urltest_preference_dialog.xml`](../../app/src/main/res/layout/layout_urltest_preference_dialog.xml)
- [`app/src/main/res/layout/layout_webview.xml`](../../app/src/main/res/layout/layout_webview.xml)
- [`app/src/main/res/layout/simple_menu_dropdown_item.xml`](../../app/src/main/res/layout/simple_menu_dropdown_item.xml)

## app/src/main/res/menu

- [`app/src/main/res/menu/add_group_menu.xml`](../../app/src/main/res/menu/add_group_menu.xml)
- [`app/src/main/res/menu/add_profile_menu.xml`](../../app/src/main/res/menu/add_profile_menu.xml)
- [`app/src/main/res/menu/add_route_menu.xml`](../../app/src/main/res/menu/add_route_menu.xml)
- [`app/src/main/res/menu/app_list_menu.xml`](../../app/src/main/res/menu/app_list_menu.xml)
- [`app/src/main/res/menu/group_action_menu.xml`](../../app/src/main/res/menu/group_action_menu.xml)
- [`app/src/main/res/menu/import_asset_menu.xml`](../../app/src/main/res/menu/import_asset_menu.xml)
- [`app/src/main/res/menu/logcat_menu.xml`](../../app/src/main/res/menu/logcat_menu.xml)
- [`app/src/main/res/menu/main_drawer_menu.xml`](../../app/src/main/res/menu/main_drawer_menu.xml)
- [`app/src/main/res/menu/main_options_menu.xml`](../../app/src/main/res/menu/main_options_menu.xml)
- [`app/src/main/res/menu/main_phone_menu.xml`](../../app/src/main/res/menu/main_phone_menu.xml)
- [`app/src/main/res/menu/per_app_proxy_menu.xml`](../../app/src/main/res/menu/per_app_proxy_menu.xml)
- [`app/src/main/res/menu/profile_apply_menu.xml`](../../app/src/main/res/menu/profile_apply_menu.xml)
- [`app/src/main/res/menu/profile_config_menu.xml`](../../app/src/main/res/menu/profile_config_menu.xml)
- [`app/src/main/res/menu/profile_share_menu.xml`](../../app/src/main/res/menu/profile_share_menu.xml)
- [`app/src/main/res/menu/scanner_menu.xml`](../../app/src/main/res/menu/scanner_menu.xml)
- [`app/src/main/res/menu/traffic_item_menu.xml`](../../app/src/main/res/menu/traffic_item_menu.xml)
- [`app/src/main/res/menu/traffic_menu.xml`](../../app/src/main/res/menu/traffic_menu.xml)
- [`app/src/main/res/menu/yacd_menu.xml`](../../app/src/main/res/menu/yacd_menu.xml)

## app/src/main/res/mipmap-anydpi-v26

- [`app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml`](../../app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml)

## app/src/main/res/mipmap-hdpi

- [`app/src/main/res/mipmap-hdpi/ic_launcher.png`](../../app/src/main/res/mipmap-hdpi/ic_launcher.png)
- [`app/src/main/res/mipmap-hdpi/ic_launcher_foreground.png`](../../app/src/main/res/mipmap-hdpi/ic_launcher_foreground.png)

## app/src/main/res/mipmap-mdpi

- [`app/src/main/res/mipmap-mdpi/ic_launcher.png`](../../app/src/main/res/mipmap-mdpi/ic_launcher.png)
- [`app/src/main/res/mipmap-mdpi/ic_launcher_foreground.png`](../../app/src/main/res/mipmap-mdpi/ic_launcher_foreground.png)

## app/src/main/res/mipmap-xhdpi

- [`app/src/main/res/mipmap-xhdpi/ic_launcher.png`](../../app/src/main/res/mipmap-xhdpi/ic_launcher.png)
- [`app/src/main/res/mipmap-xhdpi/ic_launcher_foreground.png`](../../app/src/main/res/mipmap-xhdpi/ic_launcher_foreground.png)

## app/src/main/res/mipmap-xxhdpi

- [`app/src/main/res/mipmap-xxhdpi/ic_launcher.png`](../../app/src/main/res/mipmap-xxhdpi/ic_launcher.png)
- [`app/src/main/res/mipmap-xxhdpi/ic_launcher_foreground.png`](../../app/src/main/res/mipmap-xxhdpi/ic_launcher_foreground.png)

## app/src/main/res/mipmap-xxxhdpi

- [`app/src/main/res/mipmap-xxxhdpi/ic_launcher.png`](../../app/src/main/res/mipmap-xxxhdpi/ic_launcher.png)
- [`app/src/main/res/mipmap-xxxhdpi/ic_launcher_foreground.png`](../../app/src/main/res/mipmap-xxxhdpi/ic_launcher_foreground.png)

## app/src/main/res/raw

- [`app/src/main/res/raw/insecure.txt`](../../app/src/main/res/raw/insecure.txt)
- [`app/src/main/res/raw/not_encrypted.txt`](../../app/src/main/res/raw/not_encrypted.txt)
- [`app/src/main/res/raw/shadowsocks_stream_cipher.txt`](../../app/src/main/res/raw/shadowsocks_stream_cipher.txt)
- [`app/src/main/res/raw/vmess_md5_auth.txt`](../../app/src/main/res/raw/vmess_md5_auth.txt)

## app/src/main/res/raw-zh-rCN

- [`app/src/main/res/raw-zh-rCN/insecure.txt`](../../app/src/main/res/raw-zh-rCN/insecure.txt)
- [`app/src/main/res/raw-zh-rCN/not_encrypted.txt`](../../app/src/main/res/raw-zh-rCN/not_encrypted.txt)
- [`app/src/main/res/raw-zh-rCN/shadowsocks_stream_cipher.txt`](../../app/src/main/res/raw-zh-rCN/shadowsocks_stream_cipher.txt)
- [`app/src/main/res/raw-zh-rCN/vmess_md5_auth.txt`](../../app/src/main/res/raw-zh-rCN/vmess_md5_auth.txt)

## app/src/main/res/resources.properties

- [`app/src/main/res/resources.properties`](../../app/src/main/res/resources.properties)

## app/src/main/res/values

- [`app/src/main/res/values/app_identity_actions.xml`](../../app/src/main/res/values/app_identity_actions.xml)
- [`app/src/main/res/values/arrays.xml`](../../app/src/main/res/values/arrays.xml)
- [`app/src/main/res/values/attrs.xml`](../../app/src/main/res/values/attrs.xml)
- [`app/src/main/res/values/branding.xml`](../../app/src/main/res/values/branding.xml)
- [`app/src/main/res/values/colors.xml`](../../app/src/main/res/values/colors.xml)
- [`app/src/main/res/values/crash_recovery.xml`](../../app/src/main/res/values/crash_recovery.xml)
- [`app/src/main/res/values/dimens.xml`](../../app/src/main/res/values/dimens.xml)
- [`app/src/main/res/values/ic_launcher_background.xml`](../../app/src/main/res/values/ic_launcher_background.xml)
- [`app/src/main/res/values/mode_choice.xml`](../../app/src/main/res/values/mode_choice.xml)
- [`app/src/main/res/values/remote_actions.xml`](../../app/src/main/res/values/remote_actions.xml)
- [`app/src/main/res/values/remote_focus.xml`](../../app/src/main/res/values/remote_focus.xml)
- [`app/src/main/res/values/strings.xml`](../../app/src/main/res/values/strings.xml)
- [`app/src/main/res/values/themes.xml`](../../app/src/main/res/values/themes.xml)
- [`app/src/main/res/values/tv_feedback.xml`](../../app/src/main/res/values/tv_feedback.xml)
- [`app/src/main/res/values/tv_ids.xml`](../../app/src/main/res/values/tv_ids.xml)
- [`app/src/main/res/values/tv_layout.xml`](../../app/src/main/res/values/tv_layout.xml)
- [`app/src/main/res/values/tv_strings.xml`](../../app/src/main/res/values/tv_strings.xml)

## app/src/main/res/values-ar

- [`app/src/main/res/values-ar/strings.xml`](../../app/src/main/res/values-ar/strings.xml)

## app/src/main/res/values-be

- [`app/src/main/res/values-be/strings.xml`](../../app/src/main/res/values-be/strings.xml)

## app/src/main/res/values-de

- [`app/src/main/res/values-de/strings.xml`](../../app/src/main/res/values-de/strings.xml)

## app/src/main/res/values-es

- [`app/src/main/res/values-es/strings.xml`](../../app/src/main/res/values-es/strings.xml)

## app/src/main/res/values-fa

- [`app/src/main/res/values-fa/strings.xml`](../../app/src/main/res/values-fa/strings.xml)

## app/src/main/res/values-fr

- [`app/src/main/res/values-fr/strings.xml`](../../app/src/main/res/values-fr/strings.xml)

## app/src/main/res/values-in

- [`app/src/main/res/values-in/strings.xml`](../../app/src/main/res/values-in/strings.xml)

## app/src/main/res/values-it

- [`app/src/main/res/values-it/strings.xml`](../../app/src/main/res/values-it/strings.xml)

## app/src/main/res/values-ja

- [`app/src/main/res/values-ja/strings.xml`](../../app/src/main/res/values-ja/strings.xml)

## app/src/main/res/values-ko

- [`app/src/main/res/values-ko/strings.xml`](../../app/src/main/res/values-ko/strings.xml)

## app/src/main/res/values-nb-rNO

- [`app/src/main/res/values-nb-rNO/strings.xml`](../../app/src/main/res/values-nb-rNO/strings.xml)

## app/src/main/res/values-night

- [`app/src/main/res/values-night/colors.xml`](../../app/src/main/res/values-night/colors.xml)
- [`app/src/main/res/values-night/remote_focus.xml`](../../app/src/main/res/values-night/remote_focus.xml)

## app/src/main/res/values-nl

- [`app/src/main/res/values-nl/strings.xml`](../../app/src/main/res/values-nl/strings.xml)

## app/src/main/res/values-pt-rBR

- [`app/src/main/res/values-pt-rBR/strings.xml`](../../app/src/main/res/values-pt-rBR/strings.xml)

## app/src/main/res/values-ru

- [`app/src/main/res/values-ru/app_identity_actions.xml`](../../app/src/main/res/values-ru/app_identity_actions.xml)
- [`app/src/main/res/values-ru/crash_recovery.xml`](../../app/src/main/res/values-ru/crash_recovery.xml)
- [`app/src/main/res/values-ru/mode_choice.xml`](../../app/src/main/res/values-ru/mode_choice.xml)
- [`app/src/main/res/values-ru/remote_actions.xml`](../../app/src/main/res/values-ru/remote_actions.xml)
- [`app/src/main/res/values-ru/strings.xml`](../../app/src/main/res/values-ru/strings.xml)
- [`app/src/main/res/values-ru/tv_feedback.xml`](../../app/src/main/res/values-ru/tv_feedback.xml)
- [`app/src/main/res/values-ru/tv_strings.xml`](../../app/src/main/res/values-ru/tv_strings.xml)

## app/src/main/res/values-tr

- [`app/src/main/res/values-tr/strings.xml`](../../app/src/main/res/values-tr/strings.xml)

## app/src/main/res/values-uk

- [`app/src/main/res/values-uk/strings.xml`](../../app/src/main/res/values-uk/strings.xml)

## app/src/main/res/values-w600dp-h480dp

- [`app/src/main/res/values-w600dp-h480dp/tv_layout.xml`](../../app/src/main/res/values-w600dp-h480dp/tv_layout.xml)

## app/src/main/res/values-zh-rCN

- [`app/src/main/res/values-zh-rCN/strings.xml`](../../app/src/main/res/values-zh-rCN/strings.xml)

## app/src/main/res/values-zh-rHK

- [`app/src/main/res/values-zh-rHK/strings.xml`](../../app/src/main/res/values-zh-rHK/strings.xml)

## app/src/main/res/values-zh-rTW

- [`app/src/main/res/values-zh-rTW/strings.xml`](../../app/src/main/res/values-zh-rTW/strings.xml)

## app/src/main/res/xml

- [`app/src/main/res/xml/anytls_preferences.xml`](../../app/src/main/res/xml/anytls_preferences.xml)
- [`app/src/main/res/xml/backup_descriptor.xml`](../../app/src/main/res/xml/backup_descriptor.xml)
- [`app/src/main/res/xml/backup_rules.xml`](../../app/src/main/res/xml/backup_rules.xml)
- [`app/src/main/res/xml/balancer_preferences.xml`](../../app/src/main/res/xml/balancer_preferences.xml)
- [`app/src/main/res/xml/cache_paths.xml`](../../app/src/main/res/xml/cache_paths.xml)
- [`app/src/main/res/xml/config_preferences.xml`](../../app/src/main/res/xml/config_preferences.xml)
- [`app/src/main/res/xml/global_preferences.xml`](../../app/src/main/res/xml/global_preferences.xml)
- [`app/src/main/res/xml/group_preferences.xml`](../../app/src/main/res/xml/group_preferences.xml)
- [`app/src/main/res/xml/hysteria_preferences.xml`](../../app/src/main/res/xml/hysteria_preferences.xml)
- [`app/src/main/res/xml/mieru_preferences.xml`](../../app/src/main/res/xml/mieru_preferences.xml)
- [`app/src/main/res/xml/naive_preferences.xml`](../../app/src/main/res/xml/naive_preferences.xml)
- [`app/src/main/res/xml/name_preferences.xml`](../../app/src/main/res/xml/name_preferences.xml)
- [`app/src/main/res/xml/neko_preferences.xml`](../../app/src/main/res/xml/neko_preferences.xml)
- [`app/src/main/res/xml/network_security_config.xml`](../../app/src/main/res/xml/network_security_config.xml)
- [`app/src/main/res/xml/route_preferences.xml`](../../app/src/main/res/xml/route_preferences.xml)
- [`app/src/main/res/xml/shadowsocks_preferences.xml`](../../app/src/main/res/xml/shadowsocks_preferences.xml)
- [`app/src/main/res/xml/shadowtls_preferences.xml`](../../app/src/main/res/xml/shadowtls_preferences.xml)
- [`app/src/main/res/xml/shortcuts.xml`](../../app/src/main/res/xml/shortcuts.xml)
- [`app/src/main/res/xml/socks_preferences.xml`](../../app/src/main/res/xml/socks_preferences.xml)
- [`app/src/main/res/xml/ssh_preferences.xml`](../../app/src/main/res/xml/ssh_preferences.xml)
- [`app/src/main/res/xml/standard_v2ray_preferences.xml`](../../app/src/main/res/xml/standard_v2ray_preferences.xml)
- [`app/src/main/res/xml/trojan_go_preferences.xml`](../../app/src/main/res/xml/trojan_go_preferences.xml)
- [`app/src/main/res/xml/tuic_preferences.xml`](../../app/src/main/res/xml/tuic_preferences.xml)
- [`app/src/main/res/xml/wireguard_preferences.xml`](../../app/src/main/res/xml/wireguard_preferences.xml)

## app/src/test

- [`app/src/test/java/io/nekohasekai/sagernet/group/SubscriptionUpdateGuardTest.kt`](../../app/src/test/java/io/nekohasekai/sagernet/group/SubscriptionUpdateGuardTest.kt)
- [`app/src/test/java/io/nekohasekai/sagernet/ui/BrandingRecoveryTest.kt`](../../app/src/test/java/io/nekohasekai/sagernet/ui/BrandingRecoveryTest.kt)
- [`app/src/test/java/io/nekohasekai/sagernet/ui/LaunchRegressionTest.kt`](../../app/src/test/java/io/nekohasekai/sagernet/ui/LaunchRegressionTest.kt)
- [`app/src/test/java/io/nekohasekai/sagernet/ui/ReleaseUpdatePolicyTest.kt`](../../app/src/test/java/io/nekohasekai/sagernet/ui/ReleaseUpdatePolicyTest.kt)
- [`app/src/test/java/io/nekohasekai/sagernet/ui/RemoteReadabilityTest.kt`](../../app/src/test/java/io/nekohasekai/sagernet/ui/RemoteReadabilityTest.kt)
- [`app/src/test/java/io/nekohasekai/sagernet/ui/tv/RemoteUiTest.kt`](../../app/src/test/java/io/nekohasekai/sagernet/ui/tv/RemoteUiTest.kt)
- [`app/src/test/java/io/nekohasekai/sagernet/ui/tv/TransferImportInputTest.kt`](../../app/src/test/java/io/nekohasekai/sagernet/ui/tv/TransferImportInputTest.kt)
- [`app/src/test/java/io/nekohasekai/sagernet/ui/tv/TransferImportServerTest.kt`](../../app/src/test/java/io/nekohasekai/sagernet/ui/tv/TransferImportServerTest.kt)
- [`app/src/test/java/io/nekohasekai/sagernet/ui/tv/TransferProtocolTest.kt`](../../app/src/test/java/io/nekohasekai/sagernet/ui/tv/TransferProtocolTest.kt)
- [`app/src/test/java/io/nekohasekai/sagernet/ui/tv/TvInteractionPolicyTest.kt`](../../app/src/test/java/io/nekohasekai/sagernet/ui/tv/TvInteractionPolicyTest.kt)
- [`app/src/test/java/io/nekohasekai/sagernet/ui/tv/TvLayoutPolicyTest.kt`](../../app/src/test/java/io/nekohasekai/sagernet/ui/tv/TvLayoutPolicyTest.kt)

## buildScript

- [`buildScript/check_project_docs.py`](../../buildScript/check_project_docs.py)
- [`buildScript/copyLocal.sh`](../../buildScript/copyLocal.sh)
- [`buildScript/fdroid/prebuild.sh`](../../buildScript/fdroid/prebuild.sh)
- [`buildScript/init/action/gradle.sh`](../../buildScript/init/action/gradle.sh)
- [`buildScript/init/env.sh`](../../buildScript/init/env.sh)
- [`buildScript/init/env_ndk.sh`](../../buildScript/init/env_ndk.sh)
- [`buildScript/lib/assets.sh`](../../buildScript/lib/assets.sh)
- [`buildScript/lib/core.sh`](../../buildScript/lib/core.sh)
- [`buildScript/lib/core/build.sh`](../../buildScript/lib/core/build.sh)
- [`buildScript/lib/core/get_source.sh`](../../buildScript/lib/core/get_source.sh)
- [`buildScript/lib/core/get_source_env.sh`](../../buildScript/lib/core/get_source_env.sh)
- [`buildScript/lib/core/init.sh`](../../buildScript/lib/core/init.sh)
- [`buildScript/nkmr`](../../buildScript/nkmr)
- [`buildScript/package_preview_apks.py`](../../buildScript/package_preview_apks.py)
- [`buildScript/run_emulator_smoke.sh`](../../buildScript/run_emulator_smoke.sh)
- [`buildScript/setup_signing.py`](../../buildScript/setup_signing.py)
- [`buildScript/signing_bundle.py`](../../buildScript/signing_bundle.py)
- [`buildScript/test_assets.py`](../../buildScript/test_assets.py)
- [`buildScript/test_native_alignment.py`](../../buildScript/test_native_alignment.py)
- [`buildScript/test_package_preview_apks.py`](../../buildScript/test_package_preview_apks.py)
- [`buildScript/test_signing_bundle.py`](../../buildScript/test_signing_bundle.py)
- [`buildScript/test_tv_transfer_page.cjs`](../../buildScript/test_tv_transfer_page.cjs)
- [`buildScript/verify_native_alignment.py`](../../buildScript/verify_native_alignment.py)
- [`buildScript/verify_update.py`](../../buildScript/verify_update.py)

## buildSrc

- [`buildSrc/build.gradle.kts`](../../buildSrc/build.gradle.kts)
- [`buildSrc/src/main/kotlin/Helpers.kt`](../../buildSrc/src/main/kotlin/Helpers.kt)

## docs

- [`docs/README.md`](../../docs/README.md)
- [`docs/architecture.md`](../../docs/architecture.md)
- [`docs/decisions.md`](../../docs/decisions.md)
- [`docs/development.md`](../../docs/development.md)
- [`docs/emulator-testing.md`](../../docs/emulator-testing.md)
- [`docs/features.md`](../../docs/features.md)
- [`docs/project-map.md`](../../docs/project-map.md)
- [`docs/reference/preferences.md`](../../docs/reference/preferences.md)
- [`docs/reference/repository-index.md`](../../docs/reference/repository-index.md)
- [`docs/scenarios.md`](../../docs/scenarios.md)
- [`docs/security.md`](../../docs/security.md)
- [`docs/signing-and-updates.md`](../../docs/signing-and-updates.md)
- [`docs/tv-readiness-plan.md`](../../docs/tv-readiness-plan.md)
- [`docs/tv-transfer.md`](../../docs/tv-transfer.md)
- [`docs/ui-quality-review.md`](../../docs/ui-quality-review.md)

## gradle

- [`gradle/wrapper/gradle-wrapper.jar`](../../gradle/wrapper/gradle-wrapper.jar)
- [`gradle/wrapper/gradle-wrapper.properties`](../../gradle/wrapper/gradle-wrapper.properties)

## libcore

- [`libcore/.gitignore`](../../libcore/.gitignore)
- [`libcore/LICENSE`](../../libcore/LICENSE)
- [`libcore/assets.go`](../../libcore/assets.go)
- [`libcore/assets_android.go`](../../libcore/assets_android.go)
- [`libcore/assets_other.go`](../../libcore/assets_other.go)
- [`libcore/box.go`](../../libcore/box.go)
- [`libcore/box_include.go`](../../libcore/box_include.go)
- [`libcore/build.sh`](../../libcore/build.sh)
- [`libcore/certs.go`](../../libcore/certs.go)
- [`libcore/crypto.go`](../../libcore/crypto.go)
- [`libcore/device/debug.go`](../../libcore/device/debug.go)
- [`libcore/device/device.go`](../../libcore/device/device.go)
- [`libcore/dns_android.go`](../../libcore/dns_android.go)
- [`libcore/dns_box.go`](../../libcore/dns_box.go)
- [`libcore/ech/ech.go`](../../libcore/ech/ech.go)
- [`libcore/fix.go`](../../libcore/fix.go)
- [`libcore/geoip.go`](../../libcore/geoip.go)
- [`libcore/geosite.go`](../../libcore/geosite.go)
- [`libcore/go.mod`](../../libcore/go.mod)
- [`libcore/go.sum`](../../libcore/go.sum)
- [`libcore/http.go`](../../libcore/http.go)
- [`libcore/init.sh`](../../libcore/init.sh)
- [`libcore/interface_monitor.go`](../../libcore/interface_monitor.go)
- [`libcore/io.go`](../../libcore/io.go)
- [`libcore/nb4a.go`](../../libcore/nb4a.go)
- [`libcore/platform_box.go`](../../libcore/platform_box.go)
- [`libcore/platform_java.go`](../../libcore/platform_java.go)
- [`libcore/procfs/procfs.go`](../../libcore/procfs/procfs.go)
- [`libcore/stun.go`](../../libcore/stun.go)
- [`libcore/stun/README`](../../libcore/stun/README)
- [`libcore/stun/attribute.go`](../../libcore/stun/attribute.go)
- [`libcore/stun/client.go`](../../libcore/stun/client.go)
- [`libcore/stun/const.go`](../../libcore/stun/const.go)
- [`libcore/stun/discover.go`](../../libcore/stun/discover.go)
- [`libcore/stun/doc.go`](../../libcore/stun/doc.go)
- [`libcore/stun/host.go`](../../libcore/stun/host.go)
- [`libcore/stun/log.go`](../../libcore/stun/log.go)
- [`libcore/stun/net.go`](../../libcore/stun/net.go)
- [`libcore/stun/packet.go`](../../libcore/stun/packet.go)
- [`libcore/stun/response.go`](../../libcore/stun/response.go)
- [`libcore/stun/tests.go`](../../libcore/stun/tests.go)
- [`libcore/stun/utils.go`](../../libcore/stun/utils.go)

## Корень

- [`.gitignore`](../../.gitignore)
- [`AUTHORS`](../../AUTHORS)
- [`LICENSE`](../../LICENSE)
- [`README.md`](../../README.md)
- [`build.gradle.kts`](../../build.gradle.kts)
- [`gradle.properties`](../../gradle.properties)
- [`gradlew`](../../gradlew)
- [`gradlew.bat`](../../gradlew.bat)
- [`lint.xml`](../../lint.xml)
- [`nb4a.properties`](../../nb4a.properties)
- [`release.keystore`](../../release.keystore)
- [`repositories.gradle.kts`](../../repositories.gradle.kts)
- [`run`](../../run)
- [`settings.gradle.kts`](../../settings.gradle.kts)
- [`signing-certificate.sha256`](../../signing-certificate.sha256)

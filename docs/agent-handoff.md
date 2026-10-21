# Передача разработки: TunXBox 1.6

Обновлено: 2026-10-21 UTC. Это проверенный снимок состояния, не обещание фоновой работы. Сначала перепроверьте GitHub refs, PR, runs и release assets: более поздние изменения имеют приоритет над этим текстом.

## 1. Где продолжать

- Репозиторий: https://github.com/LeonidYasin/TunXBox . Рабочая ветка `fix/tv-pointer-scroll-1.6`, единый draft [PR #12 → master](https://github.com/LeonidYasin/TunXBox/pull/12). Не продолжать 1.6 от старого `master` или superseded PR #11.
- База: `680180361fe432cb81d6fea5eeb5d697a3693f95` (`feature/tv-groups-subscription-names`), потомок опубликованного rc.150 `2c4a62c18f968585983babd68d70b917440b7f56`.
- Runtime исправления: `6b5a8b2` (портретный TV scroll + F-Droid/source port), `8cdee1e` (SEO/focus), `0bd5770` (bounded-check race + историческая лицензия шрифта).
- `40e8a93` — F-Droid scanner staging/метаданные/API36 ps16k CI; `ecaea02`, `e7e2d0a` — отдельная публикация готовых артефактов, без изменений runtime APK. Текущий HEAD может быть новее из-за handoff/docs; не подменяйте им source SHA опубликованного APK.
- `master` не merged в этой работе; stable 1.5.0 не изменён. PR #7/#8/#9 heads — предки кандидата (проверено в полной истории). PR #5 head не предок: REALITY patch перенесён отдельно как `4541e4b`; не вливать старую конфликтующую ветку целиком.

```sh
git fetch origin --tags
# В shallow clone сначала получить полную историю, иначе ancestry может быть ложным.
git fetch --unshallow origin  # только если git rev-parse --is-shallow-repository == true
git switch fix/tv-pointer-scroll-1.6
git pull --ff-only
git status --short
```

## 2. Что реально опубликовано

| Канал | Факт на дату снимка |
|---|---|
| Stable | [1.5.0](https://github.com/LeonidYasin/TunXBox/releases/tag/v1.5.0), неизменяемый baseline |
| Rolling test | [v1.6.0-rc](https://github.com/LeonidYasin/TunXBox/releases/tag/v1.6.0-rc) всё ещё rc.150; не заменён восстановительной публикацией |
| Новый ручной RC | [v1.6.0-rc.158](https://github.com/LeonidYasin/TunXBox/releases/tag/v1.6.0-rc.158), versionCode **48000158**, exact source **0bd5770d861d72f2e0523eb1c519f6a7bac1816f** |

rc.158 содержит ARM64, ARM32 и universal (ARM32/ARM64/x86/x86_64) APK, `apk-manifest.json`, `SHA256SUMS.txt`. Все три публичных файла заново скачаны: HTTP 200, SHA256 и HTTP download filenames совпали. Настоящая цель annotated tag проверена через Git refs/tag object: `0bd5770…`. Поле release `target_commitish` может показывать master даже при существующем точном теге — не использовать его как единственное доказательство.

Постоянный APK signer SHA256: `34764328dd73482587e64d56008a9ef7b860069087e279cb984f7b2e5055b5ef`. Проверяется существующим `signing-certificate.sha256`; это публичный сертификат, не private key.

### Почему была остановка и как восстановлено

1. [Исходная сборка run 39505780016](https://github.com/LeonidYasin/TunXBox/actions/runs/39505780016) успешно выполнила JVM/UI/native/signing/packaging, но завершилась failure **только на публикации**: `git push -f` rolling-тега отказал GitHub App без `workflows` permission для `.github/workflows/source-readiness.yml`.
2. Это конкретный встроенный `GITHUB_TOKEN` в Actions, а не доказательство отсутствия прав у всех подключений. Другой trigger того же workflow не добавляет permission. Не добавлять выдуманный `permissions: workflows: write` в Actions.
3. Существующий авторизованный пользовательский MCP `leonid-mcp.duckdns.org/mcp` → `create_tag` создал отдельный annotated `v1.6.0-rc.158` на точный проверенный SHA. Старый rolling tag и stable не перемещались.
4. `prerelease-from-pr.yml` получил явный режим `recover_rc158`; reusable `publish-verified-rc158.yml` повторно проверяет исходный run/успех каждого gate, 38 UI test cases, actual tag SHA, неизменность verifier contracts, конкретные APK hashes, реальную подпись/version/ABI и 16KB alignment. Скачивает опубликованные assets и проверяет их до снятия draft. Ни пересборки, ни signing secrets в этом режиме нет.
5. [Восстановительная публикация run 39511252702](https://github.com/LeonidYasin/TunXBox/actions/runs/39511252702) **SUCCESS**. Предыдущая попытка 39510852939 упала из-за `sdkmanager` вне PATH; исправлено абсолютным путём установленного SDK.

Режим recovery одноразовый, жёстко ограничен rc.158; повторный запуск отказывается перезаписывать уже публичный release. Для будущих номеров проектировать отдельный проверяемый процесс, не менять константы без нового аудита. `recover_rc158=true` — публикующий режим; с `check_only=true` caller ничего не публикует. Для обновления rolling tag отдельно потребуется корректная авторизация/действие владельца; номерной RC **не появится автоматически** в нынешнем in-app test updater.

## 3. Исправления и доказательства

### Портретный TV scroll

`ui/tv/TvPointerScrollController.kt`: touchSlop/axis lock, ровно один CANCEL дочернему горизонтальному пути; вертикальный жест идёт в настоящий outer RecyclerView `onTouchEvent` с native velocity/fling. Mouse wheel через scroll factor. Pointer mode блокирует focus realignment до D-pad. `MainBrowseFragment` не возвращает старый фокус от отложенного DB callback; D-pad восстанавливает видимый фокус и не исполняется дважды. Taps/long press/horizontal gestures остаются обычными. Регрессии JVM и реальный instrumentation `portraitTvScrollsWithFingerAndMouseThenResumesRemoteWithoutChangingVpn`.

### Гонка NET_BUSY

`group/BoundedConnectionCheck.kt` раньше возобновлял continuation до `busy=false`; мгновенная следующая проверка ошибочно видела занятость. Теперь освобождение после завершения реальной работы, но **до resume**. Настоящий зависший worker после timeout/cancel по-прежнему BUSY — не разрешать параллельный запуск. Новый детерминированный Unconfined/latch тест падал на старом коде и прошёл на новом (4/4 standalone JUnit17/Kotlin2.0.21/coroutines1.6.4); полные CI JVM tests также прошли.

| Проверка exact APK source 0bd5770 | Результат |
|---|---|
| Preview JVM/protocol/remote tests | PASS в исходном run |
| Android 15 / 4096 bytes | **19/19**, 0 failure/error/skip, actual XML inspected |
| Android 16 / 16384 bytes | **19/19**, 0 failure/error/skip, actual XML inspected |
| Persistent signer, package, increasing versionCode, three ABI packages | PASS |
| Actual native ELF/ZIP 16KB alignment | PASS |
| F-Droid clean source native/assets/dashboard + release/JVM compile | PASS в [39505477151](https://github.com/LeonidYasin/TunXBox/actions/runs/39505477151) |
| F-Droid Android15 / 4KB | 19/19 в том же run |
| F-Droid API35 ps16k | **FAIL**, system_server ReferenceQueueDaemon crash, suite aborted; НЕ считать pass |
| F-Droid API36 ps16k, source e7e2d0a | **19/19 PASS**, actual XML inspected, run [39511234808](https://github.com/LeonidYasin/TunXBox/actions/runs/39511234808); это не CI более позднего HEAD |

Снимок до recovery: 79 Python + 15 browser-transfer tests прошли. После добавления recovery regression tests и handoff: **84 Python tests PASS**, **15 browser-transfer tests PASS**, docs inventory **831 files / 224 XML preference entries / 1140 local links** и workflow YAML parse PASS. Это локальные проверки текущего continuation tree, не пересборка опубликованного APK. Успешные debug/emulator tests не являются физической приёмкой подписанного release APK.

## 4. Следующие действия — в порядке

1. Владелец ставит rc.158 вручную (backup конфигурации), проверяет **портретный TV на реальном смартфоне**: swipe/fling вверх/вниз, отпускание без snap-back, wheel/drag мыши, затем D-pad; обычные горизонтальные ряды, tap/long press и VPN/выбранный профиль не меняются сами. Приложить модель/Android/ABI/точную версию и наблюдение, без приватной конфигурации.
2. На TV: 720p/1080p, крупный шрифт, 1/3/10 групп, длинные имена, rename/update/delete/Back, импорт названий, возврат фокуса. Updater: реальная загрузка/докачка/installer permission/data preservation. Эмулятор не заменяет OEM installer.
3. Разрешить 15 конфликтующих файлов с `master` отдельным проверяемым integration commit, не через blanket ours/theirs. `git merge-tree --write-tree --name-only HEAD origin/master` показал конфликты в release workflows, MainActivity/strings, regression/instrumentation tests и docs. PR #12 сейчас `mergeable=false`; даже зелёный branch CI не делает конфликтующий PR готовым к merge. Сохранить оба набора сценариев и полные test-name gates, регенерировать docs inventory и заново собрать/протестировать итоговый merge tree. Затем посмотреть [все PR checks](https://github.com/LeonidYasin/TunXBox/pull/12/checks) и последние [Source readiness runs](https://github.com/LeonidYasin/TunXBox/actions/workflows/source-readiness.yml). Последний commit обязан иметь свои conclusions; предыдущий passed source job или cancelled run не равен full green.
4. Для #6/#10 выполнить безопасную одинаковую-профиль/сеть/устройство проверку и принять явное fix/defer решение. Не закрывать исходный report только за добавление диагностики.
5. После приёмки и final CI — review/integration PR #12 → master, затем отдельный stable release от **точного нового master SHA** с неизменной подписью/возрастающим versionCode. Numbered rc.158 не является разрешением слить/выпустить stable. Проверить contract `stable-release.yml`, включая source tag и текущий RC manifest; не обходить mismatch.
6. PR #7/#8/#9 закрывать как covered by integration только после фактического включения и принятия, не мержить повторно. PR #5 unique diff/remaining scope сопоставить с #6/#10/#13; его conflicting whole branch не вливать. См. [triage](release-triage.md).
7. [Эксперимент extended/Xray](core-extension-strategy.md) вести отдельно после фиксации stable scope. Поддержка XHTTP в 1.6 не заявляется.

## 4а. Последняя проверка и точка входа master

- Source/F-Droid run [39511234808](https://github.com/LeonidYasin/TunXBox/actions/runs/39511234808), exact source `e7e2d0a866053fcbd85758f62abe7829a6860d40`: **оба jobs SUCCESS**, native/assets/dashboard from source, release/instrumentation compilation, source preservation. Actual XML из artifact `12306771922` повторно прочитан: Android15/4096 **19/19**, Android16/16384 **19/19**, ноль failures/errors/skips; именованный portrait scroll test прошёл в обоих. Машинная запись: [F-Droid CI evidence](releases/fdroid-e7e2d0a-evidence.json). Это не официальный F-Droid buildserver и не store acceptance.
- Stable-check run [39511234765](https://github.com/LeonidYasin/TunXBox/actions/runs/39511234765) на том же старом source завершился failure на `Check project documentation`: `File inventory is stale`. На continuation commit `7deb10a` реестр уже исправлен; fresh checkout локально проходит `831 / 224 / 1140`. Не удалять проверку и не объявлять старый красный run успешным. Новый exact-head check-only run нужно проверить отдельно.
- На `7deb10a` API не показывал check runs; PR #12 конфликтует с master. Проверка ветки через workflow_dispatch не проверяет ещё не собранный merge result.
- Защищённый master отклонил прямой docs push (`Changes must be made through a pull request`); никаких изменений в master не внесено. Создан отдельный ready [PR #14](https://github.com/LeonidYasin/TunXBox/pull/14), ветка `docs/agent-entrypoint`, commit `9cfdb66460e50083cd65b6a3acfae1d73fb199dd`: только AGENTS.md, CLAUDE.md и inventory. После review/merge новый агент с master увидит ссылки на текущую ветку. До merge не утверждать, что master уже содержит эту точку входа. Не auto-merge.
- Root entry files активной ветки синхронизированы с PR #14, чтобы не создавать будущий add/add content conflict. Сгенерированный inventory при интеграции нужно обновить, а не выбирать старую сторону.
- Рабочее окружение может быть заменено: предыдущие `/data` clones/архивы в новой сессии фактически отсутствовали. Актуальная ветка заново клонирована и проверена. Repo handoff — постоянный источник, sandbox paths/старые archive session links не считать гарантированно доступным backup.

Конфликтующие файлы (master `99a8b6` против continuation `7deb10a`):

```text
.github/workflows/prerelease-from-pr.yml
.github/workflows/stable-release.yml
app/src/androidTest/java/io/nekohasekai/sagernet/ui/EmulatorSmokeTest.kt
app/src/main/java/io/nekohasekai/sagernet/ui/MainActivity.kt
app/src/main/res/values-ru/strings.xml
app/src/main/res/values/strings.xml
app/src/test/java/io/nekohasekai/sagernet/ui/LaunchRegressionTest.kt
buildScript/run_emulator_smoke.sh
docs/development.md
docs/emulator-testing.md
docs/lan-discovery.md
docs/project-map.md
docs/reference/repository-index.md
docs/roadmap.md
docs/signing-and-updates.md
```

## 5. F-Droid, источники, сайт — ограничения

- [fdroid/README.md](../fdroid/README.md), [readiness](../fdroid/READINESS.md), [source preservation](source-preservation.md) — подробные gates. Recipe **disabled**. Сборка helper из `build:`, после scanner; старый key удаляется `rm`, ZIP — `scandelete`, затем source build. Никаких scanignore/skipscan.
- fdroidserver 2.4.5 standalone scan Git app snapshot + revised staging: 0 problems. Lint прошёл на отдельной active audit copy с официальными fdroiddata categories (реальный disabled entry lint пропускает). Нет локальной binary/signature scan, полного staged Go/npm/srclib scan или actual F-Droid buildserver.
- Блокеры: явная лицензия libneko, исторические Yacd asset/copyright notices, все GeoIP/Geosite data inputs, all-transitive source availability, approved tools/srclibs/offline build, реальные Fastlane screenshots, store signing/update UX. Developer-pinned GitHub updater нельзя направлять на cross-signer APK; не ослаблять проверку сертификата.
- 7 direct snapshots с SHA256, Go module source cache, pnpm store и две datasets сохранены в переносном backup exact `0bd5770`; manifest проверен (29640 files). Не полный Git history/Maven/toolchains/все исходники; `allTransitiveSourcesArchived=false`. Private keys исключены. Archive SHA256 `ba659fde21db6d5ec217fe51e2b02280389a38429b0fdab6098aedc206cc6a35`; архив создан в предыдущей рабочей сессии, но его скачивание владельцем не подтверждено; не гарантировать доступ будущему агенту. При необходимости создать новый backup из clean current checkout; не выдавать старый archive за новый HEAD. Отдельные GitHub fork mirrors ранее запрещены permission403; независимое off-GitHub хранилище ещё не создано.
- Сайт RU/EN опубликован на GitHub Pages. Canonical/hreflang/JSON-LD/sitemap/robots проверены. Source generator сохраняет SEO при регенерации. Новый source tree может включать больше docs, чем deployed gh-pages: не объявлять их deployed без отдельного deployment/HTTP проверки. Search Console/Yandex ownership не подтверждены, индексация/ranking не гарантированы. Полные визуальные проверки новых docs routes не выполнены; домашние страницы были проверены desktop/mobile.

## 6. Команды воспроизведения

```sh
python3 -m unittest discover -s buildScript -p 'test_*.py'
node buildScript/test_tv_transfer_page.cjs
python3 buildScript/check_project_docs.py
# После добавления tracked файлов: --update-index, затем снова проверка.
python3 buildScript/sources/fetch.py prefetch
# Для offline restore: SOURCE_ARCHIVE_DIR=<verified backup archives/> SOURCE_OFFLINE=1
# Clean source preparation требует pin tools из lock.json (Go1.26.8/Node24.14.1/pnpm8.15.9/JDK17/NDK25).
bash buildScript/fdroid/prebuild.sh
bash gradlew app:assembleFdroidRelease -Ptunxbox.fdroidUnsigned=true -Ptunxbox.universalOnly=true
bash gradlew app:testFdroidDebugUnitTest app:assembleFdroidDebug app:assembleFdroidDebugAndroidTest
# На настоящих запущенных нужных эмуляторах, не подменять actual page-size:
bash buildScript/run_emulator_smoke.sh 4096 Fdroid
bash buildScript/run_emulator_smoke.sh 16384 Fdroid
# Backup — только из clean committed checkout; archive.py отказывается архивировать dirty source.
python3 buildScript/sources/archive.py --go-modules --pnpm-store .source-cache/pnpm-store
```

Не хранить секреты/приватные тестовые серверы/локальные sandbox пути в продолжении. Этот файл должен обновляться по фактам следующего агента, не становиться бесконечным транскриптом.

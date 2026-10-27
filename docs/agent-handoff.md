# Передача разработки: TunXBox 1.6

Обновлено: 2026-10-24 UTC. Это проверенный снимок состояния, не обещание фоновой работы. Сначала перепроверьте GitHub refs, PR, runs и release assets: более поздние изменения имеют приоритет над этим текстом.

## Автопереключение: обход всего списка вместо зацикливания — 2026-10-27

Владелец на устройстве увидел, что автопереключение ходит между двумя профилями (Швеция ↔ Швейцария) вместо обхода списка, и что непонятно, как режим выключить. Ветка `fix/tv-failover-rotation` от master `5432086` (после мержей #31/#32).

Причины (все три вместе давали петлю):

- `selectBestByPing` выбирал минимальный **сохранённый** пинг среди профилей с `status == 1 && ping > 0`. Непроверенные профили не участвовали, поэтому выборка схлопывалась до пары; «лучшим» обычно оказывался тот, что только что упал.
- Список кандидатов исключал только текущий сбойный профиль, без памяти о предыдущих сбоях цепочки.
- `syncHealthSession()` сбрасывал `failoverSwitches`/`failoverCooldownUntil` при любой смене активного профиля, а авто-переключение само её и вызывает (`serviceEpoch++`) → лимит и cooldown никогда не срабатывали.

Исправление:

- `TvAutoFailoverPolicy.selectNextFallback(order, currentId, triedIds)` — ротация по пользовательскому порядку (с переходом через конец), уже сбойные профили исключены; `applySwitch` добавляет выбранный id в `triedIds`. `selectBestByPing`/`selectFallback` удалены.
- Лимит = размер списка (`maxSwitchesPerSession=order.size`), cooldown 30 с сохранён; при исчерпании — сообщение «все профили из списка не отвечают» и остановка до ручной проверки.
- Цепочка сбрасывается только при успешной автоматической проверке (`result.available`), ручном выборе профиля или смене underlying network; смена профиля самим failover больше счётчики не обнуляет.
- Выключение: у включённой плитки подпись подсказывает «OK — изменить или выключить»; кнопка «Выключить» в окне сбрасывает режим и список и показывает подтверждение (`tv_auto_failover_disabled`).
- Тесты: `TvAutoFailoverPolicyTest` переписан на ротацию (обход списка, запрет возврата к сбойному, переход через конец, старт с начала, если текущего нет в списке) + прежние проверки cooldown/лимита/сериализации. Документация: `docs/tv-auto-failover.md` (включая разбор дефекта), `docs/features.md` F02.
- Статус: реализовано локально, локальные проверки зелёные; Kotlin/эмуляторы — в CI, физическая приёмка (сеть с двумя-тремя профилями, выключение режима) — за владельцем.

## Управление группами на ТВ — 2026-10-26

Владелец принял фикс диалога автопереключения (rc.165 опубликован) и попросил переработать работу с группами/подписками на ТВ: меню действий на плитке группы, сворачивание ряда и удаление/обновление/сортировка.

Ветка `feature/tv-group-actions` (от master `e7aa86b`), ещё не смержена и не опубликована:

- Плитка группы больше не выбирает группу и не запускает пинг молча: она открывает меню `showGroupActions` с пунктами «выбрать», «проверить (пинг)», «обновить подписку» (только подписки), «сортировка профилей» (порядок из подписки / по имени / по задержке через `ProxyGroup.order`), «свернуть/развернуть профили», «переименовать», «удалить».
- Свёрнутая группа оставляет в ряду только первую плитку; состояние по каждой группе хранится в `TvGroupViewPreferences` (`tv_collapsed_groups`) и переживает перезапуск, по умолчанию группа развёрнута.
- Удаление группы требует отключённого VPN, если в ней активный профиль; после удаления сбрасывается выбранный профиль из этой группы и забывается id в настройке свёрнутости.
- Новые pure-модули `TvGroupActions.kt` (каталог пунктов, `TvGroupSort`, `TvCollapsedGroups`) и `TvGroupViewPreferences.kt`; JVM-тесты `TvGroupActionsTest`, обновлён `RemoteUiTest` (подписи плитки в обычном и свёрнутом виде).
- Граф навигации: узлы `GROUP_ACTIONS`/`GROUP_SORT`/`GROUP_DELETE`, обновлён `docs/tv-navigation.md`; `docs/tv-groups-subscriptions.md` описывает меню и границы (перестановка групп выше/ниже намеренно не входит). Локально зелёные `test_tv_navigation.py`, `check_project_docs.py`, `test_project_site.py`.
- Статус: реализовано локально, не компилировалось локально (нет JDK/SDK). Нужны полный CI, публикация нового rolling RC и физическая приёмка; rc.165 это не содержит.

## Адрес LAN-прокси на ТВ и в настройках — 2026-10-26

Владельцу нужен полный адрес, который вводят клиенты в локальной сети, когда включён доступ к прокси. Ветка `feature/tv-lan-proxy-address` (от master `e7aa86b`, PR #32 draft), независима от `feature/tv-group-actions`.

- Ряд **Connection**, вторая плитка (`TvLanProxyCard` + `LanProxyCardPresenter`): показываются **все IPv4-адреса всех поднятых интерфейсов, кроме loopback**, без попытки определить «локальный» — у одного устройства бывают 4G, раздача по Wi-Fi, Ethernet, tethering, VPN. Каждый адрес — отдельной строкой `IP:порт`, без `ellipsize`, авто-размер 11–22 sp, поэтому `255.255.255.255:65535` (21 символ) виден целиком даже при крупном системном шрифте. Порядок: `wlan*`, `eth*`, точка доступа, остальные; дубликаты схлопываются. Плитки нет, если VPN не Connected, доступ из LAN выключен или IPv4-адресов нет; OK открывает подсказку со всеми адресами и именами интерфейсов.
- IPv6 намеренно не показывается: инбаунд слушает IPv4 `0.0.0.0`, такие подключения он не примет. Это ограничение протокола, а не фильтр адресов.
- Настройки: у переключателя «Разрешить подключения из локальной сети» (`Inbound Settings`) подпись становится «Клиенты в сети: <все адреса через запятую>», пересчитывается при включении, смене порта и возврате на экран; без IPv4 — честное «Нет адреса локальной сети».
- `LanProxyAddress` (общий для ТВ и настроек, порт `mixedPort`, HTTP+SOCKS5): перебор интерфейсов изолирован в `find()`, правила отбора/сортировки/формата — чистые; `LanProxyAddressTest` покрывает, что `tun`/`rmnet`/hotspot адреса не отбрасываются, loopback/IPv6/down отбрасываются, порядок и дубликаты; `RemoteUiTest` проверяет несколько полных строк без многоточия и скрытие лишней строки при сокращении списка.
- Документация: раздел в `docs/lan-access.md`, узел `LAN_PROXY` в графе навигации, `docs/features.md` (F02/F16).
- Статус: реализовано локально; Kotlin-компиляция и эмуляторы только в CI, физическая приёмка (1080p, большие шрифты, смена сети) — за владельцем.

## Цикл 1.7.0: авто-переключение профиля и диалог выбора — 2026-10-26

- PR #25 (концепт), #26 (pure-политика `TvAutoFailoverPolicy` + 12 JVM-тестов), #27 (настройка `TvAutoFailoverPreferences`, плитка, граф навигации) и #28 (`maybeAutoFailover`/`selectBestByPing` в runtime) смержены в master; head — **`36070b8`**.
- Опубликован rolling RC **`v1.7.0-rc.164`**: run [40260158319](https://github.com/LeonidYasin/TunXBox/actions/runs/40260158319) SUCCESS, тег `v1.7.0-rc` → `36070b85`, versionCode `49000164`, `apk-manifest.json` с `signatureVerified=true` и `upgradeCompatibleWithPrevious=true`; stable не тронут. Владелец установил RC на устройство; физическая приёмка авто-переключения ещё не завершена.
- Дефект, найденный владельцем на устройстве: в окне «Автопереключение при сбое» не было списка профилей — только поясняющий текст. Причина платформенная: в `AlertController` при заданном `setMessage()` `ListView` из `setMultiChoiceItems()` не добавляется в диалог; сообщение и список взаимоисключающи. Ветка `fix/tv-failover-dialog` (от `36070b8`) убирает `setMessage()`, оставляет чекбокс-список и переводит фокус пульта в список; неиспользуемая строка `tv_auto_failover_message` удалена из en/ru. Это исправление требует нового RC — rc.164 остаётся с дефектом.
- Отдельный docs-PR **#29** (`docs/lan-access.md`: обход LAN при включённом VPN) открыт 2026-10-26; первый прогон `validate-and-release` упал на флейке `EmulatorSmokeTest > aboutShowsBuildIdentitySupportAndFullLicense` (``Full license action must be reachable``, только API 36; API 35 — 19/19), упавшая джоба перезапущена. Изменение только в markdown, на код не влияет.

## Интеграция и стабильный 1.6.0 — 2026-10-24

- PR #17 (diagnostics) merged в `fix/tv-pointer-scroll-1.6` как `da2a67e`; PR #19 (artifact-only workflow) — как `e2ac19f`. Release notes обновлены под rc.162 (`911fbbf`); финальный CI PR #12 зелёный (android-fdroid/source-checks/validate-and-release, run 39844942254 и 39844942232).
- PR #12 squash-merged в master как публикационный SHA **`5a7a2d1`** (репозиторий запрещает merge commits). Итоговый tree совпадает с проверенным кандидатом.
- Published: [v1.6.0](https://github.com/LeonidYasin/TunXBox/releases/tag/v1.6.0), not draft/prerelease, from `5a7a2d1`; publication run [40101906755](https://github.com/LeonidYasin/TunXBox/actions/runs/40101906755) SUCCESS. Installed versionName **1.6.0**, versionCode **48 999 999**, flavour `oss`, channel `stable`, package `com.tunxbox.app`, signer `34764328dd73482587e64d56008a9ef7b860069087e279cb984f7b2e5055b5ef`, `upgradeCompatibleWithPrevious=true`. Три APK (arm64-v8a, armeabi-v7a, universal) независимо скачаны и проверены по SHA256/size из `SHA256SUMS.txt` и `apk-manifest.json` — ALL_MATCH. Stable 1.5.0 не изменён.
- Следующие шаги: bump `VERSION_CODE` base 49 для следующих RC; продолжение фич отдельными PR; F-Droid остаётся закрытым до разрешения лицензионных gate (см. `fdroid/README.md`). Открытые #6/#10 — мониторинг, не закрыты.

## Начало цикла 1.7.0 — 2026-10-24

После публикации stable 1.6.0 начат следующий minor-цикл на ветке `release/1.7.0-cycle` (от master `5ac4636`): `nb4a.properties` → `VERSION_NAME=1.7.0`, `PRE_VERSION_NAME=pre-1.7.0-lan-rc`, `VERSION_CODE` base **48 → 49**. Добавлен материал `docs/releases/1.7.0.md` (ожидаемый stable versionCode **49 999 999**), чтобы `validate_stable_inputs` проходил, пока 1.7.0 ещё не выпущен. Entry points `AGENTS.md`, `roadmap.md` (факт выпуска 1.6.0, base-правила), `development.md` обновлены для цикла 1.7. Реальное содержимое фич 1.7.0 определяется последующими PR; green CI ≠ публикация. Открытый вопрос — merge-метод PR: squash запрещён владельцем, использовать `merge`/`rebase`.

## Публичная витрина: stable 1.6.0 на README и сайте — 2026-10-24

Найдено расхождение: после публикации stable 1.6.0 README и сайт всё ещё указывали stable 1.5.0 и номерной кандидат rc.158. Причина — при публикации обновлялись только agent-facing docs (PR #21 → `agent-handoff.md`; PR #22 → `AGENTS.md`/`roadmap.md`/`development.md`/`releases/1.7.0.md`), а `README.md` не трогался с PR #12. Публичная витрина дополнительно «законсервирована» regression-тестом: `project_site_seo.py` `STABLE='1.5.0'`, карточки каналов в `build_project_site.py`/`site/english.html`, проверки в `test_project_site.py`.

Ветка `fix/public-stable-1.6.0` от master (`24ac095`, уже с мержем #23):

- `README.md`: stable **1.6.0** (link `v1.6.0`), предыдущая 1.5.0 сохранена; тестовый канал `v1.6.0-rc` (rolling, rc.162); раздел «Следующий цикл 1.7.0» с кандидатом #23; строка «Продолжить разработку» и статус-таблица обновлены.
- `buildScript/build_project_site.py`, `buildScript/site/english.html`: карточки канала stable 1.6.0 / rolling 1.6.0-rc; `buildScript/project_site_seo.py` `STABLE='1.6.0'`; `buildScript/test_project_site.py` обновлён (`assert` `v1.6.0`/`v1.6.0-rc`/`v1.5.0`, `softwareVersion` `1.6.0`).
- `docs/github-pages.md`: описание ожидаемых ссылок обновлено.
- `website/` перегенерирован (`build_project_site.py --output website`) — прежде это был неполный устаревший снимок (37 файлов, без `en/`, `sitemap.xml`, `robots.txt`, `SEO.md` и без `releases/1.6.0.html`).
- Локально: `check_project_docs.py` → 863 files / 1193 links; `test_project_site.py` → 7 tests OK. Это правка публичной витрины, не новый APK и не релиз; live Pages обновляется отдельным deploy в `gh-pages`.

## Перенос подписки app→app и обход VPN — 2026-10-24

Ветка `feature/tv-transfer-subscription-vpn` (PR **#23**, base — **master**, после мержа #22). Code-коммит **`9f0b4c7`** (tree `2c0b6fc9…`), docs-evidence `0e731b5`; ветка реbase-нута на master `06b1659` (tree master == base `e91c237`, поэтому rebase без конфликтов и tree сохранён). Локально нет Java/Android SDK — Kotlin-компиляция и JVM/эмуляторные тесты выполнены в CI; это не собранный APK и не опубликованный RC.

- Причина №1 (сценарий «передачи подписки»): нативный app→app переносил только статический снимок группы; `TvGroupTransfer.exportGroup` не отдавал URL подписки, а `importGroup` всегда создавал `GroupType.BASIC`. Теперь `TransferSelection.subscriptionUrl(type, link)` (новый, pure) решает переносить группу-подписку как обновляемую HTTP(S)-ссылку; `TransferExport` получил `subscriptionUrl`; `TvTransferServer.handleExport` возвращает `subscription_url` без `profiles`; `TvTransferClient` отправляет `subscription_url` (push) и импортирует подписку (pull). Границы в `docs/tv-transfer.md`/`tv-readiness-plan.md`, которые гласили «не переносим subscription credentials», изменены осознанно по решению владельца; согласовано с браузерным флоу.
- Причина №2 (VPN на смартфоне): клиент-сканер ходил `URL.openConnection()`, а маршрут `0.0.0.0/0` VPN (`VpnService.kt:120`) уводил LAN-запрос в туннель. Новый `LanTransferConnection.openLanConnection(url)` (API ≥ 23) выбирает физическую Wi-Fi/Ethernet-сеть (NOT_VPN), при необходимости по подсети целевого IP, и открывает соединение через `Network.openConnection`; fallback — обычное соединение. Сервер не менялся (уже биндится к LAN-IP).
- Тесты (JVM): новый `TransferSubscriptionTest` (pure: выбор URL, push/pull body) и кейс в `TransferImportServerTest` (экспорт `subscription_url` без `profiles`, импорт запрещён). Строки `tv_strings.xml` (en+ru) обновлены. Docs: `tv-transfer.md`, `security.md`, `tv-readiness-plan.md`, `scenarios.md`, `features.md`; inventory `docs/reference/repository-index.md` → 850 files / 1183 links.
- CI на tree `2c0b6fc9…` (исходно на `eb0a6ae`, до rebase на master; после rebase tree идентичен): [source-readiness 40115494878](https://github.com/LeonidYasin/TunXBox/actions/runs/40115494878) — source-checks PASS (1m55s), android-fdroid PASS (18m21s: `assembleFdroidRelease` + `testFdroidDebugUnitTest` + эмуляторные 4KB/16KB gates); [stable-release 40115494843](https://github.com/LeonidYasin/TunXBox/actions/runs/40115494843) — validate-and-release PASS (17m39s: `assembleOssDebug` + `testOssDebugUnitTest` + эмуляторный smoke, включая live LAN transfer round-trip). Новые кейсы реально исполнены: `subscriptionGroupExportsItsHttpUrl`, `nonSubscriptionGroupsAndInvalidLinksStayGroups`, `pushPrefersSubscriptionUrlOverGroupSnapshot`, `pullPrefersSubscriptionUrlOverSnapshot`, `exportServerProvidesSubscriptionUrlWithoutSnapshot`.
- Следующий шаг: CI на rebased head PR #23, затем физическая приёмка двух устройств (smartphone с VPN → TV). #22 смержен в master `06b1659`.

## Подготовка подписанного кандидата для устройств — 2026-10-21

Восстановление прошлого чата и проверка PR17 сохранены в [контрольной точке PR18](https://github.com/LeonidYasin/TunXBox/pull/18#issuecomment-6318370505): actual XML76/76, четыре EN portraitQR PNG отдельно просмотрены, head/merge tree equality подтверждена. Это не физическая TV/camera/RU/large-font приёмка. PR12 ba614dde всё ещё имеет красный Android15 run; не подменять его conclusion зелёным PR17. Пользователь разрешил продолжать подготовку; stable не публиковать автоматически.

Отдельная ветка `build/diagnostics-device-candidate` от bfde4b48 содержит workflow-only режим `artifact_only` плюс tests/docs, без runtime изменений. [Как запустить и принять](signing-and-updates.md): artifact_only=true/check_only=false/recover_rc158=false/allow_legacy_signer_migration=false/ref пуст. Полные Preview JVM/эмуляторы перед pinned signing, затем verify actual metadata/signature/alignment и upgrade manifest от rc158. Только Actions artifact; публичные релизы/теги не меняются. Локально100 Python и15 browser-transfer PASS, включая6 новых guards, workflow YAML parse PASS; это не Android CI нового commit и не готовый APK. Статус run/artifacts проверять live, не обещать готовность заранее.

Устройства приёмки — смартфон и ТВ/приставка владельца; точные модели неизвестны. Если ТВ недоступен, выполнить phone acceptance и явно оставить TV/camera pending. Следующее действие — запустить artifact-only workflow на сохранённой ветке, проверить каждый gate и реальный manifest, передать владельцу подходящий APK без требования удалить приложение. Master/stable/PR12/PR17 автоматически не мержить.

## Новое решение владельца: диагностика и наблюдение

Владелец не видит явных проблем Hysteria2/TLS сейчас; возможное исправление не доказано. [Точное наблюдение](releases/connection-observation-2026-10-21.json). Согласованный путь — доработать понятность/UX/передачу отчёта TV→phone и ждать новых reports, а не требовать немедленного воспроизведения исчезнувшего симптома.

Работа ведётся отдельно в `fix/diagnostics-user-journey` от candidate `ba614dde`, не в master. [Аудит и acceptance](diagnostics-ux-review.md): localized summary/stages, partial60s report, Hy2 metadata, short offline text QR, LAN TTL/counter. Все изменения требуют собственных compilation/JVM/Android gates; нельзя приписывать им CI PR16. Новый APK/stable не опубликован. Физический QR scan/D-pad/font-scale acceptance остаётся после получения тестового APK. Issues6/10 оставить OPEN monitoring; новая воспроизводимая регрессия снова требует fix/scope decision.

## Актуальное состояние после PR15/PR16 — 2026-10-21

Этот раздел имеет приоритет над историческими снимками ниже. Проверенный candidate merge — `231cdfae7145f39d49c7613c7f93df5b841ef5ba`, tree `e566f7d59a0e5efa52314e5e7a7e46402a8ce884`; последующий docs-only handoff commit не является новым APK.

- PR15 merged только в `fix/tv-pointer-scroll-1.6`: `7173b22c2814ed76fe3cf981bbad252cb43afdd2`. Master `99a8b6f2e0ea17a8fd415d541965b71223708398` не изменён и является предком кандидата. Конфликты PR12 устранены; actual candidate tree совпал с проверенным sync merge-ref. [Проверка синхронизации](https://github.com/LeonidYasin/TunXBox/pull/15#issuecomment-6306089444).
- PR16 merged только в candidate как `231cdfae...`: ранний stable preflight, отсутствовавшие release notes и ссылки rc158. Runtime приложения не менялся. [Source/F-Droid 39528903583](https://github.com/LeonidYasin/TunXBox/actions/runs/39528903583) и [OSS/check-only 39528903613](https://github.com/LeonidYasin/TunXBox/actions/runs/39528903613) SUCCESS. XML проверены заново по всем именам: 4 × 19, 0 failure/error/skip. Проверенный merge-ref `16c2b8237f4058bdace8a9e15c1c312b60c5ddf1` и реальный merge имеют одинаковый tree, но разные SHA; это не exact-head CI последующего docs commit. [Машинное доказательство](releases/validation-pr16.json).
- Сайт реально опубликован из committed source `396fa16e7669cf35250b0d9aa0275c189972f159`: gh-pages `6bb4f8118c25ee7bf1a578360dd9958f4238cf4e`, [Pages 39528818070](https://github.com/LeonidYasin/TunXBox/actions/runs/39528818070) SUCCESS. RU/EN HTTP200 и ссылки проверены; manual preview rc158, stable JSON-LD1.5, ограничение rolling rc150 явно указано. Это не поисковая индексация и не app release.
- Переносимый архив ~180MB из artifact12315040710/run39528903583 передан владельцу в предыдущем чате вместе с SHA256. SHA256 архива `cc8478275426a9d1c4a862c2103a194428edcce7c0452cdb8efc6ce71e50e5c7`; snapshot application `16c2b823...`; все 29640 file hashes проверены. [Доказательства и границы](https://github.com/LeonidYasin/TunXBox/pull/16#issuecomment-6306013373). `allTransitiveSourcesArchived=false`, полный Git history/Maven/toolchains не включены, внешнее сохранение владельцем не подтверждено. Старые sandbox/session-file пути нельзя считать доступными новому агенту.
- Исходные #6/#10 остаются OPEN: это прежние physical reports, не свежая воспроизведённая регрессия rc158. Диагностика не равна исправлению. [Минимальная проверка и безопасная форма](connection-acceptance.md) разделяет сертификат подписки, прокси TLS/QUIC и URL-пробу.

Исторический следующий шаг до нового наблюдения владельца: проверить live refs/checks PR12; повторить конкретный проблемный узел при возвращении симптома, без публикации конфигурации. Нужны safe report и поведение обычного трафика, затем целевой regression fixture/fix либо согласованный documented deferral. Не закрывать #6/#10 по CI. Не сливать PR12/master и не выпускать stable автоматически. PR14 entrypoint остаётся отдельным review; после будущей интеграции проверить superseded status.

## Историческая подготовка до интеграции PR16

Ветка `prep/stable-release-inputs` основана на candidate `2d5b5554`. Это последующая подготовка, пока не интегрированная в основной candidate и не новый APK:

- Найден реальный будущий отказ stable: workflow читает `docs/releases/1.6.0.md`, которого не было. Добавлены содержательные notes без утверждения о состоявшемся выпуске и ранний `validate_stable_inputs.py`: canonical stable version/package/versionCode, доступный файл заметок с верным заголовком/телом, формат публичного signer SHA256. Проверка идёт до установки SDK; не создаёт тег/релиз и не читает private keys.
- Девять regression tests для preflight; ещё один site regression. Локально **94 Python PASS / 15 browser-transfer PASS**, docs **837 files / 224 prefs / 1155 links**. Это не Android CI нового commit.
- Live RU/EN пока предлагали старый rolling `v1.6.0-rc` (rc.150). Генератор/English source исправлены на опубликованный номерной rc.158; объяснена ручная загрузка и отсутствие его в нынешнем in-app test updater. JSON-LD stable остаётся 1.5.0. До отдельного Pages deploy/HTTP проверки не объявлять эти изменения live.
- Проверены реальные старые emulator screenshots: файл с именем portrait-scroll scenario фактически показывает Android launcher после завершения теста. Нельзя автоматически превращать все named test screenshots в Fastlane gallery или выдавать их за экран TunXBox. Принятые store screenshots остаются отдельным gate; mock/сайт не подставлять.
- На момент подготовки PR15 merge-result CI ещё шёл; base candidate не менялся, чтобы не сбить эти проверки. Следующие действия: закончить review/merge PR15 только после полного CI и проверки exact unchanged tree; применить эту ветку отдельным reviewed change поверх синхронизированного candidate; проверить актуальные workflows и отдельно опубликовать сайт. PR12→master/stable/F-Droid inclusion этим не разрешены.

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

1. Владелец сообщил: **смартфон обновился до rc.158 без проблем; портретная прокрутка предварительно работает нормально**. [Запись приёмки](releases/rc158-device-acceptance.json). Модель/Android/ABI/предыдущая версия не сообщены. Это положительная частичная приёмка, не подтверждение всех устройств/мыши/пульта/сохранности всех данных. Оставшаяся матрица **портретного TV на реальном смартфоне**: swipe/fling вверх/вниз, отпускание без snap-back, wheel/drag мыши, затем D-pad; обычные горизонтальные ряды, tap/long press и VPN/выбранный профиль не меняются сами. Приложить модель/Android/ABI/точную версию и наблюдение, без приватной конфигурации.
2. На TV: 720p/1080p, крупный шрифт, 1/3/10 групп, длинные имена, rename/update/delete/Back, импорт названий, возврат фокуса. Updater: реальная загрузка/докачка/installer permission/data preservation. Эмулятор не заменяет OEM installer.
3. **Завершено PR15:** исторический план разрешения 15 конфликтующих файлов ниже оставлен для аудита; не выполнять повторно. Ранее требовалось разрешить их отдельным проверяемым integration commit, не через blanket ours/theirs. `git merge-tree --write-tree --name-only HEAD origin/master` показал конфликты в release workflows, MainActivity/strings, regression/instrumentation tests и docs. В старом снимке PR #12 был `mergeable=false`; после PR15 конфликты устранены. Перепроверять live mergeability и checks. Сохранить оба набора сценариев и полные test-name gates, регенерировать docs inventory и заново собрать/протестировать итоговый merge tree. Затем посмотреть [все PR checks](https://github.com/LeonidYasin/TunXBox/pull/12/checks) и последние [Source readiness runs](https://github.com/LeonidYasin/TunXBox/actions/workflows/source-readiness.yml). Последний commit обязан иметь свои conclusions; предыдущий passed source job или cancelled run не равен full green.
4. Для #6/#10 выполнить безопасную одинаковую-профиль/сеть/устройство проверку и принять явное fix/defer решение. Не закрывать исходный report только за добавление диагностики.
5. После приёмки и final CI — review/integration PR #12 → master, затем отдельный stable release от **точного нового master SHA** с неизменной подписью/возрастающим versionCode. Numbered rc.158 не является разрешением слить/выпустить stable. Проверить contract `stable-release.yml`, включая source tag и текущий RC manifest; не обходить mismatch.
6. PR #7/#8/#9 закрывать как covered by integration только после фактического включения и принятия, не мержить повторно. PR #5 unique diff/remaining scope сопоставить с #6/#10/#13; его conflicting whole branch не вливать. См. [triage](release-triage.md).
7. [Эксперимент extended/Xray](core-extension-strategy.md) вести отдельно после фиксации stable scope. Поддержка XHTTP в 1.6 не заявляется.

## 4б. Завершённый CI и интеграция истории — 2026-10-21

- Оба exact-head run на `4adc13eca33087fff813226d6fc8b2de0579e4d9` завершились **SUCCESS**: [source/F-Droid 39522276619](https://github.com/LeonidYasin/TunXBox/actions/runs/39522276619), [stable check-only 39522282529](https://github.com/LeonidYasin/TunXBox/actions/runs/39522282529). Stable не опубликован. Старый stale-inventory failure исправлен, а не проигнорирован.
- Actual XML повторно скачаны и проверены строгим `verify_emulator_results.verify` по exact-source списку всех имён: **4 × 19**, ноль failures/errors/skips, OSS и F-Droid / Android15 4096 и Android16 16384. [Машинная запись](releases/validation-4adc13e.json) содержит run/artifact IDs и SHA256 ZIP. Это не тест будущего merge tree.
- Владелец подтвердил успешное обновление смартфона до rc.158 и предварительно исправную портретную прокрутку. Точная формулировка/границы сохранены отдельно; TV/mouse/D-pad/full acceptance автоматически не закрыты.
- Полная история показывает, что единственный master-only commit — LAN/gateway PR4 (`99a8b6f2`). LAN runtime уже был перенесён в candidate: после осмысленного разрешения 15 конфликтов diff приложения против `4adc13e` пуст; меняется только smoke test (5 строк возврата в TV после phone gateway fixture) и три docs-раздела. Сохранены диагностика, все 19 имён сценариев, API36/16KB AVD, строгий per-name verifier вместо старого count=10, REALITY guards и overlay hiding для LAN/About/Updater.
- Нельзя убрать конфликт истории обычным `push_files`: он создаёт один parent. Это проверено локально: даже resolved-content one-parent commit всё ещё конфликтует с master. Не утверждать обратное и не force-push.
- Подготовлен безопасный путь через отдельную **master-based** ветку `integration/master-sync-1.6`, где только 15 пересекающихся файлов приведены к уже проверенному current candidate. PR должен идти **в `fix/tv-pointer-scroll-1.6`, не в master**. До merge проверить `git merge-tree --write-tree` и точное совпадение объединённого tree с candidate, затем обязательные CI merge result. Это записывает master ancestry, не выпускает stable и не выбрасывает LAN-код. Не выдавать standalone head этой служебной ветки за полный runtime candidate: полный app tree только в PR merge result.
- После принятия sync PR повторно проверить PR12→master mergeable и exact integration tree. Следующий merge PR12 в master и stable publication — отдельные gates; PR14 docs entry остаётся отдельным review.

## 4а. Исторический снимок до завершения CI и точка входа master

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

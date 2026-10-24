# XHTTP / Xray и sing-box-extended: проверенная стратегия

Дата аудита 2026-10-20. **Статическое исследование**, не успешная компиляция extended в TunXBox и не runtime/throughput/TLS-security certification. Задача [#13](https://github.com/LeonidYasin/TunXBox/issues/13), отдельно от стабилизации 1.6. Исходники сторонних проектов не исполнялись и зависимости не менялись в release candidate.

## Вывод

Проверить extended как **первый одноядерный эксперимент разумно**, если главная цель — XHTTP. Но тезис «достаточно заменить ядро, существующая интеграция не меняется» для TunXBox **опровергается пакетами API**. Сохранение семейства LibBox не означает идентичности custom Go/JNI/Android/JSON контрактов. Extended — другой движок с похожим происхождением, не «все протоколы Xray» и не гарантированная совместимость с каждым сервером/Happ.

## Зафиксированные источники

| Проект | Revision | SHA256 codeload source archive |
|---|---|---|
| [shtorm-7/sing-box-extended](https://github.com/shtorm-7/sing-box-extended/tree/d9442d14b19ddd02605bf3ee893a171947038e19) | d9442d14b19ddd02605bf3ee893a171947038e19 | f3edb9fc658efd325b551eeaae98e4574b88179f7121aed9b18736a2c3bb11b3 |
| [starifly/NekoBoxForAndroid](https://github.com/starifly/NekoBoxForAndroid/tree/a4c5072040329a68cc139d8823d016f8aeba6094) | a4c5072040329a68cc139d8823d016f8aeba6094 | 2c53db23e6807b9c7cc3c9165a5a9bcf010ed8b8efb38970774a1cc7222fe04c |

README extended действительно перечисляет WARP/MASQUE/MTProxy/Mieru/TrustTunnel/Sudoku/SSH, Amnezia3.1/VLESS encryption, Bond/Fallback/Failover и XHTTP. Наличие в README не доказывает Android outbound для каждого пункта, сохранность сессии, производительность или «неотличимость от HTTPS». В частности MTProxy описан как **server** — это не новый Android client protocol. Функции server/admin/manager/AI/provider не следует автоматически включать в клиент.

### XHTTP: подтверждено кодом

[option/v2ray_transport.go](https://github.com/shtorm-7/sing-box-extended/blob/d9442d14b19ddd02605bf3ee893a171947038e19/option/v2ray_transport.go) содержит `V2RayXHTTPOptions`, mode, host/path/headers, download и advanced поля `session_placement`, `uplink_data_placement`, `x_padding_*`, `xmux`. [transport/v2rayxhttp](https://github.com/shtorm-7/sing-box-extended/tree/d9442d14b19ddd02605bf3ee893a171947038e19/transport/v2rayxhttp) существует, как и [examples/xhttp](https://github.com/shtorm-7/sing-box-extended/tree/d9442d14b19ddd02605bf3ee893a171947038e19/examples/xhttp). Это больше, чем только UI/parser; совместимость конкретных комбинаций mode/TLS/REALITY/extra всё равно требует fixtures.

### Почему не drop-in

Наш [libcore/box.go](../libcore/box.go) импортирует `github.com/sagernet/sing-box/boxapi` и `experimental/libbox/platform`; [platform_box.go](../libcore/platform_box.go) реализует этот custom platform Interface. В extended snapshot **обоих пакетов нет**. Вместо них есть другой [experimental/libbox/platform.go](https://github.com/shtorm-7/sing-box-extended/blob/d9442d14b19ddd02605bf3ee893a171947038e19/experimental/libbox/platform.go) с собственным PlatformInterface и типами. Само совпадение module path `github.com/sagernet/sing-box` в go.mod не создаёт недостающие пакеты. Direct swap уже имеет статически доказанный API blocker; successful build здесь не заявляется.

Нужно явно адаптировать `BoxInstance`, routing/selector/v2ray stats API, platform/tun/protect/DNS interfaces, Android callbacks и lifecycle. Плюс Kotlin `SingBoxOptions`, ConfigBuilder, URI/editor/export/Room mapping и диагностические тесты; иначе новые поля будут потеряны ещё до ядра. Текущий Matsuri core и verified REALITY hybrid patch нельзя выбросить без сравнительных strict-auth tests.

### Что реально использует NekoBox Starifly

[Source checkout script](https://github.com/starifly/NekoBoxForAndroid/blob/a4c5072040329a68cc139d8823d016f8aeba6094/buildScript/lib/core/get_source.sh) клонирует **starifly/sing-box** и **starifly/libneko**, а [pins](https://github.com/starifly/NekoBoxForAndroid/blob/a4c5072040329a68cc139d8823d016f8aeba6094/buildScript/lib/core/get_source_env.sh) задаёт `0b7a2fc2f2c33f754e5fe5a1bb3e0a4f610b6aa9` и `6a85c185d62435a5293ef70ac3b638ae3ee1efa7` соответственно. Это **не доказательство прямого использования shtorm-7 extended**. Не заявляем отсутствие/наличие отдельных cherry-picks без дополнительного genealogy audit.

[V2RayFmt.kt](https://github.com/starifly/NekoBoxForAndroid/blob/a4c5072040329a68cc139d8823d016f8aeba6094/app/src/main/java/io/nekohasekai/sagernet/fmt/v2ray/V2RayFmt.kt) действительно импортирует/export-ит XHTTP extra и строит XHTTPOptions. Значит полезен как reference адаптации NekoBox-family, но копирование только строки зависимости не переносит его parser/beans/converter/editor/contracts. Аудит GPL/авторства и изменённых libneko обязателен перед заимствованием.

### Naive и toolchain

[Naive outbound](https://github.com/shtorm-7/sing-box-extended/blob/d9442d14b19ddd02605bf3ee893a171947038e19/protocol/naive/outbound.go) закрыт build tag `with_naive_outbound` и импортирует `github.com/sagernet/cronet-go` + `/all`. Поэтому «native = без дополнительных зависимостей и F-Droid проблем» неверно. Надо проверить source-build Cronet/native assets/licenses/ABIs, а не включать готовый бинарник. [go.mod](https://github.com/shtorm-7/sing-box-extended/blob/d9442d14b19ddd02605bf3ee893a171947038e19/go.mod) требует Go1.26.4; наш pinned tool Go1.26.8 выше этого минимального значения, но полная совместимость Android/NDK/старых устройств ещё не проверена.

## Три варианта — критерии решения

| Подход | Потенциальная польза | Реальная цена / доказательства перед выбором |
|---|---|---|
| Extended как один core | Готовый XHTTP в одном Go runtime, меньше inter-core orchestration | Порт custom adapter/schema; полная регрессия уже работающих протоколов; source/licenses всех новых deps; не считать размер/потребление заранее меньше |
| Узкий XHTTP backport / Starifly-family reference | Возможно сохранить больше Matsuri контрактов | Нельзя просто переносить transport package между поколениями API; audit зависимостей, protocol history, auth/security fixes и объёма сопровождения |
| Второе pinned Xray ядро | Прямой reference transport/crypto implementation Xray и иной compatibility envelope | Свой in-process libXray adapter или строго ограниченный native process; один VpnService/TUN owner, bridge/protect/bind/DNS/UDP/stats/lifecycle, больше упаковки/maintenance; преимущества измерять |

Не выбирать архитектуру по числу пунктов README. Сравнивать конкретную нужную матрицу protocol × transport × security × flow × extra × Android ABI. Существующий sing-box остаётся baseline до доказанной эквивалентности.

## Исполнимый план эксперимента (после stable scope)

1. От accepted 1.6 исходников создать отдельную ветку, например `experiment/extended-xhttp`; не менять stable lock/пакет/подпись. Пинить точный кандидат SHA+архив и все источники, не `latest`/плавающий extended. Предварительно проверить patch/auth/license/provenance.
2. Составить mechanical contract inventory всех импортов нашего libcore и Kotlin options; compile-only spike с отдельным build-time вариантом и существующим baseline. Первым отчётом должен быть конкретный список API errors/адаптаций, не тестовый APK с silent feature removal.
3. Портировать TUN/VpnService/socket protect/physical network binding, DNS/routing/selector, URL tests/counters/lifecycle/cancel. Не запускать второй VPN, не открывать manager/admin/shell ports и не исполнять команды из подписок. Не делать runtime download ядра.
4. Lossless fixtures URI/JSON → bean/Room → editor → export → native config: XHTTP aliases/modes/path/host/headers, upload/download options, session/uplink placement/padding/xmux и ограничения текущей версии. Unsupported параметры отклонять явно; не XHTTP→TCP/HTTPUpgrade. Не давать extra переписать arbitrary outbound/commands/listeners.
5. Controlled Xray fixtures: packet-up/stream-up/stream-one, split download, TLS и REALITY/Vision там, где комбинация поддержана; strict valid/invalid cert/SNI/shortID/publickey, cancellation/timeout, no-direct-fallback. Клиент/сервер versions и ожидаемая матрица в отчёте. Не использовать приватные серверы пользователя в CI.
6. Сравнить retained TCP REALITY, Hysteria2/QUIC/UDP/DNS, subscriptions/groups/QR/dashboard/updater, cold start/network changes/screen off/restart. Все существующие JVM + per-test-name 4KB/16KB gates, все четыре ABI и actual APK signing/version/native alignment. Реальные ARM32/ARM64 Phone/TV обязательно; Go compile не сертифицирует старый Android.
7. Измерить APK/RAM/battery/startup/throughput в одинаковых fixtures. Проверить advisories/maintenance cadence обоих upstreams, лицензии новых native/Go/npm/data inputs и F-Droid provisioning. Зафиксировать ADR choose/reject с результатами, сохранить baseline/план возврата (Android downgrade с меньшим code не считать безопасным rollback).

## Что не делать в 1.6

Не подменять ядро целиком перед выпуском scroll fix; не обещать полный Xray parity, repair Hysteria2 или невидимость DPI. Сначала принять текущие исправления и разрешить реальные reports #6/#10. Новый core эксперимент может идти параллельно только в изолированной ветке, не переопределяя проверенный RC или права публикации.

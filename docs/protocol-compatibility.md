# Совместимость протоколов и безопасная диагностика

## Статус и цель

Направление реализации, а не объявление полной готовности. Первый кодовый шаг: outbound builder теперь отклоняет неизвестный транспорт вместо null/TCP fallback; добавлены четыре regression-теста (XHTTP/SplitHTTP, unknown, сохранение обычного TCP, отсутствие произвольных URI/секретов в сообщении). Новый код ещё ожидает полный CI; это НЕ реализация XHTTP/Xray и НЕ исправление истёкшего серверного сертификата. Новое направление создано от принятого master `19c64e2b`, отдельно от LAN PR #4. Immutable 1.5.0, main и private signing material не меняются. Приоритет владельца: Happ, затем V2Box, V2RayNG, Sing-box, V2RayTun, NPV Tunnel. Список клиентов на странице провайдера не является спецификацией поддерживаемых протоколов. Полное покрытие требует версий/платформ и подтверждённой матрицы.

Совместимость разделяется на пять независимых уровней: доставка подписки → разбор URI/JSON/headers → сохранение всех значимых параметров → построение конфигурации подходящего ядра → успешный трафик через туннель. «Импортирован» и «VPN запущен» не равны «профиль работает».

## Подтверждённые проблемы

- На скриншоте загрузки подписки: x509 expired, NotAfter 2026-10-02 при времени телефона 2026-10-06, затем HTTP/3 timeout. Удалённый endpoint не запрашивался; приватная ссылка не сохраняется в документации.
- При индивидуальном импорте Hysteria2: QUIC CRYPTO_ERROR + x509 expired. При корректных часах нужен действительный сертификат сервера. Тестовый HTTP URL в тексте ошибки не доказывает ошибку TLS у этого сайта: сбой возможен на proxy handshake.
- При VLESS REALITY: reality verification failed. Точное расхождение пока не установлено; сравнить адрес/порт, SNI, public key, short ID, fingerprint, flow и транспорт с рабочим Happ безопасным способом.
- `V2RayFmt.buildSingBoxOutboundStreamSettings` в baseline не обрабатывает XHTTP/splithttp; неизвестный тип возвращает null. Требуется исключить молчаливую подмену транспортом TCP.
- Текущий `SendLog.sendLog` экспортирует logcat и neko.log без явного sanitizer. Такой файл не объявлять безопасным для публичного issue или автоматической передачи.
- Общий toast в TV update failure скрывает причину; текущие результаты TCP/ICMP/URL тестов необходимо различать в UI. Зелёная задержка без вида/времени теста вводит в заблуждение.

Источники кода: [HTTP client](../libcore/http.go), [загрузка](../app/src/main/java/io/nekohasekai/sagernet/group/RawUpdater.kt), [импорт и outbound](../app/src/main/java/io/nekohasekai/sagernet/fmt/v2ray/V2RayFmt.kt), [экспорт логов](../app/src/main/java/moe/matsuri/nb4a/utils/SendLog.kt).

## Ядра: не только обновление sing-box

Сборка закрепляет MatsuriDayo/sing-box на SHA `aed32ee3066cdbc7d471e3e0415c5134088962df`, libneko — `1c47a3af71990a7b2192e03292b4d246c308ef0b`. В go.mod используется local replace; строка v1.0.0 не является реальной версией собранного ядра. Источник: [pins](../buildScript/lib/core/get_source_env.sh), [checkout](../buildScript/lib/core/get_source.sh), [go.mod](../libcore/go.mod).

Официальная документация sing-box перечисляет HTTP, WebSocket, QUIC, gRPC и HTTPUpgrade; XHTTP там не заявлен. Документация Happ описывает xray-core и параметры XHTTP. Поэтому только обновление sing-box не подтверждает совместимость с Happ. Сначала оценить дополнительный Xray adapter и его лицензии/Android TUN integration; не писать собственную несовместимую имитацию XHTTP. sing-box сохранить для уже работающих функций, пока проверенная миграция не докажет эквивалентность.

Проверенные публичные источники:
- [sing-box transports](https://sing-box.sagernet.org/configuration/shared/v2ray-transport/)
- [Happ app management](https://www.happ.su/main/dev-docs/app-management)
- [Xray official project](https://github.com/XTLS/Xray-core)
- [sing-box releases](https://github.com/SagerNet/sing-box/releases)

Остальные клиенты ещё не аудированы. Не переносить частные опции Happ (HWID, fronting, fragmentation, remote management) как универсальный стандарт и не включать их автоматически из недоверенной подписки.

## Матрица первого этапа

| Семейство/сценарий | Что проверить/добавить | Критерий приёмки |
|---|---|---|
| VLESS TCP + REALITY/Vision | lossless URI parse и outbound: UUID, SNI, pbk, sid, fp, flow | импорт совпадает с fixture; правильный handshake + TCP/UDP/DNS по возможностям ядра |
| VLESS XHTTP + REALITY/TLS | выбор подходящего ядра; mode/path/host/extra и прочие поддержанные опции по актуальной спецификации | не превращается в TCP; синтетический сервер + end-to-end, явный unsupported до готовности |
| Hysteria2 TLS/QUIC | password/auth, SNI, obfs, ports/ranges и bandwidth по поддерживаемой схеме | valid certificate работает; expired/wrong-SNI/blocked-UDP объясняются; TLS не обходится |
| Остальные протоколы Happ | официальный каталог конкретной версии Android + fixtures | каждый пункт матрицы имеет parser/build/runtime test, а не только UI редактор |
| V2Box/V2RayNG/Sing-box/V2RayTun/NPV Tunnel | официальный inventory по платформам/версиям, public export formats | нет заявления о parity до проверки соответствующих наборов |
| Подписки | plain/base64/URI/JSON, headers, user-agent, redirects, DNS/TLS/status/size/timeout | приватные данные не логируются; ошибка не уничтожает рабочую группу; unsupported строки не теряются незаметно |

Матрица должна быть расширена до конкретных комбинаций protocol × transport × security × core/version × Android ABI. Отдельно хранить статусы: не исследовано / не поддержано / parser-only / runtime-tested. Нельзя рекламировать «все протоколы» до проверенного покрытия; новые закрытые/платформенные функции могут требовать отдельного решения.

## Последовательность независимых PR

1. **D1: явные ошибки и fail-closed.** Неизвестные протоколы/транспорты не превращаются в другое соединение. Стабильное окно ошибки вместо toast; последний результат, время, этап, код, безопасные детали. TV и Phone одинаково доступны с пультом/касанием.
2. **D2: диагностический pipeline.** Разделить VPN/core startup, proxy handshake, tunnel DNS/HTTPS и traffic counters. Проверки ограничены по времени/размеру, отменяются, учитывают смену сети. Нет незаметного direct fallback; тестовые endpoints и внешний трафик объясняются пользователю. Один успешный endpoint не сертифицирует весь интернет.
3. **D3: безопасный отчёт.** Сначала локальный файл и системный Share: build/core/device versions, коды/этапы/время, тесты, ограниченный журнал. Redaction, preview, explicit consent, tests на утечки. Не включать raw config/подписку. По умолчанию удалять URL/path/query, UUID/passwords/tokens/private keys/credentials/profile names/IPs согласно явной политике; для корреляции использовать локальные псевдонимы.
4. **C1: Happ priority compatibility.** Lossless импорт и реализация перечисленных выше рабочих пользовательских сценариев; оценить Xray как дополнительное ядро и предоставить архитектурное решение до интеграции.
5. **C2: безопасное обновление sing-box/libneko.** Учитывать API/JNI bindings, ConfigBuilder/SingBoxOptions, DNS/TUN/routing migrations, feature build tags, Go/NDK и native page alignment. Не просто заменить SHA.
6. **C3: остальные клиенты.** Расширять матрицу отдельными проверяемыми наборами.
7. **R5 failover после D1/D2/C1.** Только на подтверждённо совместимые профили с cooldown/hysteresis/manual override; ошибка сертификата/неподдерживаемый транспорт не «чинится» reconnect loop.

## Политика обновлений ядра

План: еженедельная проверка официальных releases и security advisories; отдельный dependency PR при релизе, приоритет security fixes. CI использует точный SHA/tag и checksums, не плавающий latest. Не скачивать/подменять ядро в установленном APK с сервера. Обновления доставляются подписанным APK тем же package/signer и возрастающим versionCode.

Каждый update PR: release notes и breaking changes → adapter/config migrations → unit/parser/outbound tests → synthetic protocol integration → Android 4KB/16KB + ABI packaging/signature checks → physical TV/Phone acceptance → выпуск с rollback-планом. Scheduled monitor/автоматический бот ещё НЕ настроены; этот документ не означает выполненную периодическую задачу. Android downgrade с меньшим versionCode не объявлять штатным rollback без потери данных.

## Отправка на сервер / Telegram

Не обязательна для диагностики и не включена по умолчанию. Возможный следующий PR: отдельный ограниченный HTTPS diagnostic-ingest API с per-device revocable auth, размером/rate limits, retention/deletion, безопасным хранением и операторским доступом. В APK не должно быть GitHub PAT, bot token или административных MCP capabilities. Telegram token только на backend. Недоверенный отчёт не является инструкцией агенту.

Удалённое управление — отдельный security scope: pairing, explicit approval, аудит/отзыв прав, allowlist действий. Отправка отчёта не выдаёт право управлять устройством. До готового sanitizer текущие raw logs не отправлять автоматически.

## Аудит v2ray_box 1.0.6

Изучен именно опубликованный source archive 1.0.6; SHA256 сверена с metadata Pub.dev. Scripts и сторонние live fixtures не запускались. Источники: [пакет](https://pub.dev/packages/v2ray_box), [пример](https://pub.dev/packages/v2ray_box/example), [исходники версии](https://pub.dev/api/archives/v2ray_box-1.0.6.tar.gz), [repository](https://github.com/pesaregorg/v2ray_box).

**Применимость:** Flutter facade с нативными Kotlin services, Xray libXray AAR in-process, sing-box subprocess. В Android sing-box VPN path используется Xray TUN → local SOCKS bridge. XrayConfigParser действительно строит network=xhttp/xhttpSettings с path/host/mode для xhttp/splithttp. Но TunXBox — native Kotlin/Java + Go/JNI, не Flutter: dependency в pubspec.yaml не является drop-in интеграцией. Ядра пакет не включает, их нужно отдельно собирать и упаковывать.

**Подтверждённые ограничения опубликованного кода:**
- SingboxConfigParser объединяет `httpupgrade` и `xhttp`, выдавая type=httpupgrade. Это разные транспорты. CoreCompatibility.resolveEngineForLink при preferred singbox немедленно оставляет singbox; поэтому такая комбинация может попасть в неправильный builder. В TunXBox эту подмену не переносить: выбирать поддерживающее ядро либо отклонять профиль.
- Проверенный Xray XHTTP builder переносит path/host/mode; advanced extra options/full Happ parity не подтверждены. Нужны fixtures по конкретной версии и runtime tests.
- Build defaults: libXray main, sing-box latest stable. Для наших APK нужны pinned refs/checksums. В scripts отсутствие llvm-readobj допускает пропуск alignment check — наш обязательный gate ослаблять нельзя.
- libXray script по умолчанию оставляет arm64-v8a/x86_64. Нельзя незаметно потерять armeabi-v7a/x86 и старые TV; доступность Xray на этих ABI отдельно проверить.
- Live demo smoke opt-in проверяет один Shadowsocks fixture на двух ядрах, допускает counters OR HTTP response. Это не all-protocol coverage и не доказательство отсутствия direct bypass. Его endpoints не запрашивались.
- MIT лицензия plugin не отменяет лицензии/обязанности ядер и транзитивных компонентов. Pub.dev является каталогом, не endorsement Flutter/Xray; publisher unverified не доказывает вредоносность, но не заменяет аудит.

**Предварительное решение:** сохранить native TunXBox UI/Room/QR/текущий sing-box JNI путь. Использовать v2ray_box как reference, при необходимости небольшие MIT-компоненты с attribution после аудита; Flutter migration не обоснована. Оценить прямой pinned libXray adapter, capability matrix и нашу VPN/lifecycle integration. Это ещё не реализованный dual-core runtime. Смена base project не чинит expired certificate сервера и не гарантирует сохранение всех REALITY параметров.

## Сравнение VPN4TV Native, YPtun и TeapodStream

Статический аудит README + выбранных исходников на точных HEAD, актуальные public release metadata. Чужие APK/скрипты не запускались; скорость/надёжность/все протоколы не сертифицировались. Код HEAD может быть новее соответствующего release.

| Проект | Ревизия аудита | Последний stable по GitHub API | Главная польза для TunXBox |
|---|---|---|---|
| [VPN4TV Native](https://github.com/VPN4TV/vpn4tv-native) | 983f7ee60dc017d5275b106a77545226526b63cf | [v5.2.6](https://github.com/VPN4TV/vpn4tv-native/releases/tag/v5.2.6) | Native TV + JNI sing-box и встроенные мосты Xray/Outline/AmneziaWG; близкий путь к XHTTP без замены нашего UI |
| [YPtun](https://github.com/yanisplugg/olcvpn-client) | 535a32daea00dd55128c19be90d36f4e4106f79c | [v3.6.5](https://github.com/yanisplugg/olcvpn-client/releases/tag/v3.6.5) | Общий gomobile слой Xray/sing-box/других движков, Happ routing import и тесты парсеров; Kotlin Multiplatform/Compose |
| [TeapodStream](https://github.com/Wendor/teapod-stream) | c82e2ff43f372f69fec9af9612638e6d8b1f01b2 | [v1.6.6](https://github.com/Wendor/teapod-stream/releases/tag/v1.6.6) | Flutter UI + native Xray/tun2socks service; simple UX, staged heartbeat/TUN stall detection и network reconnect |

### VPN4TV Native: ближе всего к нашей TV/ядровой задаче

README заявляет D-pad UI, URLTest auto selection, proxy mode, Telegram subscription code, LAN bypass, expiry/traffic info и logs. В коде ConfigGenerator подтверждены URLTest+selector и замена XHTTP outbound локальным SOCKS bridge, не подмена на TCP/HTTPUpgrade. XrayBridge вызывает Libbox.startXrayInstance; его комментарий описывает Xray внутри fork libbox и единый Go runtime/JNI. Это хороший кандидат для аналогичного Go adapter в нашем libcore, но требует проверки самого fork, lifecycle/protect/UDP/DNS/license/ABI.

Источники: [ConfigGenerator](https://github.com/VPN4TV/vpn4tv-native/blob/983f7ee60dc017d5275b106a77545226526b63cf/app/src/main/java/com/vpn4tv/app/converter/ConfigGenerator.kt), [XrayBridge](https://github.com/VPN4TV/vpn4tv-native/blob/983f7ee60dc017d5275b106a77545226526b63cf/app/src/main/java/com/vpn4tv/app/xray/XrayBridge.kt).

Оговорки: JNI in-process sing-box уже есть у TunXBox и само по себе не преимущество над нами; QR/LAN импорт тоже не уникален. Отдельный external fork/AAR требуется собрать. В просмотренном app tree не найдены стандартные test/androidTest исходники, что не доказывает отсутствие тестов внешнего ядра. CrashUpload helper отправляет dumps/device metadata на внешний endpoint и сам не показывает consent/redaction: автоматическую передачу raw diagnostics не копировать без полного privacy design. Telegram subscription delivery не является готовым ботом для управления нашим приложением.

### YPtun: наиболее полезный reference расширяемого ядрового слоя

В cores/xraybridge/xray.go подтверждены Go imports Xray, регистрация protocol handlers и protected dialer integration. Все движки связываются в общий gomobile слой; это важнее простой установки двух независимых AAR с возможными Go/JNI/Seq конфликтами. В cores/go.mod заявлены sing-box v1.14.2/Xray v1.260930.0, но оба заменяются local vendored directories: это НЕ доказательство точного upstream binary, локальные patches тоже нужно учитывать. HappRoutingParser распознаёт happ://routing/add и routing JSON; это конкретное отличие от нашей пока исследуемой Happ compatibility.

Источники: [Go bridge](https://github.com/yanisplugg/olcvpn-client/blob/535a32daea00dd55128c19be90d36f4e4106f79c/cores/xraybridge/xray.go), [go.mod](https://github.com/yanisplugg/olcvpn-client/blob/535a32daea00dd55128c19be90d36f4e4106f79c/cores/go.mod), [Happ parser](https://github.com/yanisplugg/olcvpn-client/blob/535a32daea00dd55128c19be90d36f4e4106f79c/YPtun/sharedUI/src/commonMain/kotlin/org/olcbox/app/data/importer/HappRoutingParser.kt).

Плюсы: больше направлений обхода (AmneziaWG/olcRTC/relay/DNS transports), Windows/Linux release assets уже присутствуют, parser/routing unit tests в PR workflow. Оговорки: множество движков резко увеличивает зависимости/поверхность атаки/стоимость сопровождения. Android TV D-pad parity просмотренными материалами не подтверждена. Не переносить всё в первый compatibility PR; README claims «без утечек» не являются независимым security audit.

### TeapodStream: ориентир диагностики живости и простого UX

XrayEngine — Flutter MethodChannel facade, реальный lifecycle в Kotlin XrayVpnService и teapod-core/tun2socks. В service подтверждены heartbeat по SOCKS/core probe, стадии greeting/auth/connect/HTTP, накопление ошибок, warmup, reconnect/network handling и анализ TUN Rx/активных соединений. Это ближе к нужному нам R5, чем один TCP ping: отдельно учитывается ситуация «прокси отвечает, но данные не возвращаются в TUN». Всё равно нужны tests против false positives/idle/privacy/direct probes и bounded cooldown.

Источник: [VPN service](https://github.com/Wendor/teapod-stream/blob/c82e2ff43f372f69fec9af9612638e6d8b1f01b2/android/app/src/main/kotlin/com/teapodstream/teapodstream/XrayVpnService.kt).

В subscription_service есть typed UntrustedCertificateException и default отказ от bad certificates — полезная UX идея. Но allowSelfSigned=true callback принимает любой bad certificate, это не pinning конкретного сертификата. Такой обход не является исправлением expired certificate и не должен становиться нашим default. Binary teapod-core может скачиваться из latest release; у нас сохранить exact pins/checksums. README сам говорит о минимальных unit tests; найденные parser/config tests не заменяют device/core integration. README ошибочно называет Xray MIT: официальный [LICENSE Xray](https://github.com/XTLS/Xray-core/blob/main/LICENSE) — MPL-2.0, поэтому лицензионную таблицу не копировать без проверки.

### Где TunXBox сохраняет преимущества и как действовать

- Уже есть оба интерфейса TV/Phone, богатые редакторы и локальная передача одного профиля/полной группы; не терять их при смене ядрового слоя. Это сильная сторона, не заявление эксклюзивности.
- Наши documented release gates проверяют signer/versionCode/metadata/hash/ABI/16KB и Android smoke; статический аудит других проектов не даёт оснований объявлять их CI хуже во всех отношениях.
- Главные текущие пробелы TunXBox: XHTTP/Xray, lossless Happ compatibility, понятные причины ошибок и continuous tunnel health/failover. Поддержка Hysteria2 в коде уже есть: expired server certificate не является отсутствием протокола.
- Приоритет: capability-based core choice и общий Go/JNI bridge по идеям VPN4TV/YPtun; затем staged diagnostics/heartbeat по идеям TeapodStream; Happ routing/parser fixtures; расширенные AWG/RTC/DNS engines отдельно, не mega-PR.
- Ни один проект не устанавливает «поддержку абсолютно всего» по списку README. Переносить небольшие лицензированно допустимые компоненты/идеи с regression tests, а не менять весь base tree.

## Acceptance evidence

Пока: скриншоты владельца и статический аудит baseline, официальные страницы. В реализации D1 пока только fail-closed builder; заметный локализованный UI ошибки ещё впереди. НЕ выполнены: воспроизведение с приватным провайдером, полная матрица клиентов, Xray integration, новый diagnostic UI, sanitizer или регулярный monitor. Предоставлять обезличенные fixtures; не помещать реальные подписочные токены/ключи в Git/CI/artifacts/issues. В каждом последующем PR отмечать точную core/build version и фактически прошедшие проверки.


## D1: первый безопасный вывод ошибки обновления подписки

В реализации: manual update failure теперь показывает заметный EN/RU диалог с этапом, advisory категорией, действием и фиксированным кодом, без raw URL/credentials. Категории: TLS time/trust, REALITY, unsupported, DNS, timeout, HTTP, empty и unknown. TLS time не объявляется исключительно expired: проверяются часы/сертификат без bypass. TV фокус OK, dismiss возвращает существующий фокус; Phone dialog закрывается при уходе activity в background и не удерживает update lock. TV общий toast не дублирует уже показанный диалог. Background update не открывает неожиданный modal. GroupUpdater boundary пишет только code, не raw exception dump.

Добавлены 11 classifier tests и 2 real Activity/binder UI regression tests; полный CI новой ревизии ещё требуется. Строковые категории — подсказка, не строгая typed network diagnosis. Это НЕ новый core, НЕ certificate/server fix, НЕ complete report sanitizer и НЕ continuous health/failover. Native core/другие существующие log paths пока не считаются очищенными; raw report всё ещё не отправлять автоматически. Last-result history и диагностика всех handshake/URL-test экранов — следующие шаги.

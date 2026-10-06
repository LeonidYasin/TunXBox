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

## Acceptance evidence

Пока: скриншоты владельца и статический аудит baseline, официальные страницы. В реализации D1 пока только fail-closed builder; заметный локализованный UI ошибки ещё впереди. НЕ выполнены: воспроизведение с приватным провайдером, полная матрица клиентов, Xray integration, новый diagnostic UI, sanitizer или регулярный monitor. Предоставлять обезличенные fixtures; не помещать реальные подписочные токены/ключи в Git/CI/artifacts/issues. В каждом последующем PR отмечать точную core/build version и фактически прошедшие проверки.

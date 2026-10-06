# Аудит старых веток и требования следующего discovery PR

## Baseline и метод

Baseline: stable 1.5.0 source `7e230f24` после PR #1/#2. Это статический аудит исходников, НЕ утверждение об успешной сборке/работе старых веток. Старые refs сохранены. Squash history означает, что ahead_by/behind_by не являются списком новых функций. Ни одна старая ветка целиком не вливается в master.

## Discovery: две ветки, один следующий design

Исследованы `feature/proxy-discovery` (`8e20a35e`) и `feature/auto-proxy-discovery` (`4e602b76`). Одинаковые LanScanner/DiscoveredProxy/ProxyScanActivity/adapter/monitor concepts; различия — сохранение профилей и упрощение service. Отдельные два PR с повторным добавлением этих классов не нужны.

| Находка | Источник | Почему нельзя переносить без адаптации |
|---|---|---|
| Подсеть по умолчанию 192.168.1 и перебор 1..254 | LanScanner в обеих ветках | Не соответствует произвольной Wi-Fi сети/prefix; нельзя сканировать VPN/мобильную сеть автоматически |
| Тип определяется номером открытого порта | LanScanner | TCP connect не доказывает SOCKS/HTTP proxy и не является Internet health check |
| Много параллельных jobs; нет общего deadline/ограничителя/явного cancel UI | LanScanner/ProxyScanActivity | Нужны bounded concurrency, total timeout, закрытие sockets при cancel и защита от повторного запуска |
| CancellationException может попасть в catch(Exception) | ProxyDiscoveryService | Отмена не должна превращаться в обычный error/success с сохранением данных |
| Автоматическое сохранение всех результатов с устаревшими beans/API | proxy-discovery/ProxyDiscoveryService | HttpBean/SocksBean и createProfile(profile) не соответствуют текущим HTTPBean/SOCKSBean и createProfile(groupId,bean); нужны выбор/подтверждение и точная группа |
| В auto variant выбор показывает Toast и закрывает экран | auto-proxy-discovery/ProxyScanActivity | Профиль фактически не создаётся — нельзя показывать «импортирован» |
| hideLoading — placeholder; строки частично hardcoded | ProxyScanActivity | Нужны реальные progress/cancel/error/retry, локализация и доступность |
| NetworkStateMonitor.internet callback и локальный monitor без сохранённого lifecycle handle | NetworkStateMonitor и старая MainActivity | Не равен Wi-Fi LAN; проверить unregister/lifecycle и не сканировать без opt-in |
| В auto branch есть дополнительный app/.../sagernet/MainActivity.kt с текстом HTTP404 ошибки | literal tracked file | Не Kotlin-код. Не переносить error-output как исходник; первопричину старой записи по этому факту установить нельзя |
| Старые workflow/manifest/MainActivity изменения | обе ветки | Не заменять принятую release/signing/QR/navigation архитектуру прежними версиями |

Пути источников: [proxy service](https://github.com/LeonidYasin/TunXBox/blob/8e20a35e/app/src/main/java/io/nekohasekai/sagernet/service/ProxyDiscoveryService.kt), [auto scanner](https://github.com/LeonidYasin/TunXBox/blob/4e602b76/app/src/main/java/io/nekohasekai/sagernet/network/tv/LanScanner.kt), [auto UI](https://github.com/LeonidYasin/TunXBox/blob/4e602b76/app/src/main/java/io/nekohasekai/sagernet/ui/ProxyScanActivity.kt). Имена «auto»/«service» сами по себе не доказывают реализованный background discovery/Android Service.

## Минимальный следующий scope

1. Начать с явного **ручного поиска в доверенной локальной сети**, одинаково доступного в TV/Phone. Auto discovery — последующий opt-in этап, не условие первого PR.
2. Определять active physical Wi-Fi/Ethernet Network/LinkProperties, валидировать ограниченную scan scope и выбранные ports; не автоматически расширять large prefixes и не сканировать VPN/public targets. Перед запуском показать область/согласие.
3. TCP candidates отделить от подтверждённого протокола. Ограниченный SOCKS5/HTTP handshake без пользовательских credentials; auth-required отдельно. Не связывать discovery с failover.
4. Один активный scan, bounded parallelism/total deadline, cancel при lifecycle/network change; cancellation rethrow, sockets закрываются. Не логировать токены/пароли.
5. Пользователь выбирает результат, проверяет/редактирует bean и подтверждает целевую группу. Использовать текущий ProfileManager; результат только после DB success; предотвратить дубликаты. Не auto-connect.
6. Видимые статусы/ошибки, Progress/Cancel/Retry, DPAD focus/click и touch; не обязательно переносить старый FAB screen.
7. Fixtures: non-proxy open port, SOCKS5 no-auth/auth-required, HTTP response/auth-required, malformed/hanging peer, cancel/network-change/double-click/duplicate, correct group; JVM + controlled local fake server + Android 4KB/16KB. Реальный скан пользовательской сети требует согласия и не используется в CI.

## fix/tv-ui-rendering

HEAD `c5c504cf` содержит старые версии MainActivityTv/MainBrowseFragment/QR server и UI layouts, которые пересекаются с уже принятым PR #1. Это не готовый новый TV PR. Сначала воспроизвести уникальную проблему на stable; сравнить именно content trees и актуальные пользовательские сценарии. Затем точечное исправление + regression test. Если unique fixes не найдены, направление закрывается как redundant, старый ref сохраняется до решения владельца.

## Состояние следующего PR

Этот документ — аудит/design и критерии, не реализация discovery. Пока нет нового scan endpoint/UI/profile creation, не объявлять функцию доступной. Код старых веток не скопирован. Следующий implementation commit должен включать документацию, tests и versionCode base следующего release cycle, а не изменение immutable v1.5.0.

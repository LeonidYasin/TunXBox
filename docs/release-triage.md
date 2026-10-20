# PR / issues: порядок к стабильной 1.6

Снимок 2026-10-20. Все шесть открытых PR и оба открытых issue рассмотрены; #11 закрыт как superseded, создано отдельное исследование #13. Проверено по API и **полной** Git истории (shallow clone ошибочно показывал unrelated heads).

## Не нужно закрывать весь backlog

Stable означает принятый определённый набор функций, воспроизводимую сборку и отсутствие неприемлемых ошибок в нём, а не пустой список issues. Незавершённые новые возможности, исследования и документированные ограничения могут оставаться открытыми. Реальная регрессия поддерживаемого сценария/утечка/ошибка обновления не становится допустимой только от смены milestone. Known issue должен иметь severity, evidence и явное fix/defer решение; нельзя писать «исправлено» без проверки исходного симптома.

## Канонический путь

[PR #12](https://github.com/LeonidYasin/TunXBox/pull/12) теперь направлен в master и является единым **draft integration candidate**. База runtime сохранена: `6801803` → scroll/NET_BUSY/source prep. После review, final CI и приёмки интегрировать его один раз; отдельный stable выпуск потом. Ретаргет не мержит master и не принимает runtime автоматически.

| Объект | Что установлено | Решение / критерий закрытия |
|---|---|---|
| [PR #5](https://github.com/LeonidYasin/TunXBox/pull/5) | Исторический compatibility/diagnostics scope; head0eb0b85 не ancestor, REALITY patch перенесён как4541e4b; branch конфликтует с master | Оставлен draft. Не merge whole branch. Unique scope проверить отдельно; unresolved runtime #6/#10, future core #13. После переноса всех unique задач можно закрыть как superseded, но не как all-protocol success |
| [PR #7](https://github.com/LeonidYasin/TunXBox/pull/7) | Updater; head6bf93fe ancestor кандидата | Сохранён как review slice1/3. Реальный installer/resume/signature/data acceptance; после интеграции закрыть covered, не merge повторно |
| [PR #8](https://github.com/LeonidYasin/TunXBox/pull/8) | TV health/navigation/updater UX; head336eb58 ancestor | Review slice2/3; TV/Phone acceptance и честные test states, не Hysteria throughput proof |
| [PR #9](https://github.com/LeonidYasin/TunXBox/pull/9) | Group rows/subscription metadata; head6801803 ancestor | Review slice3/3; группы/фокус/крупный шрифт/metadata import на реальном устройстве |
| [PR #11](https://github.com/LeonidYasin/TunXBox/pull/11) | Старый master-based source/F-Droid prep | **CLOSED, superseded by12**, не merged. Ветка/исходное описание сохранены. Открытые license/provisioning gates не закрыты |
| [PR #12](https://github.com/LeonidYasin/TunXBox/pull/12) | Полный актуальный integration candidate; rc158 tested source0bd5770 | OPEN draft → master. Physical portrait scroll + TV/updater + final exact-head CI + fix/defer issues6/10; затем owner review/merge/stable |
| [Issue #6](https://github.com/LeonidYasin/TunXBox/issues/6) | Hysteria2 intermittent/no traffic/low speed, причина неизвестна | OPEN. Реальное сравнение одинакового профиля/сети/устройства; отдельно от сертификата подписки и от XHTTP. Не закрывать health UI или smoke |
| [Issue #10](https://github.com/LeonidYasin/TunXBox/issues/10) | Auto/Sweden TLS/timeout vs Happ | OPEN. Диагностика и worker race исправлены, исходная совместимость не подтверждена. Сравнивать тот же конкретный outbound и strict certificates; Auto aggregate не доказательство |
| [Issue #13](https://github.com/LeonidYasin/TunXBox/issues/13) | Extended/Xray/XHTTP migration experiment | OPEN post-1.6; не обещание функции текущего stable. Source/adapter/license/fixture gates в [стратегии](core-extension-strategy.md) |

## Release gates

- Обязательные: безопасная миграция/неизменная подпись/package, increasing code, source tag, full JVM/UI/core/alignment CI, отсутствие silent fallback/security regression, реальная приёмка изменённых Phone/TV действий.
- Исходные жалобы #6/#10: при воспроизводимой регрессии заявленного основного сценария — исправить до релиза либо изменить согласованный release scope с честным ограничением, не «зелёный UI = всё работает».
- Новые XHTTP/Xray/автоматический failover/discovery extensions: отдельный future scope, не обязательны для каждого stable.
- Main F-Droid inclusion: отдельные юридические/build-server/distribution gates; GitHub stable не доказывает их прохождение. Recipe остаётся disabled, а release notes не заявляют F-Droid availability.

Описания существующих PR/issues обновлены с текущим triage и исходной историей. Ветки не удалялись; feature PR не merged и не закрывались как «исправлены» без приёмки.

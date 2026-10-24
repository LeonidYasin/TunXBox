# TunXBox for Android TV & Phone

**Ваш VPN-клиент. Ваши серверы. Ваш контроль.** TunXBox — независимый открытый Android-клиент на базе NekoBox/SagerNet и sing-box. Интерфейс для пульта и телефона, профили/группы, локальный обмен QR/браузером и безопасная диагностика. Проект не продаёт VPN-серверы и не гарантирует доступность вашего провайдера.

- [Сайт TunXBox](https://leonidyasin.github.io/TunXBox/) · [English](https://leonidyasin.github.io/TunXBox/en/)
- [Резервирование исходников](docs/source-preservation.md) · [Подготовка F-Droid](fdroid/README.md)

## Продолжить разработку

[AGENTS.md](AGENTS.md) → [актуальная передача работы](docs/agent-handoff.md) → [triage PR/issues](docs/release-triage.md). Единый draft кандидат 1.6 — PR #12 в master; stable ещё не выпущен. [Стратегия extended/Xray/XHTTP](docs/core-extension-strategy.md) — отдельный проверяемый эксперимент, не функция текущего APK.

## Скачать и проверить

- [Стабильная 1.5.0](https://github.com/LeonidYasin/TunXBox/releases/tag/v1.5.0) — неизменяемый опубликованный выпуск.
- [Новый ручной кандидат 1.6.0-rc.158](https://github.com/LeonidYasin/TunXBox/releases/tag/v1.6.0-rc.158) — опубликованные проверенные APK с исправлением портретной TV-прокрутки; реальная приёмка ещё нужна. Это отдельный тег, не автоматическое обновление rolling-канала. [Доказательства](docs/releases/1.6.0-rc.158-evidence.json).
- [Тестовый канал 1.6.0-rc](https://github.com/LeonidYasin/TunXBox/releases/tag/v1.6.0-rc) — только сборки, прошедшие обязательные CI gates. Новые изменения в ветке не означают, что APK уже опубликован.
- Последняя принятая владельцем проверочная сборка — rc.130, [CI](https://github.com/LeonidYasin/TunXBox/actions/runs/38090064430). Не выдавать изменения новой ветки за уже опубликованный APK.
- Каждый APK содержит Phone/TV. ABI, полная версия/code, commit и signer указаны в manifest/сведениях сборки. Проверяйте `SHA256SUMS.txt`; checksum не заменяет сертификат подписи Android.
- [Подпись и обновления](docs/signing-and-updates.md), [проверка устройства](docs/emulator-testing.md). [Обновление через меню](docs/app-updates.md) — кандидат отдельной feature-ветки, ещё требует CI/приёмки. [Подписка по словарной фразе](docs/subscription-passphrase.md) — дизайн R14.

## Честный статус

| Направление | Состояние |
|---|---|
| TV/Phone, QR и локальный обмен | Базовые функции опубликованы; проверяйте сценарии своего устройства |
| LAN-поиск и явный профиль gateway:10808 | PR #4 принят владельцем и merged в master (`99a8b6f2`); не включён задним числом в stable 1.5.0 |
| Подписки, сохранённые результаты и безопасные отчёты | Доработки PR #5 / проверочная интеграция; PR #5 ещё не merged |
| TCP VLESS REALITY на новых Xray | Патч прошёл CI rc.130; владелец сообщил, что работает более-менее; матрица реальных устройств остаётся открытой |
| XHTTP | Полная работоспособность не заявляется; отдельное направление |
| Hysteria2 | Владелец сообщил о нестабильности и низкой скорости; причина пока неизвестна. [Расследование](docs/hysteria-health.md) |
| Логический хост «шлюз», автообновление адреса | Отдельные будущие задачи, не готовые функции |

## Документация и сайт

- [Полная карта проекта](docs/project-map.md), [документация](docs/README.md), [roadmap](docs/roadmap.md).
- [Каталог функций](docs/features.md), [архитектура](docs/architecture.md), [сценарии](docs/scenarios.md), [QR/LAN](docs/tv-transfer.md), [реестр каждого файла](docs/reference/repository-index.md).
- [Исходник сайта](website/index.html) и [публикация GitHub Pages](docs/github-pages.md). [Публичный сайт](https://leonidyasin.github.io/TunXBox/) опубликован: deployment прошёл, HTTP 200 и содержимое проверены.

## Поддержать TunXBox

Помощь не сводится к деньгам: [варианты участия](docs/contributing-and-support.md).

- Тестирование на телефонах/Android TV, разных версиях Android и сетях, доступность пульта/экранных читалок.
- Воспроизводимые баг-репорты и обезличенные диагностические сводки.
- Код: Kotlin/Android, Go/ядро, импорт протоколов, тесты/CI, безопасность.
- Переводы, документация, UX/дизайн, помощь новым пользователям и распространение проверенных ссылок.
- Тестовое оборудование, добровольная инфраструктурная помощь и спонсорство разработки.
- Разовая финансовая поддержка или регулярное сопровождение — пока по обсуждению с владельцем; платёжных реквизитов/автосписаний в приложении нет.

**Публичный контакт:** [leonidyasin@gmail.com](mailto:leonidyasin@gmail.com). Telegram: [@LEONI_YA](https://t.me/LEONI_YA). [GitHub Issues](https://github.com/LeonidYasin/TunXBox/issues) включены владельцем и подтверждены через API; безопасный баг-репорт можно отправить туда, по почте или в Telegram. Discussions не обещаны включёнными. Не публикуйте подписки, UUID, пароли, private keys или сырые логи.

## Лицензия и добровольное сопровождение

GNU GPL v3 или более поздняя версия; исходное авторство и [уведомление](LICENSE) сохранены, полный GPL включён в [COPYING приложения](app/src/main/assets/COPYING). [Проект услуг](docs/support-services-proposal.md) — не новая лицензия и не действующая оферта. Добровольная оплата услуг не ограничивает GPL; договорная гарантия возможна только на согласованную выполненную работу, не на отсутствие всех багов.

## Credits / upstream

[NekoBox for Android](https://github.com/MatsuriDayo/NekoBoxForAndroid), [SagerNet](https://github.com/SagerNet/SagerNet), [sing-box](https://github.com/SagerNet/sing-box), [shadowsocks-android](https://github.com/shadowsocks/shadowsocks-android), [Yacd-meta](https://github.com/MetaCubeX/Yacd-meta). TunXBox не является официальной сборкой этих проектов; их лицензии и вклад сохраняются. Ссылки на сторонние кошельки/донаты не используются для поддержки TunXBox.

## Следующий UI-кандидат

[Граф ТВ-навигации](docs/tv-navigation.md), [автопроверка активного туннеля перед YouTube](docs/tv-connection-health.md) и явная обратная связь докачки/установки включены в опубликованную rc.135 ([CI](https://github.com/LeonidYasin/TunXBox/actions/runs/38142308237)); нужна ручная проверка. [Названия подписок и отдельные строки каждой группы](docs/tv-groups-subscriptions.md) — следующий отдельный кандидат R16, **не в rc.135**.

Кандидат R16 также включает [три стартовые карточки и offline сведения о выпуске](docs/tv-groups-subscriptions.md): установленная версия видна сразу, изменения и проверки открываются сверху; полный About — внутри. Режимы TV/Phone сохраняют начальный фокус; это не входит в rc.135.

По отзыву о rc.135 подготовлен [кандидат R17](docs/connection-diagnostics.md): трафик за сессию вместо rate, отдельная диагностика перед YouTube, полный отчёт отказа запуска и временная QR/TXT-передача на смартфон. До собственного CI/owner acceptance не считать готовым выпуском; XHTTP это не добавляет.


Коррекция по rc.143: выбранная группа первой, отдельный ряд операций перед последним рядом возможностей; реальные session totals независимо от notification interval; ограниченная повторная проверка с прогрессом. [Scope и приёмка](docs/connection-diagnostics.md). Новый кандидат требует CI и проверки владельцем.

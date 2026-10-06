# PR #1: план готовности и приёмка

PR: `feature/tv-remote-support` → `master`. Не мержить до проверки APK владельцем.
Рабочий апстрим `main` и `master` сверены; их исходный код совпадал, отличие — workflow prerelease. Эти ветки не изменяются.

## Регрессии, найденные при установке

- [x] Удалены локализованные переопределения `app_name` / `app_name_long` в RU/UK/FA: название TunXBox теперь одинаково в установщике, launcher и системном списке приложений.
- [x] Отдельная иконка TunXBox (legacy/adaptive) и горизонтальный TV-баннер; package/applicationId не менялся.
- [x] `BlankActivity` больше не открывает Android share chooser автоматически. Экран сбоя предлагает «Закрыть», «Повторить запуск», «Поделиться логами»; Back закрывает, отмена chooser оставляет доступным экран восстановления. Логи отправляются только по явной кнопке и могут содержать чувствительные сведения.
- [x] Исправлено воспроизведённое падение TV `Cannot start headers transition`: перегрузка Browse `setSelectedPosition(..., task)` недопустима при `HEADERS_DISABLED`. Восстановление фокуса выбирает элемент через встроенный `RowsSupportFragment`, без перехода headers.
- [x] Удалена неиспользуемая eager-рефлексия скрытых `Socket.getFileDescriptor$` / `FileDescriptor.getInt$`: её ошибка роняла весь `UtilsKt`, включая создание VPN-сервиса. Полный тест запуска воспроизвёл `ExceptionInInitializerError`; это устранённый источник startup-падения, хотя совпадение с device log владельца ещё требует проверки.
- [x] Защита от null/пустого результата определения имени процесса.
- [x] Добавлены проверки локализованного имени, реальных ресурсов application icon/banner, закрытия/отмены/повтора экрана сбоя и полного жизненного цикла запуска TV/Phone. В JVM-тестах используются настоящие Binder сервисов Room/VPN, но запуск Android JNI/Go-ядра пропущен.
- [ ] Повторить холодный запуск на устройстве владельца. Точную причину исходного падения без device log нельзя считать установленной; исправление автоматической отправки логов само по себе не доказывает устранение всех источников падения.

## Регрессия URL подписки в браузерной передаче

- [x] Обычная HTTPS-ссылка с путём в верхнем поле автоматически направляется в импорт подписки и сохраняется для обновлений; определение дублируется на TV для старой страницы/нативных клиентов.
- [x] `SubscriptionFoundException` апстрим-парсера обрабатывается как подписка, а не ошибка конфигурации. Root HTTP-прокси и многострочные профили сохраняют исходный маршрут.
- [x] Ошибки имеют безопасные коды и русские пояснения: неверная ссылка, ответ без поддерживаемых профилей, загрузка/импорт с TV, истёкшее сопряжение. Не возвращаются исходные URL/исключения/ответы сервера подписки.
- [x] Неуспешный/отменённый импорт не оставляет новую пустую/частично заполненную группу; пустая подписка не выдаётся за успех.
- [x] Добавлены 15 JS-регрессий реального скрипта страницы, 8 JVM-тестов распознавания и 8 тестов реального HTTP ingress с подменённым скачиванием провайдера.
- [ ] Проверить личную подписку владельца на устройстве. В тестах используются синтетические адреса; личный URL не запрашивался.

## Реализованные пункты

- [x] Временная LAN-передача профилей: случайный токен, 10 минут, ограничения размера, Host/Origin, остановка при закрытии/уходе в фон. HTTP не зашифрован: только доверенная LAN.
- [x] Ввод без пульта: QR → браузер телефона, вставка ссылок, файл, подписка, простой SOCKS5/HTTP конструктор; нативная передача между приложениями. Это перенос профилей, не всех глобальных настроек/правил маршрутизации.
- [x] TV: выбранный профиль отдельно от работающего, текстовые состояния VPN, группы, обновление подписок, короткий путь к импорту.
- [x] TV: OK выбирает профиль без незаметного запуска; подключение явно, Menu/Info и экранное меню профиля, Play/Pause, отмена подключения, защита от повторных команд.
- [x] TV: детали, редактор апстрима, QR, удаление с подтверждением, порядок профилей для ручной сортировки; запрет изменения работающего профиля.
- [x] Стабильные ID и diff вместо пересоздания рядов; возврат фокуса после диалогов, редактора, обновлений и переключения групп.
- [x] Крупнее вторичный TV-текст, перенос/ellipsis, векторные иконки, отдельные состояния focus/selected/active, EN/RU, единственное масштабирование Leanback.
- [x] QR виден независимо от прокрутки кнопок, явная новая сессия/истечение токена, импорт картинки на TV без камеры.
- [x] Phone: горизонтальный путь профиль → edit → share → delete, Back закрывает drawer, Left учитывает границы ряда, фокус не перехватывается чужими кнопками.
- [x] Phone: контрастная рамка аппаратного фокуса без замены touch ripple; меню вместо жестов, сохранение фокуса, Media-команды и защита активного профиля. Увеличение шрифта минимум до 1.2 только на TV/устройствах без touch; больший пользовательский масштаб сохраняется.
- [x] Подписки: отмена/ошибка не оставляют блокировку обновления; диалоги учитывают жизненный цикл Activity/Fragment.
- [x] Реальные имена APK и Content-Disposition, фактические ABI, подписи и SHA256; без вводящих в заблуждение GitHub labels. Универсальный APK содержит ARM32/ARM64/x86/x86_64.
- [x] В CI добавлены Robolectric UI-регрессии и отчёты. Обнаружено, что прежний preview-cache не сохранял ключ. Публикация теперь требует постоянного секрета и публичного pin; временные ключи разрешены только для непубликуемой проверки кода.

## Автоматические ворота

- Локально: 33 JVM-теста протокола/политики/отмены/читаемости.
- В GitHub Actions: полный `app:testPreviewDebugUnitTest`, Android release build, тесты упаковки, проверка подписей/манифеста/скачанных контрольных сумм.
- Проверять именно последний SHA PR и совпадение SHA в `apk-manifest.json`; предыдущая зелёная сборка не заменяет последнюю.

## Приёмка владельцем перед merge

Автотесты и статический обзор не заменяют настоящий пульт/экран. Ниже намеренно остаются незакрытые аппаратные проверки.

- [ ] TV: стрелки/OK/Back без мыши; все основные действия доступны и фокус виден на краях экрана. Проверить длинные имена, 720p/1080p/4K, увеличенный шрифт.
- [ ] Выбрать один профиль, подключить другой; различать выбранный/работающий. Проверить connecting/connected/stopping, Play/Pause, двойные нажатия, отказ разрешения VPN.
- [ ] Группы/подписки: смена, обновление/отмена/ошибка сети; повторное обновление доступно. Menu и экранное меню дают одинаковые действия; удаление по умолчанию предлагает отмену.
- [ ] Открыть редактор, сохранить/отменить, открыть/закрыть QR и диалоги; вернуться на прежний профиль/контрол. Проверить ручное изменение порядка.
- [ ] LAN: браузер телефона, ссылки/файл/подписка, нативный перенос; no-camera импорт картинки. Новая сессия инвалидирует старую, фон/закрытие останавливают сервер, истёкшая сессия не работает.
- [ ] Phone: touch не ухудшился, D-pad достигает edit/share/delete/toolbar/drawer/Save/Cancel/IME; светлая/тёмная темы, portrait/landscape. При обычном запуске сначала выбор ТВ/Смартфон; последний выбор задаёт начальный фокус. Проверить поворот TV-интерфейса на смартфоне и одну плитку launcher.

## Оценка дизайна

TV сохраняет Leanback: короткая основная полоса, профили, вторичные инструменты; статус и фокус различимы не только цветом. Phone сохраняет Material апстрима, а не превращается в сетку TV-карточек. Это целевые исправления удобства, не полная смена визуального языка. Реальное расстояние просмотра, overscan, TalkBack/RTL и специфические прошивки требуют приёмки на устройствах; оценка по коду не доказывает их качество.

## Выбор режима и адаптация ориентации

- [x] Отдельный launcher-экран ТВ/Смартфон до создания основного интерфейса; два крупных фокусируемых действия, touch ripple, D-pad и доступные названия. Последний выбор — подсказка фокуса, не обход выбора.
- [x] VIEW/import остаются на upstream MainActivity; выбор не перехватывает ссылки.
- [x] TV использует fullUser (системная автоповорот/блокировка); компактные отступы и карточки для телефонов. QR в узком portrait расположен над прокручиваемыми действиями.
- [x] Главные действия: Смартфон → Группа → Подключение → Импорт; без дубля Смартфон в инструментах.
- [ ] Приёмка на устройстве: холодный launcher, Back, пульт/тач, обе ориентации и поворот во время QR-сеанса. JNI/Go и реальный VPN требуют устройства.

## Подтверждение, диагностика и доступ к расширенным функциям

- [x] TV после импорта убирает QR, показывает модальное подтверждение с количеством и кнопкой «Продолжить на TV». После подтверждения возвращается список; HTTP остаётся жив до ответа смартфону, затем закрывается по lifecycle. Успех сохраняется при пересоздании экрана.
- [x] Браузер показывает баннер вверху, переносит фокус/прокрутку к нему, блокирует повторную отправку и предлагает закрыть вкладку. Если window.close запрещён браузером, дано указание закрыть вручную.
- [x] TV: TCP-задержка/URL-тест текущей группы, прогресс, отмена, сохранённые результаты в карточках, очистка и сортировка по задержке. TCP — проверка порта, не ICMP; URL использует тот же TestInstance апстрима.
- [x] Карточка состояния VPN, активный профиль, реальные скорости прокси-трафика и итоги сеанса из SpeedDisplayData. Проверка активного туннеля вызывает binder urlTest, отдельно от теста группы.
- [x] Главный экран TV (HOME без остановки сервиса); YouTube после подключения, TV-пакет предпочтителен, отсутствие приложения объясняется без падения.
- [x] «Все возможности» открывает полные существующие экраны профилей/групп/маршрутов/настроек/инструментов/логов/О программе, не меняя сохранённый TV-режим; Back возвращает к TV. Полные меню профилей, редакторы, dashboard и прочие пункты доступны также через drawer этих экранов с условиями апстрима.
- [ ] Это функциональный доступ через повторно используемые upstream Material-экраны, а НЕ завершённая самостоятельная Leanback-копия каждого редактора/настройки. Полное нативное TV-дублирование всех экранов ещё не выполнено; не объявлять его готовым.
- [ ] Приёмка на устройстве: заметность подтверждений, настоящие TCP/URL/активный VPN-тест и отмена, результаты группы, непрерывность VPN при HOME/YouTube, remote-доступность расширенных экранов.

## Закрытие обнаруженных пробелов пульта и обновлений

- [x] Каталог «Все возможности» формируется из того же `main_drawer_menu`, что смартфон; Dashboard и дополнительные пункты учитывают исходные условия. Отдельного неполного ручного списка больше нет.
- [x] Повторный запуск расширенного экрана через singleTask `onNewIntent` выбирает нужный раздел; Back возвращает сразу к TV, настройка режима не меняется, повторный preview-диалог не мешает.
- [x] Явные операции без жестов: перемещение/удаление групп и маршрутов, элементов цепочки; действия файлов ресурсов. Menu/Info, экранная кнопка и long press дают доступ к меню. Удаление подтверждается с фокусом на отмене; активная VPN-группа защищена.
- [x] Дополнительные настройки, раньше доступные только long press, открываются Menu/Info. Вкладки инструментов фокусируемы; горизонтальные пути к контролам имеют цели минимум 48dp. Touch-обработчики и полные upstream редакторы сохранены.
- [x] Исправлены вставочный порядок цепочки и ошибочный header-offset маршрутов при перестановке; маршруты получили stable IDs.
- [x] Код постоянной подписи, роста versionCode и fail-closed проверки APK identity/предыдущего релиза реализован; отдельный `check_only` не публикует нестабильно подписанные сборки.
- [ ] **Блокер публикации:** владелец должен сохранить постоянный ключ через `buildScript/setup_signing.py` либо предоставить безопасный административный доступ к Actions Secrets. Вход браузером не выполнен; текущий MCP не умеет изменять Secrets. Не объявлять финальный APK опубликованным до этого шага.
- [ ] Выпустить две последовательные сборки с одним pin/сертификатом и возрастающим versionCode; проверить обновление поверх на устройстве. Старые временно подписанные сборки требуют одноразовой миграции с backup.

Архитектура расширенных функций — общие полные Android/Material-экраны с доработанным пультом, а не две расходящиеся копии каждого редактора. Это закрывает найденные пропуски доступа, но не является заявлением о полностью новом Leanback-дизайне всех экранов. Полный аппаратный обход остаётся частью приёмки.

| Раздел | Источник функций | Доступ без touch-жестов |
| --- | --- | --- |
| Профили, все протоколы, меню группы | Upstream ConfigurationFragment / add-profile menu / editors | Полные меню, кнопки и предыдущие remote-пути |
| Группы и подписки | GroupFragment / GroupSettingsActivity | Edit/Options/Update, перестановка и удаление через меню |
| Маршруты | RouteFragment / RouteSettingsActivity | Включение, редактор, явные перестановка/удаление |
| Глобальные настройки | SettingsPreferenceFragment | Preference-ряды, Menu/Info для дополнительных действий |
| Инструменты и backup | ToolsFragment / NetworkFragment / BackupFragment | Фокусируемые вкладки, стандартные кнопки/диалоги |
| Файлы ресурсов | AssetsActivity | Импорт/обновление; явное удаление пользовательских файлов |
| Цепочки | ChainSettingsActivity | Добавить/заменить; явные порядок/удаление |
| Логи, dashboard, документация, о приложении | Исходное navigation menu | Общие экраны и условные исходные пункты |

Автоматический failover не включён в PR #1: отдельный сервисный мониторинг/проверки обрывов планируются отдельно. Ручной тест активного туннеля и тестирование профилей уже реализованы.

- [x] Проверка открытия настроек воспроизвела повторный Binder callback при rebind: SagerConnection теперь игнорирует поздний callback после disconnect и идемпотентен для повторного Binder. Добавлен регрессионный тест. Это JVM-воспроизведение, не утверждение о конкретной прошивке.
- [x] Клавиши JSON-редактора фокусируемы и 48dp; горизонтальный список получает оставшееся место, Undo/Redo/Format не выдавливаются за экран телефона. Вторичный текст списка приложений поднят до 14sp.

## Review follow-up: controls, modern Android and project identity

- Primary TV row: Connect/Disconnect → group → phone import → smartphone mode. Cold-launch focus is on the connection action; rotation/restoration keeps the user's current selection.
- Narrow TV windows use no focus zoom, fixed vertical row padding and no child-layout animations. Action/profile text reserves its line count; traffic only updates the status card. Real touch scrolling still needs device acceptance.
- Restart/Close are last in the tools row and smartphone drawer. Both ask confirmation with Cancel initially focused. Close removes the UI task; restart uses ProcessPhoenix with an explicit saved-mode destination. Neither sends VPN's CLOSE command; the separate background service is not force-disconnected.
- About/version checks now point to TunXBox. Updates are compared by real manifest versionCode, applicationId and installed signing certificate, not the upstream release title. Upstream source/donation attribution is explicitly labelled.
- Promotions is a local information page, not an automatic upstream advertising link. README separates TunXBox downloads from inherited upstream documentation.
- Native Go/JNI build uses external linker 16KB max/common page sizes. Old 4KB core caches are excluded. CI validates actual 64-bit ELF LOAD/RELRO segments in AAR and every APK (and ZIP alignment for uncompressed natives). The published rc.93 APK had targetSdk 35 but 4KB arm64 LOAD alignment. This addresses that compatibility issue without suppressing Android's warning. A real 16KB device/emulator VPN test remains required.
- SHA256SUMS.txt checks downloaded APK bytes; not an installer and not a replacement for APK signing.


### Follow-up: complete TV Add profile chooser
- The permanent top-row Add profile action and empty-group card open the same method chooser, never LAN QR directly. Connect remains first/default focused and phone mode stays last.
- Choices: receive from phone/browser, URL, QR/camera/image, clipboard, file and manual configuration. Phone-to-TV sending is outbound export, not an inbound add method.
- Manual configuration now reads all 17 entries from the phone + menu, and both modes share editor intents (including VLESS flags, custom configuration and chains).
- LAN pairing is opened only after explicitly choosing phone import. Existing file size/security/subscription rules are unchanged.
- Added 8 automated regressions for top/empty chooser parity, populated groups, explicit QR navigation, all manual editor routes, phone + and localization. Physical remote/file-picker/editor acceptance is still required before merge.
- Direct Happ/Incy migration is not claimed; app-specific export compatibility needs a separately scoped change.


### Explicit QR receive/send and complete group snapshot
- Add profile includes visibly separate Receive — show QR, Send profile/group — show QR, and Scan QR/image actions. The receiving browser/QR/success UI is retained.
- Sending offers selected profile (standard QR) or whole current group (authenticated LAN QR). The same group sender is available from phone +. Both scanners honor explicit import/export direction; old directionless QR keeps its old device-specific meaning.
- Full group snapshot preserves all profile beans, name, selector/front/landing and remaps internal chain profile IDs atomically to new IDs. It creates a basic copy, not a subscription/global-settings backup. Unsupported external references or cyclic chains are rejected before sharing; no profile is silently omitted.
- Group count, 2MiB transmitted/decompressed limits, token, expiry, private-LAN-only addresses and redirect restrictions remain enforced. Receiving sessions cannot export; sending sessions cannot import. GET only means data was provided, not that remote import succeeded; UI states this correctly.
- Both devices should use the new version for group snapshots. Physical two-device acceptance remains required before merge.

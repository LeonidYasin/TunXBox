# Граф навигации ТВ — source-linked

Сгенерировано из [машиночитаемого графа](reference/tv-navigation.json). Это переходы и OK-подтверждения, не скрытая телеметрия. Направления D-pad и реальная длительность измеряются отдельно на устройстве. При изменении action/menu IDs CI требует обновить граф.

```mermaid
flowchart LR
    TV["Главный экран ТВ"]
    CONNECT["Запуск выбранного профиля"]
    CONNECTED["Туннель запущен"]
    HEALTH["Результат URL-проверки"]
    STATUS["Подробности текущего соединения"]
    YOUTUBE["YouTube (внешнее приложение)"]
    GROUPS["Выбор группы"]
    ADD["Способы добавления"]
    PROFILE_ACTIONS["Действия профиля"]
    SUB_UPDATE["Обновить подписку"]
    SUB_REPORT["Отчёт подписки"]
    GROUP_TESTS["Тесты группы"]
    ALL_TOOLS["Все функции"]
    PHONE["Режим телефона"]
    HOME["Системный Home"]
    RESTART["Подтверждение перезапуска"]
    CLOSE["Подтверждение закрытия"]
    UPDATER["Обновление приложения"]
    INSTALLER["Установщик Android"]
    PERMISSION["Разрешение источника APK"]
    TV -->|"TOGGLE / OK 1"| CONNECT
    CONNECT -->|"автоматически / OK 0"| CONNECTED
    CONNECTED -->|"Авто URL-тест (foreground) / OK 0"| HEALTH
    CONNECTED -->|"STATUS / OK 1"| STATUS
    STATUS -->|"Повторить активный тест / OK 1"| HEALTH
    HEALTH -->|"YOUTUBE / OK 1"| YOUTUBE
    TV -->|"GROUPS / OK 1"| GROUPS
    TV -->|"ADD_PROFILE / OK 1"| ADD
    TV -->|"PROFILE_ACTIONS / OK 1"| PROFILE_ACTIONS
    TV -->|"UPDATE / OK 1"| SUB_UPDATE
    TV -->|"SUBSCRIPTION_DIAGNOSTICS / OK 1"| SUB_REPORT
    TV -->|"TESTS / OK 1"| GROUP_TESTS
    TV -->|"FULL_TOOLS / OK 1"| ALL_TOOLS
    TV -->|"MORE / OK 1"| ADD
    TV -->|"PHONE_MODE / OK 1"| PHONE
    TV -->|"HOME / OK 1"| HOME
    TV -->|"RESTART_APP / OK 1"| RESTART
    TV -->|"CLOSE_APP / OK 1"| CLOSE
    ALL_TOOLS -->|"nav_app_updates / OK 1"| UPDATER
    UPDATER -->|"Разрешение при необходимости / OK 1"| PERMISSION
    UPDATER -->|"После Check + Download + verify / OK 1"| INSTALLER
    PERMISSION -->|"Разрешить и вернуться / OK 1"| INSTALLER
```

## Основной путь

Раньше для явной проверки перед YouTube: подключиться → открыть статус → запустить тест → открыть YouTube: **4 OK**, плюс перемещения/первое согласие Android. Новый кандидат: подключиться → автоматический результат → YouTube: **2 OK**. Это модель подтверждений при уже выбранном профиле, не доказательство всех физических D-pad путей. Ошибка не открывает обязательный modal и не крадёт фокус.

## D-pad и порядок

- Верхний ряд: подключение → группы → добавить → Phone; начальный фокус и стабильные IDs сохранены.
- Следующий ряд: компактный статус/результат → YouTube (когда сервис запущен). Из Connect: Down к статусу, Right к YouTube при исходной колонке 0. Проверить на 720p/1080p, больших шрифтах и после обновления данных.
- Профили: отдельный ряд; число карточек и положения динамические, не выдавать условный shortest path за реальные расходы всех пользователей.
- Инструменты: текущие 9 действий пока сохраняются; граф показывает дубль MORE/ADD и длинный горизонтальный путь к редким операциям.

## Следующая оптимизация (не реализована этим графом)

Собрать редкие Home/restart/close и отчёты в один список «Ещё», убрать дубли импорта без потери методов, дать updater прямой путь. Сравнить частые и редкие сценарии: OK, D-pad, диалоги, возврат Back, focus stability, error recovery. Не уменьшать клики удаления ценой подтверждения и не скрывать ошибки. Все функции полного [каталога](features.md) должны оставаться достижимы.

## Критерии регулярного review

После каждого UI PR обновлять JSON/граф, CI coverage/confirmation budget и реальные скриншоты/DPAD smoke. Проверять выбранный/активный профиль, offline/failure/no profiles, смену сети, отмену, YouTube return, installer return. Граф не утверждает готовность runtime функций до CI и device acceptance.

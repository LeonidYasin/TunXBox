# Независимые копии исходников и восстановление

## Что закреплено

`buildScript/sources/lock.json` фиксирует шесть исходных снимков: sing-box, libneko, gomobile, Yacd-meta, генераторы GeoIP/Geosite; также фиксируются версии и SHA-256 двух готовых routing-баз. Изменяемые ветки и `latest` не участвуют в новой baseline-подготовке. Runtime-обновление routing-баз пользователем сохранено.

Снимок сохраняет полное дерево выбранной ревизии, **не всю историю Git**. GitHub fork не является полноценной резервной копией при отказе GitHub. Создание отдельных форков через текущее подключение вернуло 403; не считать эти форки созданными.

## Переносимый архив

```sh
python3 buildScript/sources/fetch.py prefetch
bash buildScript/lib/core/get_source.sh
bash buildScript/lib/dashboard.sh
python3 buildScript/sources/archive.py --go-modules --pnpm-store .source-cache/pnpm-store
```

Нужны Go, Node и pnpm закреплённых версий. До `--go-modules` подготовить source snapshot gomobile: `python3 buildScript/sources/fetch.py component gomobile`.

Архив содержит source snapshots, lock, baseline-базы, исходники приложения из текущего Git commit, MANIFEST.json с SHA-256, инструкции восстановления и, при указанных флагах, Go module ZIPs/mod и pnpm package store. Публичная архивная копия не содержит private signing keys/local.properties. Создание архива из dirty checkout отвергается, чтобы не скрывать незакоммиченные изменения.

Обязательно хранить копию **вне GitHub**: локальный диск + ещё один независимый носитель/хранилище. Отдельно и приватно хранить действующий signing backup. Нельзя включать его в публичный source archive.

```sh
# После проверки внешнего SHA256 и файлов по MANIFEST.json:
export SOURCE_ARCHIVE_DIR=/path/to/tunxbox-source-backup/archives
export SOURCE_OFFLINE=1
python3 buildScript/sources/fetch.py prefetch
bash buildScript/lib/core/get_source.sh
```

`SOURCE_ARCHIVE_DIR` должен содержать только ожидаемые lock-файлом имена; каждый проверяется по SHA-256. Установить/восстановить зависимости pnpm и Go нужно отдельно. Go module archives и pnpm store не заменяют Android SDK/NDK.

## Границы резервной копии

Не заявляется, что уже сохранены **все транзитивные исходники**. Android/Maven source JARs, все исходные входы GeoIP/Geosite, исходники компиляторов и вся Git history пока не входят в архив. Пакеты JS могут включать build-tool binaries; наличие их в store не доказывает сборку всех инструментов из исходников. `allTransitiveSourcesArchived` намеренно остаётся false.

Лицензирование libneko/Yacd и происхождение входных данных требуют отдельного разбора: см. [F-Droid preparation](../fdroid/README.md) и [third-party notices](../THIRD_PARTY_NOTICES.md).

## Интеграция и качество

Существующие stable APK/tag/signing identity не меняются. Не удаляются dashboard, протоколы, QR/LAN-обмен, UI TV/phone или runtime-обновления баз. Новая ветка требует CI, проверки source-built dashboard на старых WebView и существующих ARM/device gates до merge.

Source-readiness workflow проверяет исходники и собирает dashboard/gomobile. Android job отдельно собирает F-Droid APK и запускает существующие 4KB/16KB emulator smoke tests. Успешные Python-тесты сами по себе не означают успешный Android/F-Droid build.

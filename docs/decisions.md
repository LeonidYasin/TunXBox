# Архитектурные решения

Короткие записи фиксируют контекст → решение → последствия. Они описывают решения текущего кода, не утверждают, что формальный ADR-процесс существовал раньше.

| ADR | Контекст | Решение | Последствия / компромисс |
|---|---|---|---|
| ADR-01 | Нельзя потерять upstream редакторы при TV fork | Один APK, Phone + Leanback TV, shared catalogs/editors | Максимальная функциональная полнота без двух несовместимых implementations; shared forms требуют remote adaptation |
| ADR-02 | TV без клавиатуры/камеры | Temporary QR/browser LAN receiver | Просто с телефона без установки; trusted LAN и незашифрованный HTTP — явный trade-off |
| ADR-03 | Один QR URI раньше интерпретировался по-разному | Явный import/export mode, общий client; legacy defaults отдельно | Надёжное направление; оба устройства обновлять для нового group snapshot |
| ADR-04 | Группа содержит local IDs и internal beans | Typed snapshot, validation, one DB transaction + ID remapping | Сохраняет внутренние chains; внешние связи/cycles отказывают, вместо silent partial export |
| ADR-05 | Пользователь уходит смотреть YouTube | UI lifecycle отдельно от :bg service | Close/restart UI не означает VPN disconnect; full restore/settings restart — другой путь |
| ADR-06 | Установка новой RC не должна требовать uninstall | Stable applicationId/key + increasing versionCode | Хранить и бэкапить private key; legacy temporary signer мигрируется отдельно |
| ADR-07 | JVM не доказывает Android/JNI behavior | Layered tests + real 4KB/16KB emulators + owner acceptance | Дольше CI, но обнаруживает реальные focus/touch/runtime ошибки; не все OEM/ARM/VPN проверены |
| ADR-08 | “Все файлы” быстро устаревает | Docs-as-code, auto repository/settings references, local link checker | Документы обновляются в том же PR; checker — структурный контроль, не семантическое доказательство |
| ADR-09 | Нельзя приписывать future functionality к working app | Separate roadmap/limitations from feature catalog | Happ/Incy migration и новый auto failover не обозначаются завершёнными |

## Следующие решения, требующие отдельного объёма

- Continuous health/failover policy: критерий живости, hysteresis, cooldown, battery/network cost, group selection, UX ошибок и проверки на реальном VPN.
- App-specific migration Happ/Incy: версии/formats, supported standard exports, credentials redaction, invalid/partial payload rules.
- Reproducible assets и дополнительная ARM/Android TV instrumented matrix: deterministic pins, CI budget, test ownership.
- Более узкая cleartext policy/защищённый LAN protocol: compatibility, certificate pairing, browser constraints и threat model.

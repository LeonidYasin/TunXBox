# Безопасность и границы доверия

## Защищаемые данные

Proxy credentials/private keys, subscription URLs (opaque path/query может быть access token), configuration/backup files, QR session tokens, signing keystore/bundle, backup password. Public certificate fingerprint и SHA256 APK не являются private signing key.

## LAN transfer

- Сервер привязан к приватному IPv4 интерфейсу; QR-client проверяет private LAN IP и допустимый port, не следует redirect.
- Token 256 bits, срок 10 минут, header authentication. Browser QR token находится в URL fragment; native QR — в собственном app URI, не URL журналируемого HTTP запроса.
- Host/Origin checks, no wildcard CORS; browser CSP/Referrer/cache controls; /status не выдаёт credentials.
- Receiver запрещает GET export; sender запрещает POST import. Показ QR — явное предоставление ограниченного доступа его обладателю.
- HTTP не шифрует данные. Не использовать guest/public/untrusted LAN и port forwarding; VPN на телефоне не превращает LAN HTTP в защищённый протокол.
- Payload/decompressed group bytes ограничены 2 MiB; group record count и known types/dependencies проверяются; group import выполняется в transaction.
- Closing/backgrounding QR/phone export UI останавливает server. Sender status “provided for import” не выдаётся за подтверждение remote success.

## Storage, permissions и ОС

Room содержит секретные профили. [backup descriptors](../app/src/main/res/xml/backup_descriptor.xml) включают базы; ручной JSON backup также может содержать credentials. App-local backup не следует автоматически считать зашифрованным. Защита platform backup зависит от Android/провайдера; хранить exports безопасно.

Manifest использует network, foreground/VPN, notifications, boot/wake, package visibility, optional camera и battery-optimization capabilities. IPC service permission signature-protected; native Android VPN permission контролируется ОС. Package visibility/plugin querying не следует путать со сбором/отправкой списка приложений в облако.

[network_security_config](../app/src/main/res/xml/network_security_config.xml) разрешает cleartext в base-config, то есть политика шире одного LAN endpoint. Это наследованное ограничение, не обещание “всё приложение только HTTPS”. Settings globalAllowInsecure/allowInsecureOnRequest/custom config могут ослаблять TLS проверки; пользователь должен понимать последствия.

## Сборка и ключи

- Private release key не входит в source, APK, обычные artifacts, mind maps или issue.
- CI loads signing bundle только после device smoke gates; очищает private files в finally/always step.
- Pinned certificate + applicationId + increasing versionCode защищают update identity. SHA256SUMS обеспечивает контроль bytes, не доверие к неизвестному источнику.
- Encrypted signing-backup artifact хранится отдельно; пароль не рядом с ним. Retention не заменяет owner's offline backup.
- Tracked legacy `release.keystore` отмечен в реестре как исторический артефакт; не считать его современным private pinned identity.
- Emulation third-party action pinned commit; локальные scripts/Go source pins/version markers нужны для supply-chain traceability. Dynamic assets/latest и исторические workflows — отдельные reproducibility limits.

## Отчёты об ошибках

Сообщать versionName/code, ABI, Android/API/device, steps, expected/actual и sanitized logs. Не публиковать целую подписку, QR с действующим token, private signing files или backup. Тестовые fixtures используют reserved/loopback endpoints, не пользовательские секреты.

Security checks уменьшают риски, но не являются проведённым pentest/audit всех upstream parsers, native protocols, plugins и OEM behavior. [Приёмка](tv-readiness-plan.md) и review dependencies остаются обязательными.

# Полный справочник XML-настроек

Сгенерирован из всех tracked `app/src/main/res/xml/*preferences.xml`. Это inventory ключей/schema, не обещание, что каждый наследованный XML сейчас имеет доступный экран. Visibility/validation определяются Kotlin/Java; runtime values пользователя не включаются. Defaults здесь — XML literals, не полный результат миграций DataStore.

Всего записей schema: **224**. Один ключ может присутствовать в разных редакторах; это не число независимых функций.
## anytls_preferences.xml

[Источник](../../app/src/main/res/xml/anytls_preferences.xml)

| Key | Название / resource | Тип UI | XML default |
|---|---|---|---|
| name | Profile Name | EditTextPreference | — |
| serverAddress | Server | EditTextPreference | — |
| serverPort | Remote Port | EditTextPreference | — |
| password | Password | EditTextPreference | — |
| sni | Server Name Indication | EditTextPreference | — |
| allowInsecure | Allow Insecure | SwitchPreference | — |
| alpn | Application-Layer Protocol Negotiation | EditTextPreference | — |
| certificates | Certificates | EditTextPreference | — |
| utlsFingerprint | uTLS fingerprint | SimpleMenuPreference |  |

## balancer_preferences.xml

[Источник](../../app/src/main/res/xml/balancer_preferences.xml)

| Key | Название / resource | Тип UI | XML default |
|---|---|---|---|
| profileName | Profile Name | EditTextPreference | — |
| balancerType | Type | SimpleMenuPreference | 0 |
| balancerStrategy | Strategy | SimpleMenuPreference | — |
| balancerGroup | Group | GroupPreference | — |

## config_preferences.xml

[Источник](../../app/src/main/res/xml/config_preferences.xml)

| Key | Название / resource | Тип UI | XML default |
|---|---|---|---|
| profileName | Profile Name | EditTextPreference | — |
| isOutboundOnly | The JSON set is outbound | SwitchPreference | — |
| serverConfig | Custom Config | EditConfigPreference | — |

## global_preferences.xml

[Источник](../../app/src/main/res/xml/global_preferences.xml)

| Key | Название / resource | Тип UI | XML default |
|---|---|---|---|
| isAutoConnect | Auto Connect | SwitchPreference | false |
| appTheme | Theme | ColorPickerPreference | — |
| nightTheme | Night Mode | SimpleMenuPreference | 0 |
| serviceMode | Service Mode | SimpleMenuPreference | vpn |
| tunImplementation | TUN Implementation | SimpleMenuPreference | 0 |
| mtu | MTU | MTUPreference | 9000 |
| speedInterval | Speed Notification Update Interval | SimpleMenuPreference | 1000 |
| profileTrafficStatistics | Profile Traffic Statistics | SwitchPreference | true |
| showDirectSpeed | Show Direct Speed | SwitchPreference | true |
| showGroupInNotification | Show group name in notification | SwitchPreference | — |
| alwaysShowAddress | Always Show Address | SwitchPreference | — |
| meteredNetwork | Metered Hint | SwitchPreference | — |
| acquireWakeLock | Acquire WakeLock | SwitchPreference | — |
| logLevel | Log Level | LongClickListPreference | 0 |
| globalCustomConfig | Custom Config | EditConfigPreference | — |
| proxyApps | Apps VPN mode | SwitchPreference | — |
| bypassLan | Bypass LAN | SwitchPreference | — |
| bypassLanInCore | Bypass LAN in Core | SwitchPreference | — |
| trafficSniffing | Enable Traffic Sniffing | SimpleMenuPreference | 1 |
| resolveDestination | Resolve Destination | SwitchPreference | — |
| ipv6Mode | IPv6 Route | SimpleMenuPreference | 0 |
| rulesProvider | Rule Assets Provider | SimpleMenuPreference | 0 |
| remoteDns | Remote DNS | EditTextPreference | https://dns.google/dns-query |
| domain_strategy_for_remote | Domain strategy for Remote | SimpleMenuPreference | auto |
| directDns | Direct DNS | EditTextPreference | https://223.5.5.5/dns-query |
| domain_strategy_for_direct | Domain strategy for Direct | SimpleMenuPreference | auto |
| domain_strategy_for_server | Domain strategy for Server address | SimpleMenuPreference | auto |
| enableDnsRouting | Enable DNS Routing | SwitchPreference | true |
| enableFakeDns | Enable FakeDNS | SwitchPreference | true |
| mixedPort | Proxy Port | EditTextPreference | — |
| appendHttpProxy | Append HTTP Proxy to VPN | SwitchPreference | false |
| allowAccess | Allow Connections from the LAN | SwitchPreference | — |
| connectionTestURL | Connection Test URL | UrlTestPreference | http://cp.cloudflare.com/ |
| enableClashAPI | Enable Clash API | SwitchPreference | — |
| networkChangeResetConnections | Reset outbound connections when network changes | SwitchPreference | true |
| wakeResetConnections | Reset outbound connections when device wake from sleep | SwitchPreference | — |
| globalAllowInsecure | Always allow insecure | SwitchPreference | — |
| allowInsecureOnRequest | Disable certificate checking when updating         subscriptions | SwitchPreference | — |
| appTLSVersion | Subscription Min TLS Version | SimpleMenuPreference | 1.2 |
| showBottomBar | Show bottom bar like SagerNet | SwitchPreference | — |

## group_preferences.xml

[Источник](../../app/src/main/res/xml/group_preferences.xml)

| Key | Название / resource | Тип UI | XML default |
|---|---|---|---|
| groupName | Group name | EditTextPreference | — |
| groupType | Group Type | SimpleMenuPreference | 0 |
| groupOrder | Order | SimpleMenuPreference | 0 |
| groupIsSelector | Use selector | SwitchPreference | — |
| groupFrontProxy | Front proxy | OutboundPreference | — |
| groupLandingProxy | Landing Proxy | OutboundPreference | — |
| groupSubscription | Subscription Settings | PreferenceCategory | — |
| subscriptionLink | Subscription Link | LinkOrContentPreference | — |
| subscriptionForceResolve | Force Resolve | SwitchPreference | — |
| subscriptionDeduplication | Deduplication | SwitchPreference | — |
| subscriptionUpdate | Update Settings | PreferenceCategory | — |
| subscriptionUpdateWhenConnectedOnly | Update only when connected | SwitchPreference | — |
| subscriptionUserAgent | UserAgent | UserAgentPreference | — |
| subscriptionAutoUpdate | Auto Update | SwitchPreference | — |
| subscriptionAutoUpdateDelay | Auto Update Delay (In minutes) | EditTextPreference | 1440 |

## hysteria_preferences.xml

[Источник](../../app/src/main/res/xml/hysteria_preferences.xml)

| Key | Название / resource | Тип UI | XML default |
|---|---|---|---|
| profileName | Profile Name | EditTextPreference | — |
| protocolVersion | Protocol Version | SimpleMenuPreference | 2 |
| serverAddress | Server | EditTextPreference | — |
| serverPorts | Remote Port | EditTextPreference | — |
| serverObfs | Obfuscation Password | EditTextPreference | — |
| serverAuthType | Authentication Type | SimpleMenuPreference | — |
| serverPassword | Authentication Payload | EditTextPreference | — |
| serverProtocol | Protocol | SimpleMenuPreference | https |
| serverSNI | Server Name Indication | EditTextPreference | — |
| serverALPN | Application-Layer Protocol Negotiation | EditTextPreference | — |
| serverCertificates | Certificates | EditTextPreference | — |
| serverAllowInsecure | Allow Insecure | SwitchPreference | — |
| serverUploadSpeed | Max Upload Speed (in Mbps) | EditTextPreference | — |
| serverDownloadSpeed | Max Download Speed (in Mbps) | EditTextPreference | — |
| serverStreamReceiveWindow | QUIC Stream Receive Window | EditTextPreference | — |
| serverConnectionReceiveWindow | QUIC Connection Receive Window | EditTextPreference | — |
| serverDisableMtuDiscovery | Disable Path MTU Discovery | SwitchPreference | — |
| hopInterval | Port Hopping Interval(second) | EditTextPreference | — |

## mieru_preferences.xml

[Источник](../../app/src/main/res/xml/mieru_preferences.xml)

| Key | Название / resource | Тип UI | XML default |
|---|---|---|---|
| profileName | Profile Name | EditTextPreference | — |
| serverAddress | Server | EditTextPreference | — |
| serverPort | Remote Port | EditTextPreference | — |
| serverProtocol | Protocol | SimpleMenuPreference | TCP |
| serverUsername | Username | EditTextPreference | — |
| serverPassword | Password | EditTextPreference | — |
| serverMTU | MTU | EditTextPreference | — |

## naive_preferences.xml

[Источник](../../app/src/main/res/xml/naive_preferences.xml)

| Key | Название / resource | Тип UI | XML default |
|---|---|---|---|
| profileName | Profile Name | EditTextPreference | — |
| serverAddress | Server | EditTextPreference | — |
| serverPort | Remote Port | EditTextPreference | — |
| serverUsername | Username (Optional) | EditTextPreference | — |
| serverPassword | Password (Optional) | EditTextPreference | — |
| serverProtocol | Protocol | SimpleMenuPreference | https |
| serverHeaders | Extra Headers | EditTextPreference | — |
| serverSNI | Server Name Indication | EditTextPreference | — |
| serverCertificates | Certificates | EditTextPreference | — |
| serverInsecureConcurrency | Insecure Concurrency | EditTextPreference | — |
| sUoT | UDP over TCP | SwitchPreference | — |

## name_preferences.xml

[Источник](../../app/src/main/res/xml/name_preferences.xml)

| Key | Название / resource | Тип UI | XML default |
|---|---|---|---|
| profileName | Profile Name | EditTextPreference | — |

## neko_preferences.xml

[Источник](../../app/src/main/res/xml/neko_preferences.xml)

| Key | Название / resource | Тип UI | XML default |
|---|---|---|---|
| name | Profile Name | EditTextPreference | — |

## route_preferences.xml

[Источник](../../app/src/main/res/xml/route_preferences.xml)

| Key | Название / resource | Тип UI | XML default |
|---|---|---|---|
| routeName | Route Name | EditTextPreference | — |
| serverConfig | Custom Config | EditConfigPreference | — |
| routePackages | Applications | AppListPreference | — |
| routeDomain | domain | EditTextPreference | — |
| routeIP | dst ip | EditTextPreference | — |
| routePort | dst port | EditTextPreference | — |
| routeSource | src ip | EditTextPreference | — |
| routeSourcePort | src port | EditTextPreference | — |
| routeNetwork | network | SimpleMenuPreference | — |
| routeProtocol | protocol | EditTextPreference | — |
| routeOutbound | outbound | OutboundPreference | — |

## shadowsocks_preferences.xml

[Источник](../../app/src/main/res/xml/shadowsocks_preferences.xml)

| Key | Название / resource | Тип UI | XML default |
|---|---|---|---|
| name | Profile Name | EditTextPreference | — |
| serverAddress | Server | EditTextPreference | — |
| serverPort | Remote Port | EditTextPreference | — |
| method | Encrypt Method | SimpleMenuPreference | — |
| password | Password | EditTextPreference | — |
| pluginName | Plugin | SimpleMenuPreference |  |
| pluginConfig | Configure… | EditTextPreference | — |
| sUoT | UDP over TCP | SwitchPreference | — |

## shadowtls_preferences.xml

[Источник](../../app/src/main/res/xml/shadowtls_preferences.xml)

| Key | Название / resource | Тип UI | XML default |
|---|---|---|---|
| name | Profile Name | EditTextPreference | — |
| serverAddress | Server | EditTextPreference | — |
| serverPort | Remote Port | EditTextPreference | — |
| version | Protocol Version | SimpleMenuPreference | — |
| password | Password | EditTextPreference | — |
| serverSecurityCategory | TLS Security Settings | PreferenceCategory | — |
| sni | Server Name Indication | EditTextPreference | — |
| alpn | Application-Layer Protocol Negotiation | EditTextPreference | — |
| certificates | Certificates | EditTextPreference | — |
| allowInsecure | Allow Insecure | SwitchPreference | — |
| utlsFingerprint | uTLS fingerprint | SimpleMenuPreference |  |

## socks_preferences.xml

[Источник](../../app/src/main/res/xml/socks_preferences.xml)

| Key | Название / resource | Тип UI | XML default |
|---|---|---|---|
| profileName | Profile Name | EditTextPreference | — |
| serverProtocol | Version | SimpleMenuPreference | 2 |
| serverAddress | Server | EditTextPreference | — |
| serverPort | Remote Port | EditTextPreference | — |
| serverUsername | Username (Optional) | EditTextPreference | — |
| serverPassword | Password (Optional) | EditTextPreference | — |
| sUoT | UDP over TCP | SwitchPreference | — |

## ssh_preferences.xml

[Источник](../../app/src/main/res/xml/ssh_preferences.xml)

| Key | Название / resource | Тип UI | XML default |
|---|---|---|---|
| profileName | Profile Name | EditTextPreference | — |
| serverAddress | Server | EditTextPreference | — |
| serverPort | Remote Port | EditTextPreference | — |
| serverUsername | Username | EditTextPreference | — |
| serverAuthType | Authentication Type | SimpleMenuPreference | — |
| serverPassword | Password | EditTextPreference | — |
| serverPrivateKey | Private Key | EditTextPreference | — |
| serverPassword1 | Private Key Passphrase | EditTextPreference | — |
| serverCertificates | Public Key | EditTextPreference | — |

## standard_v2ray_preferences.xml

[Источник](../../app/src/main/res/xml/standard_v2ray_preferences.xml)

| Key | Название / resource | Тип UI | XML default |
|---|---|---|---|
| name | Profile Name | EditTextPreference | — |
| serverAddress | Server | EditTextPreference | — |
| serverPort | Remote Port | EditTextPreference | — |
| username | Username (Optional) | EditTextPreference | — |
| password | Password (Optional) | EditTextPreference | — |
| uuid | User ID | EditTextPreference | — |
| alterId | Alter ID | EditTextPreference | — |
| encryption | Encryption | SimpleMenuPreference | — |
| packetEncoding | Packet Encoding | SimpleMenuPreference | — |
| type | Network | SimpleMenuPreference | — |
| host | HTTP Host | EditTextPreference | — |
| path | HTTP Path | EditTextPreference | — |
| security | Transport layer encryption | SimpleMenuPreference | — |
| serverWsCategory | WebSocket Settings | PreferenceCategory | — |
| wsMaxEarlyData | Max early data | EditTextPreference | 0 |
| earlyDataHeaderName | Early Data Header Name | EditTextPreference | — |
| serverSecurityCategory | TLS Security Settings | PreferenceCategory | — |
| sni | Server Name Indication | EditTextPreference | — |
| alpn | Application-Layer Protocol Negotiation | EditTextPreference | — |
| certificates | Certificates | EditTextPreference | — |
| allowInsecure | Allow Insecure | SwitchPreference | — |
| serverTlsCamouflageCategory | TLS Camouflage Settings | PreferenceCategory | — |
| utlsFingerprint | uTLS fingerprint | SimpleMenuPreference |  |
| realityPubKey | Reality Public Key | EditTextPreference | — |
| realityShortId | Reality ShortId | EditTextPreference | — |
| serverMuxCategory | Mulitplex | PreferenceCategory | — |
| enableMux | Enable Multiplexer | SwitchPreference | — |
| muxType | Mux protocol | SimpleMenuPreference | 0 |
| muxConcurrency | Mux Concurrent Connections | EditTextPreference | 1 |
| muxPadding | Padding | SwitchPreference | — |
| serverECHCategory | ECH | PreferenceCategory | — |
| enableECH | Enable | SwitchPreference | — |
| echConfig | ECH Config | EditTextPreference | — |

## trojan_go_preferences.xml

[Источник](../../app/src/main/res/xml/trojan_go_preferences.xml)

| Key | Название / resource | Тип UI | XML default |
|---|---|---|---|
| profileName | Profile Name | EditTextPreference | — |
| serverAddress | Server | EditTextPreference | — |
| serverPort | Remote Port | EditTextPreference | — |
| serverPassword | Password | EditTextPreference | — |
| serverSNI | Server Name Indication | EditTextPreference | — |
| serverAllowInsecure | Allow Insecure | SwitchPreference | — |
| serverNetwork | Network | SimpleMenuPreference | none |
| serverEncryption | Encryption | SimpleMenuPreference | — |
| serverWsCategory | WebSocket Settings | PreferenceCategory | — |
| serverHost | WebSocket Host | EditTextPreference | — |
| serverPath | WebSocket Path | EditTextPreference | — |
| serverSsCategory | Shadowsocks Settings | PreferenceCategory | — |
| serverMethod | Encrypt Method | SimpleMenuPreference | AES-128-GCM |
| serverPassword1 | Password | EditTextPreference | — |

## tuic_preferences.xml

[Источник](../../app/src/main/res/xml/tuic_preferences.xml)

| Key | Название / resource | Тип UI | XML default |
|---|---|---|---|
| profileName | Profile Name | EditTextPreference | — |
| serverAddress | Server | EditTextPreference | — |
| serverPort | Remote Port | EditTextPreference | — |
| serverUsername | User ID | EditTextPreference | — |
| serverPassword | Password | EditTextPreference | — |
| serverALPN | Application-Layer Protocol Negotiation | EditTextPreference | — |
| serverCertificates | Certificates | EditTextPreference | — |
| serverUDPRelayMode | UDP Relay Mode | SimpleMenuPreference | https |
| serverCongestionController | Congestion Controller | SimpleMenuPreference | https |
| serverDisableSNI | Disable SNI | SwitchPreference | — |
| serverSNI | Server Name Indication | EditTextPreference | — |
| serverReduceRTT | Enable 0-RTT QUIC handshake | SwitchPreference | — |
| serverAllowInsecure | Allow Insecure | SwitchPreference | — |

## wireguard_preferences.xml

[Источник](../../app/src/main/res/xml/wireguard_preferences.xml)

| Key | Название / resource | Тип UI | XML default |
|---|---|---|---|
| name | Profile Name | EditTextPreference | — |
| serverAddress | Server | EditTextPreference | — |
| serverPort | Remote Port | EditTextPreference | — |
| localAddress | Local Address | EditTextPreference | — |
| privateKey | Private Key | EditTextPreference | — |
| peerPublicKey | Peer Public Key | EditTextPreference | — |
| peerPreSharedKey | Peer Pre-Shared Key | EditTextPreference | — |
| mtu | MTU | EditTextPreference | 1420 |
| reserved | Reserved | EditTextPreference | — |

package io.nekohasekai.sagernet.group

import java.util.Locale

/** Advisory classification only: no retry, certificate bypass, or raw message retention. */
enum class SubscriptionFailureCategory(val code: String) {
    TLS_TIME("SUB_TLS_TIME"), TLS_TRUST("SUB_TLS_TRUST"), REALITY("SUB_REALITY"),
    UNSUPPORTED("SUB_UNSUPPORTED"), DNS("SUB_DNS"), TIMEOUT("SUB_TIMEOUT"),
    HTTP("SUB_HTTP"), EMPTY("SUB_EMPTY"), UNKNOWN("SUB_UNKNOWN");

    companion object {
        fun fromMessage(message: String?): SubscriptionFailureCategory {
            val text = message.orEmpty().take(16_384).lowercase(Locale.ROOT)
            val tls = listOf("x509", "certificate", "tls", "сертификат").any { it in text }
            return when {
                tls && listOf("has expired", "certificate expired", "not yet valid", "истёк", "истек").any { it in text } -> TLS_TIME
                "reality verification failed" in text -> REALITY
                "unsupported v2ray transport" in text || "unsupported transport" in text -> UNSUPPORTED
                tls && listOf("failed to verify", "unknown authority", "untrusted", "hostname", "certificate is valid for", "handshakeexception").any { it in text } -> TLS_TRUST
                listOf("no such host", "unknownhostexception", "name resolution", "dns lookup failed").any { it in text } -> DNS
                listOf("timeout", "timed out", "deadline exceeded").any { it in text } -> TIMEOUT
                Regex("""(?:http(?:/[^ ]+)?\s+|status(?: code)?[ :=]+)[45][0-9]{2}\b""").containsMatchIn(text) -> HTTP
                listOf("no proxies found", "no profiles found", "не найдено профилей", "не найдены профили", "нет профилей").any { it in text } -> EMPTY
                else -> UNKNOWN
            }
        }
    }
}

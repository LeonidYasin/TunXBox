package io.nekohasekai.sagernet.group

import java.util.Locale

/** Advisory classification only: no retry, certificate bypass, or raw message retention. */
enum class SubscriptionFailureCategory(val code: String) {
    TLS_TIME("SUB_TLS_TIME"), TLS_TRUST("SUB_TLS_TRUST"), REALITY("SUB_REALITY"),
    UNSUPPORTED("SUB_UNSUPPORTED"), DNS("SUB_DNS"), TIMEOUT("SUB_TIMEOUT"),
    HTTP("SUB_HTTP"), EMPTY("SUB_EMPTY"), LOCAL_SAVE("SUB_LOCAL_SAVE"), CHANGED("SUB_CHANGED"), CLOSED("SUB_CLOSED"), REFUSED("SUB_REFUSED"), NETWORK("SUB_NETWORK"), TLS_HANDSHAKE("SUB_TLS_HANDSHAKE"),
    CORE("SUB_CORE"), BUSY("SUB_BUSY"), INVALID("SUB_INVALID"), UNKNOWN("SUB_UNKNOWN");

    companion object {
        /** Inspect bounded cause/suppressed chains without persisting their raw messages. */
        fun fromThrowable(error: Throwable): SubscriptionFailureCategory {
            val seen = java.util.Collections.newSetFromMap(java.util.IdentityHashMap<Throwable, Boolean>())
            val queue = java.util.ArrayDeque<Throwable>().apply { add(error) }
            val found = mutableSetOf<SubscriptionFailureCategory>()
            while (queue.isNotEmpty() && seen.size < 16) {
                val next = queue.removeFirst(); if (!seen.add(next)) continue
                if (next is SubscriptionUpdateFailure) found.add(next.category)
                found.add(fromMessage(next.message))
                when (next) {
                    is java.net.UnknownHostException -> found.add(DNS)
                    is java.net.MalformedURLException, is java.net.URISyntaxException -> found.add(INVALID)
                    is java.net.SocketTimeoutException -> found.add(TIMEOUT)
                    is java.io.EOFException -> found.add(CLOSED)
                    is javax.net.ssl.SSLHandshakeException -> found.add(TLS_HANDSHAKE)
                }
                next.cause?.let { if (seen.size < 16) queue.add(it) }
                next.suppressed.take(4).forEach(queue::add)
            }
            // Specific certificate/REALITY causes outrank outer timeout/HTTP fallbacks.
            return values().firstOrNull { it != UNKNOWN && it in found } ?: UNKNOWN
        }

        fun fromMessage(message: String?): SubscriptionFailureCategory {
            val text = message.orEmpty().take(16_384).lowercase(Locale.ROOT)
            values().firstOrNull { text.trim().endsWith(it.code.lowercase(Locale.ROOT)) && text.startsWith("[sub_stage_") }?.let { return it }
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
                "subscription changed during update" in text -> CHANGED
                Regex("""\b(?:unexpected eof|eof|connection reset|connection closed|broken pipe|stream reset)\b""").containsMatchIn(text) -> CLOSED
                "connection refused" in text || "econnrefused" in text || "fail connect socks5" in text -> REFUSED
                listOf("network is unreachable", "no route to host", "enetunreach", "ehostunreach").any { it in text } -> NETWORK
                tls && listOf("handshake failure", "handshake failed", "protocol version", "crypto_error", "handshakeexception").any { it in text } -> TLS_HANDSHAKE
                "core not started" in text || "not started" == text.trim() -> CORE
                "invalid url" in text || "unsupported protocol scheme" in text -> INVALID
                else -> UNKNOWN
            }
        }
    }
}

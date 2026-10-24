package io.nekohasekai.sagernet.fmt.v2ray

/** Safe diagnostic: do not echo arbitrary URI parameters or a full config. */
object UnsupportedV2RayTransport {
    fun reject(type: String?): Nothing {
        val label = when (type?.lowercase()) {
            "xhttp" -> "XHTTP"
            "splithttp" -> "SplitHTTP"
            "kcp", "mkcp" -> "mKCP"
            "domainsocket" -> "DomainSocket"
            else -> "unknown"
        }
        throw IllegalArgumentException(
            "Unsupported V2Ray transport: $label. " +
                "The current core cannot use this profile; no TCP fallback was applied."
        )
    }
}

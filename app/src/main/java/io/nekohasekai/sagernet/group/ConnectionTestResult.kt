package io.nekohasekai.sagernet.group

/** A bounded URL check using the already-running core, not a DNS/UDP/full Internet certificate. */
data class ConnectionTestResult(val profileId: Long, val attemptedAt: Long, val elapsedMs: Int,
    val category: SubscriptionFailureCategory? = null) {
    val available get() = category == null && elapsedMs >= 0
    fun encode() = listOf("1", profileId, attemptedAt, elapsedMs, category?.name ?: "-").joinToString("|")
    companion object {
        fun decode(text: String?): ConnectionTestResult? = runCatching {
            require(text != null && text.length <= 180)
            val p = text.split('|'); require(p.size == 5 && p[0] == "1")
            val r = ConnectionTestResult(p[1].toLong(), p[2].toLong(), p[3].toInt(),
                if (p[4] == "-") null else SubscriptionFailureCategory.valueOf(p[4]))
            require(r.profileId >= 0 && r.attemptedAt > 0 && (r.category == null && r.elapsedMs >= 0 || r.category != null && r.elapsedMs == -1))
            r
        }.getOrNull()
    }
}

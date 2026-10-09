package io.nekohasekai.sagernet.group

/** No provider strings, profile names, URLs, headers, keys or raw exceptions belong here. */
enum class SubscriptionUpdateStage { UPDATE, DOWNLOAD, PARSE, RESOLVE, APPLY }
enum class SubscriptionUpdateOutcome { SUCCESS, FAILURE, CANCELLED }
data class SubscriptionUpdateResult(val outcome: SubscriptionUpdateOutcome, val stage: SubscriptionUpdateStage,
    val attemptedAt: Long, val durationMs: Long, val profiles: Int = 0,
    val category: SubscriptionFailureCategory? = null) {
    fun encode(): String = listOf("1", outcome.name, stage.name, attemptedAt, durationMs, profiles, category?.name ?: "-").joinToString("|")
    companion object {
        fun decode(text: String?): SubscriptionUpdateResult? = runCatching {
            require(text != null && text.length <= 180)
            val parts = text.split('|'); require(parts.size == 7 && parts[0] == "1")
            val record = SubscriptionUpdateResult(SubscriptionUpdateOutcome.valueOf(parts[1]),
                SubscriptionUpdateStage.valueOf(parts[2]), parts[3].toLong(), parts[4].toLong(), parts[5].toInt(),
                if (parts[6] == "-") null else SubscriptionFailureCategory.valueOf(parts[6]))
            require(record.attemptedAt > 0 && record.durationMs >= 0 && record.profiles >= 0)
            require((record.outcome == SubscriptionUpdateOutcome.FAILURE) == (record.category != null))
            record
        }.getOrNull()
    }
}

/** Stage-specific safe envelope. Raw cause is retained only in memory, never in the journal/UI. */
class SubscriptionUpdateFailure(val stage: SubscriptionUpdateStage, val category: SubscriptionFailureCategory,
    cause: Exception) : Exception("[SUB_STAGE_${stage.name}] ${category.code}", cause)

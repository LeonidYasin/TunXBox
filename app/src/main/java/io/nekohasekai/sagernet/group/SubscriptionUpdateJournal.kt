package io.nekohasekai.sagernet.group

import android.content.Context
import io.nekohasekai.sagernet.ktx.app

/** One completed attempt per group, at most 64 groups. Not a raw log or a tunnel health report. */
object SubscriptionUpdateJournal {
    private const val MAX_GROUPS = 64
    private fun preferences() = app.getSharedPreferences("subscription_update_results_v1", Context.MODE_PRIVATE)
    @Synchronized fun read(groupId: Long): SubscriptionUpdateResult? = runCatching {
        if (groupId <= 0) null else SubscriptionUpdateResult.decode(preferences().getString(groupId.toString(), null))
    }.getOrNull()
    @Synchronized fun write(groupId: Long, result: SubscriptionUpdateResult) {
        if (groupId <= 0 || SubscriptionUpdateResult.decode(result.encode()) == null) return
        // Failure to write optional diagnostics must not turn a committed DB update into a failure.
        runCatching {
            val prefs = preferences()
            val entries = prefs.all.mapNotNull { (key, value) ->
                key.toLongOrNull()?.takeIf { it > 0 }?.let { id ->
                    SubscriptionUpdateResult.decode(value as? String)?.let { id to it }
                }
            }.toMap().toMutableMap().apply { put(groupId, result) }
            val keep = (entries.entries.filter { it.key != groupId }.sortedByDescending { it.value.attemptedAt }
                .take(MAX_GROUPS - 1).map { it.key.toString() } + groupId.toString()).toSet()
            prefs.edit().apply {
                prefs.all.keys.filter { it !in keep }.forEach { remove(it) }
                if (groupId.toString() in keep) putString(groupId.toString(), result.encode())
            }.commit()
        }
    }
    @Synchronized fun remove(groupId: Long) { runCatching { preferences().edit().remove(groupId.toString()).commit() } }
}

package io.nekohasekai.sagernet.group

import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext

/** A cancelled confirmation or network operation must release the per-group update lock. */
object SubscriptionUpdateGuard {
    suspend fun withUpdateLock(groupId: Long, inFlight: MutableSet<Long>, onFinish: suspend () -> Unit, work: suspend () -> Boolean): Boolean {
        if (!inFlight.add(groupId)) return false
        try { return work() }
        finally { withContext(NonCancellable) { inFlight.remove(groupId); onFinish() } }
    }
}

package io.nekohasekai.sagernet.group

import kotlinx.coroutines.*
import java.io.Closeable
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.resume

/** Binder calls are blocking and are NOT made cancellable by withContext(IO).
 * One isolated daemon worker; timeout/cancellation stops awaiting, never the VPN.
 * If native/Binder is stuck, no new worker is created and repeats return BUSY.
 */
class BoundedConnectionCheck: Closeable {
    companion object { const val LIMIT_MS=8000L;val shared=BoundedConnectionCheck() }
    private val busy=AtomicBoolean(false)
    private val worker=Executors.newSingleThreadExecutor {task->Thread(task,"connection-check").apply {isDaemon=true}}
    suspend fun check(profile: Long,limitMs: Long=LIMIT_MS,work: ()->ConnectionTestResult): ConnectionTestResult {
        require(limitMs>0)
        fun failed(category: SubscriptionFailureCategory)=ConnectionTestResult(profile,System.currentTimeMillis(),-1,category)
        if(!busy.compareAndSet(false,true))return failed(SubscriptionFailureCategory.BUSY)
        return try {
            withTimeout(limitMs) {
                suspendCancellableCoroutine {continuation ->
                    try {worker.execute {
                        try {
                            val result=try {work()}catch(error: Exception){failed(SubscriptionFailureCategory.fromThrowable(error))}
                            if(continuation.isActive)continuation.resume(result)
                        } finally {busy.set(false)}
                    }} catch(error: Exception) {busy.set(false);if(continuation.isActive)continuation.resume(failed(SubscriptionFailureCategory.CORE))}
                }
            }
        } catch(_: TimeoutCancellationException) {failed(SubscriptionFailureCategory.TIMEOUT)}
    }
    override fun close(){worker.shutdownNow()}
}

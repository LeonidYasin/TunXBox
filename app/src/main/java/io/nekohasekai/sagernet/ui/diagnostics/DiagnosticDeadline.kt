package io.nekohasekai.sagernet.ui.diagnostics

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.withTimeoutOrNull
import io.nekohasekai.sagernet.group.SubscriptionFailureCategory

/** Only our total deadline is converted to evidence; caller cancellation still propagates. */
object DiagnosticDeadline {
    suspend fun collect(steps: MutableList<ExtendedConnectionDiagnostics.Step>, limitMs: Long,
        work: suspend CoroutineScope.()->Unit) {
        require(limitMs>0)
        val completed=withTimeoutOrNull(limitMs) {work();true}
        if(completed==null)steps.add(ExtendedConnectionDiagnostics.Step("TOTAL_LIMIT","TIMEOUT",limitMs,SubscriptionFailureCategory.TIMEOUT))
    }
}

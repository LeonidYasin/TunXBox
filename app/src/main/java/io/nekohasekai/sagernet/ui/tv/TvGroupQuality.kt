package io.nekohasekai.sagernet.ui.tv

import io.nekohasekai.sagernet.group.SubscriptionFailureCategory

/** Small, comparable URL sample; not throughput, ICMP jitter or UDP packet loss. */
data class TvQualityResult(val id: Long,val samples: List<Int>,val tcpMs: Int?,
    val failure: SubscriptionFailureCategory?,val attemptedAt: Long) {
    val successes get()=samples.count {it>=0}
    val medianMs get()=samples.filter {it>=0}.sorted().let {if(it.isEmpty())Int.MAX_VALUE else it[it.size/2]}
    val spreadMs get()=samples.filter {it>=0}.let {if(it.size<2)null else it.max()-it.min()}
}
object TvGroupQuality {
    const val SAMPLES=3
    const val PROFILE_LIMIT_MS=25000L
    const val GROUP_LIMIT_MS=300000L
    fun ranked(results: List<TvQualityResult>): List<TvQualityResult> = results.sortedWith(
        compareByDescending<TvQualityResult> {it.successes}.thenBy {it.medianMs}
            .thenBy {it.spreadMs?:Int.MAX_VALUE}) // stable ties retain prior order
}

package io.nekohasekai.sagernet.ui.diagnostics

import android.content.Context
import io.nekohasekai.sagernet.R
import io.nekohasekai.sagernet.group.SubscriptionFailureCategory as C

/** Localized explanations are separate from the stable machine-readable evidence. */
object DiagnosticPresentation {
    fun stage(context: Context, code: String): String = context.getString(when {
        code == "CONFIG_COMPATIBILITY" -> R.string.diagnostic_stage_config
        code == "PHYSICAL_BINDING" -> R.string.diagnostic_stage_network
        code == "ENDPOINT_DNS" -> R.string.diagnostic_stage_dns
        code.startsWith("ENDPOINT_TCP") -> R.string.diagnostic_stage_tcp
        code == "ANDROID_STRICT_TLS" -> R.string.diagnostic_stage_tls
        code.matches(Regex("PROFILE_URL_[1-3]")) -> R.string.diagnostic_stage_url
        else -> R.string.diagnostic_stage_other
    })
    fun next(context: Context, category: C?): String = context.getString(when(category) {
        null -> R.string.diagnostic_next_pass
        C.TLS_TIME -> R.string.diagnostic_next_clock
        C.TLS_TRUST, C.TLS_HANDSHAKE, C.REALITY -> R.string.diagnostic_next_tls
        C.DNS, C.NETWORK -> R.string.diagnostic_next_network
        C.BUSY -> R.string.diagnostic_next_busy
        C.UNSUPPORTED, C.INVALID -> R.string.diagnostic_next_config
        C.CHANGED -> R.string.diagnostic_next_changed
        else -> R.string.diagnostic_next_unknown
    })
    fun summary(context: Context, record: ConnectionDiagnostic?): String = buildString {
        appendLine(context.getString(R.string.diagnostic_plain_title))
        if(record == null) appendLine(context.getString(R.string.diagnostic_none)) else {
            appendLine(context.getString(if(record.category == null) R.string.diagnostic_plain_pass else R.string.diagnostic_plain_failure))
            appendLine(context.getString(R.string.diagnostic_attempt_time,
                java.text.DateFormat.getDateTimeInstance().format(java.util.Date(record.at))))
            if(record.category != null) appendLine(ConnectionDiagnosticReport.reason(context, record.category))
            appendLine(next(context,record.category))
        }
        appendLine(context.getString(R.string.diagnostic_snapshot_scope))
    }
}

/** Closed vocabulary for export; future/fixture strings must not become a secret channel. */
object DiagnosticVocabulary {
    private val stages=setOf("CONFIG_COMPATIBILITY","PHYSICAL_BINDING","ENDPOINT_DNS","ENDPOINT_TCP","ENDPOINT_TCP_V4","ENDPOINT_TCP_V6","ANDROID_STRICT_TLS","PROFILE_URL_1","PROFILE_URL_2","PROFILE_URL_3","PROFILE_URL_SERIES","TOTAL_LIMIT")
    private val outcomes=setOf("PASS","FAIL","TIMEOUT","SKIPPED_WORKER_BUSY","SKIPPED_NETWORK_CHANGED","SKIPPED_NO_UNIQUE_PHYSICAL_NETWORK","SKIPPED_NO_BINDING","SKIPPED_NOT_APPLICABLE_OR_DNS_FAILED","SKIPPED_NOT_COMPARABLE_OR_NOT_APPLICABLE","SKIPPED_CONFIG_FAILURE")
    fun stage(value: String)=value.takeIf {it in stages}?:"OTHER"
    fun outcome(value: String)=value.takeIf {it in outcomes}?:"UNKNOWN"
    fun network(value: String)=value.takeIf {it in setOf("WIFI","ETHERNET","CELLULAR","OTHER","NONE_OR_AMBIGUOUS")}?:"OTHER"
}

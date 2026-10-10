package io.nekohasekai.sagernet.group

import android.content.Context
import io.nekohasekai.sagernet.R

/** Only fixed, localized guidance is shown. URLs, credentials and raw exceptions are not echoed. */
object SubscriptionFailurePresentation {
    fun message(context: Context, failure: String?): String {
        val category = SubscriptionFailureCategory.fromMessage(failure)
        val reason = reason(context, category)
        val stage = SubscriptionUpdateStage.values().firstOrNull { failure?.startsWith("[SUB_STAGE_${it.name}]") == true }
        val stageMessage = stage?.let { context.getString(R.string.subscription_attempt_stage, SubscriptionUpdatePresentation.stage(context, it)) }
            ?: context.getString(R.string.subscription_failure_stage)
        return stageMessage + "\n\n" + reason + "\n\n" +
            context.getString(R.string.subscription_failure_code, category.code)
    }
    fun reason(context: Context, category: SubscriptionFailureCategory): String = context.getString(when (category) {
            SubscriptionFailureCategory.TLS_TIME -> R.string.subscription_failure_tls_time
            SubscriptionFailureCategory.TLS_TRUST -> R.string.subscription_failure_tls_trust
            SubscriptionFailureCategory.REALITY -> R.string.subscription_failure_reality
            SubscriptionFailureCategory.UNSUPPORTED -> R.string.subscription_failure_unsupported
            SubscriptionFailureCategory.DNS -> R.string.subscription_failure_dns
            SubscriptionFailureCategory.TIMEOUT -> R.string.subscription_failure_timeout
            SubscriptionFailureCategory.HTTP -> R.string.subscription_failure_http
            SubscriptionFailureCategory.EMPTY -> R.string.subscription_failure_empty
            SubscriptionFailureCategory.LOCAL_SAVE -> R.string.subscription_failure_local_save
            SubscriptionFailureCategory.CHANGED -> R.string.subscription_failure_changed
            SubscriptionFailureCategory.CLOSED -> R.string.failure_closed
            SubscriptionFailureCategory.REFUSED -> R.string.failure_refused
            SubscriptionFailureCategory.NETWORK -> R.string.failure_network
            SubscriptionFailureCategory.TLS_HANDSHAKE -> R.string.failure_handshake
            SubscriptionFailureCategory.CORE -> R.string.failure_core
            SubscriptionFailureCategory.BUSY -> R.string.failure_busy
            SubscriptionFailureCategory.INVALID -> R.string.failure_invalid
            SubscriptionFailureCategory.UNKNOWN -> R.string.subscription_failure_unknown
        })
}

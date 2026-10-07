package io.nekohasekai.sagernet.group

import android.content.Context
import io.nekohasekai.sagernet.R

/** Only fixed, localized guidance is shown. URLs, credentials and raw exceptions are not echoed. */
object SubscriptionFailurePresentation {
    fun message(context: Context, failure: String?): String {
        val category = SubscriptionFailureCategory.fromMessage(failure)
        val reason = when (category) {
            SubscriptionFailureCategory.TLS_TIME -> R.string.subscription_failure_tls_time
            SubscriptionFailureCategory.TLS_TRUST -> R.string.subscription_failure_tls_trust
            SubscriptionFailureCategory.REALITY -> R.string.subscription_failure_reality
            SubscriptionFailureCategory.UNSUPPORTED -> R.string.subscription_failure_unsupported
            SubscriptionFailureCategory.DNS -> R.string.subscription_failure_dns
            SubscriptionFailureCategory.TIMEOUT -> R.string.subscription_failure_timeout
            SubscriptionFailureCategory.HTTP -> R.string.subscription_failure_http
            SubscriptionFailureCategory.EMPTY -> R.string.subscription_failure_empty
            SubscriptionFailureCategory.UNKNOWN -> R.string.subscription_failure_unknown
        }
        return context.getString(R.string.subscription_failure_stage) + "\n\n" +
            context.getString(reason) + "\n\n" +
            context.getString(R.string.subscription_failure_code, category.code)
    }
}

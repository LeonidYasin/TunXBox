package io.nekohasekai.sagernet.group

import android.content.Context
import androidx.appcompat.app.AlertDialog
import io.nekohasekai.sagernet.R
import io.nekohasekai.sagernet.ktx.app

object ConnectionTestPresentation {
    private fun prefs() = app.getSharedPreferences("connection_test_result_v1", Context.MODE_PRIVATE)
    fun save(result: ConnectionTestResult) { prefs().edit().putString("last", result.encode()).apply() }
    fun read(profile: Long) = ConnectionTestResult.decode(prefs().getString("last", null))?.takeIf { it.profileId == profile }
    fun summary(context: Context, result: ConnectionTestResult): String = if (result.available)
        context.getString(R.string.network_test_ok, result.elapsedMs)
        else context.getString(R.string.network_test_failed, "NET_${result.category!!.name}")
    fun details(context: Context, result: ConnectionTestResult): String = summary(context, result) +
        "\n\n" + java.text.DateFormat.getDateTimeInstance().format(java.util.Date(result.attemptedAt)) +
        (result.category?.let { "\n\n" + when (it) {
            SubscriptionFailureCategory.DNS -> context.getString(R.string.network_reason_dns)
            SubscriptionFailureCategory.TIMEOUT -> context.getString(R.string.network_reason_timeout)
            SubscriptionFailureCategory.HTTP -> context.getString(R.string.network_reason_http)
            SubscriptionFailureCategory.UNKNOWN -> context.getString(R.string.network_reason_unknown)
            else -> SubscriptionFailurePresentation.reason(context, it)
        } } ?: "") +
        "\n\n" + context.getString(R.string.network_test_scope)
    fun show(context: Context, result: ConnectionTestResult): AlertDialog = AlertDialog.Builder(context)
        .setTitle(R.string.network_test_title).setMessage(details(context, result))
        .setPositiveButton(android.R.string.ok, null).show().also { SubscriptionUpdatePresentation.focusClose(it) }
}

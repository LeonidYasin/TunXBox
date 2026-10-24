package io.nekohasekai.sagernet.group

import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import io.nekohasekai.sagernet.R
import java.text.DateFormat
import java.util.Date

object SubscriptionUpdatePresentation {
    fun stage(context: Context, stage: SubscriptionUpdateStage): String = context.getString(when (stage) {
        SubscriptionUpdateStage.UPDATE -> R.string.subscription_stage_update
        SubscriptionUpdateStage.DOWNLOAD -> R.string.subscription_stage_download
        SubscriptionUpdateStage.PARSE -> R.string.subscription_stage_parse
        SubscriptionUpdateStage.RESOLVE -> R.string.subscription_stage_resolve
        SubscriptionUpdateStage.APPLY -> R.string.subscription_stage_apply
    })
    fun summary(context: Context, record: SubscriptionUpdateResult): String = context.getString(
        R.string.subscription_attempt_summary, DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(Date(record.attemptedAt)),
        context.getString(when (record.outcome) {
            SubscriptionUpdateOutcome.SUCCESS -> R.string.subscription_attempt_success
            SubscriptionUpdateOutcome.FAILURE -> R.string.subscription_attempt_failure
            SubscriptionUpdateOutcome.CANCELLED -> R.string.subscription_attempt_cancelled
        }))
    fun details(context: Context, record: SubscriptionUpdateResult?): String {
        if (record == null) return context.getString(R.string.subscription_attempt_none)
        var text = summary(context, record) + "\n" + context.getString(R.string.subscription_attempt_stage, stage(context, record.stage)) +
            "\n" + context.getString(R.string.subscription_attempt_duration, record.durationMs)
        if (record.outcome == SubscriptionUpdateOutcome.SUCCESS) text += "\n\n" + context.getString(R.string.subscription_attempt_profiles, record.profiles)
        record.category?.let { text += "\n\n" + SubscriptionFailurePresentation.message(context, "[SUB_STAGE_${record.stage.name}] ${it.code}") }
        return text + "\n\n" + context.getString(R.string.subscription_attempt_privacy)
    }
    /** This is a whitelist report, NOT sanitization of arbitrary existing native logs. */
    @Suppress("DEPRECATION")
    fun report(context: Context, record: SubscriptionUpdateResult): String {
        val version = runCatching { context.packageManager.getPackageInfo(context.packageName, 0) }.getOrNull()
        val versionCode = if (Build.VERSION.SDK_INT >= 28) version?.longVersionCode else version?.versionCode?.toLong()
        return ("TunXBox subscription update summary\nAPK: ${version?.versionName ?: "—"} (${versionCode ?: 0})" +
            "\nAndroid API: ${Build.VERSION.SDK_INT}\nDevice supported ABIs: ${Build.SUPPORTED_ABIS.joinToString(", ")}" +
            "\nAttempt timestamp (epoch ms): ${record.attemptedAt}\n\n" + details(context, record)).take(8192)
    }
    /** Real Android may assign message-scroll focus during window layout after show(). */
    fun focusClose(dialog: android.app.Dialog) {
        val close = dialog.findViewById<android.widget.Button>(android.R.id.button1) ?: return
        close.isFocusable = true
        close.isFocusableInTouchMode = true
        close.requestFocus()
        close.post { if (dialog.isShowing) close.requestFocus() }
    }
    fun share(context: Context, record: SubscriptionUpdateResult) {
        val intent = Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, report(context, record))
        try {
            context.startActivity(Intent.createChooser(intent, context.getString(R.string.subscription_attempt_share))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        } catch (_: android.content.ActivityNotFoundException) {
            Toast.makeText(context, R.string.subscription_attempt_no_share, Toast.LENGTH_LONG).show()
        }
    }

}

package io.nekohasekai.sagernet.ui

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import com.jakewharton.processphoenix.ProcessPhoenix
import io.nekohasekai.sagernet.R
import io.nekohasekai.sagernet.ui.tv.TvUiPreferences

/** UI task/main process only. The separate :bg VPN service is deliberately not stopped. */
object AppLifecycleActions {
    fun restartIntent(context: Context, phoneMode: Boolean = TvUiPreferences.phoneMode): Intent =
        Intent(context, if (phoneMode) MainActivity::class.java else MainActivityTv::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            if (phoneMode) putExtra("force_phone_mode", true)
        }
    fun confirm(activity: Activity, restart: Boolean): AlertDialog {
        val title = if (restart) R.string.app_restart else R.string.app_close
        return AlertDialog.Builder(activity).setTitle(title)
            .setMessage(if (restart) R.string.app_restart_confirm else R.string.app_close_confirm)
            .setPositiveButton(title) { _, _ ->
                if (restart) ProcessPhoenix.triggerRebirth(activity, restartIntent(activity))
                else activity.finishAndRemoveTask()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .create().also { dialog ->
                dialog.setOnShowListener { dialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.requestFocus() }
                dialog.show()
            }
    }
}

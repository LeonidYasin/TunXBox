package io.nekohasekai.sagernet.ui

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import io.nekohasekai.sagernet.R
import moe.matsuri.nb4a.utils.SendLog

/** Crash recovery is a user-controlled screen, never an automatic share/restart loop. */
class BlankActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!intent.hasExtra("sendLog")) { finish(); return }
        AlertDialog.Builder(this)
            .setTitle(R.string.crash_recovery_title)
            .setMessage(R.string.crash_recovery_message)
            .setPositiveButton(R.string.crash_recovery_retry) { _, _ ->
                startActivity(Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
                finish()
            }
            .setNegativeButton(R.string.crash_recovery_close) { _, _ -> finishAndRemoveTask() }
            .setNeutralButton(R.string.crash_recovery_share, null)
            .setOnCancelListener { finishAndRemoveTask() }
            .create().also { dialog ->
                dialog.setOnShowListener {
                    dialog.getButton(AlertDialog.BUTTON_NEGATIVE).requestFocus()
                    // Do not dismiss recovery when the user cancels the Android chooser.
                    dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener {
                        try { SendLog.sendLog(this, "TunXBox Crash") }
                        catch (_: Exception) { Toast.makeText(this, R.string.crash_recovery_share_failed, Toast.LENGTH_LONG).show() }
                    }
                }
                dialog.show()
            }
    }
}

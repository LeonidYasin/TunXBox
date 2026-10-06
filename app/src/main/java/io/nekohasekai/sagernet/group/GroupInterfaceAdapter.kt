package io.nekohasekai.sagernet.group

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.nekohasekai.sagernet.R
import io.nekohasekai.sagernet.database.GroupManager
import io.nekohasekai.sagernet.database.ProxyGroup
import io.nekohasekai.sagernet.ui.ThemedActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

/** Upstream phone presentation, with main-thread and activity-lifetime safeguards. */
class GroupInterfaceAdapter(val context: ThemedActivity) : GroupManager.Interface {
    private fun alive() = !context.isFinishing && !context.isDestroyed
    private suspend fun question(title: Int, message: String, confirm: Boolean): Boolean = withContext(Dispatchers.Main) {
        if (!alive()) return@withContext false
        suspendCancellableCoroutine { continuation ->
            var finished = false
            var observer: LifecycleEventObserver? = null
            val dialog = MaterialAlertDialogBuilder(context).setTitle(title).setMessage(message).create()
            fun complete(answer: Boolean) {
                if (finished) return
                finished = true
                observer?.let { context.lifecycle.removeObserver(it) }
                if (continuation.isActive) continuation.resume(answer)
                dialog.dismiss()
            }
            dialog.setButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE, context.getString(if (confirm) R.string.yes else android.R.string.ok)) { _, _ -> complete(true) }
            if (confirm) dialog.setButton(androidx.appcompat.app.AlertDialog.BUTTON_NEGATIVE, context.getString(R.string.no)) { _, _ -> complete(false) }
            dialog.setOnCancelListener { complete(false) }
            dialog.setOnDismissListener { complete(false) }
            observer = LifecycleEventObserver { _, event -> if (event == Lifecycle.Event.ON_DESTROY) complete(false) }
            context.lifecycle.addObserver(observer!!)
            continuation.invokeOnCancellation { context.window.decorView.post { complete(false) } }
            if (alive() && continuation.isActive) dialog.show() else complete(false)
        }
    }
    override suspend fun confirm(message: String) = question(R.string.confirm, message, true)
    override suspend fun alert(message: String) { question(R.string.ooc_warning, message, false) }
    override suspend fun onUpdateSuccess(group: ProxyGroup, changed: Int, added: List<String>, updated: Map<String, String>, deleted: List<String>, duplicate: List<String>, byUser: Boolean) {
        withContext(Dispatchers.Main) {
            if (!alive()) return@withContext
            if (changed == 0 && duplicate.isEmpty()) {
                if (byUser) context.snackbar(context.getString(R.string.group_no_difference, group.displayName())).show()
                return@withContext
            }
            context.snackbar(context.getString(R.string.group_updated, group.name, changed)).show()
            var status = ""
            if (added.isNotEmpty()) status += context.getString(R.string.group_added, added.joinToString("\n", postfix = "\n\n"))
            if (updated.isNotEmpty()) status += context.getString(R.string.group_changed, updated.map { it }.joinToString("\n", postfix = "\n\n") { if (it.key == it.value) it.key else "${it.key} => ${it.value}" })
            if (deleted.isNotEmpty()) status += context.getString(R.string.group_deleted, deleted.joinToString("\n", postfix = "\n\n"))
            if (duplicate.isNotEmpty()) status += context.getString(R.string.group_duplicate, duplicate.joinToString("\n", postfix = "\n\n"))
            delay(1000)
            if (alive()) MaterialAlertDialogBuilder(context).setTitle(context.getString(R.string.group_diff, group.displayName()))
                .setMessage(status.trim()).setPositiveButton(android.R.string.ok, null).show()
        }
    }
    override suspend fun onUpdateFailure(group: ProxyGroup, message: String) {
        withContext(Dispatchers.Main) { if (alive()) context.snackbar(message).show() }
    }
}

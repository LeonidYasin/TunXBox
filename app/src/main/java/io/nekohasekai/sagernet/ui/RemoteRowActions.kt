package io.nekohasekai.sagernet.ui

import android.view.KeyEvent
import android.view.View
import io.nekohasekai.sagernet.R

/** Hardware Menu/Info alternatives to swipe/drag/long press, with an on-screen button. */
object RemoteRowActions {
    fun bind(row: View, controls: List<View>, open: () -> Unit) {
        row.isFocusable = true
        row.setTag(R.id.remote_row_actions, open)
        row.setOnLongClickListener { open(); true }
        val visible = controls.filter { it.visibility == View.VISIBLE && it.isEnabled }
        val path = listOf(row) + visible
        path.forEachIndexed { index, control ->
            if (control !== row) {
                control.isFocusable = true
                control.minimumWidth = maxOf(control.minimumWidth, (48 * control.resources.displayMetrics.density).toInt())
                control.minimumHeight = maxOf(control.minimumHeight, (48 * control.resources.displayMetrics.density).toInt())
            }
            control.setOnKeyListener { _, code, event ->
                if (event.action != KeyEvent.ACTION_DOWN) return@setOnKeyListener false
                val next = if (code == KeyEvent.KEYCODE_DPAD_RIGHT) index + 1 else if (code == KeyEvent.KEYCODE_DPAD_LEFT) index - 1 else return@setOnKeyListener false
                path.getOrNull(next)?.requestFocus() == true
            }
        }
    }
    fun handle(focus: View?, event: KeyEvent): Boolean {
        if (event.keyCode !in intArrayOf(KeyEvent.KEYCODE_MENU, KeyEvent.KEYCODE_INFO)) return false
        var view = focus
        while (view != null) {
            @Suppress("UNCHECKED_CAST")
            val action = view.getTag(R.id.remote_row_actions) as? (() -> Unit)
            if (action != null) {
                if (event.action == KeyEvent.ACTION_DOWN && event.repeatCount == 0) action()
                return true
            }
            view = view.parent as? View
        }
        return false
    }
    fun confirmDelete(context: android.content.Context, title: String, action: () -> Unit) {
        val dialog = com.google.android.material.dialog.MaterialAlertDialogBuilder(context)
            .setTitle(R.string.delete).setMessage(title)
            .setPositiveButton(R.string.yes) { _, _ -> action() }
            .setNegativeButton(android.R.string.cancel, null).create()
        dialog.setOnShowListener { dialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE).requestFocus() }
        dialog.show()
    }
}

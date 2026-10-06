package io.nekohasekai.sagernet.ui

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.View
import android.view.ViewTreeObserver
import androidx.core.content.ContextCompat
import io.nekohasekai.sagernet.R

/** Draws an overlay only for hardware navigation. Never replaces touch ripples/backgrounds. */
class RemoteFocusHighlighter(private val root: View) {
    var enabled = false
        private set
    private var decorated: View? = null
    private var outline: GradientDrawable? = null
    private val listener = ViewTreeObserver.OnGlobalFocusChangeListener { _, next -> decorate(next) }
    init { root.viewTreeObserver.addOnGlobalFocusChangeListener(listener) }
    fun setEnabled(value: Boolean) { enabled = value; decorate(if (value) root.findFocus() else null) }
    private fun decorate(next: View?) {
        outline?.let { decorated?.overlay?.remove(it) }; outline = null; decorated = null
        if (!enabled || next == null || next === root || next.width == 0 || next.height == 0) return
        val density = next.resources.displayMetrics.density
        val drawable = GradientDrawable().apply {
            setColor(Color.TRANSPARENT)
            setStroke((3 * density).toInt().coerceAtLeast(2), ContextCompat.getColor(next.context, R.color.remote_focus_indicator))
            cornerRadius = 6 * density
            setBounds(0, 0, next.width, next.height)
        }
        next.overlay.add(drawable); decorated = next; outline = drawable
    }
    fun close() {
        setEnabled(false)
        if (root.viewTreeObserver.isAlive) root.viewTreeObserver.removeOnGlobalFocusChangeListener(listener)
    }
}

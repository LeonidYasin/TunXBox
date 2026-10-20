package io.nekohasekai.sagernet.ui.tv

import android.content.Context
import android.os.Build
import android.view.InputDevice
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.ViewConfiguration
import androidx.leanback.widget.BaseGridView
import androidx.leanback.widget.VerticalGridView
import kotlin.math.abs

/**
 * Leanback is focus-driven; a horizontal child RecyclerView can take a vertical
 * gesture before the rows list does. Route ONLY axis-locked vertical drags to the
 * real rows RecyclerView, retaining its native velocity tracker/fling/overscroll.
 * Pointer browsing must not repeatedly align a previously focused off-screen row.
 * Clicks, long presses and horizontal swipes still use the normal dispatch path.
 */
class TvPointerScrollController(context: Context, private val rows: () -> VerticalGridView?) {
    private enum class Axis { UNDECIDED, HORIZONTAL, VERTICAL }
    private val slop = ViewConfiguration.get(context).scaledTouchSlop
    private val wheelFactor = if (Build.VERSION.SDK_INT >= 26) {
        ViewConfiguration.get(context).scaledVerticalScrollFactor
    } else {
        val value = android.util.TypedValue()
        if (context.theme.resolveAttribute(android.R.attr.listPreferredItemHeight, value, true))
            value.getDimension(context.resources.displayMetrics) else 48f * context.resources.displayMetrics.density
    }
    private var down: MotionEvent? = null
    private var axis = Axis.UNDECIDED
    private var pointerMode = false
    private var previousFocusStrategy = BaseGridView.FOCUS_SCROLL_ALIGNED

    private fun pointer(grid: VerticalGridView) {
        if (!pointerMode) {
            previousFocusStrategy = grid.focusScrollStrategy
            pointerMode = true
            grid.stopScroll()
            grid.setFocusSearchDisabled(true)
            grid.setFocusScrollStrategy(BaseGridView.FOCUS_SCROLL_ITEM)
        }
    }

    private fun sendToRows(grid: VerticalGridView, event: MotionEvent): Boolean {
        val location = IntArray(2)
        grid.getLocationOnScreen(location)
        val copy = MotionEvent.obtain(event)
        copy.offsetLocation(event.rawX - event.x - location[0], event.rawY - event.y - location[1])
        return try { grid.onTouchEvent(copy) } finally { copy.recycle() }
    }

    fun dispatchTouch(event: MotionEvent, dispatchNormally: (MotionEvent) -> Boolean): Boolean {
        val grid = rows() ?: return dispatchNormally(event)
        // Stylus, accessibility gestures and other inputs keep Android's dispatch.
        if (!event.isFromSource(InputDevice.SOURCE_TOUCHSCREEN) &&
            !event.isFromSource(InputDevice.SOURCE_MOUSE)) return dispatchNormally(event)
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                clearGesture()
                down = MotionEvent.obtain(event)
                pointer(grid)
            }
            MotionEvent.ACTION_MOVE -> {
                val start = down
                if (start != null && axis == Axis.UNDECIDED && event.pointerCount == 1) {
                    val dx = abs(event.x - start.x)
                    val dy = abs(event.y - start.y)
                    if (maxOf(dx, dy) > slop) {
                        axis = if (dy > dx) Axis.VERTICAL else Axis.HORIZONTAL
                        if (axis == Axis.VERTICAL) {
                            // Cancel exactly once so a card never clicks after a drag.
                            val cancel = MotionEvent.obtain(event)
                            cancel.action = MotionEvent.ACTION_CANCEL
                            try { dispatchNormally(cancel) } finally { cancel.recycle() }
                            sendToRows(grid, start)
                        }
                    }
                }
            }
        }
        val vertical = axis == Axis.VERTICAL
        val handled = if (vertical) { sendToRows(grid, event); true } else dispatchNormally(event)
        if (event.actionMasked == MotionEvent.ACTION_UP || event.actionMasked == MotionEvent.ACTION_CANCEL) clearGesture()
        return handled
    }

    fun dispatchMotion(event: MotionEvent): Boolean {
        if (event.action != MotionEvent.ACTION_SCROLL || !event.isFromSource(InputDevice.SOURCE_CLASS_POINTER)) return false
        val vertical = event.getAxisValue(MotionEvent.AXIS_VSCROLL)
        // Horizontal wheel/trackpad gestures remain assigned to the child row.
        if (vertical == 0f || abs(event.getAxisValue(MotionEvent.AXIS_HSCROLL)) > abs(vertical)) return false
        val grid = rows() ?: return false
        pointer(grid)
        grid.stopScroll()
        grid.scrollBy(0, (-vertical * wheelFactor).toInt())
        return true
    }

    fun beforeKey(event: KeyEvent) {
        if (!pointerMode || event.action != KeyEvent.ACTION_DOWN || event.keyCode !in REMOTE_KEYS) return
        val grid = rows() ?: return
        clearGesture()
        grid.stopScroll()
        grid.setFocusSearchDisabled(false)
        grid.setFocusScrollStrategy(previousFocusStrategy)
        pointerMode = false
        // Resume from a visible row, not the old off-screen selection. Retain an
        // already-visible focused card; the key is then dispatched normally once.
        val focused = grid.findFocus()
        val visibleFocus = focused != null && focused !== grid && android.graphics.Rect().let { rect ->
            focused.getGlobalVisibleRect(rect) && rect.height() > 0
        }
        if (!visibleFocus) {
            val visible = (0 until grid.childCount).map { grid.getChildAt(it) }
                .filter { it.bottom > grid.paddingTop && it.top < grid.height - grid.paddingBottom }
                .maxByOrNull { minOf(it.bottom, grid.height - grid.paddingBottom) - maxOf(it.top, grid.paddingTop) }
            visible?.requestFocus()
        }
    }

    private fun clearGesture() { down?.recycle(); down = null; axis = Axis.UNDECIDED }
    fun dispose() { clearGesture() }

    companion object {
        private val REMOTE_KEYS = setOf(KeyEvent.KEYCODE_DPAD_UP, KeyEvent.KEYCODE_DPAD_DOWN,
            KeyEvent.KEYCODE_DPAD_LEFT, KeyEvent.KEYCODE_DPAD_RIGHT, KeyEvent.KEYCODE_DPAD_CENTER,
            KeyEvent.KEYCODE_ENTER, KeyEvent.KEYCODE_NUMPAD_ENTER, KeyEvent.KEYCODE_TAB,
            KeyEvent.KEYCODE_PAGE_UP, KeyEvent.KEYCODE_PAGE_DOWN, KeyEvent.KEYCODE_MOVE_HOME, KeyEvent.KEYCODE_MOVE_END)
    }
}

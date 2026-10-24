package io.nekohasekai.sagernet.ui.tv

import android.app.Activity
import android.app.Application
import android.view.InputDevice
import android.view.KeyEvent
import android.view.MotionEvent
import androidx.leanback.widget.VerticalGridView
import io.nekohasekai.sagernet.R
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [28])
class TvPointerScrollControllerTest {
    private val activity = Robolectric.buildActivity(Activity::class.java)
    private lateinit var grid: VerticalGridView
    private lateinit var controller: TvPointerScrollController
    private val normal = mutableListOf<Int>()
    @Before fun prepare() {
        activity.get().setTheme(R.style.Theme_SagerNet)
        activity.setup()
        grid = VerticalGridView(activity.get())
        activity.get().setContentView(grid)
        controller = TvPointerScrollController(activity.get()) { grid }
    }
    @After fun finish() { controller.dispose(); activity.pause().stop().destroy() }
    private fun send(action: Int, x: Float, y: Float, time: Long, source: Int = InputDevice.SOURCE_TOUCHSCREEN) {
        val event = MotionEvent.obtain(100, time, action, x, y, 0)
        event.source = source
        try { controller.dispatchTouch(event) { normal.add(it.actionMasked); true } } finally { event.recycle() }
    }
    @Test fun verticalDragCancelsCardExactlyOnceAndKeepsFocusBlockedUntilRemoteInput() {
        send(MotionEvent.ACTION_DOWN, 100f, 500f, 100)
        send(MotionEvent.ACTION_MOVE, 102f, 300f, 180)
        send(MotionEvent.ACTION_MOVE, 103f, 100f, 260)
        send(MotionEvent.ACTION_UP, 103f, 100f, 300)
        assertEquals(listOf(MotionEvent.ACTION_DOWN, MotionEvent.ACTION_CANCEL), normal)
        assertTrue(grid.isFocusSearchDisabled)
        controller.beforeKey(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_DOWN))
        assertFalse(grid.isFocusSearchDisabled)
    }
    @Test fun horizontalSwipeTapAndLongPressStayOnNormalAndroidDispatch() {
        send(MotionEvent.ACTION_DOWN, 200f, 200f, 100)
        send(MotionEvent.ACTION_MOVE, 30f, 201f, 200)
        send(MotionEvent.ACTION_UP, 30f, 201f, 300)
        assertEquals(listOf(MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE, MotionEvent.ACTION_UP), normal)
        normal.clear()
        send(MotionEvent.ACTION_DOWN, 100f, 100f, 100)
        send(MotionEvent.ACTION_UP, 100f, 100f, 900)
        assertEquals(listOf(MotionEvent.ACTION_DOWN, MotionEvent.ACTION_UP), normal)
    }
    @Test fun mouseDragUsesSameVerticalAxisLock() {
        send(MotionEvent.ACTION_DOWN, 100f, 500f, 100, InputDevice.SOURCE_MOUSE)
        send(MotionEvent.ACTION_MOVE, 102f, 200f, 200, InputDevice.SOURCE_MOUSE)
        send(MotionEvent.ACTION_UP, 102f, 200f, 300, InputDevice.SOURCE_MOUSE)
        assertEquals(listOf(MotionEvent.ACTION_DOWN, MotionEvent.ACTION_CANCEL), normal)
    }
    @Test fun cancelEndsCaptureAndNextTapIsNotConsumed() {
        send(MotionEvent.ACTION_DOWN, 100f, 500f, 100)
        send(MotionEvent.ACTION_MOVE, 100f, 100f, 200)
        send(MotionEvent.ACTION_CANCEL, 100f, 100f, 300)
        normal.clear()
        send(MotionEvent.ACTION_DOWN, 100f, 100f, 100)
        send(MotionEvent.ACTION_UP, 100f, 100f, 200)
        assertEquals(listOf(MotionEvent.ACTION_DOWN, MotionEvent.ACTION_UP), normal)
    }
}

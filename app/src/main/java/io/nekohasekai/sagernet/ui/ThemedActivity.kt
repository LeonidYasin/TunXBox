package io.nekohasekai.sagernet.ui

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.view.MotionEvent
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.snackbar.Snackbar
import io.nekohasekai.sagernet.R
import io.nekohasekai.sagernet.utils.Theme

abstract class ThemedActivity : AppCompatActivity {
    constructor() : super()
    constructor(contentLayoutId: Int) : super(contentLayoutId)

    override fun attachBaseContext(newBase: Context) {
        val configuration = Configuration(newBase.resources.configuration)
        val remoteOnly = configuration.uiMode and Configuration.UI_MODE_TYPE_MASK == Configuration.UI_MODE_TYPE_TELEVISION ||
            configuration.navigation == Configuration.NAVIGATION_DPAD && configuration.touchscreen == Configuration.TOUCHSCREEN_NOTOUCH
        val scale = RemoteReadability.fontScale(remoteOnly, configuration.fontScale)
        if (scale != configuration.fontScale) {
            configuration.fontScale = scale
            super.attachBaseContext(newBase.createConfigurationContext(configuration))
        } else super.attachBaseContext(newBase)
    }

    private var remoteFocus: RemoteFocusHighlighter? = null
    protected val remoteFocusEnabled get() = remoteFocus?.enabled == true

    protected fun highlightRemoteFocus() { remoteFocus?.setEnabled(true) }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN && event.keyCode in intArrayOf(
                KeyEvent.KEYCODE_DPAD_UP, KeyEvent.KEYCODE_DPAD_DOWN, KeyEvent.KEYCODE_DPAD_LEFT,
                KeyEvent.KEYCODE_DPAD_RIGHT, KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER,
                KeyEvent.KEYCODE_MENU, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE)) highlightRemoteFocus()
        return super.dispatchKeyEvent(event)
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (event.actionMasked == MotionEvent.ACTION_DOWN) remoteFocus?.setEnabled(false)
        return super.dispatchTouchEvent(event)
    }

    override fun onDestroy() {
        remoteFocus?.close(); remoteFocus = null
        super.onDestroy()
    }

    var themeResId = 0
    var uiMode = 0
    open val isDialog = false

    override fun onCreate(savedInstanceState: Bundle?) {
        if (!isDialog) {
            Theme.apply(this)
        } else {
            Theme.applyDialog(this)
        }
        Theme.applyNightTheme()

        super.onCreate(savedInstanceState)

        remoteFocus = RemoteFocusHighlighter(window.decorView)
        uiMode = resources.configuration.uiMode

        if (Build.VERSION.SDK_INT >= 35) {
            ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { _, insets ->
                val top = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top
                findViewById<AppBarLayout>(R.id.appbar)?.apply {
                    updatePadding(top = top)
//                Logs.w("appbar $top")
                }
//            findViewById<NavigationView>(R.id.nav_view)?.apply {
//                updatePadding(top = top)
//            }
                insets
            }
        }
    }

    override fun setTheme(resId: Int) {
        super.setTheme(resId)

        themeResId = resId
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        if (newConfig.uiMode != uiMode) {
            uiMode = newConfig.uiMode
            ActivityCompat.recreate(this)
        }
    }

    fun snackbar(@StringRes resId: Int): Snackbar = snackbar("").setText(resId)
    fun snackbar(text: CharSequence): Snackbar = snackbarInternal(text).apply {
        view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).apply {
            maxLines = 10
        }
    }

    internal open fun snackbarInternal(text: CharSequence): Snackbar = throw NotImplementedError()

}

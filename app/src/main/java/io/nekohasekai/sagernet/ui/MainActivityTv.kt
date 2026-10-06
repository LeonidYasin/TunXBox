package io.nekohasekai.sagernet.ui

import android.os.Bundle
import android.view.KeyEvent
import android.os.Build
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import io.nekohasekai.sagernet.R
import io.nekohasekai.sagernet.ui.tv.MainBrowseFragment

/**
 * Leanback TV Activity - default UI for all devices.
 * Launched via explicit ComponentName from MainActivity.
 */
class MainActivityTv : FragmentActivity() {
    private val notifications = registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        val browse = supportFragmentManager.findFragmentById(R.id.tv_container) as? MainBrowseFragment
        if (browse != null && event.keyCode in intArrayOf(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE, KeyEvent.KEYCODE_MENU, KeyEvent.KEYCODE_INFO)) {
            if (event.action == KeyEvent.ACTION_DOWN && event.repeatCount == 0) {
                if (event.keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE) browse.handlePlayPause() else browse.showFocusedActions()
            }
            return true
        }
        return super.dispatchKeyEvent(event)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (io.nekohasekai.sagernet.ui.tv.TvUiPreferences.phoneMode) {
            startActivity(android.content.Intent(this, MainActivity::class.java).apply {
                putExtra("force_phone_mode", true)
            })
            finish()
            return
        }
        setContentView(R.layout.activity_main_tv)
        if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) notifications.launch(Manifest.permission.POST_NOTIFICATIONS)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.tv_container, MainBrowseFragment())
                .commit()
        }
    }
}

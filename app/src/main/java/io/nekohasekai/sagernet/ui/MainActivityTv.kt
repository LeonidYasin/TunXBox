package io.nekohasekai.sagernet.ui

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import io.nekohasekai.sagernet.R
import io.nekohasekai.sagernet.ui.tv.MainBrowseFragment

/**
 * Leanback-based Activity for Android TV and devices with D-pad/pult.
 * Auto-detected by MainActivity via TvDeviceUtil.isTvDevice().
 * 
 * Supports:
 * - Android TV (Leanback launcher)
 * - TV boxes with tablet Android + remote control
 * - Emulators with DPAD
 */
class MainActivityTv : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_tv)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.tv_container, MainBrowseFragment())
                .commit()
        }
    }
}

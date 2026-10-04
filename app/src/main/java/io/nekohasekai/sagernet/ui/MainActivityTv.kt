package io.nekohasekai.sagernet.ui

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import io.nekohasekai.sagernet.R
import io.nekohasekai.sagernet.ui.tv.MainBrowseFragment

/**
 * Leanback TV Activity - default UI for all devices.
 * Launched via explicit ComponentName from MainActivity.
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
    
    override fun onNewIntent(intent: android.content.Intent?) {
        super.onNewIntent(intent)
        // Clear any incoming intent to prevent share sheet
        setIntent(android.content.Intent())
    }
}

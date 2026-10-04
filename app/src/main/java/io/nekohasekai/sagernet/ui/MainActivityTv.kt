package io.nekohasekai.sagernet.ui

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import io.nekohasekai.sagernet.R
import io.nekohasekai.sagernet.ui.tv.MainBrowseFragment

/**
 * Leanback-based Activity for Android TV and all devices (TV mode is default).
 * Auto-launched from MainActivity unless user explicitly chose Phone Mode.
 */
class MainActivityTv : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Явно устанавливаем layout контейнер
        setContentView(R.layout.activity_main_tv)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.tv_container, MainBrowseFragment())
                .commit()
        }
    }
    
    // Блокируем любые incoming intents которые могут вызвать chooser
    override fun onNewIntent(intent: android.content.Intent?) {
        super.onNewIntent(intent)
        // Игнорируем все новые интенты чтобы не触发 share sheet
        setIntent(android.content.Intent()) // очищаем intent
    }
}

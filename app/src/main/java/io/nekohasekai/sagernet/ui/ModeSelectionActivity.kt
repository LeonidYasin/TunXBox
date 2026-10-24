package io.nekohasekai.sagernet.ui

import android.app.UiModeManager
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import io.nekohasekai.sagernet.R
import io.nekohasekai.sagernet.ui.tv.TvUiPreferences

/** Launcher only. VIEW/import intents keep the upstream MainActivity handler. */
class ModeSelectionActivity : ComponentActivity() {
    private var choosing = false
    private var releaseCard: View? = null
    private val releaseInfo = registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()) {
        releaseCard?.post { releaseCard?.requestFocus() }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fun dp(n: Int) = (n * resources.displayMetrics.density).toInt()
        val scroll = ScrollView(this).apply {
            isFillViewport = true
            setBackgroundColor(0xFF0F172A.toInt())
            setPadding(dp(24), dp(24), dp(24), dp(24))
            clipToPadding = false
        }
        val center = LinearLayout(this).apply { gravity = Gravity.CENTER; orientation = LinearLayout.VERTICAL }
        val column = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val available = resources.configuration.screenWidthDp - 48
        center.addView(column, LinearLayout.LayoutParams(dp(available.coerceIn(160, 640)), -2))
        scroll.addView(center, android.widget.FrameLayout.LayoutParams(-1, -1))
        fun text(value: String, size: Float, color: Int) = TextView(this).apply {
            text = value; textSize = size; setTextColor(color)
        }
        if(io.nekohasekai.sagernet.ui.update.UpdateReceipt.consume(this)) {
            column.addView(text(getString(R.string.update_installed_title,
                io.nekohasekai.sagernet.BuildConfig.VERSION_NAME),18f,0xFF67E8F9.toInt()))
            (getSystemService(NOTIFICATION_SERVICE) as android.app.NotificationManager).cancel(1331)
        }
        fun card(idValue: Int, title: String, description: String, compact: Boolean = false, action: () -> Unit): LinearLayout {
            val card = LinearLayout(this).apply {
                id = idValue; orientation = LinearLayout.VERTICAL
                isFocusable = true; isFocusableInTouchMode = true; isClickable = true
                descendantFocusability = android.view.ViewGroup.FOCUS_BLOCK_DESCENDANTS
                minimumHeight = dp(if(compact)88 else 112); setPadding(dp(24), dp(if(compact)16 else 20), dp(24), dp(if(compact)16 else 20))
                setBackgroundResource(R.drawable.mode_choice_background)
                contentDescription = title + ". " + description
                accessibilityDelegate = object : View.AccessibilityDelegate() {
                    override fun onInitializeAccessibilityNodeInfo(host: View, info: android.view.accessibility.AccessibilityNodeInfo) {
                        super.onInitializeAccessibilityNodeInfo(host, info); info.className = "android.widget.Button"
                    }
                }
                setOnClickListener { action() }
                // A focusable-in-touch-mode View otherwise consumes the first tap only for focus.
                setOnTouchListener { clicked, event ->
                    if (event.actionMasked == android.view.MotionEvent.ACTION_UP) {
                        clicked.performClick(); true
                    } else false
                }
            }
            card.addView(text(title,22f,Color.WHITE).apply {if(compact)id=R.id.mode_build_info})
            card.addView(text(description, 16f, 0xFFCBD5E1.toInt()), LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(8) })
            column.addView(card, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(16) })
            return card
        }
        val release = card(R.id.mode_release,getString(R.string.mode_release_title,io.nekohasekai.sagernet.BuildConfig.VERSION_NAME),getString(R.string.mode_release_hint),true) {
            releaseInfo.launch(Intent(this,ReleaseNotesActivity::class.java))
        }
        releaseCard=release
        val tv = card(R.id.mode_choose_tv,getString(R.string.mode_tv_title),getString(R.string.mode_tv_hint)) {choose(false)}
        val phone = card(R.id.mode_choose_phone,getString(R.string.mode_phone_title),getString(R.string.mode_phone_hint)) {choose(true)}
        release.nextFocusDownId=tv.id;release.nextFocusForwardId=tv.id
        tv.nextFocusUpId=release.id;tv.nextFocusDownId=phone.id;tv.nextFocusForwardId=phone.id
        phone.nextFocusUpId=tv.id
        setContentView(scroll)
        ViewCompat.setOnApplyWindowInsetsListener(scroll) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout())
            view.setPadding(dp(24) + bars.left, dp(24) + bars.top, dp(24) + bars.right, dp(24) + bars.bottom)
            column.layoutParams.width = (dp(available) - bars.left - bars.right).coerceIn(dp(120), dp(640))
            insets
        }
        ViewCompat.requestApplyInsets(scroll)
        val isTv = (getSystemService(UI_MODE_SERVICE) as UiModeManager).currentModeType == Configuration.UI_MODE_TYPE_TELEVISION
        if (savedInstanceState == null) (if (TvUiPreferences.lastChoicePhoneMode ?: !isTv) phone else tv).requestFocus()
    }
    private fun choose(phone: Boolean) {
        if (choosing) return
        choosing = true
        TvUiPreferences.phoneMode = phone
        val intent = Intent(this, if (phone) MainActivity::class.java else MainActivityTv::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        if (phone) intent.putExtra("force_phone_mode", true)
        startActivity(intent)
        finish()
    }
}

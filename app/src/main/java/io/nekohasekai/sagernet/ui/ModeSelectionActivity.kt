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
        column.addView(text("TunXBox", 16f, 0xFF67E8F9.toInt()))
        column.addView(text(getString(R.string.mode_choose_title), 28f, Color.WHITE), LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(12) })
        column.addView(text(getString(R.string.mode_choose_hint), 16f, 0xFFCBD5E1.toInt()), LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(12); bottomMargin = dp(24) })
        fun card(idValue: Int, title: Int, description: Int, phone: Boolean): LinearLayout {
            val card = LinearLayout(this).apply {
                id = idValue; orientation = LinearLayout.VERTICAL
                isFocusable = true; isFocusableInTouchMode = true; isClickable = true
                descendantFocusability = android.view.ViewGroup.FOCUS_BLOCK_DESCENDANTS
                minimumHeight = dp(112); setPadding(dp(24), dp(20), dp(24), dp(20))
                setBackgroundResource(R.drawable.mode_choice_background)
                contentDescription = getString(title) + ". " + getString(description)
                accessibilityDelegate = object : View.AccessibilityDelegate() {
                    override fun onInitializeAccessibilityNodeInfo(host: View, info: android.view.accessibility.AccessibilityNodeInfo) {
                        super.onInitializeAccessibilityNodeInfo(host, info); info.className = "android.widget.Button"
                    }
                }
                setOnClickListener { choose(phone) }
            }
            card.addView(text(getString(title), 22f, Color.WHITE))
            card.addView(text(getString(description), 16f, 0xFFCBD5E1.toInt()), LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(8) })
            column.addView(card, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(16) })
            return card
        }
        val tv = card(R.id.mode_choose_tv, R.string.mode_tv_title, R.string.mode_tv_hint, false)
        val phone = card(R.id.mode_choose_phone, R.string.mode_phone_title, R.string.mode_phone_hint, true)
        tv.nextFocusDownId = phone.id; tv.nextFocusForwardId = phone.id; phone.nextFocusUpId = tv.id
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

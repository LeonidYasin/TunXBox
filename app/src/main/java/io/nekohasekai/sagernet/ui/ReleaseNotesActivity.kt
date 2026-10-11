package io.nekohasekai.sagernet.ui

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.KeyEvent
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import io.nekohasekai.sagernet.BuildConfig
import io.nekohasekai.sagernet.R

/** Notes travel with this APK's source/resources. No latest-release fetch, VPN command or mode selection. */
class ReleaseNotesActivity : ComponentActivity() {
    private var aboutButton: TextView? = null
    private val about = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        aboutButton?.post { aboutButton?.requestFocus() }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fun dp(n: Int)=(n*resources.displayMetrics.density).toInt()
        val scroll=ScrollView(this).apply {
            isFillViewport=true;isFocusable=false;isFocusableInTouchMode=false;setBackgroundColor(0xFF0F172A.toInt())
            clipToPadding=false;setPadding(dp(24),dp(24),dp(24),dp(24))
        }
        val column=LinearLayout(this).apply {orientation=LinearLayout.VERTICAL}
        val center=LinearLayout(this).apply {orientation=LinearLayout.VERTICAL;gravity=Gravity.CENTER_HORIZONTAL}
        center.addView(column,LinearLayout.LayoutParams(dp((resources.configuration.screenWidthDp-48).coerceIn(160,800)),-2))
        scroll.addView(center,android.widget.FrameLayout.LayoutParams(-1,-2))
        fun label(value: String,size: Float,idValue: Int)=TextView(this).apply {
            id=idValue;text=value;textSize=size;setTextColor(Color.WHITE)
            column.addView(this,LinearLayout.LayoutParams(-1,-2).apply {bottomMargin=dp(16)})
        }
        label(getString(R.string.mode_release_title,BuildConfig.VERSION_NAME),26f,R.id.release_title)
        label(getString(R.string.mode_build_identity,BuildConfig.VERSION_NAME,BuildConfig.VERSION_CODE.toString(),
            BuildConfig.BUILD_COMMIT.take(8),BuildConfig.BUILD_RUN_NUMBER),16f,R.id.release_build_info)
        fun button(title: Int,idValue: Int,action: () -> Unit)=label(getString(title),20f,idValue).apply {
            isFocusable=true;isFocusableInTouchMode=true;isClickable=true
            minHeight=dp(64);gravity=Gravity.CENTER_VERTICAL;setPadding(dp(20),dp(16),dp(20),dp(16))
            setBackgroundResource(R.drawable.mode_choice_background)
            contentDescription=getString(title)
            accessibilityDelegate=object: android.view.View.AccessibilityDelegate() {
                override fun onInitializeAccessibilityNodeInfo(host: android.view.View,info: android.view.accessibility.AccessibilityNodeInfo) {
                    super.onInitializeAccessibilityNodeInfo(host,info);info.className="android.widget.Button"
                }
            }
            setOnClickListener {action()}
            setOnTouchListener {view,event -> if(event.actionMasked==android.view.MotionEvent.ACTION_UP){view.performClick();true}else false}
        }
        val back=button(R.string.release_back,R.id.release_back){finish()}
        val details=button(R.string.release_about,R.id.release_about) {
            about.launch(Intent(this,MainActivity::class.java).putExtra("tv_tools",true).putExtra("tv_destination",R.id.nav_about))
        }
        aboutButton=details
        fun section(title: Int,body: Int,idValue: Int)=label(getString(title)+"\n\n"+getString(body),18f,idValue).apply {
            isFocusable=true;isFocusableInTouchMode=true
            setPadding(dp(16),dp(16),dp(16),dp(16));setBackgroundResource(R.drawable.mode_choice_background)
        }
        label(getString(R.string.release_remote_hint),16f,android.view.View.NO_ID)
        val changes=section(R.string.release_changes_title,R.string.release_changes_body,R.id.release_changes)
        val checks=section(R.string.release_checks_title,R.string.release_checks_body,R.id.release_checks)
        val limits=section(R.string.release_limits_title,R.string.release_limits_body,R.id.release_limits)
        val order=listOf(back,details,changes,checks,limits)
        for((index,item) in order.withIndex()) {
            if(index>0)item.nextFocusUpId=order[index-1].id
            if(index<order.lastIndex){item.nextFocusDownId=order[index+1].id;item.nextFocusForwardId=order[index+1].id}
            // ScrollView.arrowScroll can skip a control beside a tall text block. Route
            // section changes explicitly; horizontal keys keep long notes readable.
            item.setOnKeyListener { _, key, event ->
                when(key) {
                    KeyEvent.KEYCODE_DPAD_UP,KeyEvent.KEYCODE_DPAD_DOWN -> {
                        if(event.action==KeyEvent.ACTION_DOWN) {
                            val target=order[(index+if(key==KeyEvent.KEYCODE_DPAD_UP)-1 else 1).coerceIn(0,order.lastIndex)]
                            target.requestFocus()
                            scroll.post { scroll.scrollTo(0,(center.top+column.top+target.top-scroll.paddingTop).coerceAtLeast(0)) }
                        }
                        true
                    }
                    KeyEvent.KEYCODE_DPAD_LEFT,KeyEvent.KEYCODE_DPAD_RIGHT -> {
                        if(event.action==KeyEvent.ACTION_DOWN) {
                            val page=(scroll.height-scroll.paddingTop-scroll.paddingBottom).coerceAtLeast(dp(64))*3/4
                            scroll.scrollBy(0,if(key==KeyEvent.KEYCODE_DPAD_LEFT)-page else page)
                        }
                        true
                    }
                    else -> false
                }
            }
        }
        setContentView(scroll)
        ViewCompat.setOnApplyWindowInsetsListener(scroll) {view,insets ->
            val bars=insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout())
            view.setPadding(dp(24)+bars.left,dp(24)+bars.top,dp(24)+bars.right,dp(24)+bars.bottom)
            column.layoutParams.width=(dp(resources.configuration.screenWidthDp-48)-bars.left-bars.right).coerceIn(dp(120),dp(800))
            insets
        }
        ViewCompat.requestApplyInsets(scroll)
        if(savedInstanceState==null)back.requestFocus()
    }
}

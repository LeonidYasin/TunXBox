package io.nekohasekai.sagernet.ui.tv

import android.graphics.Color
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.leanback.widget.Presenter
import io.nekohasekai.sagernet.R

class ActionPresenter : Presenter() {
    class ActionViewHolder(view: View, val icon: ImageView, val title: TextView, val subtitle: TextView) : Presenter.ViewHolder(view)
    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val context = parent.context
        val density = parent.resources.displayMetrics.density
        fun dp(value: Int) = (value * density).toInt()
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = ViewGroup.LayoutParams(dp(TvLayoutPolicy.cardWidthDp(parent.resources.configuration.screenWidthDp)), ViewGroup.LayoutParams.WRAP_CONTENT)
            minimumHeight = dp(124)
            setPadding(dp(16), dp(16), dp(16), dp(16))
            gravity = Gravity.CENTER_VERTICAL
            setBackgroundResource(R.drawable.card_background_tv)
            isFocusable = true
            isFocusableInTouchMode = true
        }
        val icon = ImageView(context).apply {
            layoutParams = LinearLayout.LayoutParams(dp(32), dp(32)).apply { marginEnd = dp(12) }
            setColorFilter(Color.WHITE)
            importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
        }
        val texts = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        }
        val title = TextView(context).apply { id = R.id.action_title; textSize = 20f; setTextColor(Color.WHITE); minLines = 2; maxLines = 2; ellipsize = TextUtils.TruncateAt.END }
        val subtitle = TextView(context).apply {
            id = R.id.action_subtitle; textSize = 16f; setTextColor(0xFFCBD5E1.toInt()); maxLines = 3; ellipsize = TextUtils.TruncateAt.END
            layoutParams = LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(6) }
        }
        texts.addView(title); texts.addView(subtitle); layout.addView(icon); layout.addView(texts)
        // Leanback owns focus zoom. Do not compete with it using a second animator.
        return ActionViewHolder(layout, icon, title, subtitle)
    }
    override fun onBindViewHolder(viewHolder: ViewHolder, item: Any?) {
        val holder = viewHolder as ActionViewHolder
        val action = item as TvAction
        val lines = if (action.id == 8L) 8 else 3
        holder.subtitle.minLines = lines
        holder.subtitle.maxLines = lines
        holder.title.text = action.title; holder.subtitle.text = action.subtitle; holder.icon.setImageResource(action.icon)
        holder.view.alpha = if (action.available) 1f else 0.65f
        // Keep stable focus even when an operation is temporarily unavailable. Click handler
        // checks availability and explains why; the subtitle states the current busy condition.
        holder.view.contentDescription = action.title + ". " + action.subtitle
    }
    override fun onUnbindViewHolder(viewHolder: ViewHolder) {}
}

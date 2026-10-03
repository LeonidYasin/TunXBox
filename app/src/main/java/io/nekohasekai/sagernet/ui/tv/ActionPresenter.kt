package io.nekohasekai.sagernet.ui.tv

import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.leanback.widget.Presenter
import io.nekohasekai.sagernet.R

class ActionPresenter : Presenter() {

    class ActionViewHolder(view: View) : Presenter.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.action_title)
        val subtitle: TextView = view.findViewById(R.id.action_subtitle)
    }

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val layout = LinearLayout(parent.context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = ViewGroup.LayoutParams(320, 120)
            setPadding(24, 16, 24, 16)
            gravity = Gravity.CENTER_VERTICAL
            setBackgroundResource(R.drawable.card_background_tv)
            isFocusable = true
            isFocusableInTouchMode = true
            
            setOnFocusChangeListener { v, hasFocus ->
                v.animate()
                    .scaleX(if (hasFocus) 1.08f else 1.0f)
                    .scaleY(if (hasFocus) 1.08f else 1.0f)
                    .setDuration(150)
                    .start()
            }
        }

        val title = TextView(parent.context).apply {
            id = R.id.action_title
            textSize = 18f
            setTextColor(Color.WHITE)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        
        val subtitle = TextView(parent.context).apply {
            id = R.id.action_subtitle
            textSize = 13f
            setTextColor(0xAAFFFFFF.toInt())
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = 4 }
        }

        layout.addView(title)
        layout.addView(subtitle)
        
        return ActionViewHolder(layout)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, item: Any?) {
        val holder = viewHolder as ActionViewHolder
        val action = item as? TvAction ?: return
        
        holder.title.text = action.title
        holder.subtitle.text = action.subtitle
    }

    override fun onUnbindViewHolder(viewHolder: ViewHolder) {}
}

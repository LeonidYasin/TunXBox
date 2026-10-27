package io.nekohasekai.sagernet.ui.tv

import android.graphics.Color
import android.text.TextUtils
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.widget.TextViewCompat
import androidx.leanback.widget.Presenter
import io.nekohasekai.sagernet.R
import io.nekohasekai.sagernet.ui.LanProxyAddress

/**
 * The addresses are retyped on another device, so each one gets the full card width: one line,
 * no ellipsis, and the text shrinks inside 11..22sp instead of being cut. Every address the device
 * currently has is listed; nothing is filtered by a guess about "the" local network.
 */
class LanProxyCardPresenter : Presenter() {
    class Holder(
        view: LinearLayout,
        val label: TextView,
        val addressList: LinearLayout,
        val hint: TextView,
    ) : ViewHolder(view) {
        val addressLines = mutableListOf<AppCompatTextView>()
    }

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val context = parent.context
        val density = parent.resources.displayMetrics.density
        fun dp(value: Int) = (value * density).toInt()
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = ViewGroup.LayoutParams(
                TvLayoutPolicy.cardWidthDp(parent.resources.configuration.screenWidthDp),
                ViewGroup.LayoutParams.WRAP_CONTENT,
            )
            minimumHeight = dp(124)
            setPadding(dp(16), dp(14), dp(16), dp(14))
            setBackgroundResource(R.drawable.card_background_tv)
            isFocusable = true
            isFocusableInTouchMode = true
        }
        val label = TextView(context).apply {
            textSize = 13f
            setTextColor(0xFF94A3B8.toInt())
            maxLines = 1
            ellipsize = TextUtils.TruncateAt.END
            layout.addView(this, LinearLayout.LayoutParams(-1, -2))
        }
        val addressList = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layout.addView(this, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(2) })
        }
        val hint = TextView(context).apply {
            textSize = 13f
            setTextColor(0xFFCBD5E1.toInt())
            maxLines = 1
            ellipsize = TextUtils.TruncateAt.END
            layout.addView(this, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(4) })
        }
        return Holder(layout, label, addressList, hint)
    }

    /** One address per line: the only line that must never be clipped is the address itself. */
    private fun createAddressLine(parent: LinearLayout): AppCompatTextView {
        val density = parent.resources.displayMetrics.density
        val line = AppCompatTextView(parent.context).apply {
            setTextColor(Color.WHITE)
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            setIncludeFontPadding(false)
            maxLines = 1
            // Do NOT call setSingleLine(): it turns on horizontally scrolling, and autosize then
            // measures the text at unbounded width, so a long address is clipped at the card edge
            // ("192.168.100.1…") instead of shrinking to fit.
            ellipsize = null
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 22f)
            TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(
                this, 11, 22, 1, TypedValue.COMPLEX_UNIT_SP,
            )
        }
        parent.addView(line, LinearLayout.LayoutParams(-1, -2).apply { topMargin = (density * 2).toInt() })
        return line
    }

    override fun onBindViewHolder(holder: ViewHolder, item: Any?) {
        val h = holder as Holder
        val card = item as TvLanProxyCard
        val context = h.view.context
        val values = card.entries.map { entry -> LanProxyAddress.format(entry.host, card.port) ?: entry.host }
        h.label.text = context.getString(R.string.tv_lan_proxy_label)
        while (h.addressLines.size < values.size) h.addressLines += createAddressLine(h.addressList)
        h.addressLines.forEachIndexed { index, line ->
            val value = values.getOrNull(index)
            if (value == null) {
                line.visibility = View.GONE
            } else {
                line.text = value
                line.visibility = View.VISIBLE
            }
        }
        h.hint.text = context.getString(R.string.tv_lan_proxy_hint)
        h.view.contentDescription = (listOf(context.getString(R.string.tv_lan_proxy_label)) + values +
            context.getString(R.string.tv_lan_proxy_hint)).joinToString(". ")
    }

    override fun onUnbindViewHolder(holder: ViewHolder) {}
}

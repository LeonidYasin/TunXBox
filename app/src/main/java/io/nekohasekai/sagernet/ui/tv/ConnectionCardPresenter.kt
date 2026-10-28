package io.nekohasekai.sagernet.ui.tv

import android.graphics.Color
import android.text.TextUtils
import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.leanback.widget.Presenter
import io.nekohasekai.sagernet.R

/** Separate one-line counter/result controls cannot consume one another's line budget. */
class ConnectionCardPresenter: Presenter() {
    class Holder(view: LinearLayout,val name: TextView,val sent: TextView,val received: TextView,val result: TextView): ViewHolder(view)
    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val context=parent.context;fun dp(n: Int)=TvCardMetrics.dp(context,n)
        val layout=LinearLayout(context).apply {
            orientation=LinearLayout.VERTICAL
            layoutParams=ViewGroup.LayoutParams(TvCardMetrics.widthPx(context),TvCardMetrics.heightPx(context))
            // Same explicit height as the LAN and action tiles; the two-line name plus three
            // counters is slightly shorter, so center it instead of leaving a gap at the bottom.
            gravity=Gravity.CENTER_VERTICAL
            setPadding(dp(16),dp(16),dp(16),dp(16))
            setBackgroundResource(R.drawable.card_background_tv);isFocusable=true;isFocusableInTouchMode=true
        }
        fun line(size: Float,lines: Int)=TextView(context).apply {
            textSize=size;setTextColor(Color.WHITE);minLines=lines;maxLines=lines;ellipsize=TextUtils.TruncateAt.END
            setIncludeFontPadding(false);gravity=Gravity.CENTER_VERTICAL
            // Fixed box: the counters must not resize the tile when a rate becomes a total.
            layout.addView(this,LinearLayout.LayoutParams(-1,TvCardMetrics.lineBoxPx(context,size,lines)))
        }
        return Holder(layout,
            line(20f,2).apply {id=R.id.connection_summary_name},
            line(16f,1).apply {id=R.id.connection_summary_sent},
            line(16f,1).apply {id=R.id.connection_summary_received},
            line(16f,1).apply {id=R.id.connection_summary_result})
    }
    override fun onBindViewHolder(holder: ViewHolder,item: Any?) {
        val h=holder as Holder;val card=item as TvConnectionCard
        h.name.text=card.name;h.sent.text=card.sent;h.received.text=card.received;h.result.text=card.result
        h.view.contentDescription=listOf(card.name,card.sent,card.received,card.result).joinToString(". ")
    }
    override fun onUnbindViewHolder(holder: ViewHolder) {}
}

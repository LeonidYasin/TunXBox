package io.nekohasekai.sagernet.ui.tv

import android.graphics.Color
import android.text.TextUtils
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.leanback.widget.Presenter

/** Separate one-line counter/result controls cannot consume one another's line budget. */
class ConnectionCardPresenter: Presenter() {
    class Holder(view: LinearLayout,val name: TextView,val sent: TextView,val received: TextView,val result: TextView): ViewHolder(view)
    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val context=parent.context;fun dp(n: Int)=(n*parent.resources.displayMetrics.density).toInt()
        val layout=LinearLayout(context).apply {
            orientation=LinearLayout.VERTICAL;layoutParams=ViewGroup.LayoutParams(dp(TvLayoutPolicy.cardWidthDp(parent.resources.configuration.screenWidthDp)),-2)
            minimumHeight=dp(124);setPadding(dp(16),dp(16),dp(16),dp(16))
            setBackgroundResource(io.nekohasekai.sagernet.R.drawable.card_background_tv);isFocusable=true;isFocusableInTouchMode=true
        }
        fun line(size: Float,lines: Int)=TextView(context).apply {
            textSize=size;setTextColor(Color.WHITE);minLines=lines;maxLines=lines;ellipsize=TextUtils.TruncateAt.END
            layout.addView(this,LinearLayout.LayoutParams(-1,-2))
        }
        return Holder(layout,
            line(20f,2).apply {id=io.nekohasekai.sagernet.R.id.connection_summary_name},
            line(16f,1).apply {id=io.nekohasekai.sagernet.R.id.connection_summary_sent},
            line(16f,1).apply {id=io.nekohasekai.sagernet.R.id.connection_summary_received},
            line(16f,1).apply {id=io.nekohasekai.sagernet.R.id.connection_summary_result})
    }
    override fun onBindViewHolder(holder: ViewHolder,item: Any?) {
        val h=holder as Holder;val card=item as TvConnectionCard
        h.name.text=card.name;h.sent.text=card.sent;h.received.text=card.received;h.result.text=card.result
        h.view.contentDescription=listOf(card.name,card.sent,card.received,card.result).joinToString(". ")
    }
    override fun onUnbindViewHolder(holder: ViewHolder) {}
}

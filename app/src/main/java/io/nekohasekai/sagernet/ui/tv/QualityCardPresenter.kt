package io.nekohasekai.sagernet.ui.tv

import android.graphics.Color
import android.text.TextUtils
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.leanback.widget.Presenter

class QualityCardPresenter: Presenter() {
    class Holder(view: LinearLayout,val lines: List<TextView>): ViewHolder(view)
    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        fun dp(n: Int)=(n*parent.resources.displayMetrics.density).toInt()
        val layout=LinearLayout(parent.context).apply {
            orientation=LinearLayout.VERTICAL;layoutParams=ViewGroup.LayoutParams(dp(TvLayoutPolicy.cardWidthDp(parent.resources.configuration.screenWidthDp)),-2)
            minimumHeight=dp(124);setPadding(dp(16),dp(16),dp(16),dp(16))
            setBackgroundResource(io.nekohasekai.sagernet.R.drawable.card_background_tv)
            isFocusable=true;isFocusableInTouchMode=true
        }
        val lines=(0..3).map {i->TextView(parent.context).apply {
            textSize=if(i==0)20f else 16f;setTextColor(Color.WHITE)
            minLines=if(i==0)2 else 1;maxLines=minLines;ellipsize=TextUtils.TruncateAt.END
            layout.addView(this,LinearLayout.LayoutParams(-1,-2))
        }}
        return Holder(layout,lines)
    }
    override fun onBindViewHolder(holder: ViewHolder,item: Any?) {
        val h=holder as Holder;val c=item as TvQualityCard
        val text=listOf(c.title,c.result,c.age,c.traffic)
        h.lines.zip(text).forEach {(v,t)->v.text=t};h.view.contentDescription=text.joinToString(". ")
    }
    override fun onUnbindViewHolder(holder: ViewHolder) {}
}

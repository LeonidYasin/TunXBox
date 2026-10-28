package io.nekohasekai.sagernet.ui.tv

import android.graphics.Color
import android.text.TextUtils
import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.leanback.widget.Presenter
import io.nekohasekai.sagernet.R

class QualityCardPresenter: Presenter() {
    class Holder(view: LinearLayout,val lines: List<TextView>): ViewHolder(view)
    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val context=parent.context;fun dp(n: Int)=TvCardMetrics.dp(context,n)
        val layout=LinearLayout(context).apply {
            orientation=LinearLayout.VERTICAL
            // Same explicit height as the other tiles of the connection row.
            layoutParams=ViewGroup.LayoutParams(TvCardMetrics.widthPx(context),TvCardMetrics.heightPx(context))
            gravity=Gravity.CENTER_VERTICAL
            setPadding(dp(16),dp(16),dp(16),dp(16))
            setBackgroundResource(R.drawable.card_background_tv)
            isFocusable=true;isFocusableInTouchMode=true
        }
        val lines=(0..3).map {i->
            val title=i==0
            TextView(context).apply {
                textSize=if(title)20f else 16f;setTextColor(Color.WHITE)
                minLines=if(title)2 else 1;maxLines=minLines;ellipsize=TextUtils.TruncateAt.END
                setIncludeFontPadding(false);gravity=Gravity.CENTER_VERTICAL
                layout.addView(this,LinearLayout.LayoutParams(-1,
                    TvCardMetrics.lineBoxPx(context,if(title)20f else 16f,minLines)))
            }
        }
        return Holder(layout,lines)
    }
    override fun onBindViewHolder(holder: ViewHolder,item: Any?) {
        val h=holder as Holder;val c=item as TvQualityCard
        val text=listOf(c.title,c.result,c.age,c.traffic)
        h.lines.zip(text).forEach {(v,t)->v.text=t};h.view.contentDescription=text.joinToString(". ")
    }
    override fun onUnbindViewHolder(holder: ViewHolder) {}
}

package io.nekohasekai.sagernet.ui.tv

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.leanback.widget.Presenter
import io.nekohasekai.sagernet.R

class ProfileCardPresenter(private val openActions: (Long) -> Unit = {}) : Presenter() {
    class ProfileCardViewHolder(view: View) : Presenter.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.profile_name)
        val protocol: TextView = view.findViewById(R.id.profile_protocol)
        val address: TextView = view.findViewById(R.id.profile_address)
        val statusDot: View = view.findViewById(R.id.status_dot)
        val statusText: TextView = view.findViewById(R.id.profile_status)
        var profileId = 0L
    }
    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val holder = ProfileCardViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.card_profile_tv, parent, false))
        holder.view.layoutParams.width = (TvLayoutPolicy.cardWidthDp(parent.resources.configuration.screenWidthDp) * parent.resources.displayMetrics.density).toInt()
        holder.view.setOnLongClickListener { if (holder.profileId > 0) { openActions(holder.profileId); true } else false }
        return holder
    }
    override fun onBindViewHolder(viewHolder: ViewHolder, item: Any?) {
        val holder = viewHolder as ProfileCardViewHolder
        val context = holder.view.context
        holder.statusDot.isVisible = false; holder.statusText.text = ""; holder.view.isActivated = false; holder.profileId = 0
        when (item) {
            is TvProfileCard -> {
                holder.profileId = item.id
                holder.name.text = item.name; holder.protocol.text = item.protocol; holder.address.text = item.address
                val labels = mutableListOf<String>()
                if (item.selected) labels.add(context.getString(R.string.tv_selected))
                item.activePhase?.let { phase ->
                    labels.add(context.getString(when (phase) {
                        TvVpnPhase.CONNECTED -> R.string.tv_active
                        TvVpnPhase.CONNECTING -> R.string.tv_connecting
                        TvVpnPhase.STOPPING -> R.string.tv_stopping
                        else -> R.string.tv_idle
                    }))
                    holder.statusDot.isVisible = true
                    holder.statusDot.backgroundTintList = ColorStateList.valueOf(if (phase == TvVpnPhase.CONNECTED) Color.rgb(74, 222, 128) else Color.rgb(251, 191, 36))
                }
                holder.statusText.text = labels.joinToString(" · ")
                holder.view.isActivated = item.selected
                holder.view.contentDescription = listOf(item.name, item.protocol, item.address, labels.joinToString(", ")).filter { it.isNotBlank() }.joinToString(". ")
            }
            is TvEmptyHint -> {
                holder.name.text = item.message; holder.protocol.text = ""; holder.address.text = ""
                holder.view.contentDescription = item.message
            }
        }
    }
    override fun onUnbindViewHolder(viewHolder: ViewHolder) { (viewHolder as ProfileCardViewHolder).profileId = 0 }
}

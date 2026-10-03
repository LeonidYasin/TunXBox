package io.nekohasekai.sagernet.ui.tv

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.leanback.widget.Presenter
import io.nekohasekai.sagernet.R
import io.nekohasekai.sagernet.database.ProxyEntity

class ProfileCardPresenter : Presenter() {

    class ProfileCardViewHolder(view: View) : Presenter.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.profile_name)
        val protocol: TextView = view.findViewById(R.id.profile_protocol)
        val statusDot: View = view.findViewById(R.id.status_dot)
        val statusText: TextView = view.findViewById(R.id.profile_status)
    }

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_profile_tv, parent, false)
        
        view.setOnFocusChangeListener { v, hasFocus ->
            v.animate()
                .scaleX(if (hasFocus) 1.08f else 1.0f)
                .scaleY(if (hasFocus) 1.08f else 1.0f)
                .setDuration(150)
                .start()
        }
        
        return ProfileCardViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, item: Any?) {
        val holder = viewHolder as ProfileCardViewHolder
        
        when (item) {
            is ProxyEntity -> {
                holder.name.text = item.displayName() ?: "Unnamed"
                holder.protocol.text = item.requireBean().javaClass.simpleName.removeSuffix("Bean")
                holder.statusText.text = item.requireBean().serverAddress
                holder.statusDot.isVisible = false
            }
            is TvEmptyHint -> {
                holder.name.text = item.message
                holder.protocol.text = ""
                holder.statusText.text = ""
                holder.statusDot.isVisible = false
            }
        }
    }

    override fun onUnbindViewHolder(viewHolder: ViewHolder) {}
}

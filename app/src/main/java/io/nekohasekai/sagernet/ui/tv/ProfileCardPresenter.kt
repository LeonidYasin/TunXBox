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
        
        // Focus animation
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
        val profile = item as? ProxyEntity ?: return
        
        holder.name.text = profile.displayName() ?: "Unnamed"
        holder.protocol.text = profile.typeName()
        holder.statusText.text = profile.requireBean().serverAddress
    }

    override fun onUnbindViewHolder(viewHolder: ViewHolder) {}
}

package io.nekohasekai.sagernet.ui

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import io.nekohasekai.sagernet.R
import io.nekohasekai.sagernet.database.ProxyEntity

data class RemoteProfileFocus(val profileId: Long, val controlId: Int) {
    companion object {
        fun capture(focus: View?): RemoteProfileFocus? {
            var current = focus
            while (current != null) {
                val entity = current.getTag(R.id.remote_profile_entity) as? ProxyEntity
                if (entity != null) return RemoteProfileFocus(entity.id, focus?.id ?: R.id.content)
                current = current.parent as? View
            }
            return null
        }
        fun restore(list: RecyclerView, ids: List<Long>, anchor: RemoteProfileFocus) {
            val index = ids.indexOf(anchor.profileId)
            if (index < 0) return
            list.scrollToPosition(index)
            list.post {
                val item = list.findViewHolderForAdapterPosition(index)?.itemView ?: return@post
                val control = item.findViewById<View>(anchor.controlId)
                if (control?.isShown == true && control.isEnabled && control.isFocusable) control.requestFocus() else item.requestFocus()
            }
        }
    }
}

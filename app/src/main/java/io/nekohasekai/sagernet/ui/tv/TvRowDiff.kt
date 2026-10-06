package io.nekohasekai.sagernet.ui.tv

import androidx.leanback.widget.DiffCallback

object TvRowDiff : DiffCallback<Any>() {
    private fun id(item: Any): Long = when (item) { is TvAction -> item.id; is TvProfileCard -> item.id; else -> -1 }
    override fun areItemsTheSame(old: Any, new: Any) = old.javaClass == new.javaClass && id(old) == id(new)
    override fun areContentsTheSame(old: Any, new: Any) = old == new
}

package io.nekohasekai.sagernet.ui.tv

import androidx.leanback.widget.DiffCallback

object TvRowDiff : DiffCallback<Any>() {
    private fun id(item: Any): Long = when (item) { is TvQualityCard -> item.id;is TvConnectionCard -> item.id;is TvLanProxyCard -> item.id;is TvAction -> item.id; is TvProfileCard -> item.id; is TvGroupCard -> item.id;is TvFunctionCard -> item.id.toLong();is TvEmptyHint -> item.groupId; else -> -1 }
    override fun areItemsTheSame(old: Any, new: Any) = old.javaClass == new.javaClass && id(old) == id(new)
    override fun areContentsTheSame(old: Any, new: Any) = old == new
}

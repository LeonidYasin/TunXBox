package io.nekohasekai.sagernet.ui.tv

import androidx.leanback.widget.BaseGridView
import androidx.leanback.widget.FocusHighlight
import androidx.leanback.widget.ListRowPresenter
import androidx.leanback.widget.RowPresenter

/** Horizontal touch scroll must not resize/recenter every row under the user's finger. */
class StableTvRowPresenter(private val compact: Boolean) :
    ListRowPresenter(if (compact) FocusHighlight.ZOOM_FACTOR_NONE else FocusHighlight.ZOOM_FACTOR_SMALL) {
    override fun initializeRowViewHolder(holder: RowPresenter.ViewHolder) {
        super.initializeRowViewHolder(holder)
        stabilize(holder)
        if (compact) (holder as ListRowPresenter.ViewHolder).gridView.setFocusScrollStrategy(BaseGridView.FOCUS_SCROLL_ITEM)
    }
    override fun onRowViewSelected(holder: RowPresenter.ViewHolder, selected: Boolean) {
        super.onRowViewSelected(holder, selected)
        stabilize(holder)
    }
    override fun onRowViewExpanded(holder: RowPresenter.ViewHolder, expanded: Boolean) {
        super.onRowViewExpanded(holder, expanded)
        stabilize(holder)
    }
    override fun freeze(holder: RowPresenter.ViewHolder, freeze: Boolean) {
        super.freeze(holder, freeze)
        stabilize(holder)
    }
    private fun stabilize(holder: RowPresenter.ViewHolder) {
        val grid = (holder as ListRowPresenter.ViewHolder).gridView
        grid.setAnimateChildLayout(false)
        if (compact) {
            val padding = (8 * grid.resources.displayMetrics.density).toInt()
            grid.setPadding(grid.paddingLeft, padding, grid.paddingRight, padding)
        }
    }
}

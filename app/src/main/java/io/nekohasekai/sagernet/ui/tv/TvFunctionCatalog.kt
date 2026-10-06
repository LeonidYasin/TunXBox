package io.nekohasekai.sagernet.ui.tv

import android.content.Context
import android.view.MenuInflater
import android.view.View
import android.widget.PopupMenu
import io.nekohasekai.sagernet.R

/** One source of truth: the exact upstream navigation menu, not a partial duplicate. */
object TvFunctionCatalog {
    data class Entry(val id: Int, val title: String)
    fun entries(context: Context, dashboardEnabled: Boolean, playFlavor: Boolean): List<Entry> {
        val menu = PopupMenu(context, View(context)).menu
        MenuInflater(context).inflate(R.menu.main_drawer_menu, menu)
        return (0 until menu.size()).map { menu.getItem(it) }.filter {
            it.isVisible && it.itemId !in setOf(R.id.nav_switch_tv_mode, R.id.nav_restart_app, R.id.nav_close_app) &&
                (it.itemId != R.id.nav_traffic || dashboardEnabled) &&
                (it.itemId != R.id.nav_tuiguang || !playFlavor)
        }.map { Entry(it.itemId, it.title.toString()) }
    }
}

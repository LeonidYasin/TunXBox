package io.nekohasekai.sagernet.ui.tv

import io.nekohasekai.sagernet.database.DataStore

/**
 * Persisted per-group collapse state for TV group rows. Expanded is the default, so existing
 * installs keep their current layout until the owner collapses a group explicitly.
 */
object TvGroupViewPreferences {
    private const val KEY_COLLAPSED = "tv_collapsed_groups"

    var collapsed: Set<Long>
        get() = TvCollapsedGroups.decode(DataStore.configurationStore.getString(KEY_COLLAPSED))
        set(value) {
            DataStore.configurationStore.putString(KEY_COLLAPSED, TvCollapsedGroups.encode(value))
        }

    fun isCollapsed(groupId: Long): Boolean = groupId in collapsed

    fun setCollapsed(groupId: Long, collapsedState: Boolean) {
        if (groupId <= 0L) return
        collapsed = if (collapsedState) collapsed + groupId else collapsed - groupId
    }

    /** Drops ids of deleted groups so the preference cannot grow without bound. */
    fun forget(groupIds: Collection<Long>) {
        if (groupIds.isNotEmpty()) collapsed = collapsed - groupIds.toSet()
    }
}

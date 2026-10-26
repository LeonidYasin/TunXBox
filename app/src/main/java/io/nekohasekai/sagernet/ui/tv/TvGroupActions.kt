package io.nekohasekai.sagernet.ui.tv

import io.nekohasekai.sagernet.GroupOrder

/**
 * Actions offered by the group tile menu. The catalog is pure so the availability rules stay
 * covered by JVM tests; the fragment only maps entries to strings and executes them.
 */
enum class TvGroupAction { SELECT, CHECK, UPDATE, SORT, TOGGLE_PROFILES, RENAME, DELETE }

data class TvGroupActionState(
    val subscription: Boolean,
    val selected: Boolean,
    val collapsed: Boolean,
    val profileCount: Int,
)

object TvGroupActionCatalog {
    /**
     * Stable menu order: orientation first, destructive action last. Entering the menu never
     * changes the selected group or the VPN; only an explicit entry does.
     */
    fun entries(state: TvGroupActionState): List<TvGroupAction> = buildList {
        if (!state.selected) add(TvGroupAction.SELECT)
        if (state.profileCount > 0) add(TvGroupAction.CHECK)
        if (state.subscription) add(TvGroupAction.UPDATE)
        add(TvGroupAction.SORT)
        add(TvGroupAction.TOGGLE_PROFILES)
        add(TvGroupAction.RENAME)
        add(TvGroupAction.DELETE)
    }
}

/** Profile sorting mirrors the phone group menu; unknown values fall back to the stored order. */
object TvGroupSort {
    val orders: List<Int> = listOf(GroupOrder.ORIGIN, GroupOrder.BY_NAME, GroupOrder.BY_DELAY)

    fun indexOf(current: Int): Int = orders.indexOf(current).takeIf { it >= 0 } ?: 0

    fun orderAt(index: Int): Int? = orders.getOrNull(index)
}

/** Collapsed group ids persist as a stable comma separated list of positive ids. */
object TvCollapsedGroups {
    fun encode(ids: Set<Long>): String = ids.filter { it > 0L }.distinct().sorted().joinToString(",")

    fun decode(raw: String?): Set<Long> = raw
        ?.split(',')
        ?.mapNotNull { it.trim().toLongOrNull() }
        ?.filter { it > 0L }
        ?.toSet()
        ?: emptySet()
}

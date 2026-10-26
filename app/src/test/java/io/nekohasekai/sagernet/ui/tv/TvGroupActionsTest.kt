package io.nekohasekai.sagernet.ui.tv

import io.nekohasekai.sagernet.GroupOrder
import org.junit.Assert.*
import org.junit.Test

class TvGroupActionsTest {
    private fun state(
        subscription: Boolean = false,
        selected: Boolean = false,
        collapsed: Boolean = false,
        profileCount: Int = 2,
    ) = TvGroupActionState(subscription, selected, collapsed, profileCount)

    @Test fun unselectedSubscriptionOffersSelectAndUpdateInStableOrder() {
        assertEquals(
            listOf(
                TvGroupAction.SELECT,
                TvGroupAction.CHECK,
                TvGroupAction.UPDATE,
                TvGroupAction.SORT,
                TvGroupAction.TOGGLE_PROFILES,
                TvGroupAction.RENAME,
                TvGroupAction.DELETE,
            ),
            TvGroupActionCatalog.entries(state(subscription = true, profileCount = 4)),
        )
    }

    @Test fun selectedBasicGroupHasNoSelectOrUpdateEntry() {
        val entries = TvGroupActionCatalog.entries(state(selected = true, profileCount = 3))
        assertFalse(entries.contains(TvGroupAction.SELECT))
        assertFalse(entries.contains(TvGroupAction.UPDATE))
        assertEquals(TvGroupAction.DELETE, entries.last())
    }

    @Test fun emptyGroupCannotStartAPingCheck() {
        assertFalse(TvGroupActionCatalog.entries(state(profileCount = 0)).contains(TvGroupAction.CHECK))
    }

    @Test fun menuNeverDropsOrientationOrDestructiveActions() {
        val entries = TvGroupActionCatalog.entries(state(selected = true, profileCount = 1))
        assertTrue(entries.contains(TvGroupAction.SORT))
        assertTrue(entries.contains(TvGroupAction.TOGGLE_PROFILES))
        assertTrue(entries.contains(TvGroupAction.RENAME))
        assertTrue(entries.contains(TvGroupAction.DELETE))
        assertEquals(entries.size, entries.toSet().size)
    }

    @Test fun collapsedFlagDoesNotChangeAvailableActions() {
        assertEquals(
            TvGroupActionCatalog.entries(state(collapsed = false)),
            TvGroupActionCatalog.entries(state(collapsed = true)),
        )
    }

    @Test fun sortIndexAndOrderRoundTrip() {
        assertEquals(listOf(GroupOrder.ORIGIN, GroupOrder.BY_NAME, GroupOrder.BY_DELAY), TvGroupSort.orders)
        for ((index, order) in TvGroupSort.orders.withIndex()) assertEquals(index, TvGroupSort.indexOf(order))
        assertEquals(0, TvGroupSort.indexOf(99))
        assertEquals(GroupOrder.BY_DELAY, TvGroupSort.orderAt(2))
        assertNull(TvGroupSort.orderAt(3))
        assertNull(TvGroupSort.orderAt(-1))
    }

    @Test fun collapsedGroupsEncodeStablyAndIgnoreInvalidIds() {
        assertEquals("3,7,11", TvCollapsedGroups.encode(setOf(11L, 3L, 7L, 3L)))
        assertEquals(emptySet<Long>(), TvCollapsedGroups.decode(null))
        assertEquals(emptySet<Long>(), TvCollapsedGroups.decode(""))
        assertEquals(setOf(4L, 9L), TvCollapsedGroups.decode(" 9 , 4 ,oops,-2,0"))
        assertEquals("4,9", TvCollapsedGroups.encode(TvCollapsedGroups.decode("9,4,4")))
    }
}

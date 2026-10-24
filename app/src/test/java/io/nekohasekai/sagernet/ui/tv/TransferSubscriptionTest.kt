package io.nekohasekai.sagernet.ui.tv

import io.nekohasekai.sagernet.GroupType
import org.junit.Assert.*
import org.junit.Test

/** Subscription groups must travel as their updatable URL, not as a frozen profile snapshot. */
class TransferSubscriptionTest {
    @Test fun subscriptionGroupExportsItsHttpUrl() {
        assertEquals("https://subscriptions.example.invalid/opaque-token",
            TransferSelection.subscriptionUrl(GroupType.SUBSCRIPTION, " https://subscriptions.example.invalid/opaque-token "))
        assertEquals("https://example.invalid/",
            TransferSelection.subscriptionUrl(GroupType.SUBSCRIPTION, "https://example.invalid/"))
    }

    @Test fun nonSubscriptionGroupsAndInvalidLinksStayGroups() {
        assertNull(TransferSelection.subscriptionUrl(GroupType.BASIC, "https://example.invalid/sub"))
        assertNull(TransferSelection.subscriptionUrl(GroupType.SUBSCRIPTION, "file:///secret"))
        assertNull(TransferSelection.subscriptionUrl(GroupType.SUBSCRIPTION, null))
        assertNull(TransferSelection.subscriptionUrl(GroupType.SUBSCRIPTION, "https://example.invalid/secret bad"))
    }

    @Test fun pushPrefersSubscriptionUrlOverGroupSnapshot() {
        val url = TvTransferClient.pushBody(TransferExport("", 3, "https://subscriptions.example.invalid/token"))
        assertEquals("subscription_url", url.field)
        assertEquals("https://subscriptions.example.invalid/token", url.value)

        val group = TvTransferClient.pushBody(TransferExport("{\"tunxbox_group\":1}", 2))
        assertEquals("profiles", group.field)
    }

    @Test fun pullPrefersSubscriptionUrlOverSnapshot() {
        val subscription = TvTransferClient.pullTarget("https://subscriptions.example.invalid/token", "{\"tunxbox_group\":1}")
        assertTrue(subscription is TvTransferClient.PullTarget.Subscription)
        assertEquals("https://subscriptions.example.invalid/token", (subscription as TvTransferClient.PullTarget.Subscription).url)

        val profiles = TvTransferClient.pullTarget(null, "{\"tunxbox_group\":1}")
        assertTrue(profiles is TvTransferClient.PullTarget.Profiles)
    }
}

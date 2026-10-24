package io.nekohasekai.sagernet.ui.tv

import io.nekohasekai.sagernet.GroupType

/** Chooses what a group transfer actually carries. Pure, so it stays independent of Room/Android. */
object TransferSelection {
    /** A subscription group travels as its updatable HTTP(S) URL, not a frozen profile snapshot.
     * Non-subscription groups and non-HTTP(S) links return null (fall back to the group snapshot). */
    fun subscriptionUrl(type: Int, link: String?): String? {
        if (type != GroupType.SUBSCRIPTION) return null
        val text = link?.trim().orEmpty()
        return runCatching { TransferImportInput.requireHttpSubscription(text) }.getOrNull()
    }
}

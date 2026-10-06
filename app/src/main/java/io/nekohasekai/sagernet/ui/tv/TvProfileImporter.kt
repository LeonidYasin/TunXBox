package io.nekohasekai.sagernet.ui.tv

import android.net.Uri
import io.nekohasekai.sagernet.GroupType
import io.nekohasekai.sagernet.database.DataStore
import io.nekohasekai.sagernet.database.GroupManager
import io.nekohasekai.sagernet.database.ProfileManager
import io.nekohasekai.sagernet.database.ProxyGroup
import io.nekohasekai.sagernet.database.SagerDatabase
import io.nekohasekai.sagernet.database.SubscriptionBean
import io.nekohasekai.sagernet.group.GroupUpdater
import io.nekohasekai.sagernet.group.RawUpdater

/** Uses upstream parsers and subscription updater, rather than a TV-only config format. */
object TvProfileImporter {
    suspend fun importProfiles(text: String): Int {
        require(text.toByteArray(Charsets.UTF_8).size <= TransferProtocol.MAX_BODY_BYTES) { "Configuration too large" }
        val proxies = RawUpdater.parseRaw(text)
        require(!proxies.isNullOrEmpty()) { "No valid proxy profiles found" }
        val targetId = DataStore.selectedGroupForImport()
        proxies.forEach { ProfileManager.createProfile(targetId, it) }
        DataStore.selectedGroup = targetId
        return proxies.size
    }

    suspend fun importSubscription(value: String): Int {
        val uri = Uri.parse(value.trim())
        val url = if (uri.scheme == "sn" || uri.scheme == "clash") uri.getQueryParameter("url") else value.trim()
        require(!url.isNullOrBlank()) { "Use a subscription HTTP(S) URL" }
        val source = Uri.parse(url)
        require(source.scheme == "https" || source.scheme == "http") { "Subscription requires HTTP(S)" }
        require(!source.host.isNullOrBlank()) { "Invalid subscription URL" }
        val group = ProxyGroup(type = GroupType.SUBSCRIPTION).apply {
            name = (if (uri.scheme == "sn" || uri.scheme == "clash") uri.getQueryParameter("name") else null)
                ?: "Subscription #${System.currentTimeMillis()}"
            subscription = SubscriptionBean().apply { link = url }
        }
        GroupManager.createGroup(group)
        // The user explicitly requested this import. Use the upstream downloader/parser and
        // retain the URL for later updates, including Subscription-Userinfo metadata.
        if (!GroupUpdater.executeUpdate(group, false, null)) {
            GroupManager.deleteGroup(group.id)
            error("Subscription download/import failed. Check the URL and connection.")
        }
        DataStore.selectedGroup = group.id
        return SagerDatabase.proxyDao.getByGroup(group.id).size
    }
}

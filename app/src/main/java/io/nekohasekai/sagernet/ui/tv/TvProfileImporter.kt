package io.nekohasekai.sagernet.ui.tv

import android.net.Uri
import io.nekohasekai.sagernet.GroupType
import io.nekohasekai.sagernet.R
import io.nekohasekai.sagernet.ktx.app
import io.nekohasekai.sagernet.ktx.SubscriptionFoundException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
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
        if (TransferImportInput.isSubscription(text)) return importSubscription(text)
        val proxies = try { RawUpdater.parseRaw(text) }
        catch (subscription: SubscriptionFoundException) { return importSubscription(subscription.link) }
        if (proxies.isNullOrEmpty()) throw TransferImportFailure(TransferImportError.PROFILE_INVALID)
        val targetId = DataStore.selectedGroupForImport()
        proxies.forEach { ProfileManager.createProfile(targetId, it) }
        DataStore.selectedGroup = targetId
        return proxies.size
    }

    suspend fun importSubscription(value: String): Int {
        val uri = Uri.parse(value.trim())
        val url = if (uri.scheme?.lowercase() == "sn" || uri.scheme?.lowercase() == "clash") uri.getQueryParameter("url") else value.trim()
        if (url.isNullOrBlank()) throw TransferImportFailure(TransferImportError.SUBSCRIPTION_INVALID)
        val normalizedUrl = TransferImportInput.requireHttpSubscription(url)
        val group = ProxyGroup(type = GroupType.SUBSCRIPTION).apply {
            name = (if (uri.scheme?.lowercase() == "sn" || uri.scheme?.lowercase() == "clash") uri.getQueryParameter("name") else null)
                ?: "Subscription #${System.currentTimeMillis()}"
            subscription = SubscriptionBean().apply { link = normalizedUrl }
        }
        GroupManager.createGroup(group)
        // The user explicitly requested this import. Use the upstream downloader/parser and
        // retain the URL for later updates, including Subscription-Userinfo metadata.
        try {
            if (!GroupUpdater.executeUpdate(group, false, null, throwOnFailure = true)) {
                throw TransferImportFailure(TransferImportError.SUBSCRIPTION_FAILED)
            }
            val count = SagerDatabase.proxyDao.getByGroup(group.id).size
            if (count == 0) throw TransferImportFailure(TransferImportError.SUBSCRIPTION_FORMAT)
            DataStore.selectedGroup = group.id
            return count
        } catch (failure: Exception) {
            // Never leave an empty/partially imported subscription group after failure/cancellation.
            withContext(NonCancellable) { GroupManager.deleteGroup(group.id) }
            if (failure is CancellationException) throw failure
            if (failure is TransferImportFailure) throw failure
            val noProfiles = failure.message == app.getString(R.string.no_proxies_found) ||
                failure.message == app.getString(R.string.no_proxies_found_in_subscription)
            throw TransferImportFailure(if (noProfiles) TransferImportError.SUBSCRIPTION_FORMAT else TransferImportError.SUBSCRIPTION_FAILED)
        }
    }
}

package io.nekohasekai.sagernet.ui.lan

import android.content.Context
import androidx.room.withTransaction
import io.nekohasekai.sagernet.GroupType
import io.nekohasekai.sagernet.database.*
import io.nekohasekai.sagernet.fmt.http.HttpBean
import io.nekohasekai.sagernet.fmt.socks.SOCKSBean
import io.nekohasekai.sagernet.ktx.app
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** Only profiles CREATED by this action are managed. Explicit refresh changes only address.
 * Existing/manual profiles are never adopted, subscriptions never mutated, VPN never restarted. */
object GatewayProfileStore {
    private val mutex = Mutex()
    private fun prefs() = app.getSharedPreferences("gateway_managed_profiles_v1", Context.MODE_PRIVATE)
    suspend fun save(candidate: ProxyCandidate, http: Boolean, group: Long, name: String, username: String, password: String): LanProfileStore.Saved = mutex.withLock {
        require(candidate.port == GatewayProfileTargets.PORT && LanScope.ipv4(candidate.host)?.let(LanScope::privateAddress) == true)
        val key = "$group:${if (http) "http" else "socks"}"
        val managedId = runCatching { prefs().getLong(key, 0) }.getOrDefault(0)
        val updated = SagerDatabase.instance.withTransaction {
            require(SagerDatabase.groupDao.getById(group)?.type == GroupType.BASIC)
            val entity = SagerDatabase.proxyDao.getById(managedId)?.takeIf { it.groupId == group }
            val bean = entity?.requireBean()
            if (entity != null && bean != null && bean.serverPort == GatewayProfileTargets.PORT &&
                (http && bean is HttpBean || !http && bean is SOCKSBean && bean.protocol == SOCKSBean.PROTOCOL_SOCKS5)) {
                check(!(DataStore.serviceState !in setOf(io.nekohasekai.sagernet.bg.BaseService.State.Idle, io.nekohasekai.sagernet.bg.BaseService.State.Stopped) && DataStore.currentProfile == entity.id)) { "Active gateway profile must be stopped before address refresh" }
                bean.serverAddress = candidate.host
                entity.putBean(bean)
                SagerDatabase.proxyDao.updateProxy(entity)
                entity
            } else null
        }
        if (updated != null) {
            ProfileManager.postUpdate(updated, true); GroupManager.postReload(group)
            LanProfileStore.Saved(updated, false)
        } else {
            val saved = LanProfileStore.save(candidate, http, group, name, username, password, protocolConfirmed = true)
            if (saved.created) {
                // ID-only metadata, bounded to 64 managed group/protocol entries; no credentials.
                val p = prefs(); val keys = p.all.keys.filter { it != key }.takeLast(63).toSet() + key
                p.edit().apply { p.all.keys.filter { it !in keys }.forEach { remove(it) }; putLong(key, saved.profile.id) }.apply()
            }
            saved
        }
    }
}

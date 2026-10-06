package io.nekohasekai.sagernet.ui.lan

import androidx.room.withTransaction
import io.nekohasekai.sagernet.ktx.applyDefaultValues
import io.nekohasekai.sagernet.GroupType
import io.nekohasekai.sagernet.database.*
import io.nekohasekai.sagernet.fmt.AbstractBean
import io.nekohasekai.sagernet.fmt.http.HttpBean
import io.nekohasekai.sagernet.fmt.socks.SOCKSBean
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext

/** Explicit save only. Does not select a profile, start VPN or append to subscription groups. */
object LanProfileStore {
    data class Saved(val profile: ProxyEntity, val created: Boolean)
    suspend fun save(candidate: ProxyCandidate, http: Boolean, groupId: Long, name: String, username: String, password: String): Saved {
        require(LanScope.ipv4(candidate.host)?.let(LanScope::privateAddress) == true)
        require(candidate.port in 1..65535 && name.length <= 128 && username.length <= 256 && password.length <= 256)
        require(when (candidate.kind) {
            ProbeKind.SOCKS5, ProbeKind.SOCKS5_AUTH -> !http
            ProbeKind.HTTP_AUTH -> http
            else -> true
        })
        require(candidate.kind !in setOf(ProbeKind.SOCKS5_AUTH, ProbeKind.HTTP_AUTH) || username.isNotBlank())
        val bean: AbstractBean = if (http) HttpBean().apply { this.username = username; this.password = password }
            else SOCKSBean().apply { this.username = username; this.password = password; protocol = SOCKSBean.PROTOCOL_SOCKS5 }
        bean.name = name; bean.serverAddress = candidate.host; bean.serverPort = candidate.port; bean.applyDefaultValues()
        val saved = SagerDatabase.instance.withTransaction {
            require(SagerDatabase.groupDao.getById(groupId)?.type == GroupType.BASIC)
            val existing = SagerDatabase.proxyDao.getByGroup(groupId).firstOrNull { profile ->
                val previous = profile.requireBean()
                previous.serverAddress == candidate.host && previous.serverPort == candidate.port &&
                    if (http) previous is HttpBean && previous.username == username && previous.password == password
                    else previous is SOCKSBean && previous.protocol == SOCKSBean.PROTOCOL_SOCKS5 && previous.username == username && previous.password == password
            }
            if (existing != null) Saved(existing, false) else {
                val profile = ProxyEntity(groupId = groupId).apply { putBean(bean); userOrder = SagerDatabase.proxyDao.nextOrder(groupId) ?: 1 }
                profile.id = SagerDatabase.proxyDao.addProxy(profile)
                Saved(profile, true)
            }
        }
        // Transaction is committed before any UI observer can report success.
        if (saved.created) withContext(NonCancellable) {
            ProfileManager.iterator { onAdd(saved.profile) }
            GroupManager.postReload(groupId)
        }
        return saved
    }
}

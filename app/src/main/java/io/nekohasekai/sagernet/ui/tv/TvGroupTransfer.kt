package io.nekohasekai.sagernet.ui.tv

import androidx.room.withTransaction
import io.nekohasekai.sagernet.GroupType
import io.nekohasekai.sagernet.database.*
import io.nekohasekai.sagernet.fmt.TypeMap
import io.nekohasekai.sagernet.fmt.KryoConverters
import io.nekohasekai.sagernet.ktx.applyDefaultValues
import moe.matsuri.nb4a.utils.Util
import io.nekohasekai.sagernet.fmt.toUniversalLink
import org.json.JSONArray
import org.json.JSONObject

data class TransferExport(val profiles: String, val count: Int)

/** Profile-group snapshot, not global VPN/settings/subscription credentials.
 * IDs are local to a device: validate all dependencies first, then remap atomically.
 */
object TvGroupTransfer {
    private fun validateChains(ids: Set<Long>, chains: Map<Long, List<Long>>) {
        require(chains.values.flatten().all { it in ids })
        val ready = (ids - chains.keys).toMutableSet()
        val remaining = chains.toMutableMap()
        while (remaining.isNotEmpty()) {
            val available = remaining.filterValues { links -> links.all { it in ready } }.keys
            require(available.isNotEmpty()) { "Cyclic chain dependencies" }
            ready.addAll(available); available.forEach { remaining.remove(it) }
        }
    }
    fun isGroup(text: String) = text.trimStart().startsWith("{") && runCatching {
        JSONObject(text).has("tunxbox_group")
    }.getOrDefault(false)
    suspend fun exportGroup(groupId: Long): TransferExport {
        val group = requireNotNull(SagerDatabase.groupDao.getById(groupId))
        val profiles = SagerDatabase.proxyDao.getByGroup(groupId)
        require(profiles.isNotEmpty() && profiles.size <= 4096)
        val ids = profiles.map { it.id }.toSet()
        fun local(id: Long) = id <= 0 || id in ids
        require(local(group.frontProxy) && local(group.landingProxy))
        validateChains(ids, profiles.filter { it.chainBean != null }.associate { it.id to it.chainBean!!.proxies.toList() })
        val entries = JSONArray()
        var rawBytes = 0
        for (profile in profiles) {
            rawBytes += KryoConverters.serialize(profile.requireBean()).size
            require(rawBytes <= TransferProtocol.MAX_BODY_BYTES)
            require(profile.chainBean?.proxies?.all { it in ids } != false)
            // Universal format supports custom configs/internal beans too; never silently skip a profile.
            entries.put(JSONObject().put("sourceId", profile.id).put("link", profile.requireBean().toUniversalLink()))
        }
        val data = JSONObject().put("tunxbox_group", 1).put("name", group.displayName())
            .put("isSelector", group.isSelector).put("order", group.order).put("frontProxy", group.frontProxy)
            .put("landingProxy", group.landingProxy).put("profiles", entries).toString()
        require(data.toByteArray(Charsets.UTF_8).size <= TransferProtocol.MAX_BODY_BYTES)
        return TransferExport(data, profiles.size)
    }
    suspend fun importGroup(text: String): Int {
        require(text.toByteArray(Charsets.UTF_8).size <= TransferProtocol.MAX_BODY_BYTES)
        val data = JSONObject(text)
        require(data.getInt("tunxbox_group") == 1)
        val entries = data.getJSONArray("profiles")
        require(entries.length() in 1..4096)
        var decodedBytes = 0
        val decoded = (0 until entries.length()).map { index ->
            val entry = entries.getJSONObject(index)
            val id = entry.getLong("sourceId"); require(id > 0)
            val link = entry.getString("link"); require(link.startsWith("sn://") && !link.startsWith("sn://subscription"))
            require(link.contains("?"))
            val type = requireNotNull(TypeMap[link.substringAfter("sn://").substringBefore("?")])
            val bytes = java.util.zip.InflaterInputStream(java.io.ByteArrayInputStream(Util.b64Decode(link.substringAfter("?"))))
                .use { TransferProtocol.readLimited(it) }
            decodedBytes += bytes.size; require(decodedBytes <= TransferProtocol.MAX_BODY_BYTES)
            val bean = ProxyEntity(type = type).apply { putByteArray(bytes) }.requireBean().applyDefaultValues()
            id to bean
        }
        val sourceIds = decoded.map { it.first }.toSet(); require(sourceIds.size == decoded.size)
        val front = data.optLong("frontProxy", -1); val landing = data.optLong("landingProxy", -1)
        fun local(id: Long) = id <= 0 || id in sourceIds
        require(local(front) && local(landing))
        for ((_, bean) in decoded) if (bean is io.nekohasekai.sagernet.fmt.internal.ChainBean)
            require(bean.proxies.all { it in sourceIds })
        validateChains(sourceIds, decoded.filter { it.second is io.nekohasekai.sagernet.fmt.internal.ChainBean }
            .associate { it.first to (it.second as io.nekohasekai.sagernet.fmt.internal.ChainBean).proxies.toList() })
        val order = data.optInt("order", 0); require(order in 0..2)
        val group = ProxyGroup(type = GroupType.BASIC, name = data.optString("name").take(256), isSelector = data.optBoolean("isSelector"), order = order)
        SagerDatabase.instance.withTransaction {
            group.userOrder = SagerDatabase.groupDao.nextOrder() ?: 1
            group.id = SagerDatabase.groupDao.createGroup(group)
            val map = mutableMapOf<Long, Long>()
            val added = decoded.mapIndexed { index, (sourceId, bean) ->
                val profile = ProxyEntity(groupId = group.id, userOrder = index.toLong() + 1).apply { putBean(bean) }
                profile.id = SagerDatabase.proxyDao.addProxy(profile); map[sourceId] = profile.id
                profile
            }
            for (profile in added) profile.chainBean?.let { chain ->
                chain.proxies = chain.proxies.map { requireNotNull(map[it]) }
                SagerDatabase.proxyDao.updateProxy(profile)
            }
            group.frontProxy = if (front > 0) requireNotNull(map[front]) else -1
            group.landingProxy = if (landing > 0) requireNotNull(map[landing]) else -1
            SagerDatabase.groupDao.updateGroup(group)
        }
        DataStore.selectedGroup = group.id
        GroupManager.iterator { groupAdd(group) }
        GroupManager.postReload(group.id)
        return decoded.size
    }
}

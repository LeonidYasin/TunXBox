package io.nekohasekai.sagernet.ui.lan

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import java.net.Inet4Address

/** Never use the default network: it may be an active VPN or cellular route. */
data class LanNetwork(val network: Network, val scope: LanScope, val fingerprint: String)
class LanEnvironment(context: Context) {
    val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    fun networks(): List<LanNetwork> = manager.allNetworks.flatMap { network ->
        val caps = manager.getNetworkCapabilities(network) ?: return@flatMap emptyList<LanNetwork>()
        if (!caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_VPN) ||
            !(caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET))) return@flatMap emptyList<LanNetwork>()
        val links = manager.getLinkProperties(network) ?: return@flatMap emptyList<LanNetwork>()
        val localAddresses = manager.allNetworks.flatMap { manager.getLinkProperties(it)?.linkAddresses.orEmpty() }.mapNotNull { it.address.hostAddress }.toSet()
        val excluded = localAddresses + links.routes.mapNotNull { it.gateway?.hostAddress }
        links.linkAddresses.filter { it.address is Inet4Address }.mapNotNull { address ->
            val host = address.address.hostAddress ?: return@mapNotNull null
            val scope = LanScope.create(host, address.prefixLength, excluded) ?: return@mapNotNull null
            val fingerprint = listOf(network.toString(), links.interfaceName, links.linkAddresses.toString(), links.routes.toString()).joinToString("|")
            LanNetwork(network, scope, fingerprint)
        }
    }
    fun current(selected: LanNetwork) = networks().any { it.network == selected.network && it.scope == selected.scope && it.fingerprint == selected.fingerprint }
}

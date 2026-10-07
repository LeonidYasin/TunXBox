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
    fun networks(): List<LanNetwork> = manager.allNetworks.mapNotNull { network ->
        val caps = manager.getNetworkCapabilities(network) ?: return@mapNotNull null
        if (!caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_VPN) ||
            caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN) ||
            !(caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET))) return@mapNotNull null
        val links = manager.getLinkProperties(network) ?: return@mapNotNull null
        val addresses = links.linkAddresses.filter { it.address is Inet4Address }.mapNotNull { address ->
            address.address.hostAddress?.let { it to address.prefixLength }
        }
        // Only on-link gateways admitted by LanScope; never follow a public/default route.
        val scope = LanScope.create(addresses, links.routes.mapNotNull { it.gateway?.hostAddress }.toSet()) ?: return@mapNotNull null
        val fingerprint = listOf(network.toString(), links.interfaceName, links.linkAddresses.toString(), links.routes.toString()).joinToString("|")
        LanNetwork(network, scope, fingerprint)
    }
    /** Read-only inventory, including ineligible networks. Never used as scan targets. */
    fun observed(): String = manager.allNetworks.joinToString("\n\n") { network ->
        val links = manager.getLinkProperties(network)
        val caps = manager.getNetworkCapabilities(network)
        val type = when {
            caps?.hasTransport(NetworkCapabilities.TRANSPORT_VPN) == true -> "VPN"
            caps?.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) == true -> "Ethernet"
            caps?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true -> "Wi-Fi"
            caps?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true -> "Cellular"
            else -> "Network"
        }
        "$type • ${links?.interfaceName.orEmpty()}\nIP: ${links?.linkAddresses.orEmpty().joinToString()}\nGateway: ${links?.routes.orEmpty().mapNotNull { it.gateway?.hostAddress }.distinct().joinToString()}"
    }
    fun current(selected: LanNetwork) = networks().any { it.network == selected.network && it.scope == selected.scope && it.fingerprint == selected.fingerprint }
}

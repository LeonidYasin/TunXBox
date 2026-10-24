package io.nekohasekai.sagernet.ui.tv

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Build
import androidx.annotation.RequiresApi
import io.nekohasekai.sagernet.ktx.app
import io.nekohasekai.sagernet.ui.lan.LanScope
import java.net.HttpURLConnection
import java.net.Inet4Address
import java.net.URL

/**
 * Opens the transfer request on the physical Wi-Fi/Ethernet network instead of the active VPN.
 * Without this, an active TunXBox VPN owns the default route, captures the private-LAN request
 * (0.0.0.0/0 unless "bypass LAN" is enabled), and the peer never receives it.
 * Best-effort: falls back to the plain connection when no physical network is available.
 */
internal fun openLanConnection(url: URL): HttpURLConnection {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return url.openConnection() as HttpURLConnection
    val manager = app.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        ?: return url.openConnection() as HttpURLConnection
    val network = physicalNetworkFor(manager, url.host) ?: return url.openConnection() as HttpURLConnection
    return network.openConnection(url) as HttpURLConnection
}

/** Prefer the physical network whose on-link IPv4 subnet contains [host]; only fall back to an
 * arbitrary physical network when a VPN is actually capturing the default route. */
@RequiresApi(Build.VERSION_CODES.M)
private fun physicalNetworkFor(manager: ConnectivityManager, host: String): Network? {
    val physical = manager.allNetworks.filter { network ->
        val caps = manager.getNetworkCapabilities(network) ?: return@filter false
        caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_VPN) &&
            !caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN) &&
            (caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET))
    }
    physical.firstOrNull { subnetContains(manager, it, host) }?.let { return it }
    // With no matching physical subnet, only override the default route when a VPN owns it.
    val vpnCapturing = manager.activeNetwork
        ?.let { manager.getNetworkCapabilities(it) }
        ?.hasTransport(NetworkCapabilities.TRANSPORT_VPN) == true
    return if (vpnCapturing) physical.firstOrNull() else null
}

private fun subnetContains(manager: ConnectivityManager, network: Network, host: String): Boolean {
    val target = LanScope.ipv4(host) ?: return false
    val links = manager.getLinkProperties(network) ?: return false
    return links.linkAddresses.any { link ->
        val address = link.address
        if (address !is Inet4Address) return@any false
        val local = LanScope.ipv4(address.hostAddress ?: return@any false) ?: return@any false
        val prefix = link.prefixLength
        if (prefix !in 1..32) return@any false
        val mask = (0xffffffffL shl (32 - prefix)) and 0xffffffffL
        (local and mask) == (target and mask)
    }
}

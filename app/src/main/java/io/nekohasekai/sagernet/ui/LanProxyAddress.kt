package io.nekohasekai.sagernet.ui

import java.net.Inet4Address
import java.net.NetworkInterface
import java.util.Collections

/**
 * Addresses other devices may use for the local mixed inbound while "Allow connections from the
 * LAN" is enabled. Which of them a client can actually reach depends on the network (Wi-Fi
 * hotspot over mobile data, Ethernet, tethering, VPN), and the app cannot know that: so every
 * non-loopback IPv4 address is reported instead of guessing one "local" address. IPv6 is not
 * listed because the inbound binds IPv4 `0.0.0.0` and would not accept those connections.
 *
 * Selection and ordering are pure and covered by JVM tests; only [find] touches the system.
 */
object LanProxyAddress {
    data class Candidate(
        val interfaceName: String,
        val hostAddress: String?,
        val up: Boolean,
        val loopback: Boolean,
        val ipv4: Boolean,
    )

    /** One address of this device together with the interface it belongs to. */
    data class Entry(val host: String, val interfaceName: String)

    /** Wi-Fi/Ethernet/hotspot first: those are the addresses clients normally use. */
    private fun rank(interfaceName: String): Int = when {
        interfaceName.startsWith("wlan") -> 0
        interfaceName.startsWith("eth") -> 1
        interfaceName.startsWith("ap") || interfaceName.startsWith("swlan") || interfaceName.startsWith("softap") -> 2
        else -> 3
    }

    /** `host:port`, or null when there is nothing a client could actually enter. */
    fun format(host: String?, port: Int): String? {
        val address = host?.trim().orEmpty()
        if (address.isEmpty() || port !in 1..65535) return null
        return "$address:$port"
    }

    /** Loopback is useless for clients; IPv6 is not served by the IPv4 inbound. */
    fun accepts(candidate: Candidate): Boolean = candidate.up &&
        !candidate.loopback &&
        candidate.ipv4 &&
        candidate.hostAddress?.isNotBlank() == true

    /** All addresses, Wi-Fi/Ethernet first, then by interface name; duplicated hosts collapsed. */
    fun collect(candidates: List<Candidate>): List<Entry> = candidates
        .filter(::accepts)
        .sortedWith(
            compareBy<Candidate>(
                { candidate -> rank(candidate.interfaceName) },
                { candidate -> candidate.interfaceName },
                { candidate -> candidate.hostAddress },
            ),
        )
        .mapNotNull { candidate ->
            val host = candidate.hostAddress?.trim()?.takeIf { it.isNotEmpty() } ?: return@mapNotNull null
            Entry(host, candidate.interfaceName)
        }
        .distinctBy { it.host }

    fun find(): List<Entry> = collect(enumerate())

    private fun enumerate(): List<Candidate> = try {
        Collections.list(NetworkInterface.getNetworkInterfaces()).flatMap { network ->
            val name = network.name.orEmpty()
            val up = network.isUp
            val loopback = network.isLoopback
            Collections.list(network.inetAddresses).map { address ->
                Candidate(
                    interfaceName = name,
                    hostAddress = address.hostAddress,
                    up = up,
                    loopback = loopback,
                    ipv4 = address is Inet4Address,
                )
            }
        }
    } catch (_: Exception) {
        emptyList()
    }
}

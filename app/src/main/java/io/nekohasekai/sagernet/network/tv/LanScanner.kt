package io.nekohasekai.sagernet.network.tv

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import java.net.InetSocketAddress
import java.net.Socket

class LanScanner {
    suspend fun scanNetwork(subnet: String = "192.168.1", ports: List<Int> = listOf(1080, 8080, 8118, 7890)): List<DiscoveredProxy> = withContext(Dispatchers.IO) {
        val foundProxies = mutableListOf<DiscoveredProxy>()
        
        coroutineScope {
            val jobs = (1..254).map { host ->
                async {
                    val ip = "$subnet.$host"
                    for (port in ports) {
                        if (isPortOpen(ip, port)) {
                            val type = when (port) {
                                1080 -> "SOCKS5"
                                8080, 8118, 7890 -> "HTTP"
                                else -> "UNKNOWN"
                            }
                            synchronized(foundProxies) {
                                foundProxies.add(DiscoveredProxy(ip, port, type))
                            }
                        }
                    }
                }
            }
            jobs.awaitAll()
        }
        
        return@withContext foundProxies
    }

    private fun isPortOpen(ip: String, port: Int): Boolean {
        return try {
            Socket().use { socket ->
                socket.connect(InetSocketAddress(ip, port), 150)
                true
            }
        } catch (e: Exception) {
            false
        }
    }
}
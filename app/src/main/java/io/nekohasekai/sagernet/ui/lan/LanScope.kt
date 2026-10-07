package io.nekohasekai.sagernet.ui.lan

/** Bounded, on-link RFC1918 IPv4 only. Gateway and local listeners are intentional targets. */
data class LanScope private constructor(val cidr: String, val localAddress: String, val hosts: List<String>,
    val gateways: List<String> = emptyList(), val localAddresses: List<String> = listOf(localAddress),
    val limited: Boolean = false) {
    fun contains(host: String) = host in hosts
    companion object {
        fun ipv4(text: String): Long? {
            val parts = text.split('.')
            if (parts.size != 4 || parts.any { it.isEmpty() || it.length > 3 || it.any { c -> c !in '0'..'9' } }) return null
            val bytes = parts.map { it.toIntOrNull() ?: return null }
            if (bytes.any { it !in 0..255 } || bytes.map { it.toString() }.joinToString(".") != text) return null
            return bytes.fold(0L) { value, byte -> (value shl 8) or byte.toLong() }
        }
        private fun render(value: Long) = (3 downTo 0).joinToString(".") { ((value shr (it * 8)) and 255).toString() }
        fun privateAddress(value: Long) = value shr 24 == 10L || value shr 20 == 0xac1L || value shr 16 == 0xc0a8L
        fun create(local: String, prefix: Int, gateways: Set<String> = emptySet()): LanScope? =
            create(listOf(local to prefix), gateways)

        fun create(addresses: List<Pair<String, Int>>, gateways: Set<String>): LanScope? {
            val valid = addresses.distinct().filter { (host, prefix) ->
                val ip = ipv4(host)
                ip != null && privateAddress(ip) && prefix in 1..30
            }
            if (valid.isEmpty()) return null
            fun bounds(host: String, prefix: Int): Pair<Long, Long> {
                val mask = (0xffffffffL shl (32 - prefix)) and 0xffffffffL
                val network = ipv4(host)!! and mask
                return network to (network or (mask xor 0xffffffffL))
            }
            fun onLink(host: String): Boolean {
                val ip = ipv4(host) ?: return false
                return privateAddress(ip) && valid.any { (local, prefix) ->
                    val (first, last) = bounds(local, prefix)
                    ip > first && ip < last
                }
            }
            val locals = valid.map { it.first }.filter(::onLink).distinct()
            if (locals.isEmpty()) return null
            val routers = gateways.filter(::onLink).sortedBy { ipv4(it) }
            // Routers first; at most four deduplicated /24 (or narrower) windows.
            val seeds = (routers + locals).distinct()
            val windows = linkedMapOf<String, Pair<Long, Long>>()
            seeds.forEach { seed ->
                val link = valid.first { (local, prefix) ->
                    val (first, last) = bounds(local, prefix)
                    ipv4(seed)!! in (first + 1) until last
                }
                val prefix = maxOf(link.second, 24)
                val range = bounds(seed, prefix)
                windows.putIfAbsent("${render(range.first)}/$prefix", range)
            }
            val selected = windows.entries.take(4)
            val hosts = linkedSetOf<String>()
            // Include gateway/local seeds even at an artificial /24 edge of a wider subnet.
            hosts.addAll(seeds.take(8))
            selected.forEach { (_, range) ->
                ((range.first + 1) until range.second).map(::render).filter(::onLink).forEach { hosts.add(it) }
            }
            val targets = hosts.take(1024)
            return LanScope(selected.joinToString(", ") { it.key }, locals.first(), targets,
                routers.filter { it in targets }, locals, windows.size > 4 || seeds.size > 8 || hosts.size > 1024)
        }
        fun defaultPorts(mixedPort: Int) = listOf(10808, 10809, mixedPort.takeIf { it in 1..65535 } ?: 2080).distinct()
        fun ports(text: String): List<Int>? {
            val parts = text.split(',').map { it.trim() }
            if (parts.isEmpty() || parts.size > 8 || parts.any { it.isEmpty() || it.any { c -> c !in '0'..'9' } }) return null
            val ports = parts.map { it.toIntOrNull() ?: return null }
            return ports.takeIf { it.all { port -> port in 1..65535 } && it.distinct().size == it.size }
        }
    }
}

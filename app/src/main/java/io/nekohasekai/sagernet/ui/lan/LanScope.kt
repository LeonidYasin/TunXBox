package io.nekohasekai.sagernet.ui.lan

/** Numeric RFC1918 IPv4 only. No DNS, public hosts, full /16, network/broadcast or local devices. */
data class LanScope private constructor(val cidr: String, val localAddress: String, val hosts: List<String>) {
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
        fun create(local: String, prefix: Int, excluded: Set<String> = emptySet()): LanScope? {
            val address = ipv4(local) ?: return null
            if (!privateAddress(address) || prefix !in 1..30) return null
            val boundedPrefix = maxOf(prefix, 24)
            val mask = (0xffffffffL shl (32 - boundedPrefix)) and 0xffffffffL
            val network = address and mask
            val broadcast = network or (mask xor 0xffffffffL)
            val exclusions = excluded + local
            val hosts = ((network + 1) until broadcast).map(::render).filter { it !in exclusions }
            return LanScope("${render(network)}/$boundedPrefix", local, hosts)
        }
        fun ports(text: String): List<Int>? {
            val parts = text.split(',').map { it.trim() }
            if (parts.isEmpty() || parts.size > 8 || parts.any { it.isEmpty() || it.any { c -> c !in '0'..'9' } }) return null
            val ports = parts.map { it.toIntOrNull() ?: return null }
            return ports.takeIf { it.all { port -> port in 1..65535 } && it.distinct().size == it.size }
        }
    }
}

package io.nekohasekai.sagernet.network.tv

data class DiscoveredProxy(
    val ip: String,
    val port: Int,
    val type: String,
    val name: String = "$type Proxy ($ip)"
)
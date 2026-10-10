package io.nekohasekai.sagernet.ui.lan

/** No packet probing, guessed .1 address, VPN route or automatic VPN switching. */
object GatewayProfileTargets {
    const val PORT = 10808
    fun candidates(scope: LanScope, defaults: List<String>) = defaults.distinct()
        .filter { it in scope.gateways && scope.contains(it) && LanScope.ipv4(it)?.let(LanScope::privateAddress) == true }
        .map { ProxyCandidate(it, PORT, ProbeKind.TCP_UNVERIFIED) }
}

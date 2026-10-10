package io.nekohasekai.sagernet.ui.lan

import org.junit.Test
import org.junit.Assert.*

class GatewayProfileTargetsTest {
    @Test fun onlyActualEligibleDefaultGatewayIsUsed() {
        val scope = LanScope.create("192.168.2.22",24,setOf("192.168.2.1","192.168.2.2"))!!
        val actual = GatewayProfileTargets.candidates(scope,listOf("192.168.2.2","192.168.2.2","8.8.8.8","2001:db8::1","192.168.2.9"))
        assertEquals(listOf(ProxyCandidate("192.168.2.2",10808,ProbeKind.TCP_UNVERIFIED)),actual)
        assertTrue(GatewayProfileTargets.candidates(scope,emptyList()).isEmpty())
    }
    @Test fun nonDefaultOnLinkRouterIsNotGuessedAndGatewayAtWindowEdgeRemainsEligible() {
        val scope = LanScope.create("10.0.2.16",24,setOf("10.0.2.2"))!!
        assertTrue(GatewayProfileTargets.candidates(scope,listOf("10.0.2.1")).isEmpty())
        assertEquals("10.0.2.2",GatewayProfileTargets.candidates(scope,listOf("10.0.2.2")).single().host)
    }
}

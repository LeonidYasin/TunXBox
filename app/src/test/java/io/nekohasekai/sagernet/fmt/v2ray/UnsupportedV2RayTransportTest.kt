package io.nekohasekai.sagernet.fmt.v2ray

import org.junit.Assert.*
import org.junit.Test

class UnsupportedV2RayTransportTest {
    @Test fun xhttpBuilderFailsInsteadOfReturningTcp() {
        for (transport in listOf("xhttp", "splithttp")) {
            val bean = VMessBean().apply { type = transport }
            val error = assertThrows(IllegalArgumentException::class.java) {
                buildSingBoxOutboundStreamSettings(bean)
            }
            assertTrue(error.message!!.contains("no TCP fallback"))
        }
    }

    @Test fun unknownBuilderFailsInsteadOfReturningTcp() {
        val bean = VMessBean().apply { type = "future-transport" }
        assertThrows(IllegalArgumentException::class.java) {
            buildSingBoxOutboundStreamSettings(bean)
        }
    }

    @Test fun plainTcpKeepsItsIntentionalNullTransport() {
        val bean = VMessBean().apply { type = "tcp" }
        assertNull(buildSingBoxOutboundStreamSettings(bean))
    }

    @Test fun diagnosticDoesNotEchoArbitraryUserData() {
        val secret = "https://example.invalid/sub/synthetic-secret?token=synthetic-token"
        val error = assertThrows(IllegalArgumentException::class.java) {
            UnsupportedV2RayTransport.reject(secret)
        }
        assertTrue(error.message!!.contains("unknown"))
        assertFalse(error.message!!.contains("synthetic-secret"))
        assertFalse(error.message!!.contains("synthetic-token"))
    }
}

package io.nekohasekai.sagernet.ui.tv

import org.junit.Assert.*
import org.junit.Test

class TransferImportInputTest {
    @Test fun recognizesOpaqueHttpsSubscriptionPath() {
        assertTrue(TransferImportInput.isSubscription("https://subscriptions.example.invalid/aB7_TestToken"))
    }
    @Test fun acceptsTrimmedAndUppercaseHttps() {
        assertTrue(TransferImportInput.isSubscription("\n HTTPS://subscriptions.example.invalid/sub/token \n"))
        assertEquals("HTTPS://subscriptions.example.invalid/sub/token", TransferImportInput.requireHttpSubscription(" HTTPS://subscriptions.example.invalid/sub/token "))
    }
    @Test fun recognizesEncodedPathQueryAndExplicitPort() {
        assertTrue(TransferImportInput.isSubscription("https://subscriptions.example.invalid:8443/api%2Fsub?token=test"))
    }
    @Test fun leavesRootHttpProxyLinksToUpstream() {
        assertFalse(TransferImportInput.isSubscription("http://proxy.example.invalid:8080"))
        assertFalse(TransferImportInput.isSubscription("https://user:pass@proxy.example.invalid:443/?sni=example.invalid#name"))
    }
    @Test fun recognizesUpstreamSubscriptionWrappers() {
        assertTrue(TransferImportInput.isSubscription("sn://subscription?url=https%3A%2F%2Fexample.invalid%2Fsub"))
        assertTrue(TransferImportInput.isSubscription("clash://install-config?url=https%3A%2F%2Fexample.invalid%2Fsub"))
    }
    @Test fun doesNotReclassifyProfilesOrMultilinePayloads() {
        assertFalse(TransferImportInput.isSubscription("vless://test@example.invalid:443"))
        assertFalse(TransferImportInput.isSubscription("https://example.invalid/sub\nvless://test@example.invalid:443"))
        assertFalse(TransferImportInput.isSubscription("{\"outbounds\":[]}"))
    }
    @Test fun validatesExplicitRootSubscriptionAndPreservesSecretBytes() {
        val value = "https://example.invalid/?token=a%2Fb%2Bz"
        assertEquals(value, TransferImportInput.requireHttpSubscription(value))
    }
    @Test fun rejectsNonHttpAndHostlessSubscriptionWithoutLeakingInput() {
        for (value in listOf("file:///secret", "https://", "https://example.invalid/secret bad")) {
            try { TransferImportInput.requireHttpSubscription(value); fail() }
            catch (e: TransferImportFailure) {
                assertEquals(TransferImportError.SUBSCRIPTION_INVALID, e.reason)
                assertFalse(e.message!!.contains(value))
            }
        }
    }
}

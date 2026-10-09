package io.nekohasekai.sagernet.group

import org.junit.Assert.*
import org.junit.Test

class SubscriptionFailureCategoryTest {
    @Test fun expiredTlsTakesPriorityOverHttp3Timeout() {
        assertEquals(SubscriptionFailureCategory.TLS_TIME, SubscriptionFailureCategory.fromMessage(
            "Get https://example.invalid/sub/synthetic-token: tls failed to verify certificate: x509 certificate has expired or is not yet valid; h3 timeout"))
    }
    @Test fun notYetValidDoesNotClaimExpiryOnly() = assertEquals(SubscriptionFailureCategory.TLS_TIME,
        SubscriptionFailureCategory.fromMessage("x509: certificate is not yet valid"))
    @Test fun trustFailureIsDistinctFromTimeFailure() = assertEquals(SubscriptionFailureCategory.TLS_TRUST,
        SubscriptionFailureCategory.fromMessage("x509: certificate signed by unknown authority"))
    @Test fun realityFailureIsNotGenericTimeout() = assertEquals(SubscriptionFailureCategory.REALITY,
        SubscriptionFailureCategory.fromMessage("reality verification failed; timeout"))
    @Test fun unknownTransportIsExplicit() = assertEquals(SubscriptionFailureCategory.UNSUPPORTED,
        SubscriptionFailureCategory.fromMessage("Unsupported V2Ray transport: XHTTP. No TCP fallback."))
    @Test fun dnsFailureIsDistinct() = assertEquals(SubscriptionFailureCategory.DNS,
        SubscriptionFailureCategory.fromMessage("lookup example.invalid: no such host"))
    @Test fun timeoutIsDistinct() = assertEquals(SubscriptionFailureCategory.TIMEOUT,
        SubscriptionFailureCategory.fromMessage("context deadline exceeded"))
    @Test fun httpErrorNeedsStatusMarker() {
        assertEquals(SubscriptionFailureCategory.HTTP, SubscriptionFailureCategory.fromMessage("HTTP/2 403 Forbidden"))
        assertEquals(SubscriptionFailureCategory.HTTP, SubscriptionFailureCategory.fromMessage("unexpected status: 401"))
        assertEquals(SubscriptionFailureCategory.UNKNOWN, SubscriptionFailureCategory.fromMessage("https://example.invalid/403/synthetic-token"))
    }
    @Test fun emptySubscriptionIsDistinct() = assertEquals(SubscriptionFailureCategory.EMPTY,
        SubscriptionFailureCategory.fromMessage("No proxies found in subscription"))
    @Test fun missingOrLongUnrecognisedMessageIsSafe() {
        assertEquals(SubscriptionFailureCategory.UNKNOWN, SubscriptionFailureCategory.fromMessage(null))
        assertEquals(SubscriptionFailureCategory.UNKNOWN, SubscriptionFailureCategory.fromMessage("x".repeat(20_000)))
    }
    @Test fun codesAreFixedAndCannotCarryUserSecrets() {
        val result = SubscriptionFailureCategory.fromMessage("user=synthetic-user password=synthetic-pass https://example.invalid/sub/synthetic-token")
        assertEquals("SUB_UNKNOWN", result.code)
        assertFalse(result.code.contains("synthetic"))
    }
}

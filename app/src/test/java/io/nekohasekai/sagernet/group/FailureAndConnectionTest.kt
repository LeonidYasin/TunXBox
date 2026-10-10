package io.nekohasekai.sagernet.group

import org.junit.Test
import org.junit.Assert.*

class FailureAndConnectionTest {
    @Test fun wrappedCertificateErrorOutranksOuterTimeout() {
        val e = RuntimeException("timeout", Exception("x509: certificate has expired https://example.invalid/private-secret"))
        assertEquals(SubscriptionFailureCategory.TLS_TIME, SubscriptionFailureCategory.fromThrowable(e))
    }
    @Test fun eofAndSuppressedCausesAreClassifiedWithoutRawExport() {
        val e = RuntimeException("generic native wrapper", java.io.EOFException())
        assertEquals(SubscriptionFailureCategory.CLOSED, SubscriptionFailureCategory.fromThrowable(e))
        e.addSuppressed(Exception("reality verification failed; uuid=private-secret"))
        assertEquals(SubscriptionFailureCategory.REALITY, SubscriptionFailureCategory.fromThrowable(e))
    }
    @Test fun cyclesAndOversizeRawErrorsRemainBounded() {
        val a = RuntimeException("unknown"); val b = RuntimeException("unknown")
        a.initCause(b); b.initCause(a)
        assertEquals(SubscriptionFailureCategory.UNKNOWN, SubscriptionFailureCategory.fromThrowable(a))
        assertEquals(SubscriptionFailureCategory.REFUSED, SubscriptionFailureCategory.fromMessage("dial tcp: connect: connection refused"))
        assertEquals(SubscriptionFailureCategory.NETWORK, SubscriptionFailureCategory.fromMessage("no route to host"))
    }
    @Test fun structuredConnectionCodecRejectsRawNativeMessagesAndInvalidResults() {
        val failure = ConnectionTestResult(1, 1234, -1, SubscriptionFailureCategory.CLOSED)
        assertEquals(failure, ConnectionTestResult.decode(failure.encode()))
        assertEquals(ConnectionTestResult(2, 1234, 42), ConnectionTestResult.decode("1|2|1234|42|-"))
        for (text in listOf("1|1|1234|-1|-", "1|1|1234|42|CLOSED", "1|1|0|42|-", "https://example.invalid/private-secret", "x".repeat(200)))
            assertNull(ConnectionTestResult.decode(text))
        assertFalse(failure.encode().contains("private-secret"))
    }
}

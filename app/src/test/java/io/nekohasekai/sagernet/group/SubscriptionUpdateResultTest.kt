package io.nekohasekai.sagernet.group

import org.junit.Assert.*
import org.junit.Test

class SubscriptionUpdateResultTest {
    @Test fun failureRoundTripContainsOnlyWhitelistedMetadata() {
        val raw = "https://fixture.invalid/sub/synthetic-token password=synthetic-pass x509 certificate expired"
        val record = SubscriptionUpdateResult(SubscriptionUpdateOutcome.FAILURE, SubscriptionUpdateStage.DOWNLOAD,
            1234, 50, category = SubscriptionFailureCategory.fromMessage(raw))
        val encoded = record.encode()
        assertEquals(record, SubscriptionUpdateResult.decode(encoded))
        for (secret in listOf("https", "fixture.invalid", "synthetic-token", "synthetic-pass", "password")) assertFalse(encoded.contains(secret))
    }
    @Test fun successAndCancelAreNotFailures() {
        for (outcome in listOf(SubscriptionUpdateOutcome.SUCCESS, SubscriptionUpdateOutcome.CANCELLED)) {
            val record = SubscriptionUpdateResult(outcome, SubscriptionUpdateStage.APPLY, 1234, 50, 3)
            assertEquals(record, SubscriptionUpdateResult.decode(record.encode()))
        }
    }
    @Test fun malformedOrUnknownRecordsAreRejected() {
        for (bad in listOf(null, "", "1|FAILURE|DOWNLOAD|1234|50|0|-", "1|SUCCESS|APPLY|1234|50|3|TLS_TIME",
            "1|SUCCESS|APPLY|-1|50|3|-", "1|SUCCESS|APPLY|1234|-1|3|-", "1|SUCCESS|APPLY|1234|50|-3|-",
            "1|FAILURE|DOWNLOAD|1234|50|0|SECRET", "2|SUCCESS|APPLY|1234|50|3|-", "x".repeat(1000))) assertNull(bad, SubscriptionUpdateResult.decode(bad))
    }
    @Test fun typedEnvelopePreservesStageAndNotPrivateCauseInMessage() {
        val failure = SubscriptionUpdateFailure(SubscriptionUpdateStage.DOWNLOAD, SubscriptionFailureCategory.TLS_TIME,
            IllegalArgumentException("https://fixture.invalid/synthetic-token password=synthetic-pass"))
        assertEquals(SubscriptionUpdateStage.DOWNLOAD, failure.stage)
        assertEquals(SubscriptionFailureCategory.TLS_TIME, SubscriptionFailureCategory.fromMessage(failure.message))
        assertFalse(failure.message!!.contains("synthetic"))
        val db = SubscriptionUpdateFailure(SubscriptionUpdateStage.APPLY, SubscriptionFailureCategory.LOCAL_SAVE,
            IllegalStateException("local fixture"))
        assertEquals(SubscriptionFailureCategory.LOCAL_SAVE, SubscriptionFailureCategory.fromMessage(db.message))
    }
}

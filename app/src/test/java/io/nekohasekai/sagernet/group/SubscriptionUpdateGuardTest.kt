package io.nekohasekai.sagernet.group

import kotlinx.coroutines.*
import org.junit.Assert.*
import org.junit.Test

class SubscriptionUpdateGuardTest {
    @Test fun successReleasesLock() = runBlocking { val locks = mutableSetOf<Long>(); var cleaned = false; assertTrue(SubscriptionUpdateGuard.withUpdateLock(1, locks, { cleaned = true }) { true }); assertTrue(locks.isEmpty()); assertTrue(cleaned) }
    @Test fun declinedConfirmationReleasesLock() = runBlocking { val locks = mutableSetOf<Long>(); assertFalse(SubscriptionUpdateGuard.withUpdateLock(1, locks, {}) { false }); assertTrue(locks.isEmpty()) }
    @Test fun duplicateUpdateDoesNotRunOrCleanSomeoneElsesLock() = runBlocking { val locks = mutableSetOf(1L); var ran = false; assertFalse(SubscriptionUpdateGuard.withUpdateLock(1, locks, { ran = true }) { ran = true; true }); assertFalse(ran); assertEquals(setOf(1L), locks) }
    @Test fun cancellationRunsSuspendingCleanupAndAllowsRetry() = runBlocking {
        val locks = mutableSetOf<Long>(); var cleaned = false
        val task = launch { SubscriptionUpdateGuard.withUpdateLock(1, locks, { delay(5); cleaned = true }) { awaitCancellation() } }
        yield(); assertEquals(setOf(1L), locks); task.cancelAndJoin(); assertTrue(cleaned); assertTrue(locks.isEmpty())
        assertTrue(SubscriptionUpdateGuard.withUpdateLock(1, locks, {}) { true })
    }
    @Test fun exceptionReleasesLock() = runBlocking {
        val locks = mutableSetOf<Long>(); var cleaned = false
        try { SubscriptionUpdateGuard.withUpdateLock(1, locks, { cleaned = true }) { error("test") }; fail("Expected exception") } catch (_: IllegalStateException) {}
        assertTrue(cleaned); assertTrue(locks.isEmpty())
    }
}

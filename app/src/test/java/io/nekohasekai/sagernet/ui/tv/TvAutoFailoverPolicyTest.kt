package io.nekohasekai.sagernet.ui.tv

import org.junit.Assert.*
import org.junit.Test

class TvAutoFailoverPolicyTest {
    private val now: Long = 1_700_000_000_000L

    @Test fun disabledModeNeverSwitches() {
        val state = TvFailoverState(enabled = false, fallbackOrder = listOf(2L, 3L))
        assertEquals(TvFailoverAction.NONE, TvAutoFailoverPolicy.decide(currentHealthy = false, state = state, candidateId = 2L, nowMs = now).action)
    }

    @Test fun healthyCurrentConnectionNeverSwitches() {
        val state = TvFailoverState(enabled = true, fallbackOrder = listOf(2L, 3L))
        assertEquals(TvFailoverAction.NONE, TvAutoFailoverPolicy.decide(currentHealthy = true, state = state, candidateId = 2L, nowMs = now).action)
    }

    @Test fun noFallbackConfiguredNeverSwitches() {
        val state = TvFailoverState(enabled = true, fallbackOrder = emptyList())
        assertEquals(TvFailoverAction.NONE, TvAutoFailoverPolicy.decide(currentHealthy = false, state = state, candidateId = 0L, nowMs = now).action)
    }

    @Test fun candidateOutsideUserEnabledFallbackListIsIgnored() {
        val state = TvFailoverState(enabled = true, fallbackOrder = listOf(2L, 3L))
        assertEquals(TvFailoverAction.NONE, TvAutoFailoverPolicy.decide(currentHealthy = false, state = state, candidateId = 99L, nowMs = now).action)
    }

    @Test fun failedCurrentConnectionSwitchesToSelectedFallback() {
        val state = TvFailoverState(enabled = true, fallbackOrder = listOf(2L, 3L))
        val decision = TvAutoFailoverPolicy.decide(currentHealthy = false, state = state, candidateId = 3L, nowMs = now)
        assertEquals(TvFailoverAction.SWITCH_TO, decision.action)
        assertEquals(3L, decision.candidateId)
        assertEquals("connection-lost", decision.reason)
    }

    @Test fun switchAppliesCooldownAndCountsSessionSwitch() {
        val state = TvFailoverState(enabled = true, fallbackOrder = listOf(2L, 3L), maxSwitchesPerSession = 2)
        val next = TvAutoFailoverPolicy.applySwitch(state = state, candidateId = 3L, nowMs = now, cooldownMs = TvAutoFailoverPolicy.DEFAULT_COOLDOWN_MS)
        assertEquals(1, next.switchesThisSession)
        assertEquals(now + TvAutoFailoverPolicy.DEFAULT_COOLDOWN_MS, next.cooldownUntilMs)
        assertEquals(now, next.lastSwitchAtMs)
        assertEquals(listOf(3L), next.triedIds)
    }

    @Test fun switchBeyondSessionCapIsRefused() {
        var state = TvFailoverState(enabled = true, fallbackOrder = listOf(2L, 3L), maxSwitchesPerSession = 1, switchesThisSession = 1)
        val decision = TvAutoFailoverPolicy.decide(currentHealthy = false, state = state, candidateId = 3L, nowMs = now)
        assertEquals(TvFailoverAction.NONE, decision.action)
        state = TvAutoFailoverPolicy.applySwitch(state = state, candidateId = 3L, nowMs = now, cooldownMs = TvAutoFailoverPolicy.DEFAULT_COOLDOWN_MS)
        assertEquals(2, state.switchesThisSession)
    }

    @Test fun cooldownBlocksSwitchUntilElapsed() {
        var state = TvFailoverState(enabled = true, fallbackOrder = listOf(2L, 3L), cooldownUntilMs = now + 1_000L, switchesThisSession = 1)
        assertEquals(TvFailoverAction.NONE, TvAutoFailoverPolicy.decide(currentHealthy = false, state = state, candidateId = 2L, nowMs = now).action)
        assertEquals(TvFailoverAction.SWITCH_TO, TvAutoFailoverPolicy.decide(currentHealthy = false, state = state, candidateId = 2L, nowMs = now + 2_000L).action)
    }

    @Test fun fallbackOrderEncodingKeepsUserOrderAndDropsInvalidIds() {
        assertEquals("3,2", TvAutoFailoverPolicy.encodeFallbackOrder(listOf(3L, 0L, 2L, 3L, -1L)))
        assertEquals("", TvAutoFailoverPolicy.encodeFallbackOrder(emptyList()))
    }

    @Test fun fallbackOrderDecodingToleratesGarbageAndRoundTrips() {
        assertEquals(emptyList<Long>(), TvAutoFailoverPolicy.decodeFallbackOrder(null))
        assertEquals(emptyList<Long>(), TvAutoFailoverPolicy.decodeFallbackOrder(""))
        assertEquals(listOf(2L, 3L), TvAutoFailoverPolicy.decodeFallbackOrder(" 2 , x ,3,2,"))
        val ids = listOf(5L, 1L, 9L)
        assertEquals(ids, TvAutoFailoverPolicy.decodeFallbackOrder(TvAutoFailoverPolicy.encodeFallbackOrder(ids)))
    }

    @Test fun rotationWalksTheWholeUserListAfterTheFailedProfile() {
        val order = listOf(2L, 3L, 4L)
        assertEquals(3L, TvAutoFailoverPolicy.selectNextFallback(order, currentId = 2L, triedIds = setOf(2L)))
        assertEquals(4L, TvAutoFailoverPolicy.selectNextFallback(order, currentId = 3L, triedIds = setOf(2L, 3L)))
        assertEquals(null, TvAutoFailoverPolicy.selectNextFallback(order, currentId = 4L, triedIds = setOf(2L, 3L, 4L)))
    }

    @Test fun neverReturnsAProfileThatAlreadyFailedInThisChain() {
        // The reported loop: Sweden fails, Switzerland works, Switzerland fails, Sweden is chosen
        // again because it still has the lowest saved ping. The failed profile must stay excluded.
        val order = listOf(2L, 3L, 4L)
        val tried = setOf(3L, 2L)
        val next = TvAutoFailoverPolicy.selectNextFallback(order, currentId = 3L, triedIds = tried)
        assertEquals(4L, next)
        assertNotEquals(2L, next)
    }

    @Test fun rotationWrapsAndStartsAtTheTopWhenCurrentIsNotInTheList() {
        val order = listOf(2L, 3L, 4L)
        assertEquals(2L, TvAutoFailoverPolicy.selectNextFallback(order, currentId = 4L, triedIds = setOf(4L)))
        assertEquals(2L, TvAutoFailoverPolicy.selectNextFallback(order, currentId = 99L, triedIds = setOf(99L)))
        assertEquals(3L, TvAutoFailoverPolicy.selectNextFallback(order, currentId = 99L, triedIds = setOf(99L, 2L)))
    }

    @Test fun emptyListHasNoFallback() {
        assertEquals(null, TvAutoFailoverPolicy.selectNextFallback(emptyList(), currentId = 2L, triedIds = setOf(2L)))
    }

    @Test fun immediateSuccessAfterSwitchDoesNotRestartTheChain() {
        // A probe that passes one second after a switch used to clear triedIds and bring a
        // just-failed profile back before the circle was finished.
        assertFalse(TvAutoFailoverPolicy.shouldResetChainOnSuccess(now, lastSwitchAtMs = now - 1_000L))
        assertFalse(TvAutoFailoverPolicy.shouldResetChainOnSuccess(now, lastSwitchAtMs = now - 1L))
    }

    @Test fun successThatHeldAfterTheSwitchRestartsTheChain() {
        assertTrue(TvAutoFailoverPolicy.shouldResetChainOnSuccess(now, lastSwitchAtMs = now - TvAutoFailoverPolicy.STABLE_CONNECTION_MS))
        assertTrue(TvAutoFailoverPolicy.shouldResetChainOnSuccess(now, lastSwitchAtMs = now - TvAutoFailoverPolicy.STABLE_CONNECTION_MS - 1L))
        // No switch happened yet in this chain, so a success is an ordinary reset.
        assertTrue(TvAutoFailoverPolicy.shouldResetChainOnSuccess(now, lastSwitchAtMs = 0L))
    }

    @Test fun pendingCandidateIsRejectedAfterTheModeIsDisabledOrTheListIsEdited() {
        val order = listOf(2L, 3L, 4L)
        assertTrue(TvAutoFailoverPolicy.isCandidateStillAllowed(enabled = true, order = order, candidateId = 3L))
        assertFalse(TvAutoFailoverPolicy.isCandidateStillAllowed(enabled = false, order = order, candidateId = 3L))
        assertFalse(TvAutoFailoverPolicy.isCandidateStillAllowed(enabled = true, order = listOf(2L, 4L), candidateId = 3L))
        assertFalse(TvAutoFailoverPolicy.isCandidateStillAllowed(enabled = true, order = emptyList(), candidateId = 3L))
    }

    @Test fun cancelledChainKeepsTheWholeCircleBeforeRevisitingAFailedProfile() {
        // Regression for rc.167: the chain used to be reset by flapping network identity and by a
        // spurious success, so the walk returned to the profile that had just failed. With the
        // chain kept intact, a three-profile circle has to finish before anything is revisited.
        val order = listOf(2L, 3L, 4L)
        val tried = linkedSetOf(2L, 3L, 4L)
        assertEquals(null, TvAutoFailoverPolicy.selectNextFallback(order, currentId = 4L, triedIds = tried))
    }
}

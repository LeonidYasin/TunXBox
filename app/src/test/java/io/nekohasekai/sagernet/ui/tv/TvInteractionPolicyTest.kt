package io.nekohasekai.sagernet.ui.tv

import org.junit.Assert.*
import org.junit.Test

class TvInteractionPolicyTest {
    @Test fun servicePhaseMappingIsCaseInsensitive() { assertEquals(TvVpnPhase.CONNECTED, TvVpnPhase.fromServiceName("Connected")); assertEquals(TvVpnPhase.IDLE, TvVpnPhase.fromServiceName("unknown")) }
    @Test fun unboundServiceCannotBeControlled() { TvVpnPhase.values().forEach { assertEquals(TvVpnCommand.NONE, TvInteractionPolicy.primary(it, 1, 1, false)) } }
    @Test fun startRequiresSelection() { assertEquals(TvVpnCommand.NONE, TvInteractionPolicy.primary(TvVpnPhase.STOPPED, 0, 0, true)); assertEquals(TvVpnCommand.START, TvInteractionPolicy.primary(TvVpnPhase.STOPPED, 1, 0, true)) }
    @Test fun selectedAndActiveAreIndependent() { assertEquals(TvVpnCommand.RELOAD, TvInteractionPolicy.primary(TvVpnPhase.CONNECTED, 2, 1, true)); assertEquals(TvVpnCommand.STOP, TvInteractionPolicy.primary(TvVpnPhase.CONNECTED, 1, 1, true)) }
    @Test fun mediaAlwaysStopsExistingConnectionEvenWhenSelectionChanged() { assertEquals(TvVpnCommand.STOP, TvInteractionPolicy.media(TvVpnPhase.CONNECTED, 2)); assertEquals(TvVpnCommand.STOP, TvInteractionPolicy.media(TvVpnPhase.CONNECTING, 2)) }
    @Test fun stoppingCannotBeRestartedOrReloaded() { assertEquals(TvVpnCommand.NONE, TvInteractionPolicy.primary(TvVpnPhase.STOPPING, 2, 1, true)); assertEquals(TvVpnCommand.NONE, TvInteractionPolicy.media(TvVpnPhase.STOPPING, 2)) }
    @Test fun connectionCanBeCancelledButNotReplacedMidTransition() { assertEquals(TvVpnCommand.STOP, TvInteractionPolicy.primary(TvVpnPhase.CONNECTING, 2, 1, true)); assertFalse(TvInteractionPolicy.canSelect(TvVpnPhase.CONNECTING)); assertFalse(TvInteractionPolicy.canSelect(TvVpnPhase.STOPPING)) }
    @Test fun activeProfileCannotBeEditedOrDeletedDuringConnection() { listOf(TvVpnPhase.CONNECTING, TvVpnPhase.CONNECTED, TvVpnPhase.STOPPING).forEach { assertFalse(TvInteractionPolicy.canMutate(1, 1, it)); assertTrue(TvInteractionPolicy.canMutate(2, 1, it)) }; assertTrue(TvInteractionPolicy.canMutate(1, 1, TvVpnPhase.STOPPED)) }
    @Test fun explicitConnectNeverDisconnectsOrRestartsAnAlreadyActiveProfile() { assertEquals(TvVpnCommand.NONE, TvInteractionPolicy.connectOnly(TvVpnPhase.CONNECTED, 1, 1)); assertEquals(TvVpnCommand.RELOAD, TvInteractionPolicy.connectOnly(TvVpnPhase.CONNECTED, 2, 1)); assertEquals(TvVpnCommand.START, TvInteractionPolicy.connectOnly(TvVpnPhase.STOPPED, 2, 1)) }
    @Test fun focusRestorationUsesIdentityNotOldPosition() { assertEquals(2, TvInteractionPolicy.focusIndex(listOf(9, 8, 7), 7)); assertEquals(0, TvInteractionPolicy.focusIndex(listOf(9, 8), 7)); assertEquals(0, TvInteractionPolicy.focusIndex(emptyList(), 7)) }
    @Test fun immutableCardStateTracksSelectionAndConnectionSeparately() { val a = TvProfileCard(1, "A", "VLESS", "example.com", true, null); val b = a.copy(selected = false, activePhase = TvVpnPhase.CONNECTED); assertEquals(a.id, b.id); assertNotEquals(a, b); assertFalse(b.selected); assertEquals(TvVpnPhase.CONNECTED, b.activePhase) }
}

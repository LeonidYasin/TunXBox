package io.nekohasekai.sagernet.ui.tv
import org.junit.Assert.*
import org.junit.Test
class TvHealthSessionTest {
 @Test fun selectedGroupFloatsWithoutReorderingOthers() {
  assertEquals(listOf(3L,1L,2L,4L),TvGroupRows.orderedGroupIds(listOf(1L,2L,3L,4L),3L))
  assertEquals(listOf(1L,2L),TvGroupRows.orderedGroupIds(listOf(1L,2L),99L))
 }
 @Test fun oncePerConnectionAndNoDuplicateWhileBusy() {val s=TvHealthSession();s.sync(true,1,"netA:1");val t=s.begin()!!;assertNull(s.begin());assertTrue(s.finish(t));assertNull(s.begin())}
 @Test fun disconnectReconnectSameProfileRejectsOldResult() {val s=TvHealthSession();s.sync(true,1,"netA:1");val old=s.begin()!!;s.sync(false,1,"netA:1");s.sync(true,1,"netA:2");val new=s.begin()!!;assertFalse(s.finish(old));assertTrue(s.finish(new))}
 @Test fun profileAndNetworkSwitchInvalidatePendingChecks() {val s=TvHealthSession();s.sync(true,1,"A");val a=s.begin()!!;s.sync(true,2,"A");assertFalse(s.finish(a));val b=s.begin()!!;s.sync(true,2,"B");assertFalse(s.finish(b));assertNotNull(s.begin())}
 @Test fun pauseAllowsOneResumeAndLateCancellationCannotEraseNewJob() {val s=TvHealthSession();s.sync(true,1,"A");val old=s.begin()!!;s.cancel(old);val next=s.begin()!!;s.cancel(old);assertTrue(s.finish(next));assertNull(s.begin())}
 @Test fun manualRepeatAllowedButNoAutomaticLoopAfterFailure() {val s=TvHealthSession();s.sync(true,1,"A");s.finish(s.begin()!!);assertNull(s.begin());assertNotNull(s.begin(true))}
 @Test fun noCheckBeforeCoreOrValidProfile() {val s=TvHealthSession();s.sync(false,1,"A");assertNull(s.begin());s.sync(true,0,"A");assertNull(s.begin())}
}

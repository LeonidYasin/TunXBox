package io.nekohasekai.sagernet.ui.tv
import org.junit.Assert.*
import org.junit.Test
import io.nekohasekai.sagernet.group.SubscriptionFailureCategory as C
class TvGroupQualityTest {
 private fun r(id: Long,s: List<Int>,tcp: Int?=1)=TvQualityResult(id,s,tcp,C.UNKNOWN,1)
 @Test fun urlReliabilityOutranksFastTcpAndLatency(){
  assertEquals(listOf(3L,2L,1L,4L),TvGroupQuality.ranked(listOf(r(1,listOf(1,-1,-1)),r(2,listOf(900,800,-1)),r(3,listOf(1000,900,800)),r(4,listOf(-1,-1,-1),0))).map {it.id})
 }
 @Test fun medianThenSpreadAndStableTie(){
  assertEquals(listOf(2L,3L,1L),TvGroupQuality.ranked(listOf(r(1,listOf(0,10,100)),r(2,listOf(9,10,11)),r(3,listOf(9,10,11)))).map {it.id})
 }
 @Test fun failedAndMissingSamplesAreNotZeroLatency(){
  val failed=r(1,listOf(-1,-1,-1));assertEquals(0,failed.successes);assertEquals(Int.MAX_VALUE,failed.medianMs);assertNull(failed.spreadMs)
 }
}

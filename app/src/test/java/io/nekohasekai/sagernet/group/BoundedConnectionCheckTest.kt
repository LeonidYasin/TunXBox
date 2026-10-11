package io.nekohasekai.sagernet.group
import kotlinx.coroutines.*
import org.junit.Assert.*
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
class BoundedConnectionCheckTest {
 @Test fun blockedBinderTimesOutWithoutWaitingForWorkerOrCreatingAnother()=runBlocking {
  val entered=CountDownLatch(1);val release=CountDownLatch(1);val finished=CountDownLatch(1)
  BoundedConnectionCheck().use {runner ->
   try {
    val start=System.nanoTime()
    val result=runner.check(1,100) {entered.countDown();release.await(5,TimeUnit.SECONDS);finished.countDown();ConnectionTestResult(1,System.currentTimeMillis(),2)}
    assertTrue(entered.await(1,TimeUnit.SECONDS));assertEquals(SubscriptionFailureCategory.TIMEOUT,result.category)
    assertTrue("Await deadline must not wait for blocked worker",TimeUnit.NANOSECONDS.toMillis(System.nanoTime()-start)<2000)
    assertEquals(SubscriptionFailureCategory.BUSY,runner.check(1,100) {error("must not allocate another worker")}.category)
   } finally {release.countDown();assertTrue(finished.await(2,TimeUnit.SECONDS))}
  }
 }
 @Test fun cancellingAwaitDoesNotCancelVpnOrWaitForBinder()=runBlocking {
  val entered=CountDownLatch(1);val release=CountDownLatch(1)
  BoundedConnectionCheck().use {runner ->
   val job=launch {runner.check(1,5000) {entered.countDown();release.await(5,TimeUnit.SECONDS);ConnectionTestResult(1,System.currentTimeMillis(),3)}}
   try {yield();assertTrue(entered.await(1,TimeUnit.SECONDS));withTimeout(1000){job.cancelAndJoin()};assertTrue(job.isCancelled)
    assertEquals(SubscriptionFailureCategory.BUSY,runner.check(1,100) {error("must not run")}.category)
   } finally {release.countDown()}
  }
 }
 @Test fun successfulAndFailedResultsRemainTyped()=runBlocking {
  BoundedConnectionCheck().use {runner ->
   val expected=ConnectionTestResult(7,System.currentTimeMillis(),99)
   assertEquals(expected,runner.check(7,1000) {expected})
  }
  BoundedConnectionCheck().use {runner ->assertEquals(SubscriptionFailureCategory.TIMEOUT,runner.check(7,1000) {throw java.net.SocketTimeoutException()}.category)}
 }
}

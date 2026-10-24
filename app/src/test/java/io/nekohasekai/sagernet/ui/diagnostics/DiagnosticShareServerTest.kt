package io.nekohasekai.sagernet.ui.diagnostics
import java.net.Inet4Address
import java.net.InetAddress
import java.net.Socket
import java.net.URI
import org.junit.Assert.*
import org.junit.Test
class DiagnosticShareServerTest {
 private val local get()=InetAddress.getByName("127.0.0.1") as Inet4Address
 private fun request(url: String,path: String?=null,host: String?=null,method: String="GET",origin: String?=null): String {
  val u=URI(url)
  return Socket(u.host,u.port).use {s ->
   s.soTimeout=3000
   s.getOutputStream().write(("$method ${path?:u.path} HTTP/1.1\r\nHost: ${host?:"${u.host}:${u.port}"}\r\n"+(origin?.let {"Origin: $it\r\n"}?:"")+"\r\n").toByteArray())
   s.getInputStream().bufferedReader().readText()
  }
 }
 @Test fun exactTokenDownloadsOnlyFixedReport() {DiagnosticShareServer(local,"safe diagnostic").use {s -> val response=request(s.url);assertTrue(response.startsWith("HTTP/1.1 200"));assertTrue(response.endsWith("safe diagnostic"));assertTrue(response.contains("no-store"));assertTrue(response.contains("TunXBox-diagnostics.txt"))}}
 @Test fun wrongTokenPostHostAndOriginCannotReadReport() {DiagnosticShareServer(local,"private report").use {s ->
  val blocked=listOf(
   request(s.url,"/wrong/diagnostics.txt"),request(s.url,method="POST"),
   request(s.url,host="evil.example"),request(s.url,origin="https://evil.example"))
  for(r in blocked) {
   assertTrue(r.startsWith("HTTP/1.1 404"));assertFalse(r.contains("private report"))
  }
 }}
 @Test fun onlyAuthorizedCompletedTransfersAreCountedAndExpiryIsVisible() {
  DiagnosticShareServer(local,"safe diagnostic",lifetimeMs=5000).use {s ->
   assertEquals(0,s.completedTransfers);assertTrue(s.remainingSeconds in 1..5)
   request(s.url,"/wrong/diagnostics.txt");assertEquals(0,s.completedTransfers)
   request(s.url);assertEquals(1,s.completedTransfers)
   s.close();assertEquals(0,s.remainingSeconds)
  }
 }
 @Test fun closeAndExpiryDisableListener() {
  val s=DiagnosticShareServer(local,"report",lifetimeMs=30);Thread.sleep(150);assertFalse(s.isOpen);s.close()
  val t=DiagnosticShareServer(local,"report");t.close();assertFalse(t.isOpen);assertTrue(runCatching {request(t.url)}.isFailure)
 }
 @Test fun untrustedFilenameAndOversizeAreRejected() {
  assertTrue(runCatching {DiagnosticShareServer(local,"report",filename="../secret.txt")}.isFailure)
  assertTrue(runCatching {DiagnosticShareServer(local,"x".repeat(512*1024+1))}.isFailure)
  assertTrue(runCatching {DiagnosticShareServer(local,"report",lifetimeMs=300001)}.isFailure)
 }
}

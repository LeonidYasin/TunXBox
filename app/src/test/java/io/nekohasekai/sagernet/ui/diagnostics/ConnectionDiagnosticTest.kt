package io.nekohasekai.sagernet.ui.diagnostics
import android.app.Application
import io.nekohasekai.sagernet.database.ProxyEntity
import io.nekohasekai.sagernet.fmt.v2ray.VMessBean
import io.nekohasekai.sagernet.group.SubscriptionFailureCategory as C
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
@RunWith(RobolectricTestRunner::class)
@Config(sdk=[28],application=Application::class)
class ConnectionDiagnosticTest {
 @Test fun unsupportedXhttpHasStructuredStageWithoutPrivateParameters() {
  val entity=ProxyEntity(id=7,type=ProxyEntity.TYPE_VMESS,vmessBean=VMessBean().apply {
   initializeDefaultValues();isVLESS=true;type="xhttp";security="reality";serverAddress="private.example";uuid="private-secret";name="private-name";path="/private-token"
  })
  val r=ConnectionDiagnostic.capture(entity,7,DiagnosticStage.STARTUP,C.UNSUPPORTED,1234)
  assertEquals("VLESS",r.protocol);assertEquals("xhttp",r.transport);assertEquals("reality",r.security)
  assertEquals(r,ConnectionDiagnostic.decode(r.encode()))
  for(secret in listOf("private.example","private-secret","private-name","private-token"))assertFalse(r.encode().contains(secret))
 }
 @Test fun untrustedTransportAndSecurityAreNotRetained() {
  val entity=ProxyEntity(id=1,type=ProxyEntity.TYPE_VMESS,vmessBean=VMessBean().apply {initializeDefaultValues();type="https://secret/path";security="password"})
  val r=ConnectionDiagnostic.capture(entity,1,DiagnosticStage.STARTUP,C.UNKNOWN,1234)
  assertEquals("unknown",r.transport);assertEquals("unknown",r.security)
 }
 @Test fun malformedRecordsAreRejected() {
  assertNull(ConnectionDiagnostic.decode("x".repeat(2000)))
  assertNull(ConnectionDiagnostic.decode(ConnectionDiagnostic(1,1234,DiagnosticStage.STARTUP,null).encode()))
  assertNull(ConnectionDiagnostic.decode(ConnectionDiagnostic(1,1234,DiagnosticStage.STARTUP,C.UNSUPPORTED,transport="secret").encode()))
 }
 @Test fun successfulUrlRecordRetainsActualRttAndNoSyntheticBytes() {
  val r=ConnectionDiagnostic(1,1234,DiagnosticStage.URL_TEST,null,elapsed=113)
  assertEquals(r,ConnectionDiagnostic.decode(r.encode()));assertFalse(r.encode().contains("bytes"))
 }
}

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
 @Test fun timeoutReportIsAboutConnectionNotSubscription() {
  val context=org.robolectric.RuntimeEnvironment.getApplication()
  val result=ConnectionDiagnosticReport.text(context,ConnectionDiagnostic(1,1234,DiagnosticStage.URL_TEST,C.TIMEOUT))
  assertTrue(result.contains(context.getString(io.nekohasekai.sagernet.R.string.diagnostic_timeout_explanation)))
  assertFalse(result.contains(io.nekohasekai.sagernet.group.SubscriptionFailurePresentation.reason(context,C.TIMEOUT)))
 }
 @Test fun extendedReportContainsOnlyBoundedEvidenceNotPrivateInputs() {
  val r=ExtendedConnectionDiagnostics.Report(1,1,"WIFI",1,1,true,true,false,false,false,2,123456,
    listOf(ExtendedConnectionDiagnostics.Step("ANDROID_STRICT_TLS","FAIL",120,C.TLS_TRUST,TlsEvidence.CHAIN)))
  val text=r.text();assertTrue(text.contains("TLS_CHAIN"));assertTrue(text.contains("NET_TLS_TRUST"))
  assertTrue(text.contains("not the existing VPN tunnel"));assertTrue(text.contains("hostname verification"))
  for(secret in listOf("private.example","private-password","private-key","private-profile"))assertFalse(text.contains(secret))
 }

 @Test fun unsupportedXhttpHasStructuredStageWithoutPrivateParameters() {
  val entity=ProxyEntity(id=7,type=ProxyEntity.TYPE_VMESS,vmessBean=VMessBean().apply {
   alterId=-1;initializeDefaultValues();type="xhttp";security="reality";serverAddress="private.example";uuid="private-secret";name="private-name";path="/private-token"
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

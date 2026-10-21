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
 @Test fun negativeNativeRttIsNotAFalsePassAndUnknownExportFieldsAreRedacted() {
  assertTrue(runCatching {ExtendedConnectionDiagnostics.requireUrlSuccess(-1)}.isFailure)
  ExtendedConnectionDiagnostics.requireUrlSuccess(0)
  val report=ExtendedConnectionDiagnostics.Report(1,1,"private-network",0,0,false,false,false,false,false,false,0,null,
   listOf(ExtendedConnectionDiagnostics.Step("private-stage","private-outcome",1)))
  assertFalse(report.text().contains("private-"))
 }

 @Test fun totalDeadlineRetainsCompletedStagesButDoesNotSwallowUserCancellation() = kotlinx.coroutines.runBlocking {
  val steps=mutableListOf(ExtendedConnectionDiagnostics.Step("CONFIG_COMPATIBILITY","PASS",1))
  DiagnosticDeadline.collect(steps,20) {kotlinx.coroutines.delay(1000)}
  assertEquals("CONFIG_COMPATIBILITY",steps.first().stage);assertEquals("TOTAL_LIMIT",steps.last().stage)
  assertEquals("TIMEOUT",steps.last().outcome)
  val cancelled=mutableListOf<ExtendedConnectionDiagnostics.Step>()
  var propagated=false
  try {DiagnosticDeadline.collect(cancelled,1000) {throw kotlinx.coroutines.CancellationException("fixture")}}
  catch(_: kotlinx.coroutines.CancellationException){propagated=true}
  assertTrue(propagated);assertTrue(cancelled.isEmpty())
 }

 @Test fun hysteria2MetadataRetainsQuicTlsWithoutSecrets() {
  val entity=ProxyEntity(id=9,type=ProxyEntity.TYPE_HYSTERIA,hysteriaBean=io.nekohasekai.sagernet.fmt.hysteria.HysteriaBean().apply {
   initializeDefaultValues();protocolVersion=2;serverAddress="private.example";sni="private-sni";authPayload="private-password"
  })
  val record=ConnectionDiagnostic.capture(entity,9,DiagnosticStage.URL_TEST,C.TIMEOUT,1234)
  assertEquals("Hysteria2",record.protocol);assertEquals("quic",record.transport);assertEquals("tls",record.security)
  for(secret in listOf("private.example","private-sni","private-password"))assertFalse(record.encode().contains(secret))
 }
 @Test fun compactOfflineQrIsBoundedPrioritizesFailuresAndRejectsUntrustedFields() {
  val record=ConnectionDiagnostic(1,1234,DiagnosticStage.URL_TEST,C.TLS_TRUST,"VLESS","tcp","tls")
  val steps=(1..12).map {ExtendedConnectionDiagnostics.Step("PROFILE_URL_1","PASS",10)}+
   ExtendedConnectionDiagnostics.Step("ANDROID_STRICT_TLS","FAIL",20,C.TLS_TRUST,TlsEvidence.CHAIN)+
   ExtendedConnectionDiagnostics.Step("private-secret-stage","private-secret-outcome",20)
  val report=ExtendedConnectionDiagnostics.Report(1,1234,"WIFI",1,1,true,false,false,false,false,false,0,null,steps)
  val text=ConnectionDiagnosticReport.offlineText(record,report)
  assertTrue(text.toByteArray().size<=1400);assertTrue(text.contains("TLS_CHAIN"));assertTrue(text.contains("not full report"))
  assertFalse(text.contains("private-secret"));assertFalse(text.contains("http://"))
  val malformed=record.copy(protocol="private-secret-protocol",security="private-secret-key")
  assertFalse(ConnectionDiagnosticReport.offlineText(malformed).contains("private-secret"))
 }
 @Test fun explanationsHaveNextActionAndDoNotPromiseContinuousMonitoring() {
  val context=org.robolectric.RuntimeEnvironment.getApplication()
  val r=ConnectionDiagnostic(1,1234,DiagnosticStage.URL_TEST,C.TLS_TRUST)
  val text=DiagnosticPresentation.summary(context,r)
  assertTrue(text.contains(context.getString(io.nekohasekai.sagernet.R.string.diagnostic_next_tls)))
  assertTrue(text.contains(context.getString(io.nekohasekai.sagernet.R.string.diagnostic_snapshot_scope)))
  assertEquals(context.getString(io.nekohasekai.sagernet.R.string.diagnostic_stage_other),DiagnosticPresentation.stage(context,"private-secret-stage"))
 }
 @Test fun reportLocalizationKeepsStableTechnicalCodesAndHonestSkippedState() {
  val context=org.robolectric.RuntimeEnvironment.getApplication()
  val r=ExtendedConnectionDiagnostics.Report(1,1,"WIFI",1,0,false,false,false,false,false,false,0,null,
   listOf(ExtendedConnectionDiagnostics.Step("ANDROID_STRICT_TLS","SKIPPED_NOT_COMPARABLE_OR_NOT_APPLICABLE",0),
    ExtendedConnectionDiagnostics.Step("TOTAL_LIMIT","TIMEOUT",60000,C.TIMEOUT)))
  val text=r.text(context)
  assertTrue(text.contains("TOTAL_LIMIT: TIMEOUT"));assertTrue(text.contains("NET_TIMEOUT"))
  assertTrue(text.contains(context.getString(io.nekohasekai.sagernet.R.string.diagnostic_outcome_skipped)))
 }

 @Test fun internallyNormalizedRealityIsNotReportedAsOrdinaryTls() {
  val entity=ProxyEntity(id=9,type=ProxyEntity.TYPE_VMESS,vmessBean=VMessBean().apply {
   alterId=-1;initializeDefaultValues();type="tcp";security="tls";realityPubKey="private-fixture-key";realityShortId="private-id"
  })
  val r=ConnectionDiagnostic.capture(entity,9,DiagnosticStage.URL_TEST,C.REALITY,1234)
  assertEquals("reality",r.security)
  assertFalse(r.encode().contains("private-fixture-key"));assertFalse(r.encode().contains("private-id"))
 }

 @Test fun timeoutExplanationIsAboutConnectionNotSubscription() {
  val context=org.robolectric.RuntimeEnvironment.getApplication()
  val result=ConnectionDiagnosticReport.reason(context,C.TIMEOUT)
  assertTrue(result.contains(context.getString(io.nekohasekai.sagernet.R.string.diagnostic_timeout_explanation)))
  assertFalse(result.contains(io.nekohasekai.sagernet.group.SubscriptionFailurePresentation.reason(context,C.TIMEOUT)))
 }
 @Test fun extendedReportContainsOnlyBoundedEvidenceNotPrivateInputs() {
  val r=ExtendedConnectionDiagnostics.Report(1,1,"WIFI",1,1,true,true,false,false,false,false,2,123456,
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

package io.nekohasekai.sagernet.ui.diagnostics
import org.junit.Assert.*
import org.junit.Test
class TlsFailureEvidenceTest {
 @Test fun combinedGoValidityMessageDoesNotInventExpiry(){
  assertEquals(TlsEvidence.UNKNOWN,TlsFailureEvidence.classify(Exception("x509: certificate has expired or is not yet valid")))
  assertEquals(TlsEvidence.NOT_YET_VALID,TlsFailureEvidence.classify(Exception("x509: certificate has expired or is not yet valid: current time is before date")))
 }
 @Test fun expiredNestedCauseIsMoreSpecificThanHandshake(){assertEquals(TlsEvidence.EXPIRED,TlsFailureEvidence.classify(javax.net.ssl.SSLHandshakeException("secret.example").apply {initCause(java.security.cert.CertificateExpiredException("expired"))}))}
 @Test fun hostnameNeverExportsHost(){assertEquals(TlsEvidence.HOSTNAME,TlsFailureEvidence.classify(Exception("hostname secret.example failed")));assertFalse(TlsEvidence.HOSTNAME.name.contains("secret"))}
 @Test fun chainAndFutureCertificate(){assertEquals(TlsEvidence.CHAIN,TlsFailureEvidence.classify(java.security.cert.CertPathValidatorException("secret chain")));assertEquals(TlsEvidence.NOT_YET_VALID,TlsFailureEvidence.classify(java.security.cert.CertificateNotYetValidException("secret")))}
 @Test fun boundedCyclesAndUnknown(){val e=Exception("private-password");val other=Exception("private-url",e);e.initCause(other);assertEquals(TlsEvidence.UNKNOWN,TlsFailureEvidence.classify(e))}
}

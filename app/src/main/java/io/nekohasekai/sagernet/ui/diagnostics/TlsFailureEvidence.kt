package io.nekohasekai.sagernet.ui.diagnostics

/** Only this closed vocabulary is exported. Exception strings are inspected in memory. */
enum class TlsEvidence { EXPIRED, NOT_YET_VALID, HOSTNAME, CHAIN, HANDSHAKE, UNKNOWN }
object TlsFailureEvidence {
 fun classify(error: Throwable): TlsEvidence {
  val seen=java.util.Collections.newSetFromMap(java.util.IdentityHashMap<Throwable,Boolean>())
  val queue=java.util.ArrayDeque<Throwable>().apply {add(error)}
  val found=mutableSetOf<TlsEvidence>()
  while(queue.isNotEmpty()&&seen.size<16) {
   val e=queue.removeFirst();if(!seen.add(e))continue
   val m=e.message.orEmpty().take(16384).lowercase(java.util.Locale.ROOT)
   when {
    "expired or" in m&&"not yet valid" in m&&"is before" in m->found.add(TlsEvidence.NOT_YET_VALID)
    "expired or" in m&&"not yet valid" in m&&"is after" !in m->Unit
    e is java.security.cert.CertificateExpiredException||"certificate has expired" in m||"certificate expired" in m->found.add(TlsEvidence.EXPIRED)
    e is java.security.cert.CertificateNotYetValidException||"not yet valid" in m->found.add(TlsEvidence.NOT_YET_VALID)
    "hostname" in m||"no subject alternative" in m||"certificate is valid for" in m->found.add(TlsEvidence.HOSTNAME)
    e is java.security.cert.CertPathValidatorException||"unknown authority" in m||"trust anchor" in m||"unable to find valid certification path" in m->found.add(TlsEvidence.CHAIN)
    e is javax.net.ssl.SSLHandshakeException||"handshake failure" in m->found.add(TlsEvidence.HANDSHAKE)
   }
   e.cause?.let(queue::add);e.suppressed.take(4).forEach(queue::add)
  }
  return TlsEvidence.values().firstOrNull {it in found}?:TlsEvidence.UNKNOWN
 }
}

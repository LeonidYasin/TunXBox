package io.nekohasekai.sagernet.ui.diagnostics

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.SystemClock
import io.nekohasekai.sagernet.SagerNet
import io.nekohasekai.sagernet.database.ProxyEntity
import io.nekohasekai.sagernet.database.DataStore
import io.nekohasekai.sagernet.fmt.buildConfig
import io.nekohasekai.sagernet.fmt.v2ray.StandardV2RayBean
import io.nekohasekai.sagernet.group.*
import io.nekohasekai.sagernet.ui.tv.TvDiagnostics
import kotlinx.coroutines.*
import java.net.InetAddress
import java.net.InetSocketAddress
import java.security.cert.X509Certificate
import javax.net.ssl.*

/** Manual, bounded endpoint diagnosis. Not a packet capture or an internal Go trace. */
object ExtendedConnectionDiagnostics {
 const val LIMIT_MS=60000L
 private val worker=BoundedConnectionCheck()
 data class Step(val stage: String,val outcome: String,val durationMs: Long,val category: SubscriptionFailureCategory?=null,val evidence: TlsEvidence?=null)
 data class Report(val profile: Long,val at: Long,val network: String,val ipv4: Int,val ipv6: Int,
   val sni: Boolean,val alpn: Boolean,val customCa: Boolean,val ech: Boolean,val insecure: Boolean,val reality: Boolean,
   val chainLength: Int,val certExpiry: Long?,val steps: List<Step>) {
  fun text()=buildString {
   appendLine("Extended diagnostic · opt-in · ${LIMIT_MS/1000}s maximum")
   appendLine("Last completed attempt epoch ms: $at")
   appendLine("Physical network: $network · resolved IPv4=$ipv4 IPv6=$ipv6")
   appendLine("TLS flags: SNI configured=$sni ALPN configured=$alpn custom CA=$customCa ECH=$ech effective allowInsecure=$insecure REALITY configured=$reality")
   appendLine("Android strict TLS probe: chain length=$chainLength expiry epoch ms=${certExpiry?:"unavailable"}")
   steps.forEach {appendLine("${it.stage}: ${it.outcome} · ${it.durationMs} ms"+(it.category?.let {c->" · NET_${c.name}"}?:"")+(it.evidence?.let {e->" · TLS_${e.name}"}?:""))}
   appendLine("No names, hosts, IPs, URLs, credentials, certificate subjects or raw exceptions. DNS/TCP/TLS use an explicitly bound physical network, never the VPN default route.")
   appendLine("TLS probe uses Android trust and hostname verification; it does not emulate Go/uTLS/REALITY, proxy authentication or ECH. Its success is not proof of proxy access. URL probes use isolated profile cores, not the existing VPN tunnel. Compare with the separate active-core URL result. No VPN switch, TLS bypass or throughput/UDP/YouTube measurement.")
  }
 }
 suspend fun run(context: Context,profile: ProxyEntity,progress: suspend (String)->Unit): Report = withTimeout(LIMIT_MS) {
  val manager=context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
  fun eligible(n: android.net.Network)=manager.getNetworkCapabilities(n)?.let {
   it.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_VPN)&&!it.hasTransport(NetworkCapabilities.TRANSPORT_VPN)
  }==true
  val physical=SagerNet.underlyingNetwork?.takeIf(::eligible)?:manager.allNetworks.filter(::eligible).singleOrNull()
  val caps=physical?.let {manager.getNetworkCapabilities(it)}
  val kind=when {
   caps==null->"NONE_OR_AMBIGUOUS"
   caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)->"WIFI"
   caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)->"ETHERNET"
   caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)->"CELLULAR"
   else->"OTHER"
  }
  val bean=profile.requireBean();val tls=bean as? StandardV2RayBean
  val sni=!tls?.sni.isNullOrBlank();val alpn=!tls?.alpn.isNullOrBlank()
  val custom=!tls?.certificates.isNullOrBlank();val ech=tls?.enableECH==true
  val insecure=tls?.allowInsecure==true||DataStore.globalAllowInsecure
  val reality=!tls?.realityPubKey.isNullOrBlank()
  val steps=mutableListOf<Step>();var addresses=emptyList<InetAddress>()
  var chainLength=0;var expiry: Long?=null;var abort=false
  var endpoint: InetAddress?=null
  suspend fun probe(stage: String,work: suspend ()->Unit): Boolean {
   ensureActive();progress(stage)
   if(abort){steps.add(Step(stage,"SKIPPED_WORKER_BUSY",0));return false}
   if(physical!=null&&!eligible(physical))throw CancellationException("physical network changed")
   val start=SystemClock.elapsedRealtime();var evidence: TlsEvidence?=null
   val completed=java.util.concurrent.atomic.AtomicBoolean(false)
   val result=worker.check(profile.id,8000L) {
    try {runBlocking {work()};ConnectionTestResult(profile.id,System.currentTimeMillis(),0)}
    catch(error: Exception) {
     evidence=TlsFailureEvidence.classify(error)
     ConnectionTestResult(profile.id,System.currentTimeMillis(),-1,SubscriptionFailureCategory.fromThrowable(error))
    } finally {completed.set(true)}
   }
   ensureActive()
   steps.add(Step(stage,if(result.available)"PASS" else "FAIL",SystemClock.elapsedRealtime()-start,result.category,evidence?.takeIf {it!=TlsEvidence.UNKNOWN}))
   if(result.category==SubscriptionFailureCategory.BUSY||result.category==SubscriptionFailureCategory.TIMEOUT&&!completed.get())abort=true
   return result.available
  }
  val config=probe("CONFIG_COMPATIBILITY") {buildConfig(profile,true)}
  steps.add(Step("PHYSICAL_BINDING",if(physical==null)"SKIPPED_NO_UNIQUE_PHYSICAL_NETWORK" else "PASS",0))
  val dns=if(physical==null)false else probe("ENDPOINT_DNS") {addresses=physical.getAllByName(bean.serverAddress).toList();check(addresses.isNotEmpty())}
  if(physical==null)steps.add(Step("ENDPOINT_DNS","SKIPPED_NO_BINDING",0))
  fun socket()=checkNotNull(physical).socketFactory.createSocket().apply {
   soTimeout=4000
   try {connect(InetSocketAddress(checkNotNull(endpoint),bean.serverPort),3000)}catch(e: Exception){close();throw e}
  }
  var tcp=false
  if(dns&&bean.canTCPing()) {
   // One representative address per family; never expose literal addresses.
   val choices=listOfNotNull(addresses.firstOrNull {it is java.net.Inet4Address},addresses.firstOrNull {it is java.net.Inet6Address})
   for(address in choices) {
    endpoint=address
    val family=if(address is java.net.Inet4Address)"V4" else "V6"
    if(probe("ENDPOINT_TCP_$family") {socket().use {}})tcp=true
   }
   if(tcp)endpoint=choices.firstOrNull {address->
       val family=if(address is java.net.Inet4Address)"V4" else "V6"
       steps.any {it.stage=="ENDPOINT_TCP_$family"&&it.outcome=="PASS"}
   }
  } else steps.add(Step("ENDPOINT_TCP","SKIPPED_NOT_APPLICABLE_OR_DNS_FAILED",0))
  val strictTls=tls?.security=="tls"&&!reality&&tcp&&!custom&&!ech&&Build.VERSION.SDK_INT>=24
  if(strictTls)probe("ANDROID_STRICT_TLS") {
   val host=tls!!.sni.orEmpty().takeIf {it.isNotBlank()}?:bean.serverAddress
   socket().use {raw->
    ((SSLSocketFactory.getDefault() as SSLSocketFactory).createSocket(raw,host,bean.serverPort,true) as SSLSocket).use {ssl->
     ssl.soTimeout=4000
     ssl.sslParameters=ssl.sslParameters.apply {endpointIdentificationAlgorithm="HTTPS"}
     ssl.startHandshake()
     val chain=ssl.session.peerCertificates.filterIsInstance<X509Certificate>()
     chain.forEach {it.checkValidity()};chainLength=chain.size;expiry=chain.firstOrNull()?.notAfter?.time
    }
   }
  } else steps.add(Step("ANDROID_STRICT_TLS","SKIPPED_NOT_COMPARABLE_OR_NOT_APPLICABLE",0))
  if(config)for(i in 1..3)probe("PROFILE_URL_$i") {TvDiagnostics.url(profile)}
  else steps.add(Step("PROFILE_URL_SERIES","SKIPPED_CONFIG_FAILURE",0))
  Report(profile.id,System.currentTimeMillis(),kind,addresses.count {it is java.net.Inet4Address},addresses.count {it is java.net.Inet6Address},
   sni,alpn,custom,ech,insecure,reality,chainLength,expiry,steps.toList())
 }
}

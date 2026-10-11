package io.nekohasekai.sagernet.ui.diagnostics

import android.content.Context
import android.os.Build
import io.nekohasekai.sagernet.BuildConfig
import io.nekohasekai.sagernet.R
import io.nekohasekai.sagernet.database.DataStore
import io.nekohasekai.sagernet.database.ProxyEntity
import io.nekohasekai.sagernet.fmt.v2ray.StandardV2RayBean
import io.nekohasekai.sagernet.group.ConnectionTestResult
import io.nekohasekai.sagernet.group.SubscriptionFailureCategory
import io.nekohasekai.sagernet.group.SubscriptionFailurePresentation
import libcore.Libcore
import org.json.JSONObject

enum class DiagnosticStage { STARTUP, DEPENDENCY, RUNTIME, URL_TEST }
/** Strict whitelist. Raw error messages, names, hosts, URLs and credentials are never retained. */
data class ConnectionDiagnostic(val profile: Long,val at: Long,val stage: DiagnosticStage,
    val category: SubscriptionFailureCategory?,val protocol: String="unknown",val transport: String="unknown",
    val security: String="unknown",val elapsed: Int=-1) {
    fun encode()=JSONObject().put("v",1).put("profile",profile).put("at",at).put("stage",stage.name)
        .put("category",category?.name?:"-").put("protocol",protocol).put("transport",transport)
        .put("security",security).put("elapsed",elapsed).toString()
    companion object {
        private val protocols=setOf("SOCKS","SOCKS4","SOCKS4A","SOCKS5","HTTP","HTTPS","Shadowsocks","VMess","VLESS","Trojan","Trojan-Go","Mieru","Naïve","Hysteria1","Hysteria2","SSH","WireGuard","TUIC","ShadowTLS","AnyTLS","unknown")
        private val transports=setOf("tcp","raw","http","ws","quic","grpc","httpupgrade","xhttp","splithttp","kcp","mkcp","domainsocket","unknown")
        private val securities=setOf("tls","reality","none","unknown")
        fun decode(value: String?): ConnectionDiagnostic?=runCatching {
            require(value!=null&&value.length<=1024)
            val j=JSONObject(value);require(j.getInt("v")==1)
            val r=ConnectionDiagnostic(j.getLong("profile"),j.getLong("at"),DiagnosticStage.valueOf(j.getString("stage")),
                j.getString("category").let {if(it=="-")null else SubscriptionFailureCategory.valueOf(it)},
                j.getString("protocol"),j.getString("transport"),j.getString("security"),j.getInt("elapsed"))
            require(r.profile>=0&&r.at>0&&r.protocol in protocols&&r.transport in transports&&r.security in securities)
            require(if(r.category==null)r.stage==DiagnosticStage.URL_TEST&&r.elapsed>=0 else r.elapsed==-1)
            r
        }.getOrNull()
        fun capture(entity: ProxyEntity?,profile: Long,stage: DiagnosticStage,category: SubscriptionFailureCategory?,at: Long=System.currentTimeMillis(),elapsed: Int=-1): ConnectionDiagnostic {
            val source=entity?.takeIf {it.id==profile}
            val bean=runCatching {source?.requireBean()}.getOrNull() as? StandardV2RayBean
            val protocol=runCatching {source?.displayType()}.getOrNull()?.takeIf {it in protocols}?:"unknown"
            return ConnectionDiagnostic(profile,at,stage,category,protocol,
                bean?.type?.lowercase(java.util.Locale.ROOT)?.takeIf {it in transports}?:"unknown",
                bean?.security?.lowercase(java.util.Locale.ROOT)?.takeIf {it in securities}?:"unknown",elapsed)
        }
    }
}
/** Room-backed key-value store: the VPN service runs in :bg, not the UI process. Last completed attempt only. */
object ConnectionDiagnosticJournal {
    private const val KEY="tunxbox_connection_diagnostic_v1"
    fun clear() {runCatching {DataStore.configurationStore.remove(KEY)}}
    fun read(profile: Long)=runCatching {ConnectionDiagnostic.decode(DataStore.configurationStore.getString(KEY))?.takeIf {it.profile==profile}}.getOrNull()
    fun write(value: ConnectionDiagnostic) {
        if(ConnectionDiagnostic.decode(value.encode())==null)return
        runCatching {
            val previous=ConnectionDiagnostic.decode(DataStore.configurationStore.getString(KEY))
            if(previous==null||previous.profile!=value.profile||previous.at<=value.at)
                DataStore.configurationStore.putString(KEY,value.encode())
        }
    }
    fun startup(entity: ProxyEntity,category: SubscriptionFailureCategory,stage: DiagnosticStage=DiagnosticStage.STARTUP) = write(ConnectionDiagnostic.capture(entity,entity.id,stage,category))
    fun url(entity: ProxyEntity?,result: ConnectionTestResult)=write(ConnectionDiagnostic.capture(entity,result.profileId,DiagnosticStage.URL_TEST,result.category,result.attemptedAt,result.elapsedMs))
}
object ConnectionDiagnosticReport {
    fun text(context: Context,record: ConnectionDiagnostic?,sent: Long?=null,received: Long?=null): String = buildString {
        appendLine("TunXBox connection diagnostic · schema 1")
        appendLine("APK: ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
        appendLine("Commit: ${BuildConfig.BUILD_COMMIT}")
        appendLine("CI: ${BuildConfig.BUILD_RUN_NUMBER} / ${BuildConfig.BUILD_RUN_ID}")
        appendLine("Core source: ${BuildConfig.CORE_SOURCE_COMMIT}")
        appendLine("sing-box: ${runCatching {Libcore.versionBox()}.getOrDefault("unavailable")}")
        appendLine("Statistics interval setting: ${DataStore.speedInterval} ms; session totals independent of notification setting")
        appendLine("Android API: ${Build.VERSION.SDK_INT} · ABI: ${Build.SUPPORTED_ABIS.joinToString()}")
        appendLine()
        if(record==null)appendLine(context.getString(R.string.diagnostic_none)) else {
            appendLine("Attempt: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss XXX",java.util.Locale.ROOT).apply {timeZone=java.util.TimeZone.getTimeZone("UTC")}.format(java.util.Date(record.at))}")
            appendLine("Stage: ${record.stage} · Protocol: ${record.protocol} · Transport: ${record.transport} · Security: ${record.security}")
            appendLine("Code: ${record.category?.let {"NET_${it.name}"}?:"NET_OK"}")
            if(record.elapsed>=0)appendLine("URL RTT: ${record.elapsed} ms")
            record.category?.let {appendLine(SubscriptionFailurePresentation.reason(context,it))}
            if(record.stage==DiagnosticStage.STARTUP&&record.category==SubscriptionFailureCategory.UNSUPPORTED)
                appendLine(context.getString(R.string.diagnostic_unsupported_action))
        }
        if(sent!=null&&received!=null)appendLine("Current session bytes: sent=$sent received=$received")
        else appendLine(context.getString(R.string.diagnostic_counters_missing))
        appendLine();appendLine(context.getString(R.string.diagnostic_scope))
        appendLine(context.getString(R.string.diagnostic_privacy))
    }
}

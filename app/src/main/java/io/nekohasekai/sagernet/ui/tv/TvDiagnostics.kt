package io.nekohasekai.sagernet.ui.tv

import android.os.SystemClock
import io.nekohasekai.sagernet.SagerNet
import io.nekohasekai.sagernet.bg.proto.UrlTest
import io.nekohasekai.sagernet.database.ProxyEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.InetSocketAddress
import java.net.InetAddress
import java.net.Socket

/** Same TCP endpoint / upstream URL test as the mobile UI, not ICMP or a speed benchmark. */
object TvDiagnostics {
    suspend fun tcp(profile: ProxyEntity): Int = withContext(Dispatchers.IO) {
        val bean = profile.requireBean()
        val network = SagerNet.underlyingNetwork
        val address = (network?.getAllByName(bean.serverAddress) ?: InetAddress.getAllByName(bean.serverAddress)).first()
        (network?.socketFactory?.createSocket() ?: Socket()).use { socket ->
            val started = SystemClock.elapsedRealtime()
            socket.connect(InetSocketAddress(address, bean.serverPort), 3000)
            (SystemClock.elapsedRealtime() - started).toInt()
        }
    }
    suspend fun url(profile: ProxyEntity): Int = withContext(Dispatchers.IO) { UrlTest().doTest(profile) }
}

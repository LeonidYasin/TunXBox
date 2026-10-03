package io.nekohasekai.sagernet.service

import android.content.Context
import io.nekohasekai.sagernet.database.ProfileManager
import io.nekohasekai.sagernet.database.ProxyEntity
import io.nekohasekai.sagernet.fmt.HttpBean
import io.nekohasekai.sagernet.fmt.SocksBean
import io.nekohasekai.sagernet.network.tv.DiscoveredProxy
import io.nekohasekai.sagernet.network.tv.LanScanner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ProxyDiscoveryService(private val context: Context) {
    
    interface ScanResultListener {
        fun onScanComplete(proxies: List<DiscoveredProxy>)
        fun onScanError(error: String)
    }

    private var listener: ScanResultListener? = null
    private val scanner = LanScanner()

    fun setListener(listener: ScanResultListener) {
        this.listener = listener
    }

    suspend fun startScan() {
        try {
            val subnet = "192.168.1" 
            
            val proxies = scanner.scanNetwork(subnet)
            
            for (proxy in proxies) {
                saveProfile(proxy)
            }
            
            listener?.onScanComplete(proxies)
        } catch (e: Exception) {
            listener?.onScanError(e.message ?: "Unknown error")
        }
    }
    
    private suspend fun saveProfile(proxy: DiscoveredProxy) = withContext(Dispatchers.IO) {
        try {
            val profile = ProxyEntity()
            profile.name = proxy.name
            
            when (proxy.type) {
                "SOCKS5" -> {
                    val bean = SocksBean()
                    bean.serverAddress = proxy.ip
                    bean.serverPort = proxy.port
                    profile.type = 2 
                    profile.setBean(bean)
                }
                "HTTP" -> {
                    val bean = HttpBean()
                    bean.serverAddress = proxy.ip
                    bean.serverPort = proxy.port
                    profile.type = 1 
                    profile.setBean(bean)
                }
            }
            ProfileManager.createProfile(profile)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
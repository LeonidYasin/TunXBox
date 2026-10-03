package io.nekohasekai.sagernet.service

import android.content.Context
import io.nekohasekai.sagernet.network.tv.DiscoveredProxy
import io.nekohasekai.sagernet.network.tv.LanScanner

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
            val proxies = scanner.scanNetwork()
            listener?.onScanComplete(proxies)
        } catch (e: Exception) {
            listener?.onScanError(e.message ?: "Unknown error")
        }
    }
}

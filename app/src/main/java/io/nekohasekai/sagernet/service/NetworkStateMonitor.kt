package io.nekohasekai.sagernet.service

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import io.nekohasekai.sagernet.SagerNet
import io.nekohasekai.sagernet.database.DataStore

class NetworkStateMonitor(private val context: Context) {
    
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private var isStarted = false

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            if (DataStore.persistAcrossReboots) { 
                SagerNet.startService()
            }
        }

        override fun onLost(network: Network) {}
    }

    fun startMonitoring() {
        if (isStarted) return
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        connectivityManager.registerNetworkCallback(request, networkCallback)
        isStarted = true
    }

    fun stopMonitoring() {
        if (!isStarted) return
        connectivityManager.unregisterNetworkCallback(networkCallback)
        isStarted = false
    }
}
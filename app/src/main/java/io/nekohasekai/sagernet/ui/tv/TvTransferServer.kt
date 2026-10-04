package io.nekohasekai.sagernet.ui.tv

import fi.iki.elonen.NanoHTTPD
import io.nekohasekai.sagernet.database.DataStore
import io.nekohasekai.sagernet.database.ProfileManager
import io.nekohasekai.sagernet.group.RawUpdater
import io.nekohasekai.sagernet.ktx.Logs
import org.json.JSONObject
import java.io.IOException
import java.util.UUID

/**
 * Локальный HTTP сервер на TV для приёма профилей с телефона.
 * Запускается на порту 8765, принимает POST /import с JSON данными.
 */
class TvTransferServer(
    private val onImportSuccess: (count: Int) -> Unit,
    private val onImportError: (error: String) -> Unit
) : NanoHTTPD(8765) {

    private val sessionToken = UUID.randomUUID().toString().substring(0, 8)
    
    init {
        // Async start
        try {
            start(SOCKET_READ_TIMEOUT, false)
            Logs.i("TvTransferServer started on port 8765, token=$sessionToken")
        } catch (e: IOException) {
            Logs.e("Failed to start TvTransferServer", e)
        }
    }

    override fun serve(session: IHTTPSession): Response {
        // CORS headers для WebView на телефоне
        val headers = mapOf(
            "Access-Control-Allow-Origin" to "*",
            "Access-Control-Allow-Methods" to "POST, OPTIONS",
            "Access-Control-Allow-Headers" to "Content-Type, X-Session-Token"
        )

        return when {
            session.method == Method.OPTIONS -> {
                newFixedLengthResponse(Response.Status.OK, "text/plain", "OK").apply {
                    headers.forEach { (k, v) -> addHeader(k, v) }
                }
            }
            
            session.uri == "/import" && session.method == Method.POST -> {
                handleImport(session, headers)
            }
            
            session.uri == "/status" -> {
                val json = JSONObject().apply {
                    put("status", "ready")
                    put("session", sessionToken)
                    put("device", "TunXBox-TV")
                }
                newFixedLengthResponse(Response.Status.OK, "application/json", json.toString()).apply {
                    headers.forEach { (k, v) -> addHeader(k, v) }
                }
            }
            
            else -> {
                newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "Not Found").apply {
                    headers.forEach { (k, v) -> addHeader(k, v) }
                }
            }
        }
    }

    private fun handleImport(session: IHTTPSession, headers: Map<String, String>): Response {
        try {
            // Проверяем session token
            val token = session.headers["x-session-token"] ?: ""
            if (token != sessionToken) {
                return newFixedLengthResponse(Response.Status.FORBIDDEN, "text/plain", "Invalid session").apply {
                    this@apply.headers.forEach { (k, v) -> addHeader(k, v) }
                }
            }

            // Читаем тело запроса
            val contentLength = session.headers["content-length"]?.toIntOrNull() ?: 0
            val buffer = ByteArray(contentLength)
            session.inputStream.read(buffer)
            val body = String(buffer)

            // Парсим JSON
            val json = JSONObject(body)
            val profilesData = json.optString("profiles", "")
            val subscriptionUrl = json.optString("subscription_url", "")

            var importedCount = 0

            if (profilesData.isNotBlank()) {
                // Импорт из списка ссылок (ss://, vmess://...)
                val links = profilesData.split("\n").filter { it.isNotBlank() }
                val targetId = DataStore.selectedGroupForImport()
                
                for (link in links) {
                    try {
                        val proxies = RawUpdater.parseRaw(link)
                        if (!proxies.isNullOrEmpty()) {
                            for (proxy in proxies) {
                                ProfileManager.createProfile(targetId, proxy)
                                importedCount++
                            }
                        }
                    } catch (e: Exception) {
                        Logs.w("Failed to import profile: $link", e)
                    }
                }
            } else if (subscriptionUrl.isNotBlank()) {
                // Импорт по URL подписки
                try {
                    val text = java.net.URL(subscriptionUrl).readText()
                    val proxies = RawUpdater.parseRaw(text)
                    if (!proxies.isNullOrEmpty()) {
                        val targetId = DataStore.selectedGroupForImport()
                        for (proxy in proxies) {
                            ProfileManager.createProfile(targetId, proxy)
                            importedCount++
                        }
                    }
                } catch (e: Exception) {
                    Logs.w("Failed to import from URL: $subscriptionUrl", e)
                    return newFixedLengthResponse(Response.Status.BAD_REQUEST, "application/json", 
                        JSONObject().apply { put("error", e.message) }.toString()
                    ).apply {
                        headers.forEach { (k, v) -> addHeader(k, v) }
                    }
                }
            }

            if (importedCount > 0) {
                onImportSuccess(importedCount)
                val response = JSONObject().apply {
                    put("status", "success")
                    put("imported", importedCount)
                }
                return newFixedLengthResponse(Response.Status.OK, "application/json", response.toString()).apply {
                    headers.forEach { (k, v) -> addHeader(k, v) }
                }
            } else {
                onImportError("No valid profiles found")
                return newFixedLengthResponse(Response.Status.BAD_REQUEST, "application/json",
                    JSONObject().apply { put("error", "No valid profiles") }.toString()
                ).apply {
                    headers.forEach { (k, v) -> addHeader(k, v) }
                }
            }

        } catch (e: Exception) {
            Logs.e("Import error", e)
            onImportError(e.message ?: "Unknown error")
            return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "application/json",
                JSONObject().apply { put("error", e.message) }.toString()
            ).apply {
                headers.forEach { (k, v) -> addHeader(k, v) }
            }
        }
    }

    fun getSessionToken(): String = sessionToken
    
    fun getLocalIpAddress(): String {
        try {
            val interfaces = java.net.NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val intf = interfaces.nextElement()
                val addrs = intf.inetAddresses
                while (addrs.hasMoreElements()) {
                    val addr = addrs.nextElement()
                    if (!addr.isLoopbackAddress && addr is java.net.InetAddress) {
                        val hostAddr = addr.hostAddress
                        if (hostAddr != null && hostAddr.contains(".")) {
                            return hostAddr
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Logs.e("Failed to get IP", e)
        }
        return "192.168.1.100" // fallback
    }
}

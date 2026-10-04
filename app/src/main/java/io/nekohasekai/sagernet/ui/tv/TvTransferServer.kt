package io.nekohasekai.sagernet.ui.tv

import fi.iki.elonen.NanoHTTPD
import io.nekohasekai.sagernet.database.DataStore
import io.nekohasekai.sagernet.database.ProfileManager
import io.nekohasekai.sagernet.group.RawUpdater
import io.nekohasekai.sagernet.ktx.Logs
import kotlinx.coroutines.runBlocking
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
        try {
            start(SOCKET_READ_TIMEOUT, false)
            Logs.i("TvTransferServer started on port 8765, token=$sessionToken")
        } catch (e: IOException) {
            Logs.e("Failed to start TvTransferServer", e)
        }
    }

    private fun addCorsHeaders(response: Response): Response {
        response.addHeader("Access-Control-Allow-Origin", "*")
        response.addHeader("Access-Control-Allow-Methods", "POST, OPTIONS")
        response.addHeader("Access-Control-Allow-Headers", "Content-Type, X-Session-Token")
        return response
    }

    override fun serve(session: IHTTPSession): Response {
        return when {
            session.method == Method.OPTIONS -> {
                addCorsHeaders(newFixedLengthResponse(Response.Status.OK, "text/plain", "OK"))
            }
            
            session.uri == "/import" && session.method == Method.POST -> {
                handleImport(session)
            }
            
            session.uri == "/status" -> {
                val json = JSONObject().apply {
                    put("status", "ready")
                    put("session", sessionToken)
                    put("device", "TunXBox-TV")
                }
                addCorsHeaders(newFixedLengthResponse(Response.Status.OK, "application/json", json.toString()))
            }
            
            else -> {
                addCorsHeaders(newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "Not Found"))
            }
        }
    }

    private fun handleImport(session: IHTTPSession): Response {
        try {
            // Проверяем session token
            val token = session.headers["x-session-token"] ?: ""
            if (token != sessionToken) {
                return addCorsHeaders(newFixedLengthResponse(Response.Status.FORBIDDEN, "text/plain", "Invalid session"))
            }

            // Читаем тело запроса
            val bodyMap = HashMap<String, String>()
            session.parseBody(bodyMap)
            val body = bodyMap["postData"] ?: ""

            if (body.isBlank()) {
                return addCorsHeaders(newFixedLengthResponse(Response.Status.BAD_REQUEST, "application/json",
                    JSONObject().apply { put("error", "Empty body") }.toString()))
            }

            // Парсим JSON
            val json = JSONObject(body)
            val profilesData = json.optString("profiles", "")
            val subscriptionUrl = json.optString("subscription_url", "")

            var importedCount = 0

            if (profilesData.isNotBlank()) {
                val links = profilesData.split("\n").filter { it.isNotBlank() }
                val targetId = DataStore.selectedGroupForImport()
                
                for (link in links) {
                    try {
                        val proxies = RawUpdater.parseRaw(link)
                        if (!proxies.isNullOrEmpty()) {
                            for (proxy in proxies) {
                                // Используем runBlocking т.к. NanoHTTPD serve() не suspend
                                runBlocking {
                                    ProfileManager.createProfile(targetId, proxy)
                                }
                                importedCount++
                            }
                        }
                    } catch (e: Exception) {
                        Logs.w("Failed to import profile: $link", e)
                    }
                }
            } else if (subscriptionUrl.isNotBlank()) {
                try {
                    val text = java.net.URL(subscriptionUrl).readText()
                    val proxies = RawUpdater.parseRaw(text)
                    if (!proxies.isNullOrEmpty()) {
                        val targetId = DataStore.selectedGroupForImport()
                        for (proxy in proxies) {
                            runBlocking {
                                ProfileManager.createProfile(targetId, proxy)
                            }
                            importedCount++
                        }
                    }
                } catch (e: Exception) {
                    Logs.w("Failed to import from URL: $subscriptionUrl", e)
                    return addCorsHeaders(newFixedLengthResponse(Response.Status.BAD_REQUEST, "application/json", 
                        JSONObject().apply { put("error", e.message) }.toString()))
                }
            }

            if (importedCount > 0) {
                onImportSuccess(importedCount)
                val response = JSONObject().apply {
                    put("status", "success")
                    put("imported", importedCount)
                }
                return addCorsHeaders(newFixedLengthResponse(Response.Status.OK, "application/json", response.toString()))
            } else {
                onImportError("No valid profiles found")
                return addCorsHeaders(newFixedLengthResponse(Response.Status.BAD_REQUEST, "application/json",
                    JSONObject().apply { put("error", "No valid profiles") }.toString()))
            }

        } catch (e: Exception) {
            Logs.e("Import error", e)
            onImportError(e.message ?: "Unknown error")
            return addCorsHeaders(newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "application/json",
                JSONObject().apply { put("error", e.message) }.toString()))
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
        return "192.168.1.100"
    }
}

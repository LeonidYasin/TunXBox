package io.nekohasekai.sagernet.ui.tv

import fi.iki.elonen.NanoHTTPD
import io.nekohasekai.sagernet.database.DataStore
import io.nekohasekai.sagernet.database.ProfileManager
import io.nekohasekai.sagernet.database.SagerDatabase
import io.nekohasekai.sagernet.group.RawUpdater
import io.nekohasekai.sagernet.ktx.Logs
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import java.io.IOException
import java.util.UUID

/**
 * Локальный HTTP сервер на TV для приёма профилей с телефона.
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

    override fun serve(session: IHTTPSession): Response {
        val corsHeaders = mapOf(
            "Access-Control-Allow-Origin" to "*",
            "Access-Control-Allow-Methods" to "POST, GET, OPTIONS",
            "Access-Control-Allow-Headers" to "Content-Type, X-Session-Token"
        )

        fun Response.addCors(): Response {
            corsHeaders.forEach { (k, v) -> addHeader(k, v) }
            return this
        }

        return when {
            session.method == Method.OPTIONS -> {
                newFixedLengthResponse(Response.Status.OK, "text/plain", "OK").addCors()
            }
            
            session.uri == "/import" && session.method == Method.POST -> {
                handleImport(session, corsHeaders)
            }
            
            session.uri == "/status" -> {
                val json = JSONObject().apply {
                    put("status", "ready")
                    put("session", sessionToken)
                    put("device", "TunXBox-TV")
                }
                newFixedLengthResponse(Response.Status.OK, "application/json", json.toString()).addCors()
            }
            
            else -> {
                newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "Not Found").addCors()
            }
        }
    }

    private fun handleImport(session: IHTTPSession, corsHeaders: Map<String, String>): Response {
        fun Response.addCors(): Response {
            corsHeaders.forEach { (k, v) -> addHeader(k, v) }
            return this
        }
        
        try {
            val token = session.headers["x-session-token"] ?: ""
            if (token != sessionToken) {
                return newFixedLengthResponse(Response.Status.FORBIDDEN, "text/plain", "Invalid session").addCors()
            }

            val contentLength = session.headers["content-length"]?.toIntOrNull() ?: 0
            val buffer = ByteArray(contentLength)
            session.inputStream.read(buffer)
            val body = String(buffer)

            val json = JSONObject(body)
            val profilesData = json.optString("profiles", "")
            val subscriptionUrl = json.optString("subscription_url", "")

            var importedCount = 0

            // Используем runBlocking для вызова suspend функций из синхронного контекста NanoHTTPD
            runBlocking {
                if (profilesData.isNotBlank()) {
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
                    }
                }
            }

            if (importedCount > 0) {
                onImportSuccess(importedCount)
                val response = JSONObject().apply {
                    put("status", "success")
                    put("imported", importedCount)
                }
                return newFixedLengthResponse(Response.Status.OK, "application/json", response.toString()).addCors()
            } else {
                onImportError("No valid profiles found")
                return newFixedLengthResponse(Response.Status.BAD_REQUEST, "application/json",
                    JSONObject().apply { put("error", "No valid profiles") }.toString()
                ).addCors()
            }

        } catch (e: Exception) {
            Logs.e("Import error", e)
            onImportError(e.message ?: "Unknown error")
            return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "application/json",
                JSONObject().apply { put("error", e.message) }.toString()
            ).addCors()
        }
    }

    private fun handleExport(session: IHTTPSession, corsHeaders: Map<String, String>): Response {
        fun Response.addCors(): Response {
            corsHeaders.forEach { (k, v) -> addHeader(k, v) }
            return this
        }
        
        try {
            val token = session.parameters["session"]?.firstOrNull() ?: ""
            if (token != sessionToken) {
                return newFixedLengthResponse(Response.Status.FORBIDDEN, "text/plain", "Invalid session").addCors()
            }

            // Get all profiles from current group
            val groupId = DataStore.selectedGroup
            val profiles = SagerDatabase.proxyDao.getByGroup(groupId)
            
            val profilesList = profiles.mapNotNull { profile ->
                try {
                    val link = profile.toStdLink()
                    if (link.isNotBlank()) {
                        JSONObject().apply {
                            put("name", profile.displayName())
                            put("type", profile.displayType())
                            put("link", link)
                        }
                    } else null
                } catch (e: Exception) {
                    Logs.w("Failed to export profile: ${profile.displayName()}", e)
                    null
                }
            }

            val json = JSONObject().apply {
                put("status", "success")
                put("device", "TunXBox-TV")
                put("profiles", org.json.JSONArray(profilesList))
                put("count", profilesList.size)
            }

            return newFixedLengthResponse(Response.Status.OK, "application/json", json.toString()).addCors()

        } catch (e: Exception) {
            Logs.e("Export error", e)
            return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "application/json",
                JSONObject().apply { put("error", e.message) }.toString()
            ).addCors()
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

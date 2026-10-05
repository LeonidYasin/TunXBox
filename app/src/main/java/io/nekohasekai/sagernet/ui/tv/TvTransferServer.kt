package io.nekohasekai.sagernet.ui.tv

import fi.iki.elonen.NanoHTTPD
import io.nekohasekai.sagernet.database.DataStore
import io.nekohasekai.sagernet.database.SagerDatabase
import io.nekohasekai.sagernet.ktx.app
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import java.io.EOFException
import java.net.Inet4Address
import java.net.NetworkInterface
import java.util.Collections

/** Temporary LAN-only pairing server. The QR is the only source of the secret token.
 * HTTP is not encrypted: use only a trusted LAN, never port-forward this service.
 * Export is opt-in and is not enabled on a receiving TV.
 */
class TvTransferServer(
    private val onImportSuccess: ((Int) -> Unit)? = null,
    private val onImportError: ((String) -> Unit)? = null,
    private val allowExport: Boolean = false,
    private val bindAddress: String = findLanAddress()
) : NanoHTTPD(bindAddress, PORT) {
    private val sessionToken = TransferProtocol.newToken()
    private val startedNanos = System.nanoTime()
    private val importLock = Any()

    init {
        // Propagate bind errors so the UI never displays a QR for a nonexistent server.
        start(SOCKET_READ_TIMEOUT, true)
    }

    private fun expired() = (System.nanoTime() - startedNanos) / 1_000_000 >= TransferProtocol.SESSION_MILLIS

    private fun reply(status: Response.Status, message: String): Response =
        newFixedLengthResponse(status, "application/json; charset=utf-8", message).apply {
            addHeader("Cache-Control", "no-store")
            addHeader("Referrer-Policy", "no-referrer")
            addHeader("X-Content-Type-Options", "nosniff")
        }

    private fun error(status: Response.Status, message: String) =
        reply(status, JSONObject().put("error", message).toString())

    override fun serve(session: IHTTPSession): Response {
        val expectedHost = "$bindAddress:$PORT"
        // Prevent DNS rebinding and cross-origin browser requests. No wildcard CORS.
        if (session.headers["host"] != expectedHost) {
            return error(Response.Status.FORBIDDEN, "Invalid host")
        }
        val origin = session.headers["origin"]
        if (origin != null && origin != "http://$expectedHost") {
            return error(Response.Status.FORBIDDEN, "Cross-origin access denied")
        }
        if (session.uri == "/" && session.method == Method.GET) {
            val html = app.assets.open("tv-transfer.html").bufferedReader().use { it.readText() }
            return newFixedLengthResponse(Response.Status.OK, "text/html; charset=utf-8", html).apply {
                addHeader("Cache-Control", "no-store")
                addHeader("Referrer-Policy", "no-referrer")
                addHeader("X-Frame-Options", "DENY")
                addHeader("X-Content-Type-Options", "nosniff")
                addHeader("Content-Security-Policy", "default-src 'none'; script-src 'unsafe-inline'; style-src 'unsafe-inline'; connect-src 'self'; frame-ancestors 'none'; base-uri 'none'; form-action 'none'")
            }
        }
        if (session.uri == "/status" && session.method == Method.GET) {
            // Never include the pairing token or any profile data here.
            return reply(Response.Status.OK, JSONObject().put("status", if (expired()) "expired" else "ready").toString())
        }
        if (session.uri != "/import" && session.uri != "/export") {
            return error(Response.Status.NOT_FOUND, "Not found")
        }
        if (expired() || !TransferProtocol.tokenMatches(sessionToken, session.headers["x-session-token"] ?: "")) {
            return error(Response.Status.FORBIDDEN, "Invalid or expired pairing. Scan the QR again.")
        }
        if (session.uri == "/export" && session.method == Method.GET) {
            if (!allowExport) return error(Response.Status.FORBIDDEN, "Export is disabled on this receiver")
            return handleExport()
        }
        if (session.uri == "/import" && session.method == Method.POST) return handleImport(session)
        return error(Response.Status.METHOD_NOT_ALLOWED, "Method not allowed")
    }

    private fun handleExport(): Response = try {
        val profiles = runBlocking { SagerDatabase.proxyDao.getByGroup(DataStore.selectedGroup) }
        val links = profiles.mapNotNull {
            try { it.toStdLink().takeIf(String::isNotBlank) } catch (_: Exception) { null }
        }
        val json = JSONObject().put("status", "ok").put("profiles", links.joinToString("\n")).put("count", links.size)
        if (json.toString().toByteArray(Charsets.UTF_8).size > TransferProtocol.MAX_BODY_BYTES) {
            error(Response.Status.PAYLOAD_TOO_LARGE, "Profiles exceed 2 MiB. Transfer a smaller group.")
        } else reply(Response.Status.OK, json.toString())
    } catch (_: Exception) {
        error(Response.Status.INTERNAL_ERROR, "Could not export profiles")
    }

    private fun handleImport(session: IHTTPSession): Response {
        if (session.headers["transfer-encoding"] != null) {
            return error(Response.Status.BAD_REQUEST, "Chunked requests are not supported; supply Content-Length")
        }
        if (session.headers["content-type"]?.substringBefore(';')?.trim() != "application/json") {
            return error(Response.Status.UNSUPPORTED_MEDIA_TYPE, "Use application/json")
        }
        val length = session.headers["content-length"]?.toLongOrNull()
            ?: return error(Response.Status.BAD_REQUEST, "Content-Length required")
        if (length > TransferProtocol.MAX_BODY_BYTES) {
            return error(Response.Status.PAYLOAD_TOO_LARGE, "Maximum payload is 2 MiB")
        }
        if (length <= 0) return error(Response.Status.BAD_REQUEST, "Empty body")
        return try {
            val body = TransferProtocol.readBody(session.inputStream, length.toInt())
            val json = JSONObject(String(body, Charsets.UTF_8))
            val profiles = json.optString("profiles", "").trim()
            val subscription = json.optString("subscription_url", "").trim()
            require(profiles.isNotBlank() xor subscription.isNotBlank()) { "Supply profiles OR a subscription URL" }
            val imported = synchronized(importLock) {
                runBlocking {
                    if (profiles.isNotBlank()) TvProfileImporter.importProfiles(profiles)
                    else TvProfileImporter.importSubscription(subscription)
                }
            }
            onImportSuccess?.invoke(imported)
            reply(Response.Status.OK, JSONObject().put("status", "success").put("imported", imported).toString())
        } catch (_: EOFException) {
            error(Response.Status.BAD_REQUEST, "Incomplete request body")
        } catch (_: Exception) {
            // Configurations, tokens and subscription credentials must not enter logs/errors.
            val message = "Import failed. Check the configuration, subscription URL and network."
            onImportError?.invoke(message)
            error(Response.Status.BAD_REQUEST, message)
        }
    }

    fun getSessionToken(): String = sessionToken
    fun getLocalIpAddress(): String = bindAddress
    fun getAppQrData(): String = "tunxbox://transfer?ip=$bindAddress&port=$PORT&session=$sessionToken"
    // Fragment stays in the browser: it is not sent in HTTP requests or Referer headers.
    fun getBrowserQrData(): String = "http://$bindAddress:$PORT/#session=$sessionToken"

    companion object {
        const val PORT = 8765
        private fun findLanAddress(): String {
            val interfaces = Collections.list(NetworkInterface.getNetworkInterfaces())
                .filter { it.isUp && !it.isLoopback && !it.name.startsWith("tun") && !it.name.startsWith("tap") && !it.name.startsWith("wg") }
                .sortedBy { if (it.name.startsWith("wlan") || it.name.startsWith("eth")) 0 else 1 }
            return interfaces.flatMap { Collections.list(it.inetAddresses) }
                .filterIsInstance<Inet4Address>().firstOrNull { it.isSiteLocalAddress }?.hostAddress
                ?: error("No private LAN IPv4 address. Connect both devices to the same Wi-Fi/Ethernet network.")
        }
    }
}

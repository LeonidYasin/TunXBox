package io.nekohasekai.sagernet.ui.tv

import android.net.Uri
import io.nekohasekai.sagernet.database.DataStore
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/** Same explicit-direction QR protocol on TV and phone. Legacy QR defaults remain device-specific. */
object TvTransferClient {
    data class Result(val count: Int, val received: Boolean)
    data class Request(val ip: String, val port: Int, val token: String, val pull: Boolean)
    fun parse(text: String, legacyPull: Boolean): Request {
        val uri = Uri.parse(text)
        require(uri.scheme == "tunxbox" && uri.host == "transfer")
        val ip = requireNotNull(uri.getQueryParameter("ip"))
        val port = uri.getQueryParameter("port")?.toIntOrNull() ?: TvTransferServer.PORT
        TransferProtocol.requireLanAddress(ip, port)
        val token = requireNotNull(uri.getQueryParameter("session"))
        require(token.matches(Regex("[0-9a-f]{64}")))
        val pull = when (uri.getQueryParameter("mode")) {
            "export" -> true
            "import" -> false
            null -> legacyPull
            else -> error("Unknown transfer direction")
        }
        return Request(ip, port, token, pull)
    }
    suspend fun transfer(text: String, legacyPull: Boolean): Result {
        val request = parse(text, legacyPull)
        val outgoing = if (request.pull) null else TvGroupTransfer.exportGroup(DataStore.currentGroupId())
        val connection = URL("http://${request.ip}:${request.port}/${if (request.pull) "export" else "import"}").openConnection() as HttpURLConnection
        val data = try {
            connection.instanceFollowRedirects = false
            connection.connectTimeout = 10000; connection.readTimeout = 10000
            connection.setRequestProperty("X-Session-Token", request.token)
            if (outgoing != null) {
                connection.requestMethod = "POST"; connection.doOutput = true
                connection.setRequestProperty("Content-Type", "application/json")
                val body = JSONObject().put("profiles", outgoing.profiles).toString().toByteArray(Charsets.UTF_8)
                require(body.size <= TransferProtocol.MAX_BODY_BYTES)
                connection.setFixedLengthStreamingMode(body.size)
                connection.outputStream.use { it.write(body) }
            }
            require(connection.responseCode in 200..299) { "Transfer request failed" }
            JSONObject(connection.inputStream.use { String(TransferProtocol.readLimited(it), Charsets.UTF_8) })
        } finally { connection.disconnect() }
        val count = if (request.pull) TvProfileImporter.importProfiles(data.getString("profiles")) else data.getInt("imported")
        require(count > 0)
        return Result(count, request.pull)
    }
}

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

    /** Outbound JSON field: an updatable subscription URL or a frozen whole-group snapshot. */
    data class PushBody(val field: String, val value: String)

    /** What the pulled response asks the receiver to import. */
    sealed interface PullTarget {
        data class Subscription(val url: String) : PullTarget
        data class Profiles(val text: String) : PullTarget
    }

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

    fun pushBody(outgoing: TransferExport): PushBody {
        val url = outgoing.subscriptionUrl?.takeIf { it.isNotBlank() }
        return if (url != null) PushBody("subscription_url", url) else PushBody("profiles", outgoing.profiles)
    }

    fun pullTarget(subscriptionUrl: String?, profiles: String?): PullTarget {
        val url = subscriptionUrl?.trim().orEmpty()
        return if (url.isNotEmpty()) PullTarget.Subscription(url) else PullTarget.Profiles(profiles.orEmpty())
    }

    suspend fun transfer(
        text: String,
        legacyPull: Boolean,
        openConnection: (URL) -> HttpURLConnection = ::openLanConnection
    ): Result {
        val request = parse(text, legacyPull)
        val outgoing = if (request.pull) null else TvGroupTransfer.exportGroup(DataStore.currentGroupId())
        val connection = openConnection(URL("http://${request.ip}:${request.port}/${if (request.pull) "export" else "import"}"))
        val data = try {
            connection.instanceFollowRedirects = false
            connection.connectTimeout = 10000; connection.readTimeout = 10000
            connection.setRequestProperty("X-Session-Token", request.token)
            if (outgoing != null) {
                val push = pushBody(outgoing)
                connection.requestMethod = "POST"; connection.doOutput = true
                connection.setRequestProperty("Content-Type", "application/json")
                val body = JSONObject().put(push.field, push.value).toString().toByteArray(Charsets.UTF_8)
                require(body.size <= TransferProtocol.MAX_BODY_BYTES)
                connection.setFixedLengthStreamingMode(body.size)
                connection.outputStream.use { it.write(body) }
            }
            require(connection.responseCode in 200..299) { "Transfer request failed" }
            JSONObject(connection.inputStream.use { String(TransferProtocol.readLimited(it), Charsets.UTF_8) })
        } finally { connection.disconnect() }
        val count = if (request.pull) {
            val target = pullTarget(
                if (data.has("subscription_url")) data.optString("subscription_url") else null,
                if (data.has("profiles")) data.optString("profiles") else null
            )
            when (target) {
                is PullTarget.Subscription -> TvProfileImporter.importSubscription(target.url)
                is PullTarget.Profiles -> TvProfileImporter.importProfiles(target.text)
            }
        } else data.getInt("imported")
        require(count > 0)
        return Result(count, request.pull)
    }
}

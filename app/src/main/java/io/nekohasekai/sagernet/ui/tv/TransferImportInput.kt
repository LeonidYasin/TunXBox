package io.nekohasekai.sagernet.ui.tv

import java.net.URI

/** Matches upstream HTTP parsing: non-root paths are subscriptions, root URLs can be HTTP proxies. */
object TransferImportInput {
    fun isSubscription(value: String): Boolean {
        val text = value.trim()
        if (text.isEmpty() || text.any(Char::isWhitespace)) return false
        val uri = try { URI(text) } catch (_: Exception) { return false }
        return when (uri.scheme?.lowercase()) {
            "http", "https" -> !uri.host.isNullOrBlank() && !uri.rawPath.isNullOrEmpty() && uri.rawPath != "/"
            "sn" -> uri.host == "subscription"
            "clash" -> uri.host == "install-config"
            else -> false
        }
    }
    fun requireHttpSubscription(value: String): String {
        val text = value.trim()
        val uri = try { URI(text) } catch (_: Exception) { throw TransferImportFailure(TransferImportError.SUBSCRIPTION_INVALID) }
        if (uri.scheme?.lowercase() !in setOf("http", "https") || uri.host.isNullOrBlank()) {
            throw TransferImportFailure(TransferImportError.SUBSCRIPTION_INVALID)
        }
        return text
    }
}

enum class TransferImportError(val code: String, val publicMessage: String) {
    INPUT_INVALID("input_invalid", "Supply one configuration or subscription URL."),
    PROFILE_INVALID("profile_invalid", "No supported profiles found in the configuration."),
    SUBSCRIPTION_INVALID("subscription_invalid", "Use a valid HTTP(S) subscription URL."),
    SUBSCRIPTION_FORMAT("subscription_format", "The subscription response contains no supported proxy profiles. It may be a web page or an expired subscription."),
    SUBSCRIPTION_FAILED("subscription_failed", "The TV received the URL but could not download or import the subscription. Check Internet access from the TV; a VPN on the phone does not give the TV access."),
    IMPORT_FAILED("import_failed", "Could not import the configuration.")
}

/** Never expose parser/native errors: they can contain URLs, credentials and response bodies. */
class TransferImportFailure(val reason: TransferImportError) : IllegalArgumentException(reason.publicMessage)

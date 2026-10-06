package io.nekohasekai.sagernet.ui.tv

import android.app.Application
import java.net.HttpURLConnection
import java.net.URL
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** Real HTTP ingress and authentication; provider download is injected, never uses live credentials. */
@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [28])
class TransferImportServerTest {
    private var server: TvTransferServer? = null
    private var profileCalls = 0
    private var subscriptionCalls = 0
    private var received = ""
    private fun start(fail: Boolean = false, unexpected: Boolean = false): TvTransferServer {
        return TvTransferServer(bindAddress = "127.0.0.1", profilesImporter = { received = it; profileCalls++; 2 }, subscriptionImporter = {
            received = it; subscriptionCalls++
            if (unexpected) error("SECRET https://example.invalid/private-token")
            if (fail) throw TransferImportFailure(TransferImportError.SUBSCRIPTION_FAILED)
            3
        }).also { server = it }
    }
    private fun post(body: JSONObject, token: String = server!!.getSessionToken()): Pair<Int, String> {
        val connection = URL("http://127.0.0.1:${TvTransferServer.PORT}/import").openConnection() as HttpURLConnection
        try {
            connection.connectTimeout = 5000; connection.readTimeout = 5000; connection.requestMethod = "POST"; connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/json"); connection.setRequestProperty("X-Session-Token", token)
            val bytes = body.toString().toByteArray(Charsets.UTF_8); connection.setFixedLengthStreamingMode(bytes.size)
            connection.outputStream.use { it.write(bytes) }
            val status = connection.responseCode
            val content = (if (status >= 400) connection.errorStream else connection.inputStream).bufferedReader().use { it.readText() }
            return status to content
        } finally { connection.disconnect() }
    }
    @After fun close() { server?.stop() }
    @Test fun httpsSubscriptionInProfilesFieldRoutesToSubscriptionUpdater() {
        start(); val url = "https://subscriptions.example.invalid/opaque-test-token"
        val result = post(JSONObject().put("profiles", url))
        assertEquals(200, result.first); assertEquals(3, JSONObject(result.second).getInt("imported"))
        assertEquals(0, profileCalls); assertEquals(1, subscriptionCalls); assertEquals(url, received)
    }
    @Test fun explicitRootSubscriptionRemainsASubscription() {
        start(); val url = "https://subscriptions.example.invalid/"
        assertEquals(200, post(JSONObject().put("subscription_url", url)).first)
        assertEquals(0, profileCalls); assertEquals(1, subscriptionCalls)
    }
    @Test fun rootHttpProxyRemainsAProfile() {
        start(); val url = "http://proxy.example.invalid:8080"
        assertEquals(200, post(JSONObject().put("profiles", url)).first)
        assertEquals(1, profileCalls); assertEquals(0, subscriptionCalls)
    }
    @Test fun multilineProfilePayloadIsNotSilentlyReclassified() {
        start(); val text = "vless://test@example.invalid:443\nss://test@example.invalid:8388"
        assertEquals(200, post(JSONObject().put("profiles", text)).first)
        assertEquals(text, received); assertEquals(1, profileCalls); assertEquals(0, subscriptionCalls)
    }
    @Test fun rejectsAmbiguousInputBeforeAnyImporterRuns() {
        start(); val result = post(JSONObject().put("profiles", "profile").put("subscription_url", "https://example.invalid/sub"))
        assertEquals(400, result.first); assertEquals("input_invalid", JSONObject(result.second).getString("code"))
        assertEquals(0, profileCalls + subscriptionCalls)
    }
    @Test fun downloadFailureHasSafeSpecificErrorCode() {
        start(fail = true); val url = "https://subscriptions.example.invalid/private-test-token"
        val result = post(JSONObject().put("subscription_url", url))
        assertEquals(400, result.first); assertEquals("subscription_failed", JSONObject(result.second).getString("code")); assertFalse(result.second.contains(url))
    }
    @Test fun unexpectedFailureNeverEchoesNativeErrorOrPrivateUrl() {
        start(unexpected = true); val result = post(JSONObject().put("subscription_url", "https://example.invalid/private-token"))
        assertEquals(400, result.first); assertEquals("import_failed", JSONObject(result.second).getString("code"))
        assertFalse(result.second.contains("SECRET")); assertFalse(result.second.contains("private-token"))
    }
    @Test fun invalidPairingCannotReachEitherImporter() {
        start(); val result = post(JSONObject().put("profiles", "https://example.invalid/sub"), "wrong")
        assertEquals(403, result.first); assertEquals(0, profileCalls + subscriptionCalls)
    }
    @Test fun receiverDoesNotExposeProfilesThroughExport() {
        start()
        val connection=URL("http://127.0.0.1:${TvTransferServer.PORT}/export").openConnection() as HttpURLConnection
        try { connection.setRequestProperty("X-Session-Token",server!!.getSessionToken());assertEquals(403,connection.responseCode) }
        finally { connection.disconnect() }
    }
    @Test fun exportServerProvidesEntireInjectedGroupButRejectsImport() {
        val text="socks://127.0.0.1:1080#One\nsocks://127.0.0.1:1081#Two"
        server=TvTransferServer(bindAddress="127.0.0.1",allowExport=true,exportProvider={TransferExport(text,2)})
        val connection=URL("http://127.0.0.1:${TvTransferServer.PORT}/export").openConnection() as HttpURLConnection
        try {
            connection.setRequestProperty("X-Session-Token",server!!.getSessionToken())
            assertEquals(200,connection.responseCode)
            val result=JSONObject(connection.inputStream.bufferedReader().use { it.readText() })
            assertEquals(2,result.getInt("count"));assertEquals(text,result.getString("profiles"))
        } finally { connection.disconnect() }
        assertEquals(403,post(JSONObject().put("profiles","profile")).first)
    }
    @Test fun explicitQrDirectionsOverrideDeviceSpecificLegacyDefaults() {
        val base="tunxbox://transfer?ip=192.168.1.2&port=8765&session="+"a".repeat(64)
        for(legacy in listOf(false,true)) {
            assertTrue(TvTransferClient.parse(base+"&mode=export",legacy).pull)
            assertFalse(TvTransferClient.parse(base+"&mode=import",legacy).pull)
            assertEquals(legacy,TvTransferClient.parse(base,legacy).pull)
        }
        try { TvTransferClient.parse(base+"&mode=invalid",false);fail("Unknown direction must fail closed") }
        catch (_:IllegalStateException) { }
    }

}

package io.nekohasekai.sagernet.ui.update

import java.net.URI
import org.json.JSONObject
import io.nekohasekai.sagernet.ui.ReleaseUpdatePolicy

enum class UpdateChannel { STABLE, TEST }
data class ReleaseAsset(val name: String, val url: String, val size: Long)
data class UpdateOffer(val apk: ReleaseAsset)
data class UpdateBuild(val offer: UpdateOffer, val version: String, val versionCode: Long,
    val commit: String, val abis: Set<String>, val sha256: String)

/** Metadata is advisory; downloaded APK package/signature/bytes are verified separately. */
object ReleaseCatalog {
    const val REPO = "https://github.com/LeonidYasin/TunXBox"
    const val PIN = "34764328dd73482587e64d56008a9ef7b860069087e279cb984f7b2e5055b5ef"
    private val hash = Regex("[0-9a-f]{64}")
    fun trustedAssetUrl(url: String): Boolean = runCatching {
        val u = URI(url)
        u.scheme == "https" && u.host == "github.com" && u.userInfo == null && u.port == -1 &&
            u.rawPath.startsWith("/LeonidYasin/TunXBox/releases/download/") &&
            u.rawPath.split('/').none { it == "." || it == ".." || '%' in it || '\\' in it } && u.query == null && u.fragment == null
    }.getOrDefault(false)
    private fun size(o: JSONObject, key: String): Long {
        val n=o.get(key); require(n is Number); val v=n.toLong()
        require(v in 1..ResumableDownload.MAX_BYTES && n.toDouble()==v.toDouble()); return v
    }
    fun metadataUrl(release: JSONObject): String? {
        val assets=release.getJSONArray("assets")
        for (i in 0 until assets.length()) {
            val a=assets.getJSONObject(i)
            if (a.optString("name")=="apk-manifest.json" && trustedAssetUrl(a.optString("browser_download_url"))) return a.getString("browser_download_url")
        }
        return null
    }
    fun readBuild(release: JSONObject, manifest: JSONObject, channel: UpdateChannel,
                  supportedAbis: List<String>, currentCode: Long, installedSigner: String): UpdateBuild {
        require(!release.getBoolean("draft") && release.getBoolean("prerelease") == (channel==UpdateChannel.TEST)) { "Update channel mismatch" }
        require(installedSigner==PIN) { "Installed APK has another or unreadable signer; automatic update refused" }
        val identity=ReleaseUpdatePolicy.parse(manifest,"com.tunxbox.app",currentCode,installedSigner)
        require(identity.versionCode>=currentCode) { "This channel is older than the installed app. Downgrade refused; keep the current version." }
        require(manifest.getBoolean("signatureVerified") && manifest.getString("platform")=="android")
        require(manifest.getString("channel")==if(channel==UpdateChannel.TEST) "rc" else "stable")
        require(manifest.getString("buildType")=="release")
        val commit=manifest.getString("commit");require(Regex("[0-9a-f]{40}").matches(commit))
        val assets=release.getJSONArray("assets");val records=manifest.getJSONArray("apks")
        val candidates=(0 until records.length()).map { i ->
            val a=records.getJSONObject(i);val name=a.getString("name")
            require(Regex("[A-Za-z0-9._-]+\\.apk").matches(name) && name.startsWith("TunXBox-"))
            val bytes=size(a,"size");val digest=a.getString("sha256");require(hash.matches(digest))
            val native=a.getJSONArray("abis");val abis=(0 until native.length()).map { native.getString(it) }.toSet()
            require(abis.isNotEmpty() && abis.all { it in setOf("arm64-v8a","armeabi-v7a","x86","x86_64") } && abis.size==native.length())
            val asset=(0 until assets.length()).map { assets.getJSONObject(it) }.single { it.optString("name")==name }
            val url=asset.getString("browser_download_url")
            require(trustedAssetUrl(url) && URI(url).path.substringAfterLast('/')==name && size(asset,"size")==bytes)
            UpdateBuild(UpdateOffer(ReleaseAsset(name,url,bytes)),identity.versionName,identity.versionCode,commit,abis,digest)
        }.filter { it.abis.any { abi -> abi in supportedAbis } }
        require(candidates.isNotEmpty()) { "No APK compatible with this device CPU" }
        return candidates.sortedWith(compareBy<UpdateBuild> { b -> supportedAbis.indexOfFirst { it in b.abis } }.thenBy { it.abis.size }).first()
    }
}

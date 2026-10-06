package io.nekohasekai.sagernet.ui

import org.json.JSONObject

/** Rolling release titles don't change. Compare real Android metadata, never upstream names. */
object ReleaseUpdatePolicy {
    data class Release(val versionName: String, val versionCode: Long, val newer: Boolean)
    fun parse(manifest: JSONObject, applicationId: String, currentCode: Long, installedSigner: String): Release {
        require(manifest.getString("applicationId") == applicationId) { "Different application" }
        val signer = manifest.getString("signingCertificateSha256").lowercase()
        require(signer.matches(Regex("[0-9a-f]{64}")) && signer == installedSigner.lowercase()) { "Different signing identity" }
        val rawCode = manifest.get("versionCode")
        require(rawCode is Number)
        val code = rawCode.toLong()
        require(code in 1..Int.MAX_VALUE.toLong() && rawCode.toDouble() == code.toDouble())
        val name = manifest.getString("installedVersionName")
        require(name.isNotBlank())
        return Release(name, code, code > currentCode)
    }
}

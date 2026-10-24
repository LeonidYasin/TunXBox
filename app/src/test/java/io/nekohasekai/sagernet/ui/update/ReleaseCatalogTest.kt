package io.nekohasekai.sagernet.ui.update

import android.app.Application
import android.content.Intent
import android.net.Uri
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[28], application=Application::class)
class ReleaseCatalogTest {
    private val code=48000130L
    private fun record(abi: String)=JSONObject().put("name","TunXBox-fixture-$abi.apk").put("abis",JSONArray(if(abi=="universal") listOf("arm64-v8a","armeabi-v7a","x86_64","x86") else listOf(abi))).put("sha256","a".repeat(64)).put("size",100L)
    private fun manifest()=JSONObject().put("applicationId","com.tunxbox.app").put("versionCode",code).put("installedVersionName","1.6.0-rc.130").put("signingCertificateSha256",ReleaseCatalog.PIN).put("signatureVerified",true).put("platform","android").put("channel","rc").put("buildType","release").put("commit","b".repeat(40)).put("apks",JSONArray(listOf(record("universal"),record("arm64-v8a"),record("armeabi-v7a"))))
    private fun release(m: JSONObject)=JSONObject().put("draft",false).put("prerelease",true).put("assets",JSONArray((0 until m.getJSONArray("apks").length()).map { i -> val a=m.getJSONArray("apks").getJSONObject(i);JSONObject().put("name",a.getString("name")).put("size",a.get("size")).put("browser_download_url",ReleaseCatalog.REPO+"/releases/download/v1.6.0-rc/"+a.getString("name")) }))
    private fun read(m: JSONObject=manifest(), rel: JSONObject=release(m), current: Long=code, signer: String=ReleaseCatalog.PIN, abis: List<String> = listOf("arm64-v8a","armeabi-v7a"))=ReleaseCatalog.readBuild(rel,m,UpdateChannel.TEST,abis,current,signer)
    @Test fun prefersDeviceFirstAbiThenSingleAbiApk() { assertEquals(setOf("arm64-v8a"),read().abis);assertEquals(setOf("armeabi-v7a"),read(abis=listOf("armeabi-v7a")).abis);assertEquals(4,read(abis=listOf("x86_64")).abis.size) }
    @Test fun sameVersionIsExplicitlyAvailableButDowngradeRejected() { assertEquals(code,read().versionCode);assertTrue(runCatching { read(current=code+1) }.isFailure) }
    @Test fun rejectsWrongChannelDraftAndSigner() { val m=manifest();assertTrue(runCatching { read(m,release(m).put("prerelease",false)) }.isFailure);assertTrue(runCatching { read(m,release(m).put("draft",true)) }.isFailure);assertTrue(runCatching { read(signer="c".repeat(64)) }.isFailure) }
    @Test fun rejectsUnsafeAssetDestinations() { val good=ReleaseCatalog.REPO+"/releases/download/v1/test.apk";assertTrue(ReleaseCatalog.trustedAssetUrl(good));for(url in listOf(good.replace("https:","http:"),good.replace("TunXBox","proxydroid"),good.replace("github.com","github.com.evil.test"),good+"?token=1",good+"#x",good.replace("/test.apk","/%2e%2e/test.apk"),good.replace("github.com","user@github.com"))) assertFalse(url,ReleaseCatalog.trustedAssetUrl(url)) }
    @Test fun rejectsInvalidHashSizeCommitAndFractionalCode() { for(pair in listOf("commit" to "bad","versionCode" to 3.5,"signatureVerified" to false)) { val m=manifest().put(pair.first,pair.second);assertTrue(runCatching { read(m) }.isFailure) };for(pair in listOf("sha256" to "bad","size" to 100.5)) { val m=manifest();m.getJSONArray("apks").getJSONObject(0).put(pair.first,pair.second);assertTrue(runCatching { read(m) }.isFailure) } }
    @Test fun rejectsDuplicateAssetsOrMismatchingByteSize() { val m=manifest();val rel=release(m);rel.getJSONArray("assets").put(rel.getJSONArray("assets").getJSONObject(0));assertTrue(runCatching { read(m,rel) }.isFailure);val rel2=release(m);rel2.getJSONArray("assets").getJSONObject(0).put("size",101);assertTrue(runCatching { read(m,rel2) }.isFailure) }
    @Test fun refusesUnsupportedCpuAndPackage() { assertTrue(runCatching { read(abis=listOf("riscv64")) }.isFailure);assertTrue(runCatching { read(manifest().put("applicationId","moe.nb4a")) }.isFailure) }
    @Test fun missingOrDifferentSignaturesNeverAuthorizeInstall() { assertNotNull(SignatureReport(emptySet(),setOf("a")).failure);assertNotNull(SignatureReport(setOf("a")).failure);assertNotNull(SignatureReport(setOf("a"),setOf("b")).failure);assertNull(SignatureReport(setOf("a"),setOf("a")).failure) }
    @Test fun installerUsesContentReadGrantClipDataWithoutNewTask() { val uri=Uri.parse("content://com.tunxbox.app.update-files/verified-updates/test.apk");for(action in listOf(Intent.ACTION_INSTALL_PACKAGE,Intent.ACTION_VIEW)) { val intent=ApkInstallIntents.create(uri,action);assertEquals(uri,intent.data);assertEquals(uri,intent.clipData!!.getItemAt(0).uri);assertEquals("application/vnd.android.package-archive",intent.type);assertTrue(intent.flags and Intent.FLAG_GRANT_READ_URI_PERMISSION != 0);assertEquals(0,intent.flags and Intent.FLAG_ACTIVITY_NEW_TASK) };assertTrue(runCatching { ApkInstallIntents.create(Uri.parse("file:///tmp/test.apk"),Intent.ACTION_VIEW) }.isFailure) }
}

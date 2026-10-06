package io.nekohasekai.sagernet.ui

import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], application = android.app.Application::class)
class ReleaseUpdatePolicyTest {
    private val signer="a".repeat(64)
    private fun manifest(code:Long=47000094)=JSONObject().put("applicationId","com.tunxbox.app")
        .put("versionCode",code).put("installedVersionName","1.5.0-rc.94").put("signingCertificateSha256",signer)
    @Test fun rollingReleaseUsesAndroidCodeNotFixedReleaseTitle() {
        assertTrue(ReleaseUpdatePolicy.parse(manifest(),"com.tunxbox.app",47000093,signer).newer)
        assertFalse(ReleaseUpdatePolicy.parse(manifest(),"com.tunxbox.app",47000094,signer).newer)
        assertFalse(ReleaseUpdatePolicy.parse(manifest(),"com.tunxbox.app",47000095,signer).newer)
    }
    @Test fun upstreamPackageCannotBeOfferedAsTunxboxUpdate() {
        assertTrue(runCatching { ReleaseUpdatePolicy.parse(manifest().put("applicationId","moe.nb4a"),"com.tunxbox.app",1,signer) }.isFailure)
    }
    @Test fun differentSigningCertificateIsRejected() {
        assertTrue(runCatching { ReleaseUpdatePolicy.parse(manifest().put("signingCertificateSha256","b".repeat(64)),"com.tunxbox.app",1,signer) }.isFailure)
    }
    @Test fun missingOrFractionalMetadataIsRejected() {
        val missing=manifest();missing.remove("versionCode")
        assertTrue(runCatching { ReleaseUpdatePolicy.parse(missing,"com.tunxbox.app",1,signer) }.isFailure)
        assertTrue(runCatching { ReleaseUpdatePolicy.parse(manifest().put("versionCode",3.5),"com.tunxbox.app",1,signer) }.isFailure)
    }
    @Test fun currentProjectLinksAreNotUpstreamDownloadsOrAdvertising() {
        assertTrue(ProjectLinks.RELEASES.startsWith(ProjectLinks.REPOSITORY))
        assertTrue(ProjectLinks.RELEASE_API.contains("LeonidYasin/TunXBox"))
        assertTrue(ProjectLinks.PREVIEW_API.contains("LeonidYasin/TunXBox"))
        assertFalse(ProjectLinks.RELEASES.contains("MatsuriDayo"))
    }
}

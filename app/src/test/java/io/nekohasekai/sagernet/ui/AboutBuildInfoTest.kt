package io.nekohasekai.sagernet.ui

import org.junit.Assert.*
import org.junit.Test

class AboutBuildInfoTest {
    @Test fun retainsFullCommitVersionAndRunIdentity() {
        val sha = "0123456789abcdef0123456789abcdef01234567"
        val text = AboutBuildInfo.format(listOf("Commit" to sha, "Version" to "1.6.0-rc.125", "Run" to "125 / 38000000000"))
        assertTrue(text.contains(sha)); assertTrue(text.contains("1.6.0-rc.125")); assertTrue(text.contains("125 / 38000000000"))
    }
    @Test fun linesCannotSpoofAdditionalFacts() {
        assertEquals("Label injected: Value forged", AboutBuildInfo.format(listOf("Label\ninjected" to "Value\rforged")))
    }
    @Test fun noImplicitProviderOrUserInformation() {
        assertEquals("Package: com.tunxbox.app", AboutBuildInfo.format(listOf("Package" to "com.tunxbox.app")))
        assertEquals("", AboutBuildInfo.format(emptyList()))
    }
}

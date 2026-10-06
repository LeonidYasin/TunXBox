package io.nekohasekai.sagernet.ui

import android.app.Application
import android.content.Intent
import android.content.res.Configuration
import android.app.AlertDialog
import io.nekohasekai.sagernet.R
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowAlertDialog
import java.util.Locale

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [28])
class BrandingRecoveryTest {
    @Test fun brandNameCannotBeOverriddenByOldTranslations() {
        val app = RuntimeEnvironment.getApplication()
        for (language in listOf("en", "ru", "uk", "fa", "ar", "zh")) {
            val cfg = Configuration(app.resources.configuration).apply { setLocale(Locale(language)) }
            val localized = app.createConfigurationContext(cfg)
            assertEquals(language, "TunXBox", localized.getString(R.string.app_name))
            assertEquals(language, "TunXBox for Android", localized.getString(R.string.app_name_long))
        }
    }
    @Test fun applicationAndInstallerUseTunXBoxIcon() {
        val app = RuntimeEnvironment.getApplication()
        val info = app.packageManager.getApplicationInfo(app.packageName, 0)
        assertEquals(R.drawable.tunxbox_launcher, info.icon)
        assertNotNull(info.loadIcon(app.packageManager))
        assertEquals("TunXBox", info.loadLabel(app.packageManager).toString())
    }
    @Test fun tvBannerIsDedicatedLandscapeBrandAsset() {
        val app = RuntimeEnvironment.getApplication()
        val info = app.packageManager.getApplicationInfo(app.packageName, 0)
        assertEquals(R.drawable.tunxbox_banner, info.banner)
        val banner = info.loadBanner(app.packageManager)
        assertNotNull(banner)
        assertTrue(banner.intrinsicWidth > banner.intrinsicHeight)
    }
    @Test fun crashRecoveryDoesNotAutomaticallyOpenChooserAndCanClose() {
        val controller = Robolectric.buildActivity(BlankActivity::class.java, Intent().putExtra("sendLog", "Crash"))
        try {
            controller.setup().visible()
            assertNull(shadowOf(controller.get()).nextStartedActivity)
            val dialog = ShadowAlertDialog.getLatestAlertDialog()
            assertTrue(dialog.isShowing)
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).performClick()
            assertTrue(controller.get().isFinishing)
        } finally { controller.pause().stop().destroy() }
    }
    @Test fun backCancelsRecoveryWithoutReopeningIt() {
        val controller = Robolectric.buildActivity(BlankActivity::class.java, Intent().putExtra("sendLog", "Crash"))
        try {
            controller.setup().visible()
            ShadowAlertDialog.getLatestAlertDialog().cancel()
            assertTrue(controller.get().isFinishing)
            assertNull(shadowOf(controller.get()).nextStartedActivity)
        } finally { controller.pause().stop().destroy() }
    }
    @Test fun retryStartsExplicitMainActivityOnlyWhenRequested() {
        val controller = Robolectric.buildActivity(BlankActivity::class.java, Intent().putExtra("sendLog", "Crash"))
        try {
            controller.setup().visible()
            assertNull(shadowOf(controller.get()).nextStartedActivity)
            ShadowAlertDialog.getLatestAlertDialog().getButton(AlertDialog.BUTTON_POSITIVE).performClick()
            assertEquals(MainActivity::class.java.name, shadowOf(controller.get()).nextStartedActivity.component!!.className)
            assertTrue(controller.get().isFinishing)
        } finally { controller.pause().stop().destroy() }
    }
}

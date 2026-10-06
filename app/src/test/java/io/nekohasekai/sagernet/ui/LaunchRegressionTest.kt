package io.nekohasekai.sagernet.ui

import android.app.Application
import android.content.Intent
import android.os.Looper
import io.nekohasekai.sagernet.SagerNet
import io.nekohasekai.sagernet.R
import io.nekohasekai.sagernet.ui.tv.MainBrowseFragment
import io.nekohasekai.sagernet.ui.tv.TvUiPreferences
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.Implements
import org.robolectric.annotation.Implementation

/** Exercise real Activity + Browse lifecycle, not just detached presenters.
 * Only native Go core initialization is omitted: JVM tests cannot load Android JNI.
 */
@Implements(value = SagerNet::class, isInAndroidSdk = false)
class StartupApplicationShadow {
    @Implementation fun onCreate() { /* Android JNI core is tested by APK/device acceptance. */ }
}

@RunWith(RobolectricTestRunner::class)
@Config(application = SagerNet::class, shadows = [StartupApplicationShadow::class], sdk = [28], qualifiers = "land")
class LaunchRegressionTest {
    @Test fun coldTvLaunchCreatesBrowseAndNeverStartsChooser() {
        TvUiPreferences.phoneMode = false
        val controller = Robolectric.buildActivity(MainActivityTv::class.java)
        try {
            controller.setup().visible()
            controller.get().supportFragmentManager.executePendingTransactions()
            shadowOf(Looper.getMainLooper()).idle()
            assertTrue(controller.get().supportFragmentManager.findFragmentById(R.id.tv_container) is MainBrowseFragment)
            assertNull(shadowOf(controller.get()).nextStartedActivity)
        } finally { controller.pause().stop().destroy() }
    }
    @Test fun ordinaryLauncherRedirectIsExplicitNotAShareIntent() {
        TvUiPreferences.phoneMode = false
        val controller = Robolectric.buildActivity(MainActivity::class.java, Intent(Intent.ACTION_MAIN))
        try {
            controller.create()
            val target = shadowOf(controller.get()).nextStartedActivity
            assertNotNull(target)
            assertEquals(MainActivityTv::class.java.name, target.component!!.className)
            assertNotEquals(Intent.ACTION_SEND, target.action)
            assertTrue(controller.get().isFinishing)
        } finally { controller.destroy() }
    }
}

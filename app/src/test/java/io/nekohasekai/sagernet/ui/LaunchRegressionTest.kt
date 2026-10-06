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
import org.junit.Before
import org.junit.After
import android.content.ComponentName
import io.nekohasekai.sagernet.Action
import io.nekohasekai.sagernet.bg.VpnService
import androidx.room.MultiInstanceInvalidationService
import org.robolectric.RuntimeEnvironment
import org.robolectric.android.controller.ServiceController
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
class StartupApplicationShadow : org.robolectric.shadows.ShadowApplication() {
    @Implementation fun onCreate() { /* Android JNI core is tested by APK/device acceptance. */ }
}

@RunWith(RobolectricTestRunner::class)
@Config(application = SagerNet::class, shadows = [StartupApplicationShadow::class], sdk = [28], qualifiers = "land")
class LaunchRegressionTest {
    private lateinit var room: ServiceController<MultiInstanceInvalidationService>
    private lateinit var vpn: ServiceController<VpnService>
    @Before fun realServiceBinders() {
        val app = RuntimeEnvironment.getApplication()
        val shadow = shadowOf(app)
        room = Robolectric.buildService(MultiInstanceInvalidationService::class.java).create()
        val roomIntent = Intent(app, MultiInstanceInvalidationService::class.java)
        shadow.setComponentNameAndServiceForBindService(ComponentName(app, MultiInstanceInvalidationService::class.java), room.get().onBind(roomIntent))
        vpn = Robolectric.buildService(VpnService::class.java).create()
        val vpnIntent = Intent(app, VpnService::class.java).setAction(Action.SERVICE)
        shadow.setComponentNameAndServiceForBindServiceForIntent(vpnIntent, ComponentName(app, VpnService::class.java), vpn.get().onBind(vpnIntent))
        shadow.setUnbindServiceCallsOnServiceDisconnected(false)
    }
    @After fun closeServices() { if (::vpn.isInitialized) vpn.destroy(); if (::room.isInitialized) room.destroy() }

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
    @Test fun savedPhoneModeStartsConfigurationWithoutSharingOrRedirecting() {
        TvUiPreferences.phoneMode = true
        val controller = Robolectric.buildActivity(MainActivity::class.java, Intent(Intent.ACTION_MAIN))
        try {
            controller.setup().visible()
            controller.get().supportFragmentManager.executePendingTransactions()
            shadowOf(Looper.getMainLooper()).idle()
            assertTrue(controller.get().supportFragmentManager.findFragmentById(R.id.fragment_holder) is ConfigurationFragment)
            assertNull(shadowOf(controller.get()).nextStartedActivity)
            assertFalse(controller.get().isFinishing)
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

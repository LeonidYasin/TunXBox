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
    @Test @Config(qualifiers = "w390dp-h844dp-port")
    fun pickerDoesNotCreateMainInterfaceBeforeChoosingAndPhoneChoiceIsExplicit() {
        TvUiPreferences.phoneMode = true
        val controller = Robolectric.buildActivity(ModeSelectionActivity::class.java)
        try {
            controller.setup().visible()
            val activity = controller.get()
            assertNull(shadowOf(activity).nextStartedActivity)
            assertNull(activity.findViewById<android.view.View>(R.id.tv_container))
            assertNull(activity.findViewById<android.view.View>(R.id.fragment_holder))
            val phone = activity.findViewById<android.view.View>(R.id.mode_choose_phone)
            assertTrue(phone.hasFocus())
            phone.performClick(); phone.performClick()
            shadowOf(Looper.getMainLooper()).idle()
            val target = shadowOf(activity).nextStartedActivity
            assertEquals(MainActivity::class.java.name, target.component!!.className)
            assertTrue(target.getBooleanExtra("force_phone_mode", false))
            assertNull(shadowOf(activity).nextStartedActivity)
            assertTrue(activity.isFinishing)
        } finally { controller.pause().stop().destroy() }
    }
    @Test fun pickerRemembersTvFocusButStillWaitsForSelection() {
        TvUiPreferences.phoneMode = false
        val controller = Robolectric.buildActivity(ModeSelectionActivity::class.java)
        try {
            controller.setup().visible()
            val tv = controller.get().findViewById<android.view.View>(R.id.mode_choose_tv)
            assertTrue(tv.hasFocus())
            assertNull(shadowOf(controller.get()).nextStartedActivity)
            tv.performClick(); shadowOf(Looper.getMainLooper()).idle()
            assertEquals(MainActivityTv::class.java.name, shadowOf(controller.get()).nextStartedActivity.component!!.className)
            assertFalse(TvUiPreferences.phoneMode)
        } finally { controller.pause().stop().destroy() }
    }
    @Test fun pickerBackDoesNotChooseOrLaunch() {
        val controller = Robolectric.buildActivity(ModeSelectionActivity::class.java).setup()
        try {
            controller.get().onBackPressedDispatcher.onBackPressed()
            shadowOf(Looper.getMainLooper()).idle()
            assertTrue(controller.get().isFinishing)
            assertNull(shadowOf(controller.get()).nextStartedActivity)
        } finally { controller.pause().stop().destroy() }
    }
    @Test @Config(qualifiers = "w390dp-h844dp-port")
    fun portraitTvLaunchKeepsActionOrderAndSystemRotation() {
        val controller = Robolectric.buildActivity(MainActivityTv::class.java)
        try {
            controller.setup().visible()
            controller.get().supportFragmentManager.executePendingTransactions()
            shadowOf(Looper.getMainLooper()).idle()
            val fragment = controller.get().supportFragmentManager.findFragmentById(R.id.tv_container) as MainBrowseFragment
            val row = fragment.adapter[0] as androidx.leanback.widget.ListRow
            val actions = row.adapter
            assertEquals(listOf(7L, 2L, 1L, 3L), (0 until actions.size()).map { (actions[it] as io.nekohasekai.sagernet.ui.tv.TvAction).id })
            val info = controller.get().packageManager.getActivityInfo(controller.get().componentName, 0)
            assertEquals(android.content.pm.ActivityInfo.SCREEN_ORIENTATION_FULL_USER, info.screenOrientation)
            assertTrue(controller.get().resources.getDimension(R.dimen.tv_browse_rows_margin_start) / controller.get().resources.displayMetrics.density < 40)
            assertNull(shadowOf(controller.get()).nextStartedActivity)
        } finally { controller.pause().stop().destroy() }
    }
    @Test fun launcherAndDeepLinkResolveToDifferentActivities() {
        val app = RuntimeEnvironment.getApplication()
        val launcher = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER).setPackage(app.packageName)
        val entries = app.packageManager.queryIntentActivities(launcher, 0)
        assertEquals(1, entries.size)
        assertEquals(ModeSelectionActivity::class.java.name, entries.single().activityInfo.name)
        val tv = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER).setPackage(app.packageName)
        assertEquals(ModeSelectionActivity::class.java.name, app.packageManager.queryIntentActivities(tv, 0).single().activityInfo.name)
        val link = Intent(Intent.ACTION_VIEW, android.net.Uri.parse("sn://subscription?url=https%3A%2F%2Fexample.invalid%2Fdemo"))
            .addCategory(Intent.CATEGORY_BROWSABLE).setPackage(app.packageName)
        assertEquals(MainActivity::class.java.name, app.packageManager.queryIntentActivities(link, 0).single().activityInfo.name)
    }

}

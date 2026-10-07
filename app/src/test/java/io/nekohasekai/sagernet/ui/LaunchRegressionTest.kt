package io.nekohasekai.sagernet.ui

import android.app.Application
import android.content.Intent
import android.os.Looper
import kotlinx.coroutines.launch
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

@Implements(value = com.jakewharton.processphoenix.ProcessPhoenix::class, isInAndroidSdk = false)
class PhoenixUiShadow {
    companion object {
        var restarted: Intent? = null
        @JvmStatic @Implementation
        fun triggerRebirth(context: android.content.Context, intents: Array<Intent>) { restarted = intents.firstOrNull() }
    }
}

@RunWith(RobolectricTestRunner::class)
@Config(application = SagerNet::class, shadows = [StartupApplicationShadow::class, PhoenixUiShadow::class], sdk = [28], qualifiers = "land")
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
        TvUiPreferences.phoneMode = false
        val controller = Robolectric.buildActivity(MainActivityTv::class.java)
        try {
            controller.setup().visible()
            controller.get().supportFragmentManager.executePendingTransactions()
            shadowOf(Looper.getMainLooper()).idle()
            val fragment = controller.get().supportFragmentManager.findFragmentById(R.id.tv_container) as MainBrowseFragment
            val row = fragment.adapter[0] as androidx.leanback.widget.ListRow
            val actions = row.adapter
            assertEquals(listOf(1L, 2L, 3L, 7L), (0 until actions.size()).map { (actions[it] as io.nekohasekai.sagernet.ui.tv.TvAction).id })
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

    @Test fun advancedScreensFromTvDoNotSwitchSavedModeAndBackReturnsToTv() {
        TvUiPreferences.phoneMode = false
        val app = RuntimeEnvironment.getApplication()
        val intent = Intent(app, MainActivity::class.java).putExtra("tv_tools", true).putExtra("tv_destination", R.id.nav_configuration)
        val controller = Robolectric.buildActivity(MainActivity::class.java, intent)
        try {
            controller.setup().visible(); controller.get().supportFragmentManager.executePendingTransactions()
            assertFalse(TvUiPreferences.phoneMode)
            assertTrue(controller.get().supportFragmentManager.findFragmentById(R.id.fragment_holder) is ConfigurationFragment)
            controller.get().onBackPressedDispatcher.onBackPressed(); shadowOf(Looper.getMainLooper()).idle()
            assertTrue(controller.get().isFinishing)
        } finally { controller.pause().stop().destroy() }
    }
    @Test fun receivedProfilesReplaceQrWithProminentAcknowledgement() {
        TvUiPreferences.phoneMode = false
        val controller = Robolectric.buildActivity(MainActivityTv::class.java).setup().visible()
        try {
            val activity = controller.get()
            activity.supportFragmentManager.executePendingTransactions()
            val fragment = io.nekohasekai.sagernet.ui.tv.QrCodeTransferFragment()
            activity.supportFragmentManager.beginTransaction().replace(R.id.tv_container, fragment).addToBackStack("qr").commit()
            activity.supportFragmentManager.executePendingTransactions()
            fragment.acknowledgeImport(3)
            val dialog = org.robolectric.shadows.ShadowAlertDialog.getLatestAlertDialog()
            assertTrue(dialog.isShowing)
            assertTrue(dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE).hasFocus())
            dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE).performClick(); shadowOf(Looper.getMainLooper()).idle()
            activity.supportFragmentManager.executePendingTransactions()
            assertTrue(activity.supportFragmentManager.findFragmentById(R.id.tv_container) is MainBrowseFragment)
        } finally { controller.pause().stop().destroy() }
    }
    @Test fun trafficCountersUpdateConnectionCardAndPreserveActionOrder() {
        TvUiPreferences.phoneMode = false
        val controller = Robolectric.buildActivity(MainActivityTv::class.java).setup().visible()
        try {
            controller.get().supportFragmentManager.executePendingTransactions()
            val fragment = controller.get().supportFragmentManager.findFragmentById(R.id.tv_container) as MainBrowseFragment
            fragment.updateTraffic(io.nekohasekai.sagernet.aidl.SpeedDisplayData(txRateProxy = 2048, rxRateProxy = 4096, txTotal = 8192, rxTotal = 16384))
            val row = fragment.adapter[1] as androidx.leanback.widget.ListRow
            val status = row.adapter[0] as io.nekohasekai.sagernet.ui.tv.TvAction
            assertEquals(8L, status.id); assertTrue(status.subtitle.contains("↑")); assertTrue(status.subtitle.contains("↓"))
            val actions = (fragment.adapter[0] as androidx.leanback.widget.ListRow).adapter
            assertEquals(listOf(1L, 2L, 3L, 7L), (0 until actions.size()).map { (actions[it] as io.nekohasekai.sagernet.ui.tv.TvAction).id })
        } finally { controller.pause().stop().destroy() }
    }
    @Test fun tvHomeActionLeavesAppWithoutSharingOrDisconnectCommand() {
        TvUiPreferences.phoneMode = false
        val controller = Robolectric.buildActivity(MainActivityTv::class.java).setup().visible()
        try {
            controller.get().supportFragmentManager.executePendingTransactions()
            val fragment = controller.get().supportFragmentManager.findFragmentById(R.id.tv_container) as MainBrowseFragment
            fragment.openHome()
            val target = shadowOf(controller.get()).nextStartedActivity
            assertEquals(Intent.ACTION_MAIN, target.action); assertTrue(target.hasCategory(Intent.CATEGORY_HOME))
            assertFalse(controller.get().isFinishing)
        } finally { controller.pause().stop().destroy() }
    }

    @Test fun tcpLatencyChecksListeningLocalPortWithoutInternet() {
        SagerNet.underlyingNetwork = null
        java.net.ServerSocket(0, 1, java.net.InetAddress.getLoopbackAddress()).use { server ->
            val bean = io.nekohasekai.sagernet.fmt.socks.SOCKSBean().apply { serverAddress = server.inetAddress.hostAddress; serverPort = server.localPort }
            val profile = io.nekohasekai.sagernet.database.ProxyEntity().putBean(bean)
            val latency = kotlinx.coroutines.runBlocking { io.nekohasekai.sagernet.ui.tv.TvDiagnostics.tcp(profile) }
            assertTrue(latency >= 0)
        }
    }
    @Test fun tcpLatencyRejectsClosedLocalPort() {
        SagerNet.underlyingNetwork = null
        val address = java.net.InetAddress.getLoopbackAddress()
        val port = java.net.ServerSocket(0, 1, address).use { it.localPort }
        val bean = io.nekohasekai.sagernet.fmt.socks.SOCKSBean().apply { serverAddress = address.hostAddress; serverPort = port }
        val profile = io.nekohasekai.sagernet.database.ProxyEntity().putBean(bean)
        assertTrue(runCatching { kotlinx.coroutines.runBlocking { io.nekohasekai.sagernet.ui.tv.TvDiagnostics.tcp(profile) } }.isFailure)
    }

    private fun checkTvDestination(id: Int, expected: Class<*>) {
        TvUiPreferences.phoneMode = false
        kotlinx.coroutines.runBlocking(kotlinx.coroutines.Dispatchers.IO) { io.nekohasekai.sagernet.database.DataStore.currentGroup() }
        val app = RuntimeEnvironment.getApplication()
        val intent = Intent(app, MainActivity::class.java).putExtra("tv_tools", true).putExtra("tv_destination", id)
        val controller = Robolectric.buildActivity(MainActivity::class.java, intent)
        try {
            controller.setup().visible(); controller.get().supportFragmentManager.executePendingTransactions()
            shadowOf(Looper.getMainLooper()).idle()
            assertTrue(expected.isInstance(controller.get().supportFragmentManager.findFragmentById(R.id.fragment_holder)))
            assertFalse(TvUiPreferences.phoneMode)
            controller.get().onBackPressedDispatcher.onBackPressed(); shadowOf(Looper.getMainLooper()).idle()
            assertTrue(controller.get().isFinishing)
        } finally { controller.pause().stop().destroy() }
    }
    @Test fun tvGroupScreenStartsAndReturnsDirectly() = checkTvDestination(R.id.nav_group, GroupFragment::class.java)
    @Test fun tvRouteScreenStartsAndReturnsDirectly() = checkTvDestination(R.id.nav_route, RouteFragment::class.java)
    @Test fun tvSettingsScreenStartsAndReturnsDirectly() = checkTvDestination(R.id.nav_settings, SettingsFragment::class.java)
    @Test fun tvToolsScreenStartsAndReturnsDirectly() = checkTvDestination(R.id.nav_tools, ToolsFragment::class.java)
    @Test fun warmTvToolsIntentNavigatesExistingSingleTaskWithoutChangingMode() {
        TvUiPreferences.phoneMode = false
        val app = RuntimeEnvironment.getApplication()
        val controller = Robolectric.buildActivity(MainActivity::class.java, Intent(app, MainActivity::class.java).putExtra("tv_tools", true)).setup().visible()
        try {
            controller.newIntent(Intent(app, MainActivity::class.java).putExtra("tv_tools", true).putExtra("tv_destination", R.id.nav_route))
            controller.get().supportFragmentManager.executePendingTransactions()
            assertTrue(controller.get().supportFragmentManager.findFragmentById(R.id.fragment_holder) is RouteFragment)
            assertFalse(TvUiPreferences.phoneMode)
        } finally { controller.pause().stop().destroy() }
    }

    @Test fun repeatedServiceBinderDeliveryRegistersOnlyOnceAndLateDeliveryIsIgnored() {
        val app = RuntimeEnvironment.getApplication()
        var registered = 0
        val fake = object : io.nekohasekai.sagernet.aidl.ISagerNetService.Stub() {
            override fun getState() = 0
            override fun getProfileName() = "test"
            override fun registerCallback(cb: io.nekohasekai.sagernet.aidl.ISagerNetServiceCallback, id: Int) { registered++ }
            override fun unregisterCallback(cb: io.nekohasekai.sagernet.aidl.ISagerNetServiceCallback) { }
            override fun urlTest() = 1
        }
        val cls = io.nekohasekai.sagernet.bg.SagerConnection.serviceClass
        val component = ComponentName(app, cls)
        val intent = Intent(app, cls).setAction(Action.SERVICE)
        shadowOf(app).setComponentNameAndServiceForBindServiceForIntent(intent, component, fake)
        val connection = io.nekohasekai.sagernet.bg.SagerConnection(2, true)
        connection.connect(app, null)
        shadowOf(Looper.getMainLooper()).idle()
        connection.onServiceConnected(component, fake); connection.onServiceConnected(component, fake)
        assertEquals(1, registered)
        connection.disconnect(app)
        connection.onServiceConnected(component, fake)
        assertEquals(1, registered); assertNull(connection.service)
    }


    @Test fun coldTvFocusStartsAtConnectAndExitActionsAreLast() {
        TvUiPreferences.phoneMode = false
        val controller = Robolectric.buildActivity(MainActivityTv::class.java).setup().visible()
        try {
            controller.get().supportFragmentManager.executePendingTransactions(); shadowOf(Looper.getMainLooper()).idle()
            val fragment = controller.get().supportFragmentManager.findFragmentById(R.id.tv_container) as MainBrowseFragment
            val focus = MainBrowseFragment::class.java.getDeclaredField("focusedId").apply { isAccessible = true }
            assertEquals(1L, focus.getLong(fragment))
            val tools = (fragment.adapter[3] as androidx.leanback.widget.ListRow).adapter
            assertEquals(listOf(13L,14L), (tools.size()-2 until tools.size()).map { (tools[it] as io.nekohasekai.sagernet.ui.tv.TvAction).id })
        } finally { controller.pause().stop().destroy() }
    }
    @Test fun tvTrafficDoesNotNotifyOtherScrollingRows() {
        TvUiPreferences.phoneMode = false
        val controller = Robolectric.buildActivity(MainActivityTv::class.java).setup().visible()
        try {
            controller.get().supportFragmentManager.executePendingTransactions()
            val fragment = controller.get().supportFragmentManager.findFragmentById(R.id.tv_container) as MainBrowseFragment
            var updates = 0
            for(index in listOf(0,2,3)) ((fragment.adapter[index] as androidx.leanback.widget.ListRow).adapter).registerObserver(object : androidx.leanback.widget.ObjectAdapter.DataObserver() {
                override fun onChanged() { updates++ }
                override fun onItemRangeChanged(positionStart: Int, itemCount: Int) { updates++ }
            })
            repeat(10) { fragment.updateTraffic(io.nekohasekai.sagernet.aidl.SpeedDisplayData(txRateProxy=it.toLong()*9999999)) }
            assertEquals(0, updates)
        } finally { controller.pause().stop().destroy() }
    }
    @Test fun closeAppConfirmationDefaultsToCancelAndDoesNotDisconnectVpn() {
        TvUiPreferences.phoneMode = false
        val controller = Robolectric.buildActivity(MainActivityTv::class.java).setup().visible()
        try {
            controller.get().supportFragmentManager.executePendingTransactions()
            val dialog=AppLifecycleActions.confirm(controller.get(),false)
            shadowOf(Looper.getMainLooper()).idle()
            assertTrue(dialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE).hasFocus())
            val app=RuntimeEnvironment.getApplication()
            val before=shadowOf(app).broadcastIntents.count { it.action==Action.CLOSE }
            dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE).performClick()
            shadowOf(Looper.getMainLooper()).idle()
            assertTrue(controller.get().isFinishing)
            assertEquals(before,shadowOf(app).broadcastIntents.count { it.action==Action.CLOSE })
        } finally { controller.pause().stop().destroy() }
    }
    @Test fun restartIntentsKeepExplicitModeAndNeverOpenShareChooser() {
        val app=RuntimeEnvironment.getApplication()
        for(phone in listOf(false,true)) {
            val intent=AppLifecycleActions.restartIntent(app,phone)
            assertEquals(if(phone) MainActivity::class.java.name else MainActivityTv::class.java.name,intent.component!!.className)
            assertEquals(phone,intent.getBooleanExtra("force_phone_mode",false))
            assertTrue(intent.flags and Intent.FLAG_ACTIVITY_CLEAR_TASK != 0)
            assertNotEquals(Intent.ACTION_SEND,intent.action)
        }
    }
    @Test fun smartphoneLifecycleActionsAreAtEndOfDrawerAndOpenConfirmation() {
        TvUiPreferences.phoneMode = true
        val controller=Robolectric.buildActivity(MainActivity::class.java,Intent(RuntimeEnvironment.getApplication(),MainActivity::class.java).putExtra("force_phone_mode",true)).setup().visible()
        try {
            controller.get().supportFragmentManager.executePendingTransactions()
            val menu=controller.get().navigation.menu
            assertEquals(R.id.nav_restart_app,menu.getItem(menu.size()-2).itemId)
            assertEquals(R.id.nav_close_app,menu.getItem(menu.size()-1).itemId)
            assertTrue(controller.get().onNavigationItemSelected(menu.findItem(R.id.nav_restart_app)))
            shadowOf(Looper.getMainLooper()).idle()
            val dialog=org.robolectric.shadows.ShadowAlertDialog.getLatestAlertDialog()
            assertTrue(dialog.isShowing);assertTrue(dialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE).hasFocus())
            dialog.dismiss()
        } finally { controller.pause().stop().destroy() }
    }
    @Test fun promotionsFromTvIsLocalAndBackReturnsWithoutBrowserLaunch() = checkTvDestination(R.id.nav_tuiguang,PromotionsFragment::class.java)

    @Test fun confirmedRestartRestartsOnlyUiInTvModeWithoutDisconnectCommand() {
        TvUiPreferences.phoneMode=false;PhoenixUiShadow.restarted=null
        val controller=Robolectric.buildActivity(MainActivityTv::class.java).setup().visible()
        try {
            controller.get().supportFragmentManager.executePendingTransactions()
            val app=RuntimeEnvironment.getApplication()
            val before=shadowOf(app).broadcastIntents.count { it.action==Action.CLOSE }
            val dialog=AppLifecycleActions.confirm(controller.get(),true)
            shadowOf(Looper.getMainLooper()).idle()
            dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE).performClick()
            shadowOf(Looper.getMainLooper()).idle()
            assertEquals(MainActivityTv::class.java.name,PhoenixUiShadow.restarted!!.component!!.className)
            assertEquals(before,shadowOf(app).broadcastIntents.count { it.action==Action.CLOSE })
            assertFalse(TvUiPreferences.phoneMode)
        } finally { controller.pause().stop().destroy() }
    }
    @Test fun aboutFromTvStartsLocallyAndKeepsTvPreference() = checkTvDestination(R.id.nav_about,AboutFragment::class.java)
    @Test fun tvTopAddAndEmptyProfileCardOpenSameChooserNotQrTransfer() {
        TvUiPreferences.phoneMode = false
        val controller = Robolectric.buildActivity(MainActivityTv::class.java).setup().visible()
        try {
            val activity = controller.get()
            activity.supportFragmentManager.executePendingTransactions(); shadowOf(Looper.getMainLooper()).idle()
            val fragment = activity.supportFragmentManager.findFragmentById(R.id.tv_container) as MainBrowseFragment
            val row = fragment.adapter[0] as androidx.leanback.widget.ListRow
            val add = row.adapter[2] as io.nekohasekai.sagernet.ui.tv.TvAction
            assertEquals(3L, add.id); assertTrue(add.available)
            assertEquals(activity.getString(R.string.add_profile), add.title)
            val click = fragment.onItemViewClickedListener!!
            click.onItemClicked(null, add, null, row)
            shadowOf(Looper.getMainLooper()).idle()
            val first = org.robolectric.shadows.ShadowAlertDialog.getLatestAlertDialog()
            assertTrue(first.isShowing); assertEquals(7, first.listView.adapter.count)
            val firstLabels = (0 until first.listView.adapter.count).map { first.listView.adapter.getItem(it).toString() }
            assertSame(fragment, activity.supportFragmentManager.findFragmentById(R.id.tv_container))
            first.dismiss(); shadowOf(Looper.getMainLooper()).idle()
            click.onItemClicked(null, io.nekohasekai.sagernet.ui.tv.TvEmptyHint(activity.getString(R.string.tv_empty)), null, fragment.adapter[2] as androidx.leanback.widget.ListRow)
            shadowOf(Looper.getMainLooper()).idle()
            val second = org.robolectric.shadows.ShadowAlertDialog.getLatestAlertDialog()
            assertTrue(second.isShowing)
            assertEquals(firstLabels, (0 until second.listView.adapter.count).map { second.listView.adapter.getItem(it).toString() })
            assertSame(fragment, activity.supportFragmentManager.findFragmentById(R.id.tv_container))
        } finally { controller.pause().stop().destroy() }
    }
    @Test fun tvAddRemainsInTopRowAfterProfilesAppearAndOpensFullManualList() {
        TvUiPreferences.phoneMode = false
        val controller = Robolectric.buildActivity(MainActivityTv::class.java).setup().visible()
        try {
            val activity = controller.get()
            activity.supportFragmentManager.executePendingTransactions(); shadowOf(Looper.getMainLooper()).idle()
            val fragment = activity.supportFragmentManager.findFragmentById(R.id.tv_container) as MainBrowseFragment
            MainBrowseFragment::class.java.getDeclaredField("profiles").apply { isAccessible = true }
                .set(fragment, listOf(io.nekohasekai.sagernet.ui.tv.TvProfileCard(123, "Existing", "VLESS", "example.com", false, null)))
            MainBrowseFragment::class.java.getDeclaredMethod("refreshCards").apply { isAccessible = true }.invoke(fragment)
            val row = fragment.adapter[0] as androidx.leanback.widget.ListRow
            val add = row.adapter[2] as io.nekohasekai.sagernet.ui.tv.TvAction
            assertEquals(3L, add.id); assertTrue(add.available)
            fragment.onItemViewClickedListener!!.onItemClicked(null, add, null, row)
            shadowOf(Looper.getMainLooper()).idle()
            val chooser = org.robolectric.shadows.ShadowAlertDialog.getLatestAlertDialog()
            val entries = io.nekohasekai.sagernet.ui.tv.TvProfileAddCatalog.entries(activity)
            val index = entries.indexOfFirst { it.id == io.nekohasekai.sagernet.ui.tv.TvProfileAddCatalog.MANUAL }
            chooser.listView.performItemClick(android.view.View(activity), index, index.toLong())
            shadowOf(Looper.getMainLooper()).idle()
            val manual = org.robolectric.shadows.ShadowAlertDialog.getLatestAlertDialog()
            assertTrue(manual.isShowing); assertEquals(17, manual.listView.adapter.count)
            val all = ProfileCreationActions.manualEntries(activity)
            val customIndex = all.indexOfFirst { it.id == R.id.action_new_config }
            manual.listView.performItemClick(android.view.View(activity), customIndex, customIndex.toLong())
            shadowOf(Looper.getMainLooper()).idle()
            assertEquals(moe.matsuri.nb4a.proxy.config.ConfigSettingActivity::class.java.name,
                shadowOf(activity).nextStartedActivity!!.component!!.className)
            assertFalse(TvUiPreferences.phoneMode)
        } finally { controller.pause().stop().destroy() }
    }
    @Test fun tvPhoneReceivingRequiresExplicitChooserSelection() {
        TvUiPreferences.phoneMode = false
        val controller = Robolectric.buildActivity(MainActivityTv::class.java).setup().visible()
        try {
            val activity = controller.get()
            activity.supportFragmentManager.executePendingTransactions(); shadowOf(Looper.getMainLooper()).idle()
            val fragment = activity.supportFragmentManager.findFragmentById(R.id.tv_container) as MainBrowseFragment
            val row = fragment.adapter[0] as androidx.leanback.widget.ListRow
            fragment.onItemViewClickedListener!!.onItemClicked(null, row.adapter[2], null, row)
            shadowOf(Looper.getMainLooper()).idle()
            val chooser = org.robolectric.shadows.ShadowAlertDialog.getLatestAlertDialog()
            assertSame(fragment, activity.supportFragmentManager.findFragmentById(R.id.tv_container))
            val index = io.nekohasekai.sagernet.ui.tv.TvProfileAddCatalog.entries(activity)
                .indexOfFirst { it.id == io.nekohasekai.sagernet.ui.tv.TvProfileAddCatalog.PHONE }
            chooser.listView.performItemClick(android.view.View(activity), index, index.toLong())
            shadowOf(Looper.getMainLooper()).idle(); activity.supportFragmentManager.executePendingTransactions()
            assertTrue(activity.supportFragmentManager.findFragmentById(R.id.tv_container) is io.nekohasekai.sagernet.ui.tv.QrCodeTransferFragment)
        } finally { controller.pause().stop().destroy() }
    }
    @Test fun phonePlusStillDispatchesEverySharedManualEditor() {
        TvUiPreferences.phoneMode = true
        val controller = Robolectric.buildActivity(MainActivity::class.java,
            Intent(RuntimeEnvironment.getApplication(), MainActivity::class.java).putExtra("force_phone_mode", true)).setup().visible()
        try {
            val activity = controller.get()
            activity.supportFragmentManager.executePendingTransactions(); shadowOf(Looper.getMainLooper()).idle()
            val fragment = activity.supportFragmentManager.findFragmentById(R.id.fragment_holder) as ConfigurationFragment
            val source = ProfileCreationActions.addMenu(activity)
            for (entry in ProfileCreationActions.manualEntries(activity)) {
                assertTrue(fragment.onMenuItemClick(source.findItem(entry.id)))
                val actual = shadowOf(activity).nextStartedActivity!!
                val expected = ProfileCreationActions.intent(activity, entry.id)!!
                assertEquals(expected.component, actual.component)
                assertEquals(expected.getBooleanExtra("vless", false), actual.getBooleanExtra("vless", false))
            }
        } finally { controller.pause().stop().destroy() }
    }

    @Test fun completeGroupRoundTripRemapsInternalChainsWithoutDroppingProfiles() = kotlinx.coroutines.runBlocking {
        val source = io.nekohasekai.sagernet.database.GroupManager.createGroup(io.nekohasekai.sagernet.database.ProxyGroup(name="Group roundtrip",isSelector=true))
        val originals = (0..1).map { index -> io.nekohasekai.sagernet.database.ProfileManager.createProfile(source.id,
            io.nekohasekai.sagernet.fmt.socks.SOCKSBean().apply { initializeDefaultValues(); name="Profile $index"; serverPort=1080+index }) }
        io.nekohasekai.sagernet.database.ProfileManager.createProfile(source.id,
            io.nekohasekai.sagernet.fmt.internal.ChainBean().apply { initializeDefaultValues(); proxies=originals.map { it.id } })
        source.frontProxy=originals[0].id
        io.nekohasekai.sagernet.database.SagerDatabase.groupDao.updateGroup(source)
        val snapshot=io.nekohasekai.sagernet.ui.tv.TvGroupTransfer.exportGroup(source.id)
        assertEquals(3,snapshot.count)
        assertEquals(3,io.nekohasekai.sagernet.ui.tv.TvProfileImporter.importProfiles(snapshot.profiles))
        val target=io.nekohasekai.sagernet.database.DataStore.currentGroup()
        assertNotEquals(source.id,target.id);assertEquals(source.name,target.name);assertTrue(target.isSelector)
        val profiles=io.nekohasekai.sagernet.database.SagerDatabase.proxyDao.getByGroup(target.id)
        assertEquals(3,profiles.size)
        val socks=profiles.filter { it.socksBean!=null }
        assertEquals(socks.map { it.id },profiles.single { it.chainBean!=null }.chainBean!!.proxies)
        assertEquals(socks[0].id,target.frontProxy)
        assertTrue(socks.none { it.id in originals.map { old -> old.id } })
    }
    @Test fun groupExportRejectsExternalDependenciesRatherThanSilentlySkippingThem() = kotlinx.coroutines.runBlocking {
        val group=io.nekohasekai.sagernet.database.GroupManager.createGroup(io.nekohasekai.sagernet.database.ProxyGroup(name="External"))
        io.nekohasekai.sagernet.database.ProfileManager.createProfile(group.id,
            io.nekohasekai.sagernet.fmt.internal.ChainBean().apply { initializeDefaultValues(); proxies=listOf(Long.MAX_VALUE) })
        try { io.nekohasekai.sagernet.ui.tv.TvGroupTransfer.exportGroup(group.id);fail("Must refuse incomplete group") }
        catch (_:IllegalArgumentException) { }
    }
    @Test fun malformedGroupCannotCreatePartialGroup() = kotlinx.coroutines.runBlocking {
        val before=io.nekohasekai.sagernet.database.SagerDatabase.groupDao.allGroups().size
        val malformed=org.json.JSONObject().put("tunxbox_group",1).put("profiles",org.json.JSONArray().put(
            org.json.JSONObject().put("sourceId",1).put("link","sn://socks?invalid"))).toString()
        try { io.nekohasekai.sagernet.ui.tv.TvGroupTransfer.importGroup(malformed);fail("Must reject invalid profile") }
        catch (_:Exception) { }
        assertEquals(before,io.nekohasekai.sagernet.database.SagerDatabase.groupDao.allGroups().size)
    }
    @Test fun exportedGroupHasExplicitSendModeAndReceiverQrKeepsImportMode() {
        val receiver=io.nekohasekai.sagernet.ui.tv.TvTransferServer(bindAddress="127.0.0.1")
        try { assertEquals("import",android.net.Uri.parse(receiver.getAppQrData()).getQueryParameter("mode")) } finally { receiver.stop() }
        val sender=io.nekohasekai.sagernet.ui.tv.TvTransferServer(bindAddress="127.0.0.1",allowExport=true,
            exportProvider={io.nekohasekai.sagernet.ui.tv.TransferExport("socks://127.0.0.1:1080",1)})
        try { assertEquals("export",android.net.Uri.parse(sender.getAppQrData()).getQueryParameter("mode")) } finally { sender.stop() }
    }

    @Test @Config(qualifiers = "ru-land")
    fun phoneSubscriptionFailureIsPersistentLocalizedAndPrivate() {
        TvUiPreferences.phoneMode = true
        val controller = Robolectric.buildActivity(MainActivity::class.java, Intent(Intent.ACTION_MAIN)).setup().visible()
        var task: kotlinx.coroutines.Job? = null
        try {
            val activity = controller.get()
            activity.supportFragmentManager.executePendingTransactions(); shadowOf(Looper.getMainLooper()).idle()
            val callback = io.nekohasekai.sagernet.group.GroupInterfaceAdapter(activity)
            task = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main.immediate).launch {
                callback.onUpdateFailure(io.nekohasekai.sagernet.database.ProxyGroup(name="Synthetic"),
                    "Get https://example.invalid/sub/synthetic-token: x509 certificate has expired; password=synthetic-pass")
            }
            shadowOf(Looper.getMainLooper()).idle()
            val dialog = org.robolectric.shadows.ShadowDialog.getLatestDialog() as androidx.appcompat.app.AlertDialog
            assertTrue(dialog.isShowing)
            val text = dialog.findViewById<android.widget.TextView>(android.R.id.message)!!.text.toString()
            assertTrue(text.contains("SUB_TLS_TIME")); assertTrue(text.contains("Сертификат"))
            assertFalse(text.contains("synthetic-token")); assertFalse(text.contains("synthetic-pass")); assertFalse(text.contains("example.invalid"))
            assertTrue(task!!.isCompleted) // No update lock held while the user reads the dialog.
            controller.pause().stop(); shadowOf(Looper.getMainLooper()).idle()
            assertFalse(dialog.isShowing)
        } finally { task?.cancel(); controller.destroy() }
    }
    @Test fun tvSubscriptionFailureShowsActionableDialogNotGenericToast() {
        TvUiPreferences.phoneMode = false
        val controller = Robolectric.buildActivity(MainActivityTv::class.java).setup().visible()
        var task: kotlinx.coroutines.Job? = null
        try {
            val activity=controller.get()
            activity.supportFragmentManager.executePendingTransactions(); shadowOf(Looper.getMainLooper()).idle()
            val fragment=activity.supportFragmentManager.findFragmentById(R.id.tv_container) as MainBrowseFragment
            val callback=MainBrowseFragment::class.java.getDeclaredField("subscriptionInterface").apply { isAccessible=true }
                .get(fragment) as io.nekohasekai.sagernet.database.GroupManager.Interface
            task=kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main.immediate).launch {
                callback.onUpdateFailure(io.nekohasekai.sagernet.database.ProxyGroup(name="Synthetic"),
                    "https://example.invalid/sub/synthetic-token: reality verification failed")
            }
            shadowOf(Looper.getMainLooper()).idle()
            val dialog=org.robolectric.shadows.ShadowAlertDialog.getLatestAlertDialog()
            assertTrue(dialog.isShowing)
            val text=dialog.findViewById<android.widget.TextView>(android.R.id.message)!!.text.toString()
            assertTrue(text.contains("SUB_REALITY"));assertFalse(text.contains("synthetic-token"));assertFalse(text.contains("example.invalid"))
            assertTrue(dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE).hasFocus())
            assertTrue(MainBrowseFragment::class.java.getDeclaredField("updateFailureShown").apply { isAccessible=true }.getBoolean(fragment))
            dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE).performClick();shadowOf(Looper.getMainLooper()).idle()
            assertFalse(dialog.isShowing)
        } finally { task?.cancel(); controller.pause().stop().destroy() }
    }

}

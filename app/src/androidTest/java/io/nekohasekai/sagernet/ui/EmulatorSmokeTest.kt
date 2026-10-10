package io.nekohasekai.sagernet.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import androidx.test.runner.lifecycle.Stage
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.UiScrollable
import androidx.test.uiautomator.UiSelector
import androidx.test.uiautomator.Until
import io.nekohasekai.sagernet.R
import io.nekohasekai.sagernet.ktx.isPreview
import io.nekohasekai.sagernet.database.DataStore
import io.nekohasekai.sagernet.database.GroupManager
import io.nekohasekai.sagernet.database.SagerDatabase
import io.nekohasekai.sagernet.ui.tv.TvProfileAddCatalog
import io.nekohasekai.sagernet.ui.tv.TvUiPreferences
import kotlinx.coroutines.runBlocking
import libcore.Libcore
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestName
import org.junit.runner.RunWith
import java.io.File

/** Installed APK, real Android/native core/Room and actual DPAD input; no Robolectric shadows.
 * Offline smoke checks deliberately never request VPN consent or use a private subscription.
 */
@RunWith(AndroidJUnit4::class)
class EmulatorSmokeTest {
    @get:Rule val testName = TestName()
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context = instrumentation.targetContext
    private val device = UiDevice.getInstance(instrumentation)
    private val timeout = 15_000L
    private var scenario: ActivityScenario<out android.app.Activity>? = null

    @Before fun prepare() {
        device.wakeUp()
        device.setOrientationNatural()
        device.executeShellCommand("pm grant ${context.packageName} android.permission.POST_NOTIFICATIONS")
        TvUiPreferences.phoneMode = false
        runBlocking { GroupManager.clearGroup(DataStore.currentGroupId()) }
    }
    @After fun finish() {
        try {
            val directory = File(context.getExternalFilesDir(null), "smoke-artifacts").apply { mkdirs() }
            val name = testName.methodName
            val hierarchy = File(directory, "$name.xml")
            device.dumpWindowHierarchy(hierarchy)
            // AGP uninstalls the test APK after connected tests, deleting app-owned external files.
            // Copy synthetic UI evidence to shell-owned Downloads BEFORE that teardown.
            device.executeShellCommand("mkdir -p /sdcard/Download/TunXBoxSmoke")
            device.executeShellCommand("screencap -p /sdcard/Download/TunXBoxSmoke/$name.png")
            device.executeShellCommand("cp ${hierarchy.absolutePath} /sdcard/Download/TunXBoxSmoke/$name.xml")
            android.util.Log.i("TunXBoxSmoke", "$name: " + hierarchy.readText())
        } finally {
            scenario?.close()
            instrumentation.runOnMainSync {
                val monitor = ActivityLifecycleMonitorRegistry.getInstance()
                for (stage in listOf(Stage.RESUMED, Stage.STARTED, Stage.PAUSED, Stage.STOPPED))
                    monitor.getActivitiesInStage(stage).toList().forEach { it.finish() }
            }
            device.unfreezeRotation()
        }
    }
    private fun text(id: Int) = context.getString(id)
    private fun visibleText(value: String): UiObject2 = requireNotNull(device.wait(Until.findObject(By.text(value)), timeout)) { "Missing UI text: $value" }
    private fun startTv() {
        scenario = ActivityScenario.launch<MainActivityTv>(Intent(context, MainActivityTv::class.java))
        visibleText(text(R.string.tv_start))
        assertTrue("App must remain foreground", device.wait(Until.hasObject(By.pkg(context.packageName)), timeout))
    }
    private fun openTopAdd() {
        // Cold launch focuses Connect. Real DPAD moves through Group to Add profile.
        assertNotNull("Connect must receive initial focus", device.wait(Until.findObject(By.descStartsWith(text(R.string.tv_start)).focused(true)), timeout))
        device.pressDPadRight(); device.pressDPadRight(); device.pressDPadCenter()
        assertChooser()
    }
    private fun assertChooser() {
        visibleText(text(R.string.add_profile))
        // The full catalog may exceed a small portrait/landscape viewport. Assert reachability,
        // not simultaneous visibility; every entry still has to be found on the real device.
        val list = UiScrollable(UiSelector().className("android.widget.ListView"))
        for (entry in TvProfileAddCatalog.entries(context)) {
            assertTrue("Unreachable add method: ${entry.title}", list.scrollIntoView(UiSelector().text(entry.title)))
            visibleText(entry.title)
        }
        list.scrollToBeginning(5)
        assertFalse("QR transfer must not open before explicit selection", device.hasObject(By.text(text(R.string.tv_qr_title))))
    }

    private fun clickAddMethod(id: Int) {
        val title = text(id)
        assertTrue("Unreachable requested add method: $title", UiScrollable(UiSelector().className("android.widget.ListView"))
            .scrollIntoView(UiSelector().text(title)))
        visibleText(title).click()
    }

    @Test fun nativeCoreLoadsOnActualExpectedPageSize() {
        val expected = InstrumentationRegistry.getArguments().getString("expectedPageSize") ?: error("CI must specify page size")
        assertEquals(expected, device.executeShellCommand("getconf PAGESIZE").trim())
        assertTrue("Actual JNI core must load", Libcore.versionBox().isNotBlank())
        TvUiPreferences.phoneMode = true
        scenario = ActivityScenario.launch<MainActivity>(Intent(context, MainActivity::class.java)
            .putExtra("tv_tools", true).putExtra("tv_destination", R.id.nav_lan_discovery).putExtra("gateway_quick", true))
        val gateway = requireNotNull(device.wait(Until.findObject(By.res(context.packageName, "lan_gateway_action")), timeout))
        assertFalse(device.hasObject(By.res(context.packageName, "lan_ports")))
        gateway.click()
        val save = requireNotNull(device.wait(Until.findObject(By.res("android", "button1")), timeout))
        assertTrue(save.text.equals(text(R.string.lan_save), ignoreCase=true))
        assertTrue(device.hasObject(By.textContains(text(R.string.lan_gateway_unverified))))
        assertFalse(device.hasObject(By.textContains(text(R.string.lan_tcp_unverified))))
        device.executeShellCommand("screencap -p /sdcard/Download/TunXBoxSmoke/gateway_profile_confirmation.png")
        // Explicit creation only; no selected profile change or VPN consent.
        val selected = DataStore.selectedProxy
        save.click()
        visibleText(text(R.string.lan_gateway_saved))
        assertEquals(selected,DataStore.selectedProxy)

        startTv()
    }
    @Test fun launcherPickerOpensTvWithoutShareChooser() {
        scenario = ActivityScenario.launch<ModeSelectionActivity>(Intent(context, ModeSelectionActivity::class.java))
        val tv = requireNotNull(device.wait(Until.findObject(By.res(context.packageName, "mode_choose_tv")), timeout))
        assertNotNull(device.findObject(By.res(context.packageName, "mode_choose_phone")))
        tv.click()
        visibleText(text(R.string.tv_start))
        assertFalse(TvUiPreferences.phoneMode)
        assertFalse(device.hasObject(By.pkg("com.android.intentresolver")))
    }
    @Test fun topAndEmptyProfileCardOfferSameAddMethods() {
        startTv(); openTopAdd()
        device.pressBack()
        device.pressDPadDown(); device.pressDPadDown()
        val empty = requireNotNull(device.wait(Until.findObject(By.desc(text(R.string.tv_empty))), timeout))
        empty.click(); assertChooser()
    }
    @Test fun manualChooserIncludesAdvancedEditorsOnRealAndroid() {
        startTv(); openTopAdd()
        clickAddMethod(R.string.add_profile_methods_manual_settings)
        visibleText(text(R.string.action_socks))
        val list = UiScrollable(UiSelector().className("android.widget.ListView"))
        assertTrue(list.scrollIntoView(UiSelector().text(text(R.string.custom_config))))
        visibleText(text(R.string.custom_config))
        assertTrue(list.scrollIntoView(UiSelector().text(text(R.string.proxy_chain))))
        visibleText(text(R.string.proxy_chain))
    }
    @Test fun clipboardImportCreatesProfileUsingRealParserAndRoom() {
        startTv()
        instrumentation.runOnMainSync {
            (context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(
                ClipData.newPlainText("Offline test profile", "socks://127.0.0.1:1080#EmulatorSmoke"))
        }
        openTopAdd()
        clickAddMethod(R.string.action_import)
        val deadline = android.os.SystemClock.elapsedRealtime() + timeout
        while (SagerDatabase.proxyDao.getByGroup(DataStore.currentGroupId()).isEmpty() && android.os.SystemClock.elapsedRealtime() < deadline)
            android.os.SystemClock.sleep(100)
        assertEquals(1, SagerDatabase.proxyDao.getByGroup(DataStore.currentGroupId()).size)
        assertFalse(TvUiPreferences.phoneMode)
        // No real connection attempted: loopback test profile exists only in disposable emulator.
    }
    @Test fun phoneChoiceLoadsConfigurationAndItsPlusMenu() {
        scenario = ActivityScenario.launch<ModeSelectionActivity>(Intent(context, ModeSelectionActivity::class.java))
        requireNotNull(device.wait(Until.findObject(By.res(context.packageName, "mode_choose_phone")), timeout)).click()
        // Preview intentionally shows a warning; stable OSS must not show that dialog.
        if (isPreview) {
            visibleText(text(android.R.string.ok)).click()
            assertTrue("Preview warning must dismiss and stay dismissed", device.wait(Until.gone(By.text(text(R.string.preview_version_hint))), timeout))
        } else {
            assertFalse("Stable OSS must not show preview warning", device.hasObject(By.text(text(R.string.preview_version_hint))))
        }
        val plus = requireNotNull(device.wait(Until.findObject(By.res(context.packageName, "action_add")), timeout))
        plus.click()
        visibleText(text(R.string.action_import))
        visibleText(text(R.string.action_import_file))
        visibleText(text(R.string.add_profile_methods_manual_settings))
        assertTrue(TvUiPreferences.phoneMode)
    }
    @Test fun receiveQrScreenRemainsReachableFromTopAdd() {
        startTv(); openTopAdd()
        clickAddMethod(R.string.tv_receive_qr)
        visibleText(text(R.string.tv_qr_title))
        visibleText(text(R.string.tv_qr_ready))
        assertFalse(device.hasObject(By.text(text(R.string.tv_send_qr_title))))
    }
    @Test fun wholeGroupTransfersOverRealAuthenticatedLanHttp() = runBlocking {
        val source = io.nekohasekai.sagernet.database.GroupManager.createGroup(io.nekohasekai.sagernet.database.ProxyGroup(name="Emulator group"))
        repeat(2) { index ->
            io.nekohasekai.sagernet.database.ProfileManager.createProfile(source.id,
                io.nekohasekai.sagernet.fmt.socks.SOCKSBean().apply { initializeDefaultValues(); name="Offline $index"; serverAddress="127.0.0.1"; serverPort=1080+index })
        }
        val payload = io.nekohasekai.sagernet.ui.tv.TvGroupTransfer.exportGroup(source.id)
        val server = io.nekohasekai.sagernet.ui.tv.TvTransferServer(allowExport=true, exportProvider={payload})
        try {
            val result = io.nekohasekai.sagernet.ui.tv.TvTransferClient.transfer(server.getAppQrData(), legacyPull=false)
            assertTrue(result.received); assertEquals(2,result.count)
            assertNotEquals(source.id,DataStore.selectedGroup)
            assertEquals(2,SagerDatabase.proxyDao.getByGroup(DataStore.selectedGroup).size)
        } finally { server.stop() }
        startTv()
    }


    @Test fun sharedLanScreenIsIdleAndUsableInBothOrientations() {
        scenario = ActivityScenario.launch<MainActivity>(Intent(context, MainActivity::class.java)
            .putExtra("tv_tools", true).putExtra("tv_destination", R.id.nav_lan_discovery))
        fun orientAndAssertIdle(orientation: Int, expected: Int) {
            scenario!!.onActivity { it.requestedOrientation = orientation }
            val deadline = android.os.SystemClock.elapsedRealtime() + timeout
            var ready = false
            while (!ready && android.os.SystemClock.elapsedRealtime() < deadline) {
                instrumentation.runOnMainSync {
                    ready = ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED).any {
                        it is MainActivity && it.resources.configuration.orientation == expected && it.findViewById<android.view.View>(R.id.lan_scan) != null
                    }
                }
                if (!ready) android.os.SystemClock.sleep(100)
            }
            assertTrue("LAN activity did not resume in the expected orientation", ready)
            instrumentation.waitForIdleSync()
            scenario!!.onActivity { activity ->
                val network = activity.findViewById<android.widget.Button>(R.id.lan_network)
                val ports = activity.findViewById<android.widget.EditText>(R.id.lan_ports)
                assertEquals(R.id.lan_ports, network.nextFocusDownId)
                assertTrue(ports.isFocusable)
                network.requestFocus()
                assertFalse(activity.findViewById<android.widget.CheckBox>(R.id.lan_consent).isChecked)
                assertFalse(activity.findViewById<android.widget.Button>(R.id.lan_scan).isEnabled)
                assertEquals(android.view.View.GONE, activity.findViewById<android.view.View>(R.id.lan_cancel).visibility)
                assertEquals(0, activity.findViewById<android.widget.LinearLayout>(R.id.lan_results).childCount)
                assertEquals("Global panel must not overlap this creation tool", android.view.View.GONE, activity.findViewById<android.view.View>(R.id.stats).visibility)
            }
            device.pressDPadDown()
            instrumentation.waitForIdleSync()
            scenario!!.onActivity { activity ->
                assertTrue("D-pad must reach the ports editor", activity.findViewById<android.widget.EditText>(R.id.lan_ports).hasFocus())
            }
            device.pressDPadUp()
        }
        visibleText(text(R.string.lan_title))
        orientAndAssertIdle(android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT, android.content.res.Configuration.ORIENTATION_PORTRAIT)
        device.executeShellCommand("mkdir -p /sdcard/Download/TunXBoxSmoke")
        device.executeShellCommand("screencap -p /sdcard/Download/TunXBoxSmoke/lan_discovery_portrait.png")
        orientAndAssertIdle(android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE, android.content.res.Configuration.ORIENTATION_LANDSCAPE)
        device.executeShellCommand("screencap -p /sdcard/Download/TunXBoxSmoke/lan_discovery_landscape.png")
    }
    @Test fun realSocketProbeAndRoomImportAreDuplicateSafeWithoutConnectingVpn() = runBlocking {
        val server = java.net.ServerSocket(0, 2, java.net.InetAddress.getByName("127.0.0.1"))
        val thread = Thread {
            server.accept().use { socket ->
                socket.soTimeout = 1000
                repeat(4) { assertTrue(socket.getInputStream().read() >= 0) }
                socket.getOutputStream().write(byteArrayOf(5, 0)); socket.getOutputStream().flush()
            }
        }.apply { isDaemon = true; start() }
        val db = SagerDatabase
        val group = GroupManager.createGroup(io.nekohasekai.sagernet.database.ProxyGroup(name = "LAN device fixture"))
        val selected = DataStore.selectedProxy; val current = DataStore.currentProfile
        try {
            val detected = io.nekohasekai.sagernet.ui.lan.ProxyProbe({ java.net.Socket() }).probe("127.0.0.1", server.localPort)!!
            assertEquals(io.nekohasekai.sagernet.ui.lan.ProbeKind.SOCKS5, detected.kind)
            // Store uses a synthetic private endpoint; loopback is NEVER accepted by production scope.
            val candidate = detected.copy(host = "192.168.1.22")
            val first = io.nekohasekai.sagernet.ui.lan.LanProfileStore.save(candidate, false, group.id, "Fixture", "", "")
            val duplicate = io.nekohasekai.sagernet.ui.lan.LanProfileStore.save(candidate, false, group.id, "Fixture", "", "")
            assertTrue(first.created); assertFalse(duplicate.created); assertEquals(first.profile.id, duplicate.profile.id)
            assertEquals(1, db.proxyDao.getByGroup(group.id).size)
            assertEquals(selected, DataStore.selectedProxy); assertEquals(current, DataStore.currentProfile)
        } finally { server.close(); thread.join(1000); db.proxyDao.deleteByGroup(group.id); db.groupDao.deleteById(group.id) }
    }
}

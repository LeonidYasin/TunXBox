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
        for (entry in TvProfileAddCatalog.entries(context)) visibleText(entry.title)
        assertFalse("QR transfer must not open before explicit selection", device.hasObject(By.text(text(R.string.tv_qr_title))))
    }

    @Test fun nativeCoreLoadsOnActualExpectedPageSize() {
        val expected = InstrumentationRegistry.getArguments().getString("expectedPageSize") ?: error("CI must specify page size")
        assertEquals(expected, device.executeShellCommand("getconf PAGESIZE").trim())
        assertTrue("Actual JNI core must load", Libcore.versionBox().isNotBlank())
        verifySavedDiagnosticsInBothModes()
    }
    /** Synthetic saved failure only: no provider request, VPN consent or automatic upload. */
    private fun verifySavedDiagnosticsInBothModes() {
        val oldGroup = DataStore.selectedGroup
        val group = runBlocking { GroupManager.createGroup(io.nekohasekai.sagernet.database.ProxyGroup(
            name = "Offline diagnostic fixture", type = io.nekohasekai.sagernet.GroupType.SUBSCRIPTION,
            subscription = io.nekohasekai.sagernet.database.SubscriptionBean().apply {
                initializeDefaultValues(); link = "https://fixture.invalid/private-token-not-for-report"
            })) }
        val record = io.nekohasekai.sagernet.group.SubscriptionUpdateResult(
            io.nekohasekai.sagernet.group.SubscriptionUpdateOutcome.FAILURE,
            io.nekohasekai.sagernet.group.SubscriptionUpdateStage.DOWNLOAD,
            1_791_324_000_000L, 1250, category = io.nekohasekai.sagernet.group.SubscriptionFailureCategory.TLS_TIME)
        try {
            DataStore.selectedGroup = group.id
            io.nekohasekai.sagernet.group.SubscriptionUpdateJournal.write(group.id, record)
            startTv()
            assertNotNull(device.wait(Until.findObject(By.descStartsWith(text(R.string.tv_start)).focused(true)), timeout))
            // Actions -> connection -> profiles -> tools, using real remote keys only.
            repeat(3) { device.pressDPadDown(); device.waitForIdle() }
            val title = text(R.string.subscription_attempt_title)
            repeat(8) {
                if (!device.hasObject(By.descStartsWith(title).focused(true))) {
                    device.pressDPadRight(); device.waitForIdle()
                }
            }
            assertNotNull("Saved diagnostic card must be reachable by DPAD",
                device.findObject(By.descStartsWith(title).focused(true)))
            device.pressDPadCenter()
            assertSafeDiagnosticDialog("subscription_diagnostics_tv")
            assertNotNull("TV dialog defaults to safe close, not Share",
                device.wait(Until.findObject(By.res("android", "button1").focused(true)), timeout))
            device.pressDPadCenter()
            assertTrue(device.wait(Until.gone(By.res("android", "message")), timeout))
            scenario?.close(); scenario = null

            TvUiPreferences.phoneMode = true
            scenario = ActivityScenario.launch<MainActivity>(Intent(context, MainActivity::class.java)
                .putExtra("tv_tools", true).putExtra("tv_destination", R.id.nav_group))
            val list = UiScrollable(UiSelector().resourceId("${context.packageName}:id/group_list"))
            assertTrue(list.scrollIntoView(UiSelector().text("Offline diagnostic fixture")))
            var card: UiObject2? = visibleText("Offline diagnostic fixture")
            var options: UiObject2? = null
            repeat(5) {
                if (options == null) {
                    options = card?.findObject(By.res(context.packageName, "options"))
                    card = card?.parent
                }
            }
            requireNotNull(options) { "Missing subscription group options" }.click()
            visibleText(title).click()
            assertSafeDiagnosticDialog("subscription_diagnostics_phone")
            visibleText(text(android.R.string.ok)).click()
            assertTrue(device.wait(Until.gone(By.res("android", "message")), timeout))
            assertEquals("Viewing/closing must retain the saved result", record,
                io.nekohasekai.sagernet.group.SubscriptionUpdateJournal.read(group.id))
        } finally {
            scenario?.close(); scenario = null
            DataStore.selectedGroup = oldGroup
            io.nekohasekai.sagernet.group.SubscriptionUpdateJournal.remove(group.id)
            runBlocking { GroupManager.deleteGroup(group.id) }
            TvUiPreferences.phoneMode = false
        }
    }
    private fun assertSafeDiagnosticDialog(screenshot: String) {
        val message = requireNotNull(device.wait(Until.findObject(By.res("android", "message")), timeout))
        assertTrue(message.text.contains("SUB_TLS_TIME"))
        assertTrue(message.text.contains("APK:"))
        assertFalse(message.text.contains("fixture.invalid"))
        assertFalse(message.text.contains("private-token-not-for-report"))
        visibleText(text(R.string.subscription_attempt_share))
        assertFalse("Opening diagnostic details must not auto-share", device.hasObject(By.pkg("com.android.intentresolver")))
        device.executeShellCommand("mkdir -p /sdcard/Download/TunXBoxSmoke")
        device.executeShellCommand("screencap -p /sdcard/Download/TunXBoxSmoke/$screenshot.png")
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
        visibleText(text(R.string.add_profile_methods_manual_settings)).click()
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
        visibleText(text(R.string.action_import)).click()
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
        visibleText(text(R.string.tv_receive_qr)).click()
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

}

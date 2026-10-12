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

    @Test fun appUpdatesIsOfflineUntilRequestedAndPreservesVpnState() {
        val selected=DataStore.selectedProxy
        val state=DataStore.serviceState
        scenario=ActivityScenario.launch<MainActivity>(Intent(context,MainActivity::class.java)
            .putExtra("tv_tools",true).putExtra("tv_destination",R.id.nav_app_updates))
        assertNotNull("Updater check action must be visible",device.wait(Until.findObject(By.res(context.packageName,"update_check")),timeout))
        scenario!!.onActivity { activity ->
            assertTrue(activity.findViewById<android.widget.RadioButton>(R.id.update_stable).isChecked)
            assertTrue(activity.findViewById<android.view.View>(R.id.update_check).isEnabled)
            assertFalse(activity.findViewById<android.view.View>(R.id.update_download).isEnabled)
            assertFalse(activity.findViewById<android.view.View>(R.id.update_install).isEnabled)
            assertEquals(text(R.string.update_idle),activity.findViewById<android.widget.TextView>(R.id.update_status).text.toString())
            assertEquals(android.view.View.GONE,activity.findViewById<android.view.View>(R.id.stats).visibility)
            activity.findViewById<android.view.View>(R.id.update_check).requestFocus()
            assertTrue(activity.findViewById<android.view.View>(R.id.update_check).hasFocus())
        }
        // Real remote input, without pressing Check (no network traffic in offline smoke).
        device.pressDPadUp();device.pressDPadCenter()
        scenario!!.onActivity { activity ->
            assertTrue("Remote must reach and select test channel",activity.findViewById<android.widget.RadioButton>(R.id.update_test).isChecked)
            assertEquals(text(R.string.update_idle),activity.findViewById<android.widget.TextView>(R.id.update_status).text.toString())
        }
        for(landscape in listOf(false,true)) {
            val expected=if(landscape)android.content.res.Configuration.ORIENTATION_LANDSCAPE else android.content.res.Configuration.ORIENTATION_PORTRAIT
            scenario!!.onActivity {it.requestedOrientation=if(landscape)android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE else android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT}
            var orientationReady=false
            val orientationDeadline=android.os.SystemClock.elapsedRealtime()+timeout
            while(!orientationReady&&android.os.SystemClock.elapsedRealtime()<orientationDeadline) {
                instrumentation.runOnMainSync {
                    orientationReady=ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED).any {
                        it is MainActivity&&it.resources.configuration.orientation==expected&&it.findViewById<android.view.View>(R.id.update_check)!=null
                    }
                }
                if(!orientationReady)android.os.SystemClock.sleep(100)
            }
            assertTrue("Updater must resume in expected orientation",orientationReady)
            instrumentation.waitForIdleSync()
            assertNotNull(device.wait(Until.findObject(By.res(context.packageName,"update_check")),timeout))
            val fixture=(1..12).joinToString("\n") {"Offline metadata line $it"}
            scenario!!.onActivity {activity ->
                val status=activity.findViewById<android.widget.TextView>(R.id.update_status)
                status.text=fixture
                assertTrue(status.minLines>=8);assertFalse(status.isFocusable);assertFalse(status.isTextSelectable)
                val preview=activity.findViewById<android.view.View>(R.id.update_log_scroll)
                assertFalse(preview.isFocusable)
                assertTrue("Log preview must have a useful multi-line height",preview.layoutParams.height/activity.resources.displayMetrics.density>=190)
                // Synthetic availability fixture only; never click Download or contact GitHub.
                activity.findViewById<android.view.View>(R.id.update_download).isEnabled=true
                val fragment=(activity as MainActivity).supportFragmentManager.findFragmentById(R.id.fragment_holder) as AppUpdateFragment
                fragment.updateFocusOrder()
                activity.findViewById<android.view.View>(R.id.update_check).requestFocus()
            }
            device.pressDPadDown()
            assertNotNull("Exactly one Down must reach Download, not the log",
                device.wait(Until.findObject(By.res(context.packageName,"update_download").focused(true)),timeout))
            device.pressDPadUp()
            assertNotNull(device.wait(Until.findObject(By.res(context.packageName,"update_check").focused(true)),timeout))
            device.pressDPadDown();device.pressDPadDown()
            assertNotNull("Disabled Pause/Install are skipped; details remain reachable",
                device.wait(Until.findObject(By.res(context.packageName,"update_details").focused(true)),timeout))
            device.pressDPadCenter()
            assertEquals(fixture,requireNotNull(device.wait(Until.findObject(By.res("android","message")),timeout)).text)
            assertNotNull(device.wait(Until.findObject(By.res("android","button1").focused(true)),timeout))
            device.pressDPadCenter()
            assertTrue(device.wait(Until.gone(By.res("android","message")),timeout))
        }
        assertEquals(selected,DataStore.selectedProxy)
        assertEquals(state,DataStore.serviceState)
        val outside=File(context.cacheDir,"update-downloads/unverified.apk").apply { parentFile!!.mkdirs();writeText("fixture") }
        try {
            assertTrue(runCatching { androidx.core.content.FileProvider.getUriForFile(context,"${context.packageName}.update-files",outside) }.isFailure)
        } finally {outside.delete()}
    }

    @Test fun tvListsEveryGroupAndRemoteSelectionDoesNotStartVpn() {
        val originalGroup=DataStore.currentGroupId()
        val selected=DataStore.selectedProxy;val state=DataStore.serviceState
        val fixture=runBlocking { GroupManager.createGroup(io.nekohasekai.sagernet.database.ProxyGroup(name="Smoke row collection")) }
        try {
            startTv()
            // Real D-pad, not a touch shortcut or fabricated focus selection.
            val maxRows=runBlocking(kotlinx.coroutines.Dispatchers.IO) {SagerDatabase.groupDao.allGroups().size}+3
            for(i in 0 until maxRows) {
                if(device.hasObject(By.descStartsWith("Smoke row collection").focused(true)))break
                device.pressDPadDown();device.waitForIdle()
            }
            assertNotNull(device.wait(Until.findObject(By.descStartsWith("Smoke row collection").focused(true)),timeout))
            scenario!!.onActivity { activity ->
                val browse=(activity as MainActivityTv).supportFragmentManager.findFragmentById(R.id.tv_container)
                    as io.nekohasekai.sagernet.ui.tv.MainBrowseFragment
                val rows=browse.adapter
                val groupRows=(0 until rows.size()).map {rows[it] as androidx.leanback.widget.ListRow}
                    .filter {it.headerItem.id>=io.nekohasekai.sagernet.ui.tv.TvGroupRows.BASE}
                val ids=runBlocking(kotlinx.coroutines.Dispatchers.IO) {SagerDatabase.groupDao.allGroups().map {it.id}}
                val expected=ids.filter {it==DataStore.selectedGroup}+ids.filter {it!=DataStore.selectedGroup}
                assertEquals(expected,groupRows.map {it.headerItem.id-io.nekohasekai.sagernet.ui.tv.TvGroupRows.BASE})
                assertEquals(text(R.string.tv_all_functions),(rows[rows.size()-1] as androidx.leanback.widget.ListRow).headerItem.name)
                val operations=rows[rows.size()-2] as androidx.leanback.widget.ListRow
                assertEquals(4L,operations.headerItem.id)
                assertEquals(listOf(4L,5L,15L,9L,11L,6L,13L,14L),(0 until operations.adapter.size()).map {(operations.adapter[it] as io.nekohasekai.sagernet.ui.tv.TvAction).id})
            }
            device.pressDPadCenter()
            assertEquals(fixture.id,DataStore.selectedGroup)
            assertEquals(selected,DataStore.selectedProxy);assertEquals(state,DataStore.serviceState)
            device.pressDPadRight();device.pressDPadCenter();assertChooser()
            assertEquals(selected,DataStore.selectedProxy);assertEquals(state,DataStore.serviceState)
        } finally {
            scenario?.close();scenario=null
            runBlocking {GroupManager.deleteGroup(fixture.id)}
            DataStore.selectedGroup=originalGroup
        }
    }

    @Test fun extendedDiagnosisHasRealActionProgressCancelAndDoesNotSwitchVpn() {
        val oldGroup=DataStore.currentGroupId();val oldProfile=DataStore.selectedProxy;val state=DataStore.serviceState
        val group=runBlocking {GroupManager.createGroup(io.nekohasekai.sagernet.database.ProxyGroup(name="Extended diagnostic smoke"))}
        val bean=io.nekohasekai.sagernet.fmt.socks.SOCKSBean().apply {initializeDefaultValues();serverAddress="fixture.invalid";serverPort=10808;name="Extended fixture"}
        val entity=runBlocking {io.nekohasekai.sagernet.database.ProfileManager.createProfile(group.id,bean)}
        try {
            DataStore.selectedGroup=group.id;DataStore.selectedProxy=entity.id;startTv()
            scenario!!.onActivity {activity->
                val browse=(activity as MainActivityTv).supportFragmentManager.findFragmentById(R.id.tv_container)
                    as io.nekohasekai.sagernet.ui.tv.MainBrowseFragment
                val probe: suspend (android.content.Context,io.nekohasekai.sagernet.database.ProxyEntity,suspend (String)->Unit)->io.nekohasekai.sagernet.ui.diagnostics.ExtendedConnectionDiagnostics.Report = {_,_,progress->
                    progress("ENDPOINT_DNS_FIXTURE");kotlinx.coroutines.awaitCancellation()
                }
                browse.javaClass.getDeclaredField("extendedProbe").apply {isAccessible=true}.set(browse,probe)
            }
            device.pressDPadDown();device.pressDPadRight();device.pressDPadCenter()
            assertNotNull(device.wait(Until.findObject(By.res(context.packageName,"diagnostic_extended_button")),timeout))
            // The report closes safely by default; move focus to actual full-check button.
            val action=device.findObject(By.res(context.packageName,"diagnostic_extended_button"))
            action.click() // explicit action through installed APK; progress/cancel are remote-tested below
            assertNotNull(device.wait(Until.findObject(By.textContains("ENDPOINT_DNS_FIXTURE")),timeout))
            assertTrue(DataStore.runningTest)
            device.pressDPadCenter() // safe Cancel has initial dialog focus
            val until=android.os.SystemClock.elapsedRealtime()+timeout
            while(DataStore.runningTest&&android.os.SystemClock.elapsedRealtime()<until)Thread.sleep(100)
            assertFalse(DataStore.runningTest);assertEquals(entity.id,DataStore.selectedProxy);assertEquals(state,DataStore.serviceState)
        } finally {
            scenario?.close();scenario=null
            runBlocking {GroupManager.deleteGroup(group.id)};DataStore.selectedGroup=oldGroup;DataStore.selectedProxy=oldProfile
        }
    }

    @Test fun groupHeaderRanksUrlResultsWithoutSwitchingVpn() {
        val oldGroup=DataStore.currentGroupId();val selected=DataStore.selectedProxy;val state=DataStore.serviceState
        val group=runBlocking {GroupManager.createGroup(io.nekohasekai.sagernet.database.ProxyGroup(name="Quality smoke"))}
        val entries=runBlocking {(1..3).map {i->
            val bean=io.nekohasekai.sagernet.fmt.socks.SOCKSBean().apply {
                initializeDefaultValues();serverAddress="fixture.invalid";serverPort=10808;name="Quality $i"
            }
            io.nekohasekai.sagernet.database.ProfileManager.createProfile(group.id,bean)
        }}
        try {
            DataStore.selectedGroup=group.id;startTv()
            scenario!!.onActivity {activity->
                val browse=(activity as MainActivityTv).supportFragmentManager.findFragmentById(R.id.tv_container)
                    as io.nekohasekai.sagernet.ui.tv.MainBrowseFragment
                isolateQualityWorker()
                installRankingWorker(browse)
                val attempts=mutableMapOf<Long,Int>()
                val probe: suspend (io.nekohasekai.sagernet.database.ProxyEntity)->Int = {profile->
                    val n=attempts.getOrDefault(profile.id,0);attempts[profile.id]=n+1
                    when(profile.id) {
                        entries[0].id->throw java.net.SocketTimeoutException("fixture timeout")
                        entries[1].id->if(n==0)throw java.net.SocketTimeoutException("fixture timeout") else 5
                        else->100
                    }
                }
                val tcp: suspend (io.nekohasekai.sagernet.database.ProxyEntity)->Int = {0}
                browse.javaClass.getDeclaredField("qualityUrlProbe").apply {isAccessible=true}.set(browse,probe)
                browse.javaClass.getDeclaredField("qualityTcpProbe").apply {isAccessible=true}.set(browse,tcp)
                val actions=(browse.adapter[0] as androidx.leanback.widget.ListRow).adapter
                assertEquals(listOf(1L,2L,3L,17L,7L),(0 until actions.size()).map {(actions[it] as io.nekohasekai.sagernet.ui.tv.TvAction).id})
            }
            device.pressDPadDown();device.pressDPadDown()
            assertNotNull(device.wait(Until.findObject(By.descStartsWith("Quality smoke").focused(true)),timeout))
            scenario!!.onActivity {activity->
                val browse=(activity as MainActivityTv).supportFragmentManager.findFragmentById(R.id.tv_container)
                    as io.nekohasekai.sagernet.ui.tv.MainBrowseFragment
                installRankingWorker(browse)
            }
            DataStore.runningTest=false
            device.pressDPadCenter()
            val completed=device.wait(Until.findObject(By.text(text(R.string.tv_test_complete))),timeout)
            if(completed==null) {
                // Capture BEFORE finally closes ActivityScenario; a later Home screen
                // would hide whether this was a real crash or a guarded test failure.
                device.takeScreenshot(File("/sdcard/Download/TunXBoxSmoke/group_quality_before_cleanup.png"))
                device.dumpWindowHierarchy(File("/sdcard/Download/TunXBoxSmoke/group_quality_before_cleanup.xml"))
                val nodes=device.findObjects(By.textContains("NET_"))
                android.util.Log.i("TunXBoxSmoke","Group result absent; visible canonical codes: "+nodes.map {it.text})
            }
            assertNotNull("Completed group ranking must have an explicit result",completed)
            val sorted=runBlocking(kotlinx.coroutines.Dispatchers.IO){SagerDatabase.proxyDao.getByGroup(group.id)}
            assertEquals(listOf(entries[2].id,entries[1].id,entries[0].id),sorted.map {it.id})
            assertEquals(listOf(1,1,2),sorted.map {it.status})
            assertEquals(100,sorted[0].ping);assertEquals(5,sorted[1].ping)
            assertEquals(selected,DataStore.selectedProxy);assertEquals(state,DataStore.serviceState)
            device.pressBack();device.waitForIdle()
        } finally {
            scenario?.close();scenario=null
            runBlocking {GroupManager.deleteGroup(group.id)};DataStore.selectedGroup=oldGroup
        }
    }

    @Test fun sessionCountersAndUnsupportedReportCanBeDownloadedByQrWithoutVpn() {
        val oldGroup=DataStore.currentGroupId();val oldProfile=DataStore.selectedProxy;val state=DataStore.serviceState
        val group=runBlocking {GroupManager.createGroup(io.nekohasekai.sagernet.database.ProxyGroup(name="Diagnostic smoke"))}
        val bean=io.nekohasekai.sagernet.fmt.v2ray.VMessBean().apply {
            alterId=-1;initializeDefaultValues();type="xhttp";security="reality";serverAddress="secret-fixture.invalid"
            uuid="secret-fixture-uuid";path="/secret-fixture-token";name="Diagnostic XHTTP"
        }
        val entity=runBlocking {io.nekohasekai.sagernet.database.ProfileManager.createProfile(group.id,bean)}
        try {
            DataStore.selectedGroup=group.id;DataStore.selectedProxy=entity.id
            io.nekohasekai.sagernet.ui.diagnostics.ConnectionDiagnosticJournal.startup(entity,io.nekohasekai.sagernet.group.SubscriptionFailureCategory.UNSUPPORTED)
            startTv()
            // Wait for the real binder before delivering this synthetic statistics fixture.
            var ready=false
            val bindDeadline=android.os.SystemClock.elapsedRealtime()+timeout
            while(!ready&&android.os.SystemClock.elapsedRealtime()<bindDeadline) {
                scenario!!.onActivity {activity ->
                    val browse=(activity as MainActivityTv).supportFragmentManager.findFragmentById(R.id.tv_container) as io.nekohasekai.sagernet.ui.tv.MainBrowseFragment
                    ready=browse.javaClass.getDeclaredField("serviceReady").apply {isAccessible=true}.getBoolean(browse)
                }
                if(!ready)android.os.SystemClock.sleep(50)
            }
            assertTrue("Service binder must be ready",ready)
            // Synthetic callback verifies totals remain nonzero even when instantaneous rates are zero.
            scenario!!.onActivity {activity ->
                val browse=(activity as MainActivityTv).supportFragmentManager.findFragmentById(R.id.tv_container) as io.nekohasekai.sagernet.ui.tv.MainBrowseFragment
                browse.updateTraffic(io.nekohasekai.sagernet.aidl.SpeedDisplayData(txTotal=1234,rxTotal=7890))
            }
            device.pressDPadDown()
            val sent=requireNotNull(device.wait(Until.findObject(By.res(context.packageName,"connection_summary_sent")),timeout)).text
            assertFalse(sent.contains("—"));assertFalse(sent.contains("/s"))
            assertEquals("Diagnostic XHTTP",requireNotNull(device.findObject(By.res(context.packageName,"connection_summary_name"))).text)
            device.pressDPadRight();device.pressDPadCenter()
            val report=requireNotNull(device.wait(Until.findObject(By.res(context.packageName,"diagnostic_report_text")),timeout)).text
            assertTrue(report.contains("NET_UNSUPPORTED"));assertTrue(report.contains("xhttp"));assertTrue(report.contains("STARTUP"))
            assertTrue(report.contains(io.nekohasekai.sagernet.BuildConfig.VERSION_NAME))
            for(secret in listOf("secret-fixture.invalid","secret-fixture-uuid","secret-fixture-token"))assertFalse(report.contains(secret))
            assertNotNull(device.wait(Until.findObject(By.res("android","button1").focused(true)),timeout))
            requireNotNull(device.findObject(By.res("android","button3"))).click()
            val deadline=android.os.SystemClock.elapsedRealtime()+timeout
            var url: String?=null
            while(url==null&&android.os.SystemClock.elapsedRealtime()<deadline) {
                if(device.hasObject(By.text(text(R.string.diagnostic_share_network)))) {
                    device.findObject(By.res("android","text1"))?.takeIf {it.text.matches(Regex("[0-9.]+"))}?.click()
                }
                val text=device.findObject(By.res(context.packageName,"diagnostic_share_status"))?.text.orEmpty()
                url=Regex("http://[^\\s]+").find(text)?.value
                if(url==null)android.os.SystemClock.sleep(100)
            }
            val uri=java.net.URI(requireNotNull(url){"Explicit LAN report link must be available on emulator Wi-Fi"})
            assertNotNull(device.findObject(By.res(context.packageName,"diagnostic_qr")))
            val response=java.net.Socket(uri.host,uri.port).use {socket ->
                socket.soTimeout=3000
                socket.getOutputStream().write(("GET ${uri.path} HTTP/1.1\r\nHost: ${uri.host}:${uri.port}\r\n\r\n").toByteArray())
                socket.getInputStream().bufferedReader().readText()
            }
            assertTrue(response.startsWith("HTTP/1.1 200"));assertTrue(response.contains("NET_UNSUPPORTED"))
            assertFalse(response.contains("secret-fixture-uuid"));assertFalse(response.contains("secret-fixture-token"))
            device.pressBack()
            assertTrue(device.wait(Until.gone(By.res(context.packageName,"diagnostic_share_status")),timeout))
            assertTrue(runCatching {java.net.Socket(uri.host,uri.port).use {}}.isFailure)
            assertEquals(entity.id,DataStore.selectedProxy);assertEquals(state,DataStore.serviceState)
        } finally {
            scenario?.close();scenario=null;runBlocking {GroupManager.deleteGroup(group.id)}
            DataStore.selectedGroup=oldGroup;DataStore.selectedProxy=oldProfile
        }
    }

    @Test fun repeatedCheckShowsProgressAndRemoteCancellationNeverStopsVpn() {
        val oldCurrent=DataStore.currentProfile;val oldSelected=DataStore.selectedProxy;val oldState=DataStore.serviceState
        val entered=java.util.concurrent.CountDownLatch(1);val release=java.util.concurrent.CountDownLatch(1)
        val finished=java.util.concurrent.CountDownLatch(1)
        var calls=0
        var actualService: io.nekohasekai.sagernet.aidl.ISagerNetService?=null
        try {
            startTv()
            var ready=false
            val deadline=android.os.SystemClock.elapsedRealtime()+timeout
            while(!ready&&android.os.SystemClock.elapsedRealtime()<deadline) {
                scenario!!.onActivity {activity ->
                    val browse=(activity as MainActivityTv).supportFragmentManager.findFragmentById(R.id.tv_container) as io.nekohasekai.sagernet.ui.tv.MainBrowseFragment
                    ready=browse.javaClass.getDeclaredField("serviceReady").apply {isAccessible=true}.getBoolean(browse)
                }
                if(!ready)android.os.SystemClock.sleep(50)
            }
            assertTrue(ready)
            // Synthetic Binder stall: installed Android dialog/remote behaviour, NOT a live VPN test.
            DataStore.currentProfile=77
            scenario!!.onActivity {activity ->
                val browse=(activity as MainActivityTv).supportFragmentManager.findFragmentById(R.id.tv_container) as io.nekohasekai.sagernet.ui.tv.MainBrowseFragment
                browse.javaClass.getDeclaredField("phase").apply {isAccessible=true}.set(browse,io.nekohasekai.sagernet.ui.tv.TvVpnPhase.CONNECTED)
                val connection=browse.javaClass.getDeclaredField("connection").apply {isAccessible=true}.get(browse) as io.nekohasekai.sagernet.bg.SagerConnection
                actualService=connection.service
                connection.service=object: io.nekohasekai.sagernet.aidl.ISagerNetService.Stub() {
                    override fun getState()=io.nekohasekai.sagernet.bg.BaseService.State.Connected.ordinal
                    override fun getProfileName()="Synthetic progress"
                    override fun registerCallback(cb: io.nekohasekai.sagernet.aidl.ISagerNetServiceCallback,id: Int) {}
                    override fun unregisterCallback(cb: io.nekohasekai.sagernet.aidl.ISagerNetServiceCallback) {}
                    override fun urlTest()=1
                    override fun testConnectionReport(): String {
                        calls++;entered.countDown()
                        try {release.await(5,java.util.concurrent.TimeUnit.SECONDS)
                            return io.nekohasekai.sagernet.group.ConnectionTestResult(77,System.currentTimeMillis(),1).encode()
                        } finally {finished.countDown()}
                    }
                }
                browse.javaClass.getDeclaredMethod("testActiveConnection",Boolean::class.javaPrimitiveType).apply {isAccessible=true}.invoke(browse,false)
            }
            assertTrue(entered.await(2,java.util.concurrent.TimeUnit.SECONDS))
            val progress=requireNotNull(device.wait(Until.findObject(By.res(context.packageName,"diagnostic_check_progress_text")),timeout))
            assertTrue(progress.text.contains("8"))
            assertNotNull(device.wait(Until.findObject(By.res("android","button2").focused(true)),timeout))
            device.pressDPadCenter()
            assertTrue(device.wait(Until.gone(By.res(context.packageName,"diagnostic_check_progress_text")),2000))
            assertEquals(1,calls);assertEquals(oldSelected,DataStore.selectedProxy);assertEquals(oldState,DataStore.serviceState)
        } finally {
            release.countDown();finished.await(2,java.util.concurrent.TimeUnit.SECONDS)
            scenario?.onActivity {activity ->
                val browse=(activity as MainActivityTv).supportFragmentManager.findFragmentById(R.id.tv_container) as? io.nekohasekai.sagernet.ui.tv.MainBrowseFragment
                if(browse!=null)(browse.javaClass.getDeclaredField("connection").apply {isAccessible=true}.get(browse) as io.nekohasekai.sagernet.bg.SagerConnection).service=actualService
            }
            scenario?.close();scenario=null;DataStore.currentProfile=oldCurrent
        }
    }

    @Before fun prepare() {
        device.wakeUp()
        device.setOrientationNatural()
        device.executeShellCommand("pm grant ${context.packageName} android.permission.POST_NOTIFICATIONS")
        TvUiPreferences.phoneMode = false
        // Group ranking uses one process-wide worker. A previous screen can cancel its
        // coroutine while a blocking probe still holds that worker; isolate every test.
        isolateQualityWorker()
        DataStore.runningTest = false
        runBlocking { GroupManager.clearGroup(DataStore.currentGroupId()) }
    }
    private fun isolateQualityWorker() {
        val fragment = io.nekohasekai.sagernet.ui.tv.MainBrowseFragment
        val previous = fragment.qualityWorker
        fragment.qualityWorker = io.nekohasekai.sagernet.group.BoundedConnectionCheck()
        if (previous !== fragment.qualityWorker) previous.close()
    }
    private fun installRankingWorker(browse: io.nekohasekai.sagernet.ui.tv.MainBrowseFragment) {
        // Instance override is read by the click that is about to run. Replacing only the
        // process-wide worker is not enough: a cancelled probe can still occupy it.
        browse.javaClass.getDeclaredField("qualityWorkerOverride").apply { isAccessible = true }
            .set(browse, io.nekohasekai.sagernet.group.BoundedConnectionCheck())
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

    @Test fun aboutShowsBuildIdentitySupportAndFullLicense() {
        TvUiPreferences.phoneMode = true
        scenario = ActivityScenario.launch<MainActivity>(Intent(context, MainActivity::class.java)
            .putExtra("tv_tools", true).putExtra("tv_destination", R.id.nav_about))
        val scroll = UiScrollable(UiSelector().resourceId(context.packageName + ":id/mal_recyclerview"))
            .setAsVerticalList().setMaxSearchSwipes(30)
        // Centered gestures avoid the system bottom edge and card-end touch regions.
        // Keep all actual reachability/click/focus/license-content assertions below.
        scroll.setSwipeDeadZonePercentage(0.25)
        assertTrue(scroll.scrollIntoView(UiSelector().text(text(R.string.about_build_details))))
        assertFalse("Global traffic panel must not cover About", device.hasObject(By.res(context.packageName, "stats")))
        assertFalse("Global service button must not cover About", device.hasObject(By.res(context.packageName, "fab")))
        visibleText(text(R.string.about_build_details)).click()
        val message = requireNotNull(device.wait(Until.findObject(By.res("android", "message")), timeout)).text
        assertTrue(message.contains(io.nekohasekai.sagernet.BuildConfig.VERSION_NAME))
        assertTrue(message.contains(io.nekohasekai.sagernet.BuildConfig.BUILD_COMMIT))
        assertTrue(message.contains("Android versionCode"))
        assertTrue(message.contains("Installed signing certificate SHA-256"))
        assertTrue(message.contains("CI build number / run ID"))
        assertNotNull(device.findObject(By.res("android", "button3")))
        device.findObject(By.res("android", "button1")).click()
        assertTrue(device.wait(Until.gone(By.res("android", "message")), timeout))
        assertTrue("Public Telegram contact must be reachable", scroll.scrollIntoView(UiSelector().text(text(R.string.project_telegram_tunxbox))))
        assertTrue(device.hasObject(By.text("@LEONI_YA")))
        assertTrue(scroll.scrollIntoView(UiSelector().text(text(R.string.project_upstream_support))))
        assertTrue(text(R.string.project_upstream_support).contains("TunXBox"))
        visibleText(text(R.string.project_upstream_support)).click()
        assertTrue(requireNotNull(device.wait(Until.findObject(By.res("android", "message")), timeout)).text.contains("GPL"))
        device.findObject(By.res("android", "button2")).click()
        assertTrue(device.wait(Until.gone(By.res("android", "message")), timeout))
        device.waitForIdle()
        assertTrue("Full license action must be reachable", scroll.scrollIntoView(UiSelector().text(text(R.string.about_license_full))))
        var license: UiObject2? = visibleText(text(R.string.about_license_full))
        repeat(6) { if (license?.isClickable != true) license = license?.parent }
        val licenseAction = requireNotNull(license) { "Full license must have a clickable action row" }
        assertTrue("Full license action remains remote-focusable", licenseAction.isFocusable)
        licenseAction.click()
        val full = requireNotNull(device.wait(Until.findObject(By.res("android", "message")), timeout)).text
        assertTrue(full.contains("Copyright (C) 2021 by nekohasekai"))
        assertTrue(full.contains("GNU GENERAL PUBLIC LICENSE"))
        assertTrue(full.contains("TERMS AND CONDITIONS"))
        device.findObject(By.res("android", "button1")).click()
        assertTrue(device.wait(Until.gone(By.res("android", "message")), timeout))
        val selected = DataStore.selectedProxy
        val state = DataStore.serviceState
        scenario?.onActivity { (it as MainActivity).displayFragmentWithId(R.id.nav_configuration) }
        assertTrue("Traffic panel returns on profiles", device.wait(Until.hasObject(By.res(context.packageName, "stats")), timeout))
        assertEquals("About navigation must not select a different profile", selected, DataStore.selectedProxy)
        assertEquals("About navigation must not change VPN service state", state, DataStore.serviceState)
    }

    @Test fun nativeCoreLoadsOnActualExpectedPageSize() {
        val expected = InstrumentationRegistry.getArguments().getString("expectedPageSize") ?: error("CI must specify page size")
        assertEquals(expected, device.executeShellCommand("getconf PAGESIZE").trim())
        assertTrue("Actual JNI core must load", Libcore.versionBox().isNotBlank())
        verifySavedDiagnosticsInBothModes()
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
        val binder = io.nekohasekai.sagernet.bg.BaseService.Binder()
        assertEquals(io.nekohasekai.sagernet.group.SubscriptionFailureCategory.CORE,
            io.nekohasekai.sagernet.group.ConnectionTestResult.decode(binder.testConnectionReport())!!.category)
        binder.close()
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
            // Selected group first, other group rows, then Operations, then Capabilities.
            val rowBudget=SagerDatabase.groupDao.allGroups().size+5
            fun operationsSelected(): Boolean {
                var selected=false
                scenario!!.onActivity {activity ->
                    val browse=(activity as MainActivityTv).supportFragmentManager.findFragmentById(R.id.tv_container) as io.nekohasekai.sagernet.ui.tv.MainBrowseFragment
                    selected=browse.javaClass.getDeclaredField("focusedRow").apply {isAccessible=true}.getLong(browse)==4L
                }
                return selected
            }
            repeat(rowBudget * 2) {
                if(!operationsSelected()) {
                    val previous=device.findObject(By.focused(true))?.contentDescription
                    android.util.Log.i("TunXBoxSmoke","Operations navigation focused: $previous")
                    device.pressDPadDown()
                    if(previous!=null)device.wait(Until.gone(By.desc(previous).focused(true)),1000)
                    device.waitForIdle()
                }
            }
            // Vertical focus follows spatial column, not necessarily card zero in a row.
            repeat(16) {
                if(!device.hasObject(By.descStartsWith(text(R.string.tv_actions)).focused(true))) {
                    val previous=device.findObject(By.focused(true))?.contentDescription
                    device.pressDPadLeft()
                    if(previous!=null)device.wait(Until.gone(By.desc(previous).focused(true)),1000)
                    device.waitForIdle()
                }
            }
            if(!device.hasObject(By.descStartsWith(text(R.string.tv_actions)).focused(true))) {
                val hierarchy=File(context.getExternalFilesDir(null),"operations_focus_failure.xml")
                device.dumpWindowHierarchy(hierarchy)
                device.executeShellCommand("mkdir -p /sdcard/Download/TunXBoxSmoke")
                device.executeShellCommand("cp ${hierarchy.absolutePath} /sdcard/Download/TunXBoxSmoke/operations_focus_failure.xml")
                device.executeShellCommand("screencap -p /sdcard/Download/TunXBoxSmoke/operations_focus_failure.png")
            }
            assertNotNull("Operations must be reachable",device.findObject(By.descStartsWith(text(R.string.tv_actions)).focused(true)))
            val title = text(R.string.subscription_attempt_title)
            val buttonBudget=8
            repeat(buttonBudget * 2) {
                if (!device.hasObject(By.descStartsWith(title).focused(true))) {
                    val previous=device.findObject(By.focused(true))?.contentDescription
                    android.util.Log.i("TunXBoxSmoke", "Report navigation focused: $previous")
                    device.pressDPadRight()
                    // A long Leanback row can still be scrolling after input delivery.
                    if(previous!=null)device.wait(Until.gone(By.desc(previous).focused(true)),1000)
                    device.waitForIdle()
                }
            }
            if(!device.hasObject(By.descStartsWith(title).focused(true))) {
                device.executeShellCommand("mkdir -p /sdcard/Download/TunXBoxSmoke")
                val hierarchy=File(context.getExternalFilesDir(null),"saved_report_focus_failure.xml")
                device.dumpWindowHierarchy(hierarchy)
                device.executeShellCommand("cp ${hierarchy.absolutePath} /sdcard/Download/TunXBoxSmoke/saved_report_focus_failure.xml")
                device.executeShellCommand("screencap -p /sdcard/Download/TunXBoxSmoke/saved_report_focus_failure.png")
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
        device.waitForIdle()
        val message = requireNotNull(device.wait(Until.findObject(By.res("android", "message")), timeout))
        assertTrue(message.text.contains("SUB_TLS_TIME"))
        assertTrue(message.text.contains("APK:"))
        assertFalse(message.text.contains("fixture.invalid"))
        assertFalse(message.text.contains("private-token-not-for-report"))
        // Capture while the dialog is open: finally closes activities even after an assertion.
        device.executeShellCommand("mkdir -p /sdcard/Download/TunXBoxSmoke")
        device.executeShellCommand("screencap -p /sdcard/Download/TunXBoxSmoke/$screenshot.png")
        val hierarchy = File(context.getExternalFilesDir(null), "smoke-artifacts/$screenshot.xml").apply { parentFile?.mkdirs() }
        device.dumpWindowHierarchy(hierarchy)
        // UiAutomation shell calls are not Bash scripts: avoid &&/echo chaining.
        device.executeShellCommand("cp ${hierarchy.absolutePath} /sdcard/Download/TunXBoxSmoke/$screenshot.xml")
        val copied = device.executeShellCommand("cat /sdcard/Download/TunXBoxSmoke/$screenshot.xml")
        assertEquals("Synthetic dialog hierarchy must be exported completely", hierarchy.readText().trim(), copied.trim())
        val share = requireNotNull(device.wait(Until.findObject(By.res("android", "button3")), timeout))
        assertTrue("Explicit Share action must be enabled", share.isEnabled)
        // Android/Material may transform the label to all caps without changing its meaning.
        assertTrue(share.text.equals(text(R.string.subscription_attempt_share), ignoreCase = true))
        assertFalse("Opening diagnostic details must not auto-share", device.hasObject(By.pkg("com.android.intentresolver")))
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
    @Test fun launcherReleaseNotesAndAboutWorkOfflineAndRestoreRemoteFocus() {
        val selected=DataStore.selectedProxy;val state=DataStore.serviceState
        scenario=ActivityScenario.launch<ModeSelectionActivity>(Intent(context,ModeSelectionActivity::class.java))
        val version=requireNotNull(device.wait(Until.findObject(By.res(context.packageName,"mode_build_info")),timeout)).text
        assertEquals(context.getString(R.string.mode_release_title,io.nekohasekai.sagernet.BuildConfig.VERSION_NAME),version)
        assertNotNull(device.wait(Until.findObject(By.res(context.packageName,"mode_choose_tv").focused(true)),timeout))
        device.pressDPadUp()
        assertNotNull(device.wait(Until.findObject(By.res(context.packageName,"mode_release").focused(true)),timeout))
        device.pressDPadCenter()
        val build=requireNotNull(device.wait(Until.findObject(By.res(context.packageName,"release_build_info")),timeout)).text
        assertTrue(build.contains(io.nekohasekai.sagernet.BuildConfig.VERSION_NAME))
        assertTrue(build.contains(io.nekohasekai.sagernet.BuildConfig.BUILD_COMMIT.take(8)))
        assertNotNull(device.wait(Until.findObject(By.res(context.packageName,"release_back").focused(true)),timeout))
        fun reach(id: String,down: Boolean) {
            // Explicit section routing must reach every control in at most five presses.
            for(i in 0 until 5) {
                if(device.hasObject(By.res(context.packageName,id).focused(true)))break
                if(down)device.pressDPadDown() else device.pressDPadUp()
                device.waitForIdle()
            }
            assertNotNull("Unreachable release section: $id",device.wait(Until.findObject(By.res(context.packageName,id).focused(true)),timeout))
        }
        reach("release_about",true)
        reach("release_changes",true)
        device.pressDPadRight();device.waitForIdle()
        assertNotNull("Reading long notes must retain the section focus",device.findObject(By.res(context.packageName,"release_changes").focused(true)))
        device.pressDPadLeft();device.waitForIdle()
        assertNotNull(device.findObject(By.res(context.packageName,"release_changes").focused(true)))
        reach("release_checks",true);reach("release_limits",true)
        reach("release_about",false)
        device.pressDPadCenter()
        assertNotNull(device.wait(Until.findObject(By.res(context.packageName,"mal_recyclerview")),timeout))
        val scroll=UiScrollable(UiSelector().resourceId(context.packageName+":id/mal_recyclerview")).setAsVerticalList().setMaxSearchSwipes(30)
        scroll.setSwipeDeadZonePercentage(0.25)
        assertTrue(scroll.scrollIntoView(UiSelector().text(text(R.string.about_build_details))))
        visibleText(text(R.string.about_build_details)).click()
        val message=requireNotNull(device.wait(Until.findObject(By.res("android","message")),timeout)).text
        assertTrue(message.contains(io.nekohasekai.sagernet.BuildConfig.VERSION_NAME))
        device.pressBack();device.pressBack()
        assertNotNull(device.wait(Until.findObject(By.res(context.packageName,"release_about").focused(true)),timeout))
        device.pressBack()
        assertNotNull(device.wait(Until.findObject(By.res(context.packageName,"mode_release").focused(true)),timeout))
        assertEquals(selected,DataStore.selectedProxy);assertEquals(state,DataStore.serviceState)
        assertFalse(TvUiPreferences.phoneMode)
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

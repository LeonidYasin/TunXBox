package io.nekohasekai.sagernet.ui

import android.Manifest.permission.POST_NOTIFICATIONS
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.RemoteException
import android.view.KeyEvent
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.drawerlayout.widget.DrawerLayout
import java.lang.ref.WeakReference
import io.nekohasekai.sagernet.ui.tv.TvInteractionPolicy
import io.nekohasekai.sagernet.ui.tv.TvVpnPhase
import io.nekohasekai.sagernet.ui.tv.TvVpnCommand
import io.nekohasekai.sagernet.database.ProxyEntity
import androidx.activity.addCallback
import androidx.annotation.IdRes
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceDataStore
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import io.nekohasekai.sagernet.BuildConfig
import io.nekohasekai.sagernet.GroupType
import io.nekohasekai.sagernet.Key
import io.nekohasekai.sagernet.R
import io.nekohasekai.sagernet.SagerNet
import io.nekohasekai.sagernet.aidl.ISagerNetService
import io.nekohasekai.sagernet.aidl.SpeedDisplayData
import io.nekohasekai.sagernet.aidl.TrafficData
import io.nekohasekai.sagernet.bg.BaseService
import io.nekohasekai.sagernet.bg.SagerConnection
import io.nekohasekai.sagernet.database.DataStore
import io.nekohasekai.sagernet.database.GroupManager
import io.nekohasekai.sagernet.database.ProfileManager
import io.nekohasekai.sagernet.database.ProxyGroup
import io.nekohasekai.sagernet.database.SubscriptionBean
import io.nekohasekai.sagernet.database.preference.OnPreferenceDataStoreChangeListener
import io.nekohasekai.sagernet.databinding.LayoutMainBinding
import io.nekohasekai.sagernet.fmt.AbstractBean
import io.nekohasekai.sagernet.fmt.KryoConverters
import io.nekohasekai.sagernet.fmt.PluginEntry
import io.nekohasekai.sagernet.group.GroupInterfaceAdapter
import io.nekohasekai.sagernet.group.GroupUpdater
import io.nekohasekai.sagernet.ktx.alert
import io.nekohasekai.sagernet.ktx.isPlay
import io.nekohasekai.sagernet.ktx.isPreview
import io.nekohasekai.sagernet.ktx.launchCustomTab
import io.nekohasekai.sagernet.ktx.onMainDispatcher
import io.nekohasekai.sagernet.ktx.parseProxies
import io.nekohasekai.sagernet.ktx.readableMessage
import io.nekohasekai.sagernet.ktx.runOnDefaultDispatcher
import moe.matsuri.nb4a.utils.Util

class MainActivity : ThemedActivity(),
    SagerConnection.Callback,
    OnPreferenceDataStoreChangeListener,
    NavigationView.OnNavigationItemSelectedListener {

    lateinit var binding: LayoutMainBinding
    lateinit var navigation: NavigationView
    private var contentFocus: WeakReference<View>? = null
    private var menuKeyHandled = false
    private var lastRemoteCommand = -750L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // TV Mode is DEFAULT for all devices
        // Only use Phone Mode if user explicitly requested it
        val forcePhoneMode = intent.getBooleanExtra("force_phone_mode", false)
        val preferPhoneMode = io.nekohasekai.sagernet.ui.tv.TvUiPreferences.phoneMode
        
        // Deep links (sn://subscription, clash://install-config, ss://, vmess://, trojan://, etc.)
        // must be processed in phone mode because the TV UI doesn't have the subscription/profile
        // import dialogs. If we redirect to TV mode, the intent data is lost and the import fails.
        // The upstream onNewIntent() handler processes these links via importSubscription/importProfile.
        val isDeepLink = intent?.action == Intent.ACTION_VIEW && intent?.data != null
        
        if (!forcePhoneMode && !preferPhoneMode && !isDeepLink && !intent.getBooleanExtra("tv_tools", false)) {
            // Redirect to TV UI using explicit component name (no implicit intent)
            val tvIntent = android.content.Intent().apply {
                component = android.content.ComponentName(this@MainActivity, MainActivityTv::class.java)
                addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP or android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(tvIntent)
            finish()
            return
        }
        
        // Phone Mode: continue with normal mobile UI initialization
        if (forcePhoneMode && !isDeepLink && !intent.getBooleanExtra("tv_tools", false)) {
            io.nekohasekai.sagernet.ui.tv.TvUiPreferences.phoneMode = true
        }
        
        binding = LayoutMainBinding.inflate(layoutInflater)
        binding.fab.initProgress(binding.fabProgress)
        if (themeResId !in intArrayOf(R.style.Theme_SagerNet_Black)) {
            navigation = binding.navView
            binding.drawerLayout.removeView(binding.navViewBlack)
        } else {
            navigation = binding.navViewBlack
            binding.drawerLayout.removeView(binding.navView)
        }
        navigation.setNavigationItemSelectedListener(this)
        binding.drawerLayout.addDrawerListener(object : DrawerLayout.SimpleDrawerListener() {
            override fun onDrawerClosed(drawerView: View) {
                if (!remoteFocusEnabled) return
                binding.root.post {
                    val previous = contentFocus?.get()
                    if (previous?.isAttachedToWindow == true && previous.isShown && previous.isEnabled) previous.requestFocus()
                    else if (findViewById<View>(R.id.configuration_list)?.requestFocus() != true) binding.fab.requestFocus()
                }
            }
        })

        if (savedInstanceState == null) {
            val destination = intent.getIntExtra("tv_destination", R.id.nav_configuration)
            val allowed = io.nekohasekai.sagernet.ui.tv.TvFunctionCatalog.entries(this, DataStore.enableClashAPI, isPlay).map { it.id }.toSet()
            if (!displayFragmentWithId(if (destination in allowed) destination else R.id.nav_configuration)) displayFragmentWithId(R.id.nav_configuration)
        }
        onBackPressedDispatcher.addCallback {
            if (binding.drawerLayout.isOpen) {
                binding.drawerLayout.closeDrawers()
            } else if (intent.getBooleanExtra("tv_tools", false)) {
                finish()
            } else if (supportFragmentManager.findFragmentById(R.id.fragment_holder) is ConfigurationFragment) {
                if (intent.getBooleanExtra("tv_tools", false) || intent?.action == Intent.ACTION_VIEW && !io.nekohasekai.sagernet.ui.tv.TvUiPreferences.phoneMode) finish()
                else moveTaskToBack(true)
            } else {
                displayFragmentWithId(R.id.nav_configuration)
            }
        }

        binding.fab.setOnClickListener { toggleService() }
        binding.stats.setOnClickListener { if (DataStore.serviceState.connected) binding.stats.testConnection() }

        setContentView(binding.root)
        changeState(BaseService.State.Idle)
        connection.connect(this, this)
        DataStore.configurationStore.registerChangeListener(this)
        GroupManager.userInterface = GroupInterfaceAdapter(this)

        if (intent?.action == Intent.ACTION_VIEW) {
            onNewIntent(intent)
        }

        refreshNavMenu(DataStore.enableClashAPI)

        // sdk 33 notification
        if (Build.VERSION.SDK_INT >= 33) {
            val checkPermission =
                ContextCompat.checkSelfPermission(this@MainActivity, POST_NOTIFICATIONS)
            if (checkPermission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this@MainActivity, arrayOf(POST_NOTIFICATIONS), 0
                )
            }
        }

        if (savedInstanceState == null && isPreview && !intent.getBooleanExtra("tv_tools", false)) {
            MaterialAlertDialogBuilder(this)
                .setTitle(BuildConfig.PRE_VERSION_NAME)
                .setMessage(R.string.preview_version_hint)
                .setPositiveButton(android.R.string.ok, null)
                .show()
        }
    }

    fun refreshNavMenu(clashApi: Boolean) {
        if (::navigation.isInitialized) {
            navigation.menu.findItem(R.id.nav_traffic)?.isVisible = clashApi
            navigation.menu.findItem(R.id.nav_tuiguang)?.isVisible = !isPlay
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (intent.getBooleanExtra("tv_tools", false)) {
            val allowed = io.nekohasekai.sagernet.ui.tv.TvFunctionCatalog.entries(this, DataStore.enableClashAPI, isPlay).map { it.id }.toSet()
            val destination = intent.getIntExtra("tv_destination", R.id.nav_configuration)
            if (!displayFragmentWithId(if (destination in allowed) destination else R.id.nav_configuration)) displayFragmentWithId(R.id.nav_configuration)
            return
        }

        val uri = intent.data ?: return

        runOnDefaultDispatcher {
            if (uri.scheme == "sn" && uri.host == "subscription" || uri.scheme == "clash") {
                importSubscription(uri)
            } else {
                importProfile(uri)
            }
        }
    }

    fun connectionTestReport(): io.nekohasekai.sagernet.group.ConnectionTestResult {
        val service = connection.service
        if (!DataStore.serviceState.connected || service == null) return io.nekohasekai.sagernet.group.ConnectionTestResult(
            DataStore.currentProfile.coerceAtLeast(0), System.currentTimeMillis(), -1, io.nekohasekai.sagernet.group.SubscriptionFailureCategory.CORE)
        return io.nekohasekai.sagernet.group.ConnectionTestResult.decode(service.testConnectionReport())
            ?: io.nekohasekai.sagernet.group.ConnectionTestResult(DataStore.currentProfile.coerceAtLeast(0), System.currentTimeMillis(), -1,
                io.nekohasekai.sagernet.group.SubscriptionFailureCategory.UNKNOWN)
    }

    fun urlTest(): Int {
        if (!DataStore.serviceState.connected || connection.service == null) {
            error("not started")
        }
        return connection.service!!.urlTest()
    }

    suspend fun importSubscription(uri: Uri) {
        val group: ProxyGroup

        val url = uri.getQueryParameter("url")
        if (!url.isNullOrBlank()) {
            group = ProxyGroup(type = GroupType.SUBSCRIPTION)
            val subscription = SubscriptionBean()
            group.subscription = subscription

            // cleartext format
            subscription.link = url
            group.name = uri.getQueryParameter("name")
        } else {
            val data = uri.encodedQuery.takeIf { !it.isNullOrBlank() } ?: return
            try {
                group = KryoConverters.deserialize(
                    ProxyGroup().apply { export = true }, Util.zlibDecompress(Util.b64Decode(data))
                ).apply {
                    export = false
                }
            } catch (e: Exception) {
                onMainDispatcher {
                    alert(e.readableMessage).show()
                }
                return
            }
        }

        val name = group.name.takeIf { !it.isNullOrBlank() } ?: group.subscription?.link
        ?: group.subscription?.token
        if (name.isNullOrBlank()) return

        group.name = group.name.takeIf { !it.isNullOrBlank() }
            ?: ("Subscription #" + System.currentTimeMillis())

        onMainDispatcher {

            displayFragmentWithId(R.id.nav_group)

            MaterialAlertDialogBuilder(this@MainActivity).setTitle(R.string.subscription_import)
                .setMessage(getString(R.string.subscription_import_message, name))
                .setPositiveButton(R.string.yes) { _, _ ->
                    runOnDefaultDispatcher {
                        finishImportSubscription(group)
                    }
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()

        }

    }

    private suspend fun finishImportSubscription(subscription: ProxyGroup) {
        GroupManager.createGroup(subscription)
        GroupUpdater.startUpdate(subscription, true)
    }

    suspend fun importProfile(uri: Uri) {
        val profile = try {
            parseProxies(uri.toString()).getOrNull(0) ?: error(getString(R.string.no_proxies_found))
        } catch (e: Exception) {
            onMainDispatcher {
                alert(e.readableMessage).show()
            }
            return
        }

        onMainDispatcher {
            MaterialAlertDialogBuilder(this@MainActivity).setTitle(R.string.profile_import)
                .setMessage(getString(R.string.profile_import_message, profile.displayName()))
                .setPositiveButton(R.string.yes) { _, _ ->
                    runOnDefaultDispatcher {
                        finishImportProfile(profile)
                    }
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
        }

    }

    private suspend fun finishImportProfile(profile: AbstractBean) {
        val targetId = DataStore.selectedGroupForImport()

        ProfileManager.createProfile(targetId, profile)
        DataStore.selectedGroup = targetId

        onMainDispatcher {
            displayFragmentWithId(R.id.nav_configuration)

            snackbar(resources.getQuantityString(R.plurals.added, 1, 1)).show()
        }
    }

    override fun missingPlugin(profileName: String, pluginName: String) {
        val pluginEntity = PluginEntry.find(pluginName)

        // unknown exe or neko plugin
        if (pluginEntity == null) {
            snackbar(getString(R.string.plugin_unknown, pluginName)).show()
            return
        }

        // official exe

        MaterialAlertDialogBuilder(this).setTitle(R.string.missing_plugin)
            .setMessage(
                getString(
                    R.string.profile_requiring_plugin, profileName, pluginEntity.displayName
                )
            )
            .setPositiveButton(R.string.action_download) { _, _ ->
                showDownloadDialog(pluginEntity)
            }
            .setNeutralButton(android.R.string.cancel, null)
            .setNeutralButton(R.string.action_learn_more) { _, _ ->
                launchCustomTab("https://matsuridayo.github.io/nb4a-plugin/")
            }
            .show()
    }

    private fun showDownloadDialog(pluginEntry: PluginEntry) {
        var index = 0
        var playIndex = -1
        var fdroidIndex = -1

        val items = mutableListOf<String>()
        if (pluginEntry.downloadSource.playStore) {
            items.add(getString(R.string.install_from_play_store))
            playIndex = index++
        }
        if (pluginEntry.downloadSource.fdroid) {
            items.add(getString(R.string.install_from_fdroid))
            fdroidIndex = index++
        }

        items.add(getString(R.string.download))
        val downloadIndex = index

        MaterialAlertDialogBuilder(this).setTitle(pluginEntry.name)
            .setItems(items.toTypedArray()) { _, which ->
                when (which) {
                    playIndex -> launchCustomTab("https://play.google.com/store/apps/details?id=${pluginEntry.packageName}")
                    fdroidIndex -> launchCustomTab("https://f-droid.org/packages/${pluginEntry.packageName}/")
                    downloadIndex -> launchCustomTab(pluginEntry.downloadSource.downloadLink)
                }
            }
            .show()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        if (item.isChecked) binding.drawerLayout.closeDrawers() else {
            when (item.itemId) {
                R.id.nav_restart_app, R.id.nav_close_app -> {
                    binding.drawerLayout.closeDrawers()
                    AppLifecycleActions.confirm(this, item.itemId == R.id.nav_restart_app)
                    return true
                }
                R.id.nav_switch_tv_mode -> {
                    binding.drawerLayout.closeDrawers()
                    // Clear phone-mode override so next launch goes to TV UI
                    io.nekohasekai.sagernet.ui.tv.TvUiPreferences.phoneMode = false
                    val tvIntent = android.content.Intent(this, MainActivityTv::class.java).apply {
                        addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP or android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    startActivity(tvIntent)
                    finish()
                    return true
                }
                else -> return displayFragmentWithId(item.itemId)
            }
        }
        return true
    }


    @SuppressLint("CommitTransaction")
    fun displayFragment(fragment: ToolbarFragment) {
        // BottomAppBar translation is not a reliable hide before its first layout.
        // This creation tool must not have an overlapping global VPN control panel.
        binding.stats.visibility = if (fragment is io.nekohasekai.sagernet.ui.lan.LanDiscoveryFragment) View.GONE else View.VISIBLE
        if (fragment is ConfigurationFragment || (DataStore.showBottomBar && fragment !is io.nekohasekai.sagernet.ui.lan.LanDiscoveryFragment)) {
            binding.stats.allowShow = true
            binding.fab.show()
        } else {
            binding.stats.allowShow = false
            binding.stats.performHide()
            binding.fab.hide()
        }
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_holder, fragment)
            .commitAllowingStateLoss()
        binding.drawerLayout.closeDrawers()
    }

    fun displayFragmentWithId(@IdRes id: Int): Boolean {
        when (id) {
            R.id.nav_configuration -> {
                displayFragment(ConfigurationFragment())
            }

            R.id.nav_group -> displayFragment(GroupFragment())
            R.id.nav_route -> displayFragment(RouteFragment())
            R.id.nav_settings -> displayFragment(SettingsFragment())
            R.id.nav_traffic -> displayFragment(WebviewFragment())
            R.id.nav_lan_discovery -> {
                val quick = intent.getBooleanExtra("gateway_quick", false)
                intent.removeExtra("gateway_quick")
                displayFragment(io.nekohasekai.sagernet.ui.lan.LanDiscoveryFragment().apply {
                    arguments = android.os.Bundle().apply { putBoolean("gateway_quick", quick) }
                })
            }
            R.id.nav_tools -> displayFragment(ToolsFragment())
            R.id.nav_logcat -> displayFragment(LogcatFragment())
            R.id.nav_faq -> {
                launchCustomTab(ProjectLinks.DOCUMENTATION)
                return false
            }

            R.id.nav_about -> displayFragment(AboutFragment())
            R.id.nav_tuiguang -> displayFragment(PromotionsFragment())

            else -> return false
        }
        navigation.menu.findItem(id).isChecked = true
        return true
    }

    private fun changeState(
        state: BaseService.State,
        msg: String? = null,
        animate: Boolean = false,
    ) {
        DataStore.serviceState = state
        // Availability of edit/delete buttons follows the active connection, not stale binds.
        runOnDefaultDispatcher {
            ProfileManager.postUpdate(DataStore.currentProfile, true)
            if (DataStore.selectedProxy != DataStore.currentProfile) ProfileManager.postUpdate(DataStore.selectedProxy, true)
        }

        binding.fab.changeState(state, DataStore.serviceState, animate)
        binding.stats.changeState(state)
        if (msg != null) snackbar(getString(R.string.vpn_error, msg)).show()
    }

    override fun snackbarInternal(text: CharSequence): Snackbar {
        return Snackbar.make(binding.coordinator, text, Snackbar.LENGTH_LONG).apply {
            if (binding.fab.isShown) {
                anchorView = binding.fab
            }
            // TODO
        }
    }

    override fun stateChanged(state: BaseService.State, profileName: String?, msg: String?) {
        changeState(state, msg, true)
    }

    val connection = SagerConnection(SagerConnection.CONNECTION_ID_MAIN_ACTIVITY_FOREGROUND, true)
    override fun onServiceConnected(service: ISagerNetService) = changeState(
        try {
            BaseService.State.values()[service.state]
        } catch (_: RemoteException) {
            BaseService.State.Idle
        }
    )

    override fun onServiceDisconnected() = changeState(BaseService.State.Idle)
    override fun onBinderDied() {
        connection.disconnect(this)
        connection.connect(this, this)
    }

    private val connect = registerForActivityResult(VpnRequestActivity.StartService()) {
        if (it) snackbar(R.string.vpn_permission_denied).show()
    }

    // may NOT called when app is in background
    // ONLY do UI update here, write DB in bg process
    override fun cbSpeedUpdate(stats: SpeedDisplayData) {
        binding.stats.updateSpeed(stats.txRateProxy, stats.rxRateProxy)
    }

    override fun cbTrafficUpdate(data: TrafficData) {
        runOnDefaultDispatcher {
            ProfileManager.postUpdate(data)
        }
    }

    override fun cbSelectorUpdate(id: Long) {
        val old = DataStore.selectedProxy
        DataStore.selectedProxy = id
        DataStore.currentProfile = id
        runOnDefaultDispatcher {
            ProfileManager.postUpdate(old, true)
            ProfileManager.postUpdate(id, true)
        }
    }

    override fun onPreferenceDataStoreChanged(store: PreferenceDataStore, key: String) {
        when (key) {
            Key.SERVICE_MODE -> onBinderDied()
            Key.PROXY_APPS, Key.BYPASS_MODE, Key.INDIVIDUAL -> {
                if (DataStore.serviceState.canStop) {
                    snackbar(getString(R.string.need_reload)).setAction(R.string.apply) {
                        SagerNet.reloadService()
                    }.show()
                }
            }
        }
    }

    override fun onStart() {
        connection.updateConnectionId(SagerConnection.CONNECTION_ID_MAIN_ACTIVITY_FOREGROUND)
        super.onStart()
    }

    override fun onStop() {
        connection.updateConnectionId(SagerConnection.CONNECTION_ID_MAIN_ACTIVITY_BACKGROUND)
        super.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::binding.isInitialized) {
            GroupManager.userInterface = null
            DataStore.configurationStore.unregisterChangeListener(this)
            connection.disconnect(this)
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (!::binding.isInitialized) return super.onKeyDown(keyCode, event)
        if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT && !binding.drawerLayout.isOpen) {
            val focused = currentFocus
            // Explicit within-row links take precedence over opening the drawer.
            if (focused != null && focused.nextFocusLeftId != View.NO_ID) {
                val target = focused.focusSearch(View.FOCUS_LEFT)
                if (target != null && target !== focused && target.isShown && target.isEnabled) {
                    target.requestFocus(); return true
                }
            }
            contentFocus = focused?.let { WeakReference(it) }
            binding.drawerLayout.open(); navigation.requestFocus(); return true
        }
        if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT && binding.drawerLayout.isOpen) {
            binding.drawerLayout.closeDrawers(); return true
        }
        if (binding.drawerLayout.isOpen) return super.onKeyDown(keyCode, event)
        val fragment = supportFragmentManager.findFragmentById(R.id.fragment_holder) as? ToolbarFragment
        if (fragment?.onKeyDown(keyCode, event) == true) return true
        return super.onKeyDown(keyCode, event)
    }


    // Hardware keys must consume both down and up; OK stays with the focused view.
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (!::binding.isInitialized) return super.dispatchKeyEvent(event)
        if (RemoteRowActions.handle(currentFocus, event)) { highlightRemoteFocus(); return true }
        if (event.keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE) {
            if (event.action == KeyEvent.ACTION_DOWN && event.repeatCount == 0) { highlightRemoteFocus(); toggleService() }
            return true
        }
        if (event.keyCode == KeyEvent.KEYCODE_MENU) {
            if (event.action == KeyEvent.ACTION_DOWN && event.repeatCount == 0) {
                highlightRemoteFocus()
                var candidate: View? = currentFocus
                var profile: ProxyEntity? = null
                while (candidate != null && profile == null) {
                    profile = candidate.getTag(R.id.remote_profile_entity) as? ProxyEntity
                    candidate = candidate.parent as? View
                }
                val fragment = supportFragmentManager.findFragmentById(R.id.fragment_holder) as? ConfigurationFragment
                menuKeyHandled = if (profile != null && fragment != null) {
                    fragment.showRemoteProfileActions(profile); true
                } else findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)?.showOverflowMenu() == true
            }
            if (menuKeyHandled) {
                if (event.action == KeyEvent.ACTION_UP) menuKeyHandled = false
                return true
            }
        }
        return super.dispatchKeyEvent(event)
    }

    fun connectProfileForRemote(id: Long) {
        val phase = TvVpnPhase.fromServiceName(DataStore.serviceState.name)
        if (!TvInteractionPolicy.canSelect(phase)) { snackbar(R.string.tv_wait).show(); return }
        val old = DataStore.selectedProxy
        DataStore.selectedProxy = id
        runOnDefaultDispatcher { ProfileManager.postUpdate(old, true); ProfileManager.postUpdate(id, true) }
        executeServiceCommand(TvInteractionPolicy.connectOnly(phase, id, DataStore.currentProfile, connection.service != null))
    }

    private fun toggleService() {
        executeServiceCommand(TvInteractionPolicy.media(TvVpnPhase.fromServiceName(DataStore.serviceState.name), DataStore.selectedProxy, connection.service != null))
    }

    private fun executeServiceCommand(command: TvVpnCommand) {
        val now = android.os.SystemClock.elapsedRealtime()
        if (now - lastRemoteCommand < 750) return
        lastRemoteCommand = now
        when (command) {
            TvVpnCommand.START -> connect.launch(null)
            TvVpnCommand.STOP -> SagerNet.stopService()
            TvVpnCommand.RELOAD -> SagerNet.reloadService()
            TvVpnCommand.NONE -> snackbar(if (DataStore.selectedProxy == 0L) R.string.tv_choose_profile else R.string.tv_wait).show()
        }
    }
}

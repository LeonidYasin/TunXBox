package io.nekohasekai.sagernet.ui.tv

import android.content.Intent
import android.os.Bundle
import android.os.RemoteException
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.leanback.app.BrowseSupportFragment
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.leanback.widget.HeaderItem
import androidx.leanback.widget.ListRow
import androidx.leanback.widget.ListRowPresenter
import androidx.leanback.widget.OnItemViewClickedListener
import io.nekohasekai.sagernet.R
import io.nekohasekai.sagernet.SagerNet
import io.nekohasekai.sagernet.aidl.ISagerNetService
import io.nekohasekai.sagernet.bg.BaseService
import io.nekohasekai.sagernet.bg.SagerConnection
import io.nekohasekai.sagernet.database.DataStore
import io.nekohasekai.sagernet.database.ProfileManager
import io.nekohasekai.sagernet.database.ProxyEntity
import io.nekohasekai.sagernet.database.SagerDatabase
import io.nekohasekai.sagernet.group.RawUpdater
import io.nekohasekai.sagernet.ktx.onMainDispatcher
import io.nekohasekai.sagernet.ktx.runOnDefaultDispatcher
import io.nekohasekai.sagernet.ui.MainActivity
import io.nekohasekai.sagernet.ui.VpnRequestActivity
import java.net.URL

class MainBrowseFragment : BrowseSupportFragment() {

    private lateinit var profilesAdapter: ArrayObjectAdapter
    private lateinit var actionsAdapter: ArrayObjectAdapter
    
    private var serviceState = BaseService.State.Idle

    // Same service connection the phone UI uses: keeps Start/Stop in sync with the real VPN state.
    private val connection = SagerConnection(SagerConnection.CONNECTION_ID_MAIN_ACTIVITY_FOREGROUND, true)
    private val connectionCallback = object : SagerConnection.Callback {
        override fun stateChanged(state: BaseService.State, profileName: String?, msg: String?) {
            onServiceState(state, msg)
        }

        override fun onServiceConnected(service: ISagerNetService) {
            onServiceState(
                try {
                    BaseService.State.values()[service.state]
                } catch (_: RemoteException) {
                    BaseService.State.Idle
                }
            )
        }

        override fun onServiceDisconnected() = onServiceState(BaseService.State.Idle)

        override fun onBinderDied() {
            val activity = activity ?: return
            connection.disconnect(activity)
            connection.connect(activity, this)
        }
    }

    // Starting the VPN needs the system consent dialog on first run; SagerNet.startService() alone
    // fails silently without it. Same contract as MainActivity.
    private val connect = registerForActivityResult(VpnRequestActivity.StartService()) {
        if (it) Toast.makeText(requireContext(), R.string.vpn_permission_denied, Toast.LENGTH_LONG).show()
    }

    companion object {
        const val ACTION_START_PROXY = 1L
        const val ACTION_STOP_PROXY = 2L
        const val ACTION_IMPORT_CLIPBOARD = 3L
        const val ACTION_ADD_PROFILE = 4L
        const val ACTION_IMPORT_URL = 5L
        const val ACTION_IMPORT_FILE = 6L
        const val ACTION_SWITCH_MODE = 7L
        const val ACTION_QR_TRANSFER = 8L
    }

    // File picker for import
    private val importFileLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            runOnDefaultDispatcher {
                try {
                    val text = requireContext().contentResolver.openInputStream(uri)?.bufferedReader()?.readText()
                    if (text.isNullOrBlank()) {
                        onMainDispatcher { Toast.makeText(requireContext(), "Empty file", Toast.LENGTH_SHORT).show() }
                        return@runOnDefaultDispatcher
                    }
                    val proxies = RawUpdater.parseRaw(text)
                    if (proxies.isNullOrEmpty()) {
                        onMainDispatcher { Toast.makeText(requireContext(), "No valid proxy in file", Toast.LENGTH_SHORT).show() }
                    } else {
                        val targetId = DataStore.selectedGroupForImport()
                        proxies.forEach { ProfileManager.createProfile(targetId, it) }
                        onMainDispatcher {
                            Toast.makeText(requireContext(), "Imported ${proxies.size} profile(s)", Toast.LENGTH_LONG).show()
                            loadProfiles()
                        }
                    }
                } catch (e: Exception) {
                    onMainDispatcher { Toast.makeText(requireContext(), "Import failed: ${e.message}", Toast.LENGTH_SHORT).show() }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Must be set before onCreateView: flipping the headers state on an already inflated
        // BrowseSupportFragment leaves the rows laid out for the "headers visible" case.
        headersState = HEADERS_DISABLED
        isHeadersTransitionOnBackEnabled = false
        connection.connect(requireActivity(), connectionCallback)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        title = "TunXBox"
        brandColor = 0xFF0EA5E9.toInt()

        setupEventListeners()
        loadContent()
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
        activity?.let { connection.disconnect(it) }
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()
        serviceState = DataStore.serviceState
        if (::actionsAdapter.isInitialized) updateActionsRow()
        // Profiles may have been added from the phone UI / by an import.
        if (::profilesAdapter.isInitialized) loadProfiles()
    }

    private fun onServiceState(state: BaseService.State, msg: String? = null) {
        DataStore.serviceState = state
        serviceState = state
        if (::actionsAdapter.isInitialized) updateActionsRow()
        if (msg != null && isAdded) Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show()
    }

    private fun setupEventListeners() {
        onItemViewClickedListener = OnItemViewClickedListener { 
            itemViewHolder, item, rowViewHolder, row ->
            
            when (item) {
                is ProxyEntity -> selectAndStartProxy(item)
                is TvAction -> handleAction(item)
            }
        }
    }

    private fun loadContent() {
        val rowsAdapter = ArrayObjectAdapter(ListRowPresenter())
        
        actionsAdapter = ArrayObjectAdapter(ActionPresenter())
        updateActionsRow()
        rowsAdapter.add(ListRow(HeaderItem("Actions"), actionsAdapter))

        profilesAdapter = ArrayObjectAdapter(ProfileCardPresenter())
        rowsAdapter.add(ListRow(HeaderItem("Profiles"), profilesAdapter))
        loadProfiles()

        adapter = rowsAdapter
    }
    
    private fun loadProfiles() {
        runOnDefaultDispatcher {
            val items: List<Any> = try {
                val groupId = DataStore.selectedGroup
                val allProfiles = SagerDatabase.proxyDao.getByGroup(groupId)
                if (allProfiles.isNotEmpty()) allProfiles
                else listOf(TvEmptyHint("No profiles. Use Import or Add below."))
            } catch (e: Exception) {
                listOf(TvEmptyHint("Error: ${e.message}"))
            }
            // Replace the content in one step on the main thread, so overlapping reloads
            // cannot interleave and duplicate cards.
            onMainDispatcher {
                profilesAdapter.clear()
                profilesAdapter.addAll(0, items)
            }
        }
    }
    
    private fun updateActionsRow() {
        actionsAdapter.clear()
        val isConnected = serviceState.canStop
        
        if (isConnected) {
            actionsAdapter.add(TvAction(ACTION_STOP_PROXY, "■ Stop Proxy", "Service running"))
        } else {
            actionsAdapter.add(TvAction(ACTION_START_PROXY, "▶ Start Proxy", "Select profile below"))
        }
        
        actionsAdapter.add(TvAction(ACTION_IMPORT_CLIPBOARD, "📋 Clipboard", "Paste link from phone"))
        actionsAdapter.add(TvAction(ACTION_IMPORT_URL, "🌐 From URL", "Subscription or direct link"))
        actionsAdapter.add(TvAction(ACTION_IMPORT_FILE, "📁 From File", "JSON/YAML/Conf file"))
        actionsAdapter.add(TvAction(ACTION_ADD_PROFILE, "➕ Manual", "Enter details via dialog"))
        actionsAdapter.add(TvAction(ACTION_QR_TRANSFER, "📲 QR Transfer", "Send from phone via QR"))
        actionsAdapter.add(TvAction(ACTION_SWITCH_MODE, "📱 Phone Mode", "Switch to mobile UI"))
    }

    private fun selectAndStartProxy(profile: ProxyEntity) {
        DataStore.selectedProxy = profile.id
        Toast.makeText(requireContext(), "Selected: ${profile.displayName()}", Toast.LENGTH_SHORT).show()

        if (serviceState.canStop) {
            SagerNet.reloadService()
        } else {
            connect.launch(null)
        }
    }

    private fun handleAction(action: TvAction) {
        when (action.id) {
            ACTION_START_PROXY -> {
                if (DataStore.selectedProxy > 0) {
                    connect.launch(null)
                } else {
                    Toast.makeText(requireContext(), "Select a profile first", Toast.LENGTH_SHORT).show()
                }
            }
            ACTION_STOP_PROXY -> SagerNet.stopService()
            ACTION_IMPORT_CLIPBOARD -> importFromClipboard()
            ACTION_IMPORT_URL -> showUrlImportDialog()
            ACTION_IMPORT_FILE -> importFileLauncher.launch("*/*")
            ACTION_ADD_PROFILE -> showManualAddDialog()
            ACTION_QR_TRANSFER -> showQrTransfer()
            ACTION_SWITCH_MODE -> switchToPhoneMode()
        }
    }

    private fun importFromClipboard() {
        val text = SagerNet.getClipboardText()
        if (text.isBlank()) {
            Toast.makeText(requireContext(), "Clipboard empty", Toast.LENGTH_SHORT).show()
            return
        }
        importProxiesFromText(text)
    }
    
    private fun showUrlImportDialog() {
        val input = android.widget.EditText(requireContext()).apply {
            hint = "https://example.com/sub.yaml or ss://..."
            setPadding(48, 32, 48, 32)
        }
        
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Import from URL")
            .setView(input)
            .setPositiveButton("Import") { _, _ ->
                val url = input.text.toString().trim()
                if (url.isBlank()) {
                    Toast.makeText(requireContext(), "URL required", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                runOnDefaultDispatcher {
                    try {
                        val text = URL(url).readText()
                        importProxiesFromText(text)
                    } catch (e: Exception) {
                        onMainDispatcher {
                            Toast.makeText(requireContext(), "Failed: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun showManualAddDialog() {
        val protocols = arrayOf("Shadowsocks", "VMess", "VLESS", "Trojan", "SOCKS5", "HTTP")
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Add Profile (Manual)")
            .setItems(protocols) { _, which ->
                val protocol = protocols[which]
                Toast.makeText(requireContext(), 
                    "$protocol: Use phone app to configure, then sync via clipboard/URL", 
                    Toast.LENGTH_LONG).show()
            }
            .show()
    }
    
    private fun showQrTransfer() {
        // Заменяем текущий фрагмент на QR transfer
        parentFragmentManager.beginTransaction()
            .replace(R.id.tv_container, QrCodeTransferFragment())
            .addToBackStack("qr_transfer")
            .commit()
    }
    
    private fun switchToPhoneMode() {
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("📱 Switch to Phone Mode")
            .setMessage("Restart app with mobile interface?\n\n• Use phone/tablet touch UI\n• To return to TV mode: clear app data or reinstall")
            .setPositiveButton("Switch to Phone") { _, _ ->
                DataStore.profileCacheStore.putString("ui_mode_override", "phone")
                
                val intent = Intent(requireContext(), MainActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                    putExtra("force_phone_mode", true)
                }
                startActivity(intent)
                requireActivity().finish()
            }
            .setNegativeButton("Keep TV Mode", null)
            .show()
    }
    
    private fun importProxiesFromText(text: String) {
        runOnDefaultDispatcher {
            try {
                val proxies = RawUpdater.parseRaw(text)
                if (proxies.isNullOrEmpty()) {
                    onMainDispatcher {
                        Toast.makeText(requireContext(), "No valid proxy found", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    val targetId = DataStore.selectedGroupForImport()
                    for (proxy in proxies) {
                        ProfileManager.createProfile(targetId, proxy)
                    }
                    onMainDispatcher {
                        Toast.makeText(requireContext(), "Imported ${proxies.size} profile(s)", Toast.LENGTH_LONG).show()
                        loadProfiles()
                    }
                }
            } catch (e: Exception) {
                onMainDispatcher {
                    Toast.makeText(requireContext(), "Import failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}

data class TvAction(val id: Long, val title: String, val subtitle: String)
data class TvEmptyHint(val message: String)

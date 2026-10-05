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
import io.nekohasekai.sagernet.database.ProxyEntity
import io.nekohasekai.sagernet.database.SagerDatabase
import io.nekohasekai.sagernet.ktx.onMainDispatcher
import io.nekohasekai.sagernet.ktx.runOnDefaultDispatcher
import io.nekohasekai.sagernet.ui.MainActivity
import io.nekohasekai.sagernet.ui.VpnRequestActivity
import android.net.Uri
import io.nekohasekai.sagernet.ui.profile.HttpSettingsActivity
import io.nekohasekai.sagernet.ui.profile.ShadowsocksSettingsActivity
import io.nekohasekai.sagernet.ui.profile.SocksSettingsActivity
import io.nekohasekai.sagernet.ui.profile.TrojanSettingsActivity
import io.nekohasekai.sagernet.ui.profile.VMessSettingsActivity

class MainBrowseFragment : BrowseSupportFragment() {

    private lateinit var profilesAdapter: ArrayObjectAdapter
    private lateinit var actionsAdapter: ArrayObjectAdapter
    
    private var serviceState = BaseService.State.Idle

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
        const val ACTION_QR_SEND = 8L
        const val ACTION_QR_SCAN = 9L
    }

    private val importFileLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            runOnDefaultDispatcher {
                try {
                    val text = requireContext().contentResolver.openInputStream(uri)?.use { stream ->
                        val data = stream.readBytesLimited()
                        String(data, Charsets.UTF_8)
                    }
                    if (text.isNullOrBlank()) {
                        onMainDispatcher { Toast.makeText(requireContext(), "Empty file", Toast.LENGTH_SHORT).show() }
                        return@runOnDefaultDispatcher
                    }
                    val count = TvProfileImporter.importProfiles(text)
                    onMainDispatcher {
                        if (isAdded && view != null) {
                            Toast.makeText(requireContext(), "Imported $count profile(s)", Toast.LENGTH_LONG).show()
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
                else listOf(TvEmptyHint("No profiles. Use Import or Scan below."))
            } catch (e: Exception) {
                listOf(TvEmptyHint("Error: ${e.message}"))
            }
            onMainDispatcher {
                if (isAdded && view != null) {
                    profilesAdapter.clear()
                    profilesAdapter.addAll(0, items)
                }
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
        actionsAdapter.add(TvAction(ACTION_IMPORT_URL, "🌐 From URL / Link", "Subscription URL or proxy link"))
        actionsAdapter.add(TvAction(ACTION_IMPORT_FILE, "📁 From File", "JSON/YAML/Conf file"))
        actionsAdapter.add(TvAction(ACTION_ADD_PROFILE, "➕ Manual", "Full profile editor"))
        actionsAdapter.add(TvAction(ACTION_QR_SCAN, "📷 Scan QR", "Camera/image — get profiles from phone"))
        actionsAdapter.add(TvAction(ACTION_QR_SEND, "📲 Import from phone", "Browser: paste a link or upload a file"))
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
            ACTION_QR_SCAN -> showQrScan()
            ACTION_QR_SEND -> showQrSend()
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
    
    private fun java.io.InputStream.readBytesLimited(): ByteArray {
        val output = java.io.ByteArrayOutputStream()
        val buffer = ByteArray(8192)
        while (true) {
            val n = read(buffer)
            if (n < 0) break
            require(output.size() + n <= TransferProtocol.MAX_BODY_BYTES) { "File exceeds 2 MiB" }
            output.write(buffer, 0, n)
        }
        return output.toByteArray()
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
                val uri = Uri.parse(url)
                if (uri.scheme == "sn" || uri.scheme == "clash") {
                    // Retain upstream confirmation and compressed subscription link support.
                    startActivity(Intent(requireContext(), MainActivity::class.java).apply {
                        action = Intent.ACTION_VIEW
                        data = uri
                        putExtra("force_phone_mode", true)
                    })
                } else if (uri.scheme == "http" || uri.scheme == "https") {
                    runOnDefaultDispatcher {
                        try {
                            val count = TvProfileImporter.importSubscription(url)
                            onMainDispatcher {
                                if (isAdded) {
                                    Toast.makeText(requireContext(), "Subscription imported: $count profile(s)", Toast.LENGTH_LONG).show()
                                    loadProfiles()
                                }
                            }
                        } catch (e: Exception) {
                            onMainDispatcher {
                                if (isAdded) Toast.makeText(requireContext(), "Subscription import failed. Check the URL and connection.", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                } else {
                    // ss://, vmess://, vless://, trojan:// etc. are profile DATA, not URLs to fetch.
                    importProxiesFromText(url)
                }
            }
            .setNeutralButton("Use phone instead") { _, _ -> showQrSend() }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun showManualAddDialog() {
        val protocols = arrayOf("Shadowsocks", "VMess", "VLESS", "Trojan", "SOCKS5", "HTTP")
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Add Profile (Manual) — easier: Import from phone")
            .setItems(protocols) { _, which ->
                // Same editors and VLESS flag as upstream ConfigurationFragment.
                val editor = when (which) {
                    0 -> ShadowsocksSettingsActivity::class.java
                    1, 2 -> VMessSettingsActivity::class.java
                    3 -> TrojanSettingsActivity::class.java
                    4 -> SocksSettingsActivity::class.java
                    else -> HttpSettingsActivity::class.java
                }
                startActivity(Intent(requireContext(), editor).apply {
                    if (which == 2) putExtra("vless", true)
                })
            }
            .show()
    }

    private fun showQrScan() {
        parentFragmentManager.beginTransaction()
            .replace(R.id.tv_container, TvScannerFragment())
            .addToBackStack("qr_scan")
            .commit()
    }
    
    private fun showQrSend() {
        parentFragmentManager.beginTransaction()
            .replace(R.id.tv_container, QrCodeTransferFragment())
            .addToBackStack("qr_send")
            .commit()
    }
    
    private fun switchToPhoneMode() {
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("📱 Switch to Phone Mode")
            .setMessage("Restart app with mobile interface?\n\n• Touch-optimized phone/tablet UI\n• To return to TV mode: open the side drawer and tap '📺 Switch to TV Mode'")
            .setPositiveButton("Switch to Phone") { _, _ ->
                TvUiPreferences.phoneMode = true
                
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
                val count = TvProfileImporter.importProfiles(text)
                onMainDispatcher {
                    if (isAdded && view != null) {
                        Toast.makeText(requireContext(), "Imported $count profile(s)", Toast.LENGTH_LONG).show()
                        loadProfiles()
                    }
                }
            } catch (e: Exception) {
                onMainDispatcher {
                    if (isAdded && view != null) Toast.makeText(requireContext(), "Import failed. Check configuration (maximum 2 MiB).", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

}

data class TvAction(val id: Long, val title: String, val subtitle: String)
data class TvEmptyHint(val message: String)

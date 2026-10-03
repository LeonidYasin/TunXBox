package io.nekohasekai.sagernet.ui.tv

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.leanback.app.BrowseSupportFragment
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.leanback.widget.HeaderItem
import androidx.leanback.widget.ListRow
import androidx.leanback.widget.ListRowPresenter
import androidx.leanback.widget.OnItemViewClickedListener
import io.nekohasekai.sagernet.SagerNet
import io.nekohasekai.sagernet.bg.BaseService
import io.nekohasekai.sagernet.database.DataStore
import io.nekohasekai.sagernet.database.ProfileManager
import io.nekohasekai.sagernet.database.ProxyEntity
import io.nekohasekai.sagernet.database.SagerDatabase
import io.nekohasekai.sagernet.group.RawUpdater
import io.nekohasekai.sagernet.ktx.onMainDispatcher
import io.nekohasekai.sagernet.ktx.runOnDefaultDispatcher

class MainBrowseFragment : BrowseSupportFragment() {

    private lateinit var profilesAdapter: ArrayObjectAdapter
    private lateinit var actionsAdapter: ArrayObjectAdapter
    
    private var serviceState = BaseService.State.Idle

    companion object {
        const val ACTION_START_PROXY = 1L
        const val ACTION_STOP_PROXY = 2L
        const val ACTION_IMPORT_CLIPBOARD = 3L
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        title = "TunXBox"
        headersState = HEADERS_DISABLED
        isHeadersTransitionOnBackEnabled = false
        brandColor = 0xFF0EA5E9.toInt()
        
        setupEventListeners()
        loadContent()
    }

    override fun onResume() {
        super.onResume()
        // Refresh service state from DataStore
        serviceState = if (DataStore.serviceState.connected) BaseService.State.Connected else BaseService.State.Idle
        if (::actionsAdapter.isInitialized) updateActionsRow()
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
        loadProfiles()

        adapter = rowsAdapter
    }
    
    private fun loadProfiles() {
        profilesAdapter.clear()
        runOnDefaultDispatcher {
            try {
                val groupId = DataStore.selectedGroup
                val allProfiles = SagerDatabase.proxyDao.getByGroup(groupId)
                
                onMainDispatcher {
                    if (allProfiles.isNotEmpty()) {
                        allProfiles.forEach { profilesAdapter.add(it) }
                    } else {
                        profilesAdapter.add(TvEmptyHint("No profiles. Use Import or add via phone."))
                    }
                }
            } catch (e: Exception) {
                onMainDispatcher {
                    profilesAdapter.add(TvEmptyHint("Error: ${e.message}"))
                }
            }
        }
    }
    
    private fun updateActionsRow() {
        actionsAdapter.clear()
        val isConnected = serviceState == BaseService.State.Connected
        
        if (isConnected) {
            actionsAdapter.add(TvAction(ACTION_STOP_PROXY, "■ Stop Proxy", "Service running"))
        } else {
            actionsAdapter.add(TvAction(ACTION_START_PROXY, "▶ Start Proxy", "Select profile below"))
        }
        
        actionsAdapter.add(TvAction(ACTION_IMPORT_CLIPBOARD, "📋 Import Clipboard", "Paste proxy link"))
    }

    private fun selectAndStartProxy(profile: ProxyEntity) {
        DataStore.selectedProxy = profile.id
        Toast.makeText(requireContext(), "Selected: ${profile.displayName()}", Toast.LENGTH_SHORT).show()
        
        if (serviceState == BaseService.State.Connected) {
            SagerNet.stopService()
            SagerNet.startService()
        } else {
            SagerNet.startService()
        }
    }

    private fun handleAction(action: TvAction) {
        when (action.id) {
            ACTION_START_PROXY -> {
                if (DataStore.selectedProxy > 0) {
                    SagerNet.startService()
                } else {
                    Toast.makeText(requireContext(), "Select a profile first", Toast.LENGTH_SHORT).show()
                }
            }
            ACTION_STOP_PROXY -> SagerNet.stopService()
            ACTION_IMPORT_CLIPBOARD -> importFromClipboard()
        }
    }

    private fun importFromClipboard() {
        val text = SagerNet.getClipboardText()
        if (text.isBlank()) {
            Toast.makeText(requireContext(), "Clipboard empty", Toast.LENGTH_SHORT).show()
            return
        }
        
        runOnDefaultDispatcher {
            try {
                val proxies = RawUpdater.parseRaw(text)
                if (proxies.isNullOrEmpty()) {
                    onMainDispatcher {
                        Toast.makeText(requireContext(), "No valid proxy in clipboard", Toast.LENGTH_SHORT).show()
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

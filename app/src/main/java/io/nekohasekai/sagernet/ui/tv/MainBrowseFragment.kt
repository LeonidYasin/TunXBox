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
import androidx.leanback.widget.Presenter
import io.nekohasekai.sagernet.SagerNet
import io.nekohasekai.sagernet.bg.BaseService
import io.nekohasekai.sagernet.bg.SagerConnection
import io.nekohasekai.sagernet.database.DataStore
import io.nekohasekai.sagernet.database.ProfileManager
import io.nekohasekai.sagernet.database.ProxyEntity
import io.nekohasekai.sagernet.database.SagerDatabase
import io.nekohasekai.sagernet.group.RawUpdater
import io.nekohasekai.sagernet.ktx.onMainDispatcher
import io.nekohasekai.sagernet.ktx.runOnDefaultDispatcher
import kotlinx.coroutines.runBlocking

class MainBrowseFragment : BrowseSupportFragment() {

    private lateinit var profilesAdapter: ArrayObjectAdapter
    private lateinit var actionsAdapter: ArrayObjectAdapter
    
    private val connection = SagerConnection(SagerConnection.CONNECTION_ID_REMOTE)
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
        brandColor = 0xFF6C63FF.toInt()
        
        setupEventListeners()
        loadContent()
        
        // Connect to service
        connection.connect(requireActivity(), object : SagerConnection.Callback {
            override fun onServiceConnected(service: io.nekohasekai.sagernet.aidl.ISagerNetService) {
                runOnDefaultDispatcher {
                    try {
                        val stateOrdinal = service.state
                        serviceState = BaseService.State.values().getOrElse(stateOrdinal) { BaseService.State.Idle }
                    } catch (_: Exception) {}
                    onMainDispatcher { updateActionsRow() }
                }
            }
            override fun onServiceDisconnected() {
                serviceState = BaseService.State.Idle
                activity?.runOnUiThread { updateActionsRow() }
            }
            override fun onBinderDied() {
                serviceState = BaseService.State.Idle
                activity?.runOnUiThread { updateActionsRow() }
            }
        })
    }

    override fun onDestroyView() {
        connection.disconnect()
        super.onDestroyView()
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
        
        // Row 1: Quick Actions
        actionsAdapter = ArrayObjectAdapter(ActionPresenter())
        updateActionsRow()
        rowsAdapter.add(ListRow(HeaderItem("Actions"), actionsAdapter))

        // Row 2: Profiles — load from DB
        profilesAdapter = ArrayObjectAdapter(ProfileCardPresenter())
        loadProfiles()

        adapter = rowsAdapter
    }
    
    private fun loadProfiles() {
        profilesAdapter.clear()
        runOnDefaultDispatcher {
            try {
                val groupId = DataStore.selectedGroup
                // Get all profile IDs for current group via Room DAO
                val profileIds = SagerDatabase.proxyDao.getByGroup(groupId).map { it.id }
                val profiles = ProfileManager.getProfiles(profileIds)
                
                onMainDispatcher {
                    if (profiles.isNotEmpty()) {
                        profiles.forEach { profilesAdapter.add(it) }
                    } else {
                        // Show empty hint
                        profilesAdapter.add(TvEmptyHint("No profiles in current group.\nUse Import or switch group on phone."))
                    }
                }
            } catch (e: Exception) {
                onMainDispatcher {
                    profilesAdapter.add(TvEmptyHint("Error loading profiles: ${e.message}"))
                }
            }
        }
    }
    
    private fun updateActionsRow() {
        actionsAdapter.clear()
        val isConnected = serviceState == BaseService.State.Connected
        
        if (isConnected) {
            actionsAdapter.add(TvAction(ACTION_STOP_PROXY, "■ Stop Proxy", "Service is running"))
        } else {
            actionsAdapter.add(TvAction(ACTION_START_PROXY, "▶ Start Proxy", "Select a profile below"))
        }
        
        actionsAdapter.add(TvAction(ACTION_IMPORT_CLIPBOARD, "📋 Import Clipboard", "Paste proxy link from phone"))
    }

    private fun selectAndStartProxy(profile: ProxyEntity) {
        DataStore.selectedProxy = profile.id
        Toast.makeText(context, "Selected: ${profile.displayName()}", Toast.LENGTH_SHORT).show()
        
        if (serviceState == BaseService.State.Connected) {
            // Restart with new profile
            runOnDefaultDispatcher {
                try {
                    connection.service?.restartService()
                } catch (e: Exception) {
                    onMainDispatcher {
                        Toast.makeText(context, "Restart failed: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
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
                    Toast.makeText(context, "Select a profile first", Toast.LENGTH_SHORT).show()
                }
            }
            ACTION_STOP_PROXY -> SagerNet.stopService()
            ACTION_IMPORT_CLIPBOARD -> importFromClipboard()
        }
    }

    private fun importFromClipboard() {
        val text = SagerNet.getClipboardText()
        if (text.isBlank()) {
            Toast.makeText(context, "Clipboard is empty", Toast.LENGTH_SHORT).show()
            return
        }
        
        runOnDefaultDispatcher {
            try {
                val proxies = RawUpdater.parseRaw(text)
                if (proxies.isNullOrEmpty()) {
                    onMainDispatcher {
                        Toast.makeText(context, "No valid proxy in clipboard", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    val targetId = DataStore.selectedGroupForImport()
                    for (proxy in proxies) {
                        ProfileManager.createProfile(targetId, proxy)
                    }
                    onMainDispatcher {
                        Toast.makeText(context, "Imported ${proxies.size} profile(s)", Toast.LENGTH_LONG).show()
                        loadProfiles()
                    }
                }
            } catch (e: Exception) {
                onMainDispatcher {
                    Toast.makeText(context, "Import failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}

data class TvAction(val id: Long, val title: String, val subtitle: String)
data class TvEmptyHint(val message: String)

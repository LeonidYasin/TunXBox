package io.nekohasekai.sagernet.ui.tv

import android.content.Intent
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
import androidx.leanback.widget.Row
import androidx.leanback.widget.RowPresenter
import io.nekohasekai.sagernet.SagerNet
import io.nekohasekai.sagernet.bg.BaseService
import io.nekohasekai.sagernet.database.DataStore
import io.nekohasekai.sagernet.database.ProfileManager
import io.nekohasekai.sagernet.database.ProxyEntity
import io.nekohasekai.sagernet.group.RawUpdater
import io.nekohasekai.sagernet.ktx.onMainDispatcher
import io.nekohasekai.sagernet.ktx.runOnDefaultDispatcher
import kotlinx.coroutines.runBlocking

class MainBrowseFragment : BrowseSupportFragment() {

    private lateinit var profilesAdapter: ArrayObjectAdapter
    private lateinit var actionsAdapter: ArrayObjectAdapter
    
    // Service connection for start/stop
    private val connection = SagerNet.connection
    private var serviceState = BaseService.State.Idle

    companion object {
        const val ACTION_START_PROXY = 1L
        const val ACTION_STOP_PROXY = 2L
        const val ACTION_IMPORT_CLIPBOARD = 3L
        const val ACTION_SETTINGS = 4L
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        title = "TunXBox"
        headersState = HEADERS_DISABLED
        isHeadersTransitionOnBackEnabled = false
        brandColor = 0xFF6C63FF.toInt()
        
        setupEventListeners()
        loadContent()
        
        // Connect to service for state updates
        connection.connect(requireActivity(), object : io.nekohasekai.sagernet.bg.SagerConnection.Callback {
            override fun onConnected(binder: io.nekohasekai.sagernet.aidl.ISagerNetService) {}
            override fun onDisconnected() {}
            override fun onStateChanged(state: BaseService.State, profileName: String?, msg: String?) {
                serviceState = state
                activity?.runOnUiThread { updateActionsRow() }
            }
            override fun onTrafficUpdated(data: io.nekohasekai.sagernet.aidl.TrafficData) {}
            override fun onSpeedUpdated(data: io.nekohasekai.sagernet.aidl.SpeedDisplayData) {}
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
                is ProxyEntity -> {
                    selectAndStartProxy(item)
                }
                is TvAction -> {
                    handleAction(item)
                }
            }
        }
    }

    private fun loadContent() {
        val rowsAdapter = ArrayObjectAdapter(ListRowPresenter())
        
        // Row 1: Quick Actions
        actionsAdapter = ArrayObjectAdapter(ActionPresenter())
        updateActionsRow()
        rowsAdapter.add(ListRow(HeaderItem("Actions"), actionsAdapter))

        // Row 2: Profiles
        profilesAdapter = ArrayObjectAdapter(ProfileCardPresenter())
        val profiles = runBlocking { ProfileManager.getAllProfiles() }
        profiles.forEach { profilesAdapter.add(it) }

        if (profilesAdapter.size() > 0) {
            val header = HeaderItem("Profiles (${profilesAdapter.size()})")
            rowsAdapter.add(ListRow(header, profilesAdapter))
        } else {
            val emptyAdapter = ArrayObjectAdapter(object : Presenter() {
                override fun onCreateViewHolder(parent: android.view.ViewGroup): ViewHolder {
                    val tv = android.widget.TextView(parent.context).apply {
                        text = "No profiles yet.\nUse \"Import from Clipboard\" or add via phone."
                        textSize = 18f
                        setTextColor(0xAAFFFFFF.toInt())
                        setPadding(48, 48, 48, 48)
                    }
                    return ViewHolder(tv)
                }
                override fun onBindViewHolder(vh: ViewHolder, item: Any?) {}
                override fun onUnbindViewHolder(vh: ViewHolder) {}
            })
            emptyAdapter.add("")
            rowsAdapter.add(ListRow(HeaderItem("Getting Started"), emptyAdapter))
        }

        adapter = rowsAdapter
    }
    
    private fun updateActionsRow() {
        actionsAdapter.clear()
        val isConnected = serviceState == BaseService.State.Connected
        
        if (isConnected) {
            actionsAdapter.add(TvAction(ACTION_STOP_PROXY, " Stop Proxy", "Currently running"))
        } else {
            actionsAdapter.add(TvAction(ACTION_START_PROXY, "▶ Start Proxy", "Select a profile first"))
        }
        
        actionsAdapter.add(TvAction(ACTION_IMPORT_CLIPBOARD, "📋 Import from Clipboard", "Paste proxy link"))
        actionsAdapter.add(TvAction(ACTION_SETTINGS, "⚙ Settings", "App preferences"))
    }

    private fun selectAndStartProxy(profile: ProxyEntity) {
        DataStore.selectedProxy = profile.id
        
        if (serviceState == BaseService.State.Connected) {
            // Restart with new profile
            connection.service?.let { svc ->
                runOnDefaultDispatcher {
                    try {
                        svc.restartService()
                    } catch (e: Exception) {
                        onMainDispatcher {
                            Toast.makeText(context, "Failed to restart: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        } else {
            // Start service
            SagerNet.startService()
        }
        
        Toast.makeText(context, "Selected: ${profile.displayName()}", Toast.LENGTH_SHORT).show()
    }

    private fun handleAction(action: TvAction) {
        when (action.id) {
            ACTION_START_PROXY -> {
                val selectedId = DataStore.selectedProxy
                if (selectedId > 0) {
                    SagerNet.startService()
                } else {
                    Toast.makeText(context, "Select a profile first", Toast.LENGTH_SHORT).show()
                }
            }
            ACTION_STOP_PROXY -> {
                SagerNet.stopService()
            }
            ACTION_IMPORT_CLIPBOARD -> {
                importFromClipboard()
            }
            ACTION_SETTINGS -> {
                // TODO: GuidedStepSettingsFragment
                Toast.makeText(context, "Settings coming soon", Toast.LENGTH_SHORT).show()
            }
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
                        Toast.makeText(context, "No valid proxy found in clipboard", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    val targetId = DataStore.selectedGroupForImport()
                    for (proxy in proxies) {
                        ProfileManager.createProfile(targetId, proxy)
                    }
                    onMainDispatcher {
                        Toast.makeText(context, "Imported ${proxies.size} profile(s)", Toast.LENGTH_LONG).show()
                        refreshProfiles()
                    }
                }
            } catch (e: Exception) {
                onMainDispatcher {
                    Toast.makeText(context, "Import failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun refreshProfiles() {
        profilesAdapter.clear()
        val profiles = runBlocking { ProfileManager.getAllProfiles() }
        profiles.forEach { profilesAdapter.add(it) }
    }
}

/**
 * Simple action item for the Actions row
 */
data class TvAction(val id: Long, val title: String, val subtitle: String)

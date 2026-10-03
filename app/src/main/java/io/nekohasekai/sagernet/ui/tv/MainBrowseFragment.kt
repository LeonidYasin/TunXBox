package io.nekohasekai.sagernet.ui.tv

import android.os.Bundle
import android.view.View
import androidx.leanback.app.BrowseSupportFragment
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.leanback.widget.HeaderItem
import androidx.leanback.widget.ListRow
import androidx.leanback.widget.ListRowPresenter
import androidx.leanback.widget.OnItemViewClickedListener
import androidx.leanback.widget.Presenter
import androidx.leanback.widget.Row
import androidx.leanback.widget.RowPresenter
import io.nekohasekai.sagernet.database.DataStore
import io.nekohasekai.sagernet.database.ProfileManager
import io.nekohasekai.sagernet.database.ProxyEntity
import kotlinx.coroutines.runBlocking

class MainBrowseFragment : BrowseSupportFragment() {

    private lateinit var profilesAdapter: ArrayObjectAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        title = "TunXBox"
        headersState = HEADERS_DISABLED
        isHeadersTransitionOnBackEnabled = false
        brandColor = 0xFF6C63FF.toInt()
        
        setupEventListeners()
        loadContent()
    }

    private fun setupEventListeners() {
        onItemViewClickedListener = OnItemViewClickedListener { 
            itemViewHolder, item, rowViewHolder, row ->
            
            when (item) {
                is ProxyEntity -> {
                    // Select and start proxy
                    DataStore.selectedProxy = item.id
                    // TODO: trigger service start via SagerConnection
                }
            }
        }
    }

    private fun loadContent() {
        val rowsAdapter = ArrayObjectAdapter(ListRowPresenter())
        profilesAdapter = ArrayObjectAdapter(ProfileCardPresenter())

        // Load profiles
        val profiles = runBlocking { ProfileManager.getAllProfiles() }
        profiles.forEach { profilesAdapter.add(it) }

        if (profilesAdapter.size() > 0) {
            val header = HeaderItem("Profiles (${profilesAdapter.size()})")
            rowsAdapter.add(ListRow(header, profilesAdapter))
        } else {
            // Empty state
            val emptyAdapter = ArrayObjectAdapter(object : Presenter() {
                override fun onCreateViewHolder(parent: android.view.ViewGroup): ViewHolder {
                    val tv = android.widget.TextView(parent.context).apply {
                        text = "No profiles configured.\nUse phone to add profiles first."
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
    
    /**
     * Refresh profiles list (call after import/add)
     */
    fun refreshProfiles() {
        profilesAdapter.clear()
        val profiles = runBlocking { ProfileManager.getAllProfiles() }
        profiles.forEach { profilesAdapter.add(it) }
    }
}

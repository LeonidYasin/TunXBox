package io.nekohasekai.sagernet.ui.tv

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.RemoteException
import android.os.SystemClock
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import android.app.AlertDialog
import androidx.leanback.app.BrowseSupportFragment
import androidx.leanback.widget.*
import androidx.lifecycle.lifecycleScope
import io.nekohasekai.sagernet.*
import io.nekohasekai.sagernet.aidl.ISagerNetService
import io.nekohasekai.sagernet.aidl.TrafficData
import io.nekohasekai.sagernet.bg.BaseService
import io.nekohasekai.sagernet.bg.SagerConnection
import io.nekohasekai.sagernet.database.*
import io.nekohasekai.sagernet.group.GroupUpdater
import io.nekohasekai.sagernet.ui.MainActivity
import io.nekohasekai.sagernet.ui.VpnRequestActivity
import io.nekohasekai.sagernet.ui.profile.*
import io.nekohasekai.sagernet.widget.QRCodeDialog
import kotlinx.coroutines.*
import kotlin.coroutines.resume

/** A remote-first screen. Stable adapter identities keep focus through service/DB updates. */
class MainBrowseFragment : BrowseSupportFragment() {
    private lateinit var actionsAdapter: ArrayObjectAdapter
    private lateinit var rowAdapter: ArrayObjectAdapter
    private val groupAdapters=linkedMapOf<Long,ArrayObjectAdapter>()
    private var groupProfiles=emptyMap<Long,List<TvProfileCard>>()
    private lateinit var connectionAdapter: ArrayObjectAdapter
    private var stats = io.nekohasekai.sagernet.aidl.SpeedDisplayData()
    private var statsAt = 0L
    private var healthJob: Job? = null
    private var health = ""
    private val healthSession=TvHealthSession()
    private var healthResult: io.nekohasekai.sagernet.group.ConnectionTestResult?=null
    private var serviceEpoch=0L
    private var testJob: Job? = null
    private var extendedJob: Job?=null
    private var extendedProbe: suspend (android.content.Context,ProxyEntity,suspend (String)->Unit)->io.nekohasekai.sagernet.ui.diagnostics.ExtendedConnectionDiagnostics.Report = {context,profile,progress->io.nekohasekai.sagernet.ui.diagnostics.ExtendedConnectionDiagnostics.run(context,profile,progress)}
    private var extendedReport: io.nekohasekai.sagernet.ui.diagnostics.ExtendedConnectionDiagnostics.Report?=null
    private var qualityByProfile=emptyMap<Long,TvQualityResult>()
    private var qualityUrlProbe: suspend (ProxyEntity)->Int = {TvDiagnostics.url(it)}
    /** Instrumentation supplies a fresh worker so a cancelled probe cannot block fixture ranking. */
    private var qualityWorkerOverride: io.nekohasekai.sagernet.group.BoundedConnectionCheck? = null
    private var qualityTcpProbe: suspend (ProxyEntity)->Int = {TvDiagnostics.tcp(it)}
    private lateinit var toolsAdapter: ArrayObjectAdapter
    private lateinit var operationsAdapter: ArrayObjectAdapter
    private var checkStartedAt=0L
    private var checkProgress: android.widget.TextView?=null
    private var checkDialog: AlertDialog?=null
    private var phase = TvVpnPhase.IDLE
    private var serviceReady = false
    private var profiles = emptyList<TvProfileCard>()
    private var groups = emptyList<ProxyGroup>()
    private var selectedName = ""
    private var activeName = ""
    private var loadJob: Job? = null
    private var subscriptionJob: Job? = null
    private var updateDeclined = false
    private var updateFailureShown = false
    private var dialog: AlertDialog? = null
    private var focusedRow = ROW_ACTIONS
    private var focusedId = TOGGLE
    private var restoreAfterLoad = true
    private var lastCommand = -750L
    private val connection = SagerConnection(SagerConnection.CONNECTION_ID_MAIN_ACTIVITY_FOREGROUND, true)

    companion object {
        private const val ROW_ACTIONS = 0L
        private const val ROW_TOOLS = 2L
        private const val ROW_OPERATIONS = 4L
        private const val TOGGLE = 1L
        private const val GROUPS = 2L
        private const val ADD_PROFILE = 3L
        private const val PROFILE_ACTIONS = 4L
        private const val UPDATE = 5L
        private const val MORE = 6L
        private const val PHONE_MODE = 7L
        private const val STATUS = 8L
        private const val TESTS = 9L
        private const val FULL_TOOLS = 10L
        private const val HOME = 11L
        private const val YOUTUBE = 12L
        private const val RESTART_APP = 13L
        private const val CLOSE_APP = 14L
        private const val SUBSCRIPTION_DIAGNOSTICS = 15L
        private const val DIAGNOSTICS = 16L
        private const val APP_UPDATE = 17L
        // Replaced by instrumentation before a fixture ranking. A cancelled screen must not
        // leave its unfinished probe occupying the next explicit test in the same process.
        internal var qualityWorker=io.nekohasekai.sagernet.group.BoundedConnectionCheck()
        private const val ROW_CONNECTION = 3L
    }

    private val diff = TvRowDiff
    private fun itemId(item: Any?)=TvGroupRows.focus(item)
    private fun toast(message: String) { if (isAdded && view != null) Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show() }
    private fun toast(resource: Int) = toast(getString(resource))
    private fun launchWork(work: suspend () -> Unit) {
        if (view == null) return
        viewLifecycleOwner.lifecycleScope.launch {
            try { work() } catch (cancelled: CancellationException) { throw cancelled }
            catch (_: Exception) { toast(R.string.tv_operation_failed) }
        }
    }

    private val connectionCallback = object : SagerConnection.Callback {
        override fun cbSpeedUpdate(stats: io.nekohasekai.sagernet.aidl.SpeedDisplayData) {
            updateTraffic(stats)
        }
        override fun cbTrafficUpdate(data: TrafficData) { launchWork { withContext(Dispatchers.IO) { ProfileManager.postUpdate(data) } } }
        override fun cbSelectorUpdate(id: Long) { DataStore.selectedProxy = id; requestSnapshot() }
        override fun stateChanged(state: BaseService.State, profileName: String?, msg: String?) {
            serviceReady = true
            updateService(state, profileName, msg)
        }
        override fun onServiceConnected(service: ISagerNetService) {
            try {
                serviceReady = true
                updateService(BaseService.State.values()[service.state], service.profileName)
            } catch (_: RemoteException) { onServiceDisconnected() }
        }
        override fun onServiceDisconnected() { serviceReady = false; updateService(BaseService.State.Idle) }
        override fun onBinderDied() {
            serviceReady = false
            activity?.let { connection.disconnect(it); connection.connect(it, this) }
            refreshCards()
        }
        override fun missingPlugin(profileName: String, pluginName: String) {
            showConnectionDiagnostics()
        }
    }
    private fun updateService(state: BaseService.State, name: String? = null, message: String? = null) {
        DataStore.serviceState = state
        val previousPhase = phase
        phase = TvVpnPhase.fromServiceName(state.name)
        if (phase != previousPhase || name != null && name != activeName) { serviceEpoch++;healthJob?.cancel();extendedJob?.cancel();extendedReport=null;health="";healthResult=null }
        if (phase in setOf(TvVpnPhase.IDLE, TvVpnPhase.STOPPED)) { stats = io.nekohasekai.sagernet.aidl.SpeedDisplayData(); statsAt = 0L; activeName = ""; health = "" }
        if (name != null && phase !in setOf(TvVpnPhase.IDLE, TvVpnPhase.STOPPED)) activeName = name
        refreshCards()
        maybeAutoCheck()
        if (message != null) {
            val saved=io.nekohasekai.sagernet.ui.diagnostics.ConnectionDiagnosticJournal.read(DataStore.selectedProxy)?.takeIf {it.category!=null&&it.stage!=io.nekohasekai.sagernet.ui.diagnostics.DiagnosticStage.URL_TEST}
            if(saved==null)io.nekohasekai.sagernet.ui.diagnostics.ConnectionDiagnosticJournal.write(
                io.nekohasekai.sagernet.ui.diagnostics.ConnectionDiagnostic(DataStore.selectedProxy,System.currentTimeMillis(),
                    io.nekohasekai.sagernet.ui.diagnostics.DiagnosticStage.STARTUP,
                    io.nekohasekai.sagernet.group.SubscriptionFailureCategory.fromMessage(message)))
            if(isResumed)showConnectionDiagnostics()
        }
    }

    private val profileListener = object : ProfileManager.Listener {
        override suspend fun onAdd(profile: ProxyEntity) { requestSnapshot() }
        override suspend fun onUpdated(data: TrafficData) { /* Traffic does not change card identity. */ }
        override suspend fun onUpdated(profile: ProxyEntity, noTraffic: Boolean) { requestSnapshot() }
        override suspend fun onRemoved(groupId: Long, profileId: Long) { requestSnapshot() }
    }
    private val groupListener = object : GroupManager.Listener {
        override suspend fun groupAdd(group: ProxyGroup) { requestSnapshot() }
        override suspend fun groupUpdated(group: ProxyGroup) { requestSnapshot() }
        override suspend fun groupRemoved(groupId: Long) { requestSnapshot() }
        override suspend fun groupUpdated(groupId: Long) { requestSnapshot() }
    }

    private val connect = registerForActivityResult(VpnRequestActivity.StartService()) {
        if (it) toast(R.string.vpn_permission_denied)
    }
    private val importFile = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) launchWork {
            val context = requireContext().applicationContext
            val count = withContext(Dispatchers.IO) {
                val text = context.contentResolver.openInputStream(uri)?.use { String(TransferProtocol.readLimited(it), Charsets.UTF_8) }
                    ?: error("Could not read file")
                TvProfileImporter.importProfiles(text)
            }
            toast(getString(R.string.tv_import_count, count)); requestSnapshot()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        headersState = HEADERS_DISABLED
        isHeadersTransitionOnBackEnabled = false
        focusedRow = savedInstanceState?.getLong("tv_focus_row", ROW_ACTIONS) ?: ROW_ACTIONS
        focusedId = savedInstanceState?.getLong("tv_focus_item", TOGGLE) ?: TOGGLE
        connection.connect(requireActivity(), connectionCallback)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        brandColor = 0xFF0EA5E9.toInt()
        titleView?.findViewById<android.widget.TextView>(androidx.leanback.R.id.title_text)?.apply {
            setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen.tv_browse_title_size))
            layoutParams.height = resources.getDimensionPixelSize(R.dimen.tv_browse_title_height)
            maxLines = 1
            ellipsize = android.text.TextUtils.TruncateAt.END
        }
        actionsAdapter = ArrayObjectAdapter(ActionPresenter())

        operationsAdapter = ArrayObjectAdapter(ActionPresenter())
        toolsAdapter = ArrayObjectAdapter(ClassPresenterSelector()
            .addClassPresenter(TvAction::class.java,ActionPresenter())
            .addClassPresenter(TvFunctionCard::class.java,FunctionCardPresenter()))
        connectionAdapter = ArrayObjectAdapter(ClassPresenterSelector()
            .addClassPresenter(TvAction::class.java,ActionPresenter())
            .addClassPresenter(TvConnectionCard::class.java,ConnectionCardPresenter())
            .addClassPresenter(TvQualityCard::class.java,QualityCardPresenter()))
        rowAdapter=ArrayObjectAdapter(StableTvRowPresenter(resources.configuration.screenWidthDp<600))
        adapter=rowAdapter
        onItemViewSelectedListener = OnItemViewSelectedListener { _, item, _, row ->
            if (item != null && row is ListRow) { focusedRow = row.headerItem.id; focusedId = itemId(item) }
        }
        onItemViewClickedListener = OnItemViewClickedListener { _, item, _, _ ->
            when (item) {
                is TvQualityCard -> showConnectionDiagnostics()
                is TvConnectionCard -> showConnectionDiagnostics()
                is TvProfileCard -> selectProfile(item.id)
                is TvGroupCard -> {selectGroup(item.id);runGroupQuality(item.id)}
                is TvFunctionCard -> openFullScreen(item.id)
                is TvEmptyHint -> {if(item.groupId>0)selectGroup(item.groupId);showImportMethods()}
                is TvAction -> if (item.available) handleAction(item.id) else toast(if (item.id == TOGGLE && DataStore.selectedProxy == 0L || item.id == PROFILE_ACTIONS) R.string.tv_choose_profile else if (item.id == UPDATE && groups.firstOrNull { it.id == DataStore.selectedGroup }?.type != GroupType.SUBSCRIPTION) R.string.tv_not_subscription else R.string.tv_wait)
            }
        }
        parentFragmentManager.setFragmentResultListener("diagnostic_repeat",viewLifecycleOwner) {_,_->
            if(phase==TvVpnPhase.CONNECTED)testActiveConnection()
        }
        parentFragmentManager.setFragmentResultListener("diagnostic_extended",viewLifecycleOwner) {_,_->runExtendedDiagnostics()}
        restoreAfterLoad = true
        view.post {
            rowsSupportFragment?.verticalGridView?.apply {
                setAnimateChildLayout(false)
                if (resources.configuration.screenWidthDp < 600) setFocusScrollStrategy(BaseGridView.FOCUS_SCROLL_ITEM)
            }
        }
        refreshCards(); requestSnapshot()
    }
    override fun onStart() {
        super.onStart()
        connection.updateConnectionId(SagerConnection.CONNECTION_ID_MAIN_ACTIVITY_FOREGROUND)
        ProfileManager.addListener(profileListener); GroupManager.addListener(groupListener)
    }
    override fun onResume() { super.onResume(); requestSnapshot();maybeAutoCheck() }
    override fun onPause() { restoreAfterLoad = true; super.onPause() }
    override fun onStop() {
        ProfileManager.removeListener(profileListener); GroupManager.removeListener(groupListener)
        connection.updateConnectionId(SagerConnection.CONNECTION_ID_MAIN_ACTIVITY_BACKGROUND)
        subscriptionJob?.cancel(); testJob?.cancel(); healthJob?.cancel(); extendedJob?.cancel()
        dialog?.dismiss(); dialog = null
        super.onStop()
    }
    override fun onDestroyView() {
        loadJob?.cancel(); subscriptionJob?.cancel(); dialog?.dismiss(); dialog = null
        super.onDestroyView()
    }
    override fun onDestroy() { activity?.let { connection.disconnect(it) }; super.onDestroy() }
    override fun onSaveInstanceState(outState: Bundle) {
        outState.putLong("tv_focus_row", focusedRow); outState.putLong("tv_focus_item", focusedId)
        super.onSaveInstanceState(outState)
    }

    private data class Snapshot(val groups: List<ProxyGroup>, val cards: Map<Long,List<TvProfileCard>>, val selectedName: String, val activeName: String)
    private fun requestSnapshot() {
        // Managers can call listeners on a worker thread. Post before accessing view lifecycle.
        view?.post {
            if (view == null) return@post
            loadJob?.cancel()
            loadJob = viewLifecycleOwner.lifecycleScope.launch {
                delay(60) // Coalesce subscription/import batches into one DB read.
                try {
                    val snapshot = withContext(Dispatchers.IO) {
                        DataStore.currentGroup() // Ensure a valid initial basic group exists.
                        val available = SagerDatabase.groupDao.allGroups()
                        if (available.none { it.id == DataStore.selectedGroup } && available.isNotEmpty()) DataStore.selectedGroup = available.first().id
                        val entities=SagerDatabase.proxyDao.getAll().groupBy {it.groupId}
                        val cards=available.associate {group ->
                            val entries=entities[group.id].orEmpty()
                            val ordered=when(group.order) {
                                GroupOrder.BY_NAME -> entries.sortedBy {it.displayName()}
                                GroupOrder.BY_DELAY -> entries.sortedBy {if(it.status==1)it.ping else 114514}
                                else -> entries.sortedBy {it.userOrder}
                            }
                            group.id to ordered.map {entity -> TvProfileCard(entity.id,entity.displayName().orEmpty(),
                                entity.displayType()+(qualityByProfile[entity.id]?.let {getString(R.string.tv_quality_profile,it.successes,TvGroupQuality.SAMPLES)}?:"")+when(entity.status){1->" · ${entity.ping} ms";2,3->" · ${getString(R.string.tv_test_unavailable)}";else->""},
                                entity.requireBean().serverAddress.orEmpty(),false,null,group.id)}
                        }
                        Snapshot(available,cards,ProfileManager.getProfile(DataStore.selectedProxy)?.displayName().orEmpty(),
                            ProfileManager.getProfile(activeProfileId())?.displayName().orEmpty())
                    }
                    groups=snapshot.groups;groupProfiles=snapshot.cards;profiles=groupProfiles[DataStore.selectedGroup].orEmpty()
                    selectedName=snapshot.selectedName;activeName=snapshot.activeName
                    val lostProfile = focusedRow>=TvGroupRows.BASE &&
                        (groups.none {TvGroupRows.row(it.id)==focusedRow} ||
                         (focusedId>0 && groupProfiles[focusedRow-TvGroupRows.BASE].orEmpty().none {it.id==focusedId}))
                    refreshCards();maybeAutoCheck()
                    if (restoreAfterLoad || lostProfile) { restoreAfterLoad = false; restoreFocus() }
                } catch (cancelled: CancellationException) { throw cancelled }
                catch (_: Exception) { toast(R.string.tv_operation_failed) }
            }
        }
    }
    private fun activeProfileId(): Long = if (phase == TvVpnPhase.CONNECTING) DataStore.selectedProxy else DataStore.currentProfile

    private fun phaseLabel(): String = getString(if (!serviceReady) R.string.tv_unavailable else when (phase) {
        TvVpnPhase.CONNECTING -> R.string.tv_connecting
        TvVpnPhase.CONNECTED -> R.string.tv_connected
        TvVpnPhase.STOPPING -> R.string.tv_stopping
        else -> R.string.tv_idle
    })
    private fun refreshCards() {
        if (view == null || !::actionsAdapter.isInitialized) return
        title = "TunXBox · ${phaseLabel()}"
        val command = TvInteractionPolicy.primary(phase, DataStore.selectedProxy, activeProfileId(), serviceReady)
        val label = when (command) {
            TvVpnCommand.START -> R.string.tv_start
            TvVpnCommand.RELOAD -> R.string.tv_switch_profile
            TvVpnCommand.STOP -> if (phase == TvVpnPhase.CONNECTING) R.string.tv_cancel else R.string.tv_stop
            else -> if (phase == TvVpnPhase.STOPPING) R.string.tv_stopping else R.string.tv_start
        }
        val names = mutableListOf(phaseLabel())
        if (selectedName.isNotBlank()) names.add(getString(R.string.tv_selected_name, selectedName))
        else names.add(getString(R.string.tv_no_selection))
        if (phase in setOf(TvVpnPhase.CONNECTING, TvVpnPhase.CONNECTED, TvVpnPhase.STOPPING) && activeName.isNotBlank() && activeName != selectedName) names.add(getString(R.string.tv_active_name, activeName))
        val group = groups.firstOrNull { it.id == DataStore.selectedGroup }
        actionsAdapter.setItems(listOf(
            TvAction(TOGGLE, getString(label), names.joinToString("\n"), if (command == TvVpnCommand.STOP) R.drawable.ic_service_active else R.drawable.ic_service_idle, command != TvVpnCommand.NONE),
            TvAction(GROUPS, getString(R.string.tv_groups), group?.let { getString(R.string.tv_group_count, it.displayName(), profiles.size) } ?: "", R.drawable.ic_remote_groups),
            TvAction(ADD_PROFILE, getString(R.string.add_profile), getString(R.string.tv_add_profile_hint), R.drawable.ic_action_note_add),
            TvAction(APP_UPDATE, getString(R.string.menu_app_updates), getString(R.string.tv_quality_update_hint), R.drawable.ic_baseline_more_vert_24),
            TvAction(PHONE_MODE, getString(R.string.tv_phone_mode), getString(R.string.tv_phone_mode_hint), R.drawable.ic_remote_phone)
        ), diff)
        val updating = group != null && GroupUpdater.updating.contains(group.id)
        toolsAdapter.setItems(listOf(TvAction(FULL_TOOLS,getString(R.string.tv_all_functions),getString(R.string.tv_all_functions_hint),R.drawable.ic_baseline_more_vert_24))+
            TvFunctionCatalog.entries(requireContext(),DataStore.enableClashAPI,io.nekohasekai.sagernet.ktx.isPlay).map {TvFunctionCard(it.id,it.title)}, diff)
        operationsAdapter.setItems(listOf(
            TvAction(PROFILE_ACTIONS, getString(R.string.tv_actions), getString(R.string.tv_actions_hint), R.drawable.ic_image_edit, DataStore.selectedProxy > 0),
            TvAction(UPDATE, getString(R.string.tv_update_group), getString(if (updating) R.string.tv_updating else if (group?.type != GroupType.SUBSCRIPTION) R.string.tv_not_subscription else R.string.tv_update_group), R.drawable.ic_social_share, group?.type == GroupType.SUBSCRIPTION && !updating),
            TvAction(SUBSCRIPTION_DIAGNOSTICS, getString(R.string.subscription_attempt_title), getString(R.string.subscription_attempt_description), R.drawable.ic_social_share, group?.type == GroupType.SUBSCRIPTION),
            TvAction(TESTS, getString(R.string.tv_tests_title), getString(R.string.tv_tests_hint), R.drawable.ic_remote_groups),
            TvAction(HOME, getString(R.string.tv_home), getString(R.string.tv_home_hint), R.drawable.ic_baseline_more_vert_24),
            TvAction(MORE, getString(R.string.tv_more_import), getString(R.string.tv_more_hint), R.drawable.ic_baseline_more_vert_24),
            TvAction(RESTART_APP, getString(R.string.app_restart), getString(R.string.app_restart_hint), R.drawable.ic_baseline_refresh_24),
            TvAction(CLOSE_APP, getString(R.string.app_close), getString(R.string.app_close_hint), R.drawable.ic_navigation_close)
        ), diff)
        val rows=mutableListOf(ListRow(HeaderItem(ROW_ACTIONS,getString(R.string.tv_row_actions)),actionsAdapter),
            ListRow(HeaderItem(ROW_CONNECTION,getString(R.string.tv_connection_row)),connectionAdapter))
        groupAdapters.keys.retainAll(groups.map {it.id}.toSet())
        for(g in TvGroupRows.orderedGroupIds(groups.map {it.id},DataStore.selectedGroup).map {id->groups.first {it.id==id}}) {
            val groupAdapter=groupAdapters.getOrPut(g.id){ArrayObjectAdapter(ClassPresenterSelector()
                .addClassPresenter(TvGroupCard::class.java,GroupCardPresenter())
                .addClassPresenter(TvProfileCard::class.java,ProfileCardPresenter {openProfileActions(it)})
                .addClassPresenter(TvEmptyHint::class.java,ProfileCardPresenter {openProfileActions(it)}))}
            val cards: List<Any> = listOf(TvGroupCard(g.id,g.displayName(),groupProfiles[g.id].orEmpty().size,g.id==DataStore.selectedGroup))+
                if(groupProfiles[g.id].isNullOrEmpty())listOf(TvEmptyHint(getString(R.string.tv_empty),g.id))
                else groupProfiles[g.id].orEmpty().map {it.copy(selected=it.id==DataStore.selectedProxy,
                    activePhase=if(it.id==activeProfileId()&&phase in setOf(TvVpnPhase.CONNECTING,TvVpnPhase.CONNECTED,TvVpnPhase.STOPPING))phase else null)}
            groupAdapter.setItems(cards,diff)
            rows.add(ListRow(HeaderItem(TvGroupRows.row(g.id),g.displayName()),groupAdapter))
        }
        rows.add(ListRow(HeaderItem(ROW_OPERATIONS,getString(R.string.tv_operations_row)),operationsAdapter))
        rows.add(ListRow(HeaderItem(ROW_TOOLS,getString(R.string.tv_all_functions)),toolsAdapter))
        rowAdapter.setItems(rows,object: DiffCallback<ListRow>() {
            override fun areItemsTheSame(old: ListRow,new: ListRow)=old.headerItem.id==new.headerItem.id
            override fun areContentsTheSame(old: ListRow,new: ListRow)=old.headerItem.name==new.headerItem.name&&old.adapter===new.adapter
        })
        refreshConnectionCards()
    }
    private fun refreshConnectionCards() {
        if (view == null || !::connectionAdapter.isInitialized) return
        val result=healthResult
        val failed=if(phase in setOf(TvVpnPhase.IDLE,TvVpnPhase.STOPPED))io.nekohasekai.sagernet.ui.diagnostics.ConnectionDiagnosticJournal.read(DataStore.selectedProxy)?.takeIf {it.category!=null} else null
        val short=when {
            phase!=TvVpnPhase.CONNECTED&&failed!=null -> getString(R.string.diagnostic_failed_short,TvHealthReason.short(requireContext(),failed.category!!))
            phase!=TvVpnPhase.CONNECTED -> phaseLabel()
            healthJob?.isActive==true -> checkingLabel()
            result==null -> health.takeIf {it==getString(R.string.diagnostic_check_cancelled)}?:getString(R.string.diagnostic_unchecked_short)
            result.available -> getString(R.string.diagnostic_ok_short,result.elapsedMs)
            else -> getString(R.string.diagnostic_failed_short,TvHealthReason.short(requireContext(),result.category!!))
        }
        val statusCards=mutableListOf<Any>(TvConnectionCard(STATUS,
            activeName.ifBlank {selectedName.ifBlank {getString(R.string.tv_no_selection)}},
            getString(R.string.diagnostic_sent,if(statsAt==0L)getString(R.string.diagnostic_stats_waiting) else bytes(stats.txTotal)),
            getString(R.string.diagnostic_received,if(statsAt==0L)getString(R.string.diagnostic_stats_waiting) else bytes(stats.rxTotal)),short),
            TvQualityCard(DIAGNOSTICS,getString(R.string.diagnostic_title),short,
                result?.let {getString(R.string.tv_quality_age,((System.currentTimeMillis()-it.attemptedAt).coerceAtLeast(0)/1000))}
                    ?:getString(R.string.diagnostic_unchecked_short),
                if(statsAt==0L)getString(R.string.diagnostic_stats_waiting) else getString(R.string.tv_quality_rates,bytes(stats.txRateProxy),bytes(stats.rxRateProxy))))
        if (phase == TvVpnPhase.CONNECTED) statusCards.add(TvAction(YOUTUBE, getString(R.string.tv_youtube), getString(R.string.tv_youtube_hint), R.drawable.ic_baseline_more_vert_24))
        connectionAdapter.setItems(statusCards, diff)
    }
    private fun restoreFocus() {
        if (dialog?.isShowing == true || parentFragmentManager.findFragmentByTag("connection_report")!=null || parentFragmentManager.findFragmentByTag("diagnostic_share")!=null || view == null) return
        val items=when {focusedRow>=TvGroupRows.BASE -> groupAdapters[focusedRow-TvGroupRows.BASE]?:actionsAdapter;focusedRow==ROW_TOOLS->toolsAdapter;focusedRow==ROW_OPERATIONS->operationsAdapter;focusedRow==ROW_CONNECTION->connectionAdapter;else->actionsAdapter}
        val ids = (0 until items.size()).map { itemId(items[it]) }
        val index = TvInteractionPolicy.focusIndex(ids, focusedId)
        val row=(0 until rowAdapter.size()).indexOfFirst {(rowAdapter[it] as ListRow).headerItem.id==focusedRow}.coerceAtLeast(0)
        view?.post {
            if (view == null || dialog?.isShowing == true || parentFragmentManager.findFragmentByTag("connection_report")!=null || parentFragmentManager.findFragmentByTag("diagnostic_share")!=null) return@post
            // Browse's three-argument overload unconditionally starts a headers
            // transition and throws when HEADERS_DISABLED. Select through the
            // embedded Rows fragment instead; no headers animation is needed.
            val rows = rowsSupportFragment
            if (rows?.view != null) {
                // Pointer mode owns the viewport until a remote/keyboard key arrives.
                // Delayed DB callbacks must not snap back to the old selected row.
                if (rows.verticalGridView?.isFocusSearchDisabled == true) return@post
                val selection = ListRowPresenter.SelectItemViewHolderTask(index).apply {
                    itemTask = object : Presenter.ViewHolderTask() {
                        override fun run(holder: Presenter.ViewHolder) {
                            if (view == null || dialog?.isShowing == true || parentFragmentManager.findFragmentByTag("connection_report")!=null || parentFragmentManager.findFragmentByTag("diagnostic_share")!=null) return
                            // Selection alone does not grant keyboard focus after a touch-based mode choice.
                            holder.view.isFocusableInTouchMode = true
                            holder.view.requestFocus()
                        }
                    }
                }
                rows.setSelectedPosition(row, false, selection)
            }
            else {
                restoreAfterLoad = true
                setSelectedPosition(row, false)
            }
        }
    }
    private fun show(builder: AlertDialog.Builder, destructive: Boolean = false): AlertDialog {
        dialog?.dismiss()
        return builder.create().also { current ->
            dialog = current
            current.setOnDismissListener { if (dialog === current) { dialog = null; restoreFocus() } }
            if (destructive) current.setOnShowListener { current.getButton(AlertDialog.BUTTON_NEGATIVE)?.requestFocus() }
            current.show()
        }
    }
    private fun handleAction(id: Long) { when (id) {
        TOGGLE -> execute(TvInteractionPolicy.primary(phase, DataStore.selectedProxy, activeProfileId(), serviceReady))
        GROUPS -> chooseGroup()
        ADD_PROFILE -> showImportMethods()
        PROFILE_ACTIONS -> if (DataStore.selectedProxy > 0) openProfileActions(DataStore.selectedProxy) else toast(R.string.tv_choose_profile)
        UPDATE -> updateSubscription()
        SUBSCRIPTION_DIAGNOSTICS -> {
            val record = io.nekohasekai.sagernet.group.SubscriptionUpdateJournal.read(DataStore.selectedGroup)
            val builder = AlertDialog.Builder(requireContext()).setTitle(R.string.subscription_attempt_title)
                .setMessage(io.nekohasekai.sagernet.group.SubscriptionUpdatePresentation.run { record?.let { report(requireContext(), it) } ?: details(requireContext(), null) })
                .setPositiveButton(android.R.string.ok, null)
            if (record != null) builder.setNeutralButton(R.string.subscription_attempt_share) { _, _ ->
                io.nekohasekai.sagernet.group.SubscriptionUpdatePresentation.share(requireContext(), record)
            }
            val current = show(builder)
            io.nekohasekai.sagernet.group.SubscriptionUpdatePresentation.focusClose(current)
        }
        MORE -> showImportMethods()
        APP_UPDATE -> openFullScreen(R.id.nav_app_updates)
        PHONE_MODE -> switchPhoneMode()
        TESTS -> showTests()
        FULL_TOOLS -> showAllFunctions()
        HOME -> openHome()
        YOUTUBE -> openYoutube()
        RESTART_APP -> io.nekohasekai.sagernet.ui.AppLifecycleActions.confirm(requireActivity(), true)
        CLOSE_APP -> io.nekohasekai.sagernet.ui.AppLifecycleActions.confirm(requireActivity(), false)
        STATUS -> showConnectionDiagnostics()
        DIAGNOSTICS -> showConnectionDiagnostics()
    } }
    private fun showConnectionDiagnostics() {
        if(!isAdded||view==null)return
        val profile=if(phase==TvVpnPhase.CONNECTED)activeProfileId() else DataStore.selectedProxy
        val record=io.nekohasekai.sagernet.ui.diagnostics.ConnectionDiagnosticJournal.read(profile)
        val counters=phase==TvVpnPhase.CONNECTED&&statsAt>0L
        val report=io.nekohasekai.sagernet.ui.diagnostics.ConnectionDiagnosticReport.text(requireContext(),record,
            if(counters)stats.txTotal else null,if(counters)stats.rxTotal else null)
        io.nekohasekai.sagernet.ui.diagnostics.DiagnosticDialog.open(parentFragmentManager,report+(extendedReport?.takeIf {it.profile==profile}?.let {"\n\n"+it.text(requireContext())}?:""),phase==TvVpnPhase.CONNECTED,profile>0,
            io.nekohasekai.sagernet.ui.diagnostics.ConnectionDiagnosticReport.offlineText(record,extendedReport?.takeIf {it.profile==profile}))
    }
    fun handlePlayPause() = execute(TvInteractionPolicy.media(phase, DataStore.selectedProxy, serviceReady))
    fun showFocusedActions() {
        if (focusedRow>=TvGroupRows.BASE && focusedId>0) openProfileActions(focusedId)
        else if (DataStore.selectedProxy > 0) openProfileActions(DataStore.selectedProxy)
        else showImportMethods()
    }
    private fun execute(command: TvVpnCommand) {
        if (command == TvVpnCommand.NONE) { toast(if (DataStore.selectedProxy == 0L) R.string.tv_choose_profile else if (phase == TvVpnPhase.CONNECTED && DataStore.selectedProxy == activeProfileId()) R.string.tv_connected else R.string.tv_wait); return }
        val now = SystemClock.elapsedRealtime()
        if (now - lastCommand < 750) return
        lastCommand = now
        when (command) {
            TvVpnCommand.START -> connect.launch(null)
            TvVpnCommand.STOP -> SagerNet.stopService()
            TvVpnCommand.RELOAD -> SagerNet.reloadService()
            else -> Unit
        }
    }
    private fun selectProfile(id: Long) {
        if (!TvInteractionPolicy.canSelect(phase)) { toast(R.string.tv_wait); return }
        val card=groupProfiles.values.asSequence().flatten().firstOrNull {it.id==id} ?: return
        val old = DataStore.selectedProxy
        DataStore.selectedProxy = id
        DataStore.selectedGroup=card.groupId;profiles=groupProfiles[card.groupId].orEmpty()
        selectedName=card.name
        refreshCards()
        launchWork { withContext(Dispatchers.IO) { ProfileManager.postUpdate(old, true); ProfileManager.postUpdate(id, true) } }
    }
    private fun selectGroup(id: Long) {
        if(groups.none {it.id==id})return
        DataStore.selectedGroup=id;profiles=groupProfiles[id].orEmpty();refreshCards()
        // Browsing another group never reloads or switches the active VPN.
    }
    private fun chooseGroup() {
        if (groups.isEmpty()) { requestSnapshot(); return }
        val choices = groups.toList()
        val checked = choices.indexOfFirst { it.id == DataStore.selectedGroup }
        show(AlertDialog.Builder(requireContext()).setTitle(R.string.tv_groups)
            .setSingleChoiceItems(choices.map { it.displayName() }.toTypedArray(), checked) { current, index ->
                selectGroup(choices[index].id);current.dismiss()
            }.setNegativeButton(android.R.string.cancel, null))
    }
    private fun updateSubscription() {
        val group = groups.firstOrNull { it.id == DataStore.selectedGroup }
        if (group?.type != GroupType.SUBSCRIPTION) { toast(R.string.tv_not_subscription); return }
        if (GroupUpdater.updating.contains(group.id) || subscriptionJob?.isActive == true) { toast(R.string.tv_updating); return }
        updateDeclined = false
        updateFailureShown = false
        subscriptionJob = viewLifecycleOwner.lifecycleScope.launch {
            try {
                val result = withContext(Dispatchers.IO) { GroupUpdater.executeUpdate(group, true, subscriptionInterface) }
                if (!updateDeclined) {
                    if (result) toast(R.string.tv_updated)
                    else if (!updateFailureShown) showSubscriptionFailure(null)
                }
            } catch (cancelled: CancellationException) { throw cancelled }
            catch (error: Exception) { showSubscriptionFailure(error.message) }
            finally { requestSnapshot() }
        }
    }
    private fun showSubscriptionFailure(message: String?) {
        if (!isAdded || view == null || !viewLifecycleOwner.lifecycle.currentState.isAtLeast(androidx.lifecycle.Lifecycle.State.STARTED)) return
        updateFailureShown = true
        val current = show(AlertDialog.Builder(requireContext())
            .setTitle(R.string.subscription_failure_title)
            .setMessage(io.nekohasekai.sagernet.group.SubscriptionFailurePresentation.message(requireContext(), message))
            .setPositiveButton(android.R.string.ok, null))
        current.getButton(AlertDialog.BUTTON_POSITIVE)?.requestFocus()
    }
    private val subscriptionInterface = object : GroupManager.Interface {
        override suspend fun confirm(message: String): Boolean = withContext(Dispatchers.Main) {
            suspendCancellableCoroutine { continuation ->
                val current = show(AlertDialog.Builder(requireContext()).setTitle(R.string.confirm).setMessage(message)
                    .setPositiveButton(R.string.yes) { _, _ -> if (continuation.isActive) continuation.resume(true) }
                    .setNegativeButton(R.string.no) { _, _ -> updateDeclined = true; if (continuation.isActive) continuation.resume(false) }
                    .setOnCancelListener { updateDeclined = true; if (continuation.isActive) continuation.resume(false) }, true)
                continuation.invokeOnCancellation { current.window?.decorView?.post { current.dismiss() } }
            }
        }
        override suspend fun alert(message: String) { withContext(Dispatchers.Main) { toast(message) } }
        override suspend fun onUpdateSuccess(group: ProxyGroup, changed: Int, added: List<String>, updated: Map<String, String>, deleted: List<String>, duplicate: List<String>, byUser: Boolean) { requestSnapshot() }
        override suspend fun onUpdateFailure(group: ProxyGroup, message: String) { withContext(Dispatchers.Main) { showSubscriptionFailure(message) } }
    }
    private fun openProfileActions(id: Long) = launchWork {
        if (!serviceReady) { toast(R.string.tv_wait); return@launchWork }
        val entity = withContext(Dispatchers.IO) { ProfileManager.getProfile(id) } ?: return@launchWork
        val options = mutableListOf<Pair<Int, () -> Unit>>()
        if (id == activeProfileId() && phase in setOf(TvVpnPhase.CONNECTING, TvVpnPhase.CONNECTED)) options.add(R.string.tv_stop to { execute(TvVpnCommand.STOP) })
        options.add(R.string.tv_select_only to { selectProfile(id) })
        options.add(R.string.tv_connect_profile to {
            if (TvInteractionPolicy.canSelect(phase)) { selectProfile(id); execute(TvInteractionPolicy.connectOnly(phase, id, activeProfileId(), serviceReady)) } else toast(R.string.tv_wait)
        })
        options.add(R.string.tv_details to { show(AlertDialog.Builder(requireContext()).setTitle(entity.displayName()).setMessage("${entity.displayType()}\n${entity.requireBean().serverAddress}:${entity.requireBean().serverPort}").setPositiveButton(android.R.string.ok, null)) })
        options.add(R.string.edit to {
            if (TvInteractionPolicy.canMutate(id, activeProfileId(), phase)) {
                val subscription = groups.firstOrNull { it.id == entity.groupId }?.type == GroupType.SUBSCRIPTION
                try { startActivity(entity.settingIntent(requireContext(), subscription)) } catch (_: Exception) { toast(R.string.tv_operation_failed) }
            } else toast(R.string.tv_stop_before_edit)
        })
        options.add(R.string.tv_share_qr to { launchWork {
            val link = withContext(Dispatchers.IO) { try { entity.toStdLink() } catch (_: Exception) { "" } }
            if (link.isBlank()) toast(R.string.tv_share_unsupported)
            else QRCodeDialog(link, entity.displayName().orEmpty()).show(parentFragmentManager, "tv_profile_qr")
        } })
        options.add(R.string.delete to {
            if (!TvInteractionPolicy.canMutate(id, activeProfileId(), phase)) toast(R.string.tv_stop_before_edit)
            else show(AlertDialog.Builder(requireContext()).setTitle(R.string.delete).setMessage(getString(R.string.tv_delete_confirm, entity.displayName()))
                .setNegativeButton(android.R.string.cancel, null).setPositiveButton(R.string.yes) { _, _ -> launchWork {
                    if (!TvInteractionPolicy.canMutate(id, activeProfileId(), phase)) { toast(R.string.tv_stop_before_edit); return@launchWork }
                    withContext(Dispatchers.IO) { ProfileManager.deleteProfile(entity.groupId, id) }; requestSnapshot()
                } }, true)
        })
        options.add(R.string.tv_move_up to { moveProfile(entity, -1) })
        options.add(R.string.tv_move_down to { moveProfile(entity, 1) })
        show(AlertDialog.Builder(requireContext()).setTitle(entity.displayName()).setItems(options.map { getString(it.first) }.toTypedArray()) { _, index -> try { options[index].second() } catch (_: Exception) { toast(R.string.tv_operation_failed) } })
    }
    private fun moveProfile(entity: ProxyEntity, offset: Int) = launchWork {
        val group = groups.firstOrNull { it.id == entity.groupId }
        if (group?.order != GroupOrder.ORIGIN) { toast(R.string.tv_order_unavailable); return@launchWork }
        withContext(Dispatchers.IO) {
            val ordered = SagerDatabase.proxyDao.getByGroup(entity.groupId)
            val index = ordered.indexOfFirst { it.id == entity.id }; val target = index + offset
            if (index >= 0 && target in ordered.indices) {
                val first = ordered[index]; val second = ordered[target]; val order = first.userOrder
                first.userOrder = second.userOrder; second.userOrder = order
                ProfileManager.updateProfile(listOf(first, second)); GroupManager.postReload(entity.groupId)
            }
        }
        requestSnapshot()
    }
    private fun showPhoneImport() { parentFragmentManager.beginTransaction().replace(R.id.tv_container, QrCodeTransferFragment()).addToBackStack("phone_import").commit() }
    private fun showSendMethods() {
        val options = arrayOf(R.string.tv_send_group_qr, R.string.tv_send_profile_qr)
        show(AlertDialog.Builder(requireContext()).setTitle(R.string.tv_send_choice)
            .setItems(options.map { getString(it) }.toTypedArray()) { _, index ->
                if (index == 0) {
                    parentFragmentManager.beginTransaction().replace(R.id.tv_container, QrCodeTransferFragment().apply {
                        arguments = Bundle().apply { putBoolean("outgoing", true); putLong("group_id", DataStore.currentGroupId()) }
                    }).addToBackStack("send_group").commit()
                } else if (DataStore.selectedProxy <= 0) toast(R.string.tv_choose_profile)
                else launchWork {
                    val entity = withContext(Dispatchers.IO) { ProfileManager.getProfile(DataStore.selectedProxy) }
                    val link = withContext(Dispatchers.IO) { runCatching { entity?.toStdLink().orEmpty() }.getOrDefault("") }
                    if (link.isBlank() || entity?.haveLink() != true) toast(R.string.tv_share_unsupported)
                    else QRCodeDialog(link, entity.displayName().orEmpty()).show(parentFragmentManager, "send_profile_qr")
                }
            }.setNegativeButton(android.R.string.cancel, null))
    }
    private fun showImportMethods() {
        val entries = TvProfileAddCatalog.entries(requireContext())
        show(AlertDialog.Builder(requireContext()).setTitle(R.string.add_profile)
            .setItems(entries.map { it.title }.toTypedArray()) { _, index ->
                when (entries[index].id) {
                    TvProfileAddCatalog.PHONE -> showPhoneImport()
                    TvProfileAddCatalog.SEND -> showSendMethods()
                    TvProfileAddCatalog.URL -> showUrlImport()
                    R.id.action_import_clipboard -> {
                        val text = SagerNet.getClipboardText()
                        if (text.isBlank()) toast(R.string.tv_clipboard_empty) else importText(text)
                    }
                    R.id.action_import_file -> importFile.launch("*/*")
                    R.id.action_gateway_proxy -> startActivity(Intent(requireContext(), MainActivity::class.java)
                        .putExtra("tv_tools", true).putExtra("tv_destination", R.id.nav_lan_discovery).putExtra("gateway_quick", true))
                    R.id.action_lan_discovery -> openFullScreen(R.id.nav_lan_discovery)
                    TvProfileAddCatalog.MANUAL -> showManualEditor()
                    R.id.action_scan_qr_code -> parentFragmentManager.beginTransaction()
                        .replace(R.id.tv_container, TvScannerFragment()).addToBackStack("qr_scan").commit()
                }
            }.setNegativeButton(android.R.string.cancel, null))
    }
    private fun importText(text: String) = launchWork {
        val count = withContext(Dispatchers.IO) { TvProfileImporter.importProfiles(text) }
        toast(getString(R.string.tv_import_count, count)); requestSnapshot()
    }
    private fun showUrlImport() {
        val input = EditText(requireContext()).apply { hint = getString(R.string.tv_url_hint); textSize = 20f; inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_URI; val density = resources.displayMetrics.density; setPadding((16 * density).toInt(), (12 * density).toInt(), (16 * density).toInt(), (12 * density).toInt()) }
        show(AlertDialog.Builder(requireContext()).setTitle(R.string.tv_url).setView(input).setPositiveButton(R.string.tv_import) { _, _ ->
            val text = input.text.toString().trim()
            if (text.isBlank()) { toast(R.string.tv_url_required); return@setPositiveButton }
            val uri = Uri.parse(text)
            when (uri.scheme) {
                "sn", "clash" -> startActivity(Intent(requireContext(), MainActivity::class.java).apply { action = Intent.ACTION_VIEW; data = uri; putExtra("force_phone_mode", true) })
                "http", "https" -> launchWork { val count = withContext(Dispatchers.IO) { TvProfileImporter.importSubscription(text) }; toast(getString(R.string.tv_import_count, count)); requestSnapshot() }
                else -> importText(text)
            }
        }.setNeutralButton(R.string.tv_import_phone) { _, _ -> showPhoneImport() }.setNegativeButton(android.R.string.cancel, null))
    }
    private fun showManualEditor() {
        val entries = io.nekohasekai.sagernet.ui.ProfileCreationActions.manualEntries(requireContext())
        show(AlertDialog.Builder(requireContext()).setTitle(R.string.add_profile_methods_manual_settings)
            .setItems(entries.map { it.title }.toTypedArray()) { _, index ->
                val intent = requireNotNull(io.nekohasekai.sagernet.ui.ProfileCreationActions.intent(requireContext(), entries[index].id))
                DataStore.selectedGroup = DataStore.selectedGroupForImport()
                startActivity(intent)
            }.setNegativeButton(android.R.string.cancel, null))
    }
    private fun switchPhoneMode() {
        show(AlertDialog.Builder(requireContext()).setTitle(R.string.tv_phone_mode).setMessage(R.string.tv_phone_confirm)
            .setPositiveButton(R.string.yes) { _, _ ->
                TvUiPreferences.phoneMode = true
                startActivity(Intent(requireContext(), MainActivity::class.java).apply { addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); putExtra("force_phone_mode", true) })
                requireActivity().finish()
            }.setNegativeButton(android.R.string.cancel, null))
    }
    internal fun updateTraffic(value: io.nekohasekai.sagernet.aidl.SpeedDisplayData) {
        stats = value.copy(); statsAt = SystemClock.elapsedRealtime(); refreshConnectionCards();maybeAutoCheck()
    }
    private fun syncHealthSession() {
        val changed=healthSession.sync(serviceReady&&phase==TvVpnPhase.CONNECTED,activeProfileId(),
            "$serviceEpoch:${SagerNet.underlyingNetwork}")
        if(changed){healthJob?.cancel();extendedJob?.cancel();extendedReport=null;health="";healthResult=null;refreshConnectionCards()}
    }
    private fun maybeAutoCheck() {
        if(view==null||!isResumed)return
        syncHealthSession()
        if(phase==TvVpnPhase.CONNECTED&&serviceReady&&healthJob?.isActive!=true) testActiveConnection(true)
    }
    private fun runExtendedDiagnostics() {
        if(view==null||!isResumed||extendedJob?.isActive==true||DataStore.runningTest){toast(R.string.tv_wait);return}
        val id=if(phase==TvVpnPhase.CONNECTED)activeProfileId() else DataStore.selectedProxy
        if(id<=0){toast(R.string.tv_choose_profile);return}
        DataStore.runningTest=true
        val progress=show(AlertDialog.Builder(requireContext()).setTitle(R.string.diagnostic_extended)
            .setMessage(R.string.diagnostic_extended_scope).setNegativeButton(android.R.string.cancel){_,_->extendedJob?.cancel()})
        progress.setOnCancelListener {extendedJob?.cancel()}
        progress.getButton(AlertDialog.BUTTON_NEGATIVE)?.apply {isFocusableInTouchMode=true;requestFocus()}
        extendedJob=viewLifecycleOwner.lifecycleScope.launch {
            val start=SystemClock.elapsedRealtime();var stage="CONFIG_COMPATIBILITY"
            val ticker=launch {while(isActive){progress.setMessage(getString(R.string.diagnostic_extended_progress,io.nekohasekai.sagernet.ui.diagnostics.DiagnosticPresentation.stage(requireContext(),stage),(SystemClock.elapsedRealtime()-start)/1000)+"\n\n"+getString(R.string.diagnostic_extended_scope));delay(250)}}
            try {
                val entity=withContext(Dispatchers.IO){ProfileManager.getProfile(id)}?:return@launch
                val fingerprint=io.nekohasekai.sagernet.fmt.KryoConverters.serialize(entity.requireBean())
                val report=extendedProbe(requireContext().applicationContext,entity){stage=it}
                ensureActive()
                val fresh=withContext(Dispatchers.IO){ProfileManager.getProfile(id)}
                if(fresh==null||!fingerprint.contentEquals(io.nekohasekai.sagernet.fmt.KryoConverters.serialize(fresh.requireBean()))||
                    id!=(if(phase==TvVpnPhase.CONNECTED)activeProfileId() else DataStore.selectedProxy)){
                    toast(R.string.tv_quality_changed);return@launch
                }
                extendedReport=report;progress.dismiss();showConnectionDiagnostics()
            } catch(e: CancellationException){if(e is TimeoutCancellationException)toast(R.string.tv_quality_incomplete) else throw e}
            catch(_: Exception){toast(R.string.tv_quality_incomplete)}
            finally {ticker.cancel();DataStore.runningTest=false;if(progress.isShowing)progress.dismiss()}
        }
    }

    private fun testActiveConnection(automatic: Boolean=false) {
        if(view==null||!isResumed||phase!=TvVpnPhase.CONNECTED||healthJob?.isActive==true) {
            if(!automatic)toast(R.string.tv_wait);return
        }
        val service=connection.service?:run {if(!automatic)toast(R.string.tv_unavailable);return}
        syncHealthSession()
        val token=healthSession.begin(force=!automatic)?:return
        val testedProfile=token.profile
        health=getString(R.string.tv_health_checking);healthResult=null;checkStartedAt=SystemClock.elapsedRealtime()
        if(!automatic) {
            val builder=AlertDialog.Builder(requireContext())
            val themed=builder.context
            checkProgress=android.widget.TextView(themed).apply {id=R.id.diagnostic_check_progress_text;text=checkingLabel();textSize=18f;setPadding(24,16,24,16)}
            val body=android.widget.LinearLayout(themed).apply {
                orientation=android.widget.LinearLayout.VERTICAL
                addView(android.widget.ProgressBar(themed));addView(checkProgress)
            }
            checkDialog=show(builder.setTitle(R.string.network_test_title).setView(body)
                .setNegativeButton(android.R.string.cancel) {_,_->healthJob?.cancel()})
            checkDialog?.setOnCancelListener {healthJob?.cancel()}
            checkDialog?.getButton(AlertDialog.BUTTON_NEGATIVE)?.apply {isFocusableInTouchMode=true;requestFocus()}
        }
        healthJob=viewLifecycleOwner.lifecycleScope.launch {
            val ticker=launch {
                while(isActive) {checkProgress?.text=checkingLabel();refreshConnectionCards();delay(250)}
            }
            try {
                var result=io.nekohasekai.sagernet.group.BoundedConnectionCheck.shared.check(testedProfile) {
                    io.nekohasekai.sagernet.group.ConnectionTestResult.decode(service.testConnectionReport())
                        ?:io.nekohasekai.sagernet.group.ConnectionTestResult(testedProfile,System.currentTimeMillis(),-1,
                            io.nekohasekai.sagernet.group.SubscriptionFailureCategory.UNKNOWN)
                }
                ensureActive()
                if(result.profileId!=testedProfile)result=io.nekohasekai.sagernet.group.ConnectionTestResult(testedProfile,System.currentTimeMillis(),-1,
                    io.nekohasekai.sagernet.group.SubscriptionFailureCategory.CHANGED)
                if(phase==TvVpnPhase.CONNECTED&&activeProfileId()==testedProfile&&healthSession.finish(token)) {
                    healthResult=result
                    saveCheckResult(result)
                    io.nekohasekai.sagernet.group.ConnectionTestPresentation.save(result)
                    health=io.nekohasekai.sagernet.group.ConnectionTestPresentation.summary(requireContext(),result)
                    // Automatic checks are inline: never steal focus or require an error OK click.
                    if(!automatic&&isResumed) {
                        checkDialog?.dismiss();checkDialog=null
                        showConnectionDiagnostics()
                    }
                }
            } catch(cancelled: CancellationException) {if(healthSession.finish(token))health=getString(R.string.diagnostic_check_cancelled);throw cancelled}
            catch(error: Exception) {
                if(phase==TvVpnPhase.CONNECTED&&activeProfileId()==testedProfile&&healthSession.finish(token)) {
                    val result=io.nekohasekai.sagernet.group.ConnectionTestResult(testedProfile,System.currentTimeMillis(),-1,
                        io.nekohasekai.sagernet.group.SubscriptionFailureCategory.fromThrowable(error))
                    healthResult=result;saveCheckResult(result);io.nekohasekai.sagernet.group.ConnectionTestPresentation.save(result)
                    health=io.nekohasekai.sagernet.group.ConnectionTestPresentation.summary(requireContext(),result)
                }
            } finally {
                ticker.cancel();checkStartedAt=0L;checkProgress=null
                checkDialog?.dismiss();checkDialog=null
                if(view!=null&&isAdded)view?.post {if(view!=null&&isAdded)refreshConnectionCards()}
            }
        }
        refreshConnectionCards()
    }
    private fun checkingLabel()=getString(R.string.diagnostic_check_progress,
        ((SystemClock.elapsedRealtime()-checkStartedAt).coerceAtLeast(0)/1000).toInt(),
        (io.nekohasekai.sagernet.group.BoundedConnectionCheck.LIMIT_MS/1000).toInt())
    private fun saveCheckResult(result: io.nekohasekai.sagernet.group.ConnectionTestResult) {
        val previous=io.nekohasekai.sagernet.ui.diagnostics.ConnectionDiagnosticJournal.read(result.profileId)
        val record=previous?.copy(at=result.attemptedAt,stage=io.nekohasekai.sagernet.ui.diagnostics.DiagnosticStage.URL_TEST,
            category=result.category,elapsed=result.elapsedMs)
            ?:io.nekohasekai.sagernet.ui.diagnostics.ConnectionDiagnostic(result.profileId,result.attemptedAt,
                io.nekohasekai.sagernet.ui.diagnostics.DiagnosticStage.URL_TEST,result.category,elapsed=result.elapsedMs)
        io.nekohasekai.sagernet.ui.diagnostics.ConnectionDiagnosticJournal.write(record)
    }
    private fun bytes(value: Long): String = android.text.format.Formatter.formatFileSize(requireContext(), value.coerceAtLeast(0))
    internal fun openHome() {
        try { startActivity(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
        catch (_: android.content.ActivityNotFoundException) { requireActivity().moveTaskToBack(true) }
    }
    internal fun openYoutube() {
        val pm = requireContext().packageManager
        val intent = listOf("com.google.android.youtube.tv", "com.google.android.youtube")
            .firstNotNullOfOrNull { pm.getLeanbackLaunchIntentForPackage(it) ?: pm.getLaunchIntentForPackage(it) }
        if (intent == null) { toast(R.string.tv_youtube_missing); return }
        try { startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
        catch (_: android.content.ActivityNotFoundException) { toast(R.string.tv_youtube_missing) }
    }
    private fun openFullScreen(destination: Int) {
        startActivity(Intent(requireContext(), MainActivity::class.java).putExtra("tv_tools", true).putExtra("tv_destination", destination))
    }
    private fun showAllFunctions() {
        // Reuse upstream screens, including their complete menus. Do not maintain a lossy copy.
        val entries = TvFunctionCatalog.entries(requireContext(), DataStore.enableClashAPI, io.nekohasekai.sagernet.ktx.isPlay)
        show(AlertDialog.Builder(requireContext()).setTitle(R.string.tv_all_functions)
            .setItems(entries.map { it.title }.toTypedArray()) { _, index -> openFullScreen(entries[index].id) }
            .setNegativeButton(android.R.string.cancel, null))
    }
    private fun showTests() {
        val options = listOf(R.string.tv_test_tcp, R.string.tv_test_url, R.string.tv_test_clear, R.string.tv_test_sort, R.string.tv_test_advanced)
        show(AlertDialog.Builder(requireContext()).setTitle(R.string.tv_tests_title).setItems(options.map { getString(it) }.toTypedArray()) { _, index ->
            when (index) {
                0, 1 -> runGroupTest(index == 1)
                2 -> launchWork {
                    if (DataStore.runningTest) { toast(R.string.tv_wait); return@launchWork }
                    withContext(Dispatchers.IO) { SagerDatabase.proxyDao.getByGroup(DataStore.selectedGroup).forEach { it.status = 0; it.ping = 0; it.error = null; ProfileManager.updateProfile(it) } }; requestSnapshot()
                }
                3 -> launchWork { withContext(Dispatchers.IO) { val group = DataStore.currentGroup(); group.order = GroupOrder.BY_DELAY; GroupManager.updateGroup(group) }; requestSnapshot() }
                else -> openFullScreen(R.id.nav_configuration)
            }
        }.setNegativeButton(android.R.string.cancel, null))
    }
    /** Explicit group action. Temporary test cores do not change the running VPN.
     * A single isolated worker prevents abandoned native calls from spawning more tests.
     * Cancellation prevents the next probe; a blocked native call may finish later.
     */
    private class GroupQualityFailure(val category: io.nekohasekai.sagernet.group.SubscriptionFailureCategory): Exception()
    private fun showGroupQualityFailure(category: io.nekohasekai.sagernet.group.SubscriptionFailureCategory) {
        if(!isAdded||view==null)return
        // Only canonical codes; never log provider names, endpoints or exception text.
        android.util.Log.i("TunXBoxGroupQuality","Group stopped: NET_${category.name}; ranking not saved")
        show(AlertDialog.Builder(requireContext()).setTitle(R.string.tv_tests_title)
            .setMessage(getString(if(category==io.nekohasekai.sagernet.group.SubscriptionFailureCategory.CHANGED)R.string.tv_quality_changed else R.string.tv_quality_incomplete)+"\nNET_${category.name}")
            .setPositiveButton(android.R.string.ok,null))
    }
    private fun runGroupQuality(groupId: Long) {
        if(DataStore.runningTest){toast(R.string.tv_wait);return}
        DataStore.runningTest=true
        val cancelled=java.util.concurrent.atomic.AtomicBoolean(false)
        val progress=show(AlertDialog.Builder(requireContext()).setTitle(R.string.tv_quality_title)
            .setMessage(R.string.tv_test_starting).setNegativeButton(android.R.string.cancel){_,_->cancelled.set(true);testJob?.cancel()})
        progress.setOnCancelListener {cancelled.set(true);testJob?.cancel()}
        testJob=viewLifecycleOwner.lifecycleScope.launch {
            val started=SystemClock.elapsedRealtime()
            // Initial callback registration is asynchronous; null -> initial physical
            // network must not be mistaken for a handover after probes have started.
            var network: android.net.Network?=null
            val results=mutableListOf<TvQualityResult>()
            var ticker: Job?=null
            var step=""
            try {
                val operationJob=currentCoroutineContext()[Job]!!
                step="NETWORK_INITIALIZATION"
                network=withTimeoutOrNull(2000L) {
                    while(SagerNet.underlyingNetwork==null)delay(25)
                    SagerNet.underlyingNetwork
                }?:SagerNet.underlyingNetwork
                val original=withContext(Dispatchers.IO){SagerDatabase.groupDao.getById(groupId)}?:return@launch
                val targets=withContext(Dispatchers.IO){SagerDatabase.proxyDao.getByGroup(groupId)}
                if(targets.isEmpty()){toast(R.string.tv_empty);return@launch}
                // These fingerprints stay in memory, never in reports or logs.
                fun fingerprint(profile: ProxyEntity)=java.security.MessageDigest.getInstance("SHA-256")
                    .digest(byteArrayOf(profile.type.toByte())+io.nekohasekai.sagernet.fmt.KryoConverters.serialize(profile.requireBean())).toList()
                val fingerprints=targets.associate {it.id to fingerprint(it)}
                ticker=launch {while(isActive){
                    progress.setMessage(getString(R.string.tv_quality_progress,results.size,targets.size,
                        (SystemClock.elapsedRealtime()-started)/1000)+"\n"+step)
                    delay(250)
                }}
                withTimeout(TvGroupQuality.GROUP_LIMIT_MS) {
                    for(profile in targets) {
                        ensureActive()
                        if(SagerNet.underlyingNetwork!=network)throw GroupQualityFailure(io.nekohasekai.sagernet.group.SubscriptionFailureCategory.CHANGED)
                        var quality: TvQualityResult?=null
                        val outcome=(qualityWorkerOverride ?: qualityWorker).check(profile.id,TvGroupQuality.PROFILE_LIMIT_MS) {
                            runBlocking {
                                var tcp: Int?=null
                                if(profile.requireBean().canTCPing()&&!cancelled.get()) {
                                    withContext(Dispatchers.Main){step=getString(R.string.tv_test_tcp)}
                                    tcp=try {qualityTcpProbe(profile)}catch(_: Exception){null}
                                }
                                val samples=mutableListOf<Int>()
                                var failure: io.nekohasekai.sagernet.group.SubscriptionFailureCategory?=null
                                repeat(TvGroupQuality.SAMPLES){index->
                                    if(cancelled.get())throw CancellationException()
                                    withContext(Dispatchers.Main){step=getString(R.string.tv_quality_sample,index+1,TvGroupQuality.SAMPLES)}
                                    try {samples.add(qualityUrlProbe(profile))}
                                    catch(error: Exception){
                                        if(error is CancellationException)throw error
                                        samples.add(-1);failure=io.nekohasekai.sagernet.group.SubscriptionFailureCategory.fromThrowable(error)
                                    }
                                }
                                quality=TvQualityResult(profile.id,samples,tcp,failure,System.currentTimeMillis())
                                io.nekohasekai.sagernet.group.ConnectionTestResult(profile.id,System.currentTimeMillis(),0)
                            }
                        }
                        ensureActive()
                        // No partial sort when the worker timed out or was still occupied.
                        if(!outcome.available||quality==null)throw GroupQualityFailure(outcome.category?:io.nekohasekai.sagernet.group.SubscriptionFailureCategory.CORE)
                        results.add(quality!!)
                    }
                }
                ensureActive()
                val ranked=TvGroupQuality.ranked(results)
                val committed=withContext(Dispatchers.IO) {
                    var saved=false
                    SagerDatabase.instance.runInTransaction {
                        val current=SagerDatabase.groupDao.getById(groupId)
                        val fresh=SagerDatabase.proxyDao.getByGroup(groupId)
                        if(!operationJob.isActive||cancelled.get()||SagerNet.underlyingNetwork!=network||current!=original||
                            fresh.map {it.id}!=targets.map {it.id}||fresh.any {fingerprints[it.id]!=fingerprint(it)||it.userOrder!=targets.first {t->t.id==it.id}.userOrder})return@runInTransaction
                        ranked.forEachIndexed {index,result->
                            val entity=fresh.first {it.id==result.id}
                            entity.userOrder=(index+1).toLong();entity.status=if(result.successes>0)1 else 2
                            entity.ping=if(result.successes>0)result.medianMs else 0
                            entity.error=if(result.successes==TvGroupQuality.SAMPLES)null else result.failure?.code
                            SagerDatabase.proxyDao.updateProxy(entity)
                        }
                        current!!.order=GroupOrder.ORIGIN;SagerDatabase.groupDao.updateGroup(current);saved=true
                    }
                    if(saved)GroupManager.postReload(groupId)
                    saved
                }
                if(!committed){showGroupQualityFailure(io.nekohasekai.sagernet.group.SubscriptionFailureCategory.CHANGED);return@launch}
                qualityByProfile=qualityByProfile.filterKeys {id->targets.none {it.id==id}}+results.associateBy {it.id}
                progress.dismiss();requestSnapshot()
                val report=ranked.joinToString("\n\n") {r->
                    val name=targets.first {it.id==r.id}.displayName()
                    name+"\n"+getString(R.string.tv_quality_profile,r.successes,TvGroupQuality.SAMPLES)+
                        (if(r.successes>0)" · ${r.medianMs} ms" else " · "+TvHealthReason.short(requireContext(),r.failure?:io.nekohasekai.sagernet.group.SubscriptionFailureCategory.UNKNOWN))+"\n"+
                        getString(R.string.tv_quality_tcp,r.tcpMs?.let {"$it ms"}?:getString(R.string.tv_test_unavailable))+
                        (r.spreadMs?.let {" · "+getString(R.string.tv_quality_spread,it)}?:"")
                }
                show(AlertDialog.Builder(requireContext()).setTitle(R.string.tv_test_complete)
                    .setMessage(report+"\n\n"+getString(R.string.tv_quality_scope)).setPositiveButton(android.R.string.ok,null))
            } catch(error: CancellationException){
                if(error is TimeoutCancellationException)showGroupQualityFailure(io.nekohasekai.sagernet.group.SubscriptionFailureCategory.TIMEOUT) else throw error
            } catch(error: Exception){showGroupQualityFailure(if(error is GroupQualityFailure)error.category else io.nekohasekai.sagernet.group.SubscriptionFailureCategory.fromThrowable(error))}
            finally {cancelled.set(true);ticker?.cancel();DataStore.runningTest=false;if(progress.isShowing)progress.dismiss()}
        }
    }

    private fun runGroupTest(url: Boolean) {
        if (DataStore.runningTest) { toast(R.string.tv_wait); return }
        val groupId = DataStore.selectedGroup
        DataStore.runningTest = true
        val progress = show(AlertDialog.Builder(requireContext()).setTitle(if (url) R.string.tv_test_url else R.string.tv_test_tcp)
            .setMessage(R.string.tv_test_starting).setNegativeButton(android.R.string.cancel) { _, _ -> testJob?.cancel() })
        progress.setOnCancelListener { testJob?.cancel() }
        testJob = viewLifecycleOwner.lifecycleScope.launch {
            var done = 0; var passed = 0; var failed = 0
            try {
                val targets = withContext(Dispatchers.IO) { SagerDatabase.proxyDao.getByGroup(groupId).filter { url || it.requireBean().canTCPing() } }
                val semaphore = kotlinx.coroutines.sync.Semaphore(DataStore.connectionTestConcurrent.coerceIn(1, 16))
                coroutineScope {
                    targets.map { profile -> launch {
                        semaphore.acquire()
                        try {
                            profile.error = null
                            try { profile.ping = if (url) TvDiagnostics.url(profile) else TvDiagnostics.tcp(profile); ensureActive(); profile.status = 1; passed++ }
                            catch (cancelled: CancellationException) { throw cancelled }
                            catch (_: Exception) { profile.status = 2; profile.ping = 0; profile.error = getString(R.string.tv_test_unavailable); failed++ }
                            withContext(Dispatchers.IO) { ProfileManager.updateProfile(profile) }
                            done++
                            progress.setMessage(getString(R.string.tv_test_progress, done, targets.size, passed, failed))
                        } finally { semaphore.release() }
                    } }.joinAll()
                }
                progress.dismiss(); requestSnapshot()
                show(AlertDialog.Builder(requireContext()).setTitle(R.string.tv_test_complete)
                    .setMessage(getString(R.string.tv_test_progress, done, targets.size, passed, failed) + "\n\n" + getString(R.string.tv_test_scope))
                    .setPositiveButton(android.R.string.ok, null))
            } catch (cancelled: CancellationException) { throw cancelled }
            catch (_: Exception) { toast(R.string.tv_operation_failed) }
            finally { DataStore.runningTest = false; if (progress.isShowing) progress.dismiss() }
        }
    }

}

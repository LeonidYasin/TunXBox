package io.nekohasekai.sagernet.ui.lan

import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.core.view.doOnLayout
import io.nekohasekai.sagernet.R
import io.nekohasekai.sagernet.GroupType
import io.nekohasekai.sagernet.database.SagerDatabase
import io.nekohasekai.sagernet.ui.ToolbarFragment
import kotlinx.coroutines.*

/** Shared phone/remote screen. Opening it NEVER scans or changes the running VPN. */
class LanDiscoveryFragment : ToolbarFragment(R.layout.layout_lan_discovery) {
    private lateinit var environment: LanEnvironment
    private var selected: LanNetwork? = null
    private var scan: Job? = null
    private var registered = false
    private var dialog: AlertDialog? = null
    private lateinit var scopeText: TextView
    private lateinit var status: TextView
    private lateinit var ports: EditText
    private lateinit var consent: CheckBox
    private lateinit var scanButton: Button
    private lateinit var cancelButton: Button
    private lateinit var networkButton: Button
    private lateinit var results: LinearLayout
    private val callback = object : ConnectivityManager.NetworkCallback() {
        override fun onLost(network: Network) { view?.post { if (selected?.network == network) invalidateNetwork() } }
        override fun onLinkPropertiesChanged(network: Network, linkProperties: LinkProperties) = changed()
        override fun onCapabilitiesChanged(network: Network, caps: NetworkCapabilities) = changed()
        private fun changed() { view?.post { selected?.let { if (!environment.current(it)) invalidateNetwork() } } }
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolbar.setTitle(R.string.lan_title)
        view.findViewById<LinearLayout>(R.id.lan_content).doOnLayout { content ->
            content.layoutParams = FrameLayout.LayoutParams(minOf(view.width, (840 * resources.displayMetrics.density).toInt()),
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT, android.view.Gravity.CENTER_HORIZONTAL)
        }
        environment = LanEnvironment(requireContext())
        scopeText = view.findViewById(R.id.lan_scope); status = view.findViewById(R.id.lan_status)
        ports = view.findViewById(R.id.lan_ports); consent = view.findViewById(R.id.lan_consent)
        scanButton = view.findViewById(R.id.lan_scan); cancelButton = view.findViewById(R.id.lan_cancel)
        networkButton = view.findViewById(R.id.lan_network); results = view.findViewById(R.id.lan_results)
        consent.isChecked = false
        consent.setOnCheckedChangeListener { _, _ -> controls() }
        networkButton.setOnClickListener { chooseNetwork() }
        scanButton.setOnClickListener { startScan() }
        cancelButton.setOnClickListener { stopScan(R.string.lan_cancelled) }
        select(environment.networks().firstOrNull())
    }
    override fun onStart() {
        super.onStart()
        val request = NetworkRequest.Builder().addCapability(NetworkCapabilities.NET_CAPABILITY_NOT_VPN)
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI).addTransportType(NetworkCapabilities.TRANSPORT_ETHERNET).build()
        try { environment.manager.registerNetworkCallback(request, callback); registered = true }
        catch (_: RuntimeException) { invalidateNetwork() }
    }
    override fun onStop() {
        if (registered) { runCatching { environment.manager.unregisterNetworkCallback(callback) }; registered = false }
        stopScan(R.string.lan_cancelled); dialog?.dismiss(); dialog = null
        super.onStop()
    }
    private fun controls() {
        val active = scan?.isActive == true
        scanButton.isEnabled = !active && consent.isChecked && selected != null
        cancelButton.visibility = if (active) View.VISIBLE else View.GONE
        ports.isEnabled = !active; networkButton.isEnabled = !active; consent.isEnabled = !active && selected != null
    }
    private fun select(network: LanNetwork?) {
        selected = network; consent.isChecked = false; results.removeAllViews()
        scopeText.text = network?.let { getString(R.string.lan_scope_details, it.scope.localAddress, it.scope.cidr, it.scope.hosts.size) } ?: getString(R.string.lan_no_network)
        status.setText(R.string.lan_idle); controls()
    }
    private fun chooseNetwork() {
        val networks = environment.networks()
        if (networks.size < 2) { select(networks.firstOrNull()); return }
        dialog = AlertDialog.Builder(requireContext()).setTitle(R.string.lan_refresh)
            .setItems(networks.map { "${it.scope.localAddress} — ${it.scope.cidr}" }.toTypedArray()) { _, index -> select(networks[index]) }
            .setNegativeButton(android.R.string.cancel, null).show()
    }
    private fun invalidateNetwork() { stopScan(R.string.lan_network_changed); selected = null; scopeText.setText(R.string.lan_no_network); controls() }
    private fun stopScan(message: Int) {
        scan?.cancel(); scan = null
        if (::consent.isInitialized) { consent.isChecked = false; results.removeAllViews(); status.setText(message); controls() }
    }
    private fun startScan() {
        val network = selected ?: return
        if (scan?.isActive == true || !consent.isChecked) return
        if (!environment.current(network)) { invalidateNetwork(); return }
        val chosenPorts = LanScope.ports(ports.text.toString())
        if (chosenPorts == null) { ports.error = getString(R.string.lan_ports_error); ports.requestFocus(); return }
        ports.error = null; results.removeAllViews(); status.setText(R.string.lan_running)
        scan = viewLifecycleOwner.lifecycleScope.launch(start = CoroutineStart.LAZY) {
            try {
                val probe = ProxyProbe(socketFactory = { network.network.socketFactory.createSocket() })
                val report = withContext(Dispatchers.Default) {
                    LanScanner(probe::probe).scan(network.scope, chosenPorts) { done, total ->
                        withContext(Dispatchers.Main) { status.text = getString(R.string.lan_progress, done, total) }
                    }
                }
                if (!environment.current(network)) { invalidateNetwork(); return@launch }
                status.text = getString(if (report.timedOut || report.limited) R.string.lan_partial else R.string.lan_done, report.candidates.size)
                report.candidates.forEach { candidate ->
                    results.addView(Button(requireContext()).apply {
                        text = "${candidate.host}:${candidate.port}\n${kindLabel(candidate.kind)}"
                        textSize = 18f; minHeight = (64 * resources.displayMetrics.density).toInt()
                        setOnClickListener { confirmProfile(network, candidate) }
                    })
                }
            } catch (cancelled: CancellationException) { throw cancelled }
            catch (_: Exception) { status.setText(R.string.lan_failed) }
            finally { if (scan === coroutineContext[Job]) { scan = null; controls(); if (isResumed) scanButton.requestFocus() } }
        }.also { it.start() }
        controls(); cancelButton.requestFocus()
    }
    private fun kindLabel(kind: ProbeKind) = getString(when (kind) {
        ProbeKind.SOCKS5 -> R.string.lan_socks_verified
        ProbeKind.SOCKS5_AUTH -> R.string.lan_socks_auth
        ProbeKind.HTTP_AUTH -> R.string.lan_http_auth
        ProbeKind.HTTP_UNVERIFIED -> R.string.lan_http_unverified
        ProbeKind.TCP_UNVERIFIED -> R.string.lan_tcp_unverified
    })
    private fun confirmProfile(network: LanNetwork, candidate: ProxyCandidate) {
        if (!environment.current(network) || !network.scope.contains(candidate.host)) { invalidateNetwork(); return }
        viewLifecycleOwner.lifecycleScope.launch {
            val groups = withContext(Dispatchers.IO) { SagerDatabase.groupDao.allGroups().filter { it.type == GroupType.BASIC } }
            if (groups.isEmpty()) { status.setText(R.string.lan_no_group); return@launch }
            if (!isResumed || !environment.current(network)) return@launch
            val context = requireContext()
            val panel = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL; setPadding((24 * resources.displayMetrics.density).toInt(), (8 * resources.displayMetrics.density).toInt(), (24 * resources.displayMetrics.density).toInt(), (8 * resources.displayMetrics.density).toInt()) }
            fun field(label: Int, value: String = "", secret: Boolean = false, limit: Int = 256): EditText {
                val input = EditText(context).apply {
                    hint = getString(label); setText(value); textSize = 18f; minHeight = (48 * resources.displayMetrics.density).toInt(); isSingleLine = true
                    inputType = InputType.TYPE_CLASS_TEXT or if (secret) InputType.TYPE_TEXT_VARIATION_PASSWORD else InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
                    filters = arrayOf(android.text.InputFilter.LengthFilter(limit)); isSaveEnabled = false
                }
                panel.addView(input); return input
            }
            panel.addView(TextView(context).apply { text = "${candidate.host}:${candidate.port}\n${kindLabel(candidate.kind)}\n${getString(R.string.lan_save_warning)}"; textSize = 18f })
            val name = field(R.string.lan_name, "LAN ${candidate.host}:${candidate.port}", limit = 128)
            panel.addView(TextView(context).apply { text = getString(R.string.lan_protocol_label); textSize = 18f })
            val protocol = Spinner(context).apply {
                minimumHeight = (48 * resources.displayMetrics.density).toInt()
                adapter = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item,
                    listOf(getString(R.string.lan_choose_protocol), "SOCKS5", "HTTP"))
                setSelection(when (candidate.kind) {
                    ProbeKind.SOCKS5, ProbeKind.SOCKS5_AUTH -> 1
                    ProbeKind.HTTP_AUTH -> 2
                    else -> 0
                })
                isEnabled = candidate.kind in setOf(ProbeKind.HTTP_UNVERIFIED, ProbeKind.TCP_UNVERIFIED)
            }
            panel.addView(protocol)
            val validation = TextView(context).apply { textSize = 18f; accessibilityLiveRegion = View.ACCESSIBILITY_LIVE_REGION_POLITE }
            panel.addView(validation)
            val username = field(R.string.lan_username); val password = field(R.string.lan_password, secret = true)
            panel.addView(TextView(context).apply { text = getString(R.string.lan_group) })
            val group = Spinner(context).apply { minimumHeight = (48 * resources.displayMetrics.density).toInt()
                adapter = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, groups.map { it.displayName() }) }
            panel.addView(group)
            val scroll = ScrollView(context).apply { addView(panel) }
            val confirmation = AlertDialog.Builder(context).setTitle(R.string.add_profile).setView(scroll)
                .setPositiveButton(R.string.lan_save, null).setNegativeButton(android.R.string.cancel, null).create()
            dialog = confirmation; confirmation.show()
            confirmation.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                if (!environment.current(network)) { confirmation.dismiss(); invalidateNetwork(); return@setOnClickListener }
                if (protocol.selectedItemPosition == 0) {
                    validation.setText(R.string.lan_choose_protocol); protocol.requestFocus(); return@setOnClickListener
                }
                validation.text = ""
                if (candidate.kind in setOf(ProbeKind.SOCKS5_AUTH, ProbeKind.HTTP_AUTH) && username.text.isBlank()) {
                    username.error = getString(R.string.lan_auth_required); username.requestFocus(); return@setOnClickListener
                }
                confirmation.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false
                val useHttp = protocol.selectedItemPosition == 2; val groupId = groups[group.selectedItemPosition].id
                val profileName = name.text.toString().trim(); val user = username.text.toString(); val secret = password.text.toString()
                viewLifecycleOwner.lifecycleScope.launch {
                    try {
                        val saved = withContext(Dispatchers.IO) { LanProfileStore.save(candidate, useHttp, groupId, profileName, user, secret, protocolConfirmed = true) }
                        confirmation.dismiss(); status.setText(if (saved.created) R.string.lan_saved else R.string.lan_duplicate)
                        status.requestFocus()
                    } catch (cancelled: CancellationException) { throw cancelled }
                    catch (_: Exception) { status.setText(R.string.lan_save_failed); confirmation.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = true }
                }
            }
        }
    }
}

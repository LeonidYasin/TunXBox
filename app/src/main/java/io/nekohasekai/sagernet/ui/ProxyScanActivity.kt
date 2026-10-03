package io.nekohasekai.sagernet.ui

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import io.nekohasekai.sagernet.R
import io.nekohasekai.sagernet.databinding.ActivityProxyScanBinding
import io.nekohasekai.sagernet.network.tv.DiscoveredProxy
import io.nekohasekai.sagernet.service.ProxyDiscoveryService
import io.nekohasekai.sagernet.ui.adapter.ProxyScanAdapter
import kotlinx.coroutines.launch

class ProxyScanActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProxyScanBinding
    private lateinit var adapter: ProxyScanAdapter
    private lateinit var discoveryService: ProxyDiscoveryService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProxyScanBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.local_network_scan)
        
        setupRecyclerView()
        setupFab()
        
        discoveryService = ProxyDiscoveryService(application)
        discoveryService.setListener(object : ProxyDiscoveryService.ScanResultListener {
            override fun onScanComplete(proxies: List<DiscoveredProxy>) {
                runOnUiThread {
                    hideLoading()
                    if (proxies.isEmpty()) {
                        showEmptyState()
                    } else {
                        showResults(proxies)
                    }
                }
            }

            override fun onScanError(error: String) {
                runOnUiThread {
                    hideLoading()
                    Snackbar.make(binding.root, "Scan failed: $error", Snackbar.LENGTH_LONG).show()
                }
            }
        })
    }

    private fun setupRecyclerView() {
        adapter = ProxyScanAdapter(items = emptyList()) { proxy ->
            Toast.makeText(this, "Selected ${proxy.ip}:${proxy.port}", Toast.LENGTH_SHORT).show()
            finish()
        }
        binding.resultsList.apply {
            layoutManager = LinearLayoutManager(this@ProxyScanActivity)
            adapter = this@ProxyScanActivity.adapter
            isNestedScrollingEnabled = false
        }
    }

    private fun setupFab() {
        binding.fabScan.setOnClickListener {
            startScan()
        }
    }

    private fun startScan() {
        showLoading()
        lifecycleScope.launch {
            discoveryService.startScan()
        }
    }

    private fun showLoading() {
        binding.emptyState.visibility = View.GONE
        binding.resultsList.visibility = View.VISIBLE
    }

    private fun hideLoading() {}

    private fun showEmptyState() {
        binding.resultsList.visibility = View.GONE
        binding.emptyState.visibility = View.VISIBLE
    }

    private fun showResults(proxies: List<DiscoveredProxy>) {
        binding.emptyState.visibility = View.GONE
        binding.resultsList.visibility = View.VISIBLE
        adapter.updateData(proxies)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressedDispatcher.onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
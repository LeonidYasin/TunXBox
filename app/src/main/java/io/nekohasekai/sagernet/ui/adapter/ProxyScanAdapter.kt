package io.nekohasekai.sagernet.ui.adapter

import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import io.nekohasekai.sagernet.R
import io.nekohasekai.sagernet.network.tv.DiscoveredProxy

class ProxyScanAdapter(
    private var items: List<DiscoveredProxy>,
    private val onItemClick: (DiscoveredProxy) -> Unit
) : RecyclerView.Adapter<ProxyScanAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameText: TextView = view.findViewById(R.id.proxy_name)
        val detailsText: TextView = view.findViewById(R.id.proxy_details)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_discovered_proxy, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val proxy = items[position]
        holder.nameText.text = proxy.name
        holder.detailsText.text = "${proxy.ip}:${proxy.port} (${proxy.type})"
        
        holder.itemView.setOnClickListener { onItemClick(proxy) }
        
        holder.itemView.isFocusable = true
        holder.itemView.isFocusableInTouchMode = true
        holder.itemView.setOnKeyListener { v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER && event.action == KeyEvent.ACTION_DOWN) {
                onItemClick(proxy)
                true
            } else {
                false
            }
        }
    }

    override fun getItemCount() = items.size

    fun updateData(newItems: List<DiscoveredProxy>) {
        items = newItems
        notifyDataSetChanged()
    }
}
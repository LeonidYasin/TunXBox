package io.nekohasekai.sagernet.ui

import android.content.Context
import android.content.Intent
import android.view.MenuInflater
import android.view.View
import android.widget.PopupMenu
import io.nekohasekai.sagernet.R
import io.nekohasekai.sagernet.ui.profile.*
import moe.matsuri.nb4a.proxy.anytls.AnyTLSSettingsActivity
import moe.matsuri.nb4a.proxy.shadowtls.ShadowTLSSettingsActivity
import moe.matsuri.nb4a.proxy.config.ConfigSettingActivity

/** Shared by phone + and TV: menu resources and editor intents cannot silently diverge. */
object ProfileCreationActions {
    data class Entry(val id: Int, val title: String)
    fun addMenu(context: Context): android.view.Menu {
        val menu = PopupMenu(context, View(context)).menu
        MenuInflater(context).inflate(R.menu.add_profile_menu, menu)
        return requireNotNull(menu.findItem(R.id.action_add).subMenu)
    }
    fun manualEntries(context: Context): List<Entry> {
        val menu = addMenu(context)
        val manual = requireNotNull((0 until menu.size()).map { menu.getItem(it) }.first { it.hasSubMenu() }.subMenu)
        return (0 until manual.size()).map { manual.getItem(it) }.filter { it.isVisible }
            .map { Entry(it.itemId, it.title.toString()) }
    }
    fun intent(context: Context, actionId: Int): Intent? {
        val editor = when (actionId) {
            R.id.action_new_socks -> SocksSettingsActivity::class.java
            R.id.action_new_http -> HttpSettingsActivity::class.java
            R.id.action_new_ss -> ShadowsocksSettingsActivity::class.java
            R.id.action_new_vmess, R.id.action_new_vless -> VMessSettingsActivity::class.java
            R.id.action_new_trojan -> TrojanSettingsActivity::class.java
            R.id.action_new_trojan_go -> TrojanGoSettingsActivity::class.java
            R.id.action_new_mieru -> MieruSettingsActivity::class.java
            R.id.action_new_naive -> NaiveSettingsActivity::class.java
            R.id.action_new_hysteria -> HysteriaSettingsActivity::class.java
            R.id.action_new_tuic -> TuicSettingsActivity::class.java
            R.id.action_new_shadowtls -> ShadowTLSSettingsActivity::class.java
            R.id.action_new_anytls -> AnyTLSSettingsActivity::class.java
            R.id.action_new_ssh -> SSHSettingsActivity::class.java
            R.id.action_new_wg -> WireGuardSettingsActivity::class.java
            R.id.action_new_config -> ConfigSettingActivity::class.java
            R.id.action_new_chain -> ChainSettingsActivity::class.java
            else -> return null
        }
        return Intent(context, editor).apply { if (actionId == R.id.action_new_vless) putExtra("vless", true) }
    }
}

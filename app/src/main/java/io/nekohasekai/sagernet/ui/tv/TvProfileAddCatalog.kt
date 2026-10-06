package io.nekohasekai.sagernet.ui.tv

import android.content.Context
import io.nekohasekai.sagernet.R
import io.nekohasekai.sagernet.ui.ProfileCreationActions

/** All inbound methods from the phone +, with LAN receiving and direct URL entry for TV.
 * Send to TV is export, not a method of adding a profile on this device.
 */
object TvProfileAddCatalog {
    const val PHONE = -1
    const val URL = -2
    const val MANUAL = -3
    const val SEND = -4
    fun entries(context: Context): List<ProfileCreationActions.Entry> {
        val menu = ProfileCreationActions.addMenu(context)
        val result = mutableListOf(
            ProfileCreationActions.Entry(PHONE, context.getString(R.string.tv_receive_qr)),
            ProfileCreationActions.Entry(SEND, context.getString(R.string.tv_send_choice)),
            ProfileCreationActions.Entry(URL, context.getString(R.string.tv_url)))
        for (index in 0 until menu.size()) {
            val item = menu.getItem(index)
            if (!item.isVisible || item.itemId == R.id.action_send_to_tv) continue
            result.add(ProfileCreationActions.Entry(if (item.hasSubMenu()) MANUAL else item.itemId,
                if (item.itemId == R.id.action_scan_qr_code) context.getString(R.string.tv_scan) else item.title.toString()))
        }
        return result
    }
}

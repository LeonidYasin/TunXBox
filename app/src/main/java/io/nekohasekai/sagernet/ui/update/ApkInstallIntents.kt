// Adapted from LeonidYasin/proxydroid e976d8b2870ab1f906e955be07882f7334632b40 (GPL); see docs/app-updates.md.
package io.nekohasekai.sagernet.ui.update

import android.content.ClipData
import android.content.Intent
import android.net.Uri

/** Both Android installer routes receive the same narrowly granted content URI. */
object ApkInstallIntents {
    @Suppress("DEPRECATION")
    fun create(uri: Uri, action: String): Intent {
        require(uri.scheme == "content") { "Installer requires a content URI" }
        require(action == Intent.ACTION_INSTALL_PACKAGE || action == Intent.ACTION_VIEW)
        return Intent(action).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            clipData = ClipData.newRawUri("TunXBox verified update", uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, false)
        }
    }
}

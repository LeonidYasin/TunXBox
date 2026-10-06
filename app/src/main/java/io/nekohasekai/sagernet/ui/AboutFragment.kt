package io.nekohasekai.sagernet.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.text.util.Linkify
import android.view.View
import android.widget.Toast
import androidx.activity.result.component1
import androidx.activity.result.component2
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.danielstone.materialaboutlibrary.MaterialAboutFragment
import com.danielstone.materialaboutlibrary.items.MaterialAboutActionItem
import com.danielstone.materialaboutlibrary.model.MaterialAboutCard
import com.danielstone.materialaboutlibrary.model.MaterialAboutList
import io.nekohasekai.sagernet.BuildConfig
import io.nekohasekai.sagernet.R
import io.nekohasekai.sagernet.databinding.LayoutAboutBinding
import io.nekohasekai.sagernet.ktx.*
import io.nekohasekai.sagernet.plugin.PluginManager.loadString
import io.nekohasekai.sagernet.utils.PackageCache
import io.nekohasekai.sagernet.widget.ListListener
import libcore.Libcore
import moe.matsuri.nb4a.plugin.Plugins
import androidx.core.net.toUri
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.nekohasekai.sagernet.SagerNet
import io.nekohasekai.sagernet.database.DataStore
import moe.matsuri.nb4a.utils.Util
import org.json.JSONObject

class AboutFragment : ToolbarFragment(R.layout.layout_about) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = LayoutAboutBinding.bind(view)

        ViewCompat.setOnApplyWindowInsetsListener(view, ListListener)
        toolbar.setTitle(R.string.menu_about)

        parentFragmentManager.beginTransaction()
            .replace(R.id.about_fragment_holder, AboutContent())
            .commitAllowingStateLoss()

        runOnDefaultDispatcher {
            val license = view.context.assets.open("LICENSE").bufferedReader().readText()
            onMainDispatcher {
                binding.license.text = license
                Linkify.addLinks(binding.license, Linkify.EMAIL_ADDRESSES or Linkify.WEB_URLS)
            }
        }
    }

    class AboutContent : MaterialAboutFragment() {

        val requestIgnoreBatteryOptimizations = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { (resultCode, _) ->
            if (resultCode == Activity.RESULT_OK) {
                parentFragmentManager.beginTransaction()
                    .replace(R.id.about_fragment_holder, AboutContent())
                    .commitAllowingStateLoss()
            }
        }

        override fun getMaterialAboutList(activityContext: Context): MaterialAboutList {
            return MaterialAboutList.Builder()
                .addCard(
                    MaterialAboutCard.Builder()
                        .outline(false)
                        .addItem(
                            MaterialAboutActionItem.Builder()
                                .icon(R.drawable.ic_baseline_update_24)
                                .text(R.string.app_version)
                                .subText(SagerNet.appVersionNameForDisplay)
                                .setOnClickAction {
                                    requireContext().launchCustomTab(
                                        ProjectLinks.RELEASES
                                    )
                                }
                                .build())
                        .addItem(
                            MaterialAboutActionItem.Builder()
                                .text(R.string.app_build_identity)
                                .subText(getString(R.string.app_build_identity_value, BuildConfig.APPLICATION_ID, BuildConfig.VERSION_CODE))
                                .build())
                        .addItem(
                            MaterialAboutActionItem.Builder()
                                .text(R.string.check_update_release)
                                .setOnClickAction {
                                    checkUpdate(false)
                                }
                                .build())
                        .addItem(
                            MaterialAboutActionItem.Builder()
                                .text(R.string.check_update_preview)
                                .setOnClickAction {
                                    checkUpdate(true)
                                }
                                .build())
                        .addItem(
                            MaterialAboutActionItem.Builder()
                                .icon(R.drawable.ic_baseline_layers_24)
                                .text(getString(R.string.version_x, "sing-box"))
                                .subText(runCatching { Libcore.versionBox() }.getOrDefault("—"))
                                .setOnClickAction { }
                                .build())
                        .apply {
                            PackageCache.awaitLoadSync()
                            for ((_, pkg) in PackageCache.installedPluginPackages) {
                                try {
                                    val pluginId =
                                        pkg.providers?.get(0)?.loadString(Plugins.METADATA_KEY_ID)
                                    if (pluginId.isNullOrBlank()) continue
                                    addItem(
                                        MaterialAboutActionItem.Builder()
                                            .icon(R.drawable.ic_baseline_nfc_24)
                                            .text(
                                                getString(
                                                    R.string.version_x,
                                                    pluginId
                                                ) + " (${Plugins.displayExeProvider(pkg.packageName)})"
                                            )
                                            .subText("v" + pkg.versionName)
                                            .setOnClickAction {
                                                startActivity(Intent().apply {
                                                    action =
                                                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                                                    data = Uri.fromParts(
                                                        "package", pkg.packageName, null
                                                    )
                                                })
                                            }
                                            .build())
                                } catch (e: Exception) {
                                    Logs.w(e)
                                }
                            }
                        }
                        .apply {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                val pm = app.getSystemService(Context.POWER_SERVICE) as PowerManager
                                if (!pm.isIgnoringBatteryOptimizations(app.packageName)) {
                                    addItem(
                                        MaterialAboutActionItem.Builder()
                                            .icon(R.drawable.ic_baseline_running_with_errors_24)
                                            .text(R.string.ignore_battery_optimizations)
                                            .subText(R.string.ignore_battery_optimizations_sum)
                                            .setOnClickAction {
                                                requestIgnoreBatteryOptimizations.launch(
                                                    Intent(
                                                        Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                                                        "package:${app.packageName}".toUri()
                                                    )
                                                )
                                            }
                                            .build())
                                }
                            }
                        }
                        .build())
                .addCard(
                    MaterialAboutCard.Builder()
                        .outline(false)
                        .title(R.string.project)
                        .addItem(
                            MaterialAboutActionItem.Builder()
                                .icon(R.drawable.ic_baseline_sanitizer_24)
                                .text(R.string.project_source_tunxbox)
                                .setOnClickAction {
                                    requireContext().launchCustomTab(
                                        ProjectLinks.REPOSITORY

                                    )
                                }
                                .build())
                        .addItem(
                            MaterialAboutActionItem.Builder()
                                .icon(R.drawable.ic_baseline_bug_report_24)
                                .text(R.string.project_report_issue)
                                .setOnClickAction {
                                    requireContext().launchCustomTab(
                                        ProjectLinks.ISSUES
                                    )
                                }
                                .build())
                        .build())
                .addCard(
                    MaterialAboutCard.Builder().outline(false).title(R.string.project_upstream)
                        .addItem(MaterialAboutActionItem.Builder()
                            .text(R.string.project_upstream_neko).subText(R.string.project_upstream_hint)
                            .setOnClickAction { requireContext().launchCustomTab(ProjectLinks.UPSTREAM) }.build())
                        .addItem(MaterialAboutActionItem.Builder()
                            .icon(R.drawable.ic_baseline_card_giftcard_24)
                            .text(R.string.project_upstream_support).subText(R.string.project_upstream_support_hint)
                            .setOnClickAction { requireContext().launchCustomTab(ProjectLinks.UPSTREAM_DONATE) }.build())
                        .build())
                .build()

        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)

            view.findViewById<RecyclerView>(R.id.mal_recyclerview).apply {
                overScrollMode = RecyclerView.OVER_SCROLL_NEVER
            }
        }

        private fun installedSigner(): String {
            val context = requireContext()
            val signatures = if (Build.VERSION.SDK_INT >= 28) {
                context.packageManager.getPackageInfo(context.packageName, android.content.pm.PackageManager.GET_SIGNING_CERTIFICATES)
                    .signingInfo?.apkContentsSigners
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(context.packageName, android.content.pm.PackageManager.GET_SIGNATURES).signatures
            }
            require(signatures?.size == 1) { "Expected one installed signer" }
            val bytes = java.security.MessageDigest.getInstance("SHA-256").digest(signatures!![0].toByteArray())
            return bytes.joinToString("") { "%02x".format(it.toInt() and 255) }
        }

        fun checkUpdate(checkPreview: Boolean) {
            val signer = runCatching { installedSigner() }.getOrNull() ?: run {
                Toast.makeText(requireContext(), R.string.update_no_compatible_release, Toast.LENGTH_LONG).show()
                return
            }
            runOnIoDispatcher {
                try {
                    val client = Libcore.newHttpClient().apply { modernTLS(); trySocks5(DataStore.mixedPort) }
                    fun fetch(url: String): String = Util.getStringBox(client.newRequest().apply { setURL(url) }.execute().contentString)
                    val release = JSONObject(fetch(if (checkPreview) ProjectLinks.PREVIEW_API else ProjectLinks.RELEASE_API))
                    val releaseUrl = release.getString("html_url")
                    require(releaseUrl.startsWith(ProjectLinks.REPOSITORY + "/releases/tag/"))
                    val assets = release.getJSONArray("assets")
                    val asset = (0 until assets.length()).map { assets.getJSONObject(it) }
                        .firstOrNull { it.optString("name") == "apk-manifest.json" }
                    if (asset == null) {
                        runOnMainDispatcher { if (isAdded) Toast.makeText(requireContext(), R.string.update_no_compatible_release, Toast.LENGTH_LONG).show() }
                        return@runOnIoDispatcher
                    }
                    val manifestUrl = asset.getString("browser_download_url")
                    require(manifestUrl.startsWith(ProjectLinks.REPOSITORY + "/releases/download/"))
                    val available = ReleaseUpdatePolicy.parse(JSONObject(fetch(manifestUrl)), BuildConfig.APPLICATION_ID, BuildConfig.VERSION_CODE.toLong(), signer)
                    runOnMainDispatcher {
                        if (!isAdded || view == null) return@runOnMainDispatcher
                        if (available.newer) {
                            val context = requireContext()
                            MaterialAlertDialogBuilder(context).setTitle(R.string.update_dialog_title)
                                .setMessage(context.getString(R.string.update_dialog_message, SagerNet.appVersionNameForDisplay, available.versionName))
                                .setPositiveButton(R.string.yes) { _, _ -> context.launchCustomTab(releaseUrl) }
                                .setNegativeButton(R.string.no, null).show()
                        } else Toast.makeText(requireContext(), R.string.check_update_no, Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Logs.w(e)
                    runOnMainDispatcher { if (isAdded) Toast.makeText(requireContext(), R.string.update_check_failed, Toast.LENGTH_LONG).show() }
                }
            }
        }
    }
}

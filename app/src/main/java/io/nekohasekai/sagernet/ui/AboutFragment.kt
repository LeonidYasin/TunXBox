package io.nekohasekai.sagernet.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
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

        ViewCompat.setOnApplyWindowInsetsListener(view, ListListener)
        toolbar.setTitle(R.string.menu_about)

        parentFragmentManager.beginTransaction()
            .replace(R.id.about_fragment_holder, AboutContent())
            .commitAllowingStateLoss()

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
                .addCard(MaterialAboutCard.Builder().outline(false)
                    .title(R.string.app_name_long)
                    .addItem(MaterialAboutActionItem.Builder().text(R.string.app_desc).build())
                    .build())
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
                                .setOnClickAction { showBuildInfo() }
                                .build())
                        .addItem(MaterialAboutActionItem.Builder()
                            .text(R.string.about_build_details)
                            .subText("${BuildConfig.VERSION_NAME} · #${BuildConfig.BUILD_RUN_NUMBER} · ${BuildConfig.BUILD_COMMIT.take(8)}")
                            .setOnClickAction { showBuildInfo() }.build())
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
                                    requireContext().launchCustomTab(ProjectLinks.ISSUES)
                                }
                                .build())
                        .addItem(MaterialAboutActionItem.Builder()
                            .text(R.string.project_contact_tunxbox).subText(ProjectLinks.SUPPORT_EMAIL)
                            .setOnClickAction { contactSupport() }.build())
                        .addItem(MaterialAboutActionItem.Builder()
                            .text(R.string.project_telegram_tunxbox).subText("@LEONI_YA")
                            .setOnClickAction { requireContext().launchCustomTab(ProjectLinks.SUPPORT_TELEGRAM) }.build())
                        .addItem(MaterialAboutActionItem.Builder()
                            .icon(R.drawable.ic_baseline_card_giftcard_24)
                            .text(R.string.project_upstream_support).subText(R.string.project_upstream_support_hint)
                            .setOnClickAction {
                                MaterialAlertDialogBuilder(requireContext()).setTitle(R.string.project_upstream_support)
                                    .setMessage(R.string.about_support_terms)
                                    .setPositiveButton(R.string.project_contact_tunxbox) { _, _ -> contactSupport() }
                                    .setNegativeButton(android.R.string.cancel, null).show()
                            }.build())
                        .build())
                .addCard(
                    MaterialAboutCard.Builder().outline(false).title(R.string.project_upstream)
                        .addItem(MaterialAboutActionItem.Builder()
                            .text(R.string.project_upstream_neko).subText(R.string.project_upstream_hint)
                            .setOnClickAction { requireContext().launchCustomTab(ProjectLinks.UPSTREAM) }.build())
                        .build())
                .addCard(MaterialAboutCard.Builder().outline(false).title(R.string.license)
                    .addItem(MaterialAboutActionItem.Builder().text(R.string.about_license_short).build())
                    .addItem(MaterialAboutActionItem.Builder().text(R.string.about_license_full)
                        .setOnClickAction { showFullLicense() }.build())
                    .build())
                .build()

        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)

            // A single native list owns all scrolling, including intro, support and license.
            view.findViewById<RecyclerView>(R.id.mal_recyclerview).apply {
                overScrollMode = RecyclerView.OVER_SCROLL_NEVER
            }
        }

        private fun showFullLicense() {
            val assets = requireContext().assets
            runOnIoDispatcher {
                val notice = assets.open("LICENSE").bufferedReader().use { it.readText() }
                val terms = assets.open("COPYING").bufferedReader().use { it.readText() }
                runOnMainDispatcher {
                    if (!isAdded) return@runOnMainDispatcher
                    MaterialAlertDialogBuilder(requireContext()).setTitle(R.string.license)
                        .setMessage(notice + "\n\n" + terms)
                        .setPositiveButton(android.R.string.ok, null).show().also { dialog ->
                            dialog.findViewById<android.widget.TextView>(android.R.id.message)?.setTextIsSelectable(true)
                            dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE).requestFocus()
                        }
                }
            }
        }

        private fun contactSupport() {
            // Explicit user action only; no diagnostics, profile or credentials are attached.
            val uri = Uri.parse("mailto:${ProjectLinks.SUPPORT_EMAIL}").buildUpon()
                .appendQueryParameter("subject", "TunXBox support").build()
            try {
                startActivity(Intent(Intent.ACTION_SENDTO, uri))
            } catch (_: android.content.ActivityNotFoundException) {
                val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                clipboard.setPrimaryClip(android.content.ClipData.newPlainText("TunXBox support", ProjectLinks.SUPPORT_EMAIL))
                Toast.makeText(requireContext(), R.string.project_contact_copied, Toast.LENGTH_LONG).show()
            }
        }

        private fun showBuildInfo() {
            val facts = listOf(
                "App" to "TunXBox ${BuildConfig.VERSION_NAME}",
                "Android versionCode" to BuildConfig.VERSION_CODE.toString(),
                "Package" to BuildConfig.APPLICATION_ID,
                "Variant" to "${BuildConfig.FLAVOR}/${BuildConfig.BUILD_TYPE}",
                "Source commit" to BuildConfig.BUILD_COMMIT,
                "Checkout ref" to BuildConfig.BUILD_SOURCE_REF,
                "CI build number / run ID" to "${BuildConfig.BUILD_RUN_NUMBER} / ${BuildConfig.BUILD_RUN_ID}",
                "CI URL" to if (BuildConfig.BUILD_RUN_ID == "local") "local build" else "${ProjectLinks.REPOSITORY}/actions/runs/${BuildConfig.BUILD_RUN_ID}",
                "Build time (UTC)" to BuildConfig.BUILD_TIME_UTC,
                "sing-box runtime" to runCatching { Libcore.versionBox() }.getOrDefault("unavailable"),
                "sing-box source" to BuildConfig.CORE_SOURCE_COMMIT,
                "libneko source" to BuildConfig.LIBNEKO_SOURCE_COMMIT,
                "REALITY patch SHA-256" to BuildConfig.CORE_PATCH_SHA256,
                "Installed signing certificate SHA-256" to runCatching { installedSigner() }.getOrDefault("unavailable"),
                "Android / API" to "${Build.VERSION.RELEASE} / ${Build.VERSION.SDK_INT}",
                "Device supported ABIs (not APK contents)" to Build.SUPPORTED_ABIS.joinToString(", ")
            )
            val summary = AboutBuildInfo.format(facts)
            MaterialAlertDialogBuilder(requireContext()).setTitle(R.string.about_build_details)
                .setMessage(summary)
                .setNeutralButton(R.string.about_copy_build_info) { _, _ ->
                    val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                    clipboard.setPrimaryClip(android.content.ClipData.newPlainText("TunXBox build", summary))
                }.setPositiveButton(android.R.string.ok, null).show().also { dialog ->
                    dialog.findViewById<android.widget.TextView>(android.R.id.message)?.setTextIsSelectable(true)
                    dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE).requestFocus()
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
            (activity as? MainActivity)?.openAppUpdates(checkPreview)
        }
    }
}

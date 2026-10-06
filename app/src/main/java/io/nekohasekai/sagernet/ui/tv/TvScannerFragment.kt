package io.nekohasekai.sagernet.ui.tv

import android.Manifest
import android.content.Intent
import android.graphics.ImageDecoder
import android.os.Build
import android.os.Bundle
import android.content.pm.PackageManager
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.CancellationException
import com.google.zxing.Result
import com.king.zxing.CameraScan
import com.king.zxing.DefaultCameraScan
import com.king.zxing.analyze.QRCodeAnalyzer
import com.king.zxing.util.CodeUtils
import com.king.zxing.util.PermissionUtils
import io.nekohasekai.sagernet.R
import io.nekohasekai.sagernet.database.DataStore
import io.nekohasekai.sagernet.database.ProfileManager
import io.nekohasekai.sagernet.group.RawUpdater
import io.nekohasekai.sagernet.ktx.Logs
import io.nekohasekai.sagernet.ktx.SubscriptionFoundException
import io.nekohasekai.sagernet.ktx.onMainDispatcher
import io.nekohasekai.sagernet.ktx.readableMessage
import io.nekohasekai.sagernet.ui.MainActivity
import java.util.concurrent.atomic.AtomicBoolean

/**
 * TV-compatible QR code scanner fragment.
 * 
 * Handles three types of QR codes:
 * 1. tunxbox://transfer — connects to remote device and GETs profiles via /export
 * 2. Standard proxy links (ss://, vmess://, etc.) — imports directly
 * 3. Subscription URLs (sn://subscription) — delegates to MainActivity
 */
class TvScannerFragment : Fragment(), CameraScan.OnScanResultCallback {

    private lateinit var previewView: androidx.camera.view.PreviewView
    private lateinit var flashlightBtn: ImageView
    private lateinit var importImageBtn: ImageView
    private lateinit var closeBtn: ImageView
    private lateinit var hintText: TextView
    
    private lateinit var cameraScan: CameraScan
    
    private val finished = AtomicBoolean(false)

    private fun scannerWork(work: suspend () -> Unit) {
        if (view == null) return
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try { work() } catch (cancelled: CancellationException) { throw cancelled }
            catch (_: Exception) { onMainDispatcher { if (isAdded && view != null) Toast.makeText(requireContext(), R.string.tv_import_failed, Toast.LENGTH_LONG).show() } }
        }
    }

    
    private val importImageLauncher = registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        if (uris.isNullOrEmpty()) return@registerForActivityResult
        
        scannerWork {
            try {
                var totalImported = 0
                var qrFound = false
                var transferDone = false
                
                uris.forEach { uri ->
                    try {
                        val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                            ImageDecoder.decodeBitmap(
                                ImageDecoder.createSource(requireContext().contentResolver, uri)
                            ) { decoder, _, _ ->
                                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                                decoder.isMutableRequired = true
                            }
                        } else {
                            @Suppress("DEPRECATION") MediaStore.Images.Media.getBitmap(
                                requireContext().contentResolver, uri
                            )
                        }
                        val result = CodeUtils.parseCodeResult(bitmap)
                        if (result != null) {
                            qrFound = true
                            if (result.text.startsWith("tunxbox://transfer")) {
                                transferDone = handleTvTransfer(result.text) || transferDone
                            } else {
                                totalImported += importFromQrText(result.text)
                            }
                        }
                    } catch (subscription: SubscriptionFoundException) {
                        onMainDispatcher {
                            startActivity(Intent(requireContext(), MainActivity::class.java).apply {
                                action = Intent.ACTION_VIEW; data = subscription.link.toUri(); putExtra("force_phone_mode", true)
                            })
                            parentFragmentManager.popBackStack()
                        }
                        return@scannerWork
                    } catch (e: Exception) {
                        if (e is CancellationException) throw e
                        Logs.w("Failed to decode QR from image")
                    }
                }
                
                onMainDispatcher {
                    if (transferDone) {
                        parentFragmentManager.popBackStack()
                    } else if (totalImported > 0) {
                        Toast.makeText(requireContext(), 
                            getString(R.string.tv_import_count, totalImported), 
                            Toast.LENGTH_LONG).show()
                        parentFragmentManager.popBackStack()
                    } else if (qrFound) {
                        Toast.makeText(requireContext(), getString(R.string.tv_import_failed), Toast.LENGTH_LONG).show()
                        parentFragmentManager.popBackStack()
                    } else {
                        Toast.makeText(requireContext(), getString(R.string.tv_import_failed), Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                        if (e is CancellationException) throw e
                Logs.w("Import image failed", e)
                onMainDispatcher {
                    Toast.makeText(requireContext(), getString(R.string.tv_import_failed), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            startCamera()
        } else {
            Toast.makeText(requireContext(), R.string.tv_scan_no_camera, Toast.LENGTH_LONG).show()
            hintText.setText(R.string.tv_scan_no_camera)
            previewView.visibility = View.GONE
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.layout_scanner_tv, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        previewView = view.findViewById(R.id.previewView)
        flashlightBtn = view.findViewById(R.id.ivFlashlight)
        importImageBtn = view.findViewById(R.id.ivImportImage)
        closeBtn = view.findViewById(R.id.ivClose)
        hintText = view.findViewById(R.id.tvHint)
        
        flashlightBtn.setOnClickListener { toggleFlashlight() }
        importImageBtn.setOnClickListener { importFromImage() }
        flashlightBtn.nextFocusRightId = R.id.ivImportImage
        importImageBtn.nextFocusLeftId = R.id.ivFlashlight
        importImageBtn.nextFocusRightId = R.id.ivClose
        closeBtn.nextFocusLeftId = R.id.ivImportImage
        closeBtn.setOnClickListener { parentFragmentManager.popBackStack() }
        
        if (!requireContext().packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
            hintText.setText(R.string.tv_scan_no_camera)
            previewView.visibility = View.GONE
            flashlightBtn.isEnabled = false
            importImageBtn.requestFocus()
            return
        }
        initCameraScan()

        if (PermissionUtils.checkPermission(requireContext(), Manifest.permission.CAMERA)) {
            startCamera()
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }
    
    private fun initCameraScan() {
        cameraScan = DefaultCameraScan(requireActivity(), previewView)
        cameraScan.setAnalyzer(QRCodeAnalyzer())
        cameraScan.setOnScanResultCallback(this)
        cameraScan.setNeedAutoZoom(true)
    }
    
    private fun startCamera() {
        try {
            cameraScan.startCamera()
            hintText.setText(R.string.tv_scan_hint)
        } catch (e: Exception) {
                        if (e is CancellationException) throw e
            Logs.w("Camera start failed", e)
            hintText.setText(R.string.tv_scan_no_camera)
            previewView.visibility = View.GONE
        }
    }
    
    private fun toggleFlashlight() {
        try {
            val isTorch = cameraScan.isTorchEnabled
            cameraScan.enableTorch(!isTorch)
            flashlightBtn.isSelected = !isTorch
        } catch (e: Exception) {
                        if (e is CancellationException) throw e
            Logs.w("Flashlight toggle failed", e)
        }
    }
    
    private fun importFromImage() {
        importImageLauncher.launch("image/*")
    }
    
    private fun releaseCamera() {
        if (!::cameraScan.isInitialized) return
        try {
            cameraScan.release()
        } catch (e: Exception) {
                        if (e is CancellationException) throw e
            Logs.w("Camera release failed", e)
        }
    }
    
    override fun onScanResultCallback(result: Result?): Boolean {
        if (finished.getAndSet(true)) return true
        
        scannerWork {
            try {
                val text = result?.text ?: throw Exception("QR code not found")

                // Check for TV transfer QR: scanned another device's QR -> GET profiles from it
                if (text.startsWith("tunxbox://transfer")) {
                    if (handleTvTransfer(text)) onMainDispatcher { parentFragmentManager.popBackStack() }
                    return@scannerWork
                }

                val count = importFromQrText(text)
                
                onMainDispatcher {
                    if (count > 0) {
                        Toast.makeText(requireContext(), 
                            "Imported $count profile(s)", 
                            Toast.LENGTH_LONG).show()
                        parentFragmentManager.popBackStack()
                    } else {
                        Toast.makeText(requireContext(), R.string.action_import_err, Toast.LENGTH_SHORT).show()
                        finished.set(false)
                    }
                }
            } catch (e: SubscriptionFoundException) {
                onMainDispatcher {
                    val intent = Intent(requireContext(), MainActivity::class.java).apply {
                        action = Intent.ACTION_VIEW
                        data = e.link.toUri()
                    }
                    startActivity(intent)
                    parentFragmentManager.popBackStack()
                }
            } catch (e: Throwable) {
                if (e is CancellationException) throw e
                Logs.w(e)
                onMainDispatcher {
                    var text = getString(R.string.action_import_err)
                    text += "\n" + e.readableMessage
                    Toast.makeText(requireContext(), text, Toast.LENGTH_SHORT).show()
                    finished.set(false)
                }
            }
        }
        return true
    }
    
    /**
     * Parse QR text and import profiles. Returns the number of profiles imported.
     * Throws SubscriptionFoundException if the QR contains a subscription URL.
     */
    private suspend fun importFromQrText(text: String): Int {
        val results = RawUpdater.parseRaw(text)
        if (results.isNullOrEmpty()) return 0
        
        val currentGroupId = DataStore.selectedGroupForImport()
        if (DataStore.selectedGroup != currentGroupId) {
            DataStore.selectedGroup = currentGroupId
        }
        
        var count = 0
        for (profile in results) {
            ProfileManager.createProfile(currentGroupId, profile)
            count++
        }
        return count
    }

    /**
     * Scanned a tunxbox://transfer QR from another device.
     * Parse the URL, connect to the remote device's /export endpoint,
     * and import the profiles it serves.
     */
    private suspend fun handleTvTransfer(qrText: String): Boolean {
        return try {
            val uri = java.net.URI(qrText)
            val params = uri.query?.split("&")?.associate { val parts = it.split("=", limit = 2); parts[0] to parts.getOrElse(1) { "" } } ?: emptyMap()
            val ip = params["ip"] ?: error("Missing LAN address")
            val port = params["port"]?.toIntOrNull() ?: TvTransferServer.PORT
            val token = params["session"] ?: error("Missing pairing token")
            TransferProtocol.requireLanAddress(ip, port)
            val connection = java.net.URL("http://$ip:$port/export").openConnection() as java.net.HttpURLConnection
            val response = try {
                connection.requestMethod = "GET"; connection.instanceFollowRedirects = false
                connection.connectTimeout = 10000; connection.readTimeout = 10000
                connection.setRequestProperty("X-Session-Token", token)
                require(connection.responseCode in 200..299)
                connection.inputStream.use { String(TransferProtocol.readLimited(it), Charsets.UTF_8) }
            } finally { connection.disconnect() }
            val data = org.json.JSONObject(response).optString("profiles", "")
            val imported = TvProfileImporter.importProfiles(data)
            onMainDispatcher { Toast.makeText(requireContext(), getString(R.string.tv_import_count, imported), Toast.LENGTH_LONG).show() }
            true
        } catch (cancelled: CancellationException) { throw cancelled }
        catch (_: Exception) {
            finished.set(false)
            onMainDispatcher { if (isAdded && view != null) Toast.makeText(requireContext(), R.string.tv_import_failed, Toast.LENGTH_LONG).show() }
            false
        }
    }

    override fun onDestroyView() {
        releaseCamera()
        super.onDestroyView()
    }
}

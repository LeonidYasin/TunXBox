package io.nekohasekai.sagernet.ui.tv

import android.Manifest
import android.content.Intent
import android.graphics.ImageDecoder
import android.os.Build
import android.os.Bundle
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
import io.nekohasekai.sagernet.ktx.runOnDefaultDispatcher
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
    
    private val importImageLauncher = registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        if (uris.isNullOrEmpty()) return@registerForActivityResult
        
        runOnDefaultDispatcher {
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
                                handleTvTransfer(result.text)
                                transferDone = true
                            } else {
                                totalImported += importFromQrText(result.text)
                            }
                        }
                    } catch (e: Exception) {
                        Logs.w("Failed to decode QR from image", e)
                    }
                }
                
                onMainDispatcher {
                    if (transferDone) {
                        parentFragmentManager.popBackStack()
                    } else if (totalImported > 0) {
                        Toast.makeText(requireContext(), 
                            "Imported $totalImported profile(s)", 
                            Toast.LENGTH_LONG).show()
                        parentFragmentManager.popBackStack()
                    } else if (qrFound) {
                        Toast.makeText(requireContext(), "QR found but no valid proxy data", Toast.LENGTH_LONG).show()
                        parentFragmentManager.popBackStack()
                    } else {
                        Toast.makeText(requireContext(), "No QR code found in image(s)", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Logs.w("Import image failed", e)
                onMainDispatcher {
                    Toast.makeText(requireContext(), "Import failed: ${e.readableMessage}", Toast.LENGTH_SHORT).show()
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
            Toast.makeText(requireContext(), 
                "Camera permission denied. Use \"Import from image\" instead.", 
                Toast.LENGTH_LONG).show()
            hintText.text = "Camera not available. Use the image import button above."
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
        closeBtn.setOnClickListener { parentFragmentManager.popBackStack() }
        
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
            hintText.text = "Point camera at QR code to import proxy profile"
        } catch (e: Exception) {
            Logs.w("Camera start failed", e)
            hintText.text = "Camera not available. Use the image import button."
            previewView.visibility = View.GONE
        }
    }
    
    private fun toggleFlashlight() {
        try {
            val isTorch = cameraScan.isTorchEnabled
            cameraScan.enableTorch(!isTorch)
            flashlightBtn.isSelected = !isTorch
        } catch (e: Exception) {
            Logs.w("Flashlight toggle failed", e)
        }
    }
    
    private fun importFromImage() {
        importImageLauncher.launch("image/*")
    }
    
    private fun releaseCamera() {
        try {
            cameraScan.release()
        } catch (e: Exception) {
            Logs.w("Camera release failed", e)
        }
    }
    
    override fun onScanResultCallback(result: Result?): Boolean {
        if (finished.getAndSet(true)) return true
        
        runOnDefaultDispatcher {
            try {
                val text = result?.text ?: throw Exception("QR code not found")

                // Check for TV transfer QR: scanned another device's QR -> GET profiles from it
                if (text.startsWith("tunxbox://transfer")) {
                    handleTvTransfer(text)
                    return@runOnDefaultDispatcher
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
    private fun handleTvTransfer(qrText: String) {
        try {
            val uri = java.net.URI(qrText)
            val params = uri.query?.split("&")?.associate {
                val parts = it.split("=")
                parts[0] to (parts.getOrNull(1) ?: "")
            } ?: emptyMap()

            val ip = params["ip"] ?: throw Exception("Missing 'ip' in QR")
            val port = params["port"]?.toIntOrNull() ?: 8765
            val session = params["session"] ?: throw Exception("Missing 'session' in QR")

            onMainDispatcher {
                Toast.makeText(requireContext(), "📡 Connecting to $ip:$port...", Toast.LENGTH_SHORT).show()
            }

            val url = java.net.URL("http://$ip:$port/export?session=$session")
            val connection = url.openConnection() as java.net.HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 10000
            connection.readTimeout = 10000

            val responseCode = connection.responseCode
            if (responseCode !in 200..299) {
                val error = connection.errorStream?.bufferedReader()?.readText() ?: "HTTP $responseCode"
                throw Exception("Remote device returned error: $error")
            }

            val responseBody = connection.inputStream.bufferedReader().readText()
            connection.disconnect()

            val json = org.json.JSONObject(responseBody)
            val profilesData = json.optString("profiles", "")
            val remoteCount = json.optInt("count", 0)

            if (profilesData.isBlank()) {
                onMainDispatcher {
                    Toast.makeText(requireContext(), "Remote device has no profiles", Toast.LENGTH_LONG).show()
                    finished.set(false)
                }
                return
            }

            val links = profilesData.split("\n").filter { it.isNotBlank() }
            val targetId = DataStore.selectedGroupForImport()
            var importedCount = 0

            for (link in links) {
                try {
                    val proxies = RawUpdater.parseRaw(link)
                    if (!proxies.isNullOrEmpty()) {
                        for (proxy in proxies) {
                            ProfileManager.createProfile(targetId, proxy)
                            importedCount++
                        }
                    }
                } catch (e: Exception) {
                    Logs.w("Failed to import profile from remote: $link", e)
                }
            }

            onMainDispatcher {
                if (importedCount > 0) {
                    Toast.makeText(requireContext(),
                        "✅ Imported $importedCount profile(s) from remote device ($remoteCount available)",
                        Toast.LENGTH_LONG).show()
                    parentFragmentManager.popBackStack()
                } else {
                    Toast.makeText(requireContext(), "No valid profiles found on remote device", Toast.LENGTH_LONG).show()
                    finished.set(false)
                }
            }

        } catch (e: Exception) {
            Logs.e("TV transfer import failed", e)
            onMainDispatcher {
                Toast.makeText(requireContext(),
                    "❌ Failed to receive profiles: ${e.readableMessage}",
                    Toast.LENGTH_LONG).show()
                finished.set(false)
            }
        }
    }
    
    override fun onDestroyView() {
        releaseCamera()
        super.onDestroyView()
    }
}

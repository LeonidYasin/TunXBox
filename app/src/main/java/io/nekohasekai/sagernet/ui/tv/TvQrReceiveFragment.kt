package io.nekohasekai.sagernet.ui.tv

import android.Manifest
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
import io.nekohasekai.sagernet.ktx.onMainDispatcher
import io.nekohasekai.sagernet.ktx.runOnDefaultDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import java.util.concurrent.atomic.AtomicBoolean

/**
 * TV fragment for scanning QR codes from remote devices to RECEIVE profiles.
 * 
 * This is the "receive" counterpart to QrCodeTransferFragment (which "sends" profiles).
 * 
 * Scans tunxbox://transfer QR codes from remote devices (phones, other TVs),
 * extracts the IP/port/session, connects to the remote device's /export endpoint,
 * and imports the received profiles.
 */
class TvQrReceiveFragment : Fragment(), CameraScan.OnScanResultCallback {

    private lateinit var previewView: androidx.camera.view.PreviewView
    private lateinit var flashlightBtn: ImageView
    private lateinit var importImageBtn: ImageView
    private lateinit var closeBtn: ImageView
    private lateinit var hintText: TextView
    private lateinit var statusText: TextView
    
    private lateinit var cameraScan: CameraScan
    private val finished = AtomicBoolean(false)
    
    private val importImageLauncher = registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        if (uris.isNullOrEmpty()) return@registerForActivityResult
        
        runOnDefaultDispatcher {
            try {
                var totalImported = 0
                var qrFound = false
                
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
                            totalImported += importFromTransferQr(result.text)
                        }
                    } catch (e: Exception) {
                        Logs.w("Failed to decode QR from image", e)
                    }
                }
                
                onMainDispatcher {
                    if (totalImported > 0) {
                        Toast.makeText(requireContext(), 
                            "✅ Received $totalImported profile(s)", 
                            Toast.LENGTH_LONG).show()
                        parentFragmentManager.popBackStack()
                    } else if (qrFound) {
                        Toast.makeText(requireContext(), "QR found but no valid transfer data", Toast.LENGTH_LONG).show()
                        parentFragmentManager.popBackStack()
                    } else {
                        Toast.makeText(requireContext(), "No transfer QR code found in image(s)", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Logs.w("Import image failed", e)
                onMainDispatcher {
                    Toast.makeText(requireContext(), "Import failed: ${e.message}", Toast.LENGTH_SHORT).show()
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
        
        // Add status text view
        val statusContainer = view as android.widget.FrameLayout
        statusText = TextView(requireContext()).apply {
            layoutParams = android.widget.FrameLayout.LayoutParams(
                android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
                android.widget.FrameLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = android.view.Gravity.BOTTOM
                topMargin = 120
            }
            gravity = android.view.Gravity.CENTER
            setPadding(32, 16, 32, 80)
            setBackgroundColor(0xAA000000.toInt())
            setTextColor(0xFF0EA5E9.toInt())
            textSize = 16f
            text = "Scanning for tunxbox://transfer QR codes..."
        }
        statusContainer.addView(statusText)
        
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
            hintText.text = "Point camera at QR code from remote device to receive profiles"
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
                val count = importFromTransferQr(text)
                
                onMainDispatcher {
                    if (count > 0) {
                        Toast.makeText(requireContext(), 
                            "✅ Received $count profile(s)", 
                            Toast.LENGTH_LONG).show()
                        parentFragmentManager.popBackStack()
                    } else {
                        Toast.makeText(requireContext(), "No valid profiles in transfer", Toast.LENGTH_SHORT).show()
                        finished.set(false) // Allow retry
                    }
                }
            } catch (e: Throwable) {
                Logs.w(e)
                onMainDispatcher {
                    Toast.makeText(requireContext(), "Failed: ${e.message}", Toast.LENGTH_SHORT).show()
                    finished.set(false)
                }
            }
        }
        return true
    }
    
    /**
     * Parse tunxbox://transfer QR and fetch profiles from remote device.
     * Returns the number of profiles imported.
     */
    private suspend fun importFromTransferQr(qrText: String): Int = withContext(Dispatchers.IO) {
        try {
            // Parse QR: tunxbox://transfer?ip=192.168.1.100&port=8765&session=abc123
            val uri = java.net.URI(qrText)
            if (!qrText.startsWith("tunxbox://transfer")) {
                // Try parsing as regular proxy QR (ss://, vmess://, etc.)
                return@withContext importFromProxyQr(qrText)
            }
            
            val params = uri.query?.split("&")?.associate { 
                val parts = it.split("=")
                parts[0] to (parts.getOrNull(1) ?: "")
            } ?: emptyMap()
            
            val ip = params["ip"] ?: throw Exception("Missing 'ip' in QR")
            val port = params["port"]?.toIntOrNull() ?: 8765
            val session = params["session"] ?: throw Exception("Missing 'session' in QR")
            
            // Update status
            onMainDispatcher {
                statusText.text = "📡 Connecting to $ip:$port..."
            }
            
            // Fetch profiles from remote device
            val url = URL("http://$ip:$port/export?session=$session")
            val connection = url.openConnection() as java.net.HttpURLConnection
            connection.connectTimeout = 10000
            connection.readTimeout = 10000
            
            val responseCode = connection.responseCode
            if (responseCode != 200) {
                throw Exception("Server returned $responseCode")
            }
            
            val responseBody = connection.inputStream.bufferedReader().readText()
            connection.disconnect()
            
            val json = JSONObject(responseBody)
            if (json.optString("status") != "success") {
                throw Exception(json.optString("error", "Unknown error"))
            }
            
            val profilesArray = json.optJSONArray("profiles") ?: return@withContext 0
            
            // Update status
            onMainDispatcher {
                statusText.text = "⬇️ Downloading ${profilesArray.length()} profiles..."
            }
            
            var importedCount = 0
            val targetId = DataStore.selectedGroupForImport()
            
            for (i in 0 until profilesArray.length()) {
                try {
                    val profileJson = profilesArray.getJSONObject(i)
                    val config = profileJson.optString("config", "")
                    
                    if (config.isNotBlank()) {
                        val proxies = RawUpdater.parseRaw(config)
                        if (!proxies.isNullOrEmpty()) {
                            for (proxy in proxies) {
                                ProfileManager.createProfile(targetId, proxy)
                                importedCount++
                            }
                        }
                    }
                } catch (e: Exception) {
                    Logs.w("Failed to import profile from transfer", e)
                }
            }
            
            return@withContext importedCount
            
        } catch (e: Exception) {
            Logs.e("Transfer QR import failed", e)
            throw e
        }
    }
    
    /**
     * Fallback: parse regular proxy QR codes (ss://, vmess://, etc.)
     */
    private fun importFromProxyQr(text: String): Int {
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
    
    override fun onDestroyView() {
        releaseCamera()
        super.onDestroyView()
    }
}

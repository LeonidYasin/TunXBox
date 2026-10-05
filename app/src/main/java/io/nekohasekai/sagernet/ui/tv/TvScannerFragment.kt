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
 * Scans QR codes via camera (if available on the TV device) or from image files.
 * Imports proxy profiles directly into the database.
 * 
 * This mirrors the phone's ScannerActivity but as a Fragment for the Leanback TV UI.
 * If the TV device has no camera, the "Import from image" fallback is available.
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
                            totalImported += importFromQrText(result.text)
                        }
                    } catch (e: Exception) {
                        Logs.w("Failed to decode QR from image", e)
                    }
                }
                
                onMainDispatcher {
                    if (totalImported > 0) {
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
        // Use requireActivity() (FragmentActivity) for compatibility with DefaultCameraScan
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
                val count = importFromQrText(text)
                
                onMainDispatcher {
                    if (count > 0) {
                        Toast.makeText(requireContext(), 
                            "Imported $count profile(s)", 
                            Toast.LENGTH_LONG).show()
                        parentFragmentManager.popBackStack()
                    } else {
                        Toast.makeText(requireContext(), R.string.action_import_err, Toast.LENGTH_SHORT).show()
                        finished.set(false) // Allow retry
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
    private fun importFromQrText(text: String): Int {
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

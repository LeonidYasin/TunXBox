package io.nekohasekai.sagernet.ui.tv

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Scan with a normal phone camera to open a local import form, or with TunXBox to
 * transfer an existing group. The receiver never offers its own profiles for export. */
class QrCodeTransferFragment : Fragment() {
    private lateinit var qrImageView: ImageView
    private lateinit var statusText: TextView
    private lateinit var ipText: TextView
    private lateinit var hintText: TextView
    private var transferServer: TvTransferServer? = null
    private var qrJob: Job? = null
    private var appQr = false
    private var qrSize = 400

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val context = requireContext()
        val metrics = resources.displayMetrics
        fun dp(value: Int) = (value * metrics.density).toInt()
        qrSize = (minOf(metrics.widthPixels, metrics.heightPixels) * 0.5f).toInt().coerceAtLeast(200)
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(dp(24), dp(16), dp(24), dp(16))
        }
        layout.addView(TextView(context).apply {
            text = "Import from phone / computer"
            textSize = 24f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
        })
        qrImageView = ImageView(context).apply {
            layoutParams = LinearLayout.LayoutParams(qrSize, qrSize)
            scaleType = ImageView.ScaleType.FIT_CENTER
        }
        layout.addView(qrImageView)
        ipText = TextView(context).apply { textSize = 16f; setTextColor(Color.CYAN); gravity = Gravity.CENTER }
        statusText = TextView(context).apply { textSize = 16f; setTextColor(Color.WHITE); gravity = Gravity.CENTER }
        hintText = TextView(context).apply { textSize = 14f; setTextColor(Color.LTGRAY); gravity = Gravity.CENTER }
        layout.addView(ipText)
        layout.addView(statusText)
        layout.addView(hintText)
        layout.addView(Button(context).apply {
            text = "Switch QR: browser / TunXBox app"
            isFocusable = true
            setOnClickListener { appQr = !appQr; renderQr() }
        })
        layout.addView(Button(context).apply {
            text = "Close transfer"
            isFocusable = true
            setOnClickListener { parentFragmentManager.popBackStack() }
        })
        return ScrollView(context).apply {
            setBackgroundColor(Color.rgb(15, 23, 42))
            addView(layout)
        }
    }

    override fun onStart() {
        super.onStart()
        try {
            transferServer = TvTransferServer(
                onImportSuccess = { count ->
                    view?.post {
                        if (view != null) {
                            statusText.text = "Imported $count profile(s). You can close this screen."
                            statusText.setTextColor(Color.GREEN)
                        }
                    }
                },
                onImportError = { message ->
                    view?.post {
                        if (view != null) {
                            statusText.text = message
                            statusText.setTextColor(Color.RED)
                        }
                    }
                }
            )
            statusText.text = "Ready. Session expires in 10 minutes."
            renderQr()
        } catch (_: Exception) {
            statusText.text = "Cannot start transfer. Connect to Wi-Fi/Ethernet; check port 8765."
            statusText.setTextColor(Color.RED)
            qrImageView.setImageDrawable(null)
        }
    }

    private fun renderQr() {
        val server = transferServer ?: return
        val data = if (appQr) server.getAppQrData() else server.getBrowserQrData()
        ipText.text = "LAN address: ${server.getLocalIpAddress()}:${TvTransferServer.PORT}"
        hintText.text = if (appQr) {
            "On phone: TunXBox → Phone Mode → + → Scan QR. Sends the current group.\nTrusted LAN only: HTTP is not encrypted."
        } else {
            "Scan with your phone camera → open browser → paste configuration, upload a file, or enter a subscription URL. No phone app required.\nBoth devices must be on the same trusted LAN. HTTP is not encrypted."
        }
        qrJob?.cancel()
        qrJob = viewLifecycleOwner.lifecycleScope.launch {
            val bitmap = withContext(Dispatchers.Default) {
                val bits = QRCodeWriter().encode(data, BarcodeFormat.QR_CODE, qrSize, qrSize)
                Bitmap.createBitmap(qrSize, qrSize, Bitmap.Config.RGB_565).apply {
                    val pixels = IntArray(qrSize * qrSize) { index ->
                        if (bits[index % qrSize, index / qrSize]) Color.BLACK else Color.WHITE
                    }
                    setPixels(pixels, 0, qrSize, 0, 0, qrSize, qrSize)
                }
            }
            qrImageView.setImageBitmap(bitmap)
        }
    }

    override fun onStop() {
        qrJob?.cancel()
        transferServer?.stop()
        transferServer = null
        qrImageView.setImageDrawable(null)
        super.onStop()
    }

    override fun onDestroyView() {
        qrJob?.cancel()
        transferServer?.stop()
        transferServer = null
        super.onDestroyView()
    }
}

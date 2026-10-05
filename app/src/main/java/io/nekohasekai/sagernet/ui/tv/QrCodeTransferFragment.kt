package io.nekohasekai.sagernet.ui.tv

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import io.nekohasekai.sagernet.ktx.Logs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Shows a QR code on TV so that a phone can scan it and send proxy profiles to the TV.
 * 
 * The QR contains a tunxbox://transfer URL with the TV's IP, port and session token.
 * The phone app scans this, connects to the TV's HTTP server, and POSTs proxy configs.
 * The TV's TvTransferServer receives and imports them.
 */
class QrCodeTransferFragment : Fragment() {

    private lateinit var qrImageView: ImageView
    private lateinit var statusText: TextView
    private lateinit var ipText: TextView
    private var transferServer: TvTransferServer? = null
    private val fragmentScope = CoroutineScope(Dispatchers.Main)

    companion object {
        const val QR_SIZE = 700
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val layout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            gravity = android.view.Gravity.CENTER
            setPadding(64, 48, 64, 48)
            setBackgroundColor(Color.parseColor("#FF0F172A"))
        }

        val title = TextView(requireContext()).apply {
            text = "📲 Receive Profiles from Phone"
            textSize = 32f
            setTextColor(Color.WHITE)
            gravity = android.view.Gravity.CENTER
            setPadding(0, 0, 0, 24)
        }

        qrImageView = ImageView(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(QR_SIZE, QR_SIZE)
            scaleType = ImageView.ScaleType.FIT_CENTER
        }

        ipText = TextView(requireContext()).apply {
            textSize = 20f
            setTextColor(Color.parseColor("#FF0EA5E9"))
            gravity = android.view.Gravity.CENTER
            setPadding(0, 24, 0, 8)
            text = "Starting server..."
        }

        statusText = TextView(requireContext()).apply {
            text = "Waiting for phone to scan..."
            textSize = 18f
            setTextColor(Color.parseColor("#AAFFFFFF"))
            gravity = android.view.Gravity.CENTER
            setPadding(0, 8, 0, 0)
        }

        val hint = TextView(requireContext()).apply {
            text = "How it works:\n\n" +
                   "1. Open TunXBox on your phone (switch to Phone Mode)\n" +
                   "2. On phone: tap + → Scan QR code\n" +
                   "3. Point phone camera at this TV screen\n" +
                   "4. Phone sends all its profiles to this TV"
            textSize = 16f
            setTextColor(Color.parseColor("#88FFFFFF"))
            gravity = android.view.Gravity.CENTER
            setPadding(0, 32, 0, 0)
            setLineSpacing(0f, 1.4f)
        }

        layout.addView(title)
        layout.addView(qrImageView)
        layout.addView(ipText)
        layout.addView(statusText)
        layout.addView(hint)

        return layout
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startTransferServer()
    }

    override fun onDestroyView() {
        transferServer?.stop()
        transferServer = null
        super.onDestroyView()
    }

    private fun startTransferServer() {
        fragmentScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    transferServer = TvTransferServer(
                        onImportSuccess = { count ->
                            // Callback is called from NanoHTTPD thread — switch to Main
                            fragmentScope.launch {
                                statusText.text = "✅ Imported $count profile(s)!"
                                statusText.setTextColor(Color.parseColor("#FF4CAF50"))
                                Toast.makeText(context, "Successfully imported $count profiles", Toast.LENGTH_LONG).show()
                                
                                view?.postDelayed({
                                    parentFragmentManager.popBackStack()
                                }, 3000)
                            }
                        },
                        onImportError = { error ->
                            fragmentScope.launch {
                                statusText.text = "❌ Error: $error"
                                statusText.setTextColor(Color.parseColor("#FFF44336"))
                            }
                        }
                    )
                }

                val ip = transferServer?.getLocalIpAddress() ?: "unknown"
                val token = transferServer?.getSessionToken() ?: ""
                val port = 8765

                val qrData = "tunxbox://transfer?ip=$ip&port=$port&session=$token"
                
                withContext(Dispatchers.Default) {
                    val writer = QRCodeWriter()
                    val bitMatrix = writer.encode(qrData, BarcodeFormat.QR_CODE, QR_SIZE, QR_SIZE)
                    
                    val bitmap = Bitmap.createBitmap(QR_SIZE, QR_SIZE, Bitmap.Config.RGB_565)
                    for (x in 0 until QR_SIZE) {
                        for (y in 0 until QR_SIZE) {
                            bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
                        }
                    }
                    
                    withContext(Dispatchers.Main) {
                        qrImageView.setImageBitmap(bitmap)
                        ipText.text = "TV IP: $ip:$port"
                        statusText.text = "Ready! Scan this QR with your phone"
                        statusText.setTextColor(Color.parseColor("#AAFFFFFF"))
                    }
                }

            } catch (e: Exception) {
                Logs.e("QR Transfer error", e)
                statusText.text = "❌ Server failed: ${e.message}"
                statusText.setTextColor(Color.parseColor("#FFF44336"))
                Toast.makeText(context, "Failed to start transfer server", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

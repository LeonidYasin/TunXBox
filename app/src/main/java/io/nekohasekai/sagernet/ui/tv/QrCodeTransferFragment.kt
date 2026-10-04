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
import io.nekohasekai.sagernet.ktx.onMainDispatcher
import io.nekohasekai.sagernet.ktx.runOnDefaultDispatcher

/**
 * Показывает QR код для передачи подписки С ТЕЛЕФОНА НА TV.
 * 
 * Протокол:
 * 1. TV запускает локальный HTTP сервер на порту 8765
 * 2. TV показывает QR: tunxbox://transfer?ip=<TV_IP>&port=8765&session=<token>
 * 3. Телефон сканирует QR → отправляет POST http://<TV_IP>:8765/import с профилями
 * 4. TV получает данные → импортирует → показывает успех
 */
class QrCodeTransferFragment : Fragment() {

    private lateinit var qrImageView: ImageView
    private lateinit var statusText: TextView
    private lateinit var ipText: TextView
    private var transferServer: TvTransferServer? = null

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
            text = " Transfer Subscription to TV"
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
            text = "1. Open TunXBox on your phone\n" +
                   "2. Go to Subscriptions → Scan QR\n" +
                   "3. Point camera at this screen\n" +
                   "4. Select profiles to send"
            textSize = 16f
            setTextColor(Color.parseColor("#88FFFFFF"))
            gravity = android.view.Gravity.CENTER
            setPadding(0, 32, 0, 0)
            lineSpacingMultiplier = 1.4f
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
        runOnDefaultDispatcher {
            try {
                transferServer = TvTransferServer(
                    onImportSuccess = { count ->
                        onMainDispatcher {
                            statusText.text = "✅ Imported $count profile(s)!"
                            statusText.setTextColor(Color.parseColor("#FF4CAF50"))
                            Toast.makeText(context, "Successfully imported $count profiles", Toast.LENGTH_LONG).show()
                            
                            // Возвращаемся назад через 3 секунды
                            view?.postDelayed({
                                parentFragmentManager.popBackStack()
                            }, 3000)
                        }
                    },
                    onImportError = { error ->
                        onMainDispatcher {
                            statusText.text = "❌ Error: $error"
                            statusText.setTextColor(Color.parseColor("#FFF44336"))
                        }
                    }
                )

                val ip = transferServer?.getLocalIpAddress() ?: "unknown"
                val token = transferServer?.getSessionToken() ?: ""
                val port = 8765

                // Генерируем QR с данными для телефона
                val qrData = "tunxbox://transfer?ip=$ip&port=$port&session=$token"
                
                val writer = QRCodeWriter()
                val bitMatrix = writer.encode(qrData, BarcodeFormat.QR_CODE, QR_SIZE, QR_SIZE)
                
                val bitmap = Bitmap.createBitmap(QR_SIZE, QR_SIZE, Bitmap.Config.RGB_565)
                for (x in 0 until QR_SIZE) {
                    for (y in 0 until QR_SIZE) {
                        bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
                    }
                }

                onMainDispatcher {
                    qrImageView.setImageBitmap(bitmap)
                    ipText.text = "TV IP: $ip:$port"
                    statusText.text = "Ready! Scan this QR with your phone"
                    statusText.setTextColor(Color.parseColor("#AAFFFFFF"))
                }

            } catch (e: Exception) {
                onMainDispatcher {
                    statusText.text = "❌ Server failed: ${e.message}"
                    statusText.setTextColor(Color.parseColor("#FFF44336"))
                    Toast.makeText(context, "Failed to start transfer server", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}

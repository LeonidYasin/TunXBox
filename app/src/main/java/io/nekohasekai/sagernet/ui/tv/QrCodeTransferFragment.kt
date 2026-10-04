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
import io.nekohasekai.sagernet.SagerNet
import io.nekohasekai.sagernet.database.DataStore
import io.nekohasekai.sagernet.database.ProfileManager
import io.nekohasekai.sagernet.database.ProxyEntity
import io.nekohasekai.sagernet.database.SagerDatabase
import io.nekohasekai.sagernet.ktx.runOnDefaultDispatcher
import kotlinx.coroutines.runBlocking

/**
 * Показывает QR код с данными для передачи подписки на TV.
 * 
 * Протокол передачи:
 * 1. TV генерирует случайный session ID и показывает QR: tunxbox://transfer?session=<uuid>&device=TV
 * 2. Телефон сканирует QR, получает session ID
 * 3. Телефон отправляет POST на локальный HTTP сервер TV (или через Firebase/cloud)
 * 4. TV получает данные и импортирует профили
 * 
 * Упрощённая версия (без сервера):
 * - QR содержит прямую ссылку на подписку или список профилей в формате JSON
 * - Телефон сканирует и открывает ссылку / импортирует данные
 */
class QrCodeTransferFragment : Fragment() {

    private lateinit var qrImageView: ImageView
    private lateinit var statusText: TextView
    
    companion object {
        const val QR_SIZE = 800 // pixels for TV display
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val layout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            gravity = android.view.Gravity.CENTER
            setPadding(64, 64, 64, 64)
            setBackgroundColor(Color.parseColor("#FF0F172A"))
        }

        val title = TextView(requireContext()).apply {
            text = "Transfer Subscription from Phone"
            textSize = 28f
            setTextColor(Color.WHITE)
            gravity = android.view.Gravity.CENTER
            setPadding(0, 0, 0, 32)
        }

        qrImageView = ImageView(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(QR_SIZE, QR_SIZE)
            scaleType = ImageView.ScaleType.FIT_CENTER
        }

        statusText = TextView(requireContext()).apply {
            text = "Generating QR code..."
            textSize = 18f
            setTextColor(Color.parseColor("#AAFFFFFF"))
            gravity = android.view.Gravity.CENTER
            setPadding(0, 32, 0, 0)
        }

        val hint = TextView(requireContext()).apply {
            text = "1. Open TunXBox on your phone\n2. Tap \"Scan QR\" in subscription menu\n3. Point camera at this screen"
            textSize = 16f
            setTextColor(Color.parseColor("#88FFFFFF"))
            gravity = android.view.Gravity.CENTER
            setPadding(0, 24, 0, 0)
            lineSpacingMultiplier = 1.3f
        }

        layout.addView(title)
        layout.addView(qrImageView)
        layout.addView(statusText)
        layout.addView(hint)

        return layout
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        generateQrCode()
    }

    private fun generateQrCode() {
        runOnDefaultDispatcher {
            try {
                // Собираем данные для передачи
                val transferData = buildTransferPayload()
                
                // Генерируем QR
                val writer = QRCodeWriter()
                val bitMatrix = writer.encode(transferData, BarcodeFormat.QR_CODE, QR_SIZE, QR_SIZE)
                
                val bitmap = Bitmap.createBitmap(QR_SIZE, QR_SIZE, Bitmap.Config.RGB_565)
                for (x in 0 until QR_SIZE) {
                    for (y in 0 until QR_SIZE) {
                        bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
                    }
                }

                activity?.runOnUiThread {
                    qrImageView.setImageBitmap(bitmap)
                    statusText.text = "Ready to scan (${transferData.length} chars)"
                }
            } catch (e: Exception) {
                activity?.runOnUiThread {
                    statusText.text = "Error: ${e.message}"
                    Toast.makeText(context, "QR generation failed", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun buildTransferPayload(): String {
        // Вариант 1: Если есть URL подписки в буфере — используем его
        val clipboard = SagerNet.getClipboardText()
        if (clipboard.startsWith("http")) {
            return "tunxbox://subscribe?url=${java.net.URLEncoder.encode(clipboard, "UTF-8")}"
        }
        
        // Вариант 2: Экспортируем все профили текущей группы как JSON
        val groupId = DataStore.selectedGroup
        val profiles = runBlocking { SagerDatabase.proxyDao.getByGroup(groupId) }
        
        if (profiles.isEmpty()) {
            return "tunxbox://empty?msg=No profiles to transfer"
        }
        
        // Формируем компактный JSON с профилями
        val profileLinks = profiles.mapNotNull { proxy ->
            try {
                proxy.toStdLink() // ss://..., vmess://... etc
            } catch (_: Exception) {
                null
            }
        }
        
        if (profileLinks.isEmpty()) {
            return "tunxbox://error?msg=Cannot export profiles"
        }
        
        // tunxbox://profiles?data=<base64 encoded links joined by newline>
        val combined = profileLinks.joinToString("\n")
        val encoded = android.util.Base64.encodeToString(combined.toByteArray(), android.util.Base64.NO_WRAP)
        return "tunxbox://profiles?count=${profileLinks.size}&data=$encoded"
    }
}

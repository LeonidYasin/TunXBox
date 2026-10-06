package io.nekohasekai.sagernet.ui.tv

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import io.nekohasekai.sagernet.R
import kotlinx.coroutines.*

/** Keep the QR fully visible while remote users scroll the help/buttons alongside it. */
class QrCodeTransferFragment : Fragment() {
    private lateinit var qrImageView: ImageView
    private lateinit var statusText: TextView
    private lateinit var ipText: TextView
    private lateinit var hintText: TextView
    private lateinit var toggle: Button
    private var transferServer: TvTransferServer? = null
    private var qrJob: Job? = null
    private var expiryJob: Job? = null
    private var appQr = false
    private var qrSize = 400
    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); appQr = savedInstanceState?.getBoolean("app_qr", false) ?: false }
    override fun onSaveInstanceState(outState: Bundle) { outState.putBoolean("app_qr", appQr); super.onSaveInstanceState(outState) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val context = requireContext(); val metrics = resources.displayMetrics
        fun dp(value: Int) = (value * metrics.density).toInt()
        val stacked = TvLayoutPolicy.stackQr(resources.configuration.screenWidthDp, metrics.heightPixels > metrics.widthPixels)
        qrSize = if (stacked) minOf((metrics.widthPixels * 0.72f).toInt(), (metrics.heightPixels * 0.38f).toInt()).coerceAtLeast(96)
            else minOf(((metrics.heightPixels - dp(128)) * 0.7f).toInt(), (metrics.widthPixels * 0.4f).toInt()).coerceAtLeast(160)
        val left = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL; gravity = Gravity.CENTER }
        left.addView(TextView(context).apply { setText(R.string.tv_qr_title); textSize = 24f; setTextColor(Color.WHITE); gravity = Gravity.CENTER; maxLines = 2 })
        qrImageView = ImageView(context).apply { layoutParams = LinearLayout.LayoutParams(qrSize, qrSize).apply { topMargin = dp(12) }; scaleType = ImageView.ScaleType.FIT_CENTER; importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO }
        left.addView(qrImageView)
        ipText = TextView(context).apply { textSize = 16f; setTextColor(0xFF67E8F9.toInt()); gravity = Gravity.CENTER; maxLines = 2 }
        left.addView(ipText)
        val right = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(16), dp(8), dp(16), dp(8)) }
        statusText = TextView(context).apply { textSize = 18f; setTextColor(Color.WHITE); accessibilityLiveRegion = View.ACCESSIBILITY_LIVE_REGION_POLITE }
        hintText = TextView(context).apply { textSize = 16f; setTextColor(0xFFCBD5E1.toInt()); setPadding(0, dp(12), 0, dp(16)) }
        right.addView(statusText); right.addView(hintText)
        fun button(label: Int) = Button(context).apply {
            id = View.generateViewId(); setText(label); textSize = 18f; setTextColor(Color.WHITE)
            minimumHeight = dp(56); isFocusable = true; setBackgroundResource(R.drawable.bg_focusable_item)
            layoutParams = LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(8) }
        }
        toggle = button(R.string.tv_qr_toggle).apply { setOnClickListener { appQr = !appQr; renderQr() } }
        val renew = button(R.string.tv_qr_refresh).apply { setOnClickListener { startSession() } }
        val close = button(R.string.tv_qr_close).apply { setOnClickListener { parentFragmentManager.popBackStack() } }
        toggle.nextFocusDownId = renew.id; renew.nextFocusUpId = toggle.id
        renew.nextFocusDownId = close.id; close.nextFocusUpId = renew.id
        right.addView(toggle); right.addView(renew); right.addView(close)
        val controls = ScrollView(context).apply { addView(right); isFocusable = false }
        return LinearLayout(context).apply {
            orientation = if (stacked) LinearLayout.VERTICAL else LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL; setPadding(dp(20), dp(16), dp(20), dp(16)); setBackgroundColor(Color.rgb(15, 23, 42))
            if (stacked) {
                addView(left, LinearLayout.LayoutParams(-1, -2))
                addView(controls, LinearLayout.LayoutParams(-1, 0, 1f))
            } else {
                addView(left, LinearLayout.LayoutParams(0, -1, 1f)); addView(controls, LinearLayout.LayoutParams(0, -1, 1f))
            }
        }
    }
    override fun onStart() { super.onStart(); startSession(); toggle.requestFocus() }
    private fun startSession() {
        qrJob?.cancel(); expiryJob?.cancel(); transferServer?.stop(); transferServer = null
        try {
            transferServer = TvTransferServer(
                onImportSuccess = { count -> view?.post { if (view != null) { statusText.text = getString(R.string.tv_qr_received, count); statusText.setTextColor(0xFF4ADE80.toInt()) } } },
                onImportError = { _ -> view?.post { if (view != null) { statusText.setText(R.string.tv_import_failed); statusText.setTextColor(0xFFFCA5A5.toInt()) } } }
            )
            statusText.setText(R.string.tv_qr_ready); statusText.setTextColor(Color.WHITE); renderQr()
            expiryJob = viewLifecycleOwner.lifecycleScope.launch {
                delay(TransferProtocol.SESSION_MILLIS)
                transferServer?.stop(); transferServer = null
                statusText.setText(R.string.tv_qr_expired); statusText.setTextColor(0xFFFBBF24.toInt()); qrImageView.setImageDrawable(null)
            }
        } catch (_: Exception) {
            statusText.setText(R.string.tv_qr_failed); statusText.setTextColor(0xFFFCA5A5.toInt()); qrImageView.setImageDrawable(null)
        }
    }
    private fun renderQr() {
        val server = transferServer ?: return
        val data = if (appQr) server.getAppQrData() else server.getBrowserQrData()
        ipText.text = getString(R.string.tv_qr_address, server.getLocalIpAddress(), TvTransferServer.PORT)
        hintText.setText(if (appQr) R.string.tv_qr_native_hint else R.string.tv_qr_browser_hint)
        qrJob?.cancel()
        qrJob = viewLifecycleOwner.lifecycleScope.launch {
            val bitmap = withContext(Dispatchers.Default) {
                val bits = QRCodeWriter().encode(data, BarcodeFormat.QR_CODE, qrSize, qrSize)
                Bitmap.createBitmap(qrSize, qrSize, Bitmap.Config.RGB_565).apply {
                    val pixels = IntArray(qrSize * qrSize) { index -> if (bits[index % qrSize, index / qrSize]) Color.BLACK else Color.WHITE }
                    setPixels(pixels, 0, qrSize, 0, 0, qrSize, qrSize)
                }
            }
            qrImageView.setImageBitmap(bitmap)
        }
    }
    private fun stopSession() { qrJob?.cancel(); expiryJob?.cancel(); transferServer?.stop(); transferServer = null }
    override fun onStop() { stopSession(); super.onStop() }
    override fun onDestroyView() { stopSession(); super.onDestroyView() }
}

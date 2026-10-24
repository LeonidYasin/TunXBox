package io.nekohasekai.sagernet.ui.diagnostics

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import io.nekohasekai.sagernet.R
import io.nekohasekai.sagernet.ui.lan.LanEnvironment
import kotlinx.coroutines.*
import java.net.Inet4Address

/** Fixed action bar, independently scrollable report, safe-close initial focus. */
class DiagnosticDialog: DialogFragment() {
    companion object {
        fun open(manager: FragmentManager,report: String,repeat: Boolean=false,extended: Boolean=false,offlineReport: String?=null) {
            if(manager.isStateSaved||manager.findFragmentByTag("connection_report")!=null)return
            require(report.toByteArray().size<=32*1024)
            DiagnosticDialog().apply {arguments=Bundle().apply {putString("report",report);putBoolean("repeat",repeat);putBoolean("extended",extended);putString("offline",offlineReport)}}.show(manager,"connection_report")
        }
    }
    override fun onCreateDialog(savedInstanceState: Bundle?): android.app.Dialog {
        val builder=android.app.AlertDialog.Builder(requireContext())
        val context=builder.context;val density=resources.displayMetrics.density
        fun dp(n: Int)=(n*density).toInt()
        val text=TextView(context).apply {id=R.id.diagnostic_report_text;text=arguments?.getString("report");textSize=16f;setPadding(dp(16),dp(12),dp(16),dp(12));isFocusable=true}
        val scroll=ScrollView(context).apply {addView(text);layoutParams=LinearLayout.LayoutParams(-1,dp((resources.configuration.screenHeightDp-220).coerceIn(120,500)))}
        val body=LinearLayout(context).apply {
            orientation=LinearLayout.VERTICAL;addView(scroll)
            if(arguments?.getBoolean("extended")==true)addView(Button(context).apply {
                id=R.id.diagnostic_extended_button;setText(R.string.diagnostic_extended)
                setOnClickListener {parentFragmentManager.setFragmentResult("diagnostic_extended",Bundle());dismiss()}
            })
        }
        builder.setTitle(R.string.diagnostic_title).setView(body)
            .setNeutralButton(R.string.diagnostic_share) {_,_-> DiagnosticShareDialog.open(parentFragmentManager,arguments?.getString("report").orEmpty(),arguments?.getString("offline"))}
            .setPositiveButton(android.R.string.ok,null)
        if(arguments?.getBoolean("repeat")==true)builder.setNegativeButton(R.string.diagnostic_repeat) {_,_->parentFragmentManager.setFragmentResult("diagnostic_repeat",Bundle())}
        return builder.create()
    }
    override fun onStart() {super.onStart();dialog?.let { io.nekohasekai.sagernet.group.SubscriptionUpdatePresentation.focusClose(it) }}
}
/** Explicit LAN export only. Back/onStop/expiry/network changes close the listener. */
class DiagnosticShareDialog: DialogFragment() {
    private var started=false
    private var offline=false
    private var server: DiagnosticShareServer?=null
    private var job: Job?=null
    private lateinit var status: TextView
    private lateinit var image: ImageView
    companion object {
        fun open(manager: FragmentManager,report: String,offlineReport: String?=null) {
            if(manager.isStateSaved||manager.findFragmentByTag("diagnostic_share")!=null)return
            require(report.toByteArray().size<=32*1024)
            DiagnosticShareDialog().apply {arguments=Bundle().apply {putString("report",report);putString("offline",offlineReport)}}.show(manager,"diagnostic_share")
        }
    }
    override fun onCreateDialog(savedInstanceState: Bundle?): android.app.Dialog {
        started=savedInstanceState?.getBoolean("started",false)?:false
        offline=savedInstanceState?.getBoolean("offline",false)?:false
        val builder=android.app.AlertDialog.Builder(requireContext())
        val context=builder.context;fun dp(n: Int)=(n*resources.displayMetrics.density).toInt()
        val height=(resources.configuration.screenHeightDp-180).coerceAtLeast(120)
        val wide=resources.configuration.screenWidthDp>=600
        val size=dp(minOf(220,if(wide)height else height/2).coerceAtLeast(64))
        image=ImageView(context).apply {id=R.id.diagnostic_qr;layoutParams=LinearLayout.LayoutParams(size,size);contentDescription=getString(R.string.diagnostic_share)}
        status=TextView(context).apply {id=R.id.diagnostic_share_status;text=getString(R.string.diagnostic_share_preparing);textSize=16f;setPadding(dp(12),dp(8),dp(12),dp(8));isFocusable=true}
        val scroll=ScrollView(context).apply {addView(status)}
        val body=LinearLayout(context).apply {
            orientation=if(wide)LinearLayout.HORIZONTAL else LinearLayout.VERTICAL;gravity=Gravity.CENTER
            layoutParams=LinearLayout.LayoutParams(-1,dp(height));addView(image)
            addView(scroll,if(wide)LinearLayout.LayoutParams(0,-1,1f) else LinearLayout.LayoutParams(-1,0,1f))
        }
        val frame=LinearLayout(context).apply {orientation=LinearLayout.VERTICAL;addView(body)}
        builder.setTitle(R.string.diagnostic_share).setView(frame).setPositiveButton(R.string.diagnostic_share_close,null)
        if(!arguments?.getString("offline").isNullOrBlank()) builder.setNeutralButton(R.string.diagnostic_offline_button,null)
        return builder.create()
    }
    override fun onStart() {
        super.onStart();dialog?.let { io.nekohasekai.sagernet.group.SubscriptionUpdatePresentation.focusClose(it) }
        (dialog as? android.app.AlertDialog)?.getButton(android.app.AlertDialog.BUTTON_NEUTRAL)?.apply {
            id=R.id.diagnostic_offline_button;setOnClickListener {showOffline()}
        }
        if(offline){showOffline();return}
        if(started){status.setText(R.string.diagnostic_share_expired);return}
        started=true
        job=lifecycleScope.launch {
            val environment=LanEnvironment(requireContext())
            val options=environment.networks().flatMap {network -> environment.manager.getLinkProperties(network.network)?.linkAddresses.orEmpty()
                .mapNotNull {it.address as? Inet4Address}.filter {it.isSiteLocalAddress&&!it.isLoopbackAddress}.map {network to it}}
            if(options.isEmpty()){status.setText(R.string.diagnostic_share_no_lan);return@launch}
            // With multiple physical interfaces/addresses the user explicitly chooses the binding.
            val selected=if(options.size==1)options.first() else suspendCancellableCoroutine<Pair<io.nekohasekai.sagernet.ui.lan.LanNetwork,Inet4Address>?> {continuation ->
                val chooser=android.app.AlertDialog.Builder(requireContext()).setTitle(R.string.diagnostic_share_network)
                    .setItems(options.map {it.second.hostAddress}.toTypedArray()){_,index->if(continuation.isActive)continuation.resumeWith(Result.success(options[index]))}
                    .setNegativeButton(android.R.string.cancel){_,_->if(continuation.isActive)continuation.resumeWith(Result.success(null))}
                    .setOnCancelListener {if(continuation.isActive)continuation.resumeWith(Result.success(null))}.show()
                continuation.invokeOnCancellation {chooser.dismiss()}
            }
            if(selected==null){status.setText(R.string.diagnostic_share_no_lan);return@launch}
            var local: DiagnosticShareServer?=null
            try {
                withContext(Dispatchers.IO){local=DiagnosticShareServer(selected.second,arguments?.getString("report").orEmpty(),filename="TunXBox-diagnostics-${System.currentTimeMillis()}.txt")}
                val sharing=checkNotNull(local);server=sharing
                val bitmap=withContext(Dispatchers.Default) {
                    val qr=QRCodeWriter().encode(sharing.url,BarcodeFormat.QR_CODE,512,512)
                    android.graphics.Bitmap.createBitmap(512,512,android.graphics.Bitmap.Config.ARGB_8888).apply {
                        setPixels(IntArray(512*512){i->if(qr[i%512,i/512])android.graphics.Color.BLACK else android.graphics.Color.WHITE},0,512,0,0,512,512)
                    }
                }
                image.setImageBitmap(bitmap);status.text=getString(R.string.diagnostic_share_help)+"\n\n"+sharing.url
                var lastState=""
                while(sharing.isOpen&&environment.current(selected.first)) {
                    val state="${sharing.remainingSeconds}/${sharing.completedTransfers}"
                    if(state!=lastState) {
                        status.text=getString(R.string.diagnostic_share_help)+"\n\n"+
                            getString(R.string.diagnostic_share_live,sharing.remainingSeconds,sharing.completedTransfers)+"\n\n"+sharing.url
                        lastState=state
                    }
                    delay(500)
                }
                image.setImageDrawable(null);status.setText(R.string.diagnostic_share_expired)
            } catch(cancelled: CancellationException){throw cancelled}
            catch(_: Exception){image.setImageDrawable(null);status.setText(R.string.diagnostic_share_failed)}
            finally {local?.close();server=null}
        }
    }
    private fun showOffline() {
        val report=arguments?.getString("offline")?:return
        if(report.toByteArray(Charsets.UTF_8).size>1400)return
        offline=true;job?.cancel();server?.close();server=null
        // Post after the cancelled LAN coroutine has run its finally block.
        image.post {
            if(!isAdded||!offline||dialog?.isShowing!=true)return@post
            try {
                val qr=QRCodeWriter().encode(report,BarcodeFormat.QR_CODE,512,512,
                    mapOf(com.google.zxing.EncodeHintType.CHARACTER_SET to "UTF-8"))
                val bitmap=android.graphics.Bitmap.createBitmap(512,512,android.graphics.Bitmap.Config.ARGB_8888).apply {
                    setPixels(IntArray(512*512){i->if(qr[i%512,i/512])android.graphics.Color.BLACK else android.graphics.Color.WHITE},0,512,0,0,512,512)
                }
                image.setImageBitmap(bitmap);image.contentDescription=getString(R.string.diagnostic_offline_button)
                status.text=getString(R.string.diagnostic_offline_help)+"\n\n"+report
            } catch(_: Exception){image.setImageDrawable(null);status.setText(R.string.diagnostic_share_failed)}
        }
    }
    override fun onSaveInstanceState(outState: Bundle){outState.putBoolean("started",started);outState.putBoolean("offline",offline);super.onSaveInstanceState(outState)}
    override fun onStop(){job?.cancel();server?.close();image.setImageDrawable(null);super.onStop()}
}

package io.nekohasekai.sagernet.ui

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.ProgressBar
import android.widget.TextView
import io.nekohasekai.sagernet.BuildConfig
import io.nekohasekai.sagernet.R
import io.nekohasekai.sagernet.ui.update.*
import kotlinx.coroutines.*
import java.io.File

/** Explicit foreground work only; closing/background pauses download, never silently installs. */
class AppUpdateFragment : ToolbarFragment(R.layout.layout_app_updates) {
    private var scope=MainScope()
    private var job: Job?=null
    private var busy=false
    private var offer: UpdateBuild?=null
    private var ready: File?=null
    private var pendingInstall=false
    private var progressEpoch=0L
    private lateinit var updater: GitHubUpdater
    private lateinit var status: TextView
    private lateinit var progress: ProgressBar
    private lateinit var check: Button
    private lateinit var download: Button
    private lateinit var install: Button
    private lateinit var pause: Button
    private lateinit var stable: RadioButton
    private lateinit var details: Button
    private lateinit var test: RadioButton
    override fun onViewCreated(view: View,savedInstanceState: Bundle?) {
        super.onViewCreated(view,savedInstanceState)
        toolbar.setTitle(R.string.menu_app_updates)
        offer=null;ready=null;pendingInstall=false;busy=false
        scope=MainScope();updater=GitHubUpdater(requireContext().applicationContext)
        status=view.findViewById(R.id.update_status);progress=view.findViewById(R.id.update_progress)
        // ScrollView.initScrollView() enables focus after superclass XML attributes.
        // Enforce read-only preview after inflation, on every recreated view.
        view.findViewById<android.widget.ScrollView>(R.id.update_log_scroll).apply {
            isFocusableInTouchMode=false;isFocusable=false
            descendantFocusability=android.view.ViewGroup.FOCUS_BLOCK_DESCENDANTS
        }
        view.findViewById<android.widget.ScrollView>(R.id.update_page_scroll).apply {
            isFocusableInTouchMode=false;isFocusable=false
            descendantFocusability=android.view.ViewGroup.FOCUS_AFTER_DESCENDANTS
        }
        check=view.findViewById(R.id.update_check);download=view.findViewById(R.id.update_download)
        install=view.findViewById(R.id.update_install);pause=view.findViewById(R.id.update_pause)
        stable=view.findViewById(R.id.update_stable);test=view.findViewById(R.id.update_test)
        details=view.findViewById(R.id.update_details)
        details.setOnClickListener {showDetails()}
        view.findViewById<RadioGroup>(R.id.update_channels).check(if(arguments?.getBoolean("test")==true) R.id.update_test else R.id.update_stable)
        view.findViewById<RadioGroup>(R.id.update_channels).setOnCheckedChangeListener { _,_ -> offer=null;ready=null;pendingInstall=false;status.setText(R.string.update_idle);render() }
        status.setText(R.string.update_idle)
        check.setOnClickListener { checkLatest() };download.setOnClickListener { downloadApk() }
        pause.setOnClickListener { progressEpoch++;job?.cancel();busy=false;showPaused();render() }
        install.setOnClickListener { installReady() };render()
    }
    private fun render() {
        if(view==null)return
        stable.isEnabled=!busy;test.isEnabled=!busy;check.isEnabled=!busy
        download.isEnabled=!busy&&offer!=null;install.isEnabled=!busy&&ready!=null;pause.isEnabled=busy
        progress.visibility=if(busy) View.VISIBLE else View.GONE
        updateFocusOrder()
    }
    /** Enabled actions only. Status text has no keyboard focus or movement-method trap. */
    internal fun updateFocusOrder() {
        val controls=listOf(stable,test,check,download,pause,install,details)
        val enabled=controls.filter {it.isEnabled&&it.visibility==View.VISIBLE}
        controls.forEach {button ->
            button.nextFocusUpId=View.NO_ID;button.nextFocusDownId=View.NO_ID
            button.setOnKeyListener {_,key,event ->
                if(event.action!=android.view.KeyEvent.ACTION_DOWN||key !in listOf(android.view.KeyEvent.KEYCODE_DPAD_UP,android.view.KeyEvent.KEYCODE_DPAD_DOWN))false
                else {
                    val targetId=if(key==android.view.KeyEvent.KEYCODE_DPAD_UP)button.nextFocusUpId else button.nextFocusDownId
                    val target=view?.findViewById<View>(targetId)
                    // ScrollView.arrowScroll otherwise spends several Down presses on a tall status pane.
                    if(target!=null&&target.isEnabled&&target.visibility==View.VISIBLE) {target.requestFocus();true} else false
                }
            }
        }
        enabled.forEachIndexed {index,button ->
            button.nextFocusUpId=enabled.getOrNull(index-1)?.id?:View.NO_ID
            button.nextFocusDownId=enabled.getOrNull(index+1)?.id?:button.id
        }
    }
    private fun showDetails() {
        val current=androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle(R.string.update_status_title).setMessage(status.text.toString())
            .setPositiveButton(android.R.string.ok,null).show()
        // Full reader is explicit; selection/scrolling never intercepts the primary action path.
        current.findViewById<TextView>(android.R.id.message)?.setTextIsSelectable(true)
        io.nekohasekai.sagernet.group.SubscriptionUpdatePresentation.focusClose(current)
    }
    private fun work(action: suspend ()->Unit) {
        if(busy)return;busy=true;render()
        job=scope.launch {
            val thisJob=currentCoroutineContext()[Job]
            try { action() }
            catch(cancelled: CancellationException) { throw cancelled }
            catch(error: Exception) { if(isAdded&&view!=null) status.text=getString(R.string.update_error,error.message?:error.javaClass.simpleName) }
            finally { if(job===thisJob) {busy=false;render()} }
        }
    }
    private fun checkLatest() {
        offer=null;ready=null;pendingInstall=false;status.setText(R.string.update_checking);progress.isIndeterminate=true
        val channel=if(test.isChecked) UpdateChannel.TEST else UpdateChannel.STABLE
        work {
            val build=withContext(Dispatchers.IO) { updater.check(channel) }
            offer=build
            status.text=getString(R.string.update_offer,BuildConfig.VERSION_NAME,build.version,build.versionCode,build.offer.apk.name,build.offer.apk.size,build.commit)
            val saved=updater.savedBytes(build)
            if(saved>0)status.append("\n\n"+getString(R.string.update_saved_bytes,saved,build.offer.apk.size))
            if(build.versionCode==BuildConfig.VERSION_CODE.toLong()) status.append("\n\n"+getString(R.string.update_same_version))
        }
    }
    private fun downloadApk() {
        val build=offer?:return;pendingInstall=false;progress.isIndeterminate=false;progress.max=1000
        val epoch=++progressEpoch
        work {
            val result=withContext(Dispatchers.IO) { updater.download(build) { p ->
                scope.launch {
                    if(view!=null&&busy&&progressEpoch==epoch) {
                        progress.progress=(p.bytes*1000/p.total).toInt()
                        status.text=DownloadFeedback.text(requireContext(),p)
                    }
                }
            } }
            progressEpoch++;ready=result.file
            status.text=getString(R.string.update_ready,result.file.absolutePath,result.signatures.installed.joinToString(),result.signatures.downloaded.joinToString())
        }
    }
    private fun installReady() {
        val build=offer?:return;val file=ready?:return
        work {
            withContext(Dispatchers.IO) { updater.verifyReady(file,build) }
            if(!isResumed||!isAdded)return@work
            pendingInstall=!updater.canInstall()
            withContext(Dispatchers.IO) {UpdateReceipt.expect(requireContext().applicationContext,build.versionCode)}
            try {updater.install(requireActivity(),file)} catch(error: Exception) {UpdateReceipt.clearExpected(requireContext());throw error}
            status.setText(if(pendingInstall) R.string.update_install_permission else R.string.update_install_launched)
        }
    }
    private fun showPaused() {
        val build=offer
        status.text=if(build==null)getString(R.string.update_paused) else getString(R.string.update_paused_bytes,updater.savedBytes(build),build.offer.apk.size)
    }
    override fun onResume() {
        super.onResume()
        if(pendingInstall&&view!=null&&!busy&&updater.canInstall()) {pendingInstall=false;installReady()}
    }
    override fun onStop() {
        if(busy) {progressEpoch++;job?.cancel();busy=false;if(view!=null) {showPaused();render()} }
        super.onStop()
    }
    override fun onDestroyView() {progressEpoch++;pendingInstall=false;job?.cancel();scope.cancel();super.onDestroyView()}
}

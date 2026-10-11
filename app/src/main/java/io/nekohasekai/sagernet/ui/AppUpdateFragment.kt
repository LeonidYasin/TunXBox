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
    private lateinit var test: RadioButton
    override fun onViewCreated(view: View,savedInstanceState: Bundle?) {
        super.onViewCreated(view,savedInstanceState)
        toolbar.setTitle(R.string.menu_app_updates)
        scope=MainScope();updater=GitHubUpdater(requireContext().applicationContext)
        status=view.findViewById(R.id.update_status);progress=view.findViewById(R.id.update_progress)
        check=view.findViewById(R.id.update_check);download=view.findViewById(R.id.update_download)
        install=view.findViewById(R.id.update_install);pause=view.findViewById(R.id.update_pause)
        stable=view.findViewById(R.id.update_stable);test=view.findViewById(R.id.update_test)
        view.findViewById<RadioGroup>(R.id.update_channels).check(if(arguments?.getBoolean("test")==true) R.id.update_test else R.id.update_stable)
        view.findViewById<RadioGroup>(R.id.update_channels).setOnCheckedChangeListener { _,_ -> offer=null;ready=null;pendingInstall=false;status.setText(R.string.update_idle);render() }
        status.setText(R.string.update_idle)
        check.setOnClickListener { checkLatest() };download.setOnClickListener { downloadApk() }
        pause.setOnClickListener { progressEpoch++;job?.cancel();busy=false;status.setText(R.string.update_paused);render() }
        install.setOnClickListener { installReady() };render()
    }
    private fun render() {
        if(view==null)return
        stable.isEnabled=!busy;test.isEnabled=!busy;check.isEnabled=!busy
        download.isEnabled=!busy&&offer!=null;install.isEnabled=!busy&&ready!=null;pause.isEnabled=busy
        progress.visibility=if(busy) View.VISIBLE else View.GONE
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
                        status.text=if(p.bytes==p.total) getString(R.string.update_verifying) else getString(R.string.update_progress_text,p.bytes,p.total,p.bytesPerSecond,p.attempt)
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
            updater.install(requireActivity(),file)
            status.setText(if(pendingInstall) R.string.update_install_permission else R.string.update_install_launched)
        }
    }
    override fun onResume() {
        super.onResume()
        if(pendingInstall&&view!=null&&!busy&&updater.canInstall()) {pendingInstall=false;installReady()}
    }
    override fun onStop() {
        if(busy) {progressEpoch++;job?.cancel();busy=false;if(view!=null) {status.setText(R.string.update_paused);render()} }
        super.onStop()
    }
    override fun onDestroyView() {progressEpoch++;pendingInstall=false;job?.cancel();scope.cancel();super.onDestroyView()}
}

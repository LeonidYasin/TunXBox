package io.nekohasekai.sagernet.ui.update
import android.content.Context
import io.nekohasekai.sagernet.R
/** Use structured phases, never parse English status messages into UI state. */
object DownloadFeedback {
 fun text(context: Context,p: DownloadProgress): String=when(p.phase) {
  DownloadPhase.RESUMING -> context.getString(R.string.update_resuming,p.bytes,p.total)
  DownloadPhase.RESTARTING -> context.getString(R.string.update_range_restart)
  DownloadPhase.RETRYING -> context.getString(R.string.update_retrying,p.bytes,p.total,p.attempt)
  DownloadPhase.VERIFYING -> context.getString(R.string.update_verifying)
  else -> if(p.bytes==p.total)context.getString(R.string.update_verifying)
   else if(p.resumedFrom>0)context.getString(R.string.update_resume_progress,p.bytes,p.total,p.bytesPerSecond,p.attempt,p.resumedFrom)
   else context.getString(R.string.update_progress_text,p.bytes,p.total,p.bytesPerSecond,p.attempt)
 }
}

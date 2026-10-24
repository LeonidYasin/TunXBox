package io.nekohasekai.sagernet.ui.update
import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import io.nekohasekai.sagernet.BuildConfig
import io.nekohasekai.sagernet.R
import io.nekohasekai.sagernet.ui.MainActivity
import io.nekohasekai.sagernet.ui.MainActivityTv
import io.nekohasekai.sagernet.ui.tv.TvUiPreferences
/** No background Activity launch, forced restart or VPN command. Android controls the installer. */
class UpdateInstalledReceiver: BroadcastReceiver() {
 override fun onReceive(context: Context,intent: Intent) {
  if(intent.action!=Intent.ACTION_MY_PACKAGE_REPLACED||!UpdateReceipt.replaced(context))return
  if(Build.VERSION.SDK_INT>=33&&ContextCompat.checkSelfPermission(context,Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED)return
  try {
   val manager=context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
   if(Build.VERSION.SDK_INT>=26)manager.createNotificationChannel(NotificationChannel("app_updates",context.getString(R.string.menu_app_updates),NotificationManager.IMPORTANCE_DEFAULT))
   val launch=Intent(context,if(TvUiPreferences.phoneMode)MainActivity::class.java else MainActivityTv::class.java)
       .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
   val pending=PendingIntent.getActivity(context,1331,launch,PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
   manager.notify(1331,NotificationCompat.Builder(context,"app_updates").setSmallIcon(R.drawable.ic_baseline_refresh_24)
       .setContentTitle(context.getString(R.string.update_installed_title,BuildConfig.VERSION_NAME))
       .setContentText(context.getString(R.string.update_open_app)).setContentIntent(pending)
       .addAction(0,context.getString(R.string.update_open_app),pending).setAutoCancel(true).build())
  } catch(_: Exception) { /* Receipt remains for the next foreground launch, even on an OEM without notifications. */ }
 }
}

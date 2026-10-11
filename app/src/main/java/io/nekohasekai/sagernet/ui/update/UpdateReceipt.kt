package io.nekohasekai.sagernet.ui.update
import android.content.Context
import io.nekohasekai.sagernet.BuildConfig
/** Only non-secret version metadata; a matching package replacement confirms success. */
object UpdateReceipt {
 private fun prefs(c: Context)=c.getSharedPreferences("app_update_receipt_v1",Context.MODE_PRIVATE)
 fun expect(c: Context,code: Long) { check(prefs(c).edit().putLong("expected",code).putLong("at",System.currentTimeMillis()).commit()) }
 fun clearExpected(c: Context) {prefs(c).edit().remove("expected").remove("at").apply()}
 fun replaced(c: Context): Boolean {
  val p=prefs(c);val code=p.getLong("expected",0);val age=System.currentTimeMillis()-p.getLong("at",0)
  if(code<=0||code!=BuildConfig.VERSION_CODE.toLong()||age !in 0..86_400_000L){clearExpected(c);return false}
  p.edit().remove("expected").remove("at").putBoolean("notice",true).apply();return true
 }
 fun consume(c: Context): Boolean {
  val p=prefs(c);val pending=p.getBoolean("notice",false)
  if(pending)p.edit().putBoolean("notice",false).apply()
  return pending
 }
}

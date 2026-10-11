package io.nekohasekai.sagernet.ui.update
import android.app.Application
import io.nekohasekai.sagernet.BuildConfig
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
@RunWith(RobolectricTestRunner::class)
@Config(sdk=[28],application=Application::class)
class UpdateFeedbackTest {
 private val context get()=RuntimeEnvironment.getApplication()
 @Before fun reset(){context.getSharedPreferences("app_update_receipt_v1",0).edit().clear().commit()}
 @Test fun resumeAndRestartAreDifferentVisibleStates() {
  val resume=DownloadFeedback.text(context,DownloadProgress(7,20,phase=DownloadPhase.RESUMING,resumedFrom=7))
  val transfer=DownloadFeedback.text(context,DownloadProgress(12,20,resumedFrom=7))
  val restart=DownloadFeedback.text(context,DownloadProgress(0,20,phase=DownloadPhase.RESTARTING))
  assertTrue(resume.contains("7"));assertTrue(transfer.contains("7"));assertNotEquals(resume,restart)
 }
 @Test fun explicitSameCodeReplacementShowsNoticeOnlyOnce(){UpdateReceipt.expect(context,BuildConfig.VERSION_CODE.toLong());assertTrue(UpdateReceipt.replaced(context));assertTrue(UpdateReceipt.consume(context));assertFalse(UpdateReceipt.consume(context));assertFalse(UpdateReceipt.replaced(context))}
 @Test fun normalLaunchOrCancelledDifferentVersionCannotClaimInstalled(){assertFalse(UpdateReceipt.replaced(context));UpdateReceipt.expect(context,BuildConfig.VERSION_CODE.toLong()+1);assertFalse(UpdateReceipt.replaced(context));assertFalse(UpdateReceipt.consume(context))}
 @Test fun noticeDoesNotDependOnNotificationPermission(){UpdateReceipt.expect(context,BuildConfig.VERSION_CODE.toLong());assertTrue(UpdateReceipt.replaced(context));assertTrue(UpdateReceipt.consume(context))}
}

package io.nekohasekai.sagernet.group
import android.app.Application
import android.util.Base64
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
@RunWith(RobolectricTestRunner::class)
@Config(sdk=[28],application=Application::class)
class SubscriptionTitleTest {
 @Test fun plainAndUtf8Base64ProfileTitleHavePriority(){val name="Моя подписка";val encoded="base64:"+Base64.encodeToString(name.toByteArray(),Base64.NO_WRAP);assertEquals(name,SubscriptionTitle.resolve(encoded,"attachment; filename=other.txt"));assertEquals("Provider",SubscriptionTitle.resolve("Provider",null))}
 @Test fun rfc5987AndQuotedFilenameFallback(){assertEquals("Моя",SubscriptionTitle.resolve(null,"attachment; filename*=UTF-8''%D0%9C%D0%BE%D1%8F.txt"));assertEquals("A+B",SubscriptionTitle.resolve(null,"attachment; filename*=UTF-8''A+B.txt"));assertEquals("Group",SubscriptionTitle.resolve(null,"attachment; filename=\"Group.txt\""))}
 @Test fun malformedExtendedFilenameStillUsesPlainFallback(){assertEquals("Group",SubscriptionTitle.resolve(null,"attachment; filename*=UTF-8''%ZZ; filename=Group.txt"))}
 @Test fun malformedTitleFallsBackWithoutFailingImport(){assertEquals("Group",SubscriptionTitle.resolve("base64:???","attachment; filename=Group.txt"));assertNull(SubscriptionTitle.resolve("base64:???",null))}
 @Test fun explicitNamesAndConcurrentRenameAreNotOverridden(){assertEquals("My custom group",SubscriptionTitle.choose("My custom group","Remote",null));assertEquals("Remote",SubscriptionTitle.choose("Subscription #123","Remote",null));assertEquals("Subscription #custom",SubscriptionTitle.choose("Subscription #custom","Remote",null))}
 @Test fun metadataCanBeAbsentAndBodyDoesNotUseFirstProfileName(){assertNull(SubscriptionTitle.resolve(null,null,"vless://fixture#Profile"));assertEquals("Collection",SubscriptionTitle.resolve(null,null,"{\"title\":\"Collection\",\"outbounds\":[]}"));assertEquals("Local",SubscriptionTitle.resolve(null,null,"# profile-title: Local\nvless://fixture"))}
 @Test fun controlsAndLongTitlesAreBounded(){assertEquals("A B",SubscriptionTitle.clean("A\nB"));assertEquals(128,SubscriptionTitle.clean("x".repeat(500))!!.length);assertNull(SubscriptionTitle.clean("x".repeat(5000)))}
}

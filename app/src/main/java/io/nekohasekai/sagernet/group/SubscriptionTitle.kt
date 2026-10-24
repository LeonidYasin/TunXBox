package io.nekohasekai.sagernet.group

import android.util.Base64
import java.net.URLDecoder
import java.nio.ByteBuffer
import java.nio.charset.CodingErrorAction
import org.json.JSONObject

/** Advisory UI metadata only; never a URL, executable setting or diagnostic log payload. */
object SubscriptionTitle {
 fun generated(name: String?)=name.isNullOrBlank()||Regex("Subscription #\\d+").matches(name.orEmpty())
 fun clean(value: String?): String? {
  if(value==null||value.length>4096)return null
  val text=value.replace(Regex("[\\p{Cc}\\p{Cf}]")," ").trim()
  if(text.isBlank())return null
  return text.substring(0,text.offsetByCodePoints(0,text.codePointCount(0,text.length).coerceAtMost(128)))
 }
 private fun title(value: String?): String?=runCatching {
  if(value==null||value.length>4096)return null
  val raw=value.trim()
  if(!raw.startsWith("base64:",ignoreCase=true))return clean(raw)
  val bytes=Base64.decode(raw.substring(7),Base64.DEFAULT)
  val text=Charsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT).onUnmappableCharacter(CodingErrorAction.REPORT).decode(ByteBuffer.wrap(bytes)).toString()
  clean(text)
 }.getOrNull()
 private fun filename(header: String?): String?=runCatching {
  if(header==null||header.length>4096)return null
  val extended=Regex("(?:^|;)\\s*filename\\*\\s*=\\s*([^;]+)",RegexOption.IGNORE_CASE).find(header)?.groupValues?.get(1)?.trim()?.trim('"')
  val plain=Regex("(?:^|;)\\s*filename\\s*=\\s*(?:\"([^\"]*)\"|([^;]*))",RegexOption.IGNORE_CASE).find(header)
  val decoded=extended?.let {
   val parts=it.split("'",limit=3)
   if(parts.size!=3||!parts[0].equals("utf-8",true))null
   else runCatching { URLDecoder.decode(parts[2].replace("+","%2B"),"UTF-8") }.getOrNull()
  }?:plain?.let {it.groupValues[1].ifBlank {it.groupValues[2]}.trim()}
  clean(decoded?.substringAfterLast('/')?.substringAfterLast('\\')
    ?.removeSuffix(".txt")?.removeSuffix(".yaml")?.removeSuffix(".yml")?.removeSuffix(".json"))
 }.getOrNull()
 fun resolve(profileTitle: String?,disposition: String?,body: String?=null): String? {
  title(profileTitle)?.let {return it}
  filename(disposition)?.let {return it}
  if(body==null||body.length>2*1024*1024)return null
  // Only explicit group-level metadata, never the first profile/server/tag/secret URL.
  val json=if(body.trimStart().startsWith("{"))runCatching {JSONObject(body)}.getOrNull() else null
  for(key in listOf("profile-title","title","remarks","name")) {
   val value=json?.opt(key)
   if(value is String)title(value)?.let {return it}
  }
  val comment=body.lineSequence().take(10).firstOrNull {it.trimStart().startsWith("# profile-title:",true)}
  return comment?.let {title(it.substringAfter(':'))}
 }
 fun choose(current: String?,profileTitle: String?,disposition: String?,body: String?=null): String? =
  if(generated(current))resolve(profileTitle,disposition,body)?:current else current
}

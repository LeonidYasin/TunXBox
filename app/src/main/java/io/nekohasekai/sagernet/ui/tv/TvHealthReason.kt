package io.nekohasekai.sagernet.ui.tv
import android.content.Context
import io.nekohasekai.sagernet.R
import io.nekohasekai.sagernet.group.SubscriptionFailureCategory as C
object TvHealthReason {
 fun short(context: Context,category: C)=when(category) {
  C.REALITY -> "REALITY"
  C.TLS_TIME,C.TLS_TRUST,C.TLS_HANDSHAKE -> "TLS"
  C.DNS -> "DNS"
  C.TIMEOUT -> context.getString(io.nekohasekai.sagernet.R.string.diagnostic_timeout_short)
  C.UNSUPPORTED -> context.getString(io.nekohasekai.sagernet.R.string.diagnostic_protocol_short)
  else -> context.getString(io.nekohasekai.sagernet.R.string.diagnostic_error_short)
 }

 fun text(context: Context,category: C)=context.getString(when(category) {
  C.REALITY -> R.string.tv_health_reason_reality
  C.TLS_TIME -> R.string.tv_health_reason_time
  C.TLS_TRUST,C.TLS_HANDSHAKE -> R.string.tv_health_reason_tls
  C.DNS -> R.string.tv_health_reason_dns
  C.TIMEOUT -> R.string.tv_health_reason_timeout
  C.NETWORK -> R.string.tv_health_reason_network
  C.REFUSED -> R.string.tv_health_reason_refused
  C.UNSUPPORTED -> R.string.tv_health_reason_unsupported
  C.CORE -> R.string.tv_health_reason_core
  C.BUSY -> R.string.tv_health_reason_busy
  C.HTTP -> R.string.tv_health_reason_http
  else -> R.string.tv_health_reason_unknown
 })
}

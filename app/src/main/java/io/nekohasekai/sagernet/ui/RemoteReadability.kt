package io.nekohasekai.sagernet.ui

object RemoteReadability {
    /** Keep phone touch density; never shrink a user's larger accessibility font scale. */
    fun fontScale(remoteOnlyDevice: Boolean, requested: Float): Float {
        val safe = if (requested.isFinite() && requested > 0) requested else 1f
        return if (remoteOnlyDevice) safe.coerceAtLeast(1.2f) else safe
    }
}

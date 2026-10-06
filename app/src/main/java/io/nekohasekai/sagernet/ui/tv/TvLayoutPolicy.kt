package io.nekohasekai.sagernet.ui.tv

/** Keep a focus-zoomed card inside small windows, while preserving 300dp TV cards. */
object TvLayoutPolicy {
    fun cardWidthDp(windowWidthDp: Int): Int = ((windowWidthDp - 48).coerceAtLeast(0) / 1.1f).toInt().coerceIn(120, 300)
    fun stackQr(windowWidthDp: Int, portrait: Boolean): Boolean = portrait && windowWidthDp < 600
}

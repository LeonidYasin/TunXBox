package io.nekohasekai.sagernet.ui.tv

/** Keep a focus-zoomed card inside small windows, while preserving 300dp TV cards. */
object TvLayoutPolicy {
    fun cardWidthDp(windowWidthDp: Int): Int = ((windowWidthDp - 48).coerceAtLeast(0) / 1.1f).toInt().coerceIn(120, 300)
    fun stackQr(windowWidthDp: Int, portrait: Boolean): Boolean = portrait && windowWidthDp < 600

    /**
     * Height budget for a scrollable list inside an AlertDialog.
     *
     * A plain list dialog grows with every profile until its button row leaves the screen: on a
     * 540dp-tall TV ten profiles are enough to push "turn off" out of reach. The list gets a fixed
     * height instead, and the remaining ~150dp for the title and buttons always stays visible.
     */
    fun dialogListHeightDp(screenHeightDp: Int, rowHeightDp: Int = 48): Int {
        val cap = (screenHeightDp * 0.45f).toInt()
        return cap.coerceAtLeast(rowHeightDp * 3).coerceAtMost(rowHeightDp * 7)
    }
}

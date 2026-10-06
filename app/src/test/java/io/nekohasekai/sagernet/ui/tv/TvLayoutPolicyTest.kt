package io.nekohasekai.sagernet.ui.tv

import org.junit.Assert.*
import org.junit.Test

class TvLayoutPolicyTest {
    @Test fun phoneCardFitsIncludingFocusZoomAndMargins() {
        for (width in listOf(240, 320, 360, 390, 480, 600)) {
            assertTrue(TvLayoutPolicy.cardWidthDp(width) * 1.1f + 48 <= width + 1)
        }
    }
    @Test fun televisionKeepsLargeCards() { assertEquals(300, TvLayoutPolicy.cardWidthDp(960)) }
    @Test fun narrowPortraitQrStacksAndLandscapeStaysSideBySide() {
        assertTrue(TvLayoutPolicy.stackQr(390, true))
        assertFalse(TvLayoutPolicy.stackQr(844, false))
        assertFalse(TvLayoutPolicy.stackQr(800, true))
    }
}

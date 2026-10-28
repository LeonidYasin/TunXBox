package io.nekohasekai.sagernet.ui.tv

import android.content.Context
import android.graphics.Paint
import android.util.TypedValue
import kotlin.math.ceil
import kotlin.math.roundToInt

/**
 * One explicit tile size for every card that is not a profile card.
 *
 * Leanback measures each tile with the height it asks for and only makes the row as tall as the
 * tallest one, so a row that mixed three-line cards with a list of LAN addresses looked ragged:
 * the address tile grew with every address while its neighbours stayed short, and the row grew
 * with it. Every action, connection, quality and LAN tile therefore asks for exactly the same
 * [widthPx] and [heightPx]; shorter content is centered inside the tile, longer address lists
 * shrink their autosized lines instead of making one tile taller than its neighbours.
 */
object TvCardMetrics {
    /** Title size of a tile; two lines are reserved on every tile. */
    private const val TITLE_SP = 20f

    /** Body size of a tile; three lines are reserved on every tile. */
    private const val DETAIL_SP = 16f

    /** Horizontal and vertical padding shared by every tile. */
    private const val PADDING_DP = 16

    /**
     * Space beyond the text block: the LAN label and hint, the focused card stroke and rounding of
     * the font metrics. Without it the last address line would touch the tile edge.
     */
    private const val SLACK_DP = 16

    fun widthPx(context: Context): Int =
        dp(context, TvLayoutPolicy.cardWidthDp(context.resources.configuration.screenWidthDp))

    /**
     * Two 20sp title lines plus three 16sp detail lines, the largest text block a tile carries.
     * Derived from real font metrics so a larger system font scale grows the tile instead of
     * clipping the last line.
     */
    fun heightPx(context: Context): Int = dp(context, PADDING_DP * 2 + SLACK_DP) +
        ceil(lineHeightPx(context, TITLE_SP) * 2 + lineHeightPx(context, DETAIL_SP) * 3).toInt()

    /**
     * Exact box for one text block. A text box that keeps its height no matter what it contains is
     * what keeps the tile height stable while the content changes (profile switch, new counters).
     */
    fun lineBoxPx(context: Context, sp: Float, lines: Int): Int =
        ceil(lineHeightPx(context, sp) * lines).toInt() + dp(context, 2)

    /** Line height of [sp] text, in px, including the current system font scale. */
    fun lineHeightPx(context: Context, sp: Float): Float {
        val paint = Paint()
        paint.textSize = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            sp,
            context.resources.displayMetrics,
        )
        val metrics = paint.fontMetrics
        return ceil(metrics.descent - metrics.ascent).toFloat()
    }

    fun dp(context: Context, value: Int): Int =
        (value * context.resources.displayMetrics.density).roundToInt()
}

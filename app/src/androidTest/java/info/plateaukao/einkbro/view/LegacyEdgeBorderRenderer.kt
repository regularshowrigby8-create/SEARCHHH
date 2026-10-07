package info.plateaukao.einkbro.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import info.plateaukao.einkbro.preference.UiBorder
import info.plateaukao.einkbro.view.compose.UiThemeState

/**
 * Pre-fix stamp/dash/solid algorithms from ThemedEdgeBorderView at 5896283.
 * Numeric values are named but the original arithmetic and drawing order remain.
 * Allocations construct test reference images, not frames in a View draw callback.
 * Do not alter this reference merely to match changed production rendering.
 */
internal class LegacyEdgeBorderRenderer(
    private val context: Context,
    width: Int,
    height: Int,
    private val edgeAtTop: Boolean,
) {
    private val width = width.toFloat()
    private val height = height.toFloat()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val edgePath = Path()
    private val fillPath = Path()
    private val background = ThemedBorders.baseArgb(context)
    private val accent = ThemedBorders.accentArgb(context)

    fun render(canvas: Canvas) {
        when (UiThemeState.uiBorder.value) {
            UiBorder.STAMP -> stamp(canvas)

            UiBorder.DASHED ->
                straight(
                    canvas,
                    dp(EDGE_DP),
                    dp(DASH_STROKE_DP),
                    DashPathEffect(floatArrayOf(dp(DASH_DP), dp(GAP_DP)), 0f),
                )

            UiBorder.CLASSIC -> {
                val stroke = dp(kotlin.math.max(UiBorder.CLASSIC.widthDp, 1f))
                straight(canvas, stroke / DIAMETER_FACTOR, stroke, null)
            }

            else -> error("This frozen reference covers stamp, dashed and classic only")
        }
    }

    private fun stamp(canvas: Canvas) {
        val radius = dp(STAMP_RADIUS_DP)
        val edge = dp(EDGE_DP)
        val margin = MARGIN_FACTOR * radius
        val span = width - DIAMETER_FACTOR * margin
        val centers =
            if (span < DIAMETER_FACTOR * radius) {
                listOf(width / DIAMETER_FACTOR)
            } else {
                val count = kotlin.math.max(1, (span / (SPACING_FACTOR * radius)).toInt())
                val step = span / count
                List(count) { margin + (it + CENTER_OFFSET) * step }
            }
        edgePath.reset()
        edgePath.moveTo(0f, y(edge))
        centers.forEach { center ->
            edgePath.lineTo(center - radius, y(edge))
            val rect = RectF(center - radius, y(edge) - radius, center + radius, y(edge) + radius)
            edgePath.arcTo(rect, HALF_TURN, if (edgeAtTop) -HALF_TURN else HALF_TURN)
        }
        edgePath.lineTo(width, y(edge))
        fillPath.reset()
        fillPath.addPath(edgePath)
        fillPath.lineTo(width, y(height))
        fillPath.lineTo(0f, y(height))
        fillPath.close()
        paint.style = Paint.Style.FILL
        paint.pathEffect = null
        paint.color = background
        canvas.drawPath(fillPath, paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = dp(STAMP_STROKE_DP)
        paint.color = accent
        canvas.drawPath(edgePath, paint)
    }

    private fun straight(
        canvas: Canvas,
        edge: Float,
        stroke: Float,
        effect: DashPathEffect?,
    ) {
        paint.style = Paint.Style.FILL
        paint.pathEffect = null
        paint.color = background
        if (edgeAtTop) {
            canvas.drawRect(0f, edge, width, height, paint)
        } else {
            canvas.drawRect(0f, 0f, width, height - edge, paint)
        }
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = stroke
        paint.pathEffect = effect
        paint.color = accent
        canvas.drawLine(0f, y(edge), width, y(edge), paint)
        paint.pathEffect = null
    }

    private fun dp(value: Float): Float = value * context.resources.displayMetrics.density

    private fun y(value: Float): Float = if (edgeAtTop) value else height - value

    private companion object {
        const val STAMP_RADIUS_DP = 3f
        const val EDGE_DP = 0.75f
        const val STAMP_STROKE_DP = 1.25f
        const val DASH_STROKE_DP = 1.5f
        const val DASH_DP = 5f
        const val GAP_DP = 4f
        const val MARGIN_FACTOR = 3f
        const val DIAMETER_FACTOR = 2f
        const val SPACING_FACTOR = 3.5f
        const val CENTER_OFFSET = 0.5f
        const val HALF_TURN = 180f
    }
}

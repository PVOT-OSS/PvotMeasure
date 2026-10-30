package org.prauga.measure.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.dp
import kotlin.math.hypot

/**
 * Horizon shown while the device stands upright: a surface that stays level with the
 * world as the device rolls, and rises or falls as it leans back or forward. It sits
 * exactly on the side marks only when the device stands perfectly vertical.
 *
 * @param rollDegrees clockwise screen rotation that keeps the surface level
 * @param leanFraction -1..1 shift of the surface, 1 being a quarter screen up
 * @param levelProgress 0 while tilted, 1 once level; blends the fill into the level colours
 */
@Composable
fun LevelHorizon(
    rollDegrees: Float,
    leanFraction: Float,
    levelProgress: Float,
    colors: LevelColors,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        // Large enough to cover the screen at any rotation
        val extent = hypot(size.width, size.height)
        val surfaceY = center.y - leanFraction * size.height / 4

        rotate(rollDegrees, center) {
            val surfaceTint = lerp(colors.surface, colors.levelSurface, levelProgress)
            val edge = lerp(colors.content, colors.onLevelBackground, levelProgress)
            // Brightest at the edge and mostly gone a quarter screen down, leaving the rest
            // only a hair lighter than above, so it reads as liquid rather than a block
            drawRect(
                brush = Brush.verticalGradient(
                    0f to surfaceTint.copy(alpha = 0.9f),
                    0.04f to surfaceTint.copy(alpha = 0.45f),
                    0.25f to surfaceTint.copy(alpha = 0.12f),
                    1f to surfaceTint.copy(alpha = 0.04f),
                    startY = surfaceY,
                    endY = surfaceY + size.height
                ),
                topLeft = Offset(center.x - extent, surfaceY),
                size = Size(extent * 2, extent * 2)
            )
            // Lit band just under the edge, like light catching the top of a liquid
            val bandHeight = 28.dp.toPx()
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(edge.copy(alpha = 0.18f), Color.Transparent),
                    startY = surfaceY,
                    endY = surfaceY + bandHeight
                ),
                topLeft = Offset(center.x - extent, surfaceY),
                size = Size(extent * 2, bandHeight)
            )
            drawLine(
                color = edge.copy(alpha = 0.85f),
                start = Offset(center.x - extent, surfaceY),
                end = Offset(center.x + extent, surfaceY),
                strokeWidth = 2.dp.toPx()
            )
        }
    }
}

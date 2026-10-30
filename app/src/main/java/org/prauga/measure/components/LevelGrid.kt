package org.prauga.measure.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/** Grid of square cells, centred so the middle of the screen sits inside a cell. */
@Composable
fun LevelGrid(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        if (color.alpha <= 0f) return@Canvas
        val stroke = 1.dp.toPx()
        val lines = gridLines(size)

        for (x in lines.xs) drawLine(color, Offset(x, 0f), Offset(x, size.height), stroke)
        for (y in lines.ys) drawLine(color, Offset(0f, y), Offset(size.width, y), stroke)
    }
}

/** Positions of the vertical ([xs]) and horizontal ([ys]) grid lines. */
class GridLines(val xs: List<Float>, val ys: List<Float>)

/** Five cells across, laid out so the screen centre falls in the middle of a cell. */
fun gridLines(size: Size): GridLines {
    val spacing = size.width / 5
    fun positions(extent: Float): List<Float> {
        val first = (extent / 2 - spacing / 2) % spacing
        return generateSequence(first) { it + spacing }.takeWhile { it <= extent }.toList()
    }
    return GridLines(positions(size.width), positions(size.height))
}

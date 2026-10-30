package org.prauga.measure.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Two glass spheres that drift apart along the tilt direction and merge into one
 * bubble as the device approaches level.
 *
 * @param x gravity along the screen's x axis (right), as a unit vector component
 * @param y gravity along the screen's y axis (up), as a unit vector component
 * @param levelProgress 0 while tilted, 1 once level; blends the spheres into the level bubble
 */
@Composable
fun LevelSpheres(
    x: Float,
    y: Float,
    levelProgress: Float,
    colors: LevelColors,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val radius = size.minDimension * 0.22f
        val planar = sqrt(x * x + y * y)
        val tilt = flatTiltDegrees(x, y)

        val direction =
            if (planar > 0.0001f) Offset(x / planar, -y / planar) else Offset(0f, 1f)
        // Each degree of tilt pushes the spheres a tenth of a radius apart. Sideways they may
        // clip a fifth past the narrow edges so they can still fully separate; vertically
        // they stop short of the nav bar
        val sideLimit = size.width / 2 - radius * 0.8f
        val verticalLimit = size.height / 2 - radius - size.height * 0.08f
        val maxSeparation = minOf(
            if (abs(direction.x) > 0.001f) sideLimit / abs(direction.x) else Float.MAX_VALUE,
            if (abs(direction.y) > 0.001f) verticalLimit / abs(direction.y) else Float.MAX_VALUE
        ).coerceAtLeast(0f)
        // Ease the spheres into one bubble as the level turns green, so 0° never shows
        // two slightly offset spheres
        val separation =
            (radius * tilt / 10f).coerceAtMost(maxSeparation) * (1f - levelProgress)

        val c1 = center + direction * separation
        val c2 = center - direction * separation
        val blob = metaballPath(c1, c2, radius)

        drawPath(blob, lerp(colors.surface, colors.levelSurface, levelProgress))
        val rim = lerp(colors.content, colors.onLevelSurface, levelProgress)
        val gridAlpha = 0.11f * (1f - levelProgress)
        clipPath(blob) {
            if (separation < 0.5f) {
                drawRefractedGrid(c1, radius, colors.content.copy(alpha = gridAlpha))
                drawSphereShading(c1, radius, rim, levelProgress)
            } else {
                val middle = (c1 + c2) / 2f
                for ((c, other) in listOf(c1 to c2, c2 to c1)) {
                    // Each sphere bends the grid on its own half, split where they meet
                    clipPath(circle(c, radius)) {
                        clipPath(halfPlane(middle, toward = c, extent = size.maxDimension * 2)) {
                            drawRefractedGrid(c, radius, colors.content.copy(alpha = gridAlpha))
                        }
                    }
                    // Shade each sphere only outside the other, so overlapping spheres read
                    // as one merged blob instead of two stacked discs
                    clipPath(circle(other, radius), ClipOp.Difference) {
                        drawSphereShading(c, radius, rim, levelProgress)
                    }
                }
            }
        }
        drawPath(
            path = blob,
            color = rim.copy(alpha = 0.14f + 0.36f * levelProgress),
            style = Stroke(width = 1.dp.toPx())
        )
    }
}

/** Lighter rim, so a sphere reads as glass rather than a flat disc. */
private fun DrawScope.drawSphereShading(
    center: Offset,
    radius: Float,
    rim: Color,
    levelProgress: Float
) {
    drawCircle(
        brush = Brush.radialGradient(
            0f to Color.Transparent,
            0.7f to Color.Transparent,
            1f to rim.copy(alpha = 0.07f + 0.13f * levelProgress),
            center = center,
            radius = radius
        ),
        radius = radius,
        center = center
    )
}

/**
 * The background grid as seen through a glass sphere: magnified at the centre and bent
 * toward the rim, which is what gives the spheres their curved inner lines.
 */
private fun DrawScope.drawRefractedGrid(center: Offset, radius: Float, color: Color) {
    if (color.alpha <= 0f) return
    val stroke = Stroke(width = 1.dp.toPx())
    val lines = gridLines(size)

    for (x in lines.xs) {
        val dx = x - center.x
        if (abs(dx) >= radius) continue
        val half = sqrt(radius * radius - dx * dx)
        val chord = refractedChord(
            Offset(x, center.y - half), Offset(x, center.y + half), center, radius
        )
        drawPath(chord, color, style = stroke)
    }
    for (y in lines.ys) {
        val dy = y - center.y
        if (abs(dy) >= radius) continue
        val half = sqrt(radius * radius - dy * dy)
        val chord = refractedChord(
            Offset(center.x - half, y), Offset(center.x + half, y), center, radius
        )
        drawPath(chord, color, style = stroke)
    }
}

/** A straight grid segment behind the sphere, traced through [refract]. */
private fun refractedChord(start: Offset, end: Offset, center: Offset, radius: Float): Path =
    Path().apply {
        val steps = 32
        for (i in 0..steps) {
            val p = refract(start + (end - start) * (i / steps.toFloat()), center, radius)
            if (i == 0) moveTo(p.x, p.y) else lineTo(p.x, p.y)
        }
    }

/** Where a point behind a glass sphere appears: pushed out from the centre, pinned at the rim. */
private fun refract(point: Offset, center: Offset, radius: Float): Offset {
    val offset = point - center
    val r = offset.getDistance() / radius
    if (r < 0.0001f) return point
    return center + offset * (sin(r * PI.toFloat() / 2) / r)
}

private fun circle(center: Offset, radius: Float) = Path().apply { addOval(Rect(center, radius)) }

/** The half of the plane on [toward]'s side of a line through [through]. */
private fun halfPlane(through: Offset, toward: Offset, extent: Float): Path {
    val direction = (toward - through) / (toward - through).getDistance()
    val across = Offset(-direction.y, direction.x)
    val a = through + across * extent
    val b = through - across * extent
    return Path().apply {
        moveTo(a.x, a.y)
        lineTo(b.x, b.y)
        lineTo(b.x + direction.x * extent, b.y + direction.y * extent)
        lineTo(a.x + direction.x * extent, a.y + direction.y * extent)
        close()
    }
}

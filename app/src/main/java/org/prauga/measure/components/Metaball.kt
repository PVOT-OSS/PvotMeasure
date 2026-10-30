package org.prauga.measure.components

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import kotlin.math.PI
import kotlin.math.acos
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

private const val HALF_PI = (PI / 2).toFloat()
private const val FULL_PI = PI.toFloat()

/**
 * Outline of two equal circles that grow a smooth neck between them as they get close,
 * so they read as one liquid blob. Based on Hiroyuki Sato's metaball construction.
 */
fun metaballPath(
    c1: Offset,
    c2: Offset,
    radius: Float,
    spread: Float = 0.5f,
    handleSize: Float = 2.4f
): Path {
    val circles = Path.combine(
        PathOperation.Union,
        Path().apply { addOval(Rect(c1, radius)) },
        Path().apply { addOval(Rect(c2, radius)) }
    )

    val distance = (c2 - c1).getDistance()
    if (distance < 1f || distance > radius * 2.6f) return circles

    // Equal radii, so the general (r1² + d² - r2²) / (2·r1·d) term reduces to d / 2r
    val overlap = if (distance < radius * 2) acos(distance / (radius * 2)) else 0f
    val angle = atan2(c2.y - c1.y, c2.x - c1.x)
    val maxSpread = HALF_PI

    val a1 = angle + overlap + (maxSpread - overlap) * spread
    val a2 = angle - overlap - (maxSpread - overlap) * spread
    val a3 = angle + FULL_PI - overlap - (FULL_PI - overlap - maxSpread) * spread
    val a4 = angle - FULL_PI + overlap + (FULL_PI - overlap - maxSpread) * spread

    val p1 = c1 + polar(radius, a1)
    val p2 = c1 + polar(radius, a2)
    val p3 = c2 + polar(radius, a3)
    val p4 = c2 + polar(radius, a4)

    val handleBase = min(spread * handleSize, (p1 - p3).getDistance() / (radius * 2))
    val handle = radius * handleBase * min(1f, distance / radius)

    val h1 = p1 + polar(handle, a1 - HALF_PI)
    val h2 = p2 + polar(handle, a2 + HALF_PI)
    val h3 = p3 + polar(handle, a3 + HALF_PI)
    val h4 = p4 + polar(handle, a4 - HALF_PI)

    val neck = Path().apply {
        moveTo(p1.x, p1.y)
        cubicTo(h1.x, h1.y, h3.x, h3.y, p3.x, p3.y)
        lineTo(p4.x, p4.y)
        cubicTo(h4.x, h4.y, h2.x, h2.y, p2.x, p2.y)
        close()
    }
    return Path.combine(PathOperation.Union, circles, neck)
}

private fun polar(length: Float, angle: Float) = Offset(cos(angle) * length, sin(angle) * length)

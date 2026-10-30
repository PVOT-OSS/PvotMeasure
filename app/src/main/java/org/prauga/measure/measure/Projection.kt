package org.prauga.measure.measure

import io.github.sceneview.math.Position

/** A point on screen, in pixels from the top left of the AR view. */
data class ScreenPoint(val x: Float, val y: Float)

/** Clip-space w below this is at or behind the camera plane and cannot be drawn. */
private const val MIN_W = 0.05f

/** [a] times [b] as column-major 4x4 matrices, as OpenGL and ARCore lay them out. */
fun multiplyMatrices(a: FloatArray, b: FloatArray): FloatArray {
    val out = FloatArray(16)
    for (col in 0 until 4) {
        for (row in 0 until 4) {
            var sum = 0f
            for (k in 0 until 4) sum += a[k * 4 + row] * b[col * 4 + k]
            out[col * 4 + row] = sum
        }
    }
    return out
}

private fun clip(m: FloatArray, p: Position): FloatArray = floatArrayOf(
    m[0] * p.x + m[4] * p.y + m[8] * p.z + m[12],
    m[1] * p.x + m[5] * p.y + m[9] * p.z + m[13],
    m[3] * p.x + m[7] * p.y + m[11] * p.z + m[15],
)

private fun toScreen(c: FloatArray, width: Float, height: Float) = ScreenPoint(
    x = (c[0] / c[2] * 0.5f + 0.5f) * width,
    y = (1f - (c[1] / c[2] * 0.5f + 0.5f)) * height,
)

/**
 * Where world point [p] lands on a view of [width] by [height] pixels, given the camera's
 * view-projection matrix, or null when it is behind the camera.
 */
fun projectToScreen(viewProjection: FloatArray, p: Position, width: Float, height: Float): ScreenPoint? {
    val c = clip(viewProjection, p)
    return if (c[2] > MIN_W) toScreen(c, width, height) else null
}

/**
 * The visible part of the segment [a]-[b] on screen. A segment that runs behind the camera is cut
 * where it crosses the camera plane, so a line from a point in front to one behind still draws.
 * Null when the whole segment is behind the camera.
 */
fun projectSegment(
    viewProjection: FloatArray,
    a: Position,
    b: Position,
    width: Float,
    height: Float,
): Pair<ScreenPoint, ScreenPoint>? {
    var ca = clip(viewProjection, a)
    var cb = clip(viewProjection, b)
    if (ca[2] <= MIN_W && cb[2] <= MIN_W) return null
    if (ca[2] <= MIN_W) ca = cut(ca, cb)
    if (cb[2] <= MIN_W) cb = cut(cb, ca)
    return toScreen(ca, width, height) to toScreen(cb, width, height)
}

/** [behind] moved along the line to [front] until it reaches the near plane. */
private fun cut(behind: FloatArray, front: FloatArray): FloatArray {
    val t = (MIN_W - behind[2]) / (front[2] - behind[2])
    return FloatArray(3) { behind[it] + (front[it] - behind[it]) * t }
}

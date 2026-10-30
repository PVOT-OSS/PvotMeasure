// Adapted from the SceneView demo app (https://github.com/sceneview/sceneview,
// samples/android-demo, Apache-2.0). See THIRD_PARTY_NOTICES.md.

package org.prauga.measure.measure

import io.github.sceneview.math.Position
import kotlin.math.sqrt

/**
 * Pure measurement maths. Free of ARCore and Filament types: everything takes plain world-space
 * [Position]s, so it runs in a JVM unit test with no device. The arithmetic here is exact; the
 * points it is fed are good to a few centimetres at best.
 */

/** Straight-line distance between two world-space points, in metres. */
fun measureDistanceMeters(a: Position, b: Position): Float {
    val dx = b.x - a.x
    val dy = b.y - a.y
    val dz = b.z - a.z
    return sqrt(dx * dx + dy * dy + dz * dz)
}

/** Midpoint of the segment [a]-[b], where a segment's label is anchored. */
fun measureMidpoint(a: Position, b: Position): Position =
    Position(x = (a.x + b.x) / 2f, y = (a.y + b.y) / 2f, z = (a.z + b.z) / 2f)

/** Total length of the polyline through [points], in metres. */
fun measureChainMeters(points: List<Position>): Float {
    var total = 0f
    for (i in 0 until points.size - 1) total += measureDistanceMeters(points[i], points[i + 1])
    return total
}

/**
 * True when any point of [current] has moved more than [epsilonMeters] from [previous], or when
 * the lists differ in size. Anchors drift as tracking refines, and writing every frame's poses
 * into Compose state would recompose the scene for jitter no one can see.
 */
fun measurePointsMoved(
    previous: List<Position>,
    current: List<Position>,
    epsilonMeters: Float = 0.001f,
): Boolean {
    if (previous.size != current.size) return true
    for (i in previous.indices) {
        if (measureDistanceMeters(previous[i], current[i]) > epsilonMeters) return true
    }
    return false
}

package org.prauga.measure.components

import android.view.Surface
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.roundToInt
import kotlin.math.sqrt

/** Unit gravity vector in screen coordinates: x right, y up, z out of the screen. */
data class Gravity(val x: Float, val y: Float, val z: Float) {

    /** Degrees the screen is tipped away from lying flat: 0 flat, 90 upright. */
    val tiltFromFlat: Float
        get() = Math.toDegrees(acos(abs(z).coerceIn(0f, 1f).toDouble())).toFloat()

    /** Flat or upright, with a 10° dead band so the mode doesn't flicker near 45°. */
    fun isFlat(wasFlat: Boolean): Boolean =
        if (wasFlat) tiltFromFlat < 50f else tiltFromFlat < 40f

    /**
     * Level once within 0.5°, and stays level until past 1°, so the screen doesn't
     * strobe green while the reading hovers around the rounding edge.
     */
    fun isLevel(isFlat: Boolean, wasLevel: Boolean): Boolean {
        val offLevel = if (isFlat) flatTiltDegrees(x, y) else uprightTiltDegrees(x, y)
        return if (wasLevel) offLevel < 1f else offLevel < 0.5f
    }

    companion object {
        val Flat = Gravity(0f, 0f, 1f)

        /**
         * Builds a unit vector from raw sensor axes, turning the device's x/y axes into the
         * screen's for the given display [rotation].
         */
        fun fromSensor(x: Float, y: Float, z: Float, rotation: Int): Gravity {
            val (sx, sy) = when (rotation) {
                Surface.ROTATION_90 -> -y to x
                Surface.ROTATION_180 -> -x to -y
                Surface.ROTATION_270 -> y to -x
                else -> x to y
            }
            val norm = sqrt(sx * sx + sy * sy + z * z)
            if (norm == 0f) return Flat
            return Gravity(sx / norm, sy / norm, z / norm)
        }
    }
}

/** Total tilt away from lying flat, from the in-screen gravity components. */
fun flatTiltDegrees(x: Float, y: Float): Float =
    Math.toDegrees(asin(sqrt(x * x + y * y).coerceAtMost(1f).toDouble())).toFloat()

/** Screen rotation that keeps the horizon level; 0 when held upright in portrait. */
fun rollDegrees(x: Float, y: Float): Float =
    Math.toDegrees(atan2(x, y).toDouble()).toFloat()

/** Quarter turns to the nearest upright or sideways hold. */
fun quarterTurns(x: Float, y: Float): Int = (rollDegrees(x, y) / 90f).roundToInt()

/**
 * Degrees the held edge leans away from straight up, counting both sideways roll and
 * leaning forward or back, so 0 means the device stands perfectly vertical.
 */
fun uprightTiltDegrees(x: Float, y: Float): Float {
    // Gravity along the edge pointing up in the nearest upright or sideways hold
    val alongEdge = when (Math.floorMod(quarterTurns(x, y), 4)) {
        0 -> y
        1 -> x
        2 -> -y
        else -> -x
    }
    return Math.toDegrees(acos(alongEdge.coerceIn(-1f, 1f).toDouble())).toFloat()
}

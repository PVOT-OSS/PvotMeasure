package org.prauga.measure.components

import android.view.Surface
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.cos
import kotlin.math.sin

class GravityTest {

    private fun rad(degrees: Double) = Math.toRadians(degrees)

    /** Device standing upright in portrait, rolled and leaning by the given degrees. */
    private fun upright(rollDeg: Double = 0.0, leanBackDeg: Double = 0.0): Gravity {
        val roll = rad(rollDeg)
        val lean = rad(leanBackDeg)
        return Gravity.fromSensor(
            x = (-sin(roll) * cos(lean)).toFloat(),
            y = (cos(roll) * cos(lean)).toFloat(),
            z = sin(lean).toFloat(),
            rotation = Surface.ROTATION_0
        )
    }

    @Test
    fun uprightAndVertical_readsZero() {
        val g = upright()
        assertEquals(0f, uprightTiltDegrees(g.x, g.y), 0.01f)
        assertTrue(g.isLevel(isFlat = false, wasLevel = false))
    }

    @Test
    fun leaningBackOnAStand_isNotLevel() {
        // Sensor reading captured from the phone on its stand: (-0.01, 8.41, 5.04)
        val g = Gravity.fromSensor(-0.01f, 8.41f, 5.04f, Surface.ROTATION_0)
        assertEquals(31f, uprightTiltDegrees(g.x, g.y), 0.5f)
        assertFalse(g.isFlat(wasFlat = false))
        assertFalse(g.isLevel(isFlat = false, wasLevel = false))
    }

    @Test
    fun rolledSideways_readsTheRoll() {
        val g = upright(rollDeg = 10.0)
        assertEquals(10f, uprightTiltDegrees(g.x, g.y), 0.01f)
    }

    @Test
    fun rollAndLean_combine() {
        val g = upright(rollDeg = 3.0, leanBackDeg = 4.0)
        assertEquals(5f, uprightTiltDegrees(g.x, g.y), 0.05f)
    }

    @Test
    fun heldSidewaysAndVertical_readsZero() {
        // Landscape with the device's left edge down
        val g = Gravity.fromSensor(x = 9.81f, y = 0f, z = 0f, rotation = Surface.ROTATION_0)
        assertEquals(1, quarterTurns(g.x, g.y))
        assertEquals(0f, uprightTiltDegrees(g.x, g.y), 0.01f)
    }

    @Test
    fun lyingFlat_readsTheSurfaceTilt() {
        // Sensor reading captured from the phone on a table: (0.52, 0.27, 9.79)
        val g = Gravity.fromSensor(0.52f, 0.27f, 9.79f, Surface.ROTATION_0)
        assertTrue(g.isFlat(wasFlat = true))
        assertEquals(3.4f, flatTiltDegrees(g.x, g.y), 0.1f)
    }

    @Test
    fun level_holdsUntilPastOneDegree() {
        val nearlyLevel = upright(rollDeg = 0.8)
        assertFalse(nearlyLevel.isLevel(isFlat = false, wasLevel = false))
        assertTrue(nearlyLevel.isLevel(isFlat = false, wasLevel = true))
        assertFalse(upright(rollDeg = 1.2).isLevel(isFlat = false, wasLevel = true))
    }
}

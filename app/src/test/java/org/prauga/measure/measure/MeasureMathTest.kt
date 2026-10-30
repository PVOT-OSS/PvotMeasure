package org.prauga.measure.measure

import io.github.sceneview.math.Position
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Locale

class MeasureMathTest {
    @Test
    fun distanceIsEuclidean() {
        assertEquals(5f, measureDistanceMeters(Position(0f, 0f, 0f), Position(3f, 4f, 0f)), 1e-6f)
        assertEquals(0f, measureDistanceMeters(Position(1f, 2f, 3f), Position(1f, 2f, 3f)), 0f)
    }

    @Test
    fun midpointIsHalfway() {
        val mid = measureMidpoint(Position(0f, 2f, 4f), Position(2f, 4f, 8f))
        assertEquals(Position(1f, 3f, 6f), mid)
    }

    @Test
    fun chainSumsSegments() {
        val points = listOf(Position(0f, 0f, 0f), Position(3f, 4f, 0f), Position(3f, 4f, 2f))
        assertEquals(7f, measureChainMeters(points), 1e-6f)
        assertEquals(0f, measureChainMeters(points.take(1)), 0f)
        assertEquals(0f, measureChainMeters(emptyList()), 0f)
    }

    @Test
    fun pointsMovedIgnoresJitter() {
        val a = listOf(Position(0f, 0f, 0f))
        assertFalse(measurePointsMoved(a, listOf(Position(0.0005f, 0f, 0f))))
        assertTrue(measurePointsMoved(a, listOf(Position(0.01f, 0f, 0f))))
        assertTrue(measurePointsMoved(a, a + a))
    }

    @Test
    fun metricLengths() {
        val us = Locale.US
        assertEquals("0 cm", formatMeasureLength(0f, MeasureUnitSystem.Metric, us))
        assertEquals("42 cm", formatMeasureLength(0.42f, MeasureUnitSystem.Metric, us))
        assertEquals("1.42 m", formatMeasureLength(1.42f, MeasureUnitSystem.Metric, us))
        assertEquals("1,42 m", formatMeasureLength(1.42f, MeasureUnitSystem.Metric, Locale.GERMANY))
        assertEquals("0 cm", formatMeasureLength(-1f, MeasureUnitSystem.Metric, us))
    }

    @Test
    fun imperialLengths() {
        val us = Locale.US
        assertEquals("11 in", formatMeasureLength(0.28f, MeasureUnitSystem.Imperial, us))
        assertEquals("1 ft", formatMeasureLength(0.3048f, MeasureUnitSystem.Imperial, us))
        assertEquals("3 ft 4 in", formatMeasureLength(1.016f, MeasureUnitSystem.Imperial, us))
    }

    @Test
    fun unitsFollowTheLocale() {
        assertEquals(MeasureUnitSystem.Imperial, MeasureUnitSystem.forLocale(Locale.US))
        assertEquals(MeasureUnitSystem.Metric, MeasureUnitSystem.forLocale(Locale.UK))
        assertEquals(MeasureUnitSystem.Metric, MeasureUnitSystem.forLocale(Locale.GERMANY))
    }
}

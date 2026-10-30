package org.prauga.measure.measure

import io.github.sceneview.math.Position
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class ProjectionTest {
    // x and y pass through, w = z: a point at (1, 0, 2) is at ndc x = 0.5.
    private val matrix = floatArrayOf(
        1f, 0f, 0f, 0f,
        0f, 1f, 0f, 0f,
        0f, 0f, 1f, 1f,
        0f, 0f, 0f, 0f,
    )

    @Test
    fun centreMapsToTheMiddleOfTheView() {
        val p = projectToScreen(matrix, Position(0f, 0f, 2f), 200f, 100f)!!
        assertEquals(100f, p.x, 1e-3f)
        assertEquals(50f, p.y, 1e-3f)
    }

    @Test
    fun rightAndUpMoveRightAndUp() {
        val p = projectToScreen(matrix, Position(1f, 1f, 2f), 200f, 100f)!!
        assertEquals(150f, p.x, 1e-3f)
        assertEquals(25f, p.y, 1e-3f)
    }

    @Test
    fun pointsBehindTheCameraAreHidden() {
        assertNull(projectToScreen(matrix, Position(0f, 0f, -1f), 200f, 100f))
        assertNull(projectToScreen(matrix, Position(0f, 0f, 0f), 200f, 100f))
    }

    @Test
    fun segmentBehindIsHidden() {
        assertNull(projectSegment(matrix, Position(0f, 0f, -1f), Position(1f, 0f, -2f), 200f, 100f))
    }

    @Test
    fun segmentCrossingTheCameraPlaneIsCut() {
        val (a, b) = projectSegment(matrix, Position(0f, 0f, 2f), Position(4f, 0f, -2f), 200f, 100f)!!
        assertEquals(100f, a.x, 1e-3f)
        assertNotNull(b)
        // The far end is cut at w = 0.05, where x/w is huge, so it ends beyond the right edge.
        assert(b.x > 200f)
    }

    @Test
    fun multiplyByIdentityKeepsTheMatrix() {
        val identity = floatArrayOf(1f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 1f)
        val product = multiplyMatrices(identity, matrix)
        for (i in 0 until 16) assertEquals(matrix[i], product[i], 0f)
        val other = multiplyMatrices(matrix, identity)
        for (i in 0 until 16) assertEquals(matrix[i], other[i], 0f)
    }
}

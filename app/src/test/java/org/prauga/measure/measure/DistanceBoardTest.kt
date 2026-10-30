package org.prauga.measure.measure

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DistanceBoardTest {
    private val empty = DistanceBoard<Int>()

    @Test
    fun pointsExtendTheActiveChain() {
        val board = empty.add(1).add(2).add(3)
        assertEquals(1, board.chains.size)
        assertEquals(listOf(1, 2, 3), board.active?.points)
        assertEquals(listOf(1, 2, 3), board.allPoints)
    }

    @Test
    fun finishNeedsASegmentAndStartsANewChain() {
        assertFalse(empty.add(1).canFinish)
        assertEquals(empty.add(1), empty.add(1).finish())

        val ended = empty.add(1).add(2).finish()
        assertNull(ended.active)
        val next = ended.add(3)
        assertEquals(listOf(listOf(1, 2), listOf(3)), next.chains.map { it.points })
        assertEquals(listOf(3), next.active?.points)
    }

    @Test
    fun undoStepsBackThroughEverything() {
        val board = empty.add(1).add(2).finish().add(3)
        val one = board.undo()
        assertEquals(listOf(listOf(1, 2)), one.chains.map { it.points })
        // The earlier chain was ended; undo reopens it and drops its last point.
        val two = one.undo()
        assertEquals(listOf(listOf(1)), two.chains.map { it.points })
        assertTrue(two.active != null)
        assertTrue(two.undo().isEmpty)
        assertTrue(empty.undo().isEmpty)
    }

    @Test
    fun clearEmptiesTheBoard() {
        assertTrue(empty.add(1).add(2).clear().isEmpty)
    }
}

// Modelled on the Distance mode of the SceneView demo app's MeasureBoard
// (https://github.com/sceneview/sceneview, Apache-2.0). See THIRD_PARTY_NOTICES.md.

package org.prauga.measure.measure

/** A run of points joined segment by segment. [ended] chains take no more points. */
data class DistanceChain<P>(val points: List<P>, val ended: Boolean = false)

/**
 * Every chain measured so far, the last one possibly in progress. Immutable: each action returns a
 * new board, so undo is a pure function and the flow is testable without a camera. [P] is whatever
 * anchors a point (an ARCore anchor on device, a plain position in tests).
 */
data class DistanceBoard<P>(val chains: List<DistanceChain<P>> = emptyList()) {

    /** The chain the next point extends, or null when the next point starts a new one. */
    val active: DistanceChain<P>?
        get() = chains.lastOrNull()?.takeIf { !it.ended }

    val isEmpty: Boolean get() = chains.isEmpty()

    /** Every anchored point on the board, for cleanup. */
    val allPoints: List<P> get() = chains.flatMap { it.points }

    /** A chain with at least one segment can be ended. */
    val canFinish: Boolean get() = (active?.points?.size ?: 0) >= 2

    /** Adds [point] to the active chain, or starts a new one. */
    fun add(point: P): DistanceBoard<P> {
        val current = active ?: return copy(chains = chains + DistanceChain(listOf(point)))
        return copy(chains = chains.dropLast(1) + current.copy(points = current.points + point))
    }

    /** Ends the active chain where it is; the next point starts a new chain. */
    fun finish(): DistanceBoard<P> {
        val current = active?.takeIf { canFinish } ?: return this
        return copy(chains = chains.dropLast(1) + current.copy(ended = true))
    }

    /**
     * Removes the last point placed. An ended chain is reopened first, so undo steps back through
     * the whole history; a chain left with no points disappears.
     */
    fun undo(): DistanceBoard<P> {
        val last = chains.lastOrNull() ?: return this
        val shorter = last.copy(points = last.points.dropLast(1), ended = false)
        return copy(chains = chains.dropLast(1) + listOfNotNull(shorter.takeIf { it.points.isNotEmpty() }))
    }

    fun clear(): DistanceBoard<P> = DistanceBoard()
}

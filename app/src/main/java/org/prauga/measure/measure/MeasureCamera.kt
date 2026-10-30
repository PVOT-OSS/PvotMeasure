// The hit test follows the SceneView demo app's Measure (https://github.com/sceneview/sceneview,
// Apache-2.0). See THIRD_PARTY_NOTICES.md.

package org.prauga.measure.measure

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.google.ar.core.Anchor
import com.google.ar.core.Config
import com.google.ar.core.DepthPoint
import com.google.ar.core.Frame
import com.google.ar.core.HitResult
import com.google.ar.core.Plane
import com.google.ar.core.Pose
import com.google.ar.core.Session
import com.google.ar.core.TrackingState
import com.prauga.pvot.coreui.PvotLoadingContent
import com.prauga.pvot.designsystem.components.PvotCard
import com.prauga.pvot.designsystem.components.PvotGlassIconButton
import com.prauga.pvot.designsystem.motion.PvotDurations
import com.prauga.pvot.designsystem.motion.PvotMotion
import com.prauga.pvot.designsystem.navigation.LocalPvotNavBarSizes
import com.prauga.pvot.designsystem.theme.PvotSpacing
import io.github.sceneview.ar.ARSceneScope
import io.github.sceneview.ar.ARSceneView
import io.github.sceneview.ar.arcore.hitTestDepth
import io.github.sceneview.math.Position
import org.prauga.measure.R
import java.util.Locale
import kotlin.math.roundToInt

/** Where the reticle points: a world position and how to pin an anchor there. */
private class AimHit(val position: Position, val createAnchor: (Session) -> Anchor?)

private const val NEAR_METERS = 0.05f
private const val FAR_METERS = 100f

/** A new aim closer than this to the shown one is jitter and does not redraw. */
private const val AIM_EPSILON_METERS = 0.002f

/**
 * The AR view with the Distance tool around it: aim the reticle, add points, read each segment and
 * the total. Points are ARCore anchors; lines, labels and the reticle are drawn by Compose from
 * the camera's matrices, so they take the Pvot theme.
 */
@Composable
internal fun MeasureCamera(
    unitSystem: MeasureUnitSystem,
    onSessionFailure: () -> Unit,
    modifier: Modifier = Modifier
) {
    val locale = LocalConfiguration.current.locales[0]
    var board by remember { mutableStateOf(DistanceBoard<Anchor>()) }
    var positions by remember { mutableStateOf<Map<Anchor, Position>>(emptyMap()) }
    val viewProjection = remember { mutableStateOf<FloatArray?>(null) }
    var aimPosition by remember { mutableStateOf<Position?>(null) }
    var tracking by remember { mutableStateOf(true) }
    var cameraReady by remember { mutableStateOf(false) }
    var viewport by remember { mutableStateOf(IntSize.Zero) }

    // Plain holders the frame callback writes: the latest hit, session and matrices.
    val latestAim = remember { arrayOfNulls<AimHit>(1) }
    val sessionHolder = remember { arrayOfNulls<Session>(1) }
    val view = remember { FloatArray(16) }
    val projection = remember { FloatArray(16) }
    val currentBoard by rememberUpdatedState(board)
    val currentViewport by rememberUpdatedState(viewport)

    DisposableEffect(Unit) {
        onDispose { currentBoard.allPoints.forEach { it.detach() } }
    }

    val haptics = LocalHapticFeedback.current

    fun addPoint() {
        val session = sessionHolder[0] ?: return
        val aimed = latestAim[0]
        val anchor = aimed?.createAnchor?.invoke(session) ?: return
        board = board.add(anchor)
        positions = positions + (anchor to anchor.pose.toPosition())
        haptics.performHapticFeedback(HapticFeedbackType.Confirm)
    }

    fun replaceBoard(next: DistanceBoard<Anchor>) {
        val kept = next.allPoints.toSet()
        board.allPoints.filterNot { it in kept }.forEach { it.detach() }
        board = next
        positions = positions.filterKeys { it in kept }
    }

    val chains = board.chains.map { chain -> chain.points.mapNotNull { positions[it] } }
    val activePoints = board.active?.points?.mapNotNull { positions[it] }.orEmpty()
    val liveFrom = activePoints.lastOrNull()
    val live = if (liveFrom != null) aimPosition else null
    val liveSegment = if (live != null && liveFrom != null) liveFrom to live else null
    val nodeColor = MaterialTheme.colorScheme.primary

    Box(modifier = modifier.onSizeChanged { viewport = it }) {
        ARSceneView(
            modifier = Modifier.fillMaxSize(),
            planeRenderer = board.isEmpty,
            sessionConfiguration = { session: Session, config: Config ->
                config.planeFindingMode = Config.PlaneFindingMode.HORIZONTAL_AND_VERTICAL
                config.depthMode = if (session.isDepthModeSupported(Config.DepthMode.AUTOMATIC)) {
                    Config.DepthMode.AUTOMATIC
                } else {
                    Config.DepthMode.DISABLED
                }
            },
            onSessionCreated = { sessionHolder[0] = it },
            onSessionFailure = { onSessionFailure() },
            onSessionUpdated = { _, frame ->
                cameraReady = true
                val camera = frame.camera
                val isTracking = camera.trackingState == TrackingState.TRACKING
                if (tracking != isTracking) tracking = isTracking
                if (isTracking) {
                    camera.getViewMatrix(view, 0)
                    camera.getProjectionMatrix(projection, 0, NEAR_METERS, FAR_METERS)
                    viewProjection.value = multiplyMatrices(projection, view)

                    // Anchors drift as tracking refines: follow them, but only on real moves.
                    val anchors = currentBoard.allPoints
                    val read = anchors.associateWith { it.pose.toPosition() }
                    val shown = positions
                    if (anchors.any { shown[it] == null } ||
                        measurePointsMoved(anchors.map { shown.getValue(it) }, anchors.map { read.getValue(it) })
                    ) {
                        positions = read
                    }

                    val size = currentViewport
                    val hit = if (size.width > 0) {
                        hitTestCenter(frame, size.width / 2f, size.height / 2f)
                    } else {
                        null
                    }
                    latestAim[0] = hit
                    val before = aimPosition
                    val after = hit?.position
                    if ((before == null) != (after == null) ||
                        (before != null && after != null &&
                            measureDistanceMeters(before, after) > AIM_EPSILON_METERS)
                    ) {
                        aimPosition = after
                    }
                } else {
                    latestAim[0] = null
                    if (aimPosition != null) aimPosition = null
                }
            }
        ) {
            // Points and lines are nodes of the AR scene, so they render in the same frame as the
            // camera image and cannot trail it.
            MeasureNodes(chains = chains, live = liveSegment, color = nodeColor)
        }

        SegmentLabels(
            chains = chains,
            live = liveSegment,
            unitSystem = unitSystem,
            locale = locale,
            viewProjection = viewProjection,
            viewport = viewport
        )
        Reticle(
            locked = aimPosition != null,
            modifier = Modifier.align(Alignment.Center)
        )

        val lastChain = chains.lastOrNull().orEmpty()
        val total = measureChainMeters(if (live != null && liveFrom != null) activePoints + live else lastChain)
        val liveMeters = if (live != null && liveFrom != null) measureDistanceMeters(liveFrom, live) else null
        ReadoutCard(
            title = when {
                !tracking -> stringResource(R.string.measure_hint_tracking)
                liveMeters != null -> formatMeasureLength(liveMeters, unitSystem, locale)
                lastChain.size >= 2 -> formatMeasureLength(total, unitSystem, locale)
                aimPosition == null -> stringResource(R.string.measure_hint_surface)
                board.isEmpty -> stringResource(R.string.measure_hint_aim)
                else -> stringResource(R.string.measure_hint_next)
            },
            subtitle = if (tracking && total > 0f && (liveMeters != null || lastChain.size >= 2) &&
                (activePoints.size >= 2 || liveMeters != null && activePoints.size >= 2)
            ) {
                stringResource(R.string.measure_total, formatMeasureLength(total, unitSystem, locale))
            } else {
                null
            },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + PvotSpacing.L)
                .padding(horizontal = PvotSpacing.L)
        )

        val barClearance = LocalPvotNavBarSizes.current.compactBarHeight +
            WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = barClearance + PvotSpacing.L + PvotSpacing.M)
                .padding(horizontal = PvotSpacing.L),
            horizontalArrangement = Arrangement.spacedBy(PvotSpacing.M, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PvotGlassIconButton(
                onClick = { replaceBoard(board.undo()) },
                backdrop = null,
                enabled = !board.isEmpty
            ) {
                Icon(painterResource(R.drawable.ic_arrow_back_up), stringResource(R.string.cd_measure_undo))
            }
            Button(onClick = ::addPoint, enabled = aimPosition != null) {
                Icon(painterResource(R.drawable.ic_plus), contentDescription = null)
                Text(stringResource(R.string.measure_add_point), modifier = Modifier.padding(start = PvotSpacing.S))
            }
            PvotGlassIconButton(
                onClick = { replaceBoard(board.finish()) },
                backdrop = null,
                enabled = board.canFinish
            ) {
                Icon(painterResource(R.drawable.ic_check), stringResource(R.string.cd_measure_done))
            }
            PvotGlassIconButton(
                onClick = { replaceBoard(board.clear()) },
                backdrop = null,
                enabled = !board.isEmpty
            ) {
                Icon(painterResource(R.drawable.ic_trash), stringResource(R.string.cd_measure_clear))
            }
        }

        if (!cameraReady) PvotLoadingContent(modifier = Modifier.align(Alignment.Center))
    }
}

@Composable
private fun ReadoutCard(title: String, subtitle: String?, modifier: Modifier = Modifier) {
    PvotCard(modifier = modifier) {
        Column(
            modifier = Modifier.padding(horizontal = PvotSpacing.L, vertical = PvotSpacing.M),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/** The ring in the middle of the view: the primary color once it has a surface to measure. */
@Composable
private fun Reticle(locked: Boolean, modifier: Modifier = Modifier) {
    val color by animateColorAsState(
        targetValue = if (locked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
        animationSpec = tween(PvotDurations.Short, easing = PvotMotion.Settle),
        label = "reticle"
    )
    Canvas(modifier = modifier.size(RETICLE_SIZE)) {
        drawCircle(color = color, radius = size.minDimension / 2f - 1.dp.toPx(), style = androidx.compose.ui.graphics.drawscope.Stroke(2.dp.toPx()))
        drawCircle(color = color, radius = 2.dp.toPx())
    }
}

private val RETICLE_SIZE = 40.dp

private const val POINT_RADIUS_METERS = 0.007f
private const val LINE_RADIUS_METERS = 0.0025f
private const val AIM_RADIUS_METERS = 0.005f

/** The points, the lines between them and the live line to the reticle, as nodes of the AR scene. */
@Composable
private fun ARSceneScope.MeasureNodes(
    chains: List<List<Position>>,
    live: Pair<Position, Position>?,
    color: Color
) {
    val material = remember(materialLoader, color) { materialLoader.createUnlitColorInstance(color) }
    DisposableEffect(material) { onDispose { materialLoader.destroyMaterialInstance(material) } }

    chains.forEachIndexed { c, points ->
        points.forEachIndexed { i, p ->
            key("point-$c-$i") { SphereNode(radius = POINT_RADIUS_METERS, materialInstance = material, position = p) }
        }
        points.zipWithNext().forEachIndexed { i, (a, b) ->
            key("line-$c-$i") { TubeNode(points = listOf(a, b), radius = LINE_RADIUS_METERS, materialInstance = material) }
        }
    }
    live?.let { (from, to) ->
        SphereNode(radius = AIM_RADIUS_METERS, materialInstance = material, position = to)
        TubeNode(points = listOf(from, to), radius = LINE_RADIUS_METERS, materialInstance = material)
    }
}

/** A chip with each segment's length at its midpoint, kept on the segment as the phone moves. */
@Composable
private fun SegmentLabels(
    chains: List<List<Position>>,
    live: Pair<Position, Position>?,
    unitSystem: MeasureUnitSystem,
    locale: Locale,
    viewProjection: State<FloatArray?>,
    viewport: IntSize
) {
    val segments = chains.flatMap { points -> points.zipWithNext() } + listOfNotNull(live)
    segments.forEach { (a, b) ->
        PvotCard(
            modifier = Modifier.atScreenOf(measureMidpoint(a, b), viewProjection, viewport)
        ) {
            Text(
                text = formatMeasureLength(measureDistanceMeters(a, b), unitSystem, locale),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = PvotSpacing.M, vertical = PvotSpacing.S)
            )
        }
    }
}

/** Centers the element on where [world] lands on screen, read at layout so frames do not recompose it. */
private fun Modifier.atScreenOf(
    world: Position,
    viewProjection: State<FloatArray?>,
    viewport: IntSize
): Modifier = layout { measurable, constraints ->
    val placeable = measurable.measure(constraints.copy(minWidth = 0, minHeight = 0))
    layout(placeable.width, placeable.height) {
        val screen = viewProjection.value?.let {
            projectToScreen(it, world, viewport.width.toFloat(), viewport.height.toFloat())
        }
        if (screen != null) {
            placeable.place(
                (screen.x - placeable.width / 2f).roundToInt(),
                (screen.y - placeable.height / 2f).roundToInt()
            )
        } else {
            placeable.place(OFF_SCREEN, OFF_SCREEN)
        }
    }
}

private const val OFF_SCREEN = -10_000

private fun Pose.toPosition(): Position = Position(tx(), ty(), tz())

/**
 * What the reticle points at: a detected surface, else depth. A loose feature point is not
 * trusted, since its depth is the least reliable, so the reticle waits for a better read.
 */
private fun hitTestCenter(frame: Frame, x: Float, y: Float): AimHit? {
    val hits = runCatching { frame.hitTest(x, y) }.getOrNull().orEmpty()
        .filter { it.trackable.trackingState == TrackingState.TRACKING }
    hits.firstOrNull { hit ->
        val plane = hit.trackable as? Plane
        plane != null && plane.subsumedBy == null && plane.isPoseInPolygon(hit.hitPose)
    }?.let { return it.toAim() }
    hits.firstOrNull { it.trackable is DepthPoint }?.let { return it.toAim() }
    frame.hitTestDepth(x, y)?.let { depth ->
        val p = depth.position
        return AimHit(p) { session -> session.createAnchor(Pose.makeTranslation(p.x, p.y, p.z)) }
    }
    return null
}

private fun HitResult.toAim(): AimHit = AimHit(hitPose.toPosition()) {
    takeIf { trackable.trackingState == TrackingState.TRACKING }?.createAnchor()
}

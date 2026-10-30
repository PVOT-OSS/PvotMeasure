package org.prauga.measure.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.prauga.measure.R
import kotlin.math.abs
import kotlin.math.asin
import kotlin.math.roundToInt

/**
 * Bubble level: glass spheres while the device lies flat, a horizon while it stands
 * upright, and a screen flooded with the level colour once it reads 0°.
 */
@Composable
fun LevelComponent(
    gravity: Gravity,
    isFlat: Boolean,
    isLevel: Boolean,
    modifier: Modifier = Modifier
) {
    val colors = levelColors()
    val motion = spring<Float>(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness = Spring.StiffnessMediumLow
    )
    val x by animateFloatAsState(gravity.x, motion, label = "gravityX")
    val y by animateFloatAsState(gravity.y, motion, label = "gravityY")
    val z by animateFloatAsState(gravity.z, motion, label = "gravityZ")

    val levelProgress by animateFloatAsState(
        targetValue = if (isLevel) 1f else 0f,
        // Short, so the half-blended colours are barely on screen
        animationSpec = tween(durationMillis = 120),
        label = "levelProgress"
    )
    // Flip the readout halfway through the fade so every frame agrees with the
    // background: mostly level colour reads 0°, mostly background reads at least 1°
    val showsLevel = levelProgress >= 0.5f

    val reading =
        if (isFlat) flatReading(x, y, showsLevel) else horizonReading(x, y, showsLevel)
    val readoutRotation by animateFloatAsState(
        targetValue = reading.readoutRotation,
        label = "readoutRotation"
    )

    // Leaning back lifts the horizon off the marks and leaning forward drops it
    val lean = Math.toDegrees(asin(z.coerceIn(-1f, 1f).toDouble())).toFloat()
    // The number sits below the horizon, and moves above once a forward lean would
    // drop the line onto it
    val readoutOffset by animateDpAsState(
        targetValue = when {
            isFlat -> 0.dp
            lean < -3f -> (-96).dp
            else -> 96.dp
        },
        label = "readoutOffset"
    )

    // Tick once each time the device settles into level, but not on first open
    val haptic = LocalHapticFeedback.current
    var wasLevel by remember { mutableStateOf(isLevel) }
    LaunchedEffect(isLevel) {
        if (isLevel && !wasLevel) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        wasLevel = isLevel
    }

    val content = lerp(
        colors.content,
        if (isFlat) colors.onLevelSurface else colors.onLevelBackground,
        levelProgress
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(lerp(colors.background, colors.levelBackground, levelProgress))
    ) {
        // Gone by the halfway point so the grid never shows over the level colour
        LevelGrid(
            color = colors.grid.copy(alpha = (1f - levelProgress * 2f).coerceAtLeast(0f)),
            modifier = Modifier.fillMaxSize()
        )

        if (isFlat) {
            LevelSpheres(
                x = x,
                y = y,
                levelProgress = levelProgress,
                colors = colors,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Ease the surface onto the marks as the level turns over, so 0° never
            // shows a line slightly slanted or off the marks
            val roll = rollDegrees(x, y)
            val hold = quarterTurns(x, y) * 90f
            // 20° of lean moves the surface the full quarter screen, so small leans near
            // level are still easy to see
            LevelHorizon(
                rollDegrees = hold + (roll - hold) * (1f - levelProgress),
                leanFraction = (lean / 20f).coerceIn(-1f, 1f) * (1f - levelProgress),
                levelProgress = levelProgress,
                colors = colors,
                modifier = Modifier.fillMaxSize()
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .rotate(readoutRotation),
            contentAlignment = Alignment.Center
        ) {
            if (!isFlat) HorizonMarks(color = content, modifier = Modifier.fillMaxSize())
            Text(
                text = stringResource(R.string.level_degrees, reading.degrees),
                color = content,
                // Tabular figures keep the readout from shifting as digits change
                style = MaterialTheme.typography.displayLarge.copy(fontFeatureSettings = "tnum"),
                modifier = Modifier.offset(y = readoutOffset)
            )
        }
    }
}

/** Signed degrees to show, and how far to turn the readout to keep it upright. */
private data class LevelReading(val degrees: Int, val readoutRotation: Float)

/** Level always reads 0°, and anything else at least 1°, so colour and number agree. */
private fun shownDegrees(angle: Float, isLevel: Boolean): Int =
    if (isLevel) 0 else maxOf(1, angle.roundToInt())

/** Total tilt from flat, signed by the axis it leans along most. */
private fun flatReading(x: Float, y: Float, isLevel: Boolean): LevelReading {
    val degrees = shownDegrees(flatTiltDegrees(x, y), isLevel)
    val negative = if (abs(y) >= abs(x)) y < 0f else x < 0f
    return LevelReading(if (negative) -degrees else degrees, readoutRotation = 0f)
}

/** Lean away from standing perfectly vertical, with the readout kept upright. */
private fun horizonReading(x: Float, y: Float, isLevel: Boolean): LevelReading =
    LevelReading(
        degrees = shownDegrees(uprightTiltDegrees(x, y), isLevel),
        readoutRotation = quarterTurns(x, y) * 90f
    )

/** Fixed marks at each edge that the horizon lines up with when level. */
@Composable
private fun HorizonMarks(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val markLength = size.width * 0.1f
        val inset = size.width * 0.05f
        val stroke = 3.dp.toPx()
        drawLine(
            color = color,
            start = Offset(inset, center.y),
            end = Offset(inset + markLength, center.y),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
        drawLine(
            color = color,
            start = Offset(size.width - inset - markLength, center.y),
            end = Offset(size.width - inset, center.y),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
    }
}

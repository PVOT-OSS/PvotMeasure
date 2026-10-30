package org.prauga.measure.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/** Theme colours the level draws with, so it follows light and dark mode. */
@Immutable
data class LevelColors(
    /** Screen behind everything while tilted. */
    val background: Color,
    /** Grid lines. */
    val grid: Color,
    /** Readout, marks, horizon edge, sphere rims and refracted lines. */
    val content: Color,
    /** Glass spheres and the liquid below the horizon. */
    val surface: Color,
    /** Screen behind everything once level. */
    val levelBackground: Color,
    /** Merged bubble and liquid once level. */
    val levelSurface: Color,
    /** Readout over [levelBackground]. */
    val onLevelBackground: Color,
    /** Readout over [levelSurface]. */
    val onLevelSurface: Color
)

@Composable
fun levelColors(): LevelColors = with(MaterialTheme.colorScheme) {
    LevelColors(
        background = background,
        grid = surfaceVariant,
        content = onBackground,
        surface = surfaceVariant,
        levelBackground = primaryContainer,
        levelSurface = primary,
        onLevelBackground = onPrimaryContainer,
        onLevelSurface = onPrimary
    )
}

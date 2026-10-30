package org.prauga.measure

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import com.prauga.pvot.designsystem.effects.pvotGlassBackdrop
import com.prauga.pvot.designsystem.effects.rememberPvotGlassBackdrop
import com.prauga.pvot.designsystem.navigation.PvotNavBar
import com.prauga.pvot.designsystem.navigation.PvotTabItem
import org.prauga.measure.screens.LevelScreen
import org.prauga.measure.screens.MeasureScreen
import org.prauga.measure.screens.SettingsScreen

/** Pixels of scroll in one event before the nav bar reacts, so a jitter doesn't flip it. */
private const val ScrollThreshold = 4f


@Composable
fun MeasureAppNavBar(preferences: AppPreferences) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var navCompact by rememberSaveable { mutableStateOf(false) }
    // Swiping up through a list shrinks the nav bar out of the way; swiping back down restores it.
    val scrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (available.y < -ScrollThreshold) navCompact = true
                else if (available.y > ScrollThreshold) navCompact = false
                return Offset.Zero
            }
        }
    }
    val backdrop = rememberPvotGlassBackdrop()

    val tabs = listOf(
        PvotTabItem(
            iconRes = R.drawable.ic_measure,
            labelRes = R.string.tab_measure,
            contentDescriptionRes = R.string.cd_measure
        ),
        PvotTabItem(
            iconRes = R.drawable.ic_level,
            labelRes = R.string.tab_level,
            contentDescriptionRes = R.string.cd_level
        ),
        PvotTabItem(
            iconRes = R.drawable.ic_settings,
            labelRes = R.string.tab_settings,
            contentDescriptionRes = R.string.cd_settings
        )
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        // The level draws under the status bar as before; screens handle their own insets.
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            PvotNavBar(
                selectedTab = selectedTab,
                onTabClick = {
                    selectedTab = it
                    navCompact = false
                },
                tabs = tabs,
                compact = navCompact,
                backdrop = backdrop
            )
        }
    ) { innerPadding ->
        // Screens draw edge to edge under the bar; the backdrop lets the glass bar frost them.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pvotGlassBackdrop(backdrop)
                .background(MaterialTheme.colorScheme.background)
        ) {
            val screenModifier = Modifier
                .fillMaxSize()
                .nestedScroll(scrollConnection)
                .padding(top = innerPadding.calculateTopPadding())

            when (selectedTab) {
                0 -> MeasureScreen(modifier = screenModifier)
                1 -> LevelScreen(modifier = screenModifier)
                2 -> SettingsScreen(preferences = preferences, modifier = screenModifier)
            }
        }
    }
}

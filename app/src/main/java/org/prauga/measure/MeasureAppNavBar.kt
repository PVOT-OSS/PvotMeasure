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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.prauga.pvot.designsystem.effects.pvotGlassBackdrop
import com.prauga.pvot.designsystem.effects.rememberPvotGlassBackdrop
import com.prauga.pvot.designsystem.navigation.PvotNavBar
import com.prauga.pvot.designsystem.navigation.PvotTabItem
import org.prauga.measure.screens.LevelScreen
import org.prauga.measure.screens.MeasureScreen


@Composable
fun MeasureAppNavBar() {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
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
                onTabClick = { selectedTab = it },
                tabs = tabs,
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
                .padding(top = innerPadding.calculateTopPadding())

            when (selectedTab) {
                0 -> MeasureScreen(modifier = screenModifier)
                1 -> LevelScreen(modifier = screenModifier)
            }
        }
    }
}

package org.prauga.measure

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.prauga.pvot.designsystem.navigation.PvotNavBar
import com.prauga.pvot.designsystem.navigation.PvotTabItem
import org.prauga.measure.screens.LevelScreen
import org.prauga.measure.screens.MeasureScreen


@Composable
fun MeasureAppNavBar() {
    var selectedTab by remember { mutableIntStateOf(0) }

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

    Box(modifier = Modifier.fillMaxSize()) {
        when (selectedTab) {
            0 -> MeasureScreen(modifier = Modifier.fillMaxSize())
            1 -> LevelScreen(modifier = Modifier.fillMaxSize())
        }

        PvotNavBar(
            selectedTab = selectedTab,
            onTabClick = { selectedTab = it },
            tabs = tabs,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

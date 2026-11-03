// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-FileCopyrightText: 2026 debz-g <77199373+debz-g@users.noreply.github.com>
// SPDX-License-Identifier: GPL-3.0-or-later

package org.prauga.measure

import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.prauga.pvot.designsystem.effects.LocalPvotGlassIntensity
import com.prauga.pvot.designsystem.platform.PvotBaseActivity
import com.prauga.pvot.designsystem.theme.PvotAppTheme

class MainActivity : PvotBaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val preferences = AppPreferences(applicationContext)
        setPvotContent {
            CompositionLocalProvider(LocalPvotGlassIntensity provides preferences.glassIntensity) {
                PvotAppTheme { MeasureAppNavBar(preferences = preferences) }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MeasureAppPreview() {
    PvotAppTheme { MeasureAppNavBar(preferences = AppPreferences(LocalContext.current)) }
}

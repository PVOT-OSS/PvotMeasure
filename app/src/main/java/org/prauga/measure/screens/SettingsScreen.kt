// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-FileCopyrightText: 2026 debz-g <77199373+debz-g@users.noreply.github.com>
// SPDX-License-Identifier: GPL-3.0-or-later

package org.prauga.measure.screens

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBars
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.prauga.pvot.designsystem.components.PvotScreen
import com.prauga.pvot.designsystem.components.PvotScreenDefaults
import com.prauga.pvot.designsystem.components.list.PvotListGroup
import com.prauga.pvot.designsystem.components.list.PvotListRow
import com.prauga.pvot.designsystem.components.list.PvotSwitchRow
import com.prauga.pvot.designsystem.motion.pvotReveal
import org.prauga.measure.AppPreferences
import org.prauga.measure.BuildConfig
import org.prauga.measure.R

@Composable
fun SettingsScreen(preferences: AppPreferences, modifier: Modifier = Modifier) {
    PvotScreen(
        modifier = modifier,
        contentPadding =
            PvotScreenDefaults.contentPadding(
                top =
                    PvotScreenDefaults.TopPadding +
                        WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
            ),
    ) {
        item {
            PvotListGroup(
                modifier = Modifier.fillMaxWidth().pvotReveal(0),
                title = stringResource(R.string.settings_section_appearance),
            ) {
                item {
                    PvotSwitchRow(
                        title = stringResource(R.string.settings_glass_title),
                        checked = preferences.liquidGlass,
                        onCheckedChange = preferences::updateLiquidGlass,
                        summary = stringResource(R.string.settings_glass_subtitle),
                    )
                }
            }
        }
        item {
            PvotListGroup(
                modifier = Modifier.fillMaxWidth().pvotReveal(1),
                title = stringResource(R.string.settings_section_units),
            ) {
                item {
                    PvotSwitchRow(
                        title = stringResource(R.string.settings_units_title),
                        checked = preferences.imperialUnits,
                        onCheckedChange = preferences::updateImperialUnits,
                        summary = stringResource(R.string.settings_units_subtitle),
                    )
                }
                // Only means something with imperial units, so it shows only then.
                if (preferences.imperialUnits) {
                    item {
                        PvotSwitchRow(
                            title = stringResource(R.string.settings_inches_title),
                            checked = preferences.inchesOnly,
                            onCheckedChange = preferences::updateInchesOnly,
                            summary = stringResource(R.string.settings_inches_subtitle),
                        )
                    }
                }
            }
        }
        item {
            PvotListGroup(
                modifier = Modifier.fillMaxWidth().pvotReveal(2),
                title = stringResource(R.string.settings_section_about),
            ) {
                item {
                    PvotListRow(
                        title = stringResource(R.string.settings_version_title),
                        supportingText = BuildConfig.VERSION_NAME,
                    )
                }
            }
        }
    }
}

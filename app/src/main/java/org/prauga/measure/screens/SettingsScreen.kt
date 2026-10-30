package org.prauga.measure.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.prauga.pvot.coreui.PvotSectionHeader
import com.prauga.pvot.designsystem.components.PvotCard
import com.prauga.pvot.designsystem.components.PvotScreen
import com.prauga.pvot.designsystem.components.PvotScreenDefaults
import com.prauga.pvot.designsystem.motion.pvotReveal
import com.prauga.pvot.designsystem.theme.PvotSpacing
import org.prauga.measure.AppPreferences
import org.prauga.measure.BuildConfig
import org.prauga.measure.R

@Composable
fun SettingsScreen(
    preferences: AppPreferences,
    modifier: Modifier = Modifier
) {
    PvotScreen(
        modifier = modifier,
        contentPadding = PvotScreenDefaults.contentPadding(
            top = PvotScreenDefaults.TopPadding +
                WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
        )
    ) {
        item {
            PvotSectionHeader(
                title = stringResource(R.string.settings_section_appearance),
                modifier = Modifier
                    .fillMaxWidth()
                    .pvotReveal(0)
            )
        }
        item {
            PvotCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .pvotReveal(1),
                onClick = { preferences.updateLiquidGlass(!preferences.liquidGlass) }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(PvotSpacing.L),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(PvotSpacing.L)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.settings_glass_title),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = stringResource(R.string.settings_glass_subtitle),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = PvotSpacing.XS)
                        )
                    }
                    Switch(
                        checked = preferences.liquidGlass,
                        onCheckedChange = preferences::updateLiquidGlass
                    )
                }
            }
        }
        item {
            PvotCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .pvotReveal(2),
                onClick = { preferences.updateImperialUnits(!preferences.imperialUnits) }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(PvotSpacing.L),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(PvotSpacing.L)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.settings_units_title),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = stringResource(R.string.settings_units_subtitle),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = PvotSpacing.XS)
                        )
                    }
                    Switch(
                        checked = preferences.imperialUnits,
                        onCheckedChange = preferences::updateImperialUnits
                    )
                }
            }
        }
        item {
            PvotSectionHeader(
                title = stringResource(R.string.settings_section_about),
                modifier = Modifier
                    .fillMaxWidth()
                    .pvotReveal(3)
            )
        }
        item {
            PvotCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .pvotReveal(4)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(PvotSpacing.L),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.settings_version_title),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = BuildConfig.VERSION_NAME,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

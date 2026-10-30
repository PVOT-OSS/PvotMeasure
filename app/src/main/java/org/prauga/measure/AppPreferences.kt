package org.prauga.measure

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.prauga.pvot.designsystem.effects.PvotGlassDefaults
import org.prauga.measure.measure.MeasureUnitSystem
import java.util.Locale

/** The app's few settings, kept in SharedPreferences and observable from Compose. */
class AppPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /** Liquid glass at full strength when on, the frosted look when off. */
    var liquidGlass by mutableStateOf(
        prefs.getBoolean(KEY_LIQUID_GLASS, PvotGlassDefaults.defaultIntensity() > 0f)
    )
        private set

    fun updateLiquidGlass(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_LIQUID_GLASS, enabled).apply()
        liquidGlass = enabled
    }

    /** Feet and inches when on, metres when off; starts as the locale's habit. */
    var imperialUnits by mutableStateOf(
        prefs.getBoolean(
            KEY_IMPERIAL,
            MeasureUnitSystem.forLocale(Locale.getDefault()) == MeasureUnitSystem.Imperial
        )
    )
        private set

    fun updateImperialUnits(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_IMPERIAL, enabled).apply()
        imperialUnits = enabled
    }

    val unitSystem: MeasureUnitSystem
        get() = if (imperialUnits) MeasureUnitSystem.Imperial else MeasureUnitSystem.Metric

    /** The value for [com.prauga.pvot.designsystem.effects.LocalPvotGlassIntensity]. */
    val glassIntensity: Float
        get() = if (liquidGlass) 1f else 0f

    private companion object {
        const val PREFS_NAME = "measure_preferences"
        const val KEY_LIQUID_GLASS = "liquid_glass"
        const val KEY_IMPERIAL = "imperial_units"
    }
}

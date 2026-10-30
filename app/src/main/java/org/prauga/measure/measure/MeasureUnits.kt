// Adapted from the SceneView demo app (https://github.com/sceneview/sceneview,
// samples/android-demo, Apache-2.0). See THIRD_PARTY_NOTICES.md.

package org.prauga.measure.measure

import java.util.Locale
import kotlin.math.roundToInt

/** Length units: feet and inches in the United States, metres everywhere else, until the user picks. */
enum class MeasureUnitSystem {
    Metric,
    Imperial,
    ;

    companion object {
        /** The units Measure starts in under [locale]. */
        fun forLocale(locale: Locale): MeasureUnitSystem =
            if (locale.country == "US") Imperial else Metric
    }
}

private const val METERS_PER_INCH = 0.0254f
private const val INCHES_PER_FOOT = 12
private const val CENTIMETERS_PER_METER = 100f

/**
 * A length as a person reads it off a tape: "42 cm" under a metre, "1.42 m" above (with the
 * locale's decimal separator), "11 in" under a foot, "3 ft 4 in" above. Whole centimetres and
 * inches only: a millimetre digit would promise precision the pose does not have.
 */
fun formatMeasureLength(meters: Float, system: MeasureUnitSystem, locale: Locale): String {
    val safe = meters.coerceAtLeast(0f)
    return when (system) {
        MeasureUnitSystem.Metric -> {
            val centimeters = (safe * CENTIMETERS_PER_METER).roundToInt()
            if (centimeters < CENTIMETERS_PER_METER) {
                "$centimeters cm"
            } else {
                String.format(locale, "%.2f m", centimeters / CENTIMETERS_PER_METER)
            }
        }
        MeasureUnitSystem.Imperial -> {
            val inches = (safe / METERS_PER_INCH).roundToInt()
            val feet = inches / INCHES_PER_FOOT
            val rest = inches % INCHES_PER_FOOT
            when {
                feet == 0 -> "$rest in"
                rest == 0 -> "$feet ft"
                else -> "$feet ft $rest in"
            }
        }
    }
}

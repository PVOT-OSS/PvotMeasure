package org.prauga.measure.screens

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.view.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import org.prauga.measure.components.Gravity
import org.prauga.measure.components.LevelComponent

@Composable
fun LevelScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val view = LocalView.current

    var gravity by remember { mutableStateOf(Gravity.Flat) }
    var isFlat by remember { mutableStateOf(true) }
    var isLevel by remember { mutableStateOf(false) }

    DisposableEffect(context) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        // The fused gravity sensor is steadier; fall back to the raw accelerometer
        val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY)
            ?: sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val smoothed = floatArrayOf(0f, 0f, SensorManager.GRAVITY_EARTH)

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                for (i in 0..2) smoothed[i] += (event.values[i] - smoothed[i]) * 0.2f
                val rotation = view.display?.rotation ?: Surface.ROTATION_0
                val next = Gravity.fromSensor(smoothed[0], smoothed[1], smoothed[2], rotation)
                isFlat = next.isFlat(wasFlat = isFlat)
                isLevel = next.isLevel(isFlat = isFlat, wasLevel = isLevel)
                gravity = next
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_GAME)

        onDispose {
            sensorManager.unregisterListener(listener)
        }
    }

    LevelComponent(
        gravity = gravity,
        isFlat = isFlat,
        isLevel = isLevel,
        modifier = modifier
    )
}

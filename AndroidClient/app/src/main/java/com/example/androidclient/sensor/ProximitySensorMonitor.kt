package com.example.androidclient.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class ProximitySensorMonitor(val context: Context) {
    val isNear: Flow<Boolean> = callbackFlow<Boolean> {
        val sensorManager: SensorManager =
            context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)

        val proximitySensorEventListener = object : SensorEventListener {
            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
            }

            override fun onSensorChanged(event: SensorEvent) {
                Log.d("ProximitySensorMonitor", "onSensorChanged: ${event.values.joinToString()}")
                if (event.sensor.type == Sensor.TYPE_PROXIMITY) {
                    if (event.values[0] == 0f) {
                        Log.d("ProximitySensorMonitor", "Near")
                        channel.trySend(true)
                    } else {
                        Log.d("ProximitySensorMonitor", "Far")
                        channel.trySend(false)
                    }
                }
            }
        }

        sensorManager.registerListener(
            proximitySensorEventListener,
            proximitySensor,
            SensorManager.SENSOR_DELAY_FASTEST
        )
        Log.d("ProximitySensorMonitor", "Sensor listener registered")

        awaitClose {
            Log.d("ProximitySensorMonitor", "Unregistering sensor listener")
            sensorManager.unregisterListener(proximitySensorEventListener)
        }
    }
}
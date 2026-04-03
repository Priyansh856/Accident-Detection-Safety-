package com.example.accidentdetection.service

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.example.accidentdetection.domain.DetectionSample
import kotlin.math.sqrt

class SensorHandler(
    context: Context,
    private val onSampleReady: (DetectionSample) -> Unit,
) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

    private var lastAcceleration = 9.8f
    private var lastRotation = 0f

    fun start() {
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
        gyroscope?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    fun stop() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                lastAcceleration = magnitude(event.values[0], event.values[1], event.values[2])
                onSampleReady(
                    DetectionSample(
                        accelerationMagnitude = lastAcceleration,
                        rotationMagnitude = lastRotation,
                        timestampMillis = System.currentTimeMillis(),
                    ),
                )
            }

            Sensor.TYPE_GYROSCOPE -> {
                lastRotation = magnitude(event.values[0], event.values[1], event.values[2])
                onSampleReady(
                    DetectionSample(
                        accelerationMagnitude = lastAcceleration,
                        rotationMagnitude = lastRotation,
                        timestampMillis = System.currentTimeMillis(),
                    ),
                )
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

    private fun magnitude(x: Float, y: Float, z: Float): Float {
        return sqrt((x * x + y * y + z * z).toDouble()).toFloat()
    }
}

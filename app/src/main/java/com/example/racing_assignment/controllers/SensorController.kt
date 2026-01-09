package com.example.racing_assignment.controllers

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

class SensorController(context: Context) {

    private var sensorManager: SensorManager? = null
    private var accelerometer: Sensor? = null
    private var listener: SensorCallback? = null

    interface SensorCallback {
        fun onTiltChanged(targetLane: Int, gameSpeed: Long)
    }

    private val sensorListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            val x = event.values[0]
            val y = event.values[1]

            val targetLane = when {
                x < -6 -> 4
                x < -3 -> 3
                x > 6 -> 0
                x > 3 -> 1
                else -> 2
            }

            val gameSpeed = when {
                y < -6 -> 1500L
                y < -3 -> 2000L
                y > 6 -> 5000L
                y > 3 -> 4000L
                else -> 3000L
            }

            listener?.onTiltChanged(targetLane, gameSpeed)
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    init {
        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    }

    fun setCallback(callback: SensorCallback) {
        listener = callback
    }

    fun register() {
        accelerometer?.let {
            sensorManager?.registerListener(sensorListener, it, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    fun unregister() {
        sensorManager?.unregisterListener(sensorListener)
    }
}
package com.thesisapp.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.provider.Settings
import android.net.Uri
import android.util.Log
import androidx.core.app.NotificationCompat
import com.thesisapp.R
import com.thesisapp.data.AppDatabase
import com.thesisapp.data.SensorData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

private const val PPG_SENSOR_NAME = "PPG Sensor"
private const val ECG_SENSOR_NAME = "ECG Sensor"

class SensorService : Service(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var gyroscope: Sensor? = null
    private var heartrate: Sensor? = null
    private var ppg: Sensor? = null
    private var ecg: Sensor? = null
    private lateinit var database: AppDatabase
    private val serviceScope = CoroutineScope(Dispatchers.IO + Job())

    // Temp holders for sensor values
    private var accelValues: FloatArray? = null
    private var gyroValues: FloatArray? = null
    private var heartRateValue: Float? = null
    private var ppgValue: Float? = null
    private var ecgValue: Float? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(1, createNotification())

        database = AppDatabase.getInstance(this)

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        heartrate = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE)
        ppg = sensorManager.getDefaultSensor(Sensor.TYPE_DEVICE_PRIVATE_BASE + 1)
        ecg = sensorManager.getDefaultSensor(Sensor.TYPE_DEVICE_PRIVATE_BASE + 2)

        for (sensor in sensorManager.getSensorList(Sensor.TYPE_ALL)) {
            when {
                sensor.name.equals("PPG Sensor", ignoreCase = true) -> {
                    ppg = sensor
                    Log.i("SensorService", "PPG sensor found: ${sensor.name}")
                }

                sensor.name.equals("ECG Sensor", ignoreCase = true) -> {
                    ecg = sensor
                    Log.i("SensorService", "ECG sensor found: ${sensor.name}")
                }
            }
        }
        requestBatteryOptimizationExemption()
        registerSensors()
    }

    private fun registerSensors() {
        accelerometer?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_FASTEST, 100000) }
        gyroscope?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_FASTEST, 100000) }
        heartrate?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_FASTEST, 100000) }
        ppg?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_FASTEST, 100000) }
        ecg?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_FASTEST, 100000) }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        when (event.sensor.type) {
            Sensor.TYPE_LINEAR_ACCELERATION -> accelValues = event.values.clone()
            Sensor.TYPE_GYROSCOPE -> gyroValues = event.values.clone()
            Sensor.TYPE_HEART_RATE -> heartRateValue = event.values[0]
        }

        when (event.sensor.name) {

            PPG_SENSOR_NAME -> ppgValue = event.values[0]
            ECG_SENSOR_NAME -> ecgValue = event.values[0]

        }

        // Save only when all three types are available
        if (accelValues != null && gyroValues != null && ppgValue != null && ecgValue!= null) {
            val currentData = SensorData(
                accel_x = accelValues!![0],
                accel_y = accelValues!![1],
                accel_z = accelValues!![2],
                gyro_x = gyroValues!![0],
                gyro_y = gyroValues!![1],
                gyro_z = gyroValues!![2],
                heartrate = heartRateValue,
                ppg = ppgValue,
                ecg = ecgValue
            )

            serviceScope.launch {
                database.sensorDataDao().insertSensorData(currentData)
                Log.d("SensorService", "Saved combined data: $currentData")
            }

            // Reset to wait for new synced values
            accelValues = null
            gyroValues = null
            heartRateValue = null
            ppgValue = null
            ecgValue = null
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY // Ensures service restarts if killed
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "sensor_service",
                "Sensor Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, "sensor_service")
            .setContentTitle("Swimming Data Collection")
            .setContentText("Sensors are running in the background")
            .setSmallIcon(R.drawable.ic_notification)
            .build()
    }

    private fun requestBatteryOptimizationExemption() {
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        if (!powerManager.isIgnoringBatteryOptimizations(packageName)) {
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                .setData(Uri.parse("package:$packageName"))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }
}
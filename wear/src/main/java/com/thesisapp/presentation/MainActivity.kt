/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter to find the
 * most up to date changes to the libraries and their usages.
 */

package com.thesisapp.presentation

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import androidx.wear.tooling.preview.devices.WearDevices
import com.thesisapp.R
import com.thesisapp.data.AppDatabase
import com.thesisapp.data.SensorData
import com.thesisapp.presentation.theme.ThesisAppTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.thesisapp.service.SensorService

class MainActivity : ComponentActivity(), SensorEventListener {
    // initialize sensors
    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var gyroscope: Sensor? = null
    private var heartRate: Sensor? = null

    // initialize database
    private lateinit var database: AppDatabase

    // by mutableStateOf requires runtime imports; allows live changes
    private var isRecording by mutableStateOf(false)

    // permissions needed to run app
    private val permissions = arrayOf(
        Manifest.permission.BODY_SENSORS,
        Manifest.permission.HIGH_SAMPLING_RATE_SENSORS,
        Manifest.permission.WAKE_LOCK
    )

    // main function
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)

        setTheme(android.R.style.Theme_DeviceDefault)

        // keeps screen from idling while app is open
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // check permissions
        checkAndRequestPermissions()

        // instantiate db
        database = AppDatabase.getInstance(this)

        // instantiate sensors
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        heartRate = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE)

        setContent {
            WearApp(::startRecording, ::stopRecording, isRecording)
        }
    }

    // get permission from user to run app
    private fun checkAndRequestPermissions() {
        if (permissions.any { ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED }) {
            ActivityCompat.requestPermissions(this, permissions, 1)
        }
    }

    // register sensors
    private fun registerSensors() {
        accelerometer?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_FASTEST) }
        gyroscope?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_FASTEST) }
        heartRate?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_FASTEST) }
    }

    private fun handleAcceleration(values: FloatArray) {
        val (ax, ay, az) = values

        lifecycleScope.launch(Dispatchers.IO) {
            database.sensorDataDao().insertSensorData(SensorData(type = "Accelerometer", x = ax, y = ay, z = az))

            Log.d("SensorData","Fetched data: ACC - $ax, $ay, $az")
        }
    }

    private fun handleGyroscope(values: FloatArray) {
        val (gx, gy, gz) = values

        lifecycleScope.launch(Dispatchers.IO) {
            database.sensorDataDao().insertSensorData(SensorData(type = "Gyroscope", x = gx, y = gy, z = gz))
            Log.d("SensorData","Fetched data: GYR - $gx, $gy, $gz")
        }
    }

    private fun handleHeartRate(hr: Float) {
        lifecycleScope.launch(Dispatchers.IO) {
            database.sensorDataDao().insertSensorData(SensorData(type = "HeartRate", x = hr, y = null, z = null))
            Log.d("SensorData","Fetched data: HR - $hr")
        }
    }

    // when sensors detect changes in values
    override fun onSensorChanged(event: SensorEvent?) {
        if (!isRecording)
            return
        else {
            event?.let {
                when (it.sensor.type) {
                    Sensor.TYPE_LINEAR_ACCELERATION -> handleAcceleration(it.values)
                    Sensor.TYPE_GYROSCOPE -> handleGyroscope(it.values)
                    Sensor.TYPE_HEART_RATE -> handleHeartRate(it.values[0])
                }
            }
        }
    }

    // do nothing
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun startRecording() {
        isRecording = true
        val serviceIntent = Intent(this, SensorService::class.java)
        startForegroundService(serviceIntent)
    }

    private fun stopRecording() {
        isRecording = false
        val serviceIntent = Intent(this, SensorService::class.java)
        stopService(serviceIntent)
        Log.d("SensorData", "Recording stopped.")
    }

    // when app is closed
    override fun onDestroy() {
        super.onDestroy()
        isRecording = false
        sensorManager.unregisterListener(this)
    }
}

@Composable
fun WearApp(startRecording: () -> Unit, stopRecording: () -> Unit, isRecording: Boolean) {
    ThesisAppTheme {
        Column(
            modifier = Modifier.fillMaxSize().background(MaterialTheme.colors.background),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (!isRecording) {
                Button(onClick = startRecording) { Text("START") }
            } else {
                Button(onClick = stopRecording) { Text("STOP") }
            }
        }
    }
}
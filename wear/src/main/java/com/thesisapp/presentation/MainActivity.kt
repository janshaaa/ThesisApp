package com.thesisapp.presentation

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import com.thesisapp.service.SensorService

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Keeps the screen from sleeping while the app is open
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // Automatically start the sensor service when app launches
        val serviceIntent = Intent(this, SensorService::class.java)
        startForegroundService(serviceIntent)

        // Finish the activity so only the background service runs
        finish()
    }
}
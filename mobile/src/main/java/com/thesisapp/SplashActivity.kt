package com.thesisapp

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Delay and go to MainDashboardActivity
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, MainDashboardActivity::class.java)
            startActivity(intent)
            finish() // Close splash so back button doesn't return here
        }, 2000) // 2000ms = 2 seconds
    }
}

package com.thesisapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainDashboardActivity : AppCompatActivity() {

    private lateinit var connectBtn: Button
    private lateinit var startTrackingBtn: Button
    private lateinit var viewHistoryBtn: Button
    private lateinit var manageSwimmersBtn: Button
    private lateinit var settingsBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_dashboard)

        connectBtn = findViewById(R.id.btnConnect)
        startTrackingBtn = findViewById(R.id.btnStartTracking)
        viewHistoryBtn = findViewById(R.id.btnViewHistory)
        manageSwimmersBtn = findViewById(R.id.btnManageSwimmers)
        settingsBtn = findViewById(R.id.btnSettings)

        connectBtn.setOnClickListener {
            // Simulate smartwatch discovery logic
            simulateSmartwatchConnection()
        }

        startTrackingBtn.setOnClickListener {
            // Start stroke tracking activity
            startActivity(Intent(this, TrackingActivity::class.java))
        }

        viewHistoryBtn.setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }

        manageSwimmersBtn.setOnClickListener {
            startActivity(Intent(this, SwimmerManagerActivity::class.java))
        }

        settingsBtn.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }

    private fun simulateSmartwatchConnection() {
        // Enable tracking button after "connecting"
        startTrackingBtn.isEnabled = true
        startTrackingBtn.setBackgroundTintList(getColorStateList(R.color.lightBlue))
    }
}

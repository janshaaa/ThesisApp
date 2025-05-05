package com.thesisapp

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ViewFlipper
import androidx.appcompat.app.AppCompatActivity

class ConnectWatchActivity : AppCompatActivity() {

    private lateinit var viewFlipper: ViewFlipper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_connect_watch)

        viewFlipper = findViewById(R.id.viewFlipper)

        startSequence()
    }

    private fun startSequence() {
        Handler(Looper.getMainLooper()).postDelayed({
            viewFlipper.showNext()  // to Searching screen

            Handler(Looper.getMainLooper()).postDelayed({
                val deviceFound = checkBluetoothForDevices()
                if (deviceFound) {
                    viewFlipper.showNext() // to Radar with Results

                    Handler(Looper.getMainLooper()).postDelayed({
                        viewFlipper.showNext() // to Device List
                    }, 3000)
                } else {
                    viewFlipper.showNext() // to Error screen
                }
            }, 3000)
        }, 3000)
    }

    private fun checkBluetoothForDevices(): Boolean {
        // Mocked logic – replace with actual BT scan logic
        return true // or false if no device found
    }
}
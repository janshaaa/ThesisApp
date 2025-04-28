package com.thesisapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Entity(tableName = "sensor_data")
data class SensorData(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),

    // Accelerometer
    val accel_x: Float? = null,
    val accel_y: Float? = null,
    val accel_z: Float? = null,

    // Gyroscope
    val gyro_x: Float? = null,
    val gyro_y: Float? = null,
    val gyro_z: Float? = null,

    // Heart Rate
    val heart_rate: Float? = null
)
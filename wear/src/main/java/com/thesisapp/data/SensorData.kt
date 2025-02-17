package com.thesisapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Entity(tableName = "sensor_data")
data class SensorData(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: String = getCurrentTime(), // Change to formatted time
    val type: String?,
    val x: Float?,
    val y: Float?,
    val z: Float?
) {
    companion object {
        fun getCurrentTime(): String {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault()) // Added milliseconds
            return dateFormat.format(Date())
        }
    }
}
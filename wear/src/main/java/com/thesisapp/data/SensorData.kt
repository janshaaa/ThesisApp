package com.thesisapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sensor_data")
data class SensorData(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val type: String?,
    val x: Float?,
    val y: Float?,
    val z: Float?
)
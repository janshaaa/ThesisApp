package com.thesisapp.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SensorDataDao {
    @Insert
    suspend fun insertSensorData(sensorData: SensorData): Long // returns entry ID

    @Query("SELECT * FROM sensor_data WHERE id = :id")
    suspend fun getSensorDataById(id: Long): SensorData? // get specific entry

    @Query("SELECT * FROM sensor_data ORDER BY timestamp DESC")
    suspend fun getAllSensorData(): List<SensorData> // get all entries
}
package com.thesisapp.data

import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

object SensorDataManager {
    private val _sensorDataFlow = MutableStateFlow<SensorData?>(null)
    val sensorDataFlow: StateFlow<SensorData?> = _sensorDataFlow

    // Direct states for UI access
    val accelerometerState = mutableStateOf<SensorData?>(null)
    val gyroscopeState = mutableStateOf<SensorData?>(null)
    val heartRateState = mutableStateOf<SensorData?>(null)

    fun updateData(sensorData: SensorData) {
        _sensorDataFlow.value = sensorData

        // Use a coroutine dispatcher if not already on main thread
        MainScope().launch(Dispatchers.Main) {
            when (sensorData.type) {
                "Accelerometer" -> accelerometerState.value = sensorData
                "Gyroscope" -> gyroscopeState.value = sensorData
                "HeartRate" -> heartRateState.value = sensorData
            }
        }
    }
}
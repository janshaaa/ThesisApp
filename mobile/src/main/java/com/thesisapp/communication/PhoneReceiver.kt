package com.thesisapp.communication

import android.content.Context
import android.util.Log
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMap
import com.google.gson.Gson
import com.thesisapp.data.AppDatabase
import com.thesisapp.data.SensorData
import com.thesisapp.data.SensorDataManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch


class PhoneReceiver(context: Context) : DataClient.OnDataChangedListener {
    private val tag = "PhoneReceiver"
    private val database = AppDatabase.getInstance(context)
    private val dao = database.sensorDataDao()
    private val receiverScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        dataEvents.forEach { event ->
            if (event.type == DataEvent.TYPE_CHANGED) {
                val dataItem = event.dataItem
                if (dataItem.uri.path == "/sentSensorData") {
                    dataItem.data?.let { data ->
                        try {
                            val dataMap = DataMap.fromByteArray(data)
                            when (dataMap.getString("type", "")) {
                                "SENSOR_DATA" -> {
                                    val sensorDataJson = dataMap.getString("sensorDataJson")
                                    sensorDataJson?.let { jsonString ->
                                        val sensorData = Gson().fromJson(jsonString, SensorData::class.java)
                                        Log.d(tag, "Received sensor data: $sensorData")
                                        processSensorData(sensorData)
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("WearableDataListener", "Error processing data: ${e.message}")
                        }
                    }
                }
            }
        }
    }

    private fun processSensorData(sensorData: SensorData) {
        // Insert into phone DB
        receiverScope.launch {
            dao.insertSensorData(sensorData)
            Log.d(tag, "Inserted into phone database: $sensorData")
        }

        SensorDataManager.updateData(sensorData)
        Log.d(tag, "Updated data: $sensorData")
    }
}
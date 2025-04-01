package com.thesisapp.communication

import android.content.Context
import android.util.Log
import com.google.android.gms.wearable.DataMap
import com.google.android.gms.wearable.PutDataRequest
import com.google.android.gms.wearable.Wearable
import com.google.gson.Gson
import com.thesisapp.data.SensorData
import com.google.android.gms.wearable.Node

class WearSender(private val context: Context) {
    private val tag = "WearSender"

    fun sendSensorDataToPhone(sensorData: SensorData?) {
        Wearable.getNodeClient(context).connectedNodes
            .addOnSuccessListener { connectedNodes: List<Node> ->
                if (connectedNodes.isNotEmpty()) {
                    val dataMap = DataMap().apply {
                        putString("type", "SENSOR_DATA")
                        putString("sensorDataJson", Gson().toJson(sensorData))
                    }

                    Wearable.getDataClient(context).putDataItem(
                        PutDataRequest.create("/sentSensorData").apply {
                            data = dataMap.toByteArray()
                        }
                    )
                        .addOnSuccessListener {
                            Log.d(tag, "Sensor data sent successfully: $sensorData")
                        }
                        .addOnFailureListener { e ->
                            Log.e(tag, "Failed to send sensor data", e)
                        }
                } else {
                    Log.w(tag, "No connected nodes. Cannot send sensor data.")
                }
            }
            .addOnFailureListener { e ->
                Log.e(tag, "Failed to retrieve connected nodes", e)
            }
    }
}
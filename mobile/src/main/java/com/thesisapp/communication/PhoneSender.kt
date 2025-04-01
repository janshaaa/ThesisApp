package com.thesisapp.communication

import android.content.Context
import android.util.Log
import com.google.android.gms.wearable.Wearable

class PhoneSender(private val context: Context) {
    private val tag = "PhoneSender"

    fun sendCommandToWear(command: String) {
        // Find nodes with the specified capability
        Wearable.getNodeClient(context)
            .connectedNodes
            .addOnSuccessListener { nodes ->
                nodes.forEach { node ->
                    Wearable.getMessageClient(context)
                        .sendMessage(
                            node.id,
                            "/sentCommands",
                            command.toByteArray()
                        )
                        .addOnSuccessListener {
                            Log.d(tag, "Message sent successfully: $command")
                        }
                        .addOnFailureListener {
                            Log.e(tag, "Message send failed", it)
                        }
                }
            }
    }
}
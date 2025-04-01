package com.thesisapp.communication

import android.content.Intent
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService

class WearReceiver : WearableListenerService() {
    private val tag = "WearReceiver"

    companion object {
        const val messagePath = "/sentCommands"
        const val receivedCommand = "com.thesisapp.receivedCommand"
        const val key = "command"
    }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        if (messageEvent.path == messagePath) {
            // Convert byte array to string
            val command = String(messageEvent.data)
            Log.d(tag, "Received command: $command")

            // Broadcast the command to MainActivity
            val intent = Intent(receivedCommand).apply {
                putExtra(key, command)
            }
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
        }
    }
}

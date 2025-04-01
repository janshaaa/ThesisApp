package com.thesisapp

import android.os.Bundle
import android.util.Log
//import android.text.Layout.Alignment
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.Text
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.Wearable
import com.thesisapp.communication.PhoneReceiver
//import java.lang.reflect.Modifier
import androidx.activity.compose.setContent
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.wear.compose.material.ButtonDefaults
import com.thesisapp.communication.PhoneSender
import com.thesisapp.data.AppDatabase
import com.thesisapp.data.SensorData
import com.thesisapp.data.SensorDataDao
import com.thesisapp.data.SensorDataManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var dataClient: DataClient
    private lateinit var receiver: PhoneReceiver
    private lateinit var sender: PhoneSender

    private lateinit var database: AppDatabase
    private lateinit var dao: SensorDataDao

    private var isRecording by mutableStateOf(false)

    //private var sensorScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContentView(R.layout.activity_main)

        sender = PhoneSender(this)
        receiver = PhoneReceiver(this)
        database = AppDatabase.getInstance(this)
        dao = database.sensorDataDao()

        setContent {
            RecordingPage(::startRecording, ::stopRecording, ::sendCommand, isRecording)
        }
    }

    private fun sendCommand(command: String) {
        sender.sendCommandToWear(command)
        if (command == "start" || command == "stop")
            isRecording = !isRecording
    }

    override fun onResume() {
        super.onResume()
        dataClient = Wearable.getDataClient(this)
        dataClient.addListener(receiver)
    }

    override fun onPause() {
        super.onPause()
        dataClient.removeListener(receiver)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopRecording()
    }

    fun startRecording() {
        //if (!sensorScope.isActive) { sensorScope = CoroutineScope(Dispatchers.Main + SupervisorJob()) }

    }

    fun stopRecording() {
        //sensorScope.cancel()
    }
}

@Composable
fun RecordingPage(startRecording: () -> Unit, stopRecording: () -> Unit, sendCommand: (String) -> Unit,isRecording: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .windowInsetsPadding(WindowInsets.systemBars)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(24.dp)  // Space between items
        ) {
            SensorDataDisplay()
            RecordButton(startRecording, stopRecording, sendCommand, isRecording)
        }
    }
}

@Composable
fun RecordButton(startRecording: () -> Unit, stopRecording: () -> Unit, sendCommand: (String) -> Unit,isRecording: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .windowInsetsPadding(WindowInsets.systemBars),
        contentAlignment = Alignment.BottomCenter
    ) {
        Button(
            onClick = { if (isRecording) {
                sendCommand("stop")
                stopRecording()
            } else {
                sendCommand("start")
                startRecording()
            }
                      },
            modifier = Modifier
                .size(100.dp),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
                backgroundColor = if (isRecording) Color.Red else Color.Green,
                contentColor = Color.White
            )
        ) {
            Text(
                text = if (isRecording) "STOP" else "START",
                fontSize = 24.sp
            )
        }
    }
}

@Composable
fun SensorDataDisplay() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Each sensor gets its own composable function for isolated recomposition
        AccelerometerDisplay()
        GyroscopeDisplay()
        HeartRateDisplay()
    }
}

@Composable
private fun AccelerometerDisplay() {
    val acc = remember { SensorDataManager.accelerometerState }

    val text = "Accelerometer: %.2f, %.2f, %.2f".format(
        acc.value?.x ?: 0.0,
        acc.value?.y ?: 0.0,
        acc.value?.z ?: 0.0
    )

    Text(
        text = text,
        color = Color.Black,
        fontSize = 20.sp
    )
}

@Composable
private fun GyroscopeDisplay() {
    val gyr = remember { SensorDataManager.gyroscopeState }

    val text = "Gyroscope: %.2f, %.2f, %.2f".format(
        gyr.value?.x ?: 0.0,
        gyr.value?.y ?: 0.0,
        gyr.value?.z ?: 0.0
    )

    Text(
        text = text,
        color = Color.Black,
        fontSize = 20.sp
    )
}

@Composable
private fun HeartRateDisplay() {
    val hr = remember { SensorDataManager.heartRateState }

    Text(
        text = "HeartRate: ${hr.value?.x ?: "0"}",
        color = Color.Black,
        fontSize = 20.sp
    )
}
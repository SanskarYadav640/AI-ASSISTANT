package com.example.service

import android.app.UiModeManager
import android.bluetooth.BluetoothClass
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.os.Build
import android.util.Log
import androidx.car.app.connection.CarConnection
import androidx.lifecycle.Observer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

class CarConnectionDetector(
    private val context: Context,
    private val voiceSpeaker: PrivaVoiceSpeaker,
    private val youtubeMusicController: YouTubeMusicController,
    private val scope: CoroutineScope,
    private val onLogCarSession: (carName: String, connectionType: String, greeting: String, playlist: String?) -> Unit
) {

    private val _isCarConnected = MutableStateFlow(false)
    val isCarConnected: StateFlow<Boolean> = _isCarConnected.asStateFlow()

    private val _connectedCarName = MutableStateFlow("Disconnected")
    val connectedCarName: StateFlow<String> = _connectedCarName.asStateFlow()

    private val _connectionType = MutableStateFlow("None")
    val connectionType: StateFlow<String> = _connectionType.asStateFlow()

    private val _lastConnectedTimestamp = MutableStateFlow(0L)
    val lastConnectedTimestamp: StateFlow<Long> = _lastConnectedTimestamp.asStateFlow()

    private val _lastSpokenGreeting = MutableStateFlow("")
    val lastSpokenGreeting: StateFlow<String> = _lastSpokenGreeting.asStateFlow()

    private var carConnectionObserver: Observer<Int>? = null
    private var isReceiverRegistered = false

    private val carBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                UiModeManager.ACTION_ENTER_CAR_MODE -> {
                    Log.i("CarConnectionDetector", "Detected ENTER_CAR_MODE broadcast")
                    handleCarConnected(
                        carName = "Vehicle Head Unit (Car Mode Dock)",
                        type = "Car Mode (Dock / Cradle)"
                    )
                }
                UiModeManager.ACTION_EXIT_CAR_MODE -> {
                    Log.i("CarConnectionDetector", "Detected EXIT_CAR_MODE broadcast")
                    handleCarDisconnected()
                }
                BluetoothDevice.ACTION_ACL_CONNECTED -> {
                    val device: BluetoothDevice? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
                    } else {
                        @Suppress("DEPRECATION")
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    }

                    if (device != null) {
                        val deviceClass = try { device.bluetoothClass?.majorDeviceClass } catch (e: SecurityException) { null }
                        val deviceSpecificClass = try { device.bluetoothClass?.deviceClass } catch (e: SecurityException) { null }
                        val isVehicle = deviceSpecificClass == BluetoothClass.Device.AUDIO_VIDEO_CAR_AUDIO ||
                                deviceClass == BluetoothClass.Device.Major.AUDIO_VIDEO
                        val deviceName = try { device.name } catch (e: SecurityException) { "Vehicle Audio" } ?: "Vehicle Audio"
                        val isCarName = isCarBluetoothName(deviceName)

                        if (isVehicle || isCarName) {
                            Log.i("CarConnectionDetector", "Detected vehicle Bluetooth device connected: $deviceName")
                            handleCarConnected(
                                carName = deviceName,
                                type = "Vehicle Bluetooth Audio"
                            )
                        }
                    }
                }
                BluetoothDevice.ACTION_ACL_DISCONNECTED -> {
                    if (_isCarConnected.value && _connectionType.value.contains("Bluetooth", ignoreCase = true)) {
                        handleCarDisconnected()
                    }
                }
            }
        }
    }

    init {
        setupCarAppLibraryConnection()
        registerBroadcastReceivers()
        checkInitialCarMode()
    }

    private fun checkInitialCarMode() {
        try {
            val uiModeManager = context.getSystemService(Context.UI_MODE_SERVICE) as? UiModeManager
            if (uiModeManager?.currentModeType == Configuration.UI_MODE_TYPE_CAR) {
                handleCarConnected(
                    carName = "Vehicle Head Unit (In-Car Projection)",
                    type = "Android Auto / Car Mode"
                )
            }
        } catch (e: Exception) {
            Log.w("CarConnectionDetector", "Initial car mode check failed: ${e.message}")
        }
    }

    private fun setupCarAppLibraryConnection() {
        try {
            val carConnection = CarConnection(context)
            val observer = Observer<Int> { connectionState ->
                when (connectionState) {
                    CarConnection.CONNECTION_TYPE_PROJECTION -> {
                        Log.i("CarConnectionDetector", "CarConnection: PROJECTION (Android Auto Active)")
                        handleCarConnected(
                            carName = "Audi MMI Pro (Wireless Android Auto)",
                            type = "Android Auto (Projection)"
                        )
                    }
                    CarConnection.CONNECTION_TYPE_NATIVE -> {
                        Log.i("CarConnectionDetector", "CarConnection: NATIVE (Automotive OS)")
                        handleCarConnected(
                            carName = "Automotive In-Dash Display",
                            type = "Android Automotive Native"
                        )
                    }
                    CarConnection.CONNECTION_TYPE_NOT_CONNECTED -> {
                        if (_connectionType.value.contains("Android Auto", ignoreCase = true)) {
                            handleCarDisconnected()
                        }
                    }
                }
            }
            carConnectionObserver = observer
            // Observe in main looper
            carConnection.type.observeForever(observer)
        } catch (e: Throwable) {
            Log.w("CarConnectionDetector", "Could not attach CarConnection observer: ${e.message}")
        }
    }

    private fun registerBroadcastReceivers() {
        if (isReceiverRegistered) return
        try {
            val filter = IntentFilter().apply {
                addAction(UiModeManager.ACTION_ENTER_CAR_MODE)
                addAction(UiModeManager.ACTION_EXIT_CAR_MODE)
                addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
                addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
            }
            context.registerReceiver(carBroadcastReceiver, filter)
            isReceiverRegistered = true
        } catch (e: Exception) {
            Log.e("CarConnectionDetector", "Failed to register Car broadcast receiver: ${e.message}")
        }
    }

    private fun isCarBluetoothName(name: String): Boolean {
        val lower = name.lowercase()
        val carKeywords = listOf(
            "car", "auto", "audi", "bmw", "mercedes", "honda", "hyundai", "toyota",
            "tata", "mahindra", "maruti", "suzuki", "ford", "volkswagen", "skoda",
            "kia", "tesla", "sync", "uconnect", "carplay", "mmi", "pioneer", "sony car"
        )
        return carKeywords.any { lower.contains(it) }
    }

    /**
     * Exact greeting logic specified by user:
     * "Hello Sir! Say Good evening good afternoon According to time. Please tell me what is your destination?"
     */
    fun getCarGreeting(): String {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val timeSalutation = when (hour) {
            in 5..11 -> "Good morning"
            in 12..16 -> "Good afternoon"
            in 17..20 -> "Good evening"
            else -> "Good night"
        }
        return "Hello Sir! $timeSalutation. Please tell me what is your destination?"
    }

    /**
     * Handles connection event:
     * 1. Updates state
     * 2. Speaks greeting in British Butler voice
     * 3. Plays playlist from YouTube Music if auto-play is enabled
     * 4. Logs to Room Database
     */
    fun handleCarConnected(
        carName: String = "Audi MMI Pro (Wireless Android Auto)",
        type: String = "Android Auto (Projection)"
    ) {
        _isCarConnected.value = true
        _connectedCarName.value = carName
        _connectionType.value = type
        val now = System.currentTimeMillis()
        _lastConnectedTimestamp.value = now

        val greeting = getCarGreeting()
        _lastSpokenGreeting.value = greeting

        val activePlaylist = youtubeMusicController.currentPlaylist.value
        val playlistTitle = if (youtubeMusicController.autoPlayOnCarConnect.value) {
            "YouTube Music: ${activePlaylist.title}"
        } else {
            null
        }

        // 1. Speak greeting through British Butler voice after allowing Bluetooth/car audio link to stabilize
        scope.launch(Dispatchers.Main) {
            kotlinx.coroutines.delay(650L)
            voiceSpeaker.speak(greeting)

            // 2. Play playlist from YouTube Music after greeting finishes
            if (youtubeMusicController.autoPlayOnCarConnect.value) {
                kotlinx.coroutines.delay(5000L)
                if (_isCarConnected.value) {
                    youtubeMusicController.play(context, activePlaylist)
                }
            }
        }

        // Log to database in coroutine
        scope.launch(Dispatchers.IO) {
            try {
                onLogCarSession(carName, type, greeting, playlistTitle)
            } catch (e: Exception) {
                Log.e("CarConnectionDetector", "Failed to log car session: ${e.message}")
            }
        }
    }

    fun handleCarDisconnected() {
        _isCarConnected.value = false
        _connectedCarName.value = "Disconnected"
        _connectionType.value = "None"
    }

    /**
     * Interactive trigger to test or simulate connecting to a car
     * (Ideal for streaming emulator testing and manual activation)
     */
    fun simulateConnectToCar(
        carName: String = "Audi MMI Pro (Wireless Android Auto)",
        type: String = "Android Auto (Projection)"
    ) {
        handleCarConnected(carName, type)
    }

    fun simulateDisconnectFromCar() {
        handleCarDisconnected()
    }

    fun speakGreetingAgain() {
        val greeting = getCarGreeting()
        _lastSpokenGreeting.value = greeting
        voiceSpeaker.speak(greeting)
    }

    fun unregister() {
        if (isReceiverRegistered) {
            try {
                context.unregisterReceiver(carBroadcastReceiver)
                isReceiverRegistered = false
            } catch (ignored: Exception) {}
        }
    }
}

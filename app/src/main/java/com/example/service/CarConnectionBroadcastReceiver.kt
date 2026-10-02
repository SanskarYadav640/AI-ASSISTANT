package com.example.service

import android.app.UiModeManager
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class CarConnectionBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            UiModeManager.ACTION_ENTER_CAR_MODE,
            BluetoothDevice.ACTION_ACL_CONNECTED -> {
                Log.i("CarConnectionReceiver", "Broadcast received: ${intent.action}")
                // Launch or bring forward the Car HUD
                val launchIntent = Intent(context, com.example.MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    putExtra("EXTRA_TARGET_TAB", "car_hud")
                    putExtra("EXTRA_AUTO_CONNECT", true)
                }
                context.startActivity(launchIntent)
            }
        }
    }
}

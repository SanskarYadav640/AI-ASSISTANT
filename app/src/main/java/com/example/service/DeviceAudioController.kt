package com.example.service

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.provider.AlarmClock
import android.util.Log

class DeviceAudioController(private val context: Context) {

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager

    fun getMaxAlarmVolume(): Int {
        return audioManager?.getStreamMaxVolume(AudioManager.STREAM_ALARM) ?: 7
    }

    fun getCurrentAlarmVolume(): Int {
        return audioManager?.getStreamVolume(AudioManager.STREAM_ALARM) ?: 5
    }

    fun getAlarmVolumePercent(): Int {
        val max = getMaxAlarmVolume()
        if (max <= 0) return 0
        val current = getCurrentAlarmVolume()
        return ((current.toFloat() / max.toFloat()) * 100).toInt().coerceIn(0, 100)
    }

    fun setAlarmVolumePercent(percent: Int): Boolean {
        return try {
            val max = getMaxAlarmVolume()
            val targetVolume = ((percent.coerceIn(0, 100).toFloat() / 100f) * max).toInt()
            audioManager?.setStreamVolume(
                AudioManager.STREAM_ALARM,
                targetVolume,
                AudioManager.FLAG_SHOW_UI
            )
            true
        } catch (e: Exception) {
            Log.e("DeviceAudioController", "Failed to set alarm volume: ${e.message}")
            false
        }
    }

    fun openAlarmSettings(context: Context) {
        try {
            val intent = Intent(AlarmClock.ACTION_SHOW_ALARMS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            try {
                val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            } catch (e2: Exception) {
                Log.e("DeviceAudioController", "Cannot launch system clock: ${e2.message}")
            }
        }
    }
}

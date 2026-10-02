package com.example.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.data.local.AppDatabase
import com.example.data.local.entity.PhoneUnlockLogEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class PhoneUnlockReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_USER_PRESENT) {
            Log.d("PhoneUnlockReceiver", "Phone unlock detected: ACTION_USER_PRESENT")
            val pendingResult = goAsync()

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    handleUnlockEvent(context)
                } catch (e: Exception) {
                    Log.e("PhoneUnlockReceiver", "Error processing unlock event: ${e.message}")
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }

    companion object {
        suspend fun handleUnlockEvent(context: Context, isSimulation: Boolean = false): PhoneUnlockLogEntity {
            val db = AppDatabase.getDatabase(context)
            val dao = db.privaDao()
            val notificationMgr = PrivaNotificationManager(context)

            val now = System.currentTimeMillis()
            val cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Kolkata"))
            cal.time = Date(now)

            val hour = cal.get(Calendar.HOUR_OF_DAY)
            val minute = cal.get(Calendar.MINUTE)
            val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(now))
            val timeFmt = SimpleDateFormat("hh:mm a", Locale.ENGLISH).apply {
                timeZone = TimeZone.getTimeZone("Asia/Kolkata")
            }.format(Date(now))

            // User requirement: "Auto set my wake up time with phone unlock logs once I unlock phone after 6 in the morning"
            val isAfterSix = hour >= 6 || isSimulation

            // Check if wake-up log already recorded for today
            val existingWakeUp = dao.getWakeUpLogForDate(dateStr)
            val isNewWakeUp = isAfterSix && existingWakeUp == null

            val note = if (isNewWakeUp) {
                "🌅 Auto-detected wake up at $timeFmt (First phone unlock after 6:00 AM)"
            } else if (isAfterSix) {
                "Phone unlocked at $timeFmt (Post wake-up)"
            } else {
                "Pre-dawn unlock at $timeFmt"
            }

            val entity = PhoneUnlockLogEntity(
                timestamp = now,
                hour = hour,
                minute = minute,
                dateString = dateStr,
                isWakeUpLog = isNewWakeUp,
                wakeUpNote = note,
                unlockMethod = if (isSimulation) "Simulated Unlock (Priva AI Test)" else "Phone Screen Unlock"
            )

            dao.insertUnlockLog(entity)

            if (isNewWakeUp) {
                val traffic = dao.getCommuteTraffic().firstOrNull()
                val commuteMin = traffic?.currentMinutes ?: 32
                val dest = traffic?.officeAddress ?: "Seawoods Grand Central Mall, Navi Mumbai"
                notificationMgr.sendWakeUpAlert(timeFmt, commuteMin, dest)
                dao.insertLog(
                    com.example.data.local.entity.NotificationLogEntity(
                        title = "Wake-Up Time Auto-Set: $timeFmt",
                        message = "Logged via phone unlock after 6:00 AM. Seawoods Grand Central commute: $commuteMin min.",
                        category = "Wake-Up Log",
                        timestamp = now
                    )
                )
            }

            return entity
        }
    }
}

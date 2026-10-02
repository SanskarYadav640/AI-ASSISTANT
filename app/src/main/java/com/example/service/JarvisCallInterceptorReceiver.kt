package com.example.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import android.util.Log
import com.example.data.local.AppDatabase
import com.example.data.local.entity.MissedCallEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * JARVIS Call Interceptor Receiver
 * Intercepts incoming calls and detects missed calls when the phone stops ringing without being answered,
 * immediately saving to local DB and triggering a high-priority heads-up notification alert.
 */
class JarvisCallInterceptorReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == TelephonyManager.ACTION_PHONE_STATE_CHANGED) {
            val stateStr = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
            val incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER) ?: "Unknown Caller"

            Log.d("JarvisCallInterceptor", "Phone state changed: $stateStr for $incomingNumber")

            when (stateStr) {
                TelephonyManager.EXTRA_STATE_RINGING -> {
                    lastRingingNumber = incomingNumber
                    isRinging = true
                }
                TelephonyManager.EXTRA_STATE_OFFHOOK -> {
                    // Call was answered
                    isRinging = false
                }
                TelephonyManager.EXTRA_STATE_IDLE -> {
                    // If it was ringing and went to idle without offhook, it was missed!
                    if (isRinging) {
                        isRinging = false
                        val missedNumber = lastRingingNumber ?: incomingNumber
                        val pendingResult = goAsync()

                        CoroutineScope(Dispatchers.IO).launch {
                            try {
                                handleInterceptedMissedCall(context, missedNumber, "Incoming call disconnected without answer.")
                            } catch (e: Exception) {
                                Log.e("JarvisCallInterceptor", "Error handling missed call: ${e.message}")
                            } finally {
                                pendingResult.finish()
                            }
                        }
                    }
                }
            }
        }
    }

    companion object {
        private var isRinging = false
        private var lastRingingNumber: String? = null

        suspend fun handleInterceptedMissedCall(
            context: Context,
            phoneNumber: String,
            reason: String = "Priority call missed",
            callerName: String? = null,
            urgency: String = "Urgent"
        ): MissedCallEntity {
            val db = AppDatabase.getDatabase(context)
            val dao = db.privaDao()
            val notificationMgr = PrivaNotificationManager(context)

            // Resolve name if available
            val resolvedName = callerName ?: if (phoneNumber.contains("555") || phoneNumber.length < 5) "VIP Contact" else phoneNumber
            val now = System.currentTimeMillis()

            val entity = MissedCallEntity(
                callerName = resolvedName,
                phoneNumber = phoneNumber,
                timestamp = now,
                urgency = urgency,
                reasonSummary = reason,
                isResolved = false,
                notified = true
            )

            dao.insertMissedCall(entity)

            // Trigger immediate local notification alert
            notificationMgr.sendInterceptedMissedCallAlert(
                callerName = resolvedName,
                phoneNumber = phoneNumber,
                urgency = urgency,
                reason = reason
            )

            dao.insertLog(
                com.example.data.local.entity.NotificationLogEntity(
                    title = "JARVIS Intercepted Missed Call: $resolvedName",
                    message = "$reason ($phoneNumber). Urgency: $urgency",
                    category = "Missed Call Interceptor",
                    timestamp = now
                )
            )

            return entity
        }
    }
}

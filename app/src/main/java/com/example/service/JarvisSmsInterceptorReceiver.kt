package com.example.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.telephony.SmsMessage
import android.util.Log
import com.example.data.local.AppDatabase
import com.example.data.local.entity.UrgentMessageEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * JARVIS SMS & Message Interceptor
 * Intercepts incoming messages, evaluates criticality (OTP, urgent keywords, bank, emergency, Seawoods meeting),
 * saves to local DB and triggers a high-priority heads-up local notification alert.
 */
class JarvisSmsInterceptorReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            if (messages.isNullOrEmpty()) return

            val sender = messages[0].displayOriginatingAddress ?: "Unknown Sender"
            val bodyBuilder = StringBuilder()
            for (msg in messages) {
                bodyBuilder.append(msg.displayMessageBody)
            }
            val fullBody = bodyBuilder.toString()

            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    handleInterceptedMessage(context, sender, fullBody)
                } catch (e: Exception) {
                    Log.e("JarvisSmsInterceptor", "Error intercepting SMS: ${e.message}")
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }

    companion object {
        suspend fun handleInterceptedMessage(
            context: Context,
            sender: String,
            rawBody: String,
            forcedUrgency: String? = null,
            forcedAction: String? = null
        ): UrgentMessageEntity {
            val db = AppDatabase.getDatabase(context)
            val dao = db.privaDao()
            val notificationMgr = PrivaNotificationManager(context)

            val lower = rawBody.lowercase()
            val isCritical = lower.contains("otp") || lower.contains("urgent") ||
                    lower.contains("emergency") || lower.contains("immediate") ||
                    lower.contains("hospital") || lower.contains("fraud") ||
                    lower.contains("blocked")

            val isHigh = lower.contains("action required") || lower.contains("seawoods") ||
                    lower.contains("meeting") || lower.contains("bank") ||
                    lower.contains("payment") || lower.contains("server") ||
                    lower.contains("deadline")

            val urgencyLevel = forcedUrgency ?: if (isCritical) "Critical" else if (isHigh) "High" else "Normal"
            val actionRequired = forcedAction ?: when {
                lower.contains("otp") -> "Do not share OTP. Review verification code."
                lower.contains("bank") || lower.contains("payment") -> "Review bank transaction and account integrity."
                lower.contains("seawoods") -> "Confirm attendance for Seawoods Grand Central work sync."
                lower.contains("urgent") -> "Respond immediately to sender."
                else -> "Read message and acknowledge."
            }

            val summary = if (rawBody.length > 90) rawBody.take(87) + "..." else rawBody
            val now = System.currentTimeMillis()

            val entity = UrgentMessageEntity(
                sender = sender,
                channel = "SMS",
                rawSnippet = rawBody,
                summary = summary,
                urgencyLevel = urgencyLevel,
                actionRequired = actionRequired,
                timestamp = now,
                isRead = false
            )

            dao.insertUrgentMessage(entity)

            // Trigger local notification alert
            notificationMgr.sendInterceptedUrgentMessageAlert(
                sender = sender,
                messageSnippet = summary,
                urgency = urgencyLevel,
                actionReq = actionRequired
            )

            dao.insertLog(
                com.example.data.local.entity.NotificationLogEntity(
                    title = "JARVIS Intercepted Message: $sender",
                    message = "[$urgencyLevel] $summary",
                    category = "Message Interceptor",
                    timestamp = now
                )
            )

            return entity
        }
    }
}

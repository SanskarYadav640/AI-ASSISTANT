package com.example.service

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.telephony.TelephonyManager
import com.example.data.local.AppDatabase
import com.example.data.local.entity.DailyPlanItemEntity
import com.example.data.local.entity.MissedCallEntity
import com.example.data.local.entity.UrgentMessageEntity
import com.example.data.local.entity.WaterIntakeLogEntity
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * JARVIS Notification Manager Service
 * Manages active interception of missed calls and urgent messages,
 * dispatches local notification alerts with actionable intents,
 * and tracks water hydration and Maharashtra meal schedules planned by JARVIS.
 */
class JarvisNotificationManagerService(private val context: Context) {

    private val db = AppDatabase.getDatabase(context)
    private val dao = db.privaDao()
    val notificationManager = PrivaNotificationManager(context)

    val callReceiver = JarvisCallInterceptorReceiver()
    val smsReceiver = JarvisSmsInterceptorReceiver()

    fun registerReceivers() {
        try {
            val phoneFilter = IntentFilter(TelephonyManager.ACTION_PHONE_STATE_CHANGED)
            context.registerReceiver(callReceiver, phoneFilter)
        } catch (ignored: Exception) {}

        try {
            val smsFilter = IntentFilter("android.provider.Telephony.SMS_RECEIVED")
            context.registerReceiver(smsReceiver, smsFilter)
        } catch (ignored: Exception) {}
    }

    fun unregisterReceivers() {
        try {
            context.unregisterReceiver(callReceiver)
        } catch (ignored: Exception) {}

        try {
            context.unregisterReceiver(smsReceiver)
        } catch (ignored: Exception) {}
    }

    /**
     * Intercept and trigger an immediate local alert for a missed call
     */
    suspend fun interceptMissedCall(
        callerName: String,
        phoneNumber: String,
        urgency: String,
        reason: String
    ): MissedCallEntity {
        return JarvisCallInterceptorReceiver.handleInterceptedMissedCall(
            context = context,
            phoneNumber = phoneNumber,
            reason = reason,
            callerName = callerName,
            urgency = urgency
        )
    }

    /**
     * Intercept and trigger an immediate local alert for an urgent message
     */
    suspend fun interceptUrgentMessage(
        sender: String,
        content: String,
        urgency: String,
        actionRequired: String
    ): UrgentMessageEntity {
        return JarvisSmsInterceptorReceiver.handleInterceptedMessage(
            context = context,
            sender = sender,
            rawBody = content,
            forcedUrgency = urgency,
            forcedAction = actionRequired
        )
    }

    /**
     * Water Hydration Tracking & Notification
     */
    suspend fun logWaterIntake(amountMl: Int): WaterIntakeLogEntity {
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val entity = WaterIntakeLogEntity(
            amountMl = amountMl,
            timestamp = System.currentTimeMillis(),
            dateString = todayStr
        )
        dao.insertWaterLog(entity)
        return entity
    }

    fun observeTodayWater(dateStr: String): Flow<Int?> {
        return dao.observeTotalWaterForDate(dateStr)
    }

    fun triggerHydrationReminder(currentMl: Int, goalMl: Int = 3000) {
        val remaining = (goalMl - currentMl).coerceAtLeast(0)
        val msg = if (remaining > 0) {
            "Sir, hydration checkpoint. Drink 250ml water now. $remaining ml remaining to hit your 3.0L goal."
        } else {
            "Hydration goal of 3,000ml achieved for today, Sir! Continue sipping as needed."
        }
        notificationManager.sendWaterHydrationReminder(currentMl, goalMl, msg)
    }

    /**
     * Maharashtra Meal Protocol Reminders
     */
    fun triggerMealReminder(item: DailyPlanItemEntity) {
        notificationManager.sendMaharashtraMealReminder(
            mealType = item.category,
            dishName = item.title,
            nutritionNote = item.details
        )
    }
}

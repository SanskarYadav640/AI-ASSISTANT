package com.example.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R

class PrivaNotificationManager(private val context: Context) {

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    companion object {
        const val CHANNEL_BRIEFINGS_ID = "priva_briefings_channel"
        const val CHANNEL_URGENT_ID = "priva_urgent_channel"
        const val CHANNEL_INTERCEPTOR_ID = "jarvis_interceptor_channel"
        const val CHANNEL_LIFESTYLE_ID = "jarvis_lifestyle_channel"

        const val ID_PERIODIC_BRIEFING = 1001
        const val ID_MISSED_CALL = 1002
        const val ID_URGENT_MESSAGE = 1003
        const val ID_ALARM_AUDIT = 1004
        const val ID_TRAFFIC_ALERT = 1005
        const val ID_SECTOR_RADAR = 1006
        const val ID_EMAIL_AUDIT = 1007
        const val ID_WAKE_UP_DETECTED = 1008
        const val ID_INTERCEPTED_CALL = 2001
        const val ID_INTERCEPTED_MSG = 2002
        const val ID_HYDRATION_ALERT = 2003
        const val ID_MEAL_ALERT = 2004
    }

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val briefingChannel = NotificationChannel(
                CHANNEL_BRIEFINGS_ID,
                context.getString(R.string.channel_briefings_name),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = context.getString(R.string.channel_briefings_desc)
                enableLights(true)
                enableVibration(true)
            }

            val urgentChannel = NotificationChannel(
                CHANNEL_URGENT_ID,
                context.getString(R.string.channel_urgent_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(R.string.channel_urgent_desc)
                enableLights(true)
                enableVibration(true)
            }

            val interceptorChannel = NotificationChannel(
                CHANNEL_INTERCEPTOR_ID,
                "JARVIS Interceptor Service",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Active interception alerts for missed calls and critical incoming communications"
                enableLights(true)
                enableVibration(true)
                lightColor = android.graphics.Color.CYAN
            }

            val lifestyleChannel = NotificationChannel(
                CHANNEL_LIFESTYLE_ID,
                "JARVIS Health & Routine",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Drink water reminders and Maharashtra meal schedule protocols"
                enableLights(true)
                enableVibration(true)
            }

            notificationManager.createNotificationChannel(briefingChannel)
            notificationManager.createNotificationChannel(urgentChannel)
            notificationManager.createNotificationChannel(interceptorChannel)
            notificationManager.createNotificationChannel(lifestyleChannel)
        }
    }

    private fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    private fun createPendingIntent(targetTab: String = "today"): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("EXTRA_TARGET_TAB", targetTab)
        }
        return PendingIntent.getActivity(
            context,
            targetTab.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    fun sendPeriodicBriefing(title: String, shortMessage: String, expandedText: String) {
        if (!hasNotificationPermission()) return

        val builder = NotificationCompat.Builder(context, CHANNEL_BRIEFINGS_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("🛡️ Priva AI: $title")
            .setContentText(shortMessage)
            .setStyle(NotificationCompat.BigTextStyle().bigText(expandedText))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(createPendingIntent("today"))
            .setAutoCancel(true)

        notificationManager.notify(ID_PERIODIC_BRIEFING, builder.build())
    }

    fun sendMissedCallAlert(callerName: String, summary: String, phone: String) {
        if (!hasNotificationPermission()) return

        val builder = NotificationCompat.Builder(context, CHANNEL_URGENT_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("📞 Missed Call: $callerName")
            .setContentText(summary)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Priva AI Reminder: You missed a call from $callerName ($phone).\n\nAI Urgency Note: $summary")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(createPendingIntent("alerts"))
            .setAutoCancel(true)

        notificationManager.notify(ID_MISSED_CALL, builder.build())
    }

    fun sendUrgentMessageAlert(sender: String, summary: String, actionRequired: String) {
        if (!hasNotificationPermission()) return

        val builder = NotificationCompat.Builder(context, CHANNEL_URGENT_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("⚡ Urgent Message from $sender")
            .setContentText(summary)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("TL;DR: $summary\nAction Required: $actionRequired\n(Stored securely on device)")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(createPendingIntent("alerts"))
            .setAutoCancel(true)

        notificationManager.notify(ID_URGENT_MESSAGE, builder.build())
    }

    fun sendAlarmAuditAlert(alarmLabel: String, currentVolume: Int, recommendation: String) {
        if (!hasNotificationPermission()) return

        val builder = NotificationCompat.Builder(context, CHANNEL_URGENT_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("⏰ Alarm Volume Check: $alarmLabel")
            .setContentText("Volume is $currentVolume%. $recommendation")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Current Alarm Stream Volume: $currentVolume%\nPriva Recommendation: $recommendation\nTap to adjust volume instantly.")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(createPendingIntent("alerts"))
            .setAutoCancel(true)

        notificationManager.notify(ID_ALARM_AUDIT, builder.build())
    }

    fun sendTrafficWarning(route: String, delayMinutes: Int, incident: String) {
        if (!hasNotificationPermission()) return

        val builder = NotificationCompat.Builder(context, CHANNEL_BRIEFINGS_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("🚗 Commute Alert: +$delayMinutes min delay")
            .setContentText("$route: $incident")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Traffic to Office ($route):\nDelay: +$delayMinutes minutes.\nIncident: $incident\nPriva suggests leaving 15 minutes earlier.")
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(createPendingIntent("today"))
            .setAutoCancel(true)

        notificationManager.notify(ID_TRAFFIC_ALERT, builder.build())
    }

    fun sendSectorInflationAlert(sector: String, changePercent: Double, advice: String) {
        if (!hasNotificationPermission()) return

        val builder = NotificationCompat.Builder(context, CHANNEL_BRIEFINGS_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("📈 Economic Forecast: $sector")
            .setContentText("Projected +$changePercent% costlier in next 6 months.")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Priva Sector Analysis:\n$sector is projected to increase by +$changePercent% over the next 6 months.\n\nHedge/Savings Advice: $advice")
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(createPendingIntent("radar"))
            .setAutoCancel(true)

        notificationManager.notify(ID_SECTOR_RADAR, builder.build())
    }

    fun sendMissingEmailAlert(sender: String, missingDetail: String) {
        if (!hasNotificationPermission()) return

        val builder = NotificationCompat.Builder(context, CHANNEL_URGENT_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("📬 Email Radar: Missing Action")
            .setContentText("From $sender: $missingDetail")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Priva Mail Scanner detected an unanswered priority item from $sender:\n\n$missingDetail")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(createPendingIntent("alerts"))
            .setAutoCancel(true)

        notificationManager.notify(ID_EMAIL_AUDIT, builder.build())
    }

    fun sendWakeUpAlert(timeFormatted: String, commuteMinutes: Int, destinationName: String) {
        if (!hasNotificationPermission()) return

        val builder = NotificationCompat.Builder(context, CHANNEL_BRIEFINGS_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("🌅 Good Morning: Wake Up Auto-Set at $timeFormatted")
            .setContentText("Detected phone unlock after 6 AM. Commute to $destinationName: $commuteMinutes min.")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Priva AI Wake-Up Logger:\n• Auto-logged wake-up time: $timeFormatted via phone unlock log.\n• Destination: $destinationName\n• Estimated Commute: $commuteMinutes min.\n\nTap to review today's briefing and agenda.")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(createPendingIntent("today"))
            .setAutoCancel(true)

        notificationManager.notify(ID_WAKE_UP_DETECTED, builder.build())
    }

    /**
     * Intercepts missed calls and triggers an immediate local notification alert
     * with direct 'Call Back' action button.
     */
    fun sendInterceptedMissedCallAlert(callerName: String, phoneNumber: String, urgency: String, reason: String) {
        if (!hasNotificationPermission()) return

        val dialIntent = Intent(Intent.ACTION_DIAL).apply {
            data = android.net.Uri.parse("tel:$phoneNumber")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val dialPendingIntent = PendingIntent.getActivity(
            context,
            (phoneNumber + "dial").hashCode(),
            dialIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_INTERCEPTOR_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("🛡️ JARVIS Interceptor: Missed Call from $callerName")
            .setContentText("Urgency: $urgency • $reason")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("JARVIS Security Interceptor detected an unanswered call.\n\n• Caller: $callerName ($phoneNumber)\n• Urgency Assessment: $urgency\n• Context / Intelligence: $reason\n\nTap Call Back to return call immediately.")
            )
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setContentIntent(createPendingIntent("alerts"))
            .addAction(android.R.drawable.ic_menu_call, "Call Back", dialPendingIntent)
            .setAutoCancel(true)

        notificationManager.notify(ID_INTERCEPTED_CALL, builder.build())
    }

    /**
     * Intercepts urgent SMS/messages and triggers an immediate local notification alert
     * with direct 'Reply via SMS' action button.
     */
    fun sendInterceptedUrgentMessageAlert(sender: String, messageSnippet: String, urgency: String, actionReq: String) {
        if (!hasNotificationPermission()) return

        val smsIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = android.net.Uri.parse("smsto:$sender")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val smsPendingIntent = PendingIntent.getActivity(
            context,
            (sender + "sms").hashCode(),
            smsIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_INTERCEPTOR_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("⚡ JARVIS Urgent Intercept: $sender")
            .setContentText("Action: $actionReq")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Priority Alert Intercepted by JARVIS:\n\n• From: $sender\n• Urgency: $urgency\n• Content: $messageSnippet\n• Recommended Action: $actionReq")
            )
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setContentIntent(createPendingIntent("alerts"))
            .addAction(android.R.drawable.ic_menu_send, "Reply SMS", smsPendingIntent)
            .setAutoCancel(true)

        notificationManager.notify(ID_INTERCEPTED_MSG, builder.build())
    }

    /**
     * Reminds user to drink water as planned by JARVIS
     */
    fun sendWaterHydrationReminder(drunkMl: Int, goalMl: Int, message: String) {
        if (!hasNotificationPermission()) return

        val builder = NotificationCompat.Builder(context, CHANNEL_LIFESTYLE_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("💧 JARVIS Hydration Protocol")
            .setContentText(message)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("JARVIS Health Protocol:\n$message\n\nProgress today: $drunkMl ml / $goalMl ml (${(drunkMl * 100 / goalMl.coerceAtLeast(1))}%)")
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(createPendingIntent("today"))
            .setAutoCancel(true)

        notificationManager.notify(ID_HYDRATION_ALERT, builder.build())
    }

    /**
     * Reminds user of scheduled Maharashtra meals planned by JARVIS
     */
    fun sendMaharashtraMealReminder(mealType: String, dishName: String, nutritionNote: String) {
        if (!hasNotificationPermission()) return

        val builder = NotificationCompat.Builder(context, CHANNEL_LIFESTYLE_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("🍽️ JARVIS Diet Protocol: $mealType")
            .setContentText(dishName)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Maharashtra Nutritional Schedule planned by JARVIS:\n\n• Meal: $mealType\n• Menu: $dishName\n• Health Objective: $nutritionNote\n\nOptimal digestion and sustained cognitive performance, Sir.")
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(createPendingIntent("today"))
            .setAutoCancel(true)

        notificationManager.notify(ID_MEAL_ALERT, builder.build())
    }
}

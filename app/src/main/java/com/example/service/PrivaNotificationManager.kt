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

        const val ID_PERIODIC_BRIEFING = 1001
        const val ID_MISSED_CALL = 1002
        const val ID_URGENT_MESSAGE = 1003
        const val ID_ALARM_AUDIT = 1004
        const val ID_TRAFFIC_ALERT = 1005
        const val ID_SECTOR_RADAR = 1006
        const val ID_EMAIL_AUDIT = 1007
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

            notificationManager.createNotificationChannel(briefingChannel)
            notificationManager.createNotificationChannel(urgentChannel)
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
}

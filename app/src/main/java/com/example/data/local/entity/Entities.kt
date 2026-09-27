package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "missed_calls")
data class MissedCallEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val callerName: String,
    val phoneNumber: String,
    val timestamp: Long,
    val urgency: String, // "Urgent", "Medium", "Normal"
    val reasonSummary: String,
    val isResolved: Boolean = false,
    val notified: Boolean = false
)

@Entity(tableName = "alarms")
data class AlarmEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val label: String,
    val timeHour: Int,
    val timeMinute: Int,
    val isEnabled: Boolean,
    val targetVolumePercent: Int,
    val aiRecommendation: String,
    val daysOfWeek: String
)

@Entity(tableName = "urgent_messages")
data class UrgentMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sender: String,
    val channel: String, // "SMS", "Signal", "WhatsApp", "Slack"
    val rawSnippet: String,
    val summary: String,
    val urgencyLevel: String, // "Critical", "High", "Normal"
    val actionRequired: String,
    val timestamp: Long,
    val isRead: Boolean = false
)

@Entity(tableName = "email_digests")
data class EmailDigestEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sender: String,
    val subject: String,
    val snippet: String,
    val category: String, // "Urgent Action", "Important Missing", "Financial", "General"
    val aiSummary: String,
    val isMissingImportant: Boolean,
    val missingDetail: String,
    val receivedTime: Long,
    val isAddressed: Boolean = false
)

@Entity(tableName = "commute_traffic")
data class CommuteTrafficEntity(
    @PrimaryKey val id: Long = 1,
    val homeAddress: String,
    val officeAddress: String,
    val currentRouteName: String,
    val normalMinutes: Int,
    val currentMinutes: Int,
    val delayMinutes: Int,
    val trafficCondition: String, // "Smooth", "Moderate Delay", "Heavy Delay"
    val incidentAlert: String,
    val lastUpdated: Long
)

@Entity(tableName = "news_items")
data class NewsItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val category: String, // "Local City", "Local AI", "Tech Economy"
    val source: String,
    val summary: String,
    val aiTakeaway: String,
    val timestamp: Long
)

@Entity(tableName = "sector_inflation")
data class SectorInflationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sectorName: String,
    val projectedChangePercent: Double,
    val isCostlier: Boolean,
    val timeHorizon: String,
    val primaryDrivers: String,
    val savingsAdvice: String
)

@Entity(tableName = "notification_logs")
data class NotificationLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val message: String,
    val category: String,
    val timestamp: Long,
    val isRead: Boolean = false
)

@Entity(tableName = "priva_settings")
data class PrivaSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val notificationFrequencyHours: Int = 2,
    val isUrgentOnly: Boolean = false,
    val enableAlarmVolumeAutoAdjust: Boolean = true,
    val enableMissedCallAlerts: Boolean = true,
    val enableTrafficBriefing: Boolean = true,
    val enableEmailAudit: Boolean = true,
    val privateOnDeviceOnly: Boolean = true,
    val homeLocation: String = "450 Evergreen Terrace",
    val officeLocation: String = "100 Innovation Tech Way",
    val lastBriefingTime: Long = System.currentTimeMillis()
)

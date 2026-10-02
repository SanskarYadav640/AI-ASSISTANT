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
    val distanceKm: Double = 16.5,
    val normalMinutes: Int,
    val currentMinutes: Int,
    val delayMinutes: Int,
    val trafficCondition: String, // "Smooth", "Moderate Delay", "Heavy Delay"
    val incidentAlert: String,
    val destinationNotes: String = "Workplace / Destination",
    val lastUpdated: Long
)

@Entity(tableName = "india_market_items")
data class IndiaMarketEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val symbol: String, // "GOLD24K", "SILVER999", "NIFTY50", "SENSEX", "TATAMOTORS", "RELIANCE", "TCS", "HDFCBANK"
    val name: String,
    val assetType: String, // "Precious Metal", "Index", "Stock"
    val priceInr: Double,
    val unit: String, // "per 10g", "per 1 kg", "pts", "₹ / share"
    val changeAmount: Double,
    val changePercent: Double,
    val isPositive: Boolean,
    val marketStatus: String = "NSE / BSE / MCX",
    val summary: String,
    val lastUpdated: Long = System.currentTimeMillis()
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
    val officeLocation: String = "Seawoods Grand Central Mall, Navi Mumbai",
    val lastBriefingTime: Long = System.currentTimeMillis()
)

@Entity(tableName = "phone_unlock_logs")
data class PhoneUnlockLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val hour: Int,
    val minute: Int,
    val dateString: String, // Format: "yyyy-MM-dd"
    val isWakeUpLog: Boolean = false, // Set to true if first unlock >= 6:00 AM on dateString
    val wakeUpNote: String = "",
    val unlockMethod: String = "Phone Screen Unlock"
)

@Entity(tableName = "calendar_events")
data class CalendarEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val eventTitle: String,
    val location: String = "Seawoods Grand Central Mall, Navi Mumbai",
    val startTimeEpoch: Long,
    val endTimeEpoch: Long,
    val description: String = "",
    val accountEmail: String = "SanskarYadav640@gmail.com",
    val isSyncedFromDevice: Boolean = false,
    val isPriority: Boolean = false
)

@Entity(tableName = "smartwatch_health")
data class SmartWatchHealthEntity(
    @PrimaryKey val id: Long = 1,
    val steps: Int = 6842,
    val dailyStepGoal: Int = 10000,
    val heartRateBpm: Int = 72,
    val sleepHours: Double = 7.4,
    val sleepScore: Int = 86,
    val caloriesKcal: Int = 485,
    val distanceKm: Double = 5.2,
    val watchModel: String = "Titan Smart World Watch",
    val devicePhone: String = "iQOO z9s (Health Connect Synced)",
    val watchBattery: Int = 82,
    val lastSyncTime: Long = System.currentTimeMillis()
)

@Entity(tableName = "water_intake_logs")
data class WaterIntakeLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amountMl: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val dateString: String // "yyyy-MM-dd"
)

@Entity(tableName = "daily_plan_items")
data class DailyPlanItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timeSlot: String, // e.g. "06:30 AM", "07:30 AM", "08:30 AM", "01:00 PM"
    val category: String, // "Hydration", "Maharashtra Breakfast", "Commute", "Maharashtra Lunch", "Maharashtra Snack", "Dinner", "Sleep"
    val title: String,
    val description: String,
    val details: String,
    val isCompleted: Boolean = false,
    val jarvisDirective: String = "JARVIS Daily Protocol"
)

@Entity(tableName = "saved_destinations")
data class SavedDestinationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val address: String,
    val category: String = "Work", // "Work", "Home", "Transit", "Fitness", "Leisure", "Saved"
    val estimatedMinutes: Int = 30,
    val distanceKm: Double = 15.0,
    val isFavorite: Boolean = true,
    val lastVisited: Long = System.currentTimeMillis()
)

@Entity(tableName = "car_connection_logs")
data class CarConnectionLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val carName: String,
    val connectionType: String, // "Android Auto (Projection)", "Car Mode (Dock)", "Bluetooth Audio (Hands-Free)"
    val connectedTimestamp: Long,
    val disconnectedTimestamp: Long? = null,
    val greetingSpoken: String,
    val destinationSelected: String? = null,
    val musicPlaylistStarted: String? = null
)

@Entity(tableName = "intercepted_notifications")
data class InterceptedNotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val notificationKey: String, // sbn.key
    val packageName: String,     // e.g. "com.whatsapp", "com.zomato", "com.google.android.gm"
    val appName: String,         // "WhatsApp", "Zomato", "Gmail", "HDFC Bank"
    val title: String,
    val content: String,
    val subText: String = "",
    val category: String,        // "Urgent Message", "Financial & OTP", "Security Alert", "Work & Update", "Promotional Spam", "Social Clutter"
    val priority: String,        // "URGENT", "REGULAR", "UNNECESSARY"
    val actionTaken: String,     // "PRIORITIZED", "SUMMARIZED", "REMOVED_FROM_SHADE"
    val isRemoved: Boolean,      // true if removed/cancelled from notification bar
    val timestamp: Long = System.currentTimeMillis(),
    val aiReasoning: String = "" // Explanation of categorization and removal decision
)

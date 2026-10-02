package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.AlarmEntity
import com.example.data.local.entity.CalendarEventEntity
import com.example.data.local.entity.CommuteTrafficEntity
import com.example.data.local.entity.DailyPlanItemEntity
import com.example.data.local.entity.EmailDigestEntity
import com.example.data.local.entity.MissedCallEntity
import com.example.data.local.entity.NewsItemEntity
import com.example.data.local.entity.NotificationLogEntity
import com.example.data.local.entity.PhoneUnlockLogEntity
import com.example.data.local.entity.PrivaSettingsEntity
import com.example.data.local.entity.SectorInflationEntity
import com.example.data.local.entity.SmartWatchHealthEntity
import com.example.data.local.entity.UrgentMessageEntity
import com.example.data.local.entity.WaterIntakeLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PrivaDao {

    // Missed Calls
    @Query("SELECT * FROM missed_calls ORDER BY timestamp DESC")
    fun getAllMissedCalls(): Flow<List<MissedCallEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMissedCall(call: MissedCallEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMissedCalls(calls: List<MissedCallEntity>)

    @Query("UPDATE missed_calls SET isResolved = :resolved WHERE id = :id")
    suspend fun setMissedCallResolved(id: Long, resolved: Boolean)

    @Query("DELETE FROM missed_calls WHERE id = :id")
    suspend fun deleteMissedCall(id: Long)

    @Query("DELETE FROM missed_calls")
    suspend fun clearAllMissedCalls()

    @Query("DELETE FROM missed_calls WHERE reasonSummary LIKE '%metabolic panel%' OR reasonSummary LIKE '%fraud security%' OR reasonSummary LIKE '%roadmap%'")
    suspend fun clearSampleMissedCalls()

    // Alarms
    @Query("SELECT * FROM alarms ORDER BY timeHour ASC, timeMinute ASC")
    fun getAllAlarms(): Flow<List<AlarmEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlarm(alarm: AlarmEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlarms(alarms: List<AlarmEntity>)

    @Update
    suspend fun updateAlarm(alarm: AlarmEntity)

    @Query("UPDATE alarms SET targetVolumePercent = :volume WHERE id = :id")
    suspend fun updateAlarmVolume(id: Long, volume: Int)

    @Query("UPDATE alarms SET isEnabled = :isEnabled WHERE id = :id")
    suspend fun toggleAlarm(id: Long, isEnabled: Boolean)

    // Urgent Messages
    @Query("SELECT * FROM urgent_messages ORDER BY timestamp DESC")
    fun getAllUrgentMessages(): Flow<List<UrgentMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUrgentMessage(message: UrgentMessageEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUrgentMessages(messages: List<UrgentMessageEntity>)

    @Query("UPDATE urgent_messages SET isRead = :isRead WHERE id = :id")
    suspend fun markMessageRead(id: Long, isRead: Boolean)

    @Query("DELETE FROM urgent_messages WHERE id = :id")
    suspend fun deleteUrgentMessage(id: Long)

    // Email Digests
    @Query("SELECT * FROM email_digests ORDER BY receivedTime DESC")
    fun getAllEmailDigests(): Flow<List<EmailDigestEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmailDigests(emails: List<EmailDigestEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmailDigest(email: EmailDigestEntity): Long

    @Query("DELETE FROM email_digests WHERE id = :id")
    suspend fun deleteEmailDigest(id: Long)

    @Query("DELETE FROM email_digests")
    suspend fun clearAllEmailDigests()

    @Query("UPDATE email_digests SET isAddressed = :addressed WHERE id = :id")
    suspend fun markEmailAddressed(id: Long, addressed: Boolean)

    // Commute Traffic
    @Query("SELECT * FROM commute_traffic WHERE id = 1 LIMIT 1")
    fun getCommuteTraffic(): Flow<CommuteTrafficEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateTraffic(traffic: CommuteTrafficEntity)

    // India Market (Gold, Silver, Stocks)
    @Query("SELECT * FROM india_market_items ORDER BY assetType ASC, priceInr DESC")
    fun getAllMarketItems(): Flow<List<com.example.data.local.entity.IndiaMarketEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMarketItems(items: List<com.example.data.local.entity.IndiaMarketEntity>)

    @Update
    suspend fun updateMarketItem(item: com.example.data.local.entity.IndiaMarketEntity)

    @Query("DELETE FROM india_market_items")
    suspend fun clearAllMarketItems()

    @Query("SELECT COUNT(*) FROM india_market_items")
    suspend fun getMarketItemsCount(): Int

    // News Items
    @Query("SELECT * FROM news_items ORDER BY timestamp DESC")
    fun getAllNews(): Flow<List<NewsItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNews(items: List<NewsItemEntity>)

    @Query("DELETE FROM news_items")
    suspend fun clearAllNews()

    // Sector Inflation
    @Query("SELECT * FROM sector_inflation ORDER BY projectedChangePercent DESC")
    fun getAllSectors(): Flow<List<SectorInflationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSectors(sectors: List<SectorInflationEntity>)

    // Notification Logs
    @Query("SELECT * FROM notification_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<NotificationLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: NotificationLogEntity): Long

    @Query("DELETE FROM notification_logs")
    suspend fun clearLogs()

    // Settings
    @Query("SELECT * FROM priva_settings WHERE id = 1 LIMIT 1")
    fun getSettings(): Flow<PrivaSettingsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateSettings(settings: PrivaSettingsEntity)

    @Query("SELECT COUNT(*) FROM missed_calls")
    suspend fun getMissedCallsCount(): Int

    @Query("SELECT COUNT(*) FROM alarms")
    suspend fun getAlarmsCount(): Int

    @Query("SELECT COUNT(*) FROM urgent_messages")
    suspend fun getUrgentMessagesCount(): Int

    @Query("SELECT COUNT(*) FROM email_digests")
    suspend fun getEmailDigestsCount(): Int

    @Query("SELECT COUNT(*) FROM news_items")
    suspend fun getNewsCount(): Int

    @Query("SELECT COUNT(*) FROM sector_inflation")
    suspend fun getSectorsCount(): Int

    // Phone Unlock Logs (Auto Wake-up after 6 AM)
    @Query("SELECT * FROM phone_unlock_logs ORDER BY timestamp DESC")
    fun getAllUnlockLogs(): Flow<List<PhoneUnlockLogEntity>>

    @Query("SELECT * FROM phone_unlock_logs WHERE dateString = :dateStr AND isWakeUpLog = 1 LIMIT 1")
    suspend fun getWakeUpLogForDate(dateStr: String): PhoneUnlockLogEntity?

    @Query("SELECT * FROM phone_unlock_logs WHERE dateString = :dateStr AND isWakeUpLog = 1 LIMIT 1")
    fun observeWakeUpLogForDate(dateStr: String): Flow<PhoneUnlockLogEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUnlockLog(log: PhoneUnlockLogEntity): Long

    @Query("DELETE FROM phone_unlock_logs")
    suspend fun clearUnlockLogs()

    // Calendar Events
    @Query("SELECT * FROM calendar_events ORDER BY startTimeEpoch ASC")
    fun getAllCalendarEvents(): Flow<List<CalendarEventEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCalendarEvent(event: CalendarEventEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCalendarEvents(events: List<CalendarEventEntity>)

    @Query("DELETE FROM calendar_events WHERE id = :id")
    suspend fun deleteCalendarEvent(id: Long)

    @Query("DELETE FROM calendar_events")
    suspend fun clearCalendarEvents()

    @Query("SELECT COUNT(*) FROM calendar_events")
    suspend fun getCalendarEventsCount(): Int

    // Smart Watch & Health Connect
    @Query("SELECT * FROM smartwatch_health WHERE id = 1 LIMIT 1")
    fun getSmartWatchHealth(): Flow<SmartWatchHealthEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateSmartWatchHealth(health: SmartWatchHealthEntity)

    // Water Intake Logs & Reminders
    @Query("SELECT * FROM water_intake_logs WHERE dateString = :dateStr ORDER BY timestamp ASC")
    fun getWaterLogsForDate(dateStr: String): Flow<List<WaterIntakeLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWaterLog(log: WaterIntakeLogEntity): Long

    @Query("SELECT SUM(amountMl) FROM water_intake_logs WHERE dateString = :dateStr")
    fun observeTotalWaterForDate(dateStr: String): Flow<Int?>

    @Query("DELETE FROM water_intake_logs WHERE dateString = :dateStr")
    suspend fun clearWaterLogsForDate(dateStr: String)

    // JARVIS Daily Master Plan (Hydration & Maharashtra Meals)
    @Query("SELECT * FROM daily_plan_items ORDER BY id ASC")
    fun getAllDailyPlanItems(): Flow<List<DailyPlanItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailyPlanItems(items: List<DailyPlanItemEntity>)

    @Query("UPDATE daily_plan_items SET isCompleted = :completed WHERE id = :id")
    suspend fun updateDailyPlanItemStatus(id: Long, completed: Boolean)

    @Query("SELECT COUNT(*) FROM daily_plan_items")
    suspend fun getDailyPlanItemsCount(): Int

    @Query("DELETE FROM daily_plan_items")
    suspend fun clearDailyPlanItems()

    // Saved Destinations for Car & Commute
    @Query("SELECT * FROM saved_destinations ORDER BY isFavorite DESC, lastVisited DESC")
    fun getAllSavedDestinations(): Flow<List<com.example.data.local.entity.SavedDestinationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavedDestination(destination: com.example.data.local.entity.SavedDestinationEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavedDestinations(destinations: List<com.example.data.local.entity.SavedDestinationEntity>)

    @Update
    suspend fun updateSavedDestination(destination: com.example.data.local.entity.SavedDestinationEntity)

    @Query("DELETE FROM saved_destinations WHERE id = :id")
    suspend fun deleteSavedDestination(id: Long)

    @Query("SELECT COUNT(*) FROM saved_destinations")
    suspend fun getSavedDestinationsCount(): Int

    // Car Connection Logs
    @Query("SELECT * FROM car_connection_logs ORDER BY connectedTimestamp DESC")
    fun getAllCarConnectionLogs(): Flow<List<com.example.data.local.entity.CarConnectionLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCarConnectionLog(log: com.example.data.local.entity.CarConnectionLogEntity): Long

    @Update
    suspend fun updateCarConnectionLog(log: com.example.data.local.entity.CarConnectionLogEntity)

    @Query("DELETE FROM car_connection_logs")
    suspend fun clearCarConnectionLogs()

    @Query("SELECT COUNT(*) FROM car_connection_logs")
    suspend fun getCarConnectionLogsCount(): Int

    // Intercepted Notifications (Priority, Urgent, Regular, Unnecessary)
    @Query("SELECT * FROM intercepted_notifications ORDER BY timestamp DESC")
    fun getAllInterceptedNotifications(): Flow<List<com.example.data.local.entity.InterceptedNotificationEntity>>

    @Query("SELECT * FROM intercepted_notifications WHERE priority = :priority ORDER BY timestamp DESC")
    fun getNotificationsByPriority(priority: String): Flow<List<com.example.data.local.entity.InterceptedNotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInterceptedNotification(item: com.example.data.local.entity.InterceptedNotificationEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInterceptedNotifications(items: List<com.example.data.local.entity.InterceptedNotificationEntity>)

    @Query("DELETE FROM intercepted_notifications WHERE id = :id")
    suspend fun deleteInterceptedNotification(id: Long)

    @Query("DELETE FROM intercepted_notifications")
    suspend fun clearAllInterceptedNotifications()

    @Query("SELECT COUNT(*) FROM intercepted_notifications")
    suspend fun getInterceptedNotificationsCount(): Int

    @Query("SELECT COUNT(*) FROM intercepted_notifications WHERE priority = :priority")
    suspend fun getInterceptedNotificationsCountByPriority(priority: String): Int
}

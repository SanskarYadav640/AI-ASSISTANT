package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.AlarmEntity
import com.example.data.local.entity.CommuteTrafficEntity
import com.example.data.local.entity.EmailDigestEntity
import com.example.data.local.entity.MissedCallEntity
import com.example.data.local.entity.NewsItemEntity
import com.example.data.local.entity.NotificationLogEntity
import com.example.data.local.entity.PrivaSettingsEntity
import com.example.data.local.entity.SectorInflationEntity
import com.example.data.local.entity.UrgentMessageEntity
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

    @Query("UPDATE email_digests SET isAddressed = :addressed WHERE id = :id")
    suspend fun markEmailAddressed(id: Long, addressed: Boolean)

    // Commute Traffic
    @Query("SELECT * FROM commute_traffic WHERE id = 1 LIMIT 1")
    fun getCommuteTraffic(): Flow<CommuteTrafficEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateTraffic(traffic: CommuteTrafficEntity)

    // News Items
    @Query("SELECT * FROM news_items ORDER BY timestamp DESC")
    fun getAllNews(): Flow<List<NewsItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNews(items: List<NewsItemEntity>)

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
}

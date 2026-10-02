package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.PrivaDao
import com.example.data.local.entity.AlarmEntity
import com.example.data.local.entity.CalendarEventEntity
import com.example.data.local.entity.CommuteTrafficEntity
import com.example.data.local.entity.DailyPlanItemEntity
import com.example.data.local.entity.EmailDigestEntity
import com.example.data.local.entity.IndiaMarketEntity
import com.example.data.local.entity.MissedCallEntity
import com.example.data.local.entity.NewsItemEntity
import com.example.data.local.entity.NotificationLogEntity
import com.example.data.local.entity.PhoneUnlockLogEntity
import com.example.data.local.entity.PrivaSettingsEntity
import com.example.data.local.entity.SectorInflationEntity
import com.example.data.local.entity.SavedDestinationEntity
import com.example.data.local.entity.CarConnectionLogEntity
import com.example.data.local.entity.InterceptedNotificationEntity
import com.example.data.local.entity.SmartWatchHealthEntity
import com.example.data.local.entity.UrgentMessageEntity
import com.example.data.local.entity.WaterIntakeLogEntity

@Database(
    entities = [
        MissedCallEntity::class,
        AlarmEntity::class,
        UrgentMessageEntity::class,
        EmailDigestEntity::class,
        CommuteTrafficEntity::class,
        IndiaMarketEntity::class,
        NewsItemEntity::class,
        SectorInflationEntity::class,
        NotificationLogEntity::class,
        PrivaSettingsEntity::class,
        PhoneUnlockLogEntity::class,
        CalendarEventEntity::class,
        SmartWatchHealthEntity::class,
        WaterIntakeLogEntity::class,
        DailyPlanItemEntity::class,
        SavedDestinationEntity::class,
        CarConnectionLogEntity::class,
        InterceptedNotificationEntity::class
    ],
    version = 7,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun privaDao(): PrivaDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "priva_ai_vault.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

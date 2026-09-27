package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.PrivaDao
import com.example.data.local.entity.AlarmEntity
import com.example.data.local.entity.CommuteTrafficEntity
import com.example.data.local.entity.EmailDigestEntity
import com.example.data.local.entity.MissedCallEntity
import com.example.data.local.entity.NewsItemEntity
import com.example.data.local.entity.NotificationLogEntity
import com.example.data.local.entity.PrivaSettingsEntity
import com.example.data.local.entity.SectorInflationEntity
import com.example.data.local.entity.UrgentMessageEntity

@Database(
    entities = [
        MissedCallEntity::class,
        AlarmEntity::class,
        UrgentMessageEntity::class,
        EmailDigestEntity::class,
        CommuteTrafficEntity::class,
        NewsItemEntity::class,
        SectorInflationEntity::class,
        NotificationLogEntity::class,
        PrivaSettingsEntity::class
    ],
    version = 1,
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
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

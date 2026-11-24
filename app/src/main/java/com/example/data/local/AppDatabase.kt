package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
  entities = [DailyRecordEntity::class, NextDoseTimerEntity::class],
  version = 2,
  exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
  abstract fun dailyRecordDao(): DailyRecordDao

  abstract fun nextDoseTimerDao(): NextDoseTimerDao

  companion object {
    @Volatile private var instance: AppDatabase? = null

    fun getInstance(context: Context): AppDatabase {
      return instance
        ?: synchronized(this) {
          instance
            ?: Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "senior_health_check.db",
              )
              .fallbackToDestructiveMigration(dropAllTables = true)
              .build()
              .also { instance = it }
        }
    }
  }
}

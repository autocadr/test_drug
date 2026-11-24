package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface NextDoseTimerDao {
  @Query("SELECT * FROM next_dose_timer WHERE id = 1 LIMIT 1")
  fun observeTimer(): Flow<NextDoseTimerEntity?>

  @Query("SELECT * FROM next_dose_timer WHERE id = 1 LIMIT 1")
  suspend fun getTimer(): NextDoseTimerEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsertTimer(timer: NextDoseTimerEntity)

  @Query("DELETE FROM next_dose_timer WHERE id = 1")
  suspend fun clearTimer()

  @Query("UPDATE next_dose_timer SET notified = 1 WHERE id = 1")
  suspend fun markNotified()
}

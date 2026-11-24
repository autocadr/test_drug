package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "next_dose_timer")
data class NextDoseTimerEntity(
  @PrimaryKey val id: Int = SINGLETON_ID,
  val targetTimeMillis: Long,
  val triggerMealType: String,
  val triggerMealLabel: String,
  val notified: Boolean = false,
) {
  companion object {
    const val SINGLETON_ID = 1
  }
}

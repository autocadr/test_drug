package com.example.data.repository

import com.example.data.local.DailyRecordDao
import com.example.data.local.DailyRecordEntity
import com.example.data.local.NextDoseTimerDao
import com.example.data.local.NextDoseTimerEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

enum class MealType(val koreanLabel: String) {
  MORNING("아침 약"),
  LUNCH("점심 약"),
  DINNER("저녁 약"),
}

sealed interface MealToggleOutcome {
  data class TimerStarted(val targetTimeMillis: Long, val mealLabel: String) : MealToggleOutcome

  data object TimerCancelled : MealToggleOutcome

  data object NoTimerChange : MealToggleOutcome
}

interface DailyHealthRepository {
  fun observeRecord(dateKey: String): Flow<DailyRecordEntity>

  fun observeTimer(): Flow<NextDoseTimerEntity?>

  suspend fun clearOldRecords(todayKey: String)

  suspend fun toggleMeal(
    dateKey: String,
    mealType: MealType,
    nowMillis: Long,
    countdownDurationMillis: Long = EIGHT_HOURS_MILLIS,
  ): MealToggleOutcome

  suspend fun incrementWater(dateKey: String)

  suspend fun decrementWater(dateKey: String)

  suspend fun markTimerNotified()

  suspend fun clearTimer()

  companion object {
    const val EIGHT_HOURS_MILLIS = 8L * 60L * 60L * 1000L
  }
}

class RoomDailyHealthRepository(
  private val dao: DailyRecordDao,
  private val timerDao: NextDoseTimerDao,
) : DailyHealthRepository {

  override fun observeRecord(dateKey: String): Flow<DailyRecordEntity> {
    return dao.observeRecord(dateKey).map { record ->
      record ?: DailyRecordEntity(dateKey = dateKey)
    }
  }

  override fun observeTimer(): Flow<NextDoseTimerEntity?> {
    return timerDao.observeTimer()
  }

  override suspend fun clearOldRecords(todayKey: String) {
    dao.clearOldRecords(todayKey)
  }

  override suspend fun toggleMeal(
    dateKey: String,
    mealType: MealType,
    nowMillis: Long,
    countdownDurationMillis: Long,
  ): MealToggleOutcome {
    val current = dao.getRecord(dateKey) ?: DailyRecordEntity(dateKey = dateKey)
    val updated =
      when (mealType) {
        MealType.MORNING -> current.copy(morningTaken = !current.morningTaken)
        MealType.LUNCH -> current.copy(lunchTaken = !current.lunchTaken)
        MealType.DINNER -> current.copy(dinnerTaken = !current.dinnerTaken)
      }.copy(updatedAtMillis = nowMillis)
    dao.upsertRecord(updated)

    val becameTaken =
      when (mealType) {
        MealType.MORNING -> updated.morningTaken
        MealType.LUNCH -> updated.lunchTaken
        MealType.DINNER -> updated.dinnerTaken
      }

    return if (becameTaken) {
      val targetMillis = nowMillis + countdownDurationMillis
      timerDao.upsertTimer(
        NextDoseTimerEntity(
          targetTimeMillis = targetMillis,
          triggerMealType = mealType.name,
          triggerMealLabel = mealType.koreanLabel,
          notified = false,
        )
      )
      MealToggleOutcome.TimerStarted(targetMillis, mealType.koreanLabel)
    } else {
      val activeTimer = timerDao.getTimer()
      val allUnchecked = !updated.morningTaken && !updated.lunchTaken && !updated.dinnerTaken
      if (activeTimer != null && (allUnchecked || activeTimer.triggerMealType == mealType.name)) {
        timerDao.clearTimer()
        MealToggleOutcome.TimerCancelled
      } else {
        MealToggleOutcome.NoTimerChange
      }
    }
  }

  override suspend fun incrementWater(dateKey: String) {
    val current = dao.getRecord(dateKey) ?: DailyRecordEntity(dateKey = dateKey)
    val updated =
      current.copy(
        waterCups = (current.waterCups + 1).coerceAtMost(99),
        updatedAtMillis = System.currentTimeMillis(),
      )
    dao.upsertRecord(updated)
  }

  override suspend fun decrementWater(dateKey: String) {
    val current = dao.getRecord(dateKey) ?: DailyRecordEntity(dateKey = dateKey)
    if (current.waterCups <= 0) return
    val updated =
      current.copy(
        waterCups = (current.waterCups - 1).coerceAtLeast(0),
        updatedAtMillis = System.currentTimeMillis(),
      )
    dao.upsertRecord(updated)
  }

  override suspend fun markTimerNotified() {
    timerDao.markNotified()
  }

  override suspend fun clearTimer() {
    timerDao.clearTimer()
  }
}

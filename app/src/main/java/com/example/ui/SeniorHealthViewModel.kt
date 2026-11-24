package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.DailyRecordEntity
import com.example.data.local.NextDoseTimerEntity
import com.example.data.repository.DailyHealthRepository
import com.example.data.repository.MealToggleOutcome
import com.example.data.repository.MealType
import com.example.notification.DoseAlarmScheduler
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

sealed interface NextDoseTimerUiState {
  data object Idle : NextDoseTimerUiState

  data class Counting(
    val remainingMillis: Long,
    val formattedRemaining: String,
    val scheduledTimeLabel: String,
    val triggerMealLabel: String,
  ) : NextDoseTimerUiState

  data class Completed(val triggerMealLabel: String) : NextDoseTimerUiState
}

sealed interface SeniorHealthUiState {
  data object Loading : SeniorHealthUiState

  data class Ready(
    val dateKey: String,
    val formattedDate: String,
    val morningTaken: Boolean,
    val lunchTaken: Boolean,
    val dinnerTaken: Boolean,
    val waterCups: Int,
    val waterGoalCups: Int = DEFAULT_WATER_GOAL,
    val nextDoseTimer: NextDoseTimerUiState = NextDoseTimerUiState.Idle,
  ) : SeniorHealthUiState {
    val completedMedsCount: Int
      get() = listOf(morningTaken, lunchTaken, dinnerTaken).count { it }

    val totalMedsCount: Int
      get() = 3
  }

  companion object {
    const val DEFAULT_WATER_GOAL = 8
  }
}

interface DateProvider {
  fun currentTimeMillis(): Long

  fun currentDateKey(): String

  fun formatKoreanDate(dateKey: String): String

  fun formatScheduledClockTime(targetTimeMillis: Long): String
}

class SystemDateProvider : DateProvider {
  override fun currentTimeMillis(): Long = System.currentTimeMillis()

  override fun currentDateKey(): String {
    val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    return formatter.format(Date(currentTimeMillis()))
  }

  override fun formatKoreanDate(dateKey: String): String {
    val keyFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    val displayFormatter = SimpleDateFormat("yyyy년 M월 d일 EEEE", Locale.KOREAN)
    val parsed = runCatching { keyFormatter.parse(dateKey) }.getOrNull() ?: Date()
    return displayFormatter.format(parsed)
  }

  override fun formatScheduledClockTime(targetTimeMillis: Long): String {
    val formatter = SimpleDateFormat("a h시 mm분", Locale.KOREAN)
    return formatter.format(Date(targetTimeMillis))
  }
}

@OptIn(ExperimentalCoroutinesApi::class)
class SeniorHealthViewModel(
  private val repository: DailyHealthRepository,
  private val alarmScheduler: DoseAlarmScheduler,
  private val dateProvider: DateProvider = SystemDateProvider(),
  private val countdownDurationMillis: Long = DailyHealthRepository.EIGHT_HOURS_MILLIS,
  private val enableTicker: Boolean = true,
) : ViewModel() {

  private val currentDateKey = MutableStateFlow(dateProvider.currentDateKey())
  private val nowMillisFlow = MutableStateFlow(dateProvider.currentTimeMillis())

  private val todayRecordFlow =
    currentDateKey.flatMapLatest { dateKey -> repository.observeRecord(dateKey) }

  val uiState: StateFlow<SeniorHealthUiState> =
    combine(todayRecordFlow, repository.observeTimer(), nowMillisFlow) {
        record: DailyRecordEntity,
        timer: NextDoseTimerEntity?,
        nowMillis: Long ->
        val timerState = buildTimerUiState(timer, nowMillis)
        SeniorHealthUiState.Ready(
          dateKey = record.dateKey,
          formattedDate = dateProvider.formatKoreanDate(record.dateKey),
          morningTaken = record.morningTaken,
          lunchTaken = record.lunchTaken,
          dinnerTaken = record.dinnerTaken,
          waterCups = record.waterCups,
          nextDoseTimer = timerState,
        )
      }
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SeniorHealthUiState.Loading,
      )

  init {
    viewModelScope.launch { repository.clearOldRecords(currentDateKey.value) }
    if (enableTicker) {
      viewModelScope.launch {
        while (isActive) {
          syncClockAndMidnight()
          delay(1000L)
        }
      }
    }
  }

  fun refreshDateIfNeeded() {
    syncClockAndMidnight()
  }

  private fun syncClockAndMidnight() {
    nowMillisFlow.value = dateProvider.currentTimeMillis()
    val todayKey = dateProvider.currentDateKey()
    if (currentDateKey.value != todayKey) {
      currentDateKey.value = todayKey
      viewModelScope.launch { repository.clearOldRecords(todayKey) }
    }
  }

  private fun buildTimerUiState(
    timer: NextDoseTimerEntity?,
    nowMillis: Long,
  ): NextDoseTimerUiState {
    if (timer == null) return NextDoseTimerUiState.Idle
    val remainingMillis = timer.targetTimeMillis - nowMillis
    return if (remainingMillis > 0L) {
      NextDoseTimerUiState.Counting(
        remainingMillis = remainingMillis,
        formattedRemaining = formatCountdown(remainingMillis),
        scheduledTimeLabel = dateProvider.formatScheduledClockTime(timer.targetTimeMillis),
        triggerMealLabel = timer.triggerMealLabel,
      )
    } else {
      if (!timer.notified) {
        viewModelScope.launch {
          alarmScheduler.cancelAlarm()
          alarmScheduler.showNextDoseNotification(timer.triggerMealLabel)
          repository.markTimerNotified()
        }
      }
      NextDoseTimerUiState.Completed(triggerMealLabel = timer.triggerMealLabel)
    }
  }

  fun onToggleMeal(mealType: MealType) {
    syncClockAndMidnight()
    val dateKey = currentDateKey.value
    val nowMillis = dateProvider.currentTimeMillis()
    viewModelScope.launch {
      when (
        val outcome =
          repository.toggleMeal(
            dateKey = dateKey,
            mealType = mealType,
            nowMillis = nowMillis,
            countdownDurationMillis = countdownDurationMillis,
          )
      ) {
        is MealToggleOutcome.TimerStarted -> {
          alarmScheduler.scheduleAlarm(outcome.targetTimeMillis, outcome.mealLabel)
        }
        MealToggleOutcome.TimerCancelled -> {
          alarmScheduler.cancelAlarm()
        }
        MealToggleOutcome.NoTimerChange -> Unit
      }
    }
  }

  fun onDismissCompletedTimer() {
    viewModelScope.launch {
      alarmScheduler.cancelAlarm()
      repository.clearTimer()
    }
  }

  fun onAddWaterCup() {
    syncClockAndMidnight()
    val dateKey = currentDateKey.value
    viewModelScope.launch { repository.incrementWater(dateKey) }
  }

  fun onRemoveWaterCup() {
    syncClockAndMidnight()
    val dateKey = currentDateKey.value
    viewModelScope.launch { repository.decrementWater(dateKey) }
  }

  companion object {
    fun formatCountdown(remainingMillis: Long): String {
      val totalSeconds = (remainingMillis / 1000L).coerceAtLeast(0L)
      val hours = totalSeconds / 3600L
      val minutes = (totalSeconds % 3600L) / 60L
      val seconds = totalSeconds % 60L
      return String.format(Locale.KOREAN, "%02d시간 %02d분 %02d초", hours, minutes, seconds)
    }
  }

  class Factory(
    private val repository: DailyHealthRepository,
    private val alarmScheduler: DoseAlarmScheduler,
    private val dateProvider: DateProvider = SystemDateProvider(),
  ) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
      if (modelClass.isAssignableFrom(SeniorHealthViewModel::class.java)) {
        return SeniorHealthViewModel(repository, alarmScheduler, dateProvider) as T
      }
      throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
  }
}

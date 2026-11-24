package com.example

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.repository.DailyHealthRepository
import com.example.data.repository.MealType
import com.example.data.repository.RoomDailyHealthRepository
import com.example.notification.DoseAlarmScheduler
import com.example.ui.DateProvider
import com.example.ui.NextDoseTimerUiState
import com.example.ui.SeniorHealthScreen
import com.example.ui.SeniorHealthUiState
import com.example.ui.SeniorHealthViewModel
import com.example.ui.theme.SeniorHealthTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var database: AppDatabase
  private lateinit var repository: RoomDailyHealthRepository
  private lateinit var fakeScheduler: FakeDoseAlarmScheduler
  private lateinit var fakeDateProvider: FakeDateProvider

  private class FakeDoseAlarmScheduler : DoseAlarmScheduler {
    var scheduledTargetMillis: Long? = null
    var scheduledMealLabel: String? = null
    var cancelledCount: Int = 0
    val notificationsShown = mutableListOf<String>()

    override fun scheduleAlarm(targetTimeMillis: Long, mealLabel: String) {
      scheduledTargetMillis = targetTimeMillis
      scheduledMealLabel = mealLabel
    }

    override fun cancelAlarm() {
      cancelledCount += 1
      scheduledTargetMillis = null
    }

    override fun showNextDoseNotification(mealLabel: String) {
      notificationsShown.add(mealLabel)
    }
  }

  private class FakeDateProvider(
    var nowMillis: Long = 1_700_000_000_000L,
    var dateKey: String = "2026-11-17",
  ) : DateProvider {
    override fun currentTimeMillis(): Long = nowMillis

    override fun currentDateKey(): String = dateKey

    override fun formatKoreanDate(dateKey: String): String =
      if (dateKey == "2026-11-18") "2026년 11월 18일 수요일" else "2026년 11월 17일 화요일"

    override fun formatScheduledClockTime(targetTimeMillis: Long): String = "오후 4시 00분"
  }

  @Before
  fun setUp() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    database =
      Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
        .allowMainThreadQueries()
        .build()
    repository =
      RoomDailyHealthRepository(
        dao = database.dailyRecordDao(),
        timerDao = database.nextDoseTimerDao(),
      )
    fakeScheduler = FakeDoseAlarmScheduler()
    fakeDateProvider = FakeDateProvider()
  }

  @After
  fun tearDown() {
    database.close()
  }

  @Test
  fun `app_name matches localized Korean title`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("오늘 약·물 체크", appName)
  }

  @Test
  fun `toggling medication and adding water persists in Room across ViewModel recreation`() {
    runBlocking {
      val viewModel1 =
        SeniorHealthViewModel(
          repository = repository,
          alarmScheduler = fakeScheduler,
          dateProvider = fakeDateProvider,
          enableTicker = false,
        )
      viewModel1.onToggleMeal(MealType.MORNING)
      viewModel1.onAddWaterCup()
      viewModel1.onAddWaterCup()

      val saved =
        repository.observeRecord("2026-11-17").first { it.morningTaken && it.waterCups == 2 }
      assertTrue(saved.morningTaken)
      assertEquals(2, saved.waterCups)

      val viewModel2 =
        SeniorHealthViewModel(
          repository = repository,
          alarmScheduler = fakeScheduler,
          dateProvider = fakeDateProvider,
          enableTicker = false,
        )
      composeTestRule.setContent {
        SeniorHealthTheme {
          val state = viewModel2.uiState.collectAsStateWithLifecycle().value
          SeniorHealthScreen(
            uiState = state,
            hasNotificationPermission = true,
            onRequestNotificationPermission = {},
            onToggleMeal = viewModel2::onToggleMeal,
            onDismissCompletedTimer = viewModel2::onDismissCompletedTimer,
            onAddWaterCup = viewModel2::onAddWaterCup,
            onRemoveWaterCup = viewModel2::onRemoveWaterCup,
          )
        }
      }
      composeTestRule.waitForIdle()

      composeTestRule.onNodeWithTag("today_date_text").assertTextEquals("2026년 11월 17일 화요일")
      composeTestRule.onNodeWithText("아침 약").assertIsDisplayed()
      composeTestRule.onNodeWithText("점심 약").assertIsDisplayed()
      composeTestRule.onNodeWithText("저녁 약").assertIsDisplayed()
      composeTestRule.onNodeWithText("복용 완료").assertIsDisplayed()
      composeTestRule.onNodeWithTag("water_count_number").assertTextEquals("2")

      composeTestRule.onNodeWithTag("add_water_button").performScrollTo().performClick()
      composeTestRule.waitForIdle()
      composeTestRule.onNodeWithTag("water_count_number").assertTextEquals("3")
    }
  }

  @Test
  fun `midnight rollover resets medication checks and water cups automatically`() {
    runBlocking {
      val viewModel =
        SeniorHealthViewModel(
          repository = repository,
          alarmScheduler = fakeScheduler,
          dateProvider = fakeDateProvider,
          enableTicker = false,
        )
      viewModel.onToggleMeal(MealType.MORNING)
      viewModel.onToggleMeal(MealType.LUNCH)
      viewModel.onAddWaterCup()
      viewModel.onAddWaterCup()

      val beforeMidnight =
        repository.observeRecord("2026-11-17").first { it.morningTaken && it.waterCups == 2 }
      assertTrue(beforeMidnight.morningTaken)
      assertTrue(beforeMidnight.lunchTaken)
      assertEquals(2, beforeMidnight.waterCups)

      // Advance clock past midnight to 2026-11-18
      fakeDateProvider.dateKey = "2026-11-18"
      fakeDateProvider.nowMillis += 24L * 60L * 60L * 1000L
      viewModel.refreshDateIfNeeded()

      val readyState =
        viewModel.uiState.first {
          it is SeniorHealthUiState.Ready && it.dateKey == "2026-11-18"
        } as SeniorHealthUiState.Ready

      assertEquals("2026년 11월 18일 수요일", readyState.formattedDate)
      assertFalse(readyState.morningTaken)
      assertFalse(readyState.lunchTaken)
      assertFalse(readyState.dinnerTaken)
      assertEquals(0, readyState.waterCups)
    }
  }

  @Test
  fun `checking medication starts 8 hour countdown and sends notification on completion`() {
    runBlocking {
      val startMillis = fakeDateProvider.nowMillis
      val viewModel =
        SeniorHealthViewModel(
          repository = repository,
          alarmScheduler = fakeScheduler,
          dateProvider = fakeDateProvider,
          enableTicker = false,
        )

      viewModel.onToggleMeal(MealType.MORNING)

      val countingState =
        viewModel.uiState.first {
          it is SeniorHealthUiState.Ready && it.nextDoseTimer is NextDoseTimerUiState.Counting
        } as SeniorHealthUiState.Ready

      val timer = countingState.nextDoseTimer as NextDoseTimerUiState.Counting
      assertEquals(DailyHealthRepository.EIGHT_HOURS_MILLIS, timer.remainingMillis)
      assertEquals("08시간 00분 00초", timer.formattedRemaining)
      assertEquals(startMillis + DailyHealthRepository.EIGHT_HOURS_MILLIS, fakeScheduler.scheduledTargetMillis)
      assertEquals("아침 약", fakeScheduler.scheduledMealLabel)

      // Advance clock by 8 hours
      fakeDateProvider.nowMillis = startMillis + DailyHealthRepository.EIGHT_HOURS_MILLIS
      viewModel.refreshDateIfNeeded()

      val completedState =
        viewModel.uiState.first {
          it is SeniorHealthUiState.Ready && it.nextDoseTimer is NextDoseTimerUiState.Completed
        } as SeniorHealthUiState.Ready

      assertTrue(completedState.nextDoseTimer is NextDoseTimerUiState.Completed)
      assertEquals(listOf("아침 약"), fakeScheduler.notificationsShown)
    }
  }
}

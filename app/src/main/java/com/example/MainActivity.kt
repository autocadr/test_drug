package com.example

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.AppDatabase
import com.example.data.repository.RoomDailyHealthRepository
import com.example.notification.AndroidDoseAlarmScheduler
import com.example.notification.NextDoseNotificationHelper
import com.example.ui.SeniorHealthScreen
import com.example.ui.SeniorHealthViewModel
import com.example.ui.theme.SeniorHealthTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    NextDoseNotificationHelper.ensureChannel(applicationContext)
    val database = AppDatabase.getInstance(applicationContext)
    val repository =
      RoomDailyHealthRepository(
        dao = database.dailyRecordDao(),
        timerDao = database.nextDoseTimerDao(),
      )
    val alarmScheduler = AndroidDoseAlarmScheduler(applicationContext)

    setContent {
      SeniorHealthTheme {
        val viewModel: SeniorHealthViewModel =
          viewModel(factory = SeniorHealthViewModel.Factory(repository, alarmScheduler))
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()

        var hasNotificationPermission by remember {
          mutableStateOf(NextDoseNotificationHelper.hasNotificationPermission(applicationContext))
        }
        var askedPermissionOnCheck by remember { mutableStateOf(false) }

        val permissionLauncher =
          rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
          ) { granted ->
            hasNotificationPermission = granted
          }

        val requestNotificationPermission = {
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
          }
        }

        val lifecycleOwner = LocalLifecycleOwner.current
        DisposableEffect(lifecycleOwner) {
          val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
              viewModel.refreshDateIfNeeded()
              hasNotificationPermission =
                NextDoseNotificationHelper.hasNotificationPermission(applicationContext)
            }
          }
          lifecycleOwner.lifecycle.addObserver(observer)
          onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
        }

        SeniorHealthScreen(
          uiState = uiState,
          hasNotificationPermission = hasNotificationPermission,
          onRequestNotificationPermission = requestNotificationPermission,
          onToggleMeal = { mealType ->
            if (!hasNotificationPermission && !askedPermissionOnCheck) {
              askedPermissionOnCheck = true
              requestNotificationPermission()
            }
            viewModel.onToggleMeal(mealType)
          },
          onDismissCompletedTimer = viewModel::onDismissCompletedTimer,
          onAddWaterCup = viewModel::onAddWaterCup,
          onRemoveWaterCup = viewModel::onRemoveWaterCup,
        )
      }
    }
  }
}

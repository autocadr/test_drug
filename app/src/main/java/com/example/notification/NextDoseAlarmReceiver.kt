package com.example.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.local.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NextDoseAlarmReceiver : BroadcastReceiver() {
  override fun onReceive(context: Context, intent: Intent) {
    val pendingResult = goAsync()
    val appContext = context.applicationContext
    val extraMealLabel = intent.getStringExtra(EXTRA_MEAL_LABEL) ?: "약"
    val extraTargetMillis = intent.getLongExtra(EXTRA_TARGET_MILLIS, 0L)

    CoroutineScope(Dispatchers.IO).launch {
      try {
        val dao = AppDatabase.getInstance(appContext).nextDoseTimerDao()
        val currentTimer = dao.getTimer()
        if (
          currentTimer != null &&
            !currentTimer.notified &&
            (extraTargetMillis == 0L || currentTimer.targetTimeMillis == extraTargetMillis)
        ) {
          NextDoseNotificationHelper.showNextDoseNotification(
            appContext,
            currentTimer.triggerMealLabel.ifBlank { extraMealLabel },
          )
          dao.markNotified()
        }
      } finally {
        pendingResult.finish()
      }
    }
  }

  companion object {
    const val EXTRA_MEAL_LABEL = "extra_meal_label"
    const val EXTRA_TARGET_MILLIS = "extra_target_millis"
  }
}

package com.example.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build

interface DoseAlarmScheduler {
  fun scheduleAlarm(targetTimeMillis: Long, mealLabel: String)

  fun cancelAlarm()

  fun showNextDoseNotification(mealLabel: String)
}

class AndroidDoseAlarmScheduler(private val context: Context) : DoseAlarmScheduler {

  override fun scheduleAlarm(targetTimeMillis: Long, mealLabel: String) {
    NextDoseNotificationHelper.ensureChannel(context)
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val pendingIntent = buildPendingIntent(mealLabel, targetTimeMillis)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
      if (alarmManager.canScheduleExactAlarms()) {
        alarmManager.setExactAndAllowWhileIdle(
          AlarmManager.RTC_WAKEUP,
          targetTimeMillis,
          pendingIntent,
        )
      } else {
        alarmManager.setAndAllowWhileIdle(
          AlarmManager.RTC_WAKEUP,
          targetTimeMillis,
          pendingIntent,
        )
      }
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      alarmManager.setExactAndAllowWhileIdle(
        AlarmManager.RTC_WAKEUP,
        targetTimeMillis,
        pendingIntent,
      )
    } else {
      alarmManager.set(AlarmManager.RTC_WAKEUP, targetTimeMillis, pendingIntent)
    }
  }

  override fun cancelAlarm() {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val pendingIntent = buildPendingIntent("", 0L)
    alarmManager.cancel(pendingIntent)
    pendingIntent.cancel()
  }

  override fun showNextDoseNotification(mealLabel: String) {
    NextDoseNotificationHelper.showNextDoseNotification(context, mealLabel)
  }

  private fun buildPendingIntent(mealLabel: String, targetTimeMillis: Long): PendingIntent {
    val intent =
      Intent(context, NextDoseAlarmReceiver::class.java).apply {
        putExtra(NextDoseAlarmReceiver.EXTRA_MEAL_LABEL, mealLabel)
        putExtra(NextDoseAlarmReceiver.EXTRA_TARGET_MILLIS, targetTimeMillis)
      }
    return PendingIntent.getBroadcast(
      context,
      ALARM_REQUEST_CODE,
      intent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )
  }

  companion object {
    private const val ALARM_REQUEST_CODE = 8002
  }
}

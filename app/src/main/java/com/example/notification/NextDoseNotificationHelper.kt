package com.example.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R

object NextDoseNotificationHelper {
  const val CHANNEL_ID = "next_medication_channel"
  private const val NOTIFICATION_ID = 8001

  fun ensureChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
      val channel =
        NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_HIGH,
          )
          .apply {
            description = context.getString(R.string.notification_channel_desc)
            enableVibration(true)
          }
      manager.createNotificationChannel(channel)
    }
  }

  fun hasNotificationPermission(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
        PackageManager.PERMISSION_GRANTED
    } else {
      true
    }
  }

  fun showNextDoseNotification(context: Context, mealLabel: String) {
    ensureChannel(context)
    if (!hasNotificationPermission(context)) return

    val openAppIntent =
      Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
      }
    val contentPendingIntent =
      PendingIntent.getActivity(
        context,
        NOTIFICATION_ID,
        openAppIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
      )

    val notification =
      NotificationCompat.Builder(context, CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_notification_pill)
        .setContentTitle(context.getString(R.string.notification_next_dose_title))
        .setContentText(context.getString(R.string.notification_next_dose_body, mealLabel))
        .setStyle(
          NotificationCompat.BigTextStyle()
            .bigText(context.getString(R.string.notification_next_dose_body, mealLabel))
        )
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setCategory(NotificationCompat.CATEGORY_REMINDER)
        .setAutoCancel(true)
        .setContentIntent(contentPendingIntent)
        .build()

    NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
  }
}

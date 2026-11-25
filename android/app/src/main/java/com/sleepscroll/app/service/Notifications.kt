package com.sleepscroll.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.sleepscroll.app.MainActivity
import com.sleepscroll.app.R
import com.sleepscroll.app.core.SleepLogic
import com.sleepscroll.app.data.Permissions

object Notifications {
    const val CHANNEL_ACTIVE = "sleep_active"
    const val CHANNEL_NIGHT = "good_night"
    const val ID_ACTIVE = 1001
    const val ID_NIGHT = 1002
    const val ACTION_STOP = "com.sleepscroll.app.STOP"

    fun createChannels(context: Context) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.createNotificationChannel(
            NotificationChannel(CHANNEL_ACTIVE, "Sleep mode", NotificationManager.IMPORTANCE_LOW).apply {
                description = "Shown while sleep mode is running"
                setShowBadge(false)
            }
        )
        nm.createNotificationChannel(
            NotificationChannel(CHANNEL_NIGHT, "Good night", NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = "A quiet note when a wind-down finishes"
            }
        )
    }

    fun active(context: Context, remainingMs: Long): Notification {
        val open = PendingIntent.getActivity(
            context, 0, Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val stop = PendingIntent.getService(
            context, 1,
            Intent(context, SleepService::class.java).setAction(ACTION_STOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        return NotificationCompat.Builder(context, CHANNEL_ACTIVE)
            .setSmallIcon(R.drawable.ic_moon)
            .setContentTitle("Sleep mode is on")
            .setContentText("${SleepLogic.formatRemaining(remainingMs)} until lights out")
            .setContentIntent(open)
            .addAction(0, "Stop", stop)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    fun showGoodNight(context: Context) {
        if (!Permissions.hasNotifications(context)) return
        val open = PendingIntent.getActivity(
            context, 2, Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val n = NotificationCompat.Builder(context, CHANNEL_NIGHT)
            .setSmallIcon(R.drawable.ic_moon)
            .setContentTitle("Good night 🌙")
            .setContentText("Wind-down complete. Put the phone down and rest.")
            .setContentIntent(open)
            .setAutoCancel(true)
            .build()
        (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).notify(ID_NIGHT, n)
    }
}

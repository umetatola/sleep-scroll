package com.sleepscroll.app.schedule

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.sleepscroll.app.core.Schedule
import com.sleepscroll.app.core.SleepLogic
import com.sleepscroll.app.data.Permissions
import com.sleepscroll.app.data.SleepRepository
import com.sleepscroll.app.service.SleepController

/** Nightly auto-start. Re-arms itself each time it fires so it survives DST and clock changes. */
object Scheduler {
    private const val REQUEST_CODE = 4210

    fun apply(context: Context, schedule: Schedule = SleepRepository.schedule.value) {
        val alarms = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pending = pendingIntent(context)
        alarms.cancel(pending)
        if (!schedule.enabled) return

        val at = SleepLogic.nextOccurrence(schedule.hour, schedule.minute, System.currentTimeMillis())
        if (Permissions.canExactAlarms(context)) {
            alarms.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pending)
        } else {
            // Exact-alarm access not granted: still fires, just within a few minutes of the target.
            alarms.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pending)
        }
    }

    private fun pendingIntent(context: Context): PendingIntent =
        PendingIntent.getBroadcast(
            context, REQUEST_CODE,
            Intent(context, ScheduleReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
}

class ScheduleReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        SleepRepository.init(context)
        val schedule = SleepRepository.schedule.value
        if (schedule.enabled && !SleepRepository.sleep.value.active) {
            SleepController.start(context, schedule.duration)
        }
        Scheduler.apply(context, schedule) // arm tomorrow night
    }
}

/** Alarms are cleared on reboot and app update; put them back and resume any running session. */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED && intent.action != Intent.ACTION_MY_PACKAGE_REPLACED) return
        SleepRepository.init(context)
        Scheduler.apply(context)
        SleepController.resumeOrFinish(context)
    }
}

/** Exact-alarm access can be granted or revoked at any time; re-arm so the schedule stays accurate. */
class AlarmPermissionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != AlarmManager.ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED) return
        SleepRepository.init(context)
        Scheduler.apply(context)
    }
}

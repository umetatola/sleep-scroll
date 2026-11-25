package com.sleepscroll.app.service

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.sleepscroll.app.core.SleepLogic
import com.sleepscroll.app.core.SleepState
import com.sleepscroll.app.data.SleepRepository

/** Start / stop / snooze / finish. The one place sessions are created and recorded. */
object SleepController {

    fun start(context: Context, durationMinutes: Int) {
        SleepRepository.init(context)
        if (SleepRepository.sleep.value.active) return
        SleepRepository.setSleep(SleepState(true, System.currentTimeMillis(), durationMinutes))
        launchService(context)
    }

    /** Re-launch the service for a session that is already active (boot, process death). */
    fun resume(context: Context) {
        SleepRepository.init(context)
        if (SleepRepository.sleep.value.active) launchService(context)
    }

    /** Stopped by the user before the end. */
    fun stop(context: Context) = end(context, completed = false)

    /** Ran to the end. */
    fun finish(context: Context) = end(context, completed = true)

    fun snooze(context: Context, minutes: Int = SleepLogic.SNOOZE_MINUTES) {
        SleepRepository.init(context)
        val s = SleepRepository.sleep.value
        if (s.active) SleepRepository.setSleep(s.copy(duration = s.duration + minutes))
    }

    private fun end(context: Context, completed: Boolean) {
        SleepRepository.init(context)
        val s = SleepRepository.sleep.value
        if (!s.active) return
        SleepRepository.addSession(
            SleepLogic.record(s.startTime, System.currentTimeMillis(), s.duration, completed)
        )
        SleepRepository.setSleep(SleepState(active = false, duration = s.duration))
        context.stopService(Intent(context, SleepService::class.java))
        if (completed) Notifications.showGoodNight(context)
    }

    private fun launchService(context: Context) {
        try {
            ContextCompat.startForegroundService(context, Intent(context, SleepService::class.java))
        } catch (_: Exception) {
            // Background-start restrictions can reject this; the state is saved, so the next app open resumes it.
        }
    }
}

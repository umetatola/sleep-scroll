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
        // From the background (the nightly schedule) Android can refuse to start a foreground
        // service: e.g. an inexact alarm on Android 12+, or, targeting 15+, holding overlay
        // permission no longer exempts us. The session is saved, so ask the user to open the app,
        // which resumes it.
        if (!launchService(context)) Notifications.showStartPending(context)
    }

    /** Re-launch the service for a session that is already active (boot, process death). */
    fun resume(context: Context) {
        SleepRepository.init(context)
        if (SleepRepository.sleep.value.active) launchService(context)
    }

    /**
     * Pick up a saved session: restart its service if time is left, or record it if it ran out
     * while nothing was running. Safe to call repeatedly (app start, boot, every app open).
     */
    fun resumeOrFinish(context: Context) {
        SleepRepository.init(context)
        val sleep = SleepRepository.sleep.value
        if (!sleep.active) return
        if (SleepLogic.remainingMs(sleep.startTime, sleep.duration, System.currentTimeMillis()) > 0) {
            resume(context)
        } else {
            finish(context)
        }
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

    /** False if Android refused the start (background-start restrictions). */
    private fun launchService(context: Context): Boolean =
        try {
            ContextCompat.startForegroundService(context, Intent(context, SleepService::class.java))
            true
        } catch (_: Exception) {
            false
        }
}

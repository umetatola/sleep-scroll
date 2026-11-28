package com.sleepscroll.app.service

import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import com.sleepscroll.app.data.Permissions
import com.sleepscroll.app.data.SleepRepository
import com.sleepscroll.app.core.SleepLogic
import com.sleepscroll.app.overlay.OverlayController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Runs for the length of a sleep session. Every couple of seconds it works out the phase, checks
 * whether a target app is in front, and tells the overlay what to show.
 */
class SleepService : Service() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var loop: Job? = null
    private lateinit var overlay: OverlayController
    private lateinit var detector: ForegroundAppDetector

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        SleepRepository.init(this)
        Notifications.createChannels(this)
        overlay = OverlayController(this)
        detector = ForegroundAppDetector(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val state = SleepRepository.sleep.value
        val remaining = SleepLogic.remainingMs(state.startTime, state.duration, System.currentTimeMillis())
        startInForeground(remaining)
        Notifications.cancelStartPending(this)

        if (intent?.action == Notifications.ACTION_STOP) {
            SleepController.stop(this)
            stopSelf()
            return START_NOT_STICKY
        }
        if (!state.active) {
            // Restarted after the process was killed, but the session already ended.
            stopSelf()
            return START_NOT_STICKY
        }
        if (loop?.isActive != true) loop = scope.launch { runLoop() }
        return START_STICKY
    }

    private fun startInForeground(remainingMs: Long) {
        val n = Notifications.active(this, remainingMs)
        if (Build.VERSION.SDK_INT >= 34) {
            startForeground(Notifications.ID_ACTIVE, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(Notifications.ID_ACTIVE, n)
        }
    }

    private suspend fun runLoop() {
        var ticks = 0
        while (true) {
            val state = SleepRepository.sleep.value
            if (!state.active) break

            val now = System.currentTimeMillis()
            val remaining = SleepLogic.remainingMs(state.startTime, state.duration, now)
            if (remaining <= 0L) {
                overlay.clear()
                SleepController.finish(this)
                break
            }

            val settings = SleepRepository.settings.value
            val phase = SleepLogic.phaseFor(state.startTime, state.duration, now)
            val foreground = detector.poll(now)
            val inTargetApp = foreground != null && foreground in settings.targetPackages

            if (inTargetApp && phase > 0 && Permissions.hasOverlay(this)) {
                overlay.render(phase, remaining, settings.windDownStyle, settings.mode)
            } else {
                overlay.clear()
            }

            // Keep the notification countdown fresh without spamming the system.
            if (ticks++ % 15 == 0) {
                (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                    .notify(Notifications.ID_ACTIVE, Notifications.active(this, remaining))
            }
            delay(TICK_MS)
        }
        stopSelf()
    }

    override fun onDestroy() {
        loop?.cancel()
        overlay.clear()
        scope.cancel()
        super.onDestroy()
    }

    private companion object {
        const val TICK_MS = 2_000L
    }
}

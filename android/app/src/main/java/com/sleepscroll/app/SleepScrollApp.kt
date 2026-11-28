package com.sleepscroll.app

import android.app.Application
import com.sleepscroll.app.data.SleepRepository
import com.sleepscroll.app.schedule.Scheduler
import com.sleepscroll.app.service.Notifications
import com.sleepscroll.app.service.SleepController

class SleepScrollApp : Application() {
    override fun onCreate() {
        super.onCreate()
        SleepRepository.init(this)
        Notifications.createChannels(this)
        Scheduler.apply(this)

        // If the process was killed mid-session, pick the session back up (or close it out if it ran out).
        SleepController.resumeOrFinish(this)
    }
}

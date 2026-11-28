package com.sleepscroll.app.service

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context

/**
 * Tracks which app is in the foreground by replaying usage events since the last poll.
 * Needs Usage Access; without it [poll] keeps returning null and no overlay is shown.
 */
class ForegroundAppDetector(context: Context) {
    private val usage = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
    private var lastQuery = 0L
    private var current: String? = null

    @Suppress("DEPRECATION") // MOVE_TO_* are the only constants available on API 26-28
    fun poll(now: Long = System.currentTimeMillis()): String? {
        // First poll looks back a couple of minutes to learn what is on screen right now. Later polls
        // overlap the previous window: an event can be stamped just before the last poll but only
        // become queryable after it, and without the overlap it was skipped forever (seen on the
        // API 36 emulator: YouTube came to the front and no overlay appeared). Replaying events in
        // order is harmless, since the last relevant event in the window decides.
        val from = if (lastQuery == 0L) now - 2 * 60_000L else lastQuery - OVERLAP_MS
        try {
            val events = usage.queryEvents(from, now)
            val e = UsageEvents.Event()
            while (events.hasNextEvent()) {
                events.getNextEvent(e)
                when (e.eventType) {
                    UsageEvents.Event.MOVE_TO_FOREGROUND -> current = e.packageName
                    UsageEvents.Event.MOVE_TO_BACKGROUND -> if (current == e.packageName) current = null
                    UsageEvents.Event.SCREEN_NON_INTERACTIVE -> current = null
                }
            }
            lastQuery = now
        } catch (_: SecurityException) {
            current = null
        }
        return current
    }

    private companion object {
        const val OVERLAP_MS = 10_000L
    }
}

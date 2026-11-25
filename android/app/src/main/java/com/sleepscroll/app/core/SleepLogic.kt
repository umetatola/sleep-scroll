package com.sleepscroll.app.core

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.math.ceil

/** Pure functions with no Android dependencies, so they can be unit tested on the JVM. */
object SleepLogic {

    /** Phase boundaries as a fraction of the session: normal, banner, dim, full wind-down. */
    val PHASE_THRESHOLDS = doubleArrayOf(0.0, 0.33, 0.67, 0.83)

    const val SNOOZE_MINUTES = 5
    const val MAX_SESSIONS = 60

    fun totalMs(durationMinutes: Int): Long = durationMinutes * 60_000L

    fun remainingMs(startTime: Long, durationMinutes: Int, now: Long): Long =
        (startTime + totalMs(durationMinutes) - now).coerceAtLeast(0L)

    fun phaseFor(startTime: Long, durationMinutes: Int, now: Long): Int {
        val total = totalMs(durationMinutes)
        if (total <= 0) return 3
        val progress = (now - startTime).toDouble() / total
        return when {
            progress >= PHASE_THRESHOLDS[3] -> 3
            progress >= PHASE_THRESHOLDS[2] -> 2
            progress >= PHASE_THRESHOLDS[1] -> 1
            else -> 0
        }
    }

    /** 0f..1f progress through the session. */
    fun progress(startTime: Long, durationMinutes: Int, now: Long): Float {
        val total = totalMs(durationMinutes)
        if (total <= 0) return 1f
        return ((now - startTime).toDouble() / total).coerceIn(0.0, 1.0).toFloat()
    }

    /** m:ss, rounding up so the display never shows 0:00 while time remains. */
    fun formatRemaining(ms: Long): String {
        val s = ceil(ms.coerceAtLeast(0L) / 1000.0).toLong()
        return "${s / 60}:${(s % 60).toString().padStart(2, '0')}"
    }

    /** Epoch ms of the next time the wall clock reads [hour]:[minute] strictly after [nowMs]. */
    fun nextOccurrence(hour: Int, minute: Int, nowMs: Long, zone: ZoneId = ZoneId.systemDefault()): Long {
        val now = ZonedDateTime.ofInstant(Instant.ofEpochMilli(nowMs), zone)
        var next = now.withHour(hour).withMinute(minute).withSecond(0).withNano(0)
        if (!next.isAfter(now)) next = next.plusDays(1)
        return next.toInstant().toEpochMilli()
    }

    fun formatClock(hour: Int, minute: Int): String {
        val h12 = if (hour % 12 == 0) 12 else hour % 12
        return "$h12:${minute.toString().padStart(2, '0')} ${if (hour < 12) "AM" else "PM"}"
    }

    fun dateOf(timeMs: Long, zone: ZoneId = ZoneId.systemDefault()): String =
        Instant.ofEpochMilli(timeMs).atZone(zone).toLocalDate().toString()

    fun record(
        startTime: Long, endTime: Long, durationMinutes: Int, completed: Boolean,
        zone: ZoneId = ZoneId.systemDefault(),
    ) = SessionRecord(dateOf(startTime, zone), startTime, endTime, durationMinutes, completed)

    fun appendCapped(sessions: List<SessionRecord>, added: SessionRecord): List<SessionRecord> =
        (sessions + added).takeLast(MAX_SESSIONS)

    // ── Analytics ────────────────────────────────────────────────────────────

    /** A day is DONE if any session that day ran to the end, PARTIAL if all were stopped early. */
    fun statusByDate(sessions: List<SessionRecord>): Map<String, DayStatus> {
        val out = HashMap<String, DayStatus>()
        for (s in sessions) {
            val current = out[s.date] ?: DayStatus.NONE
            out[s.date] = when {
                s.completed -> DayStatus.DONE
                current == DayStatus.DONE -> DayStatus.DONE
                else -> DayStatus.PARTIAL
            }
        }
        return out
    }

    /**
     * Consecutive days with a session, counting back from today. If today has no session yet the
     * streak still counts from yesterday — you haven't gone to bed yet tonight, so it isn't broken.
     */
    fun streak(sessions: List<SessionRecord>, today: LocalDate): Int {
        val days = sessions.map { it.date }.toSet()
        var day = if (today.toString() in days) today else today.minusDays(1)
        var count = 0
        while (day.toString() in days && count < 365) {
            count++
            day = day.minusDays(1)
        }
        return count
    }

    fun averageDuration(sessions: List<SessionRecord>): Int =
        if (sessions.isEmpty()) 0
        else Math.round(sessions.sumOf { it.duration }.toDouble() / sessions.size).toInt()

    /** Oldest → newest: the last seven days ending today. */
    fun lastSevenDays(sessions: List<SessionRecord>, today: LocalDate): List<Pair<LocalDate, DayStatus>> {
        val status = statusByDate(sessions)
        return (6 downTo 0).map { back ->
            val d = today.minusDays(back.toLong())
            d to (status[d.toString()] ?: DayStatus.NONE)
        }
    }
}

package com.sleepscroll.app.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime

class SleepLogicTest {
    private val min = 60_000L

    @Test fun phaseBoundariesFollowThresholds() {
        val start = 1_000_000L
        fun at(m: Int) = SleepLogic.phaseFor(start, 100, start + m * min)
        assertEquals(0, at(0))
        assertEquals(0, at(32))
        assertEquals(1, at(33))
        assertEquals(1, at(66))
        assertEquals(2, at(67))
        assertEquals(2, at(82))
        assertEquals(3, at(83))
        assertEquals(3, at(100))
        assertEquals(3, at(500))
    }

    @Test fun snoozeExtendingDurationMovesPhaseBack() {
        // 28 minutes into a 30-minute session is the final wind-down; +5 min snooze drops it to dim.
        assertEquals(3, SleepLogic.phaseFor(0, 30, 28 * min))
        assertEquals(2, SleepLogic.phaseFor(0, 35, 28 * min))
    }

    @Test fun remainingClampsAtZero() {
        assertEquals(10 * min, SleepLogic.remainingMs(0, 30, 20 * min))
        assertEquals(0L, SleepLogic.remainingMs(0, 30, 45 * min))
    }

    @Test fun formatRemainingRoundsUp() {
        assertEquals("0:00", SleepLogic.formatRemaining(0))
        assertEquals("0:01", SleepLogic.formatRemaining(1))
        assertEquals("1:00", SleepLogic.formatRemaining(59_001))
        assertEquals("1:00", SleepLogic.formatRemaining(60_000))
        assertEquals("30:00", SleepLogic.formatRemaining(30 * min))
        assertEquals("0:00", SleepLogic.formatRemaining(-5))
    }

    @Test fun progressIsClamped() {
        assertEquals(0f, SleepLogic.progress(100, 10, 50), 0f)
        assertEquals(0.5f, SleepLogic.progress(0, 10, 5 * min), 0.001f)
        assertEquals(1f, SleepLogic.progress(0, 10, 99 * min), 0f)
    }

    private fun zoned(ms: Long, zone: ZoneId) = ZonedDateTime.ofInstant(Instant.ofEpochMilli(ms), zone)

    @Test fun nextOccurrenceLaterToday() {
        val zone = ZoneId.of("America/New_York")
        val now = ZonedDateTime.of(2026, 3, 1, 20, 0, 0, 0, zone).toInstant().toEpochMilli()
        val next = zoned(SleepLogic.nextOccurrence(22, 30, now, zone), zone)
        assertEquals(LocalDate.of(2026, 3, 1), next.toLocalDate())
        assertEquals(22, next.hour)
        assertEquals(30, next.minute)
    }

    @Test fun nextOccurrenceRollsToTomorrowWhenPastOrExactlyNow() {
        val zone = ZoneId.of("America/New_York")
        val now = ZonedDateTime.of(2026, 3, 1, 22, 30, 0, 0, zone).toInstant().toEpochMilli()
        val next = zoned(SleepLogic.nextOccurrence(22, 30, now, zone), zone)
        assertEquals(LocalDate.of(2026, 3, 2), next.toLocalDate())
    }

    @Test fun nextOccurrenceSurvivesDstSpringForward() {
        val zone = ZoneId.of("America/New_York") // 2026-03-08: 2:00 jumps to 3:00
        val now = ZonedDateTime.of(2026, 3, 7, 23, 0, 0, 0, zone).toInstant().toEpochMilli()
        val next = zoned(SleepLogic.nextOccurrence(2, 30, now, zone), zone)
        assertEquals(LocalDate.of(2026, 3, 8), next.toLocalDate())
        assertEquals(3, next.hour) // 2:30 doesn't exist that night; java.time shifts it forward
    }

    @Test fun clockFormatting() {
        assertEquals("12:00 AM", SleepLogic.formatClock(0, 0))
        assertEquals("12:05 PM", SleepLogic.formatClock(12, 5))
        assertEquals("10:30 PM", SleepLogic.formatClock(22, 30))
        assertEquals("9:07 AM", SleepLogic.formatClock(9, 7))
    }

    private fun s(date: String, done: Boolean, dur: Int = 30) = SessionRecord(date, 0, 0, dur, done)

    @Test fun streakCountsBackFromToday() {
        val today = LocalDate.of(2026, 5, 10)
        val list = listOf(s("2026-05-10", true), s("2026-05-09", false), s("2026-05-08", true), s("2026-05-06", true))
        assertEquals(3, SleepLogic.streak(list, today))
    }

    @Test fun streakNotBrokenIfTonightHasntHappenedYet() {
        val today = LocalDate.of(2026, 5, 10)
        assertEquals(2, SleepLogic.streak(listOf(s("2026-05-09", true), s("2026-05-08", true)), today))
        assertEquals(0, SleepLogic.streak(listOf(s("2026-05-07", true)), today))
        assertEquals(0, SleepLogic.streak(emptyList(), today))
    }

    @Test fun dayStatusPrefersCompleted() {
        val m = SleepLogic.statusByDate(listOf(s("a", true), s("a", false), s("b", false), s("b", false)))
        assertEquals(DayStatus.DONE, m["a"])
        assertEquals(DayStatus.PARTIAL, m["b"])
        assertNull(m["c"])
    }

    @Test fun lastSevenDaysIsOldestFirstEndingToday() {
        val today = LocalDate.of(2026, 5, 10)
        val days = SleepLogic.lastSevenDays(listOf(s("2026-05-10", true), s("2026-05-04", false)), today)
        assertEquals(7, days.size)
        assertEquals(LocalDate.of(2026, 5, 4), days.first().first)
        assertEquals(DayStatus.PARTIAL, days.first().second)
        assertEquals(today, days.last().first)
        assertEquals(DayStatus.DONE, days.last().second)
        assertEquals(DayStatus.NONE, days[3].second)
    }

    @Test fun averageAndCap() {
        assertEquals(0, SleepLogic.averageDuration(emptyList()))
        assertEquals(38, SleepLogic.averageDuration(listOf(s("a", true, 15), s("b", true, 60)))) // 37.5 rounds up
        var list = emptyList<SessionRecord>()
        repeat(75) { list = SleepLogic.appendCapped(list, SessionRecord("d$it", it.toLong(), 0, 30, true)) }
        assertEquals(SleepLogic.MAX_SESSIONS, list.size)
        assertEquals(15L, list.first().startTime)
        assertEquals(74L, list.last().startTime)
    }

    @Test fun journalPromptIndexesSundayAsZero() {
        assertEquals(WindDownContent.JOURNAL_PROMPTS[0], WindDownContent.journalPrompt(7)) // Sunday
        assertEquals(WindDownContent.JOURNAL_PROMPTS[1], WindDownContent.journalPrompt(1)) // Monday
    }
}

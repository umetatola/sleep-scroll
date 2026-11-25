import XCTest
@testable import SleepScrollCore

final class SleepLogicTests: XCTestCase {

    private let min: TimeInterval = 60
    private let start = Date(timeIntervalSince1970: 1_000_000)

    private var utc: Calendar {
        var c = Calendar(identifier: .gregorian)
        c.timeZone = TimeZone(identifier: "UTC")!
        return c
    }

    private func date(_ y: Int, _ m: Int, _ d: Int, _ h: Int = 12, _ mi: Int = 0, cal: Calendar? = nil) -> Date {
        let c = cal ?? utc
        return c.date(from: DateComponents(year: y, month: m, day: d, hour: h, minute: mi))!
    }

    // MARK: Phases

    func testPhaseBoundariesFollowThresholds() {
        func at(_ m: Double) -> Int { SleepLogic.phase(start: start, duration: 100, now: start + m * min) }
        XCTAssertEqual(at(0), 0)
        XCTAssertEqual(at(32), 0)
        XCTAssertEqual(at(33), 1)
        XCTAssertEqual(at(66), 1)
        XCTAssertEqual(at(67), 2)
        XCTAssertEqual(at(82), 2)
        XCTAssertEqual(at(83), 3)
        XCTAssertEqual(at(100), 3)
        XCTAssertEqual(at(500), 3)
    }

    func testSnoozeExtendingDurationMovesPhaseBack() {
        let state = SleepState(active: true, startTime: start, duration: 30)
        let now = start + 28 * min
        XCTAssertEqual(SleepLogic.phase(start: start, duration: state.duration, now: now), 3)
        let snoozed = SleepLogic.snoozed(state)
        XCTAssertEqual(snoozed.duration, 35)
        XCTAssertEqual(SleepLogic.phase(start: start, duration: snoozed.duration, now: now), 2)
    }

    func testPhaseStartsMatchThresholds() {
        XCTAssertEqual(SleepLogic.phaseStart(1, start: start, duration: 100), start + 33 * min)
        XCTAssertEqual(SleepLogic.phaseStart(2, start: start, duration: 100), start + 67 * min)
        XCTAssertEqual(SleepLogic.phaseStart(3, start: start, duration: 100), start + 83 * min)
        XCTAssertEqual(SleepLogic.end(start: start, duration: 100), start + 100 * min)
    }

    func testRemainingClampsAtZero() {
        XCTAssertEqual(SleepLogic.remaining(start: start, duration: 30, now: start + 20 * min), 10 * min)
        XCTAssertEqual(SleepLogic.remaining(start: start, duration: 30, now: start + 45 * min), 0)
    }

    func testProgressIsClamped() {
        XCTAssertEqual(SleepLogic.progress(start: start, duration: 10, now: start - 50), 0)
        XCTAssertEqual(SleepLogic.progress(start: start, duration: 10, now: start + 5 * min), 0.5, accuracy: 0.001)
        XCTAssertEqual(SleepLogic.progress(start: start, duration: 10, now: start + 99 * min), 1)
    }

    // MARK: Formatting

    func testFormatRemainingRoundsUp() {
        XCTAssertEqual(SleepLogic.formatRemaining(0), "0:00")
        XCTAssertEqual(SleepLogic.formatRemaining(0.001), "0:01")
        XCTAssertEqual(SleepLogic.formatRemaining(59.001), "1:00")
        XCTAssertEqual(SleepLogic.formatRemaining(60), "1:00")
        XCTAssertEqual(SleepLogic.formatRemaining(30 * min), "30:00")
        XCTAssertEqual(SleepLogic.formatRemaining(-5), "0:00")
    }

    func testMinutesLeftRoundsUp() {
        XCTAssertEqual(SleepLogic.minutesLeft(0), 0)
        XCTAssertEqual(SleepLogic.minutesLeft(1), 1)
        XCTAssertEqual(SleepLogic.minutesLeft(150), 3)
    }

    func testClockFormatting() {
        XCTAssertEqual(SleepLogic.formatClock(hour: 0, minute: 0), "12:00 AM")
        XCTAssertEqual(SleepLogic.formatClock(hour: 12, minute: 5), "12:05 PM")
        XCTAssertEqual(SleepLogic.formatClock(hour: 22, minute: 30), "10:30 PM")
        XCTAssertEqual(SleepLogic.formatClock(hour: 9, minute: 7), "9:07 AM")
    }

    // MARK: Schedule

    func testNextOccurrenceLaterToday() {
        let next = SleepLogic.nextOccurrence(hour: 22, minute: 30, after: date(2026, 3, 1, 20, 0), calendar: utc)
        XCTAssertEqual(next, date(2026, 3, 1, 22, 30))
    }

    func testNextOccurrenceRollsToTomorrowWhenPastOrExactlyNow() {
        XCTAssertEqual(SleepLogic.nextOccurrence(hour: 22, minute: 30, after: date(2026, 3, 1, 23, 0), calendar: utc),
                       date(2026, 3, 2, 22, 30))
        XCTAssertEqual(SleepLogic.nextOccurrence(hour: 22, minute: 30, after: date(2026, 3, 1, 22, 30), calendar: utc),
                       date(2026, 3, 2, 22, 30))
    }

    func testNextOccurrenceSurvivesDstSpringForward() {
        var ny = Calendar(identifier: .gregorian)
        ny.timeZone = TimeZone(identifier: "America/New_York")!
        // 2:30 AM doesn't exist on 2026-03-08 in New York; whatever Foundation picks must still be that day and later than now.
        let now = date(2026, 3, 7, 23, 0, cal: ny)
        let next = SleepLogic.nextOccurrence(hour: 2, minute: 30, after: now, calendar: ny)
        XCTAssertGreaterThan(next, now)
        XCTAssertEqual(SleepLogic.dateKey(next, calendar: ny), "2026-03-08")
    }

    // MARK: Sessions and analytics

    private func s(_ date: String, _ done: Bool, _ dur: Int = 30) -> SessionRecord {
        SessionRecord(date: date, startTime: start, endTime: start, duration: dur, completed: done)
    }

    func testDateKeyUsesCalendarTimeZone() {
        XCTAssertEqual(SleepLogic.dateKey(date(2026, 5, 10, 23, 30), calendar: utc), "2026-05-10")
        var seoul = Calendar(identifier: .gregorian)
        seoul.timeZone = TimeZone(identifier: "Asia/Seoul")!
        XCTAssertEqual(SleepLogic.dateKey(date(2026, 5, 10, 23, 30), calendar: seoul), "2026-05-11")
    }

    func testAppendCappedKeepsNewest() {
        var list: [SessionRecord] = []
        for i in 0..<70 { list = SleepLogic.appendCapped(list, s("d\(i)", true)) }
        XCTAssertEqual(list.count, SleepLogic.maxSessions)
        XCTAssertEqual(list.first?.date, "d10")
        XCTAssertEqual(list.last?.date, "d69")
    }

    func testStreakCountsBackFromToday() {
        let list = [s("2026-05-10", true), s("2026-05-09", false), s("2026-05-08", true), s("2026-05-06", true)]
        XCTAssertEqual(SleepLogic.streak(list, today: date(2026, 5, 10), calendar: utc), 3)
    }

    func testStreakNotBrokenIfTonightHasntHappenedYet() {
        let today = date(2026, 5, 10)
        XCTAssertEqual(SleepLogic.streak([s("2026-05-09", true), s("2026-05-08", true)], today: today, calendar: utc), 2)
        XCTAssertEqual(SleepLogic.streak([s("2026-05-07", true)], today: today, calendar: utc), 0)
        XCTAssertEqual(SleepLogic.streak([], today: today, calendar: utc), 0)
    }

    func testDayStatusPrefersCompleted() {
        let m = SleepLogic.statusByDate([s("a", false), s("a", true), s("a", false), s("b", false)])
        XCTAssertEqual(m["a"], .done)
        XCTAssertEqual(m["b"], .partial)
        XCTAssertNil(m["c"])
    }

    func testAverageDuration() {
        XCTAssertEqual(SleepLogic.averageDuration([]), 0)
        XCTAssertEqual(SleepLogic.averageDuration([s("a", true, 30), s("b", true, 45)]), 38)
    }

    func testLastSevenDaysIsOldestFirstEndingToday() {
        let days = SleepLogic.lastSevenDays([s("2026-05-04", false), s("2026-05-10", true)],
                                            today: date(2026, 5, 10), calendar: utc)
        XCTAssertEqual(days.count, 7)
        XCTAssertEqual(SleepLogic.dateKey(days.first!.date, calendar: utc), "2026-05-04")
        XCTAssertEqual(days.first!.status, .partial)
        XCTAssertEqual(SleepLogic.dateKey(days.last!.date, calendar: utc), "2026-05-10")
        XCTAssertEqual(days.last!.status, .done)
        XCTAssertEqual(days[3].status, .none)
    }

    // MARK: Wind-down and shield text

    func testJournalPromptIndexedFromSunday() {
        XCTAssertEqual(WindDownContent.journalPrompt(weekday: 1), WindDownContent.journalPrompts[0])
        XCTAssertEqual(WindDownContent.journalPrompt(weekday: 7), WindDownContent.journalPrompts[6])
    }

    func testSnoozeOnlyInSoftPhaseAndNormalMode() {
        XCTAssertTrue(ShieldText.allowsSnooze(phase: 2, mode: .normal))
        XCTAssertFalse(ShieldText.allowsSnooze(phase: 3, mode: .normal))
        XCTAssertFalse(ShieldText.allowsSnooze(phase: 2, mode: .strict))
    }

    func testShieldTextPerPhase() {
        XCTAssertEqual(ShieldText.title(phase: 2), "Time to wind down")
        XCTAssertEqual(ShieldText.title(phase: 3), "Time to sleep")
        XCTAssertTrue(ShieldText.subtitle(phase: 2, remaining: 150, style: .breathing, weekday: 1).hasPrefix("About 3 min left."))
        XCTAssertTrue(ShieldText.subtitle(phase: 3, remaining: 30, style: .silence, weekday: 1).hasPrefix("Rest now"))
    }

    func testStateRoundTripsThroughJSON() throws {
        let state = SleepState(active: true, startTime: start, duration: 45)
        let back = try JSONDecoder().decode(SleepState.self, from: JSONEncoder().encode(state))
        XCTAssertEqual(back, state)
    }
}

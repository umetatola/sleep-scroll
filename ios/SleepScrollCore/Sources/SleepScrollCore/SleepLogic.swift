import Foundation

/// Pure functions with no UI or Screen Time dependencies. Mirrors `SleepLogic.kt` on Android.
public enum SleepLogic {

    /// Phase boundaries as a fraction of the session: normal, reminder, soft shield, full wind-down.
    public static let phaseThresholds: [Double] = [0.0, 0.33, 0.67, 0.83]

    public static let snoozeMinutes = 5
    public static let maxSessions = 60
    public static let durations = [15, 30, 45, 60]

    public static func total(_ minutes: Int) -> TimeInterval { TimeInterval(minutes * 60) }

    public static func end(start: Date, duration: Int) -> Date {
        start.addingTimeInterval(total(duration))
    }

    public static func remaining(start: Date, duration: Int, now: Date) -> TimeInterval {
        max(0, end(start: start, duration: duration).timeIntervalSince(now))
    }

    public static func phase(start: Date, duration: Int, now: Date) -> Int {
        let t = total(duration)
        guard t > 0 else { return 3 }
        let p = now.timeIntervalSince(start) / t
        if p >= phaseThresholds[3] { return 3 }
        if p >= phaseThresholds[2] { return 2 }
        if p >= phaseThresholds[1] { return 1 }
        return 0
    }

    /// 0...1 progress through the session.
    public static func progress(start: Date, duration: Int, now: Date) -> Double {
        let t = total(duration)
        guard t > 0 else { return 1 }
        return min(1, max(0, now.timeIntervalSince(start) / t))
    }

    /// When phase 1, 2 or 3 begins.
    public static func phaseStart(_ phase: Int, start: Date, duration: Int) -> Date {
        let i = min(max(phase, 0), phaseThresholds.count - 1)
        return start.addingTimeInterval(total(duration) * phaseThresholds[i])
    }

    /// m:ss, rounding up so the display never shows 0:00 while time remains.
    public static func formatRemaining(_ seconds: TimeInterval) -> String {
        let s = Int(ceil(max(0, seconds)))
        return "\(s / 60):" + String(format: "%02d", s % 60)
    }

    /// Whole minutes left, rounded up ("about 3 min").
    public static func minutesLeft(_ seconds: TimeInterval) -> Int {
        Int(ceil(max(0, seconds) / 60))
    }

    /// The next time the wall clock reads hour:minute strictly after `now`.
    public static func nextOccurrence(hour: Int, minute: Int, after now: Date, calendar: Calendar = .current) -> Date {
        var c = calendar.dateComponents([.year, .month, .day], from: now)
        c.hour = hour
        c.minute = minute
        c.second = 0
        var next = calendar.date(from: c) ?? now
        if next <= now { next = calendar.date(byAdding: .day, value: 1, to: next) ?? next }
        return next
    }

    public static func formatClock(hour: Int, minute: Int) -> String {
        let h12 = hour % 12 == 0 ? 12 : hour % 12
        return "\(h12):" + String(format: "%02d", minute) + (hour < 12 ? " AM" : " PM")
    }

    public static func dateKey(_ date: Date, calendar: Calendar = .current) -> String {
        let c = calendar.dateComponents([.year, .month, .day], from: date)
        return String(format: "%04d-%02d-%02d", c.year ?? 0, c.month ?? 0, c.day ?? 0)
    }

    public static func record(start: Date, end: Date, duration: Int, completed: Bool,
                              calendar: Calendar = .current) -> SessionRecord {
        SessionRecord(date: dateKey(start, calendar: calendar), startTime: start, endTime: end,
                      duration: duration, completed: completed)
    }

    public static func appendCapped(_ sessions: [SessionRecord], _ added: SessionRecord) -> [SessionRecord] {
        Array((sessions + [added]).suffix(maxSessions))
    }

    /// Snoozing adds time to the session, so the phase can drop back.
    public static func snoozed(_ state: SleepState) -> SleepState {
        var s = state
        s.duration += snoozeMinutes
        return s
    }

    // MARK: Analytics

    /// A day is done if any session that day ran to the end, partial if all were stopped early.
    public static func statusByDate(_ sessions: [SessionRecord]) -> [String: DayStatus] {
        var out: [String: DayStatus] = [:]
        for s in sessions {
            let current = out[s.date] ?? DayStatus.none
            if s.completed || current == .done {
                out[s.date] = .done
            } else {
                out[s.date] = .partial
            }
        }
        return out
    }

    /// Consecutive days with a session, counting back from today. If today has no session yet the
    /// streak still counts from yesterday — you haven't gone to bed yet tonight, so it isn't broken.
    public static func streak(_ sessions: [SessionRecord], today: Date, calendar: Calendar = .current) -> Int {
        let days = Set(sessions.map(\.date))
        var day = days.contains(dateKey(today, calendar: calendar))
            ? today
            : (calendar.date(byAdding: .day, value: -1, to: today) ?? today)
        var count = 0
        while days.contains(dateKey(day, calendar: calendar)) && count < 365 {
            count += 1
            guard let prev = calendar.date(byAdding: .day, value: -1, to: day) else { break }
            day = prev
        }
        return count
    }

    public static func averageDuration(_ sessions: [SessionRecord]) -> Int {
        guard !sessions.isEmpty else { return 0 }
        let sum = sessions.reduce(0) { $0 + $1.duration }
        return Int((Double(sum) / Double(sessions.count)).rounded())
    }

    /// Oldest → newest: the last seven days ending today.
    public static func lastSevenDays(_ sessions: [SessionRecord], today: Date,
                                     calendar: Calendar = .current) -> [DayEntry] {
        let status = statusByDate(sessions)
        return (0...6).reversed().map { back in
            let d = calendar.date(byAdding: .day, value: -back, to: today) ?? today
            return DayEntry(date: d, status: status[dateKey(d, calendar: calendar)] ?? DayStatus.none)
        }
    }
}

import Foundation
import DeviceActivity
import ManagedSettings
import UserNotifications
import SleepScrollCore

extension DeviceActivityName {
    static let phase1 = Self("sleepscroll.phase1")
    static let phase2 = Self("sleepscroll.phase2")
    static let phase3 = Self("sleepscroll.phase3")
    static let sessionEnd = Self("sleepscroll.end")
    static let nightly = Self("sleepscroll.nightly")

    static let sessionActivities: [DeviceActivityName] = [.phase1, .phase2, .phase3, .sessionEnd]
}

extension ManagedSettingsStore.Name {
    static let sleepScroll = Self("sleepscroll")
}

/// Starts, advances, snoozes and finishes a sleep-mode session. Used by the app and by the
/// DeviceActivity monitor / shield-action extensions, so every path goes through the same rules.
///
/// iOS can't draw over other apps, so the phases map to what Screen Time allows:
///   phase 1 → a "starting soon" notification
///   phase 2 → a shield on the chosen apps with a Snooze button (normal mode)
///   phase 3 → the same shield without Snooze, plus a notification that opens the full wind-down
enum SleepEngine {

    private static var store: SharedStore { .shared }
    private static let managed = ManagedSettingsStore(named: .sleepScroll)
    private static let center = DeviceActivityCenter()

    /// DeviceActivity refuses intervals shorter than this, so every one-shot schedule is padded to it.
    private static let minimumInterval: TimeInterval = 15 * 60

    // MARK: Session lifecycle

    static func start(duration: Int, now: Date = Date()) {
        guard !store.sleep.active else { return }
        store.sleep = SleepState(active: true, startTime: now, duration: duration)
        store.shieldPhase = 0
        scheduleSession(now: now)
        apply(now: now)
    }

    static func stop(now: Date = Date()) {
        finish(completed: false, now: now)
    }

    static func snooze(now: Date = Date()) {
        let state = store.sleep
        guard state.active else { return }
        store.sleep = SleepLogic.snoozed(state)
        scheduleSession(now: now)
        apply(now: now)
    }

    /// Bring shields in line with the clock. `atLeast` covers a DeviceActivity callback that fires a
    /// moment before its phase boundary.
    static func apply(now: Date = Date(), atLeast minimumPhase: Int = 0) {
        let state = store.sleep
        guard state.active else { clearShield(); return }
        if now >= state.endTime {
            finish(completed: true, now: now)
            return
        }
        let phase = max(SleepLogic.phase(start: state.startTime, duration: state.duration, now: now), minimumPhase)
        if phase >= 2 {
            store.shieldPhase = phase
            shield()
        } else {
            store.shieldPhase = phase
            clearShield()
        }
    }

    static func finish(completed: Bool, now: Date = Date()) {
        let state = store.sleep
        guard state.active else { return }
        store.sessions = SleepLogic.appendCapped(
            store.sessions,
            SleepLogic.record(start: state.startTime, end: now, duration: state.duration, completed: completed)
        )
        store.sleep = SleepState(active: false, duration: state.duration)
        store.shieldPhase = 0
        center.stopMonitoring(DeviceActivityName.sessionActivities)
        clearShield()
        Notifications.cancelSession()
        if completed { Notifications.goodNight() }
    }

    // MARK: Scheduling

    /// One DeviceActivity per remaining phase boundary. Only `intervalDidStart` matters; each interval
    /// is padded to the 15-minute minimum and its end is ignored.
    private static func scheduleSession(now: Date) {
        let state = store.sleep
        center.stopMonitoring(DeviceActivityName.sessionActivities)
        Notifications.cancelSession()

        let boundaries: [(DeviceActivityName, Date)] = [
            (.phase1, SleepLogic.phaseStart(1, start: state.startTime, duration: state.duration)),
            (.phase2, SleepLogic.phaseStart(2, start: state.startTime, duration: state.duration)),
            (.phase3, SleepLogic.phaseStart(3, start: state.startTime, duration: state.duration)),
            (.sessionEnd, state.endTime),
        ]
        for (name, at) in boundaries where at > now {
            try? center.startMonitoring(name, during: oneShot(at))
        }
        Notifications.scheduleSession(state, now: now)
    }

    private static func oneShot(_ date: Date) -> DeviceActivitySchedule {
        let cal = Calendar.current
        let parts: Set<Calendar.Component> = [.year, .month, .day, .hour, .minute, .second]
        return DeviceActivitySchedule(
            intervalStart: cal.dateComponents(parts, from: date),
            intervalEnd: cal.dateComponents(parts, from: date.addingTimeInterval(minimumInterval)),
            repeats: false
        )
    }

    /// Daily activity at bedtime; the monitor extension starts the session when it begins.
    static func updateNightly(_ schedule: Schedule) {
        center.stopMonitoring([.nightly])
        guard schedule.enabled else { return }
        var endHour = schedule.hour
        var endMinute = schedule.minute + max(schedule.duration, 15)
        endHour = (endHour + endMinute / 60) % 24
        endMinute %= 60
        try? center.startMonitoring(.nightly, during: DeviceActivitySchedule(
            intervalStart: DateComponents(hour: schedule.hour, minute: schedule.minute),
            intervalEnd: DateComponents(hour: endHour, minute: endMinute),
            repeats: true
        ))
    }

    // MARK: Shields

    private static func shield() {
        let selection = store.selection
        managed.shield.applications = selection.applicationTokens.isEmpty ? nil : selection.applicationTokens
        managed.shield.applicationCategories = selection.categoryTokens.isEmpty ? nil : .specific(selection.categoryTokens)
        managed.shield.webDomains = selection.webDomainTokens.isEmpty ? nil : selection.webDomainTokens
    }

    private static func clearShield() {
        managed.clearAllSettings()
    }
}

/// Local notifications stand in for the Android banner and the "Good night" message.
enum Notifications {
    private static let center = UNUserNotificationCenter.current()
    private static let sessionIds = ["sleepscroll.soon", "sleepscroll.winddown"]

    static func requestPermission() async -> Bool {
        (try? await center.requestAuthorization(options: [.alert, .sound])) ?? false
    }

    static func scheduleSession(_ state: SleepState, now: Date) {
        let soon = SleepLogic.phaseStart(1, start: state.startTime, duration: state.duration)
        let windDown = SleepLogic.phaseStart(3, start: state.startTime, duration: state.duration)
        let minsToShield = SleepLogic.minutesLeft(
            SleepLogic.phaseStart(2, start: state.startTime, duration: state.duration).timeIntervalSince(soon))

        add(id: "sleepscroll.soon", at: soon, now: now,
            title: "Sleep mode starting soon 🌙",
            body: "Your social apps start winding down in about \(minsToShield) min.")
        add(id: "sleepscroll.winddown", at: windDown, now: now,
            title: "Time to sleep",
            body: "Tap for your wind-down.")
    }

    static func cancelSession() {
        center.removePendingNotificationRequests(withIdentifiers: sessionIds)
        center.removeDeliveredNotifications(withIdentifiers: sessionIds)
    }

    static func goodNight() {
        let content = UNMutableNotificationContent()
        content.title = "Good night 🌙"
        content.body = "Sleep mode finished. Rest well."
        center.add(UNNotificationRequest(identifier: "sleepscroll.goodnight", content: content, trigger: nil))
    }

    private static func add(id: String, at date: Date, now: Date, title: String, body: String) {
        let delay = date.timeIntervalSince(now)
        guard delay > 1 else { return }
        let content = UNMutableNotificationContent()
        content.title = title
        content.body = body
        let trigger = UNTimeIntervalNotificationTrigger(timeInterval: delay, repeats: false)
        center.add(UNNotificationRequest(identifier: id, content: content, trigger: trigger))
    }
}

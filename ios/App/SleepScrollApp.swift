import SwiftUI
import Combine
import FamilyControls
import UserNotifications
import SleepScrollCore

@main
struct SleepScrollApp: App {
    @StateObject private var model = AppModel()

    var body: some Scene {
        WindowGroup {
            RootView()
                .environmentObject(model)
                .preferredColorScheme(.dark)
                .onReceive(NotificationCenter.default.publisher(for: UIApplication.didBecomeActiveNotification)) { _ in
                    model.reload()
                }
        }
    }
}

/// UI state mirrored from `SharedStore`. Every change goes through `SleepEngine`, the same code the
/// Screen Time extensions run, then reloads.
@MainActor
final class AppModel: ObservableObject {
    @Published private(set) var sleep = SleepState()
    @Published private(set) var schedule = Schedule()
    @Published private(set) var settings = Settings()
    @Published private(set) var sessions: [SessionRecord] = []
    @Published private(set) var selection = FamilyActivitySelection()
    @Published private(set) var screenTimeApproved = false
    @Published private(set) var notificationsAllowed = false
    @Published private(set) var now = Date()
    @Published var chosenDuration = 30
    @Published var showWindDown = false

    private let store = SharedStore.shared
    private var ticker: AnyCancellable?
    private var lastPhase = -1

    init() {
        reload()
        ticker = Timer.publish(every: 1, on: .main, in: .common).autoconnect().sink { [weak self] date in
            self?.tick(date)
        }
    }

    var hasSelection: Bool { store.hasSelection }
    var setupComplete: Bool { screenTimeApproved && hasSelection && notificationsAllowed }

    var phase: Int { SleepLogic.phase(start: sleep.startTime, duration: sleep.duration, now: now) }
    var remaining: TimeInterval { SleepLogic.remaining(start: sleep.startTime, duration: sleep.duration, now: now) }
    var progress: Double { SleepLogic.progress(start: sleep.startTime, duration: sleep.duration, now: now) }

    func reload() {
        SleepEngine.apply()
        sleep = store.sleep
        schedule = store.schedule
        settings = store.settings
        sessions = store.sessions
        selection = store.selection
        now = Date()
        if !sleep.active { chosenDuration = SleepLogic.durations.contains(sleep.duration) ? sleep.duration : 30 }
        screenTimeApproved = AuthorizationCenter.shared.authorizationStatus == .approved
        if sleep.active && phase >= 3 { showWindDown = true }
        lastPhase = sleep.active ? phase : -1
        Task {
            let status = await UNUserNotificationCenter.current().notificationSettings().authorizationStatus
            notificationsAllowed = status == .authorized || status == .provisional
        }
    }

    private func tick(_ date: Date) {
        guard sleep.active else { return }
        now = date
        // The monitor extension does the real work in the background; this keeps the open app in step.
        if remaining <= 0 || phase != lastPhase { reload() }
    }

    // MARK: Actions

    func requestScreenTime() async {
        try? await AuthorizationCenter.shared.requestAuthorization(for: .individual)
        reload()
    }

    func requestNotifications() async {
        _ = await Notifications.requestPermission()
        reload()
    }

    func start() {
        SleepEngine.start(duration: chosenDuration)
        reload()
    }

    func stop() {
        SleepEngine.stop()
        showWindDown = false
        reload()
    }

    func snooze() {
        SleepEngine.snooze()
        showWindDown = false
        reload()
    }

    func setSelection(_ value: FamilyActivitySelection) {
        store.selection = value
        if store.sleep.active { SleepEngine.apply() }
        reload()
    }

    func setSchedule(_ value: Schedule) {
        store.schedule = value
        SleepEngine.updateNightly(value)
        reload()
    }

    func setSettings(_ value: Settings) {
        store.settings = value
        reload()
    }
}

import DeviceActivity
import Foundation
import SleepScrollCore

/// Woken by iOS at each phase boundary the app scheduled, and at bedtime for the nightly schedule.
/// Runs in its own short-lived process, so all state comes from the App Group.
final class Monitor: DeviceActivityMonitor {

    override func intervalDidStart(for activity: DeviceActivityName) {
        super.intervalDidStart(for: activity)
        switch activity {
        case .nightly:
            let schedule = SharedStore.shared.schedule
            guard schedule.enabled else { return }
            SleepEngine.start(duration: schedule.duration)
        case .phase1:
            SleepEngine.apply(atLeast: 1)
        case .phase2:
            SleepEngine.apply(atLeast: 2)
        case .phase3:
            SleepEngine.apply(atLeast: 3)
        case .sessionEnd:
            SleepEngine.finish(completed: true)
        default:
            SleepEngine.apply()
        }
    }
}

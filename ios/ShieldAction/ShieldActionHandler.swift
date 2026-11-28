import Foundation
import ManagedSettings
import SleepScrollCore

/// Handles the shield's buttons: "Close app" closes it, "Snooze" lifts the shield for 5 more minutes.
final class ShieldActionHandler: ShieldActionDelegate {

    override func handle(action: ShieldAction, for application: ApplicationToken,
                         completionHandler: @escaping (ShieldActionResponse) -> Void) {
        completionHandler(respond(to: action))
    }

    override func handle(action: ShieldAction, for webDomain: WebDomainToken,
                         completionHandler: @escaping (ShieldActionResponse) -> Void) {
        completionHandler(respond(to: action))
    }

    override func handle(action: ShieldAction, for category: ActivityCategoryToken,
                         completionHandler: @escaping (ShieldActionResponse) -> Void) {
        completionHandler(respond(to: action))
    }

    private func respond(to action: ShieldAction) -> ShieldActionResponse {
        switch action {
        case .primaryButtonPressed:
            return .close
        case .secondaryButtonPressed:
            let store = SharedStore.shared
            let state = store.sleep
            let phase = SleepLogic.phase(start: state.startTime, duration: state.duration, now: Date())
            guard ShieldText.allowsSnooze(phase: phase, mode: store.settings.mode) else { return .none }
            SleepEngine.snooze()
            // .defer re-evaluates the shield; it was just removed, so the app opens.
            return .defer
        @unknown default:
            return .close
        }
    }
}

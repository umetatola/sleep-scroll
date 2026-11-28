import Foundation
import ManagedSettings
import ManagedSettingsUI
import UIKit
import SleepScrollCore

/// What the shield looks like when a chosen app is opened during phases 2–3.
final class ShieldConfig: ShieldConfigurationDataSource {

    override func configuration(shielding application: Application) -> ShieldConfiguration { make() }

    override func configuration(shielding application: Application, in category: ActivityCategory) -> ShieldConfiguration { make() }

    override func configuration(shielding webDomain: WebDomain) -> ShieldConfiguration { make() }

    override func configuration(shielding webDomain: WebDomain, in category: ActivityCategory) -> ShieldConfiguration { make() }

    private func make() -> ShieldConfiguration {
        let store = SharedStore.shared
        let state = store.sleep
        let now = Date()
        let phase = max(store.shieldPhase, SleepLogic.phase(start: state.startTime, duration: state.duration, now: now))
        let remaining = SleepLogic.remaining(start: state.startTime, duration: state.duration, now: now)
        let weekday = Calendar.current.component(.weekday, from: now)
        let settings = store.settings

        let night = UIColor(red: 0.04, green: 0.04, blue: 0.12, alpha: 1)
        let ink = UIColor(red: 0.88, green: 0.91, blue: 1.0, alpha: 1)
        let accent = UIColor(red: 0.78, green: 0.82, blue: 1.0, alpha: 1)
        let deep = UIColor(red: 0.12, green: 0.11, blue: 0.29, alpha: 1)

        return ShieldConfiguration(
            backgroundBlurStyle: .systemUltraThinMaterialDark,
            backgroundColor: night,
            icon: UIImage(systemName: "moon.stars.fill")?.withTintColor(.systemYellow, renderingMode: .alwaysOriginal),
            title: .init(text: ShieldText.title(phase: phase), color: .white),
            subtitle: .init(text: ShieldText.subtitle(phase: phase, remaining: remaining,
                                                      style: settings.windDownStyle, weekday: weekday), color: ink),
            primaryButtonLabel: .init(text: "Close app", color: deep),
            primaryButtonBackgroundColor: accent,
            secondaryButtonLabel: ShieldText.allowsSnooze(phase: phase, mode: settings.mode)
                ? .init(text: "Snooze \(SleepLogic.snoozeMinutes) min", color: ink)
                : nil
        )
    }
}

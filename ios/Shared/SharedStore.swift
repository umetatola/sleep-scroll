import Foundation
import FamilyControls
import SleepScrollCore

enum AppGroup {
    static let id = "group.com.sleepscroll.ios"
}

/// The single source of truth, kept in the App Group so the app and the Screen Time extensions
/// (which run in their own processes) read and write the same state.
final class SharedStore {
    static let shared = SharedStore()

    private let defaults: UserDefaults
    private let encoder = JSONEncoder()
    private let decoder = JSONDecoder()

    init(defaults: UserDefaults? = UserDefaults(suiteName: AppGroup.id)) {
        self.defaults = defaults ?? .standard
    }

    private func load<T: Decodable>(_ key: String, default fallback: T) -> T {
        guard let data = defaults.data(forKey: key),
              let value = try? decoder.decode(T.self, from: data) else { return fallback }
        return value
    }

    private func save<T: Encodable>(_ value: T, _ key: String) {
        if let data = try? encoder.encode(value) { defaults.set(data, forKey: key) }
    }

    var sleep: SleepState {
        get { load("sleep", default: SleepState()) }
        set { save(newValue, "sleep") }
    }

    var schedule: Schedule {
        get { load("schedule", default: Schedule()) }
        set { save(newValue, "schedule") }
    }

    var settings: Settings {
        get { load("settings", default: Settings()) }
        set { save(newValue, "settings") }
    }

    var sessions: [SessionRecord] {
        get { load("sessions", default: []) }
        set { save(newValue, "sessions") }
    }

    /// The apps, categories and sites the user picked. Opaque tokens — iOS never tells us which apps they are.
    var selection: FamilyActivitySelection {
        get { load("selection", default: FamilyActivitySelection()) }
        set { save(newValue, "selection") }
    }

    var hasSelection: Bool {
        let s = selection
        return !(s.applicationTokens.isEmpty && s.categoryTokens.isEmpty && s.webDomainTokens.isEmpty)
    }

    /// The last shield phase applied, so the shield extension knows which text to show.
    var shieldPhase: Int {
        get { defaults.integer(forKey: "shieldPhase") }
        set { defaults.set(newValue, forKey: "shieldPhase") }
    }
}

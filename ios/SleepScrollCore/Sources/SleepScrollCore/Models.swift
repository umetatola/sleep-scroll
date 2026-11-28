import Foundation

public enum WindDownStyle: String, Codable, CaseIterable, Identifiable, Sendable {
    case breathing, reflection, journaling, sounds, silence

    public var id: String { rawValue }

    public var label: String {
        switch self {
        case .breathing: return "Breathing"
        case .reflection: return "Reflection"
        case .journaling: return "Journaling"
        case .sounds: return "Sounds"
        case .silence: return "Silence"
        }
    }

    public var blurb: String {
        switch self {
        case .breathing: return "A slow guided breathing ring"
        case .reflection: return "Gentle gratitude prompts that rotate"
        case .journaling: return "One question for tonight"
        case .sounds: return "A nudge to put on calming audio"
        case .silence: return "A dark screen, nothing else"
        }
    }

    public var emoji: String {
        switch self {
        case .breathing: return "🌙"
        case .reflection: return "🙏"
        case .journaling: return "📓"
        case .sounds: return "🎵"
        case .silence: return "🌑"
        }
    }
}

public enum IntensityMode: String, Codable, CaseIterable, Identifiable, Sendable {
    case normal, strict

    public var id: String { rawValue }

    public var label: String { self == .normal ? "Normal" : "Strict" }

    public var blurb: String {
        switch self {
        case .normal: return "You can snooze for 5 minutes from the wind-down screen"
        case .strict: return "No snoozing — the wind-down runs its course"
        }
    }
}

public struct SleepState: Codable, Equatable, Sendable {
    public var active: Bool
    public var startTime: Date
    /// Minutes. Grows when the user snoozes.
    public var duration: Int

    public init(active: Bool = false, startTime: Date = .distantPast, duration: Int = 30) {
        self.active = active
        self.startTime = startTime
        self.duration = duration
    }

    public var endTime: Date { SleepLogic.end(start: startTime, duration: duration) }
}

public struct Schedule: Codable, Equatable, Sendable {
    public var enabled: Bool
    public var hour: Int
    public var minute: Int
    public var duration: Int

    public init(enabled: Bool = false, hour: Int = 22, minute: Int = 30, duration: Int = 30) {
        self.enabled = enabled
        self.hour = hour
        self.minute = minute
        self.duration = duration
    }
}

public struct Settings: Codable, Equatable, Sendable {
    public var windDownStyle: WindDownStyle
    public var mode: IntensityMode

    public init(windDownStyle: WindDownStyle = .breathing, mode: IntensityMode = .normal) {
        self.windDownStyle = windDownStyle
        self.mode = mode
    }
}

public struct SessionRecord: Codable, Equatable, Identifiable, Sendable {
    /// Local date the session started, yyyy-MM-dd.
    public var date: String
    public var startTime: Date
    public var endTime: Date
    /// Configured minutes, including snoozes.
    public var duration: Int
    /// true = ran to the end, false = stopped early.
    public var completed: Bool

    public var id: Date { startTime }

    public init(date: String, startTime: Date, endTime: Date, duration: Int, completed: Bool) {
        self.date = date
        self.startTime = startTime
        self.endTime = endTime
        self.duration = duration
        self.completed = completed
    }
}

public enum DayStatus: Equatable, Sendable { case none, partial, done }

public struct DayEntry: Equatable, Identifiable, Sendable {
    public var date: Date
    public var status: DayStatus
    public var id: Date { date }

    public init(date: Date, status: DayStatus) {
        self.date = date
        self.status = status
    }
}

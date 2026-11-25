import Foundation

public enum WindDownContent {
    public static let reflectionPrompts = [
        "What are you grateful for tonight?",
        "Take a breath. You did enough today.",
        "What would you like to let go of?",
        "Rest. Tomorrow is a new beginning.",
        "You are worthy of good sleep.",
        "What brought you joy today?",
    ]

    /// Indexed Sunday = 0 … Saturday = 6.
    public static let journalPrompts = [
        "What was one good thing about today?",
        "What are three things you're grateful for?",
        "What made you smile today?",
        "What's one thing you're proud of today?",
        "What do you want to let go of tonight?",
        "What was the best moment of this week?",
        "What's one intention for tomorrow?",
    ]

    public static let breathSteps = ["Breathe in...", "Hold...", "Breathe out...", "Hold..."]

    /// `weekday` is Calendar's weekday (Sunday = 1 … Saturday = 7).
    public static func journalPrompt(weekday: Int) -> String {
        journalPrompts[((weekday - 1) % 7 + 7) % 7]
    }
}

/// Text for the Screen Time shield. iOS shields can only show a title, a subtitle and two buttons,
/// so each phase gets a short, supportive line instead of the Android overlay's full layout.
public enum ShieldText {
    public static func title(phase: Int) -> String {
        phase >= 3 ? "Time to sleep" : "Time to wind down"
    }

    public static func subtitle(phase: Int, remaining: TimeInterval, style: WindDownStyle, weekday: Int) -> String {
        let mins = SleepLogic.minutesLeft(remaining)
        let left = mins <= 1 ? "Less than a minute left." : "About \(mins) min left."
        guard phase >= 3 else {
            return "\(left) Finish what you're doing and put the phone down soon."
        }
        switch style {
        case .breathing: return "Open sleep-scroll for a slow breathing exercise. \(left)"
        case .reflection: return "\(WindDownContent.reflectionPrompts[weekday % WindDownContent.reflectionPrompts.count]) \(left)"
        case .journaling: return "Tonight's question: \(WindDownContent.journalPrompt(weekday: weekday))"
        case .sounds: return "Put on something calm — rain, a soft playlist, a slow podcast."
        case .silence: return "Rest now."
        }
    }

    /// Snooze is only offered in the soft phase, and never in strict mode.
    public static func allowsSnooze(phase: Int, mode: IntensityMode) -> Bool {
        mode == .normal && phase == 2
    }
}

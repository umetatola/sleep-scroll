package com.sleepscroll.app.core

enum class WindDownStyle(val label: String, val blurb: String, val emoji: String) {
    BREATHING("Breathing", "A slow guided breathing ring", "🌙"),
    REFLECTION("Reflection", "Gentle gratitude prompts that rotate", "🙏"),
    JOURNALING("Journaling", "One question for tonight", "📓"),
    SOUNDS("Sounds", "A nudge to put on calming audio", "🎵"),
    SILENCE("Silence", "A dark screen, nothing else", "🌑");

    companion object {
        fun from(key: String?) = entries.firstOrNull { it.name == key } ?: BREATHING
    }
}

enum class IntensityMode(val label: String, val blurb: String) {
    NORMAL("Normal", "You can dismiss the banner and snooze for 5 minutes"),
    STRICT("Strict", "No dismissing, no snoozing — the wind-down runs its course");

    companion object {
        fun from(key: String?) = entries.firstOrNull { it.name == key } ?: NORMAL
    }
}

/** Social apps sleep-scroll watches. Mirrors the browser extension's target sites. */
enum class TargetApp(val label: String, val packages: Set<String>) {
    TIKTOK("TikTok", setOf("com.zhiliaoapp.musically", "com.ss.android.ugc.trill")),
    INSTAGRAM("Instagram", setOf("com.instagram.android")),
    YOUTUBE("YouTube", setOf("com.google.android.youtube")),
    TWITTER("X / Twitter", setOf("com.twitter.android")),
    FACEBOOK("Facebook", setOf("com.facebook.katana")),
    REDDIT("Reddit", setOf("com.reddit.frontpage"));
}

data class SleepState(
    val active: Boolean = false,
    val startTime: Long = 0L,
    /** Minutes. Grows when the user snoozes. */
    val duration: Int = 30,
)

data class Schedule(
    val enabled: Boolean = false,
    val hour: Int = 22,
    val minute: Int = 30,
    val duration: Int = 30,
)

data class Settings(
    val windDownStyle: WindDownStyle = WindDownStyle.BREATHING,
    val mode: IntensityMode = IntensityMode.NORMAL,
    val targetApps: Set<TargetApp> = TargetApp.entries.toSet(),
) {
    val targetPackages: Set<String> get() = targetApps.flatMap { it.packages }.toSet()
}

data class SessionRecord(
    /** Local date the session started, ISO yyyy-MM-dd. */
    val date: String,
    val startTime: Long,
    val endTime: Long,
    /** Configured minutes (including snoozes). */
    val duration: Int,
    /** true = ran to the end, false = stopped early. */
    val completed: Boolean,
)

enum class DayStatus { NONE, PARTIAL, DONE }

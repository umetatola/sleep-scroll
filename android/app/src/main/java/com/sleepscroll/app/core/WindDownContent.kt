package com.sleepscroll.app.core

object WindDownContent {
    val REFLECTION_PROMPTS = listOf(
        "What are you grateful for tonight?",
        "Take a breath. You did enough today.",
        "What would you like to let go of?",
        "Rest. Tomorrow is a new beginning.",
        "You are worthy of good sleep.",
        "What brought you joy today?",
    )

    /** Indexed Sunday = 0 … Saturday = 6. */
    val JOURNAL_PROMPTS = listOf(
        "What was one good thing about today?",
        "What are three things you're grateful for?",
        "What made you smile today?",
        "What's one thing you're proud of today?",
        "What do you want to let go of tonight?",
        "What was the best moment of this week?",
        "What's one intention for tomorrow?",
    )

    val BREATH_STEPS = listOf("Breathe in...", "Hold...", "Breathe out...", "Hold...")

    /** [dayOfWeek] is java.time.DayOfWeek.value (Monday = 1 … Sunday = 7). */
    fun journalPrompt(dayOfWeek: Int): String = JOURNAL_PROMPTS[dayOfWeek % 7]
}

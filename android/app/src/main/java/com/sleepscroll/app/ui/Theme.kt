package com.sleepscroll.app.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// A night-time app: always dark, so opening it at 11pm never blasts your eyes.
val Night = Color(0xFF0B0B1E)
val Surface1 = Color(0xFF14143A)
val Surface2 = Color(0xFF1E1B4B)
val Indigo = Color(0xFF818CF8)
val IndigoSoft = Color(0xFFC7D2FE)
val Mint = Color(0xFF6EE7B7)
val Amber = Color(0xFFFCD34D)

private val Scheme = darkColorScheme(
    primary = Indigo,
    onPrimary = Surface2,
    primaryContainer = Surface2,
    onPrimaryContainer = IndigoSoft,
    secondary = IndigoSoft,
    background = Night,
    onBackground = Color(0xFFE0E7FF),
    surface = Surface1,
    onSurface = Color(0xFFE0E7FF),
    surfaceVariant = Surface2,
    onSurfaceVariant = Color(0xFFA5B4FC),
    outline = Color(0xFF4338CA),
    error = Color(0xFFFCA5A5),
)

@Composable
fun SleepScrollTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Scheme, content = content)
}

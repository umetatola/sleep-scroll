package com.sleepscroll.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sleepscroll.app.core.DayStatus
import com.sleepscroll.app.core.SessionRecord
import com.sleepscroll.app.core.SleepLogic
import com.sleepscroll.app.data.SleepRepository
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

@Composable
fun ProgressScreen(modifier: Modifier = Modifier) {
    val sessions by SleepRepository.sessions.collectAsStateWithLifecycle()
    val today = LocalDate.now()
    val recent = sessions.asReversed().take(14)

    LazyColumn(modifier.padding(horizontal = 16.dp), contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 24.dp)) {
        item { ScreenTitle("Progress", "Consistency beats perfection.") }

        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatTile(SleepLogic.streak(sessions, today).toString(), "day streak", Modifier.weight(1f))
                StatTile(sessions.size.toString(), "sessions", Modifier.weight(1f))
                StatTile(if (sessions.isEmpty()) "–" else "${SleepLogic.averageDuration(sessions)}m", "avg length", Modifier.weight(1f))
            }
        }

        item {
            SectionLabel("Last 7 days")
            NightCard {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    SleepLogic.lastSevenDays(sessions, today).forEach { (date, status) ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                Modifier
                                    .size(28.dp)
                                    .background(
                                        when (status) {
                                            DayStatus.DONE -> Mint
                                            DayStatus.PARTIAL -> Amber
                                            DayStatus.NONE -> Surface2
                                        },
                                        CircleShape,
                                    )
                            )
                            Text(
                                date.dayOfWeek.getDisplayName(java.time.format.TextStyle.SHORT, Locale.getDefault()),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 6.dp),
                            )
                        }
                    }
                }
                Row(Modifier.padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Legend(Mint, "Completed")
                    Legend(Amber, "Stopped early")
                }
            }
        }

        item { SectionLabel("Recent sessions") }

        if (recent.isEmpty()) {
            item {
                NightCard {
                    Text("No sessions yet", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(
                        "Start a wind-down tonight and it will show up here.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        } else {
            items(recent, key = { it.startTime }) { SessionRow(it) }
        }
    }
}

@Composable
private fun Legend(color: androidx.compose.ui.graphics.Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(10.dp).background(color, CircleShape))
        Text("  $label", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun SessionRow(s: SessionRecord) {
    val date = runCatching {
        LocalDate.parse(s.date).format(DateTimeFormatter.ofPattern("EEE, MMM d", Locale.getDefault()))
    }.getOrDefault(s.date)
    val time = Instant.ofEpochMilli(s.startTime).atZone(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT))
    val elapsed = ((s.endTime - s.startTime) / 60_000L).coerceAtLeast(0)

    NightCard(Modifier.padding(bottom = 8.dp)) {
        RowSpaced {
            Column {
                Text(date, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                Text(time, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("${s.duration} min", style = MaterialTheme.typography.bodyLarge)
                Text(
                    if (s.completed) "✓ completed" else "stopped at ${elapsed}m",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (s.completed) Mint else Amber,
                )
            }
        }
    }
}

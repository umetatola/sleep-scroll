package com.sleepscroll.app.ui

import android.Manifest
import android.app.TimePickerDialog
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sleepscroll.app.core.IntensityMode
import com.sleepscroll.app.core.SleepLogic
import com.sleepscroll.app.data.Permissions
import com.sleepscroll.app.data.SleepRepository
import com.sleepscroll.app.schedule.Scheduler
import com.sleepscroll.app.service.SleepController
import kotlinx.coroutines.delay

private val DURATIONS = listOf(15, 30, 45, 60)

@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
    val sleep by SleepRepository.sleep.collectAsStateWithLifecycle()
    val perms = rememberPermissionStatus()

    Column(
        modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(bottom = 24.dp)
    ) {
        ScreenTitle("Tonight", "Ease into sleep, one gentle nudge at a time.")
        if (!perms.requiredOk || !perms.notifications) SetupCard(perms)
        Spacer(Modifier.height(12.dp))
        if (sleep.active) ActiveCard() else StartCard()
        Spacer(Modifier.height(12.dp))
        ScheduleCard(perms.exactAlarms)
    }
}

// ── Setup ────────────────────────────────────────────────────────────────────

@Composable
private fun SetupCard(perms: PermissionStatus) {
    val ctx = LocalContext.current
    val notifLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}

    NightCard {
        Text("Finish setup", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Text(
            "sleep-scroll needs a few permissions to notice when you open a social app, show the wind-down on top of it, and keep a countdown in your notifications. " +
                "Nothing is recorded or sent anywhere.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp, bottom = 8.dp),
        )
        SetupRow("Usage access", "Lets us see which app is open", perms.usage, "Allow") {
            ctx.startActivity(Permissions.usageAccessIntent())
        }
        SetupRow("Display over other apps", "Lets us draw the wind-down screens", perms.overlay, "Allow") {
            ctx.startActivity(Permissions.overlayIntent(ctx))
        }
        if (Build.VERSION.SDK_INT >= 33) {
            SetupRow("Notifications", "Shows the countdown while sleep mode runs", perms.notifications, "Allow") {
                notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}

@Composable
private fun SetupRow(title: String, why: String, granted: Boolean, action: String, onClick: () -> Unit) {
    RowSpaced {
        Column(Modifier.weight(1f).padding(vertical = 6.dp)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(why, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (granted) Text("✓ On", color = Mint, style = MaterialTheme.typography.labelLarge)
        else TextButton(onClick = onClick) { Text(action) }
    }
}

// ── Start ────────────────────────────────────────────────────────────────────

@Composable
private fun StartCard() {
    val ctx = LocalContext.current
    var minutes by rememberSaveable { mutableIntStateOf(30) }
    val settings by SleepRepository.settings.collectAsStateWithLifecycle()

    NightCard {
        Text("🌙", fontSize = 48.sp, modifier = Modifier.align(Alignment.CenterHorizontally))
        Text(
            "Start a wind-down",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        )
        Text(
            "Social apps stay open at first, then softly give way to rest over the next $minutes minutes.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 16.dp),
        )
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DURATIONS.forEach { d ->
                FilterChip(
                    selected = minutes == d,
                    onClick = { minutes = d },
                    label = { Text("$d min") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Indigo,
                        selectedLabelColor = Surface2,
                    ),
                )
            }
        }
        Text(
            "${settings.windDownStyle.label} wind-down · ${settings.mode.label} mode",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 12.dp),
        )
        Button(
            onClick = { SleepController.start(ctx, minutes) },
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp).height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Indigo, contentColor = Surface2),
        ) { Text("Start sleep mode", fontWeight = FontWeight.Bold) }
    }
}

// ── Active ───────────────────────────────────────────────────────────────────

@Composable
private fun ActiveCard() {
    val ctx = LocalContext.current
    val sleep by SleepRepository.sleep.collectAsStateWithLifecycle()
    val settings by SleepRepository.settings.collectAsStateWithLifecycle()
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            now = System.currentTimeMillis()
            delay(1000)
        }
    }

    val remaining = SleepLogic.remainingMs(sleep.startTime, sleep.duration, now)
    val phase = SleepLogic.phaseFor(sleep.startTime, sleep.duration, now)
    val label = when (phase) {
        0 -> "Plenty of time — enjoy your feed."
        1 -> "Sleep mode is starting soon."
        2 -> "Time to wrap up."
        else -> "Time to sleep."
    }

    NightCard {
        Text(
            "SLEEP MODE ON",
            style = MaterialTheme.typography.labelMedium,
            color = Mint,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )
        Text(
            SleepLogic.formatRemaining(remaining),
            fontSize = 64.sp,
            fontWeight = FontWeight.Bold,
            color = IndigoSoft,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        )
        Text(
            label,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        LinearProgressIndicator(
            progress = { SleepLogic.progress(sleep.startTime, sleep.duration, now) },
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
            color = Indigo,
            trackColor = Surface2,
        )
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (settings.mode == IntensityMode.NORMAL) {
                OutlinedButton(
                    onClick = { SleepController.snooze(ctx) },
                    modifier = Modifier.weight(1f),
                ) { Text("+${SleepLogic.SNOOZE_MINUTES} min") }
            }
            OutlinedButton(
                onClick = { SleepController.stop(ctx) },
                modifier = Modifier.weight(1f),
            ) { Text("Stop") }
        }
    }
}

// ── Schedule ─────────────────────────────────────────────────────────────────

@Composable
private fun ScheduleCard(exactAlarmsOk: Boolean) {
    val ctx = LocalContext.current
    val schedule by SleepRepository.schedule.collectAsStateWithLifecycle()

    fun save(next: com.sleepscroll.app.core.Schedule) {
        SleepRepository.setSchedule(next)
        Scheduler.apply(ctx, next)
    }

    NightCard {
        RowSpaced {
            Column(Modifier.weight(1f)) {
                Text("Every night", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(
                    if (schedule.enabled) "Starts at ${SleepLogic.formatClock(schedule.hour, schedule.minute)} for ${schedule.duration} min"
                    else "Start sleep mode automatically at bedtime",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Switch(checked = schedule.enabled, onCheckedChange = { save(schedule.copy(enabled = it)) })
        }
        if (schedule.enabled) {
            Row(
                Modifier.fillMaxWidth().padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedButton(onClick = {
                    TimePickerDialog(ctx, { _, h, m -> save(schedule.copy(hour = h, minute = m)) }, schedule.hour, schedule.minute, false).show()
                }) { Text(SleepLogic.formatClock(schedule.hour, schedule.minute)) }
                DURATIONS.forEach { d ->
                    FilterChip(
                        selected = schedule.duration == d,
                        onClick = { save(schedule.copy(duration = d)) },
                        label = { Text("$d") },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Indigo, selectedLabelColor = Surface2),
                    )
                }
            }
            if (!exactAlarmsOk) {
                Text(
                    "Android may start this a few minutes late. Allow exact alarms for on-the-minute timing.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Amber,
                    modifier = Modifier.padding(top = 8.dp),
                )
                TextButton(onClick = { ctx.startActivity(Permissions.exactAlarmIntent(ctx)) }) { Text("Allow exact alarms") }
            }
        }
    }
}

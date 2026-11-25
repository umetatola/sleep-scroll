package com.sleepscroll.app.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sleepscroll.app.BuildConfig
import com.sleepscroll.app.core.IntensityMode
import com.sleepscroll.app.core.TargetApp
import com.sleepscroll.app.core.WindDownStyle
import com.sleepscroll.app.data.Permissions
import com.sleepscroll.app.data.SleepRepository

@Composable
fun SettingsScreen(modifier: Modifier = Modifier) {
    val ctx = LocalContext.current
    val settings by SleepRepository.settings.collectAsStateWithLifecycle()
    val sleep by SleepRepository.sleep.collectAsStateWithLifecycle()
    val perms = rememberPermissionStatus()
    var confirmClear by remember { mutableStateOf(false) }

    Column(
        modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(bottom = 24.dp)
    ) {
        ScreenTitle("Settings")

        SectionLabel("Wind-down style")
        NightCard {
            WindDownStyle.entries.forEach { style ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable { SleepRepository.updateSettings { it.copy(windDownStyle = style) } },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RadioButton(selected = settings.windDownStyle == style, onClick = {
                        SleepRepository.updateSettings { it.copy(windDownStyle = style) }
                    })
                    Column(Modifier.padding(vertical = 6.dp)) {
                        Text("${style.emoji}  ${style.label}", style = MaterialTheme.typography.bodyLarge)
                        Text(style.blurb, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        SectionLabel("Intensity")
        NightCard {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IntensityMode.entries.forEach { m ->
                    FilterChip(
                        selected = settings.mode == m,
                        onClick = { SleepRepository.updateSettings { it.copy(mode = m) } },
                        label = { Text(m.label) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Indigo, selectedLabelColor = Surface2),
                    )
                }
            }
            Text(
                settings.mode.blurb,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp),
            )
            if (sleep.active) {
                Text(
                    "Changes apply to the running session too.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Amber,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }

        SectionLabel("Apps to wind down")
        NightCard {
            TargetApp.entries.forEach { app ->
                RowSpaced {
                    Text(app.label, style = MaterialTheme.typography.bodyLarge)
                    Switch(
                        checked = app in settings.targetApps,
                        onCheckedChange = { on ->
                            SleepRepository.updateSettings {
                                it.copy(targetApps = if (on) it.targetApps + app else it.targetApps - app)
                            }
                        },
                    )
                }
            }
        }

        SectionLabel("Permissions")
        NightCard {
            PermRow("Usage access", perms.usage) { ctx.startActivity(Permissions.usageAccessIntent()) }
            PermRow("Display over other apps", perms.overlay) { ctx.startActivity(Permissions.overlayIntent(ctx)) }
            PermRow("Notifications", perms.notifications) { ctx.startActivity(Permissions.appDetailsIntent(ctx)) }
            PermRow("Exact alarms (optional)", perms.exactAlarms) { ctx.startActivity(Permissions.exactAlarmIntent(ctx)) }
        }

        SectionLabel("Your data")
        NightCard {
            Text(
                "Everything stays on this phone. sleep-scroll has no account, no analytics and no network access.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            TextButton(onClick = { confirmClear = true }) { Text("Clear session history") }
        }

        Text(
            "sleep-scroll ${BuildConfig.VERSION_NAME}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 24.dp).align(Alignment.CenterHorizontally),
        )
    }

    if (confirmClear) {
        AlertDialog(
            onDismissRequest = { confirmClear = false },
            title = { Text("Clear history?") },
            text = { Text("This removes your streak and all recorded sessions. It can't be undone.") },
            confirmButton = {
                TextButton(onClick = { SleepRepository.clearSessions(); confirmClear = false }) { Text("Clear") }
            },
            dismissButton = { TextButton(onClick = { confirmClear = false }) { Text("Cancel") } },
        )
    }
}

@Composable
private fun PermRow(label: String, granted: Boolean, onFix: () -> Unit) {
    RowSpaced {
        Text(label, style = MaterialTheme.typography.bodyLarge)
        if (granted) Text("✓ On", color = Mint, style = MaterialTheme.typography.labelLarge)
        else TextButton(onClick = onFix) { Text("Allow") }
    }
}

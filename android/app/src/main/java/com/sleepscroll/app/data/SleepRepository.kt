package com.sleepscroll.app.data

import android.content.Context
import android.content.SharedPreferences
import com.sleepscroll.app.core.IntensityMode
import com.sleepscroll.app.core.Schedule
import com.sleepscroll.app.core.SessionRecord
import com.sleepscroll.app.core.Settings
import com.sleepscroll.app.core.SleepLogic
import com.sleepscroll.app.core.SleepState
import com.sleepscroll.app.core.TargetApp
import com.sleepscroll.app.core.WindDownStyle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

/**
 * Single source of truth, backed by SharedPreferences and exposed as StateFlows so the UI and the
 * foreground service always see the same state. Everything is on-device; nothing leaves the phone.
 */
object SleepRepository {
    private const val FILE = "sleep_scroll"
    private lateinit var prefs: SharedPreferences

    private val _sleep = MutableStateFlow(SleepState())
    private val _schedule = MutableStateFlow(Schedule())
    private val _settings = MutableStateFlow(Settings())
    private val _sessions = MutableStateFlow<List<SessionRecord>>(emptyList())

    val sleep: StateFlow<SleepState> = _sleep.asStateFlow()
    val schedule: StateFlow<Schedule> = _schedule.asStateFlow()
    val settings: StateFlow<Settings> = _settings.asStateFlow()
    val sessions: StateFlow<List<SessionRecord>> = _sessions.asStateFlow()

    @Synchronized
    fun init(context: Context) {
        if (::prefs.isInitialized) return
        prefs = context.applicationContext.getSharedPreferences(FILE, Context.MODE_PRIVATE)
        _sleep.value = SleepState(
            active = prefs.getBoolean("sleep_active", false),
            startTime = prefs.getLong("sleep_start", 0L),
            duration = prefs.getInt("sleep_duration", 30),
        )
        _schedule.value = Schedule(
            enabled = prefs.getBoolean("sched_enabled", false),
            hour = prefs.getInt("sched_hour", 22),
            minute = prefs.getInt("sched_minute", 30),
            duration = prefs.getInt("sched_duration", 30),
        )
        val targets = prefs.getStringSet("targets", null)
        _settings.value = Settings(
            windDownStyle = WindDownStyle.from(prefs.getString("style", null)),
            mode = IntensityMode.from(prefs.getString("mode", null)),
            targetApps = if (targets == null) TargetApp.entries.toSet()
            else TargetApp.entries.filter { it.name in targets }.toSet(),
        )
        _sessions.value = parseSessions(prefs.getString("sessions", null))
    }

    @Synchronized
    fun setSleep(state: SleepState) {
        prefs.edit()
            .putBoolean("sleep_active", state.active)
            .putLong("sleep_start", state.startTime)
            .putInt("sleep_duration", state.duration)
            .apply()
        _sleep.value = state
    }

    @Synchronized
    fun setSchedule(schedule: Schedule) {
        prefs.edit()
            .putBoolean("sched_enabled", schedule.enabled)
            .putInt("sched_hour", schedule.hour)
            .putInt("sched_minute", schedule.minute)
            .putInt("sched_duration", schedule.duration)
            .apply()
        _schedule.value = schedule
    }

    @Synchronized
    fun setSettings(settings: Settings) {
        prefs.edit()
            .putString("style", settings.windDownStyle.name)
            .putString("mode", settings.mode.name)
            .putStringSet("targets", settings.targetApps.map { it.name }.toSet())
            .apply()
        _settings.value = settings
    }

    fun updateSettings(transform: (Settings) -> Settings) = setSettings(transform(_settings.value))
    fun updateSchedule(transform: (Schedule) -> Schedule) = setSchedule(transform(_schedule.value))

    @Synchronized
    fun addSession(record: SessionRecord) {
        val next = SleepLogic.appendCapped(_sessions.value, record)
        prefs.edit().putString("sessions", serializeSessions(next)).apply()
        _sessions.value = next
    }

    @Synchronized
    fun clearSessions() {
        prefs.edit().remove("sessions").apply()
        _sessions.value = emptyList()
    }

    private fun serializeSessions(list: List<SessionRecord>): String {
        val arr = JSONArray()
        for (s in list) {
            arr.put(
                JSONObject()
                    .put("date", s.date).put("start", s.startTime).put("end", s.endTime)
                    .put("duration", s.duration).put("completed", s.completed)
            )
        }
        return arr.toString()
    }

    private fun parseSessions(json: String?): List<SessionRecord> {
        if (json.isNullOrBlank()) return emptyList()
        return try {
            val arr = JSONArray(json)
            (0 until arr.length()).map {
                val o = arr.getJSONObject(it)
                SessionRecord(
                    date = o.getString("date"),
                    startTime = o.getLong("start"),
                    endTime = o.getLong("end"),
                    duration = o.getInt("duration"),
                    completed = o.getBoolean("completed"),
                )
            }
        } catch (_: Exception) {
            emptyList()
        }
    }
}

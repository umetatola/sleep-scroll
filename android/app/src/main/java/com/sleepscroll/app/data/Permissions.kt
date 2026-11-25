package com.sleepscroll.app.data

import android.Manifest
import android.app.AlarmManager
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Process
import android.provider.Settings
import androidx.core.content.ContextCompat

/** What sleep-scroll needs from the system, and the settings screens that grant each one. */
object Permissions {

    /** Required: lets us see which app is in the foreground (UsageStatsManager). */
    fun hasUsageAccess(context: Context): Boolean {
        val ops = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = if (Build.VERSION.SDK_INT >= 29) {
            ops.unsafeCheckOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), context.packageName)
        } else {
            @Suppress("DEPRECATION")
            ops.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), context.packageName)
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    /** Required: lets us draw the wind-down screens on top of other apps. */
    fun hasOverlay(context: Context): Boolean = Settings.canDrawOverlays(context)

    /** Recommended: the ongoing "sleep mode" notification and the good-night note. */
    fun hasNotifications(context: Context): Boolean =
        Build.VERSION.SDK_INT < 33 ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED

    /** Optional: lets the nightly schedule fire at the exact minute instead of "around" it. */
    fun canExactAlarms(context: Context): Boolean =
        Build.VERSION.SDK_INT < 31 ||
            (context.getSystemService(Context.ALARM_SERVICE) as AlarmManager).canScheduleExactAlarms()

    fun requiredGranted(context: Context) = hasUsageAccess(context) && hasOverlay(context)

    fun usageAccessIntent() = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).newTask()

    fun overlayIntent(context: Context) =
        Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${context.packageName}")).newTask()

    fun exactAlarmIntent(context: Context): Intent =
        if (Build.VERSION.SDK_INT >= 31) {
            Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:${context.packageName}")).newTask()
        } else {
            appDetailsIntent(context)
        }

    fun appDetailsIntent(context: Context) =
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${context.packageName}")).newTask()

    private fun Intent.newTask() = apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
}

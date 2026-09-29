package com.example.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import com.example.receiver.AlarmReceiver
import java.util.Calendar

object CadenceAlarmScheduler {

    fun canScheduleExactAlarms(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
            alarmManager?.canScheduleExactAlarms() ?: false
        } else {
            true
        }
    }

    fun getExactAlarmSettingsIntent(context: Context): Intent {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                data = Uri.parse("package:${context.packageName}")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
        } else {
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:${context.packageName}")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
        }
    }

    fun scheduleAlarm(
        context: Context,
        alarmId: Int,
        triggerTimeMillis: Long,
        title: String,
        body: String,
        category: String = "ALARM",
        destinationScreen: String = "TODAY",
        extraId: Long = 0L,
        isAlarm: Boolean = true
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = "com.example.ACTION_ALARM_TRIGGER"
            putExtra("EXTRA_ALARM_ID", alarmId)
            putExtra("EXTRA_TITLE", title)
            putExtra("EXTRA_BODY", body)
            putExtra("EXTRA_CATEGORY", category)
            putExtra("EXTRA_SCREEN", destinationScreen)
            putExtra("EXTRA_TARGET_ID", extraId)
            putExtra("EXTRA_IS_ALARM", isAlarm)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarmId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (canScheduleExactAlarms(context)) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTimeMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTimeMillis,
                        pendingIntent
                    )
                }
            } else {
                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    triggerTimeMillis,
                    pendingIntent
                )
            }
        } catch (_: SecurityException) {
            // Fallback for security restrictions
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTimeMillis, pendingIntent)
        }
    }

    fun cancelAlarm(context: Context, alarmId: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = "com.example.ACTION_ALARM_TRIGGER"
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarmId,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    /**
     * Parses time string e.g. "06:00 AM", "14:30", "8:00 PM" into next upcoming millis
     */
    fun parseTimeStringToNextMillis(timeStr: String): Long {
        val calendar = Calendar.getInstance()
        val now = System.currentTimeMillis()

        try {
            val isPm = timeStr.contains("PM", ignoreCase = true)
            val isAm = timeStr.contains("AM", ignoreCase = true)
            val cleanTime = timeStr.replace("AM", "", ignoreCase = true)
                .replace("PM", "", ignoreCase = true)
                .trim()
            val parts = cleanTime.split(":")
            var hour = parts[0].trim().toInt()
            val minute = if (parts.size > 1) parts[1].trim().toInt() else 0

            if (isPm && hour < 12) hour += 12
            if (isAm && hour == 12) hour = 0

            calendar.set(Calendar.HOUR_OF_DAY, hour)
            calendar.set(Calendar.MINUTE, minute)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)

            if (calendar.timeInMillis <= now) {
                calendar.add(Calendar.DAY_OF_YEAR, 1)
            }
            return calendar.timeInMillis
        } catch (_: Exception) {
            // Default 1 hour from now
            return now + 3600 * 1000
        }
    }
}

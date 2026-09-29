package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.alarm.CadenceAlarmScheduler
import com.example.data.CompanionDatabase
import com.example.haptics.CadenceHaptics
import com.example.notification.CadenceNotificationManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val alarmId = intent.getIntExtra("EXTRA_ALARM_ID", 1001)
        val notifId = intent.getIntExtra("EXTRA_NOTIFICATION_ID", alarmId)
        val title = intent.getStringExtra("EXTRA_TITLE") ?: "Cadence Reminder"
        val body = intent.getStringExtra("EXTRA_BODY") ?: "Time for your scheduled stillness & reading."
        val category = intent.getStringExtra("EXTRA_CATEGORY") ?: "ALARM"
        val screen = intent.getStringExtra("EXTRA_SCREEN") ?: "TODAY"
        val targetId = intent.getLongExtra("EXTRA_TARGET_ID", 0L)
        val isAlarm = intent.getBooleanExtra("EXTRA_IS_ALARM", true)

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = CompanionDatabase.getInstance(context)
                val prefs = db.userPreferencesDao().getUserPreferencesDirect()
                val isHapticsEnabled = prefs?.haptics ?: true
                val isNotificationsEnabled = prefs?.notifications ?: true
                val quietHours = prefs?.quietHours ?: "22:00 - 06:00"

                when (action) {
                    "com.example.ACTION_ALARM_TRIGGER" -> {
                        val isQuiet = CadenceNotificationManager.isQuietHoursActive(quietHours)
                        // If notifications are disabled or non-alarm in quiet hours, suppress non-essential triggers
                        if (isNotificationsEnabled) {
                            if (!isQuiet || isAlarm) {
                                // Trigger subtle haptic if enabled
                                CadenceHaptics.performHaptic(context, CadenceHaptics.HapticType.ALARM_PULSE, isHapticsEnabled)

                                // Show notification with sound/vibration & action buttons
                                CadenceNotificationManager.showNotification(
                                    context = context,
                                    notificationId = alarmId,
                                    category = category,
                                    title = title,
                                    body = body,
                                    destinationScreen = screen,
                                    extraId = targetId,
                                    isAlarm = isAlarm
                                )
                            }
                        }
                    }

                    "com.example.ACTION_ALARM_SNOOZE" -> {
                        // Cancel active notification
                        CadenceNotificationManager.cancelNotification(context, notifId)

                        // Reschedule for 10 minutes from now
                        val snoozeTime = System.currentTimeMillis() + 10 * 60 * 1000
                        val snoozedAlarmId = notifId + 50000

                        CadenceAlarmScheduler.scheduleAlarm(
                            context = context,
                            alarmId = snoozedAlarmId,
                            triggerTimeMillis = snoozeTime,
                            title = "Snoozed: $title",
                            body = body,
                            category = category,
                            destinationScreen = screen,
                            extraId = targetId,
                            isAlarm = true
                        )

                        // Feedback haptic
                        CadenceHaptics.performHaptic(context, CadenceHaptics.HapticType.BUTTON_CLICK, isHapticsEnabled)
                    }

                    "com.example.ACTION_ALARM_DISMISS" -> {
                        CadenceNotificationManager.cancelNotification(context, notifId)
                        CadenceHaptics.performHaptic(context, CadenceHaptics.HapticType.BUTTON_CLICK, isHapticsEnabled)
                    }
                }
            } catch (_: Exception) {
            } finally {
                pendingResult.finish()
            }
        }
    }
}

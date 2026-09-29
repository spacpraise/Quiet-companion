package com.example.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.receiver.AlarmReceiver

object CadenceNotificationManager {

    const val CHANNEL_READING = "channel_reading"
    const val CHANNEL_ROUTINE = "channel_routine"
    const val CHANNEL_REMINDER = "channel_reminder"
    const val CHANNEL_ACHIEVEMENT = "channel_achievement"
    const val CHANNEL_AI = "channel_ai"
    const val CHANNEL_SYSTEM = "channel_system"
    const val CHANNEL_ALARM = "channel_alarm"

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build()

            val channels = listOf(
                NotificationChannel(
                    CHANNEL_READING,
                    "Reading Cadence",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Reading reminders, bookmark syncs, and chapter targets"
                    setSound(defaultSoundUri, audioAttributes)
                    enableVibration(true)
                },
                NotificationChannel(
                    CHANNEL_ROUTINE,
                    "Routine & Rituals",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Daily morning, evening, and study cadence anchors"
                    setSound(defaultSoundUri, audioAttributes)
                    enableVibration(true)
                },
                NotificationChannel(
                    CHANNEL_REMINDER,
                    "Task Reminders",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Scheduled task notifications and upcoming rhythm items"
                    setSound(defaultSoundUri, audioAttributes)
                    enableVibration(true)
                },
                NotificationChannel(
                    CHANNEL_ACHIEVEMENT,
                    "Milestones & Harmony",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Completed books, reading streaks, and sanctuary milestones"
                    setSound(defaultSoundUri, audioAttributes)
                },
                NotificationChannel(
                    CHANNEL_AI,
                    "Quiet Companion AI",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Proactive insights and smart reading schedule adjustments"
                    setSound(defaultSoundUri, audioAttributes)
                },
                NotificationChannel(
                    CHANNEL_SYSTEM,
                    "System & Backup",
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "Database synchronization and general maintenance"
                },
                NotificationChannel(
                    CHANNEL_ALARM,
                    "Alarms & Wake Chimes",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Exact time alarms for morning prayer, bible, and study blocks"
                    val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                    setSound(
                        alarmSound,
                        AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .build()
                    )
                    enableVibration(true)
                }
            )

            notificationManager.createNotificationChannels(channels)
        }
    }

    fun showNotification(
        context: Context,
        notificationId: Int,
        category: String,
        title: String,
        body: String,
        destinationScreen: String = "TODAY",
        extraId: Long = 0L,
        isAlarm: Boolean = false
    ) {
        val channelId = when (category.uppercase()) {
            "READING" -> CHANNEL_READING
            "ROUTINE" -> CHANNEL_ROUTINE
            "REMINDER" -> CHANNEL_REMINDER
            "ACHIEVEMENT" -> CHANNEL_ACHIEVEMENT
            "AI" -> CHANNEL_AI
            "ALARM" -> CHANNEL_ALARM
            else -> CHANNEL_SYSTEM
        }

        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("EXTRA_SCREEN", destinationScreen)
            putExtra("EXTRA_TARGET_ID", extraId)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(if (isAlarm) NotificationCompat.PRIORITY_MAX else NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setColor(Color.parseColor("#1B1C1C")) // Monochrome InkBlack

        if (isAlarm) {
            // Snooze Action (10 mins)
            val snoozeIntent = Intent(context, AlarmReceiver::class.java).apply {
                action = "com.example.ACTION_ALARM_SNOOZE"
                putExtra("EXTRA_NOTIFICATION_ID", notificationId)
                putExtra("EXTRA_TITLE", title)
                putExtra("EXTRA_BODY", body)
                putExtra("EXTRA_SCREEN", destinationScreen)
                putExtra("EXTRA_TARGET_ID", extraId)
            }
            val snoozePendingIntent = PendingIntent.getBroadcast(
                context,
                notificationId + 100000,
                snoozeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Dismiss Action
            val dismissIntent = Intent(context, AlarmReceiver::class.java).apply {
                action = "com.example.ACTION_ALARM_DISMISS"
                putExtra("EXTRA_NOTIFICATION_ID", notificationId)
            }
            val dismissPendingIntent = PendingIntent.getBroadcast(
                context,
                notificationId + 200000,
                dismissIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            builder.addAction(android.R.drawable.ic_menu_recent_history, "Snooze 10m", snoozePendingIntent)
            builder.addAction(android.R.drawable.ic_menu_close_clear_cancel, "Dismiss", dismissPendingIntent)
            builder.setCategory(NotificationCompat.CATEGORY_ALARM)
        }

        try {
            val notificationManager = NotificationManagerCompat.from(context)
            if (notificationManager.areNotificationsEnabled()) {
                notificationManager.notify(notificationId, builder.build())
            }
        } catch (_: SecurityException) {
            // Handled when notification permission is not granted
        }
    }

    fun cancelNotification(context: Context, notificationId: Int) {
        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.cancel(notificationId)
    }

    /**
     * Checks if the current time falls within user quiet hours (e.g. "22:00 - 06:00")
     */
    fun isQuietHoursActive(quietHours: String = "22:00 - 06:00"): Boolean {
        try {
            val parts = quietHours.split("-").map { it.trim() }
            if (parts.size != 2) return false

            val startParts = parts[0].split(":").map { it.trim().toInt() }
            val endParts = parts[1].split(":").map { it.trim().toInt() }

            val cal = java.util.Calendar.getInstance()
            val currentMinutes = cal.get(java.util.Calendar.HOUR_OF_DAY) * 60 + cal.get(java.util.Calendar.MINUTE)

            val startMinutes = startParts[0] * 60 + (if (startParts.size > 1) startParts[1] else 0)
            val endMinutes = endParts[0] * 60 + (if (endParts.size > 1) endParts[1] else 0)

            return if (startMinutes <= endMinutes) {
                currentMinutes in startMinutes..endMinutes
            } else {
                // Overnight quiet hours, e.g. 22:00 (1320m) to 06:00 (360m)
                currentMinutes >= startMinutes || currentMinutes < endMinutes
            }
        } catch (_: Exception) {
            return false
        }
    }
}

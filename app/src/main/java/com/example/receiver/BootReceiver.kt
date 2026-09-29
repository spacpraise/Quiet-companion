package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.alarm.CadenceAlarmScheduler
import com.example.data.CompanionDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        if (action == Intent.ACTION_BOOT_COMPLETED ||
            action == Intent.ACTION_MY_PACKAGE_REPLACED ||
            action == "android.intent.action.QUICKBOOT_POWERON"
        ) {
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = CompanionDatabase.getInstance(context)

                    // 1. Reschedule all enabled routines with alarms/chimes
                    val routines = db.routineDao().getAllRoutines().firstOrNull() ?: emptyList()
                    for (routine in routines) {
                        if (routine.enabled) {
                            val nextMillis = CadenceAlarmScheduler.parseTimeStringToNextMillis(routine.time)
                            CadenceAlarmScheduler.scheduleAlarm(
                                context = context,
                                alarmId = (routine.id + 1000).toInt(),
                                triggerTimeMillis = nextMillis,
                                title = routine.title,
                                body = "${routine.title} begins now (${routine.duration} mins).",
                                category = "ROUTINE",
                                destinationScreen = "ROUTINE",
                                extraId = routine.id,
                                isAlarm = routine.hasAlarm
                            )
                        }
                    }

                    // 2. Reschedule all uncompleted tasks with notifications or alarms
                    val tasks = db.taskDao().getIncompleteTasks().firstOrNull() ?: emptyList()
                    for (task in tasks) {
                        if (task.hasNotification || task.hasAlarm) {
                            val nextMillis = CadenceAlarmScheduler.parseTimeStringToNextMillis(task.time)
                            CadenceAlarmScheduler.scheduleAlarm(
                                context = context,
                                alarmId = (task.id + 20000).toInt(),
                                triggerTimeMillis = nextMillis,
                                title = task.title,
                                body = if (task.description.isBlank()) "Scheduled cadence activity" else task.description,
                                category = "REMINDER",
                                destinationScreen = "TODAY",
                                extraId = task.id,
                                isAlarm = task.hasAlarm
                            )
                        }
                    }

                } catch (_: Exception) {
                    // Log or ignore during background reboot
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}

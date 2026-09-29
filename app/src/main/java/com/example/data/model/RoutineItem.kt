package com.example.data.model

data class RoutineItem(
    val id: Long = 0,
    val title: String,
    val category: String, // "scripture", "study", "reading", "exercise", "prayer", "other"
    val time: String,     // e.g. "06:15 AM", "14:00 PM", "19:30 PM"
    val durationMinutes: Int = 30,
    val subtitle: String = "",
    val days: String = "Every day", // "Every day", "Monday–Friday", "Weekly", "Monthly", "Custom"
    val isCompleted: Boolean = false,
    val isRecurring: Boolean = true,
    val enabled: Boolean = true,
    val gentleChime: Boolean = true,
    val hasAlarm: Boolean = false
)

fun Routine.toRoutineItem(): RoutineItem {
    return RoutineItem(
        id = id,
        title = title,
        category = type,
        time = time,
        durationMinutes = duration,
        subtitle = "$duration min anchor",
        days = days,
        isCompleted = isCompletedToday,
        isRecurring = days != "Once",
        enabled = enabled,
        gentleChime = gentleChime,
        hasAlarm = hasAlarm
    )
}

fun RoutineItem.toRoutine(): Routine {
    return Routine(
        id = id,
        title = title,
        type = category,
        time = time,
        days = days,
        duration = durationMinutes,
        enabled = enabled,
        isCompletedToday = isCompleted,
        gentleChime = gentleChime,
        hasAlarm = hasAlarm
    )
}

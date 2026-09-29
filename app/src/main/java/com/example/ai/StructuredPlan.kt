package com.example.ai

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class DailyPlanItem(
    val time: String,
    val title: String,
    val category: String, // "Reading", "Study", "Prayer", "Scripture", "Focus"
    val durationMinutes: Int,
    val hasAlarm: Boolean = false,
    val hasNotification: Boolean = true
)

@JsonClass(generateAdapter = true)
data class ProposedDailyPlan(
    val date: String,
    val summary: String,
    val items: List<DailyPlanItem>
)

@JsonClass(generateAdapter = true)
data class ReadingPlanDay(
    val dayNumber: Int,
    val dayLabel: String,
    val startPage: Int,
    val endPage: Int,
    val pagesToRead: Int,
    val estimatedMinutes: Int
)

@JsonClass(generateAdapter = true)
data class ProposedReadingPlan(
    val bookId: Long,
    val bookTitle: String,
    val targetDays: Int,
    val pagesPerDay: Int,
    val remainingPages: Int,
    val currentPage: Int,
    val totalPages: Int,
    val schedule: List<ReadingPlanDay>
)

@JsonClass(generateAdapter = true)
data class ProposedRoutine(
    val title: String,
    val type: String,
    val time: String,
    val duration: Int,
    val days: String,
    val hasAlarm: Boolean,
    val gentleChime: Boolean
)

@JsonClass(generateAdapter = true)
data class ProposedTask(
    val title: String,
    val description: String,
    val date: String,
    val time: String,
    val duration: Int,
    val category: String,
    val hasNotification: Boolean,
    val hasAlarm: Boolean,
    val recurrence: String
)

@JsonClass(generateAdapter = true)
data class ClarificationRequest(
    val question: String,
    val options: List<String> = emptyList(),
    val missingField: String? = null
)

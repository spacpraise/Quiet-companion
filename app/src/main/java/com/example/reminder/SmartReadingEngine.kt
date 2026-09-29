package com.example.reminder

import android.content.Context
import com.example.data.model.Book
import com.example.data.model.Notification
import com.example.data.model.ReadingSession
import com.example.data.model.Routine
import com.example.data.model.UserPreferences
import com.example.notification.CadenceNotificationManager
import java.util.Calendar
import java.util.concurrent.TimeUnit

data class ReadingTrigger(
    val id: String,
    val type: TriggerType,
    val title: String,
    val body: String,
    val destinationScreen: String = "READER",
    val bookId: Long? = null,
    val routineId: Long? = null
)

enum class TriggerType {
    ABANDONED_BOOK,
    ALMOST_FINISHED,
    MISSED_READING,
    EMPTY_SHELF,
    SCHEDULED_SOON,
    STREAK_PRESERVATION
}

object SmartReadingEngine {

    /**
     * Evaluates real database state to produce deterministic reading reminders
     */
    fun evaluateTriggers(
        books: List<Book>,
        routines: List<Routine>,
        sessions: List<ReadingSession>,
        preferences: UserPreferences?,
        existingNotifications: List<Notification>,
        abandonedDaysThreshold: Int = 3
    ): List<ReadingTrigger> {
        val triggers = mutableListOf<ReadingTrigger>()
        val now = System.currentTimeMillis()

        // 1. Check user notification settings and quiet hours
        val notifsEnabled = preferences?.notifications ?: true
        if (!notifsEnabled) return emptyList()

        if (isQuietHours(preferences?.quietHours ?: "22:00 - 06:00")) {
            return emptyList()
        }

        val unfinishedBooks = books.filter { it.currentPage < it.totalPages }
        val completedBooks = books.filter { it.currentPage >= it.totalPages && it.totalPages > 0 }

        // RULE 1: EMPTY SHELF (Occasional reminder, reduced frequency if repeatedly ignored)
        if (books.isNotEmpty() && unfinishedBooks.isEmpty()) {
            val unreadEmptyShelfNotifs = existingNotifications.count {
                !it.read && (it.body.contains("shelf is empty", ignoreCase = true) || it.title.contains("Shelf Completed", ignoreCase = true))
            }
            // Frequency back-off: 0 ignored = 1 day, 1 ignored = 3 days, 2+ ignored = 7 days
            val requiredIntervalDays = when {
                unreadEmptyShelfNotifs >= 2 -> 7
                unreadEmptyShelfNotifs == 1 -> 3
                else -> 1
            }
            if (!hasRecentNotification(existingNotifications, "shelf is empty", requiredIntervalDays)) {
                val triggerId = "empty_shelf_${getCurrentDateString()}"
                triggers.add(
                    ReadingTrigger(
                        id = triggerId,
                        type = TriggerType.EMPTY_SHELF,
                        title = "Shelf Completed",
                        body = "Your shelf is empty. Add another book or revisit a favorite.",
                        destinationScreen = "LIBRARY"
                    )
                )
            }
        }

        // RULE 2: ALMOST FINISHED BOOK (progress >= 85% or remaining pages <= 25)
        for (book in unfinishedBooks) {
            val remainingPages = (book.totalPages - book.currentPage).coerceAtLeast(1)
            val progress = if (book.totalPages > 0) book.currentPage.toFloat() / book.totalPages else 0f

            if (progress >= 0.85f || remainingPages <= 25) {
                val triggerId = "almost_finished_${book.id}"
                if (!hasRecentNotification(existingNotifications, "almost finished with ${book.title}")) {
                    triggers.add(
                        ReadingTrigger(
                            id = triggerId,
                            type = TriggerType.ALMOST_FINISHED,
                            title = "Almost Finished",
                            body = "You're $remainingPages pages from finishing ${book.title}. Keep the cadence alive!",
                            destinationScreen = "READER",
                            bookId = book.id
                        )
                    )
                }
            }
        }

        // RULE 3: ABANDONED BOOK (Haven't read in >= abandonedDaysThreshold days)
        for (book in unfinishedBooks) {
            // Find most recent session for this book or use book.lastReadAt to check
            val lastSession = sessions.filter { it.bookId == book.id }.maxByOrNull { it.endedAt }
            val lastReadTime = lastSession?.endedAt ?: book.lastReadAt

            if (lastReadTime > 0) {
                val diffDays = TimeUnit.MILLISECONDS.toDays(now - lastReadTime)
                if (diffDays >= abandonedDaysThreshold) {
                    val triggerId = "abandoned_${book.id}_${diffDays}d"
                    if (!hasRecentNotification(existingNotifications, "waiting for you")) {
                        triggers.add(
                            ReadingTrigger(
                                id = triggerId,
                                type = TriggerType.ABANDONED_BOOK,
                                title = "${book.title} is waiting",
                                body = "${book.title} is waiting for you. You haven't read it in $diffDays days.",
                                destinationScreen = "READER",
                                bookId = book.id
                            )
                        )
                    }
                }
            }
        }


        // RULE 4: MISSED READING SESSION
        // Check if any reading routine was scheduled for earlier today but remains uncompleted
        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        for (routine in routines) {
            if (routine.enabled && !routine.isCompletedToday && isReadingRoutine(routine)) {
                val routineHour = parseHour(routine.time)
                // If scheduled hour has passed by at least 1 hour and it's evening (> 18:00)
                if (routineHour in 0..currentHour && currentHour >= 18) {
                    val triggerId = "missed_routine_${routine.id}_${getCurrentDateString()}"
                    if (!hasRecentNotification(existingNotifications, "missed your reading session")) {
                        triggers.add(
                            ReadingTrigger(
                                id = triggerId,
                                type = TriggerType.MISSED_READING,
                                title = "Unfinished Reading Cadence",
                                body = "You planned to read ${routine.title} today. Take 15 minutes of calm focus tonight.",
                                destinationScreen = "ROUTINE",
                                routineId = routine.id
                            )
                        )
                    }
                }
            }
        }

        return triggers
    }

    private fun isReadingRoutine(routine: Routine): Boolean {
        val text = "${routine.title} ${routine.type}".lowercase()
        return text.contains("read") || text.contains("bible") || text.contains("book") || text.contains("study")
    }

    private fun parseHour(timeStr: String): Int {
        return try {
            val isPm = timeStr.contains("PM", ignoreCase = true)
            val isAm = timeStr.contains("AM", ignoreCase = true)
            val clean = timeStr.replace("AM", "", ignoreCase = true).replace("PM", "", ignoreCase = true).trim()
            var h = clean.split(":")[0].trim().toInt()
            if (isPm && h < 12) h += 12
            if (isAm && h == 12) h = 0
            h
        } catch (_: Exception) {
            12
        }
    }

    private fun hasRecentNotification(notifications: List<Notification>, snippet: String, days: Int = 1): Boolean {
        val cutoff = System.currentTimeMillis() - (days.toLong() * 24 * 60 * 60 * 1000)
        return notifications.any {
            it.createdAt > cutoff && (it.body.contains(snippet, ignoreCase = true) || it.title.contains(snippet, ignoreCase = true))
        }
    }

    private fun isQuietHours(quietRange: String): Boolean {
        return try {
            val parts = quietRange.split("-")
            if (parts.size != 2) return false
            val startHour = parts[0].trim().split(":")[0].toInt()
            val endHour = parts[1].trim().split(":")[0].toInt()
            val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)

            if (startHour > endHour) {
                currentHour >= startHour || currentHour < endHour
            } else {
                currentHour in startHour until endHour
            }
        } catch (_: Exception) {
            false
        }
    }

    private fun getCurrentDateString(): String {
        val cal = Calendar.getInstance()
        return "${cal.get(Calendar.YEAR)}-${cal.get(Calendar.MONTH)}-${cal.get(Calendar.DAY_OF_MONTH)}"
    }
}

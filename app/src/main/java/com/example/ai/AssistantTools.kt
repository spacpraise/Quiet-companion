package com.example.ai

import com.example.data.CompanionRepository
import com.example.data.model.Book
import com.example.data.model.ReadingSession
import com.example.data.model.Routine
import com.example.data.model.Task
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AssistantToolExecutor(private val repository: CompanionRepository) {

    suspend fun getTodaySchedule(): String {
        val routines = repository.allRoutines.first()
        val tasks = repository.allTasks.first()
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        val sb = StringBuilder("=== Today's Schedule ===\n")
        sb.append("Routines:\n")
        if (routines.isEmpty()) sb.append("  (No routines configured)\n")
        routines.forEach {
            sb.append("  - [${if (it.isCompletedToday) "DONE" else "PENDING"}] ${it.title} at ${it.time} (${it.duration}m, ${it.days})\n")
        }
        sb.append("Tasks:\n")
        val todayTasks = tasks.filter { it.date == todayStr || it.recurrence.isNotEmpty() }
        if (todayTasks.isEmpty()) sb.append("  (No tasks scheduled for today)\n")
        todayTasks.forEach {
            sb.append("  - [${if (it.completed) "DONE" else "TODO"}] ${it.title} (${it.time}, ${it.duration}m)\n")
        }
        return sb.toString()
    }

    suspend fun getUpcomingTasks(): String {
        val tasks = repository.allTasks.first().filter { !it.completed }
        if (tasks.isEmpty()) return "No upcoming pending tasks found."
        return tasks.joinToString("\n") {
            "Task #${it.id}: '${it.title}' on ${it.date} at ${it.time} (${it.duration}m) - Recurrence: ${it.recurrence.ifEmpty { "None" }}"
        }
    }

    suspend fun getBooks(): String {
        val books = repository.allBooks.first()
        if (books.isEmpty()) return "No books found in library."
        return books.joinToString("\n") {
            val pct = if (it.totalPages > 0) (it.currentPage * 100 / it.totalPages) else 0
            "Book #${it.id}: '${it.title}' by ${it.author} — Page ${it.currentPage}/${it.totalPages} ($pct% completed)"
        }
    }

    suspend fun getBookProgress(bookId: Long): String {
        val book = repository.getBookByIdDirect(bookId) ?: return "Book with ID $bookId not found."
        val remaining = (book.totalPages - book.currentPage).coerceAtLeast(0)
        val pct = if (book.totalPages > 0) (book.currentPage * 100 / book.totalPages) else 0
        return "Book '${book.title}': Current Page ${book.currentPage} of ${book.totalPages} ($remaining pages remaining, $pct% progress)."
    }

    suspend fun getLastReadPosition(bookId: Long?): String {
        val books = repository.allBooks.first()
        val book = if (bookId != null) {
            repository.getBookByIdDirect(bookId)
        } else {
            books.maxByOrNull { it.currentPage }
        } ?: return "No reading position found."
        return "Last read position in '${book.title}' is Page ${book.currentPage} of ${book.totalPages}."
    }


    suspend fun getReadingHistory(): String {
        val sessions = repository.allSessions.first().take(10)
        if (sessions.isEmpty()) return "No reading sessions recorded yet."
        return sessions.joinToString("\n") {
            val dateStr = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()).format(Date(it.startedAt))
            val pagesRead = (it.endPage - it.startPage).coerceAtLeast(0)
            val durationMin = (it.duration / 60).toInt()
            "Session on $dateStr: Read $pagesRead pages (pp. ${it.startPage}–${it.endPage}) in $durationMin minutes."
        }
    }

    suspend fun getCurrentReading(): String {
        val books = repository.allBooks.first()
        val current = books.firstOrNull { it.currentPage in 1 until it.totalPages } ?: books.firstOrNull()
        if (current == null) return "No books in library."
        return "Currently reading '${current.title}' by ${current.author}. Page ${current.currentPage}/${current.totalPages}."
    }

    suspend fun getReadingStatistics(): String {
        val sessions = repository.allSessions.first()
        val totalMinutes = sessions.sumOf { (it.duration / 60).toInt() }
        val totalPages = sessions.sumOf { (it.endPage - it.startPage).coerceAtLeast(0) }
        val sessionCount = sessions.size
        return "Reading Stats: $sessionCount completed sessions, $totalMinutes total minutes read, $totalPages total pages turned."
    }


    suspend fun createTask(
        title: String,
        description: String,
        date: String,
        time: String,
        duration: Int,
        category: String = "Reading",
        hasNotification: Boolean = true,
        hasAlarm: Boolean = false,
        recurrence: String = ""
    ): String {
        val task = Task(
            title = title,
            description = description,
            date = date,
            time = time,
            duration = duration,
            category = category,
            hasNotification = hasNotification,
            hasAlarm = hasAlarm,
            recurrence = recurrence,
            completed = false
        )
        val id = repository.insertTask(task)
        return "Task '$title' created successfully with ID #$id scheduled for $date at $time."
    }

    suspend fun updateTask(taskId: Long, title: String?, date: String?, time: String?, duration: Int?): String {
        val existing = repository.allTasks.first().firstOrNull { it.id == taskId }
            ?: return "Task #$taskId not found."
        val updated = existing.copy(
            title = title ?: existing.title,
            date = date ?: existing.date,
            time = time ?: existing.time,
            duration = duration ?: existing.duration
        )
        repository.updateTask(updated)
        return "Task #$taskId updated to '${updated.title}' on ${updated.date} at ${updated.time}."
    }

    suspend fun completeTask(taskId: Long): String {
        val existing = repository.allTasks.first().firstOrNull { it.id == taskId }
            ?: return "Task #$taskId not found."
        repository.setTaskCompletion(taskId, true)
        return "Task '${existing.title}' marked as completed."
    }

    suspend fun deleteTask(taskId: Long): String {
        val existing = repository.allTasks.first().firstOrNull { it.id == taskId }
            ?: return "Task #$taskId not found."
        repository.deleteTask(existing)
        return "Task '${existing.title}' (#$taskId) deleted."
    }

    suspend fun createRoutine(
        title: String,
        type: String,
        time: String,
        duration: Int,
        days: String = "Every day",
        gentleChime: Boolean = true,
        hasAlarm: Boolean = false
    ): String {
        val routine = Routine(
            title = title,
            type = type,
            time = time,
            duration = duration,
            days = days,
            gentleChime = gentleChime,
            hasAlarm = hasAlarm,
            enabled = true
        )
        val id = repository.insertRoutine(routine)
        return "Routine '$title' ($days at $time, $duration min) created successfully with ID #$id."
    }

    suspend fun updateRoutine(
        routineId: Long,
        title: String?,
        time: String?,
        duration: Int?,
        days: String?,
        enabled: Boolean?
    ): String {
        val existing = repository.allRoutines.first().firstOrNull { it.id == routineId }
            ?: return "Routine #$routineId not found."
        val updated = existing.copy(
            title = title ?: existing.title,
            time = time ?: existing.time,
            duration = duration ?: existing.duration,
            days = days ?: existing.days,
            enabled = enabled ?: existing.enabled
        )
        repository.updateRoutine(updated)
        return "Routine #$routineId updated to '${updated.title}' ($days at $time)."
    }

    suspend fun deleteRoutine(routineId: Long): String {
        val existing = repository.allRoutines.first().firstOrNull { it.id == routineId }
            ?: return "Routine #$routineId not found."
        repository.deleteRoutine(existing)
        return "Routine '${existing.title}' (#$routineId) deleted."
    }

    fun calculateReadingPlan(book: Book, targetDays: Int): ProposedReadingPlan {
        val startPage = book.currentPage
        val totalPages = book.totalPages
        val remainingPages = (totalPages - startPage).coerceAtLeast(1)
        val days = targetDays.coerceAtLeast(1)
        val pagesPerDay = kotlin.math.ceil(remainingPages.toDouble() / days).toInt()

        val schedule = mutableListOf<ReadingPlanDay>()
        var cur = startPage
        for (day in 1..days) {
            val nextEnd = kotlin.math.min(cur + pagesPerDay, totalPages)
            val pagesCount = (nextEnd - cur).coerceAtLeast(1)
            val estMinutes = (pagesCount * 1.5).toInt().coerceAtLeast(10)
            schedule.add(
                ReadingPlanDay(
                    dayNumber = day,
                    dayLabel = "Day $day",
                    startPage = cur,
                    endPage = nextEnd,
                    pagesToRead = pagesCount,
                    estimatedMinutes = estMinutes
                )
            )
            cur = nextEnd
            if (cur >= totalPages) break
        }

        return ProposedReadingPlan(
            bookId = book.id,
            bookTitle = book.title,
            targetDays = days,
            pagesPerDay = pagesPerDay,
            remainingPages = remainingPages,
            currentPage = startPage,
            totalPages = totalPages,
            schedule = schedule
        )
    }
}

package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reading_sessions")
data class ReadingSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val bookId: Long,
    val startedAt: Long = System.currentTimeMillis(),
    val endedAt: Long = System.currentTimeMillis(),
    val startPage: Int = 1,
    val endPage: Int = 1,
    val duration: Long = 0 // duration in seconds
) {
    val durationMinutes: Int get() = (duration / 60).toInt()
    val pagesRead: Int get() = (endPage - startPage).coerceAtLeast(0)
    val startTime: Long get() = startedAt
    val endTime: Long get() = endedAt
}


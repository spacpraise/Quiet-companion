package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val category: String = "general", // "prayer", "reading", "study", "focus", "general"
    val date: String = "Today", // "Today", "Tomorrow", "yyyy-MM-dd"
    val time: String = "08:00 AM",
    val duration: Int = 30, // in minutes
    val completed: Boolean = false,
    val completedAt: Long? = null,
    val recurrence: String = "Once", // "Once", "Daily", "Mon–Fri", "Weekly", "Monthly"
    val hasNotification: Boolean = true,
    val hasAlarm: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

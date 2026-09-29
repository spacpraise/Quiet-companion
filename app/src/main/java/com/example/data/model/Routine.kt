package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "routines")
data class Routine(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val type: String, // "prayer", "reading", "study", "focus", "winddown", "health"
    val time: String,
    val days: String = "Every day", // "Every day", "Monday–Friday", "Weekly", "Monthly", "Custom"
    val duration: Int = 30, // in minutes
    val enabled: Boolean = true,
    val isCompletedToday: Boolean = false,
    val lastCompletedDate: String = "",
    val gentleChime: Boolean = true,
    val hasAlarm: Boolean = false
)

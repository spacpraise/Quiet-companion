package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notifications")
data class Notification(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: String, // "reading", "routine", "milestones", "system"
    val title: String,
    val body: String,
    val createdAt: Long = System.currentTimeMillis(),
    val read: Boolean = false,
    val action: String = "" // "RESUME_BOOK", "START_ROUTINE", etc.
)

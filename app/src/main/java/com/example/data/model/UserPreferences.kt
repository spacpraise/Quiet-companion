package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_preferences")
data class UserPreferences(
    @PrimaryKey
    val id: Int = 1,
    val theme: String = "Linen",
    val haptics: Boolean = true,
    val notifications: Boolean = true,
    val quietHours: String = "22:00 - 06:00",
    val readingPreferences: String = "16sp;Linen;Vertical",
    val profileName: String = "Quiet Reader",
    val profileBio: String = "Local Profile",
    val wakeTime: String = "06:00 AM",
    val bedTime: String = "10:00 PM"
)

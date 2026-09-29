package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alarms")
data class Alarm(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val taskId: Long? = null,
    val time: String,
    val sound: String = "gentle_bell",
    val vibration: Boolean = true,
    val enabled: Boolean = true
)

package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "intercessions")
data class IntercessionItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val number: Int,
    val title: String,
    val prayerText: String,
    val scriptureRef: String,
    val scriptureText: String,
    val isPrayedToday: Boolean = false,
    val notes: String = ""
)

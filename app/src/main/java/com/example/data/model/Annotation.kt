package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "annotations")
data class Annotation(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val bookId: Long,
    val page: Int,
    val position: Float = 0f, // vertical offset percentage 0.0 to 1.0
    val type: String, // "HIGHLIGHT", "BOOKMARK", "NOTE", "IDEA", "QUESTION"
    val content: String,
    val quoteSnippet: String = "",
    val color: String = "Default",
    val createdAt: Long = System.currentTimeMillis()
)

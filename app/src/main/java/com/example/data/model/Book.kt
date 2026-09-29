package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "books")
data class Book(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val author: String = "Unknown",
    val fileUri: String = "", // Persistent absolute path to local PDF file
    val coverUri: String = "", // Persistent path to generated thumbnail PNG
    val totalPages: Int = 1,
    val currentPage: Int = 1,
    val scrollPosition: Float = 0f, // Scroll percentage (0.0 to 1.0)
    val zoomLevel: Float = 1.0f, // e.g. 1.0f to 3.0f
    val progress: Float = 0f, // Reading progress percentage (0.0 to 1.0)
    val lastReadAt: Long = System.currentTimeMillis(),
    val dateAdded: Long = System.currentTimeMillis(),
    val status: String = "READING" // "READING", "COMPLETED", "UNREAD", "PAUSED"
)

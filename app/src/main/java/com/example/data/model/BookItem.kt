package com.example.data.model

data class BookItem(
    val id: Long = 0,
    val title: String,
    val author: String,
    val totalPages: Int,
    val currentPage: Int,
    val status: String = "READING", // "READING", "UNREAD", "PAUSED", "COMPLETED"
    val coverDrawableName: String? = null,
    val lastRead: String = "Yesterday 8:45 PM",
    val focusHoursFormatted: String = "6h 40m",
    val sessionsLogged: Int = 14,
    val currentChapter: String = "Chapter 16: The Cardinal Rule of Behavior Change",
    val fileUri: String = "",
    val coverUri: String = "",
    val scrollPosition: Float = 0f,
    val zoomLevel: Float = 1.0f
) {
    val progressPercent: Int
        get() = if (totalPages > 0) ((currentPage.toFloat() / totalPages) * 100).toInt().coerceIn(0, 100) else 0

    val pagesLeft: Int
        get() = (totalPages - currentPage).coerceAtLeast(0)
}

fun Book.toBookItem(): BookItem {
    return BookItem(
        id = id,
        title = title,
        author = author,
        totalPages = totalPages,
        currentPage = currentPage,
        status = status,
        lastRead = if (lastReadAt > 0) "Page $currentPage" else "Unread",
        focusHoursFormatted = "${(progress * 10).toInt()}h ${(progress * 60 % 60).toInt()}m",
        sessionsLogged = if (progress > 0) ((progress * 15).toInt() + 1) else 0,
        currentChapter = "Page $currentPage of $totalPages",
        fileUri = fileUri,
        coverUri = coverUri,
        scrollPosition = scrollPosition,
        zoomLevel = zoomLevel
    )
}

fun BookItem.toBook(): Book {
    return Book(
        id = id,
        title = title,
        author = author,
        fileUri = fileUri,
        coverUri = coverUri,
        totalPages = totalPages,
        currentPage = currentPage,
        scrollPosition = scrollPosition,
        zoomLevel = zoomLevel,
        progress = if (totalPages > 0) currentPage.toFloat() / totalPages else 0f,
        lastReadAt = System.currentTimeMillis(),
        dateAdded = System.currentTimeMillis(),
        status = status
    )
}

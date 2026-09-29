package com.example.data.model

data class HighlightItem(
    val id: Long = 0,
    val bookTitle: String,
    val quote: String,
    val pageNumber: Int,
    val chapter: String = "",
    val isNote: Boolean = false,
    val personalNote: String? = null,
    val dateAdded: String = "Page 1",
    val color: String = "Default"
)

fun Annotation.toHighlightItem(bookTitle: String = ""): HighlightItem {
    val isNoteType = type != "HIGHLIGHT"
    val displayQuote = if (content.isNotBlank()) content else quoteSnippet
    return HighlightItem(
        id = id,
        bookTitle = bookTitle,
        quote = displayQuote,
        pageNumber = page,
        chapter = "Page $page · $type",
        isNote = isNoteType,
        personalNote = if (isNoteType) content else null,
        dateAdded = "Page $page · $type",
        color = color
    )
}

fun HighlightItem.toAnnotation(bookId: Long): Annotation {
    return Annotation(
        id = id,
        bookId = bookId,
        page = pageNumber,
        position = 0f,
        type = if (isNote) "NOTE" else "HIGHLIGHT",
        content = if (isNote) (personalNote ?: quote) else quote,
        color = color,
        createdAt = System.currentTimeMillis()
    )
}

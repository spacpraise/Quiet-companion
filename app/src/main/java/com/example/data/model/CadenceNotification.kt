package com.example.data.model

data class CadenceNotification(
    val id: Long = 0,
    val category: String = "reading", // "reading", "routine", "milestones", "reflection"
    val title: String,
    val description: String,
    val timestampFormatted: String = "Just now",
    val isRead: Boolean = false,
    val actionType: String? = null, // "RESUME_BOOK", "START_ROUTINE", "VIEW_REFLECTION"
    val actionLabel: String? = null,
    val targetData: String? = null
)

fun Notification.toCadenceNotification(): CadenceNotification {
    return CadenceNotification(
        id = id,
        category = type,
        title = title,
        description = body,
        timestampFormatted = "Recent",
        isRead = read,
        actionType = action.ifEmpty { null },
        actionLabel = if (action.isNotEmpty()) "Open" else null,
        targetData = null
    )
}

fun CadenceNotification.toNotification(): Notification {
    return Notification(
        id = id,
        type = category,
        title = title,
        body = description,
        createdAt = System.currentTimeMillis(),
        read = isRead,
        action = actionType ?: ""
    )
}

package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Timelapse
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Book
import com.example.data.model.toBookItem
import com.example.ui.CompanionViewModel
import com.example.ui.Screen
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.PetalDot
import com.example.ui.components.QuietTopBar
import com.example.ui.theme.BorderOutline
import com.example.ui.theme.CanvasSurface
import com.example.ui.theme.InkBlack
import com.example.ui.theme.InkWhite
import com.example.ui.theme.PetalAccent
import com.example.ui.theme.SecondaryContainer
import com.example.ui.theme.SurfaceContainer
import com.example.ui.theme.SurfaceContainerHigh
import com.example.ui.theme.SurfaceContainerLow
import com.example.ui.theme.SurfaceContainerLowest
import com.example.ui.theme.TextOutline
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun InsightsScreen(
    viewModel: CompanionViewModel,
    onNavigate: (Screen) -> Unit
) {
    val rawBooks by viewModel.rawBooks.collectAsState()
    val rawSessions by viewModel.rawSessions.collectAsState()
    val tasks by viewModel.tasks.collectAsState()
    val rawRoutines by viewModel.rawRoutines.collectAsState()
    val currentScreen by viewModel.currentScreen.collectAsState()

    // Real Data Calculations from Room Database
    val insights = remember(rawBooks, rawSessions, tasks, rawRoutines) {
        val cal = Calendar.getInstance()
        cal.firstDayOfWeek = Calendar.MONDAY
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val startOfWeekMillis = cal.timeInMillis

        val weekSessions = rawSessions.filter { it.startedAt >= startOfWeekMillis }

        // Determine which of the 7 days (M, T, W, T, F, S, S) have recorded sessions
        val weekDaysActive = BooleanArray(7) { false }
        for (s in weekSessions) {
            val sCal = Calendar.getInstance().apply { timeInMillis = s.startedAt }
            val dayOfWeek = sCal.get(Calendar.DAY_OF_WEEK)
            val index = when (dayOfWeek) {
                Calendar.MONDAY -> 0
                Calendar.TUESDAY -> 1
                Calendar.WEDNESDAY -> 2
                Calendar.THURSDAY -> 3
                Calendar.FRIDAY -> 4
                Calendar.SATURDAY -> 5
                Calendar.SUNDAY -> 6
                else -> 0
            }
            weekDaysActive[index] = true
        }

        // Reading days this week
        val readingDaysThisWeek = weekDaysActive.count { it }

        // Total Reading Time
        val totalSeconds = rawSessions.sumOf { it.duration }
        val weekSeconds = weekSessions.sumOf { it.duration }
        val totalTimeFormatted = formatDuration(totalSeconds)
        val weekTimeFormatted = formatDuration(weekSeconds)

        // Total Pages Read
        val sessionPages = rawSessions.sumOf { it.pagesRead }
        val bookPagesSum = rawBooks.sumOf { it.currentPage }
        val totalPagesRead = if (sessionPages > 0) sessionPages else bookPagesSum

        // Reading Sessions Count
        val totalSessionsCount = rawSessions.size

        // Currently Reading Books (status == READING or uncompleted)
        val currentlyReading = rawBooks.filter {
            it.status == "READING" || (it.currentPage < it.totalPages && it.status != "COMPLETED")
        }

        // Completed Books
        val completedBooks = rawBooks.filter {
            it.status == "COMPLETED" || (it.currentPage >= it.totalPages && it.totalPages > 0)
        }

        // Routine Consistency Percentage
        val totalTasks = tasks.size
        val completedTasks = tasks.count { it.completed }
        val consistencyPercent = if (totalTasks > 0) {
            ((completedTasks.toFloat() / totalTasks.toFloat()) * 100).toInt().coerceIn(0, 100)
        } else if (rawRoutines.isNotEmpty()) {
            ((rawRoutines.count { it.isCompletedToday }.toFloat() / rawRoutines.size.toFloat()) * 100).toInt().coerceIn(0, 100)
        } else {
            100
        }

        InsightsData(
            readingDaysThisWeek = readingDaysThisWeek,
            weekDaysActive = weekDaysActive.toList(),
            totalReadingTimeFormatted = totalTimeFormatted,
            weekReadingTimeFormatted = weekTimeFormatted,
            totalPagesRead = totalPagesRead,
            totalSessionsCount = totalSessionsCount,
            currentlyReadingBooks = currentlyReading,
            completedBooks = completedBooks,
            routineConsistencyPercent = consistencyPercent,
            completedTasksCount = completedTasks,
            totalTasksCount = totalTasks
        )
    }

    Box(modifier = Modifier.fillMaxSize().background(CanvasSurface)) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 80.dp)
        ) {
            // Top Bar
            item {
                QuietTopBar(
                    title = "Insights",
                    subtitle = "Calculated from your stored sanctuary data",
                    onUserClick = { onNavigate(Screen.SETTINGS) }
                )
            }

            // Key Metrics Summary Cards
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Reading Overview Card
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = SurfaceContainerLowest,
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, BorderOutline.copy(alpha = 0.45f), RoundedCornerShape(18.dp))
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "READING METRICS",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextOutline,
                                        letterSpacing = 1.sp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    PetalDot(size = 5.dp)
                                }
                                Text(
                                    text = "${insights.totalSessionsCount} sessions logged",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // 4 Key Insight Stat Boxes (Real Data)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                InsightStatCard(
                                    modifier = Modifier.weight(1f),
                                    icon = Icons.Default.DateRange,
                                    value = "${insights.readingDaysThisWeek}d",
                                    label = "Days this week"
                                )
                                InsightStatCard(
                                    modifier = Modifier.weight(1f),
                                    icon = Icons.Default.Timelapse,
                                    value = insights.totalReadingTimeFormatted,
                                    label = "Reading time"
                                )
                                InsightStatCard(
                                    modifier = Modifier.weight(1f),
                                    icon = Icons.AutoMirrored.Filled.MenuBook,
                                    value = "${insights.totalPagesRead}",
                                    label = "Pages read"
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Simple Minimal 7-Day Visual Indicator (Real Stored Data)
                            Text(
                                text = "THIS WEEK'S ACTIVE CADENCE",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextOutline,
                                fontSize = 10.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            MinimalWeekIndicator(weekDaysActive = insights.weekDaysActive)
                        }
                    }

                    // Routine Consistency Card (Real Data)
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = SurfaceContainerLowest,
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, BorderOutline.copy(alpha = 0.45f), RoundedCornerShape(18.dp))
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "ROUTINE CONSISTENCY",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextOutline,
                                        letterSpacing = 1.sp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    PetalDot(size = 5.dp)
                                }
                                Text(
                                    text = "${insights.completedTasksCount}/${insights.totalTasksCount} tasks kept",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextOutline
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                verticalAlignment = Alignment.Bottom,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "${insights.routineConsistencyPercent}%",
                                    style = MaterialTheme.typography.displaySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "task completion index",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary,
                                    modifier = Modifier.padding(bottom = 6.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Minimal Progress Line
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(CircleShape)
                                    .background(SurfaceContainerHigh)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(insights.routineConsistencyPercent / 100f)
                                        .height(6.dp)
                                        .background(InkBlack)
                                )
                            }
                        }
                    }

                    // Currently Reading Section (Real Stored Books)
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = SurfaceContainerLowest,
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, BorderOutline.copy(alpha = 0.45f), RoundedCornerShape(18.dp))
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "CURRENTLY READING (${insights.currentlyReadingBooks.size})",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextOutline,
                                        letterSpacing = 1.sp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    PetalDot(size = 5.dp)
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            if (insights.currentlyReadingBooks.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No books currently in reading.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextSecondary
                                    )
                                }
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    insights.currentlyReadingBooks.forEach { book ->
                                        CurrentlyReadingItem(
                                            book = book,
                                            onResume = {
                                                viewModel.openReaderForBook(book.toBookItem())
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Completed Books Section (Real Stored Books)
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = SurfaceContainerLowest,
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, BorderOutline.copy(alpha = 0.45f), RoundedCornerShape(18.dp))
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "COMPLETED BOOKS (${insights.completedBooks.size})",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextOutline,
                                        letterSpacing = 1.sp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    PetalDot(size = 5.dp)
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            if (insights.completedBooks.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Completed books will appear here once finished.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextSecondary
                                    )
                                }
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    insights.completedBooks.forEach { book ->
                                        CompletedBookItem(
                                            book = book,
                                            onReRead = {
                                                viewModel.reReadBook(book.toBookItem())
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

private data class InsightsData(
    val readingDaysThisWeek: Int,
    val weekDaysActive: List<Boolean>,
    val totalReadingTimeFormatted: String,
    val weekReadingTimeFormatted: String,
    val totalPagesRead: Int,
    val totalSessionsCount: Int,
    val currentlyReadingBooks: List<Book>,
    val completedBooks: List<Book>,
    val routineConsistencyPercent: Int,
    val completedTasksCount: Int,
    val totalTasksCount: Int
)

private fun formatDuration(seconds: Long): String {
    if (seconds <= 0) return "0m"
    val hrs = seconds / 3600
    val mins = (seconds % 3600) / 60
    return when {
        hrs > 0 && mins > 0 -> "${hrs}h ${mins}m"
        hrs > 0 -> "${hrs}h"
        else -> "${mins.coerceAtLeast(1)}m"
    }
}

@Composable
private fun InsightStatCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    value: String,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceContainerLow)
            .padding(vertical = 12.dp, horizontal = 6.dp)
    ) {
        Icon(icon, contentDescription = null, tint = InkBlack, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextOutline,
            fontSize = 10.sp,
            maxLines = 1
        )
    }
}

@Composable
private fun MinimalWeekIndicator(weekDaysActive: List<Boolean>) {
    val dayLabels = listOf("M", "T", "W", "T", "F", "S", "S")

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        dayLabels.forEachIndexed { idx, label ->
            val isActive = weekDaysActive.getOrElse(idx) { false }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(if (isActive) InkBlack else SurfaceContainerLow)
                        .border(
                            1.dp,
                            if (isActive) InkBlack else BorderOutline.copy(alpha = 0.4f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        fontSize = 11.sp,
                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                        color = if (isActive) InkWhite else TextOutline
                    )
                }
            }
        }
    }
}

@Composable
private fun CurrentlyReadingItem(
    book: Book,
    onResume: () -> Unit
) {
    val progressPercent = if (book.totalPages > 0) ((book.currentPage.toFloat() / book.totalPages.toFloat()) * 100).toInt().coerceIn(0, 100) else 0

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = SurfaceContainerLow,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onResume() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = book.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "p. ${book.currentPage} of ${book.totalPages} · $progressPercent% read",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }

            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(InkBlack)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Resume", tint = InkWhite, modifier = Modifier.size(12.dp))
                    Text("Resume", style = MaterialTheme.typography.labelSmall, color = InkWhite, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun CompletedBookItem(
    book: Book,
    onReRead: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = SurfaceContainerLow,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = InkBlack, modifier = Modifier.size(18.dp))
                Column {
                    Text(
                        text = book.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${book.totalPages} pages · Finished",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextOutline
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(SurfaceContainerLowest)
                    .border(1.dp, BorderOutline.copy(alpha = 0.5f), CircleShape)
                    .clickable { onReRead() }
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Text(
                    text = "Read again",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextPrimary
                )
            }
        }
    }
}

package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.SelfImprovement
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.RoutineItem
import com.example.ui.CompanionViewModel
import com.example.ui.Screen
import com.example.ui.components.PetalDot
import com.example.ui.components.QuietTopBar
import com.example.ui.dialogs.AddEditRoutineDialog
import com.example.ui.theme.BorderOutline
import com.example.ui.theme.CanvasSurface
import com.example.ui.theme.InkBlack
import com.example.ui.theme.InkWhite
import com.example.ui.theme.PetalAccent
import com.example.ui.theme.SecondaryContainer
import com.example.ui.theme.SurfaceContainer
import com.example.ui.theme.SurfaceContainerHigh
import com.example.ui.theme.SurfaceContainerHighest
import com.example.ui.theme.SurfaceContainerLow
import com.example.ui.theme.SurfaceContainerLowest
import com.example.ui.theme.TextOutline
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun TodayScreen(
    viewModel: CompanionViewModel,
    onNavigate: (Screen) -> Unit
) {
    val routines by viewModel.routines.collectAsState()
    val tasks by viewModel.tasks.collectAsState()
    val books by viewModel.books.collectAsState()
    val intercessions by viewModel.intercessions.collectAsState()
    val isTimerActive by viewModel.isSessionTimerActive.collectAsState()
    val remainingSeconds by viewModel.sessionRemainingSeconds.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var editingRoutine by remember { mutableStateOf<RoutineItem?>(null) }
    var activeTaskMenu by remember { mutableStateOf<com.example.data.model.Task?>(null) }
    var isFabMenuOpen by remember { mutableStateOf(false) }

    val activeBook = books.firstOrNull { it.status == "READING" } ?: books.firstOrNull()
    val activeRoutine = routines.firstOrNull { !it.isCompleted } ?: routines.firstOrNull()

    val currentDateStr = remember {
        java.time.LocalDate.now().format(
            java.time.format.DateTimeFormatter.ofPattern("EEEE, MMMM d")
        ).uppercase()
    }

    val greetingStr = remember {
        val hour = java.time.LocalTime.now().hour
        when {
            hour < 12 -> "Good morning"
            hour < 17 -> "Good afternoon"
            else -> "Good evening"
        }
    }

    val formattedRemaining = remember(remainingSeconds) {
        val m = remainingSeconds / 60
        val s = remainingSeconds % 60
        String.format("%02d:%02d", m, s)
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
                    title = "Today",
                    subtitle = null,
                    onUserClick = { onNavigate(Screen.SETTINGS) }
                )
            }

            // Top Greeting & Cadence status
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = currentDateStr,
                            style = MaterialTheme.typography.labelSmall,
                            color = TextOutline,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = greetingStr,
                            style = MaterialTheme.typography.headlineLarge,
                            color = TextPrimary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(SurfaceContainerLow)
                            .clickable { onNavigate(Screen.INSIGHTS) }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            PetalDot(size = 6.dp)
                            Text(
                                text = "QUIET CADENCE",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Sunrise Sanctuary Hero Banner
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .height(130.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(SurfaceContainerLow)
                        .border(1.dp, BorderOutline.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                ) {
                    val bannerRes = com.example.R.drawable.sanctuary_dawn_banner_1790510076835
                    Image(
                        painter = painterResource(id = bannerRes),
                        contentDescription = "Sunrise quiet sanctuary",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        CanvasSurface.copy(alpha = 0.5f),
                                        CanvasSurface.copy(alpha = 0.9f)
                                    )
                                )
                            )
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.WbTwilight,
                                contentDescription = null,
                                tint = InkBlack,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Sunrise period • 30m uninterrupted",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = TextPrimary
                            )
                        }
                        Text(
                            text = "SANCTUARY",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextOutline,
                            letterSpacing = 1.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Current Book / Catch Up Routine Card (Dynamic, no hardcoded Bible or time)
            item {
                val currentBook = books.firstOrNull { it.status == "READING" } ?: books.firstOrNull()
                val catchUpRoutine = routines.firstOrNull { !it.isCompleted } ?: routines.firstOrNull()

                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = SurfaceContainerLowest,
                    shadowElevation = 2.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .border(1.dp, BorderOutline.copy(alpha = 0.5f), RoundedCornerShape(18.dp))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(SurfaceContainerLow)
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    PetalDot(size = 5.dp)
                                    Text(
                                        text = if (isTimerActive) "SESSION ACTIVE" else if (currentBook != null) "CURRENT READING" else if (catchUpRoutine != null) "CATCH UP ROUTINE" else "QUIET FOCUS",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                }
                            }
                            Text(
                                text = if (currentBook != null) "Page ${currentBook.currentPage} / ${currentBook.totalPages}" else catchUpRoutine?.time ?: "Today",
                                style = MaterialTheme.typography.labelMedium,
                                color = TextOutline
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if (currentBook != null) {
                            Text(
                                text = currentBook.title,
                                style = MaterialTheme.typography.headlineLarge,
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${currentBook.author} • ${currentBook.pagesLeft} pages remaining",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        } else if (catchUpRoutine != null) {
                            Text(
                                text = catchUpRoutine.title,
                                style = MaterialTheme.typography.headlineLarge,
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${catchUpRoutine.category} • ${catchUpRoutine.durationMinutes} min • ${catchUpRoutine.days}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        } else {
                            Text(
                                text = "Quiet Sanctuary",
                                style = MaterialTheme.typography.headlineLarge,
                                color = TextPrimary
                            )
                            Text(
                                text = "Add a book or routine to anchor your daily focus.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Block detail bar with timer
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(SurfaceContainerLow)
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.HourglassTop,
                                    contentDescription = null,
                                    tint = InkBlack,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = if (isTimerActive) "$formattedRemaining remaining" else if (currentBook != null) "${currentBook.progressPercent}% completed" else "30 min intentional block",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Text(
                                text = if (currentBook != null) "ACTIVE BOOK" else if (catchUpRoutine != null) catchUpRoutine.time else "INTENTIONAL",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextOutline
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Actions
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .clip(CircleShape)
                                    .background(if (isTimerActive) SurfaceContainerHigh else InkBlack)
                                    .clickable {
                                        if (currentBook != null) {
                                            viewModel.openReaderForBook(currentBook)
                                        } else if (catchUpRoutine != null) {
                                            viewModel.toggleRoutineCompleted(catchUpRoutine)
                                        } else {
                                            viewModel.toggleSessionTimer()
                                        }
                                    }
                                    .testTag("start_session_button"),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isTimerActive) Icons.Default.Stop else Icons.Default.PlayArrow,
                                        contentDescription = null,
                                        tint = if (isTimerActive) TextPrimary else InkWhite,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = if (isTimerActive) "Pause Contemplation" else if (currentBook != null) "Resume Reading" else if (catchUpRoutine != null) "Mark Routine Done" else "Start Session",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = if (isTimerActive) TextPrimary else InkWhite,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(SurfaceContainerLow)
                                    .clickable { onNavigate(Screen.LIBRARY) }
                                    .testTag("open_library_button"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Book,
                                    contentDescription = "Library",
                                    tint = TextPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Continue Reading Active Book Card
            if (activeBook != null) {
                item {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = SurfaceContainerLowest,
                        shadowElevation = 1.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .border(1.dp, BorderOutline.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                            .clickable {
                                viewModel.openReaderForBook(activeBook)
                            }
                            .testTag("today_continue_reading_card")
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    PetalDot(size = 6.dp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Continue Reading",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextPrimary
                                    )
                                }
                                Text(
                                    text = "Page ${activeBook.currentPage} / ${activeBook.totalPages}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextOutline
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = activeBook.title,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "${activeBook.author} • ${activeBook.pagesLeft} pages remaining",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextOutline
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Progress Bar
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(CircleShape)
                                    .background(SurfaceContainerHigh)
                            ) {
                                val frac = if (activeBook.totalPages > 0) (activeBook.currentPage.toFloat() / activeBook.totalPages).coerceIn(0f, 1f) else 0f
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(frac)
                                        .height(4.dp)
                                        .background(InkBlack)
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Restores exact saved location",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextOutline
                                )
                                Button(
                                    onClick = { viewModel.openReaderForBook(activeBook) },
                                    colors = ButtonDefaults.buttonColors(containerColor = InkBlack),
                                    shape = CircleShape,
                                    modifier = Modifier.height(36.dp).testTag("today_resume_button")
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp), tint = InkWhite)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Resume", style = MaterialTheme.typography.labelSmall, color = InkWhite)
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }
            }

            // Spiritual Intercessions Quick Module (User Prompt Brief)
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = SurfaceContainerLowest,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .border(1.dp, BorderOutline.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                PetalDot(size = 6.dp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Intercession Sanctuary",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                            }
                            Text(
                                text = "PRAYER CADENCE",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextOutline
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Operation Compel Them to Come • 4 Guided Intercessions",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        intercessions.take(2).forEach { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(SurfaceContainerLow)
                                    .clickable { onNavigate(Screen.INTERCESSIONS) }
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Intercession ${item.number}: ${item.scriptureRef}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = item.prayerText,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (item.isPrayedToday) InkBlack else SurfaceContainerHigh)
                                        .clickable { viewModel.toggleIntercessionPrayed(item) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (item.isPrayedToday) {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = null,
                                            tint = InkWhite,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Pray all 4 intercessions in Sanctuary →",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = InkBlack,
                            modifier = Modifier
                                .clickable { onNavigate(Screen.INTERCESSIONS) }
                                .padding(vertical = 4.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Quiet Thought / Micro reflection
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(SurfaceContainerLow)
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    PetalDot(size = 6.dp, modifier = Modifier.padding(top = 6.dp))
                    Text(
                        text = "\"Peace is not the absence of trouble, but the quiet rhythm within it.\"",
                        style = MaterialTheme.typography.bodyMedium,
                        fontStyle = FontStyle.Italic,
                        color = TextPrimary,
                        lineHeight = 22.sp
                    )
                }
                Spacer(modifier = Modifier.height(18.dp))
            }

            // Today's Schedule Timeline Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Today's Schedule",
                        style = MaterialTheme.typography.headlineSmall,
                        color = TextPrimary
                    )
                    Text(
                        text = "${routines.count { it.isCompleted }} of ${routines.size} completed",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextOutline
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Timeline Items
            items(routines.size) { index ->
                val routine = routines[index]
                TimelineCard(
                    routine = routine,
                    isFirst = index == 0,
                    isLast = index == routines.size - 1,
                    onToggleCheck = { viewModel.toggleRoutineCompleted(routine) },
                    onEdit = { editingRoutine = routine },
                    onBookClick = {
                        activeBook?.let { viewModel.openReaderForBook(it) }
                    }
                )
            }

            // Today's Intentional Tasks Section (Room-Persisted)
            if (tasks.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(20.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            PetalDot(size = 6.dp)
                            Text(
                                text = "Today's Focus Tasks",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                        Text(
                            text = "${tasks.count { it.completed }} of ${tasks.size} done",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextOutline
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                items(tasks.size) { index ->
                    val task = tasks[index]
                    TodayTaskCard(
                        task = task,
                        onToggle = { viewModel.toggleTaskCompleted(task) },
                        onMore = { activeTaskMenu = task },
                        onSnooze = { viewModel.snoozeTask(task, 15) },
                        onPostpone = { viewModel.postponeTask(task, "Tomorrow") }
                    )
                }
            }

            // Quiet pace footer note
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Quiet pace maintained for 6 consecutive days",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextOutline
                    )
                }
            }
        }

        // Floating Action Button with Menu
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 90.dp, end = 20.dp),
            horizontalAlignment = Alignment.End
        ) {
            AnimatedVisibility(
                visible = isFabMenuOpen,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    FabSubItem(
                        icon = Icons.Outlined.Book,
                        label = "Reading Session",
                        onClick = {
                            isFabMenuOpen = false
                            activeBook?.let { viewModel.openReaderForBook(it) }
                        }
                    )
                    FabSubItem(
                        icon = Icons.Outlined.Checklist,
                        label = "Routine Anchor",
                        onClick = {
                            isFabMenuOpen = false
                            onNavigate(Screen.TASK_CREATION)
                        }
                    )
                    FabSubItem(
                        icon = Icons.Outlined.EditNote,
                        label = "Reflection",
                        onClick = {
                            isFabMenuOpen = false
                            onNavigate(Screen.COMPANION)
                        }
                    )
                }
            }

            val rotation by animateFloatAsState(if (isFabMenuOpen) 45f else 0f, label = "fab_rotation")
            FloatingActionButton(
                onClick = { isFabMenuOpen = !isFabMenuOpen },
                containerColor = InkBlack,
                contentColor = InkWhite,
                shape = CircleShape,
                modifier = Modifier
                    .size(54.dp)
                    .testTag("today_fab")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Item",
                    modifier = Modifier.rotate(rotation)
                )
            }
        }
    }

    if (showAddDialog) {
        AddEditRoutineDialog(
            initialItem = null,
            onDismiss = { showAddDialog = false },
            onSave = { title, category, time, duration, days, isRecurring ->
                viewModel.addRoutine(title, category, time, duration, days, isRecurring)
            }
        )
    }

    editingRoutine?.let { routine ->
        AddEditRoutineDialog(
            initialItem = routine,
            onDismiss = { editingRoutine = null },
            onSave = { title, category, time, duration, days, isRecurring ->
                viewModel.updateRoutine(
                    routine.copy(
                        title = title,
                        category = category,
                        time = time,
                        durationMinutes = duration,
                        days = days,
                        isRecurring = isRecurring
                    )
                )
            },
            onDelete = {
                viewModel.deleteRoutine(routine)
            }
        )
    }

    // Task Action / Options Dialog (Complete, Snooze, Postpone, Delete)
    activeTaskMenu?.let { task ->
        AlertDialog(
            onDismissRequest = { activeTaskMenu = null },
            title = {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "${task.time} • ${task.duration} min • ${task.recurrence}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextOutline
                    )
                    if (task.description.isNotBlank()) {
                        Text(
                            text = task.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Actions list
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = SurfaceContainerLow,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.toggleTaskCompleted(task)
                                activeTaskMenu = null
                            }
                    ) {
                        Text(
                            text = if (task.completed) "Mark as Incomplete" else "Mark as Complete",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary,
                            modifier = Modifier.padding(12.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = SurfaceContainerLow,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.snoozeTask(task, 15)
                                activeTaskMenu = null
                            }
                    ) {
                        Text(
                            text = "Snooze 15 Minutes",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary,
                            modifier = Modifier.padding(12.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = SurfaceContainerLow,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.postponeTask(task, "Tomorrow")
                                activeTaskMenu = null
                            }
                    ) {
                        Text(
                            text = "Postpone to Tomorrow",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary,
                            modifier = Modifier.padding(12.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = SurfaceContainerLow,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.deleteTask(task)
                                activeTaskMenu = null
                            }
                    ) {
                        Text(
                            text = "Delete Task",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFBA1A1A),
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { activeTaskMenu = null }) {
                    Text("Close", color = InkBlack, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = CanvasSurface
        )
    }
}

@Composable
private fun TodayTaskCard(
    task: com.example.data.model.Task,
    onToggle: () -> Unit,
    onMore: () -> Unit,
    onSnooze: () -> Unit,
    onPostpone: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (task.completed) SurfaceContainerLow else SurfaceContainerLowest,
        shadowElevation = if (task.completed) 0.dp else 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp)
            .border(
                1.dp,
                if (task.completed) Color.Transparent else BorderOutline.copy(alpha = 0.5f),
                RoundedCornerShape(14.dp)
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Checkbox
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (task.completed) InkBlack else SurfaceContainerHigh)
                        .clickable { onToggle() }
                        .testTag("task_check_${task.id}"),
                    contentAlignment = Alignment.Center
                ) {
                    if (task.completed) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Completed",
                            tint = InkWhite,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onMore() }
                ) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (task.completed) TextOutline else TextPrimary,
                        textDecoration = if (task.completed) TextDecoration.LineThrough else TextDecoration.None
                    )
                    Text(
                        text = "${task.time} • ${task.duration}m • ${task.recurrence}",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (task.completed) TextOutline else TextSecondary
                    )
                }
            }

            IconButton(onClick = onMore, modifier = Modifier.size(32.dp)) {
                Icon(
                    imageVector = Icons.Default.MoreHoriz,
                    contentDescription = "Task Actions",
                    tint = TextOutline,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}


@Composable
private fun TimelineCard(
    routine: RoutineItem,
    isFirst: Boolean,
    isLast: Boolean,
    onToggleCheck: () -> Unit,
    onEdit: () -> Unit,
    onBookClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Timeline Track & Node
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(28.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(CanvasSurface)
                    .border(
                        1.5.dp,
                        if (routine.isCompleted) TextOutline else InkBlack,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (routine.isCompleted) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = TextOutline,
                        modifier = Modifier.size(12.dp)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(InkBlack)
                    )
                }
            }
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(1.5.dp)
                        .height(68.dp)
                        .background(SurfaceContainerHighest)
                )
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Content Card
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = if (routine.isCompleted) SurfaceContainerLow else SurfaceContainerLowest,
            shadowElevation = if (routine.isCompleted) 0.dp else 1.dp,
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    1.dp,
                    if (routine.isCompleted) Color.Transparent else BorderOutline.copy(alpha = 0.5f),
                    RoundedCornerShape(14.dp)
                )
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = routine.time,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextOutline,
                        fontWeight = FontWeight.Bold
                    )
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(if (routine.isCompleted) SurfaceContainerHigh else SurfaceContainerLow)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (routine.isCompleted) "DONE" else if (routine.title.contains("Atomic", ignoreCase = true)) "EVENING READING" else "IN PROGRESS",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (routine.isCompleted) TextOutline else TextPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                if (routine.category == "reading") {
                                    onBookClick()
                                } else {
                                    onEdit()
                                }
                            }
                    ) {
                        Text(
                            text = routine.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = if (routine.isCompleted) TextOutline else TextPrimary,
                            textDecoration = if (routine.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                        )
                        Text(
                            text = routine.subtitle.ifEmpty { "${routine.durationMinutes} min • ${routine.days}" },
                            style = MaterialTheme.typography.bodySmall,
                            color = if (routine.isCompleted) TextOutline else TextSecondary
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Checkbox
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (routine.isCompleted) InkBlack else SurfaceContainerHigh)
                                .clickable { onToggleCheck() }
                                .testTag("routine_check_${routine.id}"),
                            contentAlignment = Alignment.Center
                        ) {
                            if (routine.isCompleted) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Completed",
                                    tint = InkWhite,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                            Icon(
                                imageVector = Icons.Default.MoreHoriz,
                                contentDescription = "Options",
                                tint = TextOutline,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // If this is Atomic Habits, show a delicate reading progress track
                if (routine.category == "reading" && !routine.isCompleted) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp)
                            .clip(CircleShape)
                            .background(SurfaceContainerHigh)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.68f)
                                .height(3.dp)
                                .background(InkBlack)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FabSubItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Surface(
        shape = CircleShape,
        color = SurfaceContainerLowest,
        shadowElevation = 4.dp,
        modifier = Modifier.clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = InkBlack, modifier = Modifier.size(16.dp))
            Text(text = label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Medium, color = TextPrimary)
        }
    }
}

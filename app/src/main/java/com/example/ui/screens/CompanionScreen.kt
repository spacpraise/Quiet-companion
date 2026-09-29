package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.automirrored.filled.ScheduleSend
import androidx.compose.material.icons.filled.Timelapse
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.EditCalendar
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.DailyPlanItem
import com.example.ai.ProposedDailyPlan
import com.example.ai.ProposedReadingPlan
import com.example.ai.ProposedRoutine
import com.example.data.model.CompanionMessage
import com.example.ui.CompanionViewModel
import com.example.ui.Screen
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
import com.example.ui.theme.SurfaceContainerHighest
import com.example.ui.theme.SurfaceContainerLow
import com.example.ui.theme.SurfaceContainerLowest
import com.example.ui.theme.TextOutline
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory


@Composable
fun CompanionScreen(
    viewModel: CompanionViewModel,
    onNavigate: (Screen) -> Unit
) {
    val messages by viewModel.companionMessages.collectAsState()
    val books by viewModel.books.collectAsState()
    val routines by viewModel.routines.collectAsState()
    val isDemoLoading by viewModel.isDemoLoading.collectAsState()
    val currentScreen by viewModel.currentScreen.collectAsState()

    var inputQuery by remember { mutableStateOf("") }
    var isMicActive by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size)
        }
    }

    val activeBook = books.firstOrNull { it.status == "READING" } ?: books.firstOrNull()
    val nextRoutine = routines.firstOrNull { !it.isCompleted } ?: routines.firstOrNull()

    Box(modifier = Modifier.fillMaxSize().background(CanvasSurface)) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Bar
            Surface(
                color = CanvasSurface,
                shadowElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Companion",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(SecondaryContainer)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "SYNCED",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { viewModel.clearCompanionHistory() }) {
                            Icon(Icons.Default.RestartAlt, contentDescription = "Restart", tint = TextOutline)
                        }
                        IconButton(onClick = { onNavigate(Screen.SETTINGS) }) {
                            Icon(Icons.Default.Person, contentDescription = "Settings", tint = TextPrimary)
                        }
                    }
                }
            }

            // Scrollable Content
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // Presence Subtitle
                item {
                    Text(
                        text = "Attentive to shelf and afternoon rhythms",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextOutline,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )
                }

                // Live Context Awareness Rail
                item {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = SurfaceContainerLow,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 14.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    PetalDot(size = 6.dp)
                                    Text(
                                        text = "LIVE CONTEXT",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = TextOutline,
                                        letterSpacing = 1.sp
                                    )
                                }
                                Text(
                                    text = "Updated 4m ago",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextOutline
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Active book well
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = SurfaceContainerLowest,
                                    shadowElevation = 1.dp,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable {
                                            activeBook?.let { viewModel.openReaderForBook(it) }
                                        }
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("READING", style = MaterialTheme.typography.labelSmall, color = TextOutline)
                                            if (activeBook != null) {
                                                Text("${activeBook.progressPercent}%", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = TextPrimary)
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = activeBook?.title ?: "No active book",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary,
                                            maxLines = 1
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        if (activeBook != null) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(2.5.dp)
                                                    .clip(CircleShape)
                                                    .background(SurfaceContainerHigh)
                                            ) {
                                                val frac = if (activeBook.totalPages > 0) (activeBook.currentPage.toFloat() / activeBook.totalPages).coerceIn(0f, 1f) else 0f
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth(frac)
                                                        .height(2.5.dp)
                                                        .background(InkBlack)
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text("p. ${activeBook.currentPage} of ${activeBook.totalPages}", style = MaterialTheme.typography.labelSmall, color = TextOutline)
                                        } else {
                                            Text("Shelf is empty", style = MaterialTheme.typography.labelSmall, color = TextOutline)
                                        }
                                    }
                                }

                                // Active routine well
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = SurfaceContainerLowest,
                                    shadowElevation = 1.dp,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text("NEXT UP", style = MaterialTheme.typography.labelSmall, color = TextOutline)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = nextRoutine?.title ?: "No routines",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary,
                                            maxLines = 1
                                        )
                                        if (nextRoutine != null) {
                                            Text("${nextRoutine.time} (${nextRoutine.durationMinutes}m)", style = MaterialTheme.typography.labelSmall, color = TextOutline)
                                        } else {
                                            Text("No routines yet", style = MaterialTheme.typography.labelSmall, color = TextOutline)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Chat Messages Feed
                items(messages) { message ->
                    CompanionMessageBubble(
                        message = message,
                        onAcceptPlan = { viewModel.acceptProposedPlan(message) },
                        onCancelPlan = { viewModel.cancelProposedPlan(message) },
                        onOptionSelected = { option -> viewModel.sendCompanionQuery(option) },
                        onOpenBook = {
                            activeBook?.let { viewModel.openReaderForBook(it) }
                        }
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                if (isDemoLoading) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            PetalDot(size = 8.dp)
                            Text(
                                text = "Companion is contemplating your daily rhythm...",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextOutline,
                                fontStyle = FontStyle.Italic
                            )
                        }
                    }
                }
            }

            // Suggested Inquiries Carousel
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)) {
                Text(
                    text = "NATURAL CADENCE PROMPTS",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextOutline,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val promptSuggestions = listOf(
                        "Plan tomorrow for me",
                        "Remind me to read every evening at 8",
                        "Remind me to read tomorrow",
                        "Where did I stop reading?",
                        "Check reading statistics",
                        "Show my library"
                    )
                    promptSuggestions.forEach { prompt ->
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(SurfaceContainerLowest)
                                .border(1.dp, BorderOutline.copy(alpha = 0.5f), CircleShape)
                                .clickable {
                                    viewModel.sendCompanionQuery(prompt)
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                PetalDot(size = 5.dp)
                                Text(
                                    text = prompt,
                                    fontSize = 12.sp,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            // Docked Aesthetic Input Console
            Surface(
                color = CanvasSurface,
                shadowElevation = 6.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(SurfaceContainerLowest)
                            .border(1.dp, BorderOutline.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = TextOutline,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedTextField(
                            value = inputQuery,
                            onValueChange = { inputQuery = it },
                            placeholder = {
                                Text(
                                    if (isMicActive) "Listening softly to your cadence..." else "Ask companion or tap a suggestion...",
                                    color = TextOutline,
                                    fontSize = 13.sp
                                )
                            },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("companion_input_field")
                        )

                        IconButton(onClick = {
                            isMicActive = !isMicActive
                            if (isMicActive) {
                                inputQuery = "Read Atomic Habits every night at 8 for 30 minutes"
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "Dictate",
                                tint = if (isMicActive) PetalAccent else TextOutline,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(InkBlack)
                                .clickable {
                                    if (inputQuery.isNotBlank()) {
                                        viewModel.sendCompanionQuery(inputQuery)
                                        inputQuery = ""
                                    }
                                }
                                .testTag("companion_send_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowUpward,
                                contentDescription = "Send",
                                tint = InkWhite,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        PetalDot(size = 4.dp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Deterministic schedule source of truth • Powered by Gemini Assistant",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextOutline
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CompanionMessageBubble(
    message: CompanionMessage,
    onAcceptPlan: () -> Unit,
    onCancelPlan: () -> Unit,
    onOptionSelected: (String) -> Unit,
    onOpenBook: () -> Unit
) {
    val moshi = remember { Moshi.Builder().add(KotlinJsonAdapterFactory()).build() }

    if (message.isUser) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.End
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp).copy(bottomEnd = androidx.compose.foundation.shape.CornerSize(2.dp)),
                color = SurfaceContainerHigh,
                shadowElevation = 1.dp,
                modifier = Modifier.fillMaxWidth(0.85f)
            ) {
                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary,
                    modifier = Modifier.padding(14.dp)
                )
            }
            Text(
                text = message.timestamp,
                style = MaterialTheme.typography.labelSmall,
                color = TextOutline,
                modifier = Modifier.padding(top = 2.dp, end = 4.dp)
            )
        }
    } else {
        Row(
            modifier = Modifier.fillMaxWidth(0.96f),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(InkBlack),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = InkWhite,
                    modifier = Modifier.size(13.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Surface(
                    shape = RoundedCornerShape(16.dp).copy(topStart = androidx.compose.foundation.shape.CornerSize(2.dp)),
                    color = SurfaceContainerLowest,
                    shadowElevation = 1.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = message.text,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary,
                            lineHeight = 22.sp
                        )

                        // 1. Structured Daily Plan Card
                        if (message.planType == "DAILY_PLAN" && message.planPayload != null) {
                            val plan = remember(message.planPayload) {
                                try {
                                    moshi.adapter(ProposedDailyPlan::class.java).fromJson(message.planPayload)
                                } catch (_: Exception) { null }
                            }
                            if (plan != null) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = SurfaceContainerLow,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderOutline),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                Icon(Icons.Default.CalendarToday, contentDescription = null, tint = PetalAccent, modifier = Modifier.size(16.dp))
                                                Text("PROPOSED CADENCE • ${plan.date.uppercase()}", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = TextPrimary)
                                            }
                                            if (message.planStatus == "ACCEPTED") {
                                                Text("ACCEPTED", style = MaterialTheme.typography.labelSmall, color = PetalAccent, fontWeight = FontWeight.Bold)
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        plan.items.forEach { item ->
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 4.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(SurfaceContainerLowest)
                                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(item.title, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                                                    Text("${item.time} • ${item.durationMinutes} min • ${item.category}", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                                                }
                                                if (item.hasAlarm) {
                                                    Icon(Icons.Default.Alarm, contentDescription = "Alarm", tint = TextOutline, modifier = Modifier.size(14.dp))
                                                } else if (item.hasNotification) {
                                                    Icon(Icons.Default.NotificationsActive, contentDescription = "Notification", tint = TextOutline, modifier = Modifier.size(14.dp))
                                                }
                                            }
                                        }

                                        if (message.planStatus != "ACCEPTED" && message.planStatus != "CANCELLED") {
                                            Spacer(modifier = Modifier.height(10.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Button(
                                                    onClick = onAcceptPlan,
                                                    colors = ButtonDefaults.buttonColors(containerColor = InkBlack),
                                                    shape = RoundedCornerShape(8.dp),
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Text("Accept Schedule", color = InkWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                }
                                                OutlinedButton(
                                                    onClick = onCancelPlan,
                                                    shape = RoundedCornerShape(8.dp),
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Text("Cancel", color = TextPrimary, fontSize = 12.sp)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // 2. Structured Reading Plan Card
                        if (message.planType == "READING_PLAN" && message.planPayload != null) {
                            val readingPlan = remember(message.planPayload) {
                                try {
                                    moshi.adapter(ProposedReadingPlan::class.java).fromJson(message.planPayload)
                                } catch (_: Exception) { null }
                            }
                            if (readingPlan != null) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = SurfaceContainerLow,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderOutline),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                "${readingPlan.bookTitle.uppercase()} • ${readingPlan.targetDays} DAYS",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = PetalAccent
                                            )
                                            Text(
                                                "~${readingPlan.pagesPerDay} pages/day",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = TextSecondary
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        readingPlan.schedule.take(5).forEach { day ->
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 3.dp)
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(SurfaceContainerLowest)
                                                    .padding(horizontal = 8.dp, vertical = 5.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(day.dayLabel, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                                                Text("Pages ${day.startPage}–${day.endPage} (${day.pagesToRead} pp)", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                                Text("~${day.estimatedMinutes}m", style = MaterialTheme.typography.labelSmall, color = TextOutline)
                                            }
                                        }

                                        if (readingPlan.schedule.size > 5) {
                                            Text(
                                                "+ ${readingPlan.schedule.size - 5} remaining daily targets",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = TextOutline,
                                                modifier = Modifier.padding(vertical = 4.dp)
                                            )
                                        }

                                        if (message.planStatus != "ACCEPTED" && message.planStatus != "CANCELLED") {
                                            Spacer(modifier = Modifier.height(10.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Button(
                                                    onClick = onAcceptPlan,
                                                    colors = ButtonDefaults.buttonColors(containerColor = InkBlack),
                                                    shape = RoundedCornerShape(8.dp),
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Text("Adopt Reading Plan", color = InkWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                }
                                                OutlinedButton(
                                                    onClick = onCancelPlan,
                                                    shape = RoundedCornerShape(8.dp),
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Text("Cancel", color = TextPrimary, fontSize = 12.sp)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // 3. Structured Routine Proposal Card
                        if (message.planType == "ROUTINE_PROPOSAL" && message.planPayload != null) {
                            val routine = remember(message.planPayload) {
                                try {
                                    moshi.adapter(ProposedRoutine::class.java).fromJson(message.planPayload)
                                } catch (_: Exception) { null }
                            }
                            if (routine != null) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = SurfaceContainerLow,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderOutline),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text("ROUTINE PROPOSAL", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = PetalAccent)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(routine.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = TextPrimary)
                                        Text("${routine.days} at ${routine.time} • ${routine.duration} mins", style = MaterialTheme.typography.bodySmall, color = TextSecondary)

                                        if (message.planStatus != "ACCEPTED" && message.planStatus != "CANCELLED") {
                                            Spacer(modifier = Modifier.height(10.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Button(
                                                    onClick = onAcceptPlan,
                                                    colors = ButtonDefaults.buttonColors(containerColor = InkBlack),
                                                    shape = RoundedCornerShape(8.dp),
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Text("Confirm Routine", color = InkWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                }
                                                OutlinedButton(
                                                    onClick = onCancelPlan,
                                                    shape = RoundedCornerShape(8.dp),
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Text("Dismiss", color = TextPrimary, fontSize = 12.sp)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Direct Proposal Action Button if present
                        if (message.proposalTitle != null && message.planType == null) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = onAcceptPlan,
                                enabled = !message.proposalApplied,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (message.proposalApplied) SurfaceContainerHigh else InkBlack
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = if (message.proposalApplied) Icons.Default.Check else Icons.AutoMirrored.Filled.ScheduleSend,
                                        contentDescription = null,
                                        tint = if (message.proposalApplied) TextOutline else InkWhite,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = if (message.proposalApplied) "Cadence Sync Applied" else "Apply: ${message.proposalTitle}",
                                        color = if (message.proposalApplied) TextOutline else InkWhite,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(start = 4.dp)
                ) {
                    PetalDot(size = 4.dp)
                    Text("Companion synchronized", style = MaterialTheme.typography.labelSmall, color = TextOutline)
                }
            }
        }
    }
}


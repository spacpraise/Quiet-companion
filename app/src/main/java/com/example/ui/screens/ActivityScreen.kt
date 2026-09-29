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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CadenceNotification
import com.example.ui.CompanionViewModel
import com.example.ui.Screen
import com.example.ui.components.PetalDot
import com.example.ui.components.QuietTopBar
import com.example.ui.theme.BorderOutline
import com.example.ui.theme.CanvasSurface
import com.example.ui.theme.InkBlack
import com.example.ui.theme.InkWhite
import com.example.ui.theme.PetalAccent
import com.example.ui.theme.SurfaceContainer
import com.example.ui.theme.SurfaceContainerHigh
import com.example.ui.theme.SurfaceContainerLow
import com.example.ui.theme.SurfaceContainerLowest
import com.example.ui.theme.TextOutline
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun ActivityScreen(
    viewModel: CompanionViewModel,
    onNavigate: (Screen) -> Unit
) {
    val notifications by viewModel.notifications.collectAsState()
    val books by viewModel.books.collectAsState()
    val isDemoLoading by viewModel.isDemoLoading.collectAsState()
    val isDemoEmpty by viewModel.isDemoEmpty.collectAsState()
    val currentScreen by viewModel.currentScreen.collectAsState()

    var selectedFilter by remember { mutableStateOf("All") }

    val activeBook = books.firstOrNull { it.title.contains("Atomic", ignoreCase = true) } ?: books.firstOrNull()

    val filtered = remember(notifications, selectedFilter, isDemoEmpty) {
        if (isDemoEmpty) return@remember emptyList()
        when (selectedFilter) {
            "Reading" -> notifications.filter { it.category == "reading" }
            "Routine" -> notifications.filter { it.category == "routine" }
            "Milestones" -> notifications.filter { it.category == "milestones" }
            else -> notifications
        }
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
                    title = "Activity",
                    subtitle = "Quiet Notifications & Cadence Log",
                    onUserClick = { onNavigate(Screen.SETTINGS) },
                    actions = {
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(SurfaceContainerLow)
                                .clickable { viewModel.markAllNotificationsRead() }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.DoneAll, contentDescription = null, tint = TextOutline, modifier = Modifier.size(15.dp))
                                Text("Mark all read", style = MaterialTheme.typography.labelSmall, color = TextPrimary)
                            }
                        }
                    }
                )
            }

            // Category Filter Pills
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val filters = listOf(
                        "All" to notifications.size,
                        "Reading" to notifications.count { it.category == "reading" },
                        "Routine" to notifications.count { it.category == "routine" },
                        "Milestones" to notifications.count { it.category == "milestones" }
                    )
                    filters.forEach { (cat, count) ->
                        val isSelected = selectedFilter == cat
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(if (isSelected) InkBlack else SurfaceContainerLowest)
                                .border(1.dp, if (isSelected) InkBlack else BorderOutline.copy(alpha = 0.5f), CircleShape)
                                .clickable { selectedFilter = cat }
                                .padding(horizontal = 14.dp, vertical = 7.dp)
                        ) {
                            Text(
                                text = "$cat ($count)",
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (isSelected) InkWhite else TextSecondary
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Section: Today
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "TODAY",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextOutline,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "${notifications.count { !it.isRead }} unread",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextOutline
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
            }

            // Items or Loading/Empty state
            if (isDemoLoading) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        com.example.ui.components.LoadingSkeletonCard(height = 90.dp)
                        com.example.ui.components.LoadingSkeletonCard(height = 90.dp)
                    }
                }
            } else if (filtered.isEmpty()) {
                item {
                    Box(modifier = Modifier.padding(20.dp)) {
                        com.example.ui.components.EmptyStateCard(
                            title = "Sanctuary is Peaceful",
                            description = "No alerts or interruptions scheduled for this category. Your cadence is quiet and undisturbed.",
                            icon = Icons.Outlined.CheckCircle,
                            actionLabel = "Review Today's Rhythm",
                            onActionClick = { onNavigate(Screen.TODAY) }
                        )
                    }
                }
            } else {
                items(filtered) { notif ->
                    NotificationCardItem(
                        notification = notif,
                        onDismiss = { viewModel.dismissNotification(notif.id) },
                        onPrimaryAction = {
                            if (notif.actionType == "RESUME_BOOK" && activeBook != null) {
                                viewModel.openReaderForBook(activeBook)
                            } else if (notif.actionType == "START_ROUTINE") {
                                onNavigate(Screen.TODAY)
                                viewModel.toggleSessionTimer()
                            } else {
                                onNavigate(Screen.TODAY)
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }

            // Bottom quiet archive footer
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp, horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(modifier = Modifier.width(32.dp).height(1.dp).background(BorderOutline))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Notifications older than 30 days are respectfully archived to preserve a quiet mind.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextOutline,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun NotificationCardItem(
    notification: CadenceNotification,
    onDismiss: () -> Unit,
    onPrimaryAction: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (notification.isRead) SurfaceContainerLow.copy(alpha = 0.7f) else SurfaceContainerLowest,
        shadowElevation = if (notification.isRead) 0.dp else 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .border(
                1.dp,
                if (notification.isRead) Color.Transparent else BorderOutline.copy(alpha = 0.5f),
                RoundedCornerShape(16.dp)
            )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (!notification.isRead) {
                        PetalDot(size = 6.dp)
                    }
                    Text(
                        text = "${notification.category.uppercase()} • ${notification.timestampFormatted}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextOutline,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = TextOutline, modifier = Modifier.size(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = notification.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = notification.description,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                lineHeight = 20.sp
            )

            if (notification.category == "reading" && !notification.isRead) {
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

            if (notification.actionLabel != null && !notification.isRead) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(InkBlack)
                            .clickable { onPrimaryAction() }
                            .padding(horizontal = 14.dp, vertical = 7.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            if (notification.category == "reading") {
                                Icon(Icons.Outlined.AutoStories, contentDescription = null, tint = InkWhite, modifier = Modifier.size(14.dp))
                            }
                            Text(
                                text = notification.actionLabel,
                                style = MaterialTheme.typography.labelSmall,
                                color = InkWhite,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(SurfaceContainerLow)
                            .clickable { onDismiss() }
                            .padding(horizontal = 12.dp, vertical = 7.dp)
                    ) {
                        Text(
                            text = "Later",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextPrimary
                        )
                    }
                }
            }
        }
    }
}

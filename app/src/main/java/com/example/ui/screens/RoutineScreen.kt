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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.ModeStandby
import androidx.compose.material.icons.outlined.SelfImprovement
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material.icons.outlined.WbTwilight
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.RoutineItem
import com.example.ui.CompanionViewModel
import com.example.ui.Screen
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.LoadingSkeletonCard
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
import com.example.ui.theme.SurfaceContainerLowest
import com.example.ui.theme.SurfaceContainerLow
import com.example.ui.theme.TextOutline
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun RoutineScreen(
    viewModel: CompanionViewModel,
    onNavigate: (Screen) -> Unit
) {
    val routines by viewModel.routines.collectAsState()
    val isDemoLoading by viewModel.isDemoLoading.collectAsState()
    val isDemoEmpty by viewModel.isDemoEmpty.collectAsState()
    val activeFilter by viewModel.routineFilter.collectAsState()
    val currentScreen by viewModel.currentScreen.collectAsState()

    var editingRoutine by remember { mutableStateOf<RoutineItem?>(null) }
    var showEditDialog by remember { mutableStateOf(false) }

    // Filter routines
    val filteredRoutines = remember(routines, activeFilter, isDemoEmpty) {
        if (isDemoEmpty) return@remember emptyList()
        when (activeFilter) {
            "Morning" -> routines.filter { it.time.contains("AM", ignoreCase = true) || it.title.contains("morning", ignoreCase = true) || it.category == "prayer" }
            "Afternoon" -> routines.filter { it.time.contains("12:", ignoreCase = true) || it.time.contains("13:", ignoreCase = true) || it.time.contains("14:", ignoreCase = true) || it.time.contains("15:", ignoreCase = true) || it.time.contains("16:", ignoreCase = true) || it.category == "study" || it.category == "focus" }
            "Evening" -> routines.filter { it.time.contains("PM", ignoreCase = true) && !it.time.contains("12:") && !it.time.contains("13:") && !it.time.contains("14:") || it.category == "reading" }
            "Completed" -> routines.filter { it.isCompleted }
            else -> routines
        }
    }

    val totalCount = routines.size
    val completedCount = routines.count { it.isCompleted }
    val isAllCompleted = totalCount > 0 && totalCount == completedCount

    Box(modifier = Modifier.fillMaxSize().background(CanvasSurface)) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 80.dp)
        ) {
            // Top Bar
            item {
                QuietTopBar(
                    title = "Routine",
                    subtitle = "Daily rhythm & intentional cadence",
                    onUserClick = { onNavigate(Screen.SETTINGS) }
                )
            }

            // Rhythm Anchors Summary Card
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = SurfaceContainerLowest,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp)
                        .border(1.dp, BorderOutline.copy(alpha = 0.45f), RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "CADENCE HARMONY",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextOutline,
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                PetalDot(size = 5.dp)
                            }
                            Text(
                                text = "$completedCount of $totalCount completed",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isAllCompleted) InkBlack else TextSecondary
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Progress line
                        val progressFraction = if (totalCount > 0) completedCount.toFloat() / totalCount else 0f
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(CircleShape)
                                .background(SurfaceContainerHigh)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(progressFraction.coerceIn(0.04f, 1f))
                                    .height(4.dp)
                                    .background(InkBlack)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Three anchors row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            AnchorPill(icon = Icons.Outlined.WbSunny, time = "06:00 AM", label = "Dawn Anchor")
                            AnchorPill(icon = Icons.Outlined.ModeStandby, time = "13:00 PM", label = "Focus Block")
                            AnchorPill(icon = Icons.Outlined.WbTwilight, time = "21:00 PM", label = "Dusk Rest")
                        }
                    }
                }
            }

            // Cadence filter chips
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("All", "Morning", "Afternoon", "Evening", "Completed").forEach { filter ->
                        val isSelected = activeFilter == filter
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(if (isSelected) InkBlack else SurfaceContainerLow)
                                .border(
                                    1.dp,
                                    if (isSelected) InkBlack else BorderOutline.copy(alpha = 0.5f),
                                    CircleShape
                                )
                                .clickable { viewModel.setRoutineFilter(filter) }
                                .padding(horizontal = 14.dp, vertical = 7.dp)
                                .testTag("filter_$filter")
                        ) {
                            Text(
                                text = filter,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (isSelected) InkWhite else TextPrimary
                            )
                        }
                    }
                }
            }

            // 100% completion card banner
            if (isAllCompleted && !isDemoEmpty && !isDemoLoading) {
                item {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = SecondaryContainer.copy(alpha = 0.45f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 6.dp)
                            .border(1.dp, PetalAccent, RoundedCornerShape(16.dp))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(InkBlack),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = InkWhite, modifier = Modifier.size(20.dp))
                                }
                                Column {
                                    Text(
                                        text = "Cadence 100% Fulfilled",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "All intentional blocks honored today.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(InkBlack)
                                    .clickable { viewModel.triggerCompletionModal("CADENCE") }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("Reflect", color = InkWhite, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }

            // Loading skeleton state
            if (isDemoLoading) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        LoadingSkeletonCard(height = 84.dp)
                        LoadingSkeletonCard(height = 84.dp)
                        LoadingSkeletonCard(height = 84.dp)
                    }
                }
            } else if (filteredRoutines.isEmpty()) {
                // Empty state
                item {
                    Box(modifier = Modifier.padding(20.dp)) {
                        EmptyStateCard(
                            title = if (activeFilter == "All") "No routines configured" else "No $activeFilter routines",
                            description = "Cultivate an intentional rhythm by creating a morning scripture anchor or reading block.",
                            icon = Icons.Outlined.Checklist,
                            actionLabel = "+ Create Cadence Routine",
                            onActionClick = { onNavigate(Screen.TASK_CREATION) }
                        )
                    }
                }
            } else {
                // Routine item cards
                items(filteredRoutines, key = { it.id }) { routine ->
                    RoutineCard(
                        routine = routine,
                        onToggle = { viewModel.toggleRoutineCompleted(routine) },
                        onToggleEnabled = { viewModel.toggleRoutineEnabled(routine) },
                        onEdit = {
                            editingRoutine = routine
                            showEditDialog = true
                        },
                        onNavigate = onNavigate
                    )
                }
            }

            // Bottom space
            item { Spacer(modifier = Modifier.height(24.dp)) }
        }

        // Floating Action Button to create a task
        FloatingActionButton(
            onClick = { onNavigate(Screen.TASK_CREATION) },
            containerColor = InkBlack,
            contentColor = InkWhite,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 20.dp)
                .testTag("fab_create_routine")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Routine", modifier = Modifier.size(20.dp))
                Text("New Routine", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
            }
        }
    }

    // Edit dialog if requested
    if (showEditDialog && editingRoutine != null) {
        AddEditRoutineDialog(
            initialItem = editingRoutine,
            onDismiss = {
                showEditDialog = false
                editingRoutine = null
            },
            onSave = { title, category, time, duration, days, isRecurring ->
                editingRoutine?.let { current ->
                    viewModel.updateRoutine(
                        current.copy(
                            title = title,
                            category = category,
                            time = time,
                            durationMinutes = duration,
                            days = days,
                            isRecurring = isRecurring
                        )
                    )
                }
                showEditDialog = false
                editingRoutine = null
            },
            onDelete = { itemToDelete ->
                viewModel.deleteRoutine(itemToDelete)
                showEditDialog = false
                editingRoutine = null
            }
        )
    }
}

@Composable
private fun AnchorPill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    time: String,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(SurfaceContainerLow)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Icon(icon, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = time, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = TextOutline, fontSize = 9.sp)
    }
}

@Composable
private fun StateTogglePill(
    label: String,
    active: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(if (active) SecondaryContainer else SurfaceContainerLowest)
            .border(1.dp, if (active) PetalAccent else BorderOutline.copy(alpha = 0.5f), CircleShape)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (active) PetalDot(size = 5.dp)
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal,
                color = TextPrimary
            )
        }
    }
}

@Composable
private fun RoutineCard(
    routine: RoutineItem,
    onToggle: () -> Unit,
    onToggleEnabled: () -> Unit,
    onEdit: () -> Unit,
    onNavigate: (Screen) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (!routine.enabled) SurfaceContainerLow.copy(alpha = 0.6f) else SurfaceContainerLowest,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 5.dp)
            .border(
                1.dp,
                if (!routine.enabled) BorderOutline.copy(alpha = 0.2f) else BorderOutline.copy(alpha = 0.4f),
                RoundedCornerShape(16.dp)
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Time & Category badge
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (routine.enabled) SurfaceContainerLow else SurfaceContainerHigh)
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = routine.time,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (routine.enabled) TextPrimary else TextOutline
                    )
                    Text(
                        text = "${routine.durationMinutes}m",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextOutline,
                        fontSize = 10.sp
                    )
                }

                // Info
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onEdit() }
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = routine.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = if (routine.isCompleted || !routine.enabled) TextOutline else TextPrimary,
                            textDecoration = if (routine.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                        )
                        if (routine.category == "reading") {
                            Spacer(modifier = Modifier.width(4.dp))
                            PetalDot(size = 4.dp)
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = if (routine.enabled) routine.days else "${routine.days} • Paused",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextOutline,
                            fontSize = 11.sp
                        )
                        if (routine.isRecurring) {
                            Icon(Icons.Default.Repeat, contentDescription = "Recurring", tint = TextOutline, modifier = Modifier.size(12.dp))
                        }
                        if (routine.gentleChime) {
                            Icon(Icons.Default.NotificationsActive, contentDescription = "Chime", tint = TextOutline, modifier = Modifier.size(12.dp))
                        }
                    }
                }
            }

            // Action / Checkbox & Enable toggle
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (routine.isCompleted) InkBlack else SurfaceContainerHigh)
                        .clickable { onToggle() }
                        .testTag("routine_check_${routine.id}"),
                    contentAlignment = Alignment.Center
                ) {
                    if (routine.isCompleted) {
                        Icon(Icons.Default.Check, contentDescription = "Completed", tint = InkWhite, modifier = Modifier.size(18.dp))
                    }
                }

                IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.MoreHoriz, contentDescription = "Edit", tint = TextOutline, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

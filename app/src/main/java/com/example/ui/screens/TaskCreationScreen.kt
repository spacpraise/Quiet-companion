package com.example.ui.screens

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import com.example.ui.CompanionViewModel
import com.example.ui.Screen
import com.example.ui.components.PetalDot
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

@Composable
fun TaskCreationScreen(
    viewModel: CompanionViewModel,
    onNavigate: (Screen) -> Unit,
    onBack: () -> Unit
) {
    val currentScreen by viewModel.currentScreen.collectAsState()

    var title by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("reading") }
    var selectedAnchor by remember { mutableStateOf("Dawn Anchor") }
    var selectedTime by remember { mutableStateOf("06:30 AM") }
    var durationMinutes by remember { mutableStateOf(30) }
    var recurrenceDays by remember { mutableStateOf("Daily") }
    var gentleChime by remember { mutableStateOf(true) }

    val categories = listOf(
        Triple("reading", "Reading", Icons.Default.AutoStories),
        Triple("prayer", "Prayer", Icons.Default.SelfImprovement),
        Triple("study", "Study", Icons.Default.School),
        Triple("focus", "Focus", Icons.Default.Spa),
        Triple("winddown", "Wind-down", Icons.Default.Nightlight),
        Triple("health", "Respite", Icons.Default.FitnessCenter)
    )

    val templates = listOf(
        Pair("Morning Scripture Meditation", 30),
        Pair("Deep Focus Study Block", 45),
        Pair("Evening Reading & Wind-down", 30),
        Pair("Midday Respite & Walk", 20)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CanvasSurface)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 80.dp)
    ) {
        // Top Navigation Header
        Surface(color = CanvasSurface, modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("back_button")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = InkBlack
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "New Cadence Anchor",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    PetalDot(size = 6.dp)
                }

                Button(
                    onClick = {
                        if (title.isNotBlank()) {
                            viewModel.createTask(
                                title = title,
                                category = selectedCategory,
                                time = selectedTime,
                                durationMinutes = durationMinutes,
                                anchor = selectedAnchor,
                                days = recurrenceDays,
                                isRecurring = true,
                                gentleChime = gentleChime
                            )
                        } else {
                            viewModel.emitToast("Please enter a routine title")
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = InkBlack,
                        contentColor = InkWhite
                    ),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.testTag("save_task_button")
                ) {
                    Text("Save", fontWeight = FontWeight.SemiBold)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Curated Templates
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Text(
                text = "CURATED ANCHOR TEMPLATES",
                style = MaterialTheme.typography.labelSmall,
                color = TextOutline,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                templates.forEach { (tplTitle, tplDuration) ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(SurfaceContainerLowest)
                            .border(1.dp, BorderOutline.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .clickable {
                                title = tplTitle
                                durationMinutes = tplDuration
                                if (tplTitle.contains("Scripture")) {
                                    selectedCategory = "prayer"
                                    selectedAnchor = "Dawn Anchor"
                                    selectedTime = "06:00 AM"
                                } else if (tplTitle.contains("Reading")) {
                                    selectedCategory = "reading"
                                    selectedAnchor = "Dusk Rest"
                                    selectedTime = "21:00 PM"
                                } else if (tplTitle.contains("Study")) {
                                    selectedCategory = "study"
                                    selectedAnchor = "Focus Block"
                                    selectedTime = "13:00 PM"
                                }
                            }
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Column {
                            Text(text = tplTitle, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                            Text(text = "$tplDuration minutes", style = MaterialTheme.typography.labelSmall, color = TextOutline, fontSize = 10.sp)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Title Input Card
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = SurfaceContainerLowest,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .border(1.dp, BorderOutline.copy(alpha = 0.45f), RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "ROUTINE TITLE",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextOutline,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    placeholder = { Text("e.g., Deep Focus & Reading", color = TextOutline) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("task_title_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = InkBlack,
                        unfocusedBorderColor = BorderOutline,
                        focusedContainerColor = CanvasSurface,
                        unfocusedContainerColor = CanvasSurface
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Anchor Section
                Text(
                    text = "CADENCE ANCHOR",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextOutline,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Dawn Anchor", "Focus Block", "Dusk Rest").forEach { anchor ->
                        val isSelected = selectedAnchor == anchor
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) InkBlack else SurfaceContainerLow)
                                .border(1.dp, if (isSelected) InkBlack else BorderOutline.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                            .clickable { selectedAnchor = anchor }
                            .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = anchor,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (isSelected) InkWhite else TextPrimary
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Category Selection
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = SurfaceContainerLowest,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .border(1.dp, BorderOutline.copy(alpha = 0.45f), RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "INTENTION CATEGORY",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextOutline,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { (catId, catLabel, catIcon) ->
                        val isSelected = selectedCategory == catId
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(if (isSelected) InkBlack else SurfaceContainerLow)
                                .border(1.dp, if (isSelected) InkBlack else BorderOutline.copy(alpha = 0.5f), CircleShape)
                                .clickable { selectedCategory = catId }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = catIcon,
                                    contentDescription = null,
                                    tint = if (isSelected) InkWhite else TextOutline,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = catLabel,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (isSelected) InkWhite else TextPrimary
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Time & Duration Selection
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = SurfaceContainerLowest,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .border(1.dp, BorderOutline.copy(alpha = 0.45f), RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "SCHEDULE & DURATION",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextOutline,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(10.dp))

                // Time presets
                Text(text = "Start Time", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("06:00 AM", "06:30 AM", "08:00 AM", "13:00 PM", "16:30 PM", "19:30 PM", "21:00 PM").forEach { time ->
                        val isSelected = selectedTime == time
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(if (isSelected) InkBlack else SurfaceContainerLow)
                                .border(1.dp, if (isSelected) InkBlack else BorderOutline.copy(alpha = 0.5f), CircleShape)
                                .clickable { selectedTime = time }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = time,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (isSelected) InkWhite else TextPrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Duration presets
                Text(text = "Duration", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(15 to "15m", 30 to "30m", 45 to "45m", 60 to "1h", 120 to "2h").forEach { (mins, label) ->
                        val isSelected = durationMinutes == mins
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) InkBlack else SurfaceContainerLow)
                                .border(1.dp, if (isSelected) InkBlack else BorderOutline.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .clickable { durationMinutes = mins }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (isSelected) InkWhite else TextPrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Recurrence
                Text(text = "Recurrence Cadence", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Every day", "Monday–Friday", "Weekly", "Monthly", "Custom").forEach { days ->
                        val isSelected = recurrenceDays == days
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) InkBlack else SurfaceContainerLow)
                                .border(1.dp, if (isSelected) InkBlack else BorderOutline.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .clickable { recurrenceDays = days }
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = days,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (isSelected) InkWhite else TextPrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Gentle Chime
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(20.dp))
                        Column {
                            Text(text = "Gentle Sanctuary Chime", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = TextPrimary)
                            Text(text = "Play soft bell at block start", style = MaterialTheme.typography.labelSmall, color = TextOutline)
                        }
                    }
                    Switch(
                        checked = gentleChime,
                        onCheckedChange = { gentleChime = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = InkWhite,
                            checkedTrackColor = InkBlack,
                            uncheckedThumbColor = TextOutline,
                            uncheckedTrackColor = SurfaceContainerHigh
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Big Primary Button
        Box(modifier = Modifier.padding(horizontal = 20.dp)) {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        viewModel.createTask(
                            title = title,
                            category = selectedCategory,
                            time = selectedTime,
                            durationMinutes = durationMinutes,
                            anchor = selectedAnchor,
                            days = recurrenceDays,
                            isRecurring = true,
                            gentleChime = gentleChime
                        )
                    } else {
                        viewModel.emitToast("Please enter a routine title")
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = InkBlack,
                    contentColor = InkWhite
                ),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text("Add to Daily Cadence", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

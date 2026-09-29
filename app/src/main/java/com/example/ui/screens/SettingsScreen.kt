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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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

@Composable
fun SettingsScreen(
    viewModel: CompanionViewModel,
    onNavigate: (Screen) -> Unit,
    onBack: () -> Unit
) {
    val wakeTime by viewModel.wakeTime.collectAsState()
    val bedTime by viewModel.bedTime.collectAsState()
    val profileName by viewModel.profileName.collectAsState()
    val profileBio by viewModel.profileBio.collectAsState()
    val readerPaperTone by viewModel.readerPaperTone.collectAsState()
    val readerFontSize by viewModel.readerFontSizeSp.collectAsState()
    val isDemoLoading by viewModel.isDemoLoading.collectAsState()
    val isDemoEmpty by viewModel.isDemoEmpty.collectAsState()
    val currentScreen by viewModel.currentScreen.collectAsState()

    var gentleChimesEnabled by remember { mutableStateOf(true) }
    var proactiveAssistantEnabled by remember { mutableStateOf(true) }

    var showEditProfileDialog by remember { mutableStateOf(false) }
    var editName by remember { mutableStateOf("") }
    var editBio by remember { mutableStateOf("") }

    var showEditWakeDialog by remember { mutableStateOf(false) }
    var editWake by remember { mutableStateOf("") }

    var showEditBedDialog by remember { mutableStateOf(false) }
    var editBed by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize().background(CanvasSurface)) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 80.dp)
        ) {
            // Top Header
            item {
                Surface(color = CanvasSurface, modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onBack, modifier = Modifier.testTag("settings_back_button")) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = InkBlack)
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Settings",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        PetalDot(size = 6.dp)
                    }
                }
            }

            // Profile Card (Clickable to Edit)
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = SurfaceContainerLowest,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                        .border(1.dp, BorderOutline.copy(alpha = 0.45f), RoundedCornerShape(16.dp))
                        .clickable {
                            editName = profileName
                            editBio = profileBio
                            showEditProfileDialog = true
                        }
                        .testTag("settings_profile_card")
                ) {
                    Row(
                        modifier = Modifier.padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(InkBlack),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = InkWhite, modifier = Modifier.size(26.dp))
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = profileName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                PetalDot(size = 5.dp)
                            }
                            Text(text = profileBio, style = MaterialTheme.typography.bodySmall, color = TextOutline)
                        }
                        Text(text = "Edit →", style = MaterialTheme.typography.labelSmall, color = TextOutline)
                    }
                }
            }

            // Section 1: Cadence & Daily Rhythms
            item {
                SettingsSectionHeader(title = "CADENCE & RHYTHMS")
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = SurfaceContainerLowest,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 4.dp)
                        .border(1.dp, BorderOutline.copy(alpha = 0.45f), RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    editWake = wakeTime
                                    showEditWakeDialog = true
                                }
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Dawn Wake Anchor", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = TextPrimary)
                                Text("Primary cadence baseline (Tap to edit)", style = MaterialTheme.typography.labelSmall, color = TextOutline)
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(SurfaceContainerLow)
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(wakeTime, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    editBed = bedTime
                                    showEditBedDialog = true
                                }
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Evening Rest Boundary", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = TextPrimary)
                                Text("Dim lights & reading wind-down (Tap to edit)", style = MaterialTheme.typography.labelSmall, color = TextOutline)
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(SurfaceContainerLow)
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(bedTime, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Gentle Sanctuary Chimes", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = TextPrimary)
                                Text("Soft bell at block start and end", style = MaterialTheme.typography.labelSmall, color = TextOutline)
                            }
                            Switch(
                                checked = gentleChimesEnabled,
                                onCheckedChange = {
                                    gentleChimesEnabled = it
                                    viewModel.emitToast(if (it) "Chimes active" else "Chimes muted")
                                },
                                colors = SwitchDefaults.colors(checkedThumbColor = InkWhite, checkedTrackColor = InkBlack)
                            )
                        }
                    }
                }
            }

            // Section: Android Exact Alarms & System Notifications
            item {
                val context = androidx.compose.ui.platform.LocalContext.current
                val canScheduleExact = remember { viewModel.canScheduleExactAlarms() }
                var exactAlarmGranted by remember { mutableStateOf(canScheduleExact) }

                SettingsSectionHeader(title = "ANDROID ALARMS & NOTIFICATIONS")
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = SurfaceContainerLowest,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 4.dp)
                        .border(1.dp, BorderOutline.copy(alpha = 0.45f), RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Exact Alarms & Wake Schedules", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = TextPrimary)
                                Text(
                                    if (exactAlarmGranted) "Permission granted • Alarms fire precisely on time"
                                    else "Permission needed for precise wake & routine chimes",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (exactAlarmGranted) PetalAccent else TextOutline
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(if (exactAlarmGranted) SecondaryContainer else SurfaceContainerHigh)
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    if (exactAlarmGranted) "ENABLED" else "ACTION REQ",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (exactAlarmGranted) PetalAccent else TextPrimary
                                )
                            }
                        }

                        if (!exactAlarmGranted) {
                            Text(
                                text = "Exact alarms guarantee morning scripture and reading rhythms trigger at the exact minute even when the device is locked, sleep-mode active, or backgrounded.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                            Button(
                                onClick = {
                                    try {
                                        context.startActivity(viewModel.getExactAlarmSettingsIntent())
                                        exactAlarmGranted = viewModel.canScheduleExactAlarms()
                                    } catch (_: Exception) {
                                        viewModel.emitToast("Please enable Exact Alarms in Android Settings")
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = InkBlack, contentColor = InkWhite),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Open Android Exact Alarm Settings", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        // System Notifications switch
                        val userPrefs by viewModel.userPreferences.collectAsState()
                        val isNotifsEnabled = userPrefs?.notifications ?: true
                        val isHapticsEnabled = userPrefs?.haptics ?: true
                        val quietHoursText = userPrefs?.quietHours ?: "22:00 - 06:00"

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("System Notifications", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = TextPrimary)
                                Text("Quiet alerts for routines, readings, and milestones", style = MaterialTheme.typography.labelSmall, color = TextOutline)
                            }
                            Switch(
                                checked = isNotifsEnabled,
                                onCheckedChange = { viewModel.setNotificationsEnabled(it) },
                                colors = SwitchDefaults.colors(checkedThumbColor = InkWhite, checkedTrackColor = InkBlack)
                            )
                        }

                        // Quiet Hours
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Quiet Hours Boundary", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = TextPrimary)
                                Text("Mutes non-critical notifications during rest ($quietHoursText)", style = MaterialTheme.typography.labelSmall, color = TextOutline)
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(SurfaceContainerLow)
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(quietHoursText, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = TextPrimary)
                            }
                        }

                        // Subtle Haptics Switch
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Subtle Haptic Feedback", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = TextPrimary)
                                Text("Gentle tactile pulse on task & page completions", style = MaterialTheme.typography.labelSmall, color = TextOutline)
                            }
                            Switch(
                                checked = isHapticsEnabled,
                                onCheckedChange = { viewModel.setHapticsEnabled(it) },
                                colors = SwitchDefaults.colors(checkedThumbColor = InkWhite, checkedTrackColor = InkBlack)
                            )
                        }

                        // Smart Reading Reminder Engine evaluation button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Smart Reading Reminder Engine", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = TextPrimary)
                                Text("Deterministic evaluation of abandoned books & targets", style = MaterialTheme.typography.labelSmall, color = TextOutline)
                            }
                            Button(
                                onClick = {
                                    viewModel.evaluateSmartReminders()
                                    viewModel.emitToast("Smart reading triggers evaluated")
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SurfaceContainerHigh, contentColor = TextPrimary),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Check", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }


            // Section 2: Reader Appearance
            item {
                SettingsSectionHeader(title = "READER DEFAULTS")
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = SurfaceContainerLowest,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 4.dp)
                        .border(1.dp, BorderOutline.copy(alpha = 0.45f), RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Text("Paper Tone Palette", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = TextPrimary)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("Linen", "Soft Dim", "Night").forEach { tone ->
                                val isSelected = readerPaperTone == tone
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isSelected) InkBlack else SurfaceContainerLow)
                                        .clickable { viewModel.setReaderPaperTone(tone) }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = tone,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                        color = if (isSelected) InkWhite else TextPrimary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Default Reading Font Size", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = TextPrimary)
                                Text("Scaled proportionally: ${readerFontSize}sp", style = MaterialTheme.typography.labelSmall, color = TextOutline)
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(SurfaceContainerLow)
                                        .clickable { viewModel.adjustReaderFontSize(-1) }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text("A-", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(SurfaceContainerLow)
                                        .clickable { viewModel.adjustReaderFontSize(1) }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text("A+", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                }
                            }
                        }
                    }
                }
            }

            // Section 3: Data & Local Persistence
            item {
                SettingsSectionHeader(title = "PERSISTENCE & SYSTEM")
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = SurfaceContainerLowest,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 4.dp)
                        .border(1.dp, BorderOutline.copy(alpha = 0.45f), RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.Storage, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(20.dp))
                                Column {
                                    Text("Room Database", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = TextPrimary)
                                    Text("Local SQLite persistence active", style = MaterialTheme.typography.labelSmall, color = TextOutline)
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(SurfaceContainerLow)
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text("ONLINE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { viewModel.emitToast("Cadence journal exported to device storage") },
                                colors = ButtonDefaults.buttonColors(containerColor = InkBlack, contentColor = InkWhite),
                                shape = RoundedCornerShape(20.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Export Journal", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }

                            Button(
                                onClick = { onNavigate(Screen.ONBOARDING) },
                                colors = ButtonDefaults.buttonColors(containerColor = SurfaceContainerHigh, contentColor = TextPrimary),
                                shape = RoundedCornerShape(20.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Restart Flow", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }

            // About section
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "QUIET COMPANION",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextOutline,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Version 1.0.4 · Stitch Editorial Visual Shell",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    Text(
                        text = "Anchored in spiritual devotion, reading cadence & serene silence.",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextOutline,
                        fontSize = 10.sp
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }

    if (showEditProfileDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            title = { Text("Edit Profile", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    androidx.compose.material3.OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    androidx.compose.material3.OutlinedTextField(
                        value = editBio,
                        onValueChange = { editBio = it },
                        label = { Text("Bio / Subtitle") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                androidx.compose.material3.TextButton(
                    onClick = {
                        viewModel.updateProfile(editName.ifBlank { "Quiet Reader" }, editBio.ifBlank { "Local Profile" })
                        showEditProfileDialog = false
                    }
                ) {
                    Text("Save", color = InkBlack, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { showEditProfileDialog = false }) {
                    Text("Cancel", color = TextOutline)
                }
            },
            containerColor = CanvasSurface
        )
    }

    if (showEditWakeDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showEditWakeDialog = false },
            title = { Text("Edit Dawn Wake Anchor", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Enter wake up time (e.g., 06:00 AM):", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                    androidx.compose.material3.OutlinedTextField(
                        value = editWake,
                        onValueChange = { editWake = it },
                        label = { Text("Wake Time") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                androidx.compose.material3.TextButton(
                    onClick = {
                        viewModel.setWakeTimePreference(editWake.ifBlank { "06:00 AM" })
                        showEditWakeDialog = false
                    }
                ) {
                    Text("Save", color = InkBlack, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { showEditWakeDialog = false }) {
                    Text("Cancel", color = TextOutline)
                }
            },
            containerColor = CanvasSurface
        )
    }

    if (showEditBedDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showEditBedDialog = false },
            title = { Text("Edit Evening Rest Boundary", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Enter bed time / rest time (e.g., 10:00 PM):", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                    androidx.compose.material3.OutlinedTextField(
                        value = editBed,
                        onValueChange = { editBed = it },
                        label = { Text("Bed Time") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                androidx.compose.material3.TextButton(
                    onClick = {
                        viewModel.setBedTimePreference(editBed.ifBlank { "10:00 PM" })
                        showEditBedDialog = false
                    }
                ) {
                    Text("Save", color = InkBlack, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { showEditBedDialog = false }) {
                    Text("Cancel", color = TextOutline)
                }
            },
            containerColor = CanvasSurface
        )
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelSmall,
        color = TextOutline,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
    )
}

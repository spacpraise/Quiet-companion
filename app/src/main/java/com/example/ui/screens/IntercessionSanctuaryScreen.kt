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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.SelfImprovement
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.IntercessionItem
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
fun IntercessionSanctuaryScreen(
    viewModel: CompanionViewModel,
    onBack: () -> Unit
) {
    val intercessions by viewModel.intercessions.collectAsState()
    val isTimerActive by viewModel.isSessionTimerActive.collectAsState()
    val remainingSeconds by viewModel.sessionRemainingSeconds.collectAsState()

    val currentScreen by viewModel.currentScreen.collectAsState()

    val formattedRemaining = remember(remainingSeconds) {
        val m = remainingSeconds / 60
        val s = remainingSeconds % 60
        String.format("%02d:%02d", m, s)
    }

    Box(modifier = Modifier.fillMaxSize().background(CanvasSurface)) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 24.dp)
        ) {
            // Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                        }
                        Text(
                            text = "Intercession Sanctuary",
                            style = MaterialTheme.typography.titleLarge,
                            color = TextPrimary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(SecondaryContainer)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "OPERATION COMPEL",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                }
            }

            // Introduction & Scripture Anchor Card
            item {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = SurfaceContainerLowest,
                    shadowElevation = 1.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp)
                        .border(1.dp, BorderOutline.copy(alpha = 0.5f), RoundedCornerShape(18.dp))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            PetalDot(size = 6.dp)
                            Text(
                                text = "OPERATION COMPEL THEM TO COME",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = TextOutline,
                                letterSpacing = 1.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Concluding Week Intercessions",
                            style = MaterialTheme.typography.headlineSmall,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Pray through these covenant declarations with reverence and expectant faith for the harvest.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Contemplation timer bar
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(SurfaceContainerLow)
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.HourglassTop, contentDescription = null, tint = InkBlack, modifier = Modifier.size(16.dp))
                                Text(
                                    text = if (isTimerActive) "$formattedRemaining remaining" else "30m Sanctuary Block",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(if (isTimerActive) SurfaceContainerHigh else InkBlack)
                                    .clickable { viewModel.toggleSessionTimer() }
                                    .padding(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = if (isTimerActive) "Pause" else "Begin Prayer",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isTimerActive) TextPrimary else InkWhite,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // List of 4 Intercessions
            items(intercessions) { item ->
                IntercessionCard(
                    item = item,
                    onToggle = { viewModel.toggleIntercessionPrayed(item) },
                    onSaveNotes = { notes -> viewModel.updateIntercessionNotes(item, notes) }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Complete Devotion Action
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    Button(
                        onClick = {
                            intercessions.forEach { if (!it.isPrayedToday) viewModel.toggleIntercessionPrayed(it) }
                            viewModel.emitToast("All 4 Intercessions prayed. Morning Devotion complete!")
                            onBack()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = InkBlack),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("complete_devotion_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.DoneAll, contentDescription = null, tint = InkWhite, modifier = Modifier.size(18.dp))
                            Text(
                                text = "Seal Prayer & Complete Morning Cadence",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = InkWhite
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun IntercessionCard(
    item: IntercessionItem,
    onToggle: () -> Unit,
    onSaveNotes: (String) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    var notesText by remember { mutableStateOf(item.notes) }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (item.isPrayedToday) SurfaceContainerLow else SurfaceContainerLowest,
        shadowElevation = 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .border(
                1.dp,
                if (item.isPrayedToday) Color.Transparent else BorderOutline.copy(alpha = 0.5f),
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
                    PetalDot(size = 6.dp)
                    Text(
                        text = "INTERCESSION ${item.number} • ${item.scriptureRef}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                // Checkbox
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (item.isPrayedToday) InkBlack else SurfaceContainerHigh)
                        .clickable { onToggle() }
                        .testTag("intercession_check_${item.number}"),
                    contentAlignment = Alignment.Center
                ) {
                    if (item.isPrayedToday) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Prayed",
                            tint = InkWhite,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Prayer text
            Text(
                text = item.prayerText,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = TextPrimary,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Scripture text block
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(SurfaceContainerLowest)
                    .border(1.dp, BorderOutline.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                    .padding(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .width(2.5.dp)
                        .height(38.dp)
                        .background(PetalAccent)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = item.scriptureText,
                        style = MaterialTheme.typography.bodySmall,
                        fontStyle = FontStyle.Italic,
                        color = TextPrimary,
                        lineHeight = 19.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = item.scriptureRef,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextOutline
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Personal reflection toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded }
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isExpanded) "Hide personal burdens ↑" else "Add prayer burden / notes ↓",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextOutline
                )
                if (item.notes.isNotBlank() && !isExpanded) {
                    Text("1 note saved", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                }
            }

            if (isExpanded) {
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = notesText,
                    onValueChange = {
                        notesText = it
                        onSaveNotes(it)
                    },
                    placeholder = { Text("Record what the Spirit quickened in your heart...", color = TextOutline, fontSize = 13.sp) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = InkBlack,
                        unfocusedBorderColor = BorderOutline,
                        focusedContainerColor = SurfaceContainerLowest,
                        unfocusedContainerColor = SurfaceContainerLowest
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

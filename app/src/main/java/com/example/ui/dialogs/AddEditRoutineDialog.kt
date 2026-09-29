package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.RoutineItem
import com.example.ui.components.PetalDot
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
fun AddEditRoutineDialog(
    initialItem: RoutineItem? = null,
    onDismiss: () -> Unit,
    onSave: (title: String, category: String, time: String, duration: Int, days: String, isRecurring: Boolean) -> Unit,
    onDelete: ((RoutineItem) -> Unit)? = null
) {
    var title by remember { mutableStateOf(initialItem?.title ?: "") }
    var time by remember { mutableStateOf(initialItem?.time ?: "20:00 PM") }
    var durationText by remember { mutableStateOf(initialItem?.durationMinutes?.toString() ?: "30") }
    var isRecurring by remember { mutableStateOf(initialItem?.isRecurring ?: true) }
    var category by remember { mutableStateOf(initialItem?.category ?: "reading") }
    var gentleChime by remember { mutableStateOf(initialItem?.gentleChime ?: true) }

    val daysList = listOf("M", "T", "W", "T", "F", "S", "S")
    var selectedDays by remember { mutableStateOf(setOf(0, 1, 2, 3, 4)) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = CanvasSurface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        PetalDot(size = 6.dp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (initialItem != null) "Edit Cadence Item" else "New Cadence Anchor",
                            style = MaterialTheme.typography.titleLarge,
                            color = TextPrimary
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextOutline)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Recurring vs Task Segmented control
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceContainerLow)
                        .padding(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isRecurring) InkBlack else CanvasSurface)
                            .clickable { isRecurring = true }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Recurring Routine",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isRecurring) InkWhite else TextSecondary
                        )
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (!isRecurring) InkBlack else CanvasSurface)
                            .clickable { isRecurring = false }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "One-time Task",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = if (!isRecurring) InkWhite else TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Title Input
                Text(
                    text = "TITLE",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextOutline,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    placeholder = { Text("e.g. Evening Reflection & Reading", color = TextOutline) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = InkBlack,
                        unfocusedBorderColor = BorderOutline,
                        focusedContainerColor = SurfaceContainerLowest,
                        unfocusedContainerColor = SurfaceContainerLowest
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("routine_title_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Time & Duration
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "SCHEDULED TIME", style = MaterialTheme.typography.labelSmall, color = TextOutline)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = time,
                            onValueChange = { time = it },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = InkBlack,
                                unfocusedBorderColor = BorderOutline,
                                focusedContainerColor = SurfaceContainerLowest,
                                unfocusedContainerColor = SurfaceContainerLowest
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "DURATION (MIN)", style = MaterialTheme.typography.labelSmall, color = TextOutline)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = durationText,
                            onValueChange = { durationText = it },
                            singleLine = true,
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

                Spacer(modifier = Modifier.height(12.dp))

                // Days selection if recurring
                if (isRecurring) {
                    Text(text = "DAYS ACTIVE", style = MaterialTheme.typography.labelSmall, color = TextOutline)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        daysList.forEachIndexed { index, dayLetter ->
                            val isDaySelected = selectedDays.contains(index)
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(if (isDaySelected) InkBlack else SurfaceContainerHigh)
                                    .clickable {
                                        selectedDays = if (isDaySelected) {
                                            selectedDays - index
                                        } else {
                                            selectedDays + index
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = dayLetter,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isDaySelected) InkWhite else TextPrimary
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Gentle Chime switch
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceContainerLow)
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "Gentle Chime", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                        Text(text = "Soft acoustic tone on session start", style = MaterialTheme.typography.labelMedium, color = TextOutline)
                    }
                    Switch(
                        checked = gentleChime,
                        onCheckedChange = { gentleChime = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = InkWhite,
                            checkedTrackColor = InkBlack,
                            uncheckedThumbColor = InkWhite,
                            uncheckedTrackColor = BorderOutline
                        )
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (initialItem != null && onDelete != null) {
                        Button(
                            onClick = {
                                onDelete(initialItem)
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SurfaceContainerHigh),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.height(48.dp)
                        ) {
                            Text(text = "Delete", color = MaterialTheme.colorScheme.error)
                        }
                    }
                    Button(
                        onClick = {
                            val dur = durationText.toIntOrNull() ?: 30
                            val daysDesc = if (selectedDays.size == 7) "Daily" else if (selectedDays == setOf(0, 1, 2, 3, 4)) "Mon–Fri" else "Selected days"
                            val cleanTitle = if (title.isBlank()) "Quiet Session" else title.trim()
                            onSave(cleanTitle, category, time, dur, daysDesc, isRecurring)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = InkBlack),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("save_routine_button")
                    ) {
                        Text(
                            text = if (initialItem != null) "Save Changes" else "Anchor to Cadence",
                            color = InkWhite,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

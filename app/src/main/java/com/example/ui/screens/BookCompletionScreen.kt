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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Replay
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BookItem
import com.example.ui.CompanionViewModel
import com.example.ui.Screen
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
fun BookCompletionScreen(
    viewModel: CompanionViewModel,
    onNavigate: (Screen) -> Unit
) {
    val selectedBook by viewModel.selectedBook.collectAsState()
    val rawSessions by viewModel.rawSessions.collectAsState()
    val books by viewModel.books.collectAsState()

    val currentBook = selectedBook ?: books.firstOrNull { it.status == "COMPLETED" } ?: books.firstOrNull() ?: BookItem(
        title = "Completed Volume",
        author = "Local Document",
        totalPages = 0,
        currentPage = 0,
        status = "COMPLETED"
    )

    val bookSessions = remember(rawSessions, currentBook.id) {
        rawSessions.filter { it.bookId == currentBook.id }
    }

    val sessionCount = if (bookSessions.isNotEmpty()) bookSessions.size else (currentBook.sessionsLogged.coerceAtLeast(1))
    val totalSeconds = bookSessions.sumOf { it.duration }
    val readingTimeFormatted = remember(totalSeconds, currentBook.focusHoursFormatted) {
        if (totalSeconds > 0) {
            val hrs = totalSeconds / 3600
            val mins = (totalSeconds % 3600) / 60
            if (hrs > 0) "${hrs}h ${mins}m" else "${mins.coerceAtLeast(1)} min"
        } else {
            currentBook.focusHoursFormatted.ifEmpty { "45 min" }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CanvasSurface)
            .padding(horizontal = 24.dp, vertical = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            // Minimal Serene Check Indicator
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(SurfaceContainerLowest)
                    .border(1.5.dp, BorderOutline.copy(alpha = 0.6f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Finished",
                    tint = InkBlack,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Book Title
            Text(
                text = currentBook.title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Finished Status Label
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                PetalDot(size = 6.dp)
                Text(
                    text = "Finished",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = TextSecondary,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Minimal Stats Card
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = SurfaceContainerLowest,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BorderOutline.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 20.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CompletionStatItem(
                        label = "reading sessions",
                        value = "$sessionCount"
                    )
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(32.dp)
                            .background(BorderOutline.copy(alpha = 0.4f))
                    )
                    CompletionStatItem(
                        label = "reading time",
                        value = readingTimeFormatted
                    )
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(32.dp)
                            .background(BorderOutline.copy(alpha = 0.4f))
                    )
                    CompletionStatItem(
                        label = "pages",
                        value = "${currentBook.totalPages}"
                    )
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            // Action Buttons: Read again, Read another book, Return to shelf
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Read again Button
                Button(
                    onClick = {
                        viewModel.reReadBook(currentBook)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = InkBlack,
                        contentColor = InkWhite
                    ),
                    shape = RoundedCornerShape(28.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("completion_read_again_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Replay, contentDescription = null, modifier = Modifier.size(18.dp))
                        Text(
                            text = "Read again",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Read another book Button
                Surface(
                    shape = RoundedCornerShape(28.dp),
                    color = SurfaceContainerLowest,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .border(1.dp, BorderOutline.copy(alpha = 0.5f), RoundedCornerShape(28.dp))
                        .clickable {
                            onNavigate(Screen.LIBRARY)
                        }
                        .testTag("completion_read_another_button")
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Read another book",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                    }
                }

                // Return to shelf Button
                Surface(
                    shape = RoundedCornerShape(28.dp),
                    color = CanvasSurface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .clickable {
                            onNavigate(Screen.LIBRARY)
                        }
                        .testTag("completion_return_shelf_button")
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Return to shelf",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextSecondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CompletionStatItem(
    label: String,
    value: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextOutline,
            fontSize = 11.sp
        )
    }
}

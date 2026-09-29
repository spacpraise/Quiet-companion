package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.CollectionsBookmark
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
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
fun BookDetailScreen(
    viewModel: CompanionViewModel,
    onNavigate: (Screen) -> Unit,
    onBack: () -> Unit
) {
    val books by viewModel.books.collectAsState()
    val book by viewModel.selectedBook.collectAsState()
    val highlights by viewModel.highlights.collectAsState()
    val currentScreen by viewModel.currentScreen.collectAsState()

    var activeTab by remember { mutableStateOf("Highlights") } // "Highlights", "Notes"

    val currentBook = book ?: books.firstOrNull() ?: com.example.data.model.BookItem(
        title = "No Book Selected",
        author = "Unknown",
        totalPages = 0,
        currentPage = 0,
        status = "UNREAD"
    )

    Box(modifier = Modifier.fillMaxSize().background(CanvasSurface)) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 24.dp)
        ) {
            // Header Top Bar
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
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = TextPrimary
                            )
                        }
                        Text(
                            text = "Book Details",
                            style = MaterialTheme.typography.titleLarge,
                            color = TextPrimary
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { viewModel.toggleBookmark() }) {
                            Icon(
                                imageVector = Icons.Default.BookmarkBorder,
                                contentDescription = "Bookmark",
                                tint = TextPrimary
                            )
                        }
                    }
                }
            }

            // Top Presentation Section (Status, Cover, Title)
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(SurfaceContainerLow)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            PetalDot(size = 6.dp)
                            Text(
                                text = if (currentBook.status == "COMPLETED") "ARCHIVED COMPLETED" else "CURRENTLY READING",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Cover Frame
                    Box(
                        modifier = Modifier
                            .width(172.dp)
                            .aspectRatio(0.70f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(SurfaceContainerHigh)
                            .border(1.dp, BorderOutline.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    ) {
                        val coverRes = if (currentBook.title.contains("Atomic", ignoreCase = true)) {
                            com.example.R.drawable.atomic_habits_cover_1790510049748
                        } else null

                        if (coverRes != null) {
                            Image(
                                painter = painterResource(id = coverRes),
                                contentDescription = currentBook.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(SurfaceContainerLowest)
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = currentBook.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = currentBook.author,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextOutline
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = currentBook.title,
                        style = MaterialTheme.typography.headlineLarge,
                        color = TextPrimary
                    )
                    Text(
                        text = currentBook.author,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextOutline
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Progress Module Card
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = SurfaceContainerLowest,
                    shadowElevation = 1.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .border(1.dp, BorderOutline.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Text(
                                text = "${currentBook.progressPercent}% completed",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "${currentBook.pagesLeft} pages left",
                                style = MaterialTheme.typography.labelMedium,
                                color = TextOutline
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Progress Bar with Pinpoint Dot
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(CircleShape)
                                .background(SurfaceContainerHigh)
                        ) {
                            val frac = (currentBook.currentPage.toFloat() / currentBook.totalPages).coerceIn(0f, 1f)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(frac)
                                    .height(4.dp)
                                    .background(InkBlack)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            PetalDot(size = 6.dp)
                            Text(
                                text = "Page ${currentBook.currentPage} of ${currentBook.totalPages} • ${currentBook.currentChapter}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Primary Continue Reading Button & Quick Actions
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Continue reading pill button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .clip(CircleShape)
                            .background(InkBlack)
                            .clickable {
                                viewModel.openReaderForBook(currentBook)
                            }
                            .padding(horizontal = 20.dp)
                            .testTag("continue_reading_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(InkWhite.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = null,
                                        tint = InkWhite,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = "Continue reading",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.SemiBold,
                                        color = InkWhite
                                    )
                                    Text(
                                        text = "From page ${currentBook.currentPage}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = SurfaceContainerHigh
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = InkWhite,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Quiet actions strip
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .clip(CircleShape)
                                .background(SurfaceContainerLowest)
                                .border(1.dp, BorderOutline.copy(alpha = 0.5f), CircleShape)
                            .clickable { onNavigate(Screen.LIBRARY) },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Outlined.CollectionsBookmark, contentDescription = null, tint = TextOutline, modifier = Modifier.size(16.dp))
                                Text("Return to shelf", style = MaterialTheme.typography.labelMedium, color = TextPrimary)
                            }
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .clip(CircleShape)
                                .background(SurfaceContainerLowest)
                                .border(1.dp, BorderOutline.copy(alpha = 0.5f), CircleShape)
                                .clickable {
                                    viewModel.finishBook(currentBook)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.Replay, contentDescription = null, tint = TextOutline, modifier = Modifier.size(16.dp))
                                Text("Complete Book", style = MaterialTheme.typography.labelMedium, color = TextPrimary)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(18.dp))
            }

            // Quiet Reading Statistics Card
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = SurfaceContainerLow,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            StatColumn("LAST READ", currentBook.lastRead.substringBefore(" "), currentBook.lastRead.substringAfter(" ", ""))
                            StatColumn("SESSIONS", currentBook.sessionsLogged.toString(), "logged")
                            StatColumn("TOTAL TIME", currentBook.focusHoursFormatted, "focus")
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            PetalDot(size = 5.dp)
                            Text(
                                text = "Pace: ~22 pages / session • Calm focus",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(18.dp))
            }

            // Highlights & Notes Section
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(SurfaceContainerLow)
                                .padding(4.dp)
                        ) {
                            val countH = highlights.count { !it.isNote }
                            val countN = highlights.count { it.isNote }
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(if (activeTab == "Highlights") SurfaceContainerLowest else CanvasSurface)
                                    .clickable { activeTab = "Highlights" }
                                    .padding(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "Highlights ($countH)",
                                    fontSize = 12.sp,
                                    fontWeight = if (activeTab == "Highlights") FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (activeTab == "Highlights") TextPrimary else TextOutline
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(if (activeTab == "Notes") SurfaceContainerLowest else CanvasSurface)
                                    .clickable { activeTab = "Notes" }
                                    .padding(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "Notes ($countN)",
                                    fontSize = 12.sp,
                                    fontWeight = if (activeTab == "Notes") FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (activeTab == "Notes") TextPrimary else TextOutline
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(SurfaceContainerLowest)
                                .border(1.dp, BorderOutline.copy(alpha = 0.5f), CircleShape)
                                .clickable {
                                    viewModel.emitToast("Add reflection in Reader canvas")
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add Reflection", tint = TextPrimary, modifier = Modifier.size(18.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    val filteredHighlights = if (activeTab == "Highlights") {
                        highlights.filter { !it.isNote }
                    } else {
                        highlights.filter { it.isNote }
                    }

                    filteredHighlights.forEach { item ->
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (item.isNote) SurfaceContainerLow else SurfaceContainerLowest,
                            shadowElevation = 1.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 10.dp)
                                .border(1.dp, BorderOutline.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
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
                                        PetalDot(size = 5.dp)
                                        Text(
                                            text = item.dateAdded,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = TextOutline,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    IconButton(
                                        onClick = { viewModel.emitToast("Quote copied to clipboard") },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Share, contentDescription = "Share", tint = TextOutline, modifier = Modifier.size(16.dp))
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Row(modifier = Modifier.fillMaxWidth()) {
                                    Box(
                                        modifier = Modifier
                                            .width(2.5.dp)
                                            .height(36.dp)
                                            .background(PetalAccent)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "\"${item.quote}\"",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontStyle = FontStyle.Italic,
                                        color = TextPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatColumn(
    label: String,
    primary: String,
    secondary: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = TextOutline)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = primary, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
        Text(text = secondary, style = MaterialTheme.typography.labelSmall, color = TextOutline)
    }
}

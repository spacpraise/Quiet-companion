package com.example.ui.screens

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BookmarkAdded
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.outlined.CollectionsBookmark
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BookItem
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
import java.io.File

@Composable
fun LibraryScreen(
    viewModel: CompanionViewModel,
    onNavigate: (Screen) -> Unit
) {
    val books by viewModel.books.collectAsState()
    val isShelfEmpty by viewModel.isShelfEmptyView.collectAsState()
    val isDemoLoading by viewModel.isDemoLoading.collectAsState()
    val isDemoEmpty by viewModel.isDemoEmpty.collectAsState()
    val isImportingPdf by viewModel.isImportingPdf.collectAsState()
    val importError by viewModel.importError.collectAsState()
    val currentScreen by viewModel.currentScreen.collectAsState()

    var activeFilter by remember { mutableStateOf("All") }
    var isArchiveExpanded by remember { mutableStateOf(false) }
    var showFavoritePicker by remember { mutableStateOf(false) }

    // System File Picker for PDF documents
    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.importPdfFromUri(uri)
        }
    }

    val activeReadingBooks = remember(books) {
        books.filter { it.status == "READING" || (it.status != "COMPLETED" && it.currentPage < it.totalPages) }
    }

    val completedBooks = remember(books) {
        books.filter { it.status == "COMPLETED" || (it.currentPage >= it.totalPages && it.totalPages > 0) }
    }

    val isAllCompletedOrEmpty = books.isNotEmpty() && activeReadingBooks.isEmpty()
    val showEmptyShelfState = isShelfEmpty || isDemoEmpty || isAllCompletedOrEmpty || books.isEmpty()

    val filteredBooks = remember(books, activeFilter, showEmptyShelfState) {
        if (showEmptyShelfState) return@remember emptyList()
        when (activeFilter) {
            "Reading" -> books.filter { it.status == "READING" }
            "Unread" -> books.filter { it.status == "UNREAD" }
            "Paused" -> books.filter { it.status == "PAUSED" }
            else -> books.filter { it.status != "COMPLETED" }
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
                    title = "Library",
                    subtitle = "Quiet Sanctuary Bookshelf",
                    onUserClick = { onNavigate(Screen.SETTINGS) },
                    actions = {
                        IconButton(onClick = { viewModel.toggleEmptyShelfView() }) {
                            Icon(
                                imageVector = Icons.Outlined.CollectionsBookmark,
                                contentDescription = "Toggle Shelf View",
                                tint = TextOutline
                            )
                        }
                        IconButton(
                            onClick = { pdfPickerLauncher.launch(arrayOf("application/pdf")) },
                            modifier = Modifier.testTag("add_book_top_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Import PDF", tint = TextPrimary)
                        }
                    }
                )
            }

            // Error Banner if PDF import failed
            if (importError != null) {
                item {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFFDE8E8),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF8B4B4)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = Color(0xFF9B1C1C))
                                Column {
                                    Text("PDF Import Error", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color(0xFF9B1C1C))
                                    Text(importError ?: "", style = MaterialTheme.typography.bodySmall, color = Color(0xFF9B1C1C))
                                }
                            }
                            IconButton(onClick = { viewModel.clearImportError() }) {
                                Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = Color(0xFF9B1C1C))
                            }
                        }
                    }
                }
            }

            // Category Filter Pills (if shelf is populated)
            if (!showEmptyShelfState) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("All", "Reading", "Unread", "Paused").forEach { filter ->
                            val isSelected = activeFilter == filter
                            Surface(
                                shape = CircleShape,
                                color = if (isSelected) InkBlack else SurfaceContainerHigh,
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .clickable { activeFilter = filter }
                            ) {
                                Text(
                                    text = filter,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isSelected) InkWhite else TextSecondary,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Import PDF Card / Progress
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = SurfaceContainerLowest,
                    shadowElevation = 1.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp)
                        .border(1.dp, BorderOutline.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                        .clickable {
                            pdfPickerLauncher.launch(arrayOf("application/pdf"))
                        }
                        .testTag("import_pdf_button")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(SurfaceContainerLow),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isImportingPdf) {
                                    CircularProgressIndicator(
                                        color = InkBlack,
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.FileOpen,
                                        contentDescription = null,
                                        tint = TextPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = if (isImportingPdf) "Importing PDF document..." else "Import PDF Document",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Select from local storage • Saved permanently offline",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextOutline
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Import",
                            tint = TextOutline,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Loading, Intelligent Empty-Shelf State, or Grid
            if (isDemoLoading) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        com.example.ui.components.LoadingSkeletonCard(height = 140.dp)
                        com.example.ui.components.LoadingSkeletonCard(height = 140.dp)
                    }
                }
            } else if (showEmptyShelfState) {
                // Intelligent Empty-Shelf State
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 24.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(SurfaceContainerLowest)
                            .border(1.dp, BorderOutline.copy(alpha = 0.45f), RoundedCornerShape(20.dp))
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(SurfaceContainerLow),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.CollectionsBookmark,
                                contentDescription = null,
                                tint = InkBlack,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Your shelf is empty.",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            PetalDot(size = 6.dp)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (isAllCompletedOrEmpty)
                                "You have completed all active volumes. Add another book to begin your next reading rhythm or revisit a favorite."
                            else
                                "Import a PDF from your device to begin your peaceful cadence. Local-first and preserved offline.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        Spacer(modifier = Modifier.height(20.dp))

                        // Actions: Add another book, Read a favorite again
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    pdfPickerLauncher.launch(arrayOf("application/pdf"))
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = InkBlack,
                                    contentColor = InkWhite
                                ),
                                shape = RoundedCornerShape(24.dp),
                                modifier = Modifier.fillMaxWidth().height(48.dp).testTag("empty_shelf_add_book_button")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Text(text = "Add another book", fontWeight = FontWeight.SemiBold)
                                }
                            }

                            if (completedBooks.isNotEmpty()) {
                                Surface(
                                    shape = RoundedCornerShape(24.dp),
                                    color = SurfaceContainerLowest,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .border(1.dp, BorderOutline.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                                        .clickable {
                                            showFavoritePicker = true
                                        }
                                        .testTag("empty_shelf_read_favorite_button")
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxSize(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(Icons.Default.Replay, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Read a favorite again",
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.Medium,
                                            color = TextPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // Two-column active bookshelf grid
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                    ) {
                        for (i in filteredBooks.indices step 2) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 18.dp),
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                val book1 = filteredBooks[i]
                                BookShelfItem(
                                    book = book1,
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        viewModel.selectBook(book1)
                                        onNavigate(Screen.BOOK_DETAIL)
                                    }
                                )

                                if (i + 1 < filteredBooks.size) {
                                    val book2 = filteredBooks[i + 1]
                                    BookShelfItem(
                                        book = book2,
                                        modifier = Modifier.weight(1f),
                                        onClick = {
                                            viewModel.selectBook(book2)
                                            onNavigate(Screen.BOOK_DETAIL)
                                        }
                                    )
                                } else {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }

            // Finished Books Section (Real Stored Completed Books from Room)
            if (completedBooks.isNotEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = SurfaceContainerLowest,
                            shadowElevation = 1.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, BorderOutline.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                                .clickable { isArchiveExpanded = !isArchiveExpanded }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .background(SecondaryContainer.copy(alpha = 0.6f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.DoneAll,
                                            contentDescription = null,
                                            tint = InkBlack,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "Finished Books",
                                                style = MaterialTheme.typography.titleMedium,
                                                color = TextPrimary
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "(${completedBooks.size})",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = TextOutline
                                            )
                                        }
                                        Text(
                                            text = "Archived notes & annotations preserved",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = TextOutline
                                        )
                                    }
                                }
                                Icon(
                                    imageVector = if (isArchiveExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = null,
                                    tint = TextOutline
                                )
                            }
                        }

                        AnimatedVisibility(visible = isArchiveExpanded) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 10.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                completedBooks.forEach { completedBook ->
                                    ArchiveBookItemRow(
                                        book = completedBook,
                                        onReRead = {
                                            viewModel.reReadBook(completedBook)
                                        },
                                        onOpen = {
                                            viewModel.selectBook(completedBook)
                                            onNavigate(Screen.BOOK_DETAIL)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Favorite Book Picker Dialog for "Read a favorite again"
        if (showFavoritePicker) {
            AlertDialog(
                onDismissRequest = { showFavoritePicker = false },
                title = {
                    Text(
                        text = "Select a Favorite to Re-read",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Choose a completed volume to reset and re-read from page 1:",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        completedBooks.forEach { b ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = SurfaceContainerLow,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        showFavoritePicker = false
                                        viewModel.reReadBook(b)
                                    }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = b.title,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = TextPrimary
                                        )
                                        Text(
                                            text = "${b.author} · ${b.totalPages} pages",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextOutline
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.Default.Replay,
                                        contentDescription = "Re-read",
                                        tint = InkBlack,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showFavoritePicker = false }) {
                        Text("Cancel", color = TextOutline)
                    }
                },
                containerColor = CanvasSurface
            )
        }
    }
}

@Composable
private fun BookShelfItem(
    book: BookItem,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val coverBitmap = remember(book.coverUri) {
        if (book.coverUri.isNotEmpty()) {
            val f = File(book.coverUri)
            if (f.exists()) BitmapFactory.decodeFile(f.absolutePath) else null
        } else null
    }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .testTag("book_card_${book.id}")
    ) {
        // Book Cover Frame
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.70f)
                .clip(RoundedCornerShape(12.dp))
                .background(SurfaceContainerHigh)
                .border(1.dp, BorderOutline.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
        ) {
            val coverRes = if (book.title.contains("Atomic", ignoreCase = true)) {
                com.example.R.drawable.atomic_habits_cover_1790510049748
            } else {
                null
            }

            if (coverBitmap != null) {
                Image(
                    bitmap = coverBitmap.asImageBitmap(),
                    contentDescription = book.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else if (coverRes != null) {
                Image(
                    painter = painterResource(id = coverRes),
                    contentDescription = book.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                // Editorial minimal book canvas
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(SurfaceContainerLowest)
                        .padding(14.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = book.author.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = TextOutline,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = book.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${book.totalPages} pages",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextOutline
                    )
                }
            }

            // Top right status indicator or badge
            if (book.status == "READING") {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                ) {
                    PetalDot(size = 8.dp)
                }
            } else if (book.status == "UNREAD") {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                        .clip(CircleShape)
                        .background(SurfaceContainerLowest.copy(alpha = 0.9f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "NEW",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else if (book.status == "PAUSED") {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                        .clip(CircleShape)
                        .background(SurfaceContainerLowest.copy(alpha = 0.9f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "PAUSED",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextOutline,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Title & Author
        Text(
            text = book.title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = book.author,
            style = MaterialTheme.typography.bodySmall,
            color = TextOutline,
            maxLines = 1
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Progress line
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .clip(CircleShape)
                .background(SurfaceContainerHighest)
        ) {
            val frac = if (book.totalPages > 0) (book.currentPage.toFloat() / book.totalPages).coerceIn(0f, 1f) else 0f
            Box(
                modifier = Modifier
                    .fillMaxWidth(frac)
                    .height(3.dp)
                    .background(if (book.status == "PAUSED") TextOutline else InkBlack)
            )
        }

        Spacer(modifier = Modifier.height(2.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "${book.progressPercent}%",
                style = MaterialTheme.typography.labelSmall,
                color = TextOutline
            )
            Text(
                text = "p. ${book.currentPage} / ${book.totalPages}",
                style = MaterialTheme.typography.labelSmall,
                color = TextOutline
            )
        }
    }
}

@Composable
private fun ArchiveBookItemRow(
    book: BookItem,
    onReRead: () -> Unit,
    onOpen: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceContainerLow)
            .clickable { onOpen() }
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.Default.BookmarkAdded,
                contentDescription = null,
                tint = InkBlack,
                modifier = Modifier.size(18.dp)
            )
            Column {
                Text(
                    text = book.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${book.author} · ${book.totalPages} pages · Finished",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextOutline
                )
            }
        }
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .background(SurfaceContainerLowest)
                .border(1.dp, BorderOutline.copy(alpha = 0.5f), CircleShape)
                .clickable { onReRead() }
                .padding(horizontal = 10.dp, vertical = 5.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(Icons.Default.Replay, contentDescription = null, tint = InkBlack, modifier = Modifier.size(12.dp))
                Text(
                    text = "Read again",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextPrimary
                )
            }
        }
    }
}

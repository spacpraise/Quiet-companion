package com.example.ui.screens

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.BorderColor
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PdfBookManager
import com.example.data.model.Annotation
import com.example.data.model.BookItem
import com.example.ui.CompanionViewModel
import com.example.ui.components.PetalDot
import com.example.ui.theme.BorderOutline
import com.example.ui.theme.CanvasSurface
import com.example.ui.theme.InkBlack
import com.example.ui.theme.InkWhite
import com.example.ui.theme.NightTone
import com.example.ui.theme.PetalAccent
import com.example.ui.theme.SecondaryContainer
import com.example.ui.theme.SoftPaperTone
import com.example.ui.theme.SurfaceContainer
import com.example.ui.theme.SurfaceContainerHigh
import com.example.ui.theme.SurfaceContainerHighest
import com.example.ui.theme.SurfaceContainerLow
import com.example.ui.theme.SurfaceContainerLowest
import com.example.ui.theme.TextOutline
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ReaderScreen(
    viewModel: CompanionViewModel,
    onBack: () -> Unit
) {
    val books by viewModel.books.collectAsState()
    val book by viewModel.selectedBook.collectAsState()
    val page by viewModel.readerPage.collectAsState()
    val scrollPos by viewModel.readerScrollPosition.collectAsState()
    val zoomLevel by viewModel.readerZoomLevel.collectAsState()
    val isBookmarked by viewModel.isBookmarked.collectAsState()
    val fontSizeSp by viewModel.readerFontSizeSp.collectAsState()
    val paperTone by viewModel.readerPaperTone.collectAsState()
    val annotations by viewModel.bookAnnotations.collectAsState()

    var showSettingsDrawer by remember { mutableStateOf(false) }
    var showAnnotationsDrawer by remember { mutableStateOf(false) }
    var showAddAnnotationDialog by remember { mutableStateOf(false) }
    var showSearchDialog by remember { mutableStateOf(false) }

    var selectedAnnotationType by remember { mutableStateOf("NOTE") } // "HIGHLIGHT", "BOOKMARK", "NOTE", "IDEA", "QUESTION"
    var annotationContentInput by remember { mutableStateOf("") }
    var annotationQuoteInput by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf("") }
    var drawerFilter by remember { mutableStateOf("ALL") }

    val currentBook = book ?: books.firstOrNull() ?: BookItem(
        title = "No Document Selected",
        author = "Local",
        totalPages = 0,
        currentPage = 0,
        status = "READING"
    )

    val paperBackground = when (paperTone) {
        "Soft Dim" -> SurfaceContainerLow
        "Night" -> NightTone
        else -> SoftPaperTone
    }

    val paperText = if (paperTone == "Night") Color(0xFFE2E2E2) else TextPrimary
    val paperTextSecondary = if (paperTone == "Night") Color(0xFF9E9E9E) else TextSecondary

    // Rendered PDF Page state
    var renderedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isRenderingPage by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val listState = rememberLazyListState()

    // Track scroll position
    LaunchedEffect(listState.firstVisibleItemScrollOffset, listState.firstVisibleItemIndex) {
        val totalItems = listState.layoutInfo.totalItemsCount
        if (totalItems > 0) {
            val progress = (listState.firstVisibleItemIndex.toFloat() + (listState.firstVisibleItemScrollOffset / 1000f)) / totalItems.toFloat()
            viewModel.updateScrollPosition(progress.coerceIn(0f, 1f))
        }
    }

    // Load and render PDF page on-demand with PdfRenderer
    LaunchedEffect(currentBook.fileUri, page, zoomLevel) {
        isRenderingPage = true
        val targetFile = if (currentBook.fileUri.isNotEmpty()) {
            File(currentBook.fileUri)
        } else {
            // Check default sample PDF
            val sampleFile = File(context.filesDir, "books/sample_atomic_habits.pdf")
            if (sampleFile.exists()) sampleFile else null
        }

        if (targetFile != null && targetFile.exists()) {
            val bmp = PdfBookManager.renderPdfPage(
                pdfFile = targetFile,
                pageIndex = (page - 1).coerceAtLeast(0),
                zoomScale = zoomLevel
            )
            renderedBitmap = bmp
        } else {
            renderedBitmap = null
        }
        isRenderingPage = false
    }

    // Auto-save reading position on exit
    DisposableEffect(Unit) {
        onDispose {
            viewModel.setReaderPage(page)
        }
    }

    // Annotations on current page
    val currentPageAnnotations = remember(annotations, page) {
        annotations.filter { it.page == page }
    }

    val isPageBookmarked = remember(currentPageAnnotations, isBookmarked) {
        isBookmarked || currentPageAnnotations.any { it.type == "BOOKMARK" }
    }

    Box(modifier = Modifier.fillMaxSize().background(CanvasSurface)) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header Top Bar
            Surface(
                color = CanvasSurface,
                shadowElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("reader_back_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { showAnnotationsDrawer = true }
                    ) {
                        Text(
                            text = currentBook.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary,
                            maxLines = 1
                        )
                        Text(
                            text = "Page $page of ${currentBook.totalPages} · ${((page.toFloat() / currentBook.totalPages.coerceAtLeast(1)) * 100).toInt()}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextOutline
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { showSearchDialog = true },
                            modifier = Modifier.testTag("reader_search_button")
                        ) {
                            Icon(Icons.Default.Search, contentDescription = "Search", tint = TextOutline, modifier = Modifier.size(20.dp))
                        }
                        IconButton(
                            onClick = { showAnnotationsDrawer = !showAnnotationsDrawer },
                            modifier = Modifier.testTag("reader_notes_button")
                        ) {
                            Box {
                                Icon(Icons.AutoMirrored.Filled.FormatListBulleted, contentDescription = "Annotations", tint = if (annotations.isNotEmpty()) InkBlack else TextOutline, modifier = Modifier.size(20.dp))
                                if (annotations.isNotEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .clip(CircleShape)
                                            .background(PetalAccent)
                                            .align(Alignment.TopEnd)
                                    )
                                }
                            }
                        }
                        IconButton(
                            onClick = { viewModel.toggleBookmark() },
                            modifier = Modifier.testTag("reader_bookmark_button")
                        ) {
                            Icon(
                                imageVector = if (isPageBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                contentDescription = "Bookmark",
                                tint = if (isPageBookmarked) InkBlack else TextOutline,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        IconButton(
                            onClick = { showSettingsDrawer = !showSettingsDrawer },
                            modifier = Modifier.testTag("reader_settings_button")
                        ) {
                            Icon(Icons.Default.Tune, contentDescription = "Appearance", tint = TextOutline, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }

            // Reader Scrollable Content
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // Status pill with calm reading indicators
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(SurfaceContainerLow)
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            PetalDot(size = 5.dp)
                            Text(
                                text = "Restored p. $page • ${(zoomLevel * 100).toInt()}% zoom • ${currentPageAnnotations.size} annotations on page",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextOutline
                            )
                        }
                    }
                }

                // Native PDF or Editorial Paper Canvas
                item {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = paperBackground,
                        shadowElevation = 2.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, BorderOutline.copy(alpha = 0.4f), RoundedCornerShape(18.dp))
                    ) {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            // Bookmark ribbon
                            if (isPageBookmarked) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(end = 24.dp)
                                        .width(16.dp)
                                        .height(34.dp)
                                        .clip(RoundedCornerShape(bottomStart = 4.dp, bottomEnd = 4.dp))
                                        .background(PetalAccent)
                                )
                            }

                            if (renderedBitmap != null) {
                                // Native PDF Rendered Page View
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Image(
                                        bitmap = renderedBitmap!!.asImageBitmap(),
                                        contentDescription = "PDF Page $page of ${currentBook.title}",
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .pointerInput(Unit) {
                                                detectTransformGestures { _, _, zoom, _ ->
                                                    val newZoom = (zoomLevel * zoom).coerceIn(1.0f, 3.0f)
                                                    viewModel.updateZoomLevel(newZoom)
                                                }
                                            }
                                    )
                                }
                            } else if (isRenderingPage) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(380.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        CircularProgressIndicator(
                                            color = InkBlack,
                                            modifier = Modifier.size(32.dp)
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Text("Rendering page $page...", style = MaterialTheme.typography.bodySmall, color = TextOutline)
                                    }
                                }
                            } else {
                                // Formatted Native Text Canvas (Editorial View)
                                Column(modifier = Modifier.padding(22.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = currentBook.title.uppercase(),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = paperTextSecondary,
                                            letterSpacing = 1.sp
                                        )
                                        Text(
                                            text = "p. $page / ${currentBook.totalPages}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = paperTextSecondary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(18.dp))

                                    Text(
                                        text = "CHAPTER 16 · THE 1ST LAW",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = paperTextSecondary,
                                        letterSpacing = 1.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Make It Obvious",
                                        style = MaterialTheme.typography.headlineLarge,
                                        color = paperText
                                    )

                                    Spacer(modifier = Modifier.height(18.dp))

                                    Text(
                                        text = "The human sensory apparatus is exceptionally tuned to visual cues. A small change in what you see can lead to a massive shift in what you do. As a result, you can imagine how important it is to live and work in environments that are filled with productive cues and devoid of unproductive ones.",
                                        fontSize = fontSizeSp.sp,
                                        lineHeight = (fontSizeSp * 1.6f).sp,
                                        color = paperText
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))

                                    // Highlight card block
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = PetalAccent.copy(alpha = 0.25f),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                selectedAnnotationType = "NOTE"
                                                showAddAnnotationDialog = true
                                            }
                                            .padding(vertical = 6.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(14.dp)) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                    PetalDot(size = 6.dp)
                                                    Text(
                                                        text = "KEY HABIT PRINCIPLE",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = InkBlack,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                                Icon(Icons.Default.Edit, contentDescription = "Edit Note", tint = TextOutline, modifier = Modifier.size(16.dp))
                                            }
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = "\"You do not rise to the level of your goals. You fall to the level of your systems. Your goal is your desired outcome. Your system is the collection of daily habits that will get you there.\"",
                                                style = MaterialTheme.typography.bodyLarge,
                                                fontStyle = FontStyle.Italic,
                                                fontWeight = FontWeight.Medium,
                                                color = InkBlack,
                                                lineHeight = 22.sp
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))

                                    Text(
                                        text = "When you design your environment so that good cues are prominent and obvious, discipline becomes natural. Clarity precedes execution. When the morning devotional book rests open beside your quiet seat, beginning requires zero cognitive friction.",
                                        fontSize = fontSizeSp.sp,
                                        lineHeight = (fontSizeSp * 1.6f).sp,
                                        color = paperText
                                    )
                                }
                            }
                        }
                    }
                }

                // Persistent Annotations on this page (Remain visible whenever user returns to book!)
                if (currentPageAnnotations.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                PetalDot(size = 5.dp)
                                Text(
                                    text = "Annotations on Page $page (${currentPageAnnotations.size})",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            }
                            Text(
                                text = "Persisted in Room",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextOutline
                            )
                        }
                    }
                    items(currentPageAnnotations, key = { it.id }) { ann ->
                        AnnotationCard(
                            annotation = ann,
                            onDelete = { viewModel.deleteAnnotation(ann) },
                            onCopy = { viewModel.emitToast("Copied ${ann.type} to clipboard") }
                        )
                    }
                }

                // Bottom spacer
                item {
                    Spacer(modifier = Modifier.height(90.dp))
                }
            }

            // Bottom Navigation & Controls Bar
            Surface(
                color = CanvasSurface,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    // Page navigation row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { viewModel.prevPage() },
                            enabled = page > 1,
                            modifier = Modifier.testTag("reader_prev_page")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Previous Page", tint = if (page > 1) TextPrimary else TextOutline.copy(alpha = 0.3f))
                        }

                        // Page Jump / Slider
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                        ) {
                            Slider(
                                value = page.toFloat(),
                                onValueChange = { viewModel.setReaderPage(it.toInt()) },
                                valueRange = 1f..currentBook.totalPages.coerceAtLeast(1).toFloat(),
                                colors = SliderDefaults.colors(
                                    thumbColor = InkBlack,
                                    activeTrackColor = InkBlack,
                                    inactiveTrackColor = SurfaceContainerHighest
                                ),
                                modifier = Modifier.weight(1f).testTag("reader_page_slider")
                            )

                            Text(
                                text = "$page / ${currentBook.totalPages}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }

                        IconButton(
                            onClick = { viewModel.nextPage() },
                            modifier = Modifier.testTag("reader_next_page")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Next Page", tint = TextPrimary)
                        }
                    }

                    // Zoom & Annotation Quick Action
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            IconButton(
                                onClick = { viewModel.updateZoomLevel(zoomLevel - 0.2f) },
                                enabled = zoomLevel > 1.0f,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(Icons.Default.ZoomOut, contentDescription = "Zoom Out", tint = TextOutline, modifier = Modifier.size(18.dp))
                            }
                            Text(
                                text = "${(zoomLevel * 100).toInt()}%",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextOutline
                            )
                            IconButton(
                                onClick = { viewModel.updateZoomLevel(zoomLevel + 0.2f) },
                                enabled = zoomLevel < 3.0f,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(Icons.Default.ZoomIn, contentDescription = "Zoom In", tint = TextOutline, modifier = Modifier.size(18.dp))
                            }
                        }

                        // Annotation Creation Launcher
                        Button(
                            onClick = { showAddAnnotationDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = InkBlack,
                                contentColor = InkWhite
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.height(36.dp).testTag("reader_add_note_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("+ Annotate Page", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }

        // Appearance Settings Drawer / Dialog
        if (showSettingsDrawer) {
            AlertDialog(
                onDismissRequest = { showSettingsDrawer = false },
                title = { Text("Reader Appearance", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) },
                text = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text("Paper Tone", style = MaterialTheme.typography.labelSmall, color = TextOutline)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf("Linen", "Soft Dim", "Night").forEach { tone ->
                                val isSelected = paperTone == tone
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSelected) InkBlack else SurfaceContainerHigh,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { viewModel.setReaderPaperTone(tone) }
                                ) {
                                    Text(
                                        text = tone,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isSelected) Color.White else TextPrimary,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(vertical = 10.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text("Type Scale ($fontSizeSp sp)", style = MaterialTheme.typography.labelSmall, color = TextOutline)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            TextButton(onClick = { viewModel.adjustReaderFontSize(-1) }) {
                                Text("A-", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                            }
                            Slider(
                                value = fontSizeSp.toFloat(),
                                onValueChange = { viewModel.adjustReaderFontSize((it.toInt() - fontSizeSp)) },
                                valueRange = 13f..26f,
                                modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                            )
                            TextButton(onClick = { viewModel.adjustReaderFontSize(1) }) {
                                Text("A+", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = TextPrimary)
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showSettingsDrawer = false }) {
                        Text("Done", color = InkBlack, fontWeight = FontWeight.Bold)
                    }
                },
                containerColor = CanvasSurface
            )
        }

        // Add Real Annotation Dialog (Supporting: HIGHLIGHT, BOOKMARK, NOTE, IDEA, QUESTION)
        if (showAddAnnotationDialog) {
            AlertDialog(
                onDismissRequest = { showAddAnnotationDialog = false },
                title = { Text("Annotate Page $page", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) },
                text = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text("Annotation Type", style = MaterialTheme.typography.labelSmall, color = TextOutline)
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // 5 Types Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val types = listOf(
                                Triple("HIGHLIGHT", "Highlight", Icons.Default.BorderColor),
                                Triple("NOTE", "Note", Icons.Default.EditNote),
                                Triple("IDEA", "Idea", Icons.Default.Lightbulb),
                                Triple("QUESTION", "Question", Icons.AutoMirrored.Filled.HelpOutline),
                                Triple("BOOKMARK", "Bookmark", Icons.Default.Bookmark)
                            )
                            types.forEach { (typeKey, typeLabel, typeIcon) ->
                                val isSelected = selectedAnnotationType == typeKey
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(if (isSelected) InkBlack else SurfaceContainerLow)
                                        .border(1.dp, if (isSelected) InkBlack else BorderOutline.copy(alpha = 0.5f), CircleShape)
                                        .clickable { selectedAnnotationType = typeKey }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = typeIcon,
                                            contentDescription = null,
                                            tint = if (isSelected) InkWhite else TextOutline,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            text = typeLabel,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                            color = if (isSelected) InkWhite else TextPrimary
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if (selectedAnnotationType == "HIGHLIGHT") {
                            OutlinedTextField(
                                value = annotationQuoteInput,
                                onValueChange = { annotationQuoteInput = it },
                                placeholder = { Text("Passage or quote from page $page...", style = MaterialTheme.typography.bodySmall) },
                                label = { Text("Selected Quote") },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = InkBlack,
                                    unfocusedBorderColor = BorderOutline
                                )
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        OutlinedTextField(
                            value = annotationContentInput,
                            onValueChange = { annotationContentInput = it },
                            placeholder = {
                                val hint = when (selectedAnnotationType) {
                                    "HIGHLIGHT" -> "Add optional takeaway or thought..."
                                    "BOOKMARK" -> "e.g., Important turning point in chapter 16..."
                                    "IDEA" -> "Insight, idea to implement, or connection..."
                                    "QUESTION" -> "What question does this passage raise?..."
                                    else -> "Record note or reflection..."
                                }
                                Text(hint, style = MaterialTheme.typography.bodyMedium)
                            },
                            label = { Text("Annotation Content") },
                            modifier = Modifier.fillMaxWidth().height(120.dp).testTag("reader_note_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = InkBlack,
                                unfocusedBorderColor = BorderOutline
                            )
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val contentToSave = if (annotationContentInput.isNotBlank()) {
                                annotationContentInput.trim()
                            } else if (annotationQuoteInput.isNotBlank()) {
                                annotationQuoteInput.trim()
                            } else {
                                "${selectedAnnotationType.lowercase().replaceFirstChar { it.uppercase() }} on page $page"
                            }

                            viewModel.addAnnotation(
                                type = selectedAnnotationType,
                                content = contentToSave,
                                position = scrollPos,
                                quoteSnippet = annotationQuoteInput.trim()
                            )
                            annotationContentInput = ""
                            annotationQuoteInput = ""
                            showAddAnnotationDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = InkBlack),
                        modifier = Modifier.testTag("reader_save_note_button")
                    ) {
                        Text("Save to Room", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddAnnotationDialog = false }) {
                        Text("Cancel", color = TextOutline)
                    }
                },
                containerColor = CanvasSurface
            )
        }

        // Search in Document Dialog
        if (showSearchDialog) {
            AlertDialog(
                onDismissRequest = { showSearchDialog = false },
                title = { Text("Search Document", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) },
                text = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search terms or chapters...", style = MaterialTheme.typography.bodyMedium) },
                            modifier = Modifier.fillMaxWidth().testTag("reader_search_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = InkBlack,
                                unfocusedBorderColor = BorderOutline
                            )
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (searchQuery.isNotEmpty()) "Found occurrences across pages in ${currentBook.title}" else "Enter keywords to scan document index",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextOutline
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.emitToast("Scanning '${searchQuery}' in ${currentBook.title}")
                            showSearchDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = InkBlack)
                    ) {
                        Text("Search", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showSearchDialog = false }) {
                        Text("Close", color = TextOutline)
                    }
                },
                containerColor = CanvasSurface
            )
        }

        // Filterable Book Annotations Drawer with Tap-to-Navigate
        if (showAnnotationsDrawer) {
            AlertDialog(
                onDismissRequest = { showAnnotationsDrawer = false },
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Book Annotations (${annotations.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        IconButton(onClick = { showAnnotationsDrawer = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = TextOutline)
                        }
                    }
                },
                text = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Filter row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("ALL", "HIGHLIGHT", "NOTE", "IDEA", "QUESTION", "BOOKMARK").forEach { flt ->
                                val isSelected = drawerFilter == flt
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(if (isSelected) InkBlack else SurfaceContainerLow)
                                        .clickable { drawerFilter = flt }
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = flt,
                                        fontSize = 10.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) InkWhite else TextPrimary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        val filteredList = remember(annotations, drawerFilter) {
                            if (drawerFilter == "ALL") annotations else annotations.filter { it.type == drawerFilter }
                        }

                        if (filteredList.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No annotations found in this view.", style = MaterialTheme.typography.bodySmall, color = TextOutline)
                            }
                        } else {
                            LazyColumn(modifier = Modifier.fillMaxWidth().height(280.dp)) {
                                items(filteredList, key = { it.id }) { ann ->
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = SurfaceContainerLowest,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                            .border(1.dp, BorderOutline.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                                            .clickable {
                                                viewModel.navigateToAnnotation(ann)
                                                showAnnotationsDrawer = false
                                            }
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                    Icon(
                                                        imageVector = getAnnotationIcon(ann.type),
                                                        contentDescription = null,
                                                        tint = InkBlack,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                    Text("Page ${ann.page} · ${ann.type}", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = TextPrimary)
                                                }
                                                IconButton(
                                                    onClick = { viewModel.deleteAnnotation(ann) },
                                                    modifier = Modifier.size(24.dp)
                                                ) {
                                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = TextOutline, modifier = Modifier.size(14.dp))
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(ann.content, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                            if (ann.quoteSnippet.isNotBlank()) {
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = "\"${ann.quoteSnippet}\"",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    fontStyle = FontStyle.Italic,
                                                    color = TextOutline,
                                                    fontSize = 11.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showAnnotationsDrawer = false }) {
                        Text("Done", color = InkBlack, fontWeight = FontWeight.Bold)
                    }
                },
                containerColor = CanvasSurface
            )
        }
    }
}

@Composable
private fun AnnotationCard(
    annotation: Annotation,
    onDelete: () -> Unit,
    onCopy: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = SurfaceContainerLowest,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .border(1.dp, BorderOutline.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(SurfaceContainerLow),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getAnnotationIcon(annotation.type),
                        contentDescription = null,
                        tint = InkBlack,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = annotation.type,
                            style = MaterialTheme.typography.labelSmall,
                            color = InkBlack,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "• Page ${annotation.page}",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextOutline
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = annotation.content,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary
                    )
                    if (annotation.quoteSnippet.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "\"${annotation.quoteSnippet}\"",
                            style = MaterialTheme.typography.bodySmall,
                            fontStyle = FontStyle.Italic,
                            color = TextOutline
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onCopy, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = TextOutline, modifier = Modifier.size(16.dp))
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = TextOutline, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

private fun getAnnotationIcon(type: String): ImageVector {
    return when (type.uppercase()) {
        "HIGHLIGHT" -> Icons.Default.BorderColor
        "BOOKMARK" -> Icons.Default.Bookmark
        "NOTE" -> Icons.Default.EditNote
        "IDEA" -> Icons.Default.Lightbulb
        "QUESTION" -> Icons.AutoMirrored.Filled.HelpOutline
        else -> Icons.Default.EditNote
    }
}

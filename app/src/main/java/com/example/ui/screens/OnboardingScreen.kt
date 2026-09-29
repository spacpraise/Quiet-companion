package com.example.ui.screens

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.LaptopChromebook
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.WbTwilight
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
import com.example.ui.theme.SurfaceContainerHighest
import com.example.ui.theme.SurfaceContainerLow
import com.example.ui.theme.SurfaceContainerLowest
import com.example.ui.theme.TextOutline
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun OnboardingScreen(
    viewModel: CompanionViewModel,
    onComplete: () -> Unit
) {
    val step by viewModel.onboardingStep.collectAsState()
    val wakeTime by viewModel.wakeTime.collectAsState()
    val selectedPractices by viewModel.selectedPractices.collectAsState()
    val naturalRoutine by viewModel.naturalRoutineText.collectAsState()

    val pdfPickerLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.OpenDocument()
    ) { uri: android.net.Uri? ->
        if (uri != null) {
            viewModel.importPdfFromUri(uri)
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(CanvasSurface)) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (step > 1) {
                    IconButton(onClick = { viewModel.setOnboardingStep(step - 1) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                } else {
                    Spacer(modifier = Modifier.size(48.dp))
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    PetalDot(size = 6.dp)
                    Text(
                        text = when (step) {
                            1 -> "QUIET COMPANION"
                            2 -> "MORNING CADENCE"
                            3 -> "PRACTICES"
                            4 -> "DOCUMENT SETUP"
                            else -> "CADENCE SYNTHESIS"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = TextOutline,
                        letterSpacing = 1.sp
                    )
                }

                Text(
                    text = "Skip",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextOutline,
                    modifier = Modifier
                        .clickable {
                            viewModel.finishOnboarding()
                            onComplete()
                        }
                        .padding(horizontal = 8.dp)
                )
            }

            // Progress bar
            if (step > 1) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 4.dp)
                        .height(3.dp)
                        .clip(CircleShape)
                        .background(SurfaceContainerHigh)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(step / 5f)
                            .height(3.dp)
                            .background(InkBlack)
                    )
                }
            }

            // Content
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                when (step) {
                    1 -> {
                        item { Step1Welcome(onBegin = { viewModel.setOnboardingStep(2) }) }
                    }
                    2 -> {
                        item {
                            Step2WakeTime(
                                wakeTime = wakeTime,
                                onSelectTime = { viewModel.setWakeTime(it) },
                                onNext = { viewModel.setOnboardingStep(3) }
                            )
                        }
                    }
                    3 -> {
                        item {
                            Step3Practices(
                                selected = selectedPractices,
                                onToggle = { viewModel.togglePractice(it) },
                                onNext = { viewModel.setOnboardingStep(4) }
                            )
                        }
                    }
                    4 -> {
                        item {
                            Step4PdfImport(
                                onBrowsePdf = {
                                    pdfPickerLauncher.launch(arrayOf("application/pdf"))
                                },
                                onNext = { viewModel.setOnboardingStep(5) }
                            )
                        }
                    }
                    5 -> {
                        item {
                            Step5NaturalFlow(
                                routineText = naturalRoutine,
                                onTextChange = { viewModel.setNaturalRoutineText(it) },
                                onNext = {
                                    viewModel.finishOnboarding()
                                    onComplete()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Step1Welcome(onBegin: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Zen artwork
        Box(
            modifier = Modifier
                .size(240.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(SurfaceContainerLowest)
                .border(1.dp, BorderOutline.copy(alpha = 0.5f), RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center
        ) {
            val artRes = com.example.R.drawable.onboarding_zen_art_1790510112137
            Image(
                painter = painterResource(id = artRes),
                contentDescription = "Zen sunrise reading artwork",
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(220.dp)
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = "Put your day back together.",
            style = MaterialTheme.typography.headlineLarge,
            color = TextPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "A calm, dedicated sanctuary to restore your natural reading cadence, intentional routines, scripture & prayer intercessions, and focused stillness.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp,
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Progress indicators
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Box(modifier = Modifier.size(24.dp, 4.dp).clip(CircleShape).background(InkBlack))
            Box(modifier = Modifier.size(6.dp, 4.dp).clip(CircleShape).background(SurfaceContainerHigh))
            Box(modifier = Modifier.size(6.dp, 4.dp).clip(CircleShape).background(SurfaceContainerHigh))
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onBegin,
            colors = ButtonDefaults.buttonColors(containerColor = InkBlack),
            shape = CircleShape,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("begin_cadence_button")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Begin your cadence",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = InkWhite
                )
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = InkWhite, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun Step2WakeTime(
    wakeTime: String,
    onSelectTime: (String) -> Unit,
    onNext: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "What time do you usually wake up?",
            style = MaterialTheme.typography.headlineLarge,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Your morning anchor sets the rhythm for your quiet reading, spiritual devotion, and focused blocks.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Zen Dial Center Readout
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = SurfaceContainerLowest,
            shadowElevation = 2.dp,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, BorderOutline.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Default.WbTwilight, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(16.dp))
                    Text("FIRST GLOW", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = TextOutline)
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = wakeTime.substringBefore(" "),
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(SurfaceContainerHigh)
                        .padding(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(InkBlack)
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text("AM", style = MaterialTheme.typography.labelSmall, color = InkWhite, fontWeight = FontWeight.Bold)
                    }
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text("PM", style = MaterialTheme.typography.labelSmall, color = TextOutline)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Curated Anchors
        Text("CURATED ANCHORS", style = MaterialTheme.typography.labelSmall, color = TextOutline, letterSpacing = 1.sp)
        Spacer(modifier = Modifier.height(8.dp))

        val anchors = listOf(
            "05:30 AM" to "(Early Light)",
            "06:00 AM" to "(Dawn)",
            "06:45 AM" to "(Gentle Start)",
            "07:30 AM" to "(Clear Awakening)"
        )

        anchors.forEach { (timeStr, desc) ->
            val isSelected = wakeTime == timeStr
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isSelected) InkBlack else SurfaceContainerLowest,
                shadowElevation = 1.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .border(1.dp, if (isSelected) InkBlack else BorderOutline.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .clickable { onSelectTime(timeStr) }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PetalDot(size = 6.dp)
                        Text(
                            text = timeStr,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isSelected) InkWhite else TextPrimary
                        )
                        Text(
                            text = desc,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isSelected) SurfaceContainerHigh else TextOutline
                        )
                    }
                    if (isSelected) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = PetalAccent, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onNext,
            colors = ButtonDefaults.buttonColors(containerColor = InkBlack),
            shape = CircleShape,
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text("Continue", color = InkWhite, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun Step3Practices(
    selected: Set<String>,
    onToggle: (String) -> Unit,
    onNext: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "What do you want to make time for?",
            style = MaterialTheme.typography.headlineLarge,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Select the practices you wish to protect and weave into your quiet daily cadence.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(16.dp))

        val practices = listOf(
            Triple("Bible", "Scripture & morning reflection", Icons.Default.AutoStories),
            Triple("Reading", "Books, essays, and deep literature", Icons.AutoMirrored.Filled.MenuBook),
            Triple("Study", "Academic coursework & research", Icons.Default.HistoryEdu),
            Triple("Exercise", "Movement, walks, & physical grounding", Icons.AutoMirrored.Filled.DirectionsWalk),
            Triple("Prayer", "Stillness, intercession, & meditation", Icons.Default.SelfImprovement),
            Triple("Work", "Dedicated deep work blocks", Icons.Default.LaptopChromebook)
        )

        practices.forEach { (name, desc, icon) ->
            val isSelected = selected.contains(name)
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = if (isSelected) SurfaceContainerLowest else SurfaceContainerLow,
                shadowElevation = if (isSelected) 2.dp else 0.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .border(
                        1.dp,
                        if (isSelected) BorderOutline.copy(alpha = 0.6f) else Color.Transparent,
                        RoundedCornerShape(14.dp)
                    )
                    .clickable { onToggle(name) }
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) SurfaceContainer else SurfaceContainerHigh),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(icon, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(20.dp))
                        }
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                                if (isSelected) PetalDot(size = 5.dp)
                            }
                            Text(desc, style = MaterialTheme.typography.bodySmall, color = TextOutline)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSelected) InkBlack else SurfaceContainerHigh),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = InkWhite, modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = onNext,
            colors = ButtonDefaults.buttonColors(containerColor = InkBlack),
            shape = CircleShape,
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text("Continue (${selected.size} selected)", color = InkWhite, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun Step4PdfImport(
    onBrowsePdf: () -> Unit,
    onNext: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Add your first book",
            style = MaterialTheme.typography.headlineLarge,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Import a PDF from your device storage, or continue to start with an empty shelf.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Dropzone card
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = SurfaceContainerLow,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onBrowsePdf() }
        ) {
            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(SurfaceContainerLowest),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Description, contentDescription = null, tint = InkBlack, modifier = Modifier.size(26.dp))
                }
                Spacer(modifier = Modifier.height(14.dp))
                Text("Select PDF from files", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Local-first and saved offline", style = MaterialTheme.typography.bodySmall, color = TextOutline)

                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(SurfaceContainerLowest)
                        .border(1.dp, BorderOutline.copy(alpha = 0.5f), CircleShape)
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                ) {
                    Text("Browse files", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onNext,
            colors = ButtonDefaults.buttonColors(containerColor = InkBlack),
            shape = CircleShape,
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text("Continue", color = InkWhite, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun Step5NaturalFlow(
    routineText: String,
    onTextChange: (String) -> Unit,
    onNext: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "What does a normal day look like?",
            style = MaterialTheme.typography.headlineLarge,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Describe your flow in your own words. The companion will parse your rhythms and synthesize an intentional cadence.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Reflection Engine Input Card
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = SurfaceContainerLowest,
            shadowElevation = 2.dp,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, BorderOutline.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(16.dp))
                        PetalDot(size = 5.dp)
                        Text("Quiet Reflection Engine", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = TextPrimary)
                    }
                    Icon(Icons.Default.Mic, contentDescription = "Dictate", tint = TextOutline, modifier = Modifier.size(18.dp))
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = routineText,
                    onValueChange = onTextChange,
                    minLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text("QUICK FLOW PROMPTS", style = MaterialTheme.typography.labelSmall, color = TextOutline, letterSpacing = 1.sp)
        Spacer(modifier = Modifier.height(8.dp))

        val prompts = listOf(
            "Wake at 6, morning Scripture, work until 5, evening chapter",
            "Early devotion, midday study block, quiet reading at 9 PM",
            "Focused mornings, open afternoons, twilight reflection"
        )

        prompts.forEach { prompt ->
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = SurfaceContainerLowest,
                shadowElevation = 1.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 3.dp)
                    .clickable { onTextChange(prompt) }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        PetalDot(size = 5.dp)
                        Text("\"$prompt\"", style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                    }
                    Icon(Icons.Default.Add, contentDescription = null, tint = TextOutline, modifier = Modifier.size(16.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onNext,
            colors = ButtonDefaults.buttonColors(containerColor = InkBlack),
            shape = CircleShape,
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = InkWhite, modifier = Modifier.size(18.dp))
                Text("Synthesize Proposed Routine", color = InkWhite, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

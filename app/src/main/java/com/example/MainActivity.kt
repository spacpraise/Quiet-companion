package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ui.CompanionViewModel
import com.example.ui.Screen
import com.example.ui.components.CelebrationCompletionDialog
import com.example.ui.components.FloatingToast
import com.example.ui.components.QuietBottomNavBar
import com.example.ui.screens.ActivityScreen
import com.example.ui.screens.BookCompletionScreen
import com.example.ui.screens.BookDetailScreen
import com.example.ui.screens.CompanionScreen
import com.example.ui.screens.InsightsScreen
import com.example.ui.screens.IntercessionSanctuaryScreen
import com.example.ui.screens.LibraryScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.ReaderScreen
import com.example.ui.screens.RoutineScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.TaskCreationScreen
import com.example.ui.screens.TodayScreen
import com.example.ui.theme.CanvasSurface
import com.example.ui.theme.QuietCompanionTheme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {

    private val viewModel: CompanionViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handleIntent(intent)

        setContent {

            QuietCompanionTheme {
                val currentScreen by viewModel.currentScreen.collectAsState()
                val activeCompletionModal by viewModel.activeCompletionModal.collectAsState()
                var toastMessage by remember { mutableStateOf<String?>(null) }
                var isToastVisible by remember { mutableStateOf(false) }

                LaunchedEffect(Unit) {
                    viewModel.toastEvent.collect { msg ->
                        toastMessage = msg
                        isToastVisible = true
                        delay(2400)
                        isToastVisible = false
                    }
                }

                BackHandler(enabled = currentScreen != Screen.TODAY) {
                    viewModel.navigateBack()
                }

                Scaffold(
                    containerColor = CanvasSurface,
                    contentWindowInsets = WindowInsets.safeDrawing,
                    bottomBar = {
                        val shouldShowBottomNav = currentScreen in listOf(
                            Screen.TODAY,
                            Screen.ROUTINE,
                            Screen.LIBRARY,
                            Screen.COMPANION,
                            Screen.ACTIVITY
                        )
                        if (shouldShowBottomNav) {
                            QuietBottomNavBar(
                                currentScreen = currentScreen,
                                unreadNotifCount = 2,
                                onNavigate = { viewModel.navigateTo(it) }
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        AnimatedContent(
                            targetState = currentScreen,
                            transitionSpec = { fadeIn() togetherWith fadeOut() },
                            label = "ScreenTransition"
                        ) { targetScreen ->
                            when (targetScreen) {
                                Screen.ONBOARDING -> {
                                    OnboardingScreen(
                                        viewModel = viewModel,
                                        onComplete = { viewModel.navigateTo(Screen.TODAY) }
                                    )
                                }
                                Screen.TODAY -> {
                                    TodayScreen(
                                        viewModel = viewModel,
                                        onNavigate = { viewModel.navigateTo(it) }
                                    )
                                }
                                Screen.ROUTINE -> {
                                    RoutineScreen(
                                        viewModel = viewModel,
                                        onNavigate = { viewModel.navigateTo(it) }
                                    )
                                }
                                Screen.TASK_CREATION -> {
                                    TaskCreationScreen(
                                        viewModel = viewModel,
                                        onNavigate = { viewModel.navigateTo(it) },
                                        onBack = { viewModel.navigateBack() }
                                    )
                                }
                                Screen.LIBRARY -> {
                                    LibraryScreen(
                                        viewModel = viewModel,
                                        onNavigate = { viewModel.navigateTo(it) }
                                    )
                                }
                                Screen.BOOK_DETAIL -> {
                                    BookDetailScreen(
                                        viewModel = viewModel,
                                        onNavigate = { viewModel.navigateTo(it) },
                                        onBack = { viewModel.navigateBack() }
                                    )
                                }
                                Screen.READER -> {
                                    ReaderScreen(
                                        viewModel = viewModel,
                                        onBack = { viewModel.navigateBack() }
                                    )
                                }
                                Screen.COMPANION -> {
                                    CompanionScreen(
                                        viewModel = viewModel,
                                        onNavigate = { viewModel.navigateTo(it) }
                                    )
                                }
                                Screen.ACTIVITY -> {
                                    ActivityScreen(
                                        viewModel = viewModel,
                                        onNavigate = { viewModel.navigateTo(it) }
                                    )
                                }
                                Screen.INSIGHTS -> {
                                    InsightsScreen(
                                        viewModel = viewModel,
                                        onNavigate = { viewModel.navigateTo(it) }
                                    )
                                }
                                Screen.SETTINGS -> {
                                    SettingsScreen(
                                        viewModel = viewModel,
                                        onNavigate = { viewModel.navigateTo(it) },
                                        onBack = { viewModel.navigateBack() }
                                    )
                                }
                                Screen.INTERCESSIONS -> {
                                    IntercessionSanctuaryScreen(
                                        viewModel = viewModel,
                                        onBack = { viewModel.navigateBack() }
                                    )
                                }
                                Screen.BOOK_COMPLETION -> {
                                    BookCompletionScreen(
                                        viewModel = viewModel,
                                        onNavigate = { viewModel.navigateTo(it) }
                                    )
                                }
                            }
                        }

                        // Floating Toast
                        FloatingToast(
                            message = toastMessage,
                            visible = isToastVisible,
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 16.dp)
                        )

                        // Celebration Completion Dialog
                        activeCompletionModal?.let { modalType ->
                            CelebrationCompletionDialog(
                                type = modalType,
                                onDismiss = { viewModel.dismissCompletionModal() },
                                onPrimaryAction = {
                                    viewModel.emitToast("Reflection anchored in your cadence journal")
                                    viewModel.dismissCompletionModal()
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        val screenName = intent?.getStringExtra("EXTRA_SCREEN") ?: return
        val targetId = intent.getLongExtra("EXTRA_TARGET_ID", 0L)
        when (screenName.uppercase()) {
            "READER" -> {
                if (targetId > 0) {
                    val book = viewModel.books.value.firstOrNull { it.id == targetId }
                    if (book != null) {
                        viewModel.openReaderForBook(book)
                    } else {
                        viewModel.navigateTo(Screen.READER)
                    }
                } else {
                    viewModel.navigateTo(Screen.READER)
                }
            }
            "ROUTINE" -> viewModel.navigateTo(Screen.ROUTINE)
            "TODAY" -> viewModel.navigateTo(Screen.TODAY)
            "LIBRARY" -> viewModel.navigateTo(Screen.LIBRARY)
            "COMPANION" -> viewModel.navigateTo(Screen.COMPANION)
            "INTERCESSIONS" -> viewModel.navigateTo(Screen.INTERCESSIONS)
            "SETTINGS" -> viewModel.navigateTo(Screen.SETTINGS)
        }
    }
}


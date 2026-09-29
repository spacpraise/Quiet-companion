package com.example.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.ClarificationRequest
import com.example.ai.DailyPlanItem
import com.example.ai.GeminiAssistantEngine
import com.example.ai.ProposedDailyPlan
import com.example.ai.ProposedReadingPlan
import com.example.ai.ProposedRoutine
import com.example.alarm.CadenceAlarmScheduler
import com.example.data.CompanionDatabase
import com.example.data.CompanionRepository
import com.example.data.PdfBookManager
import com.example.data.PdfImportResult
import com.example.data.model.Annotation
import com.example.data.model.Book
import com.example.data.model.BookItem
import com.example.data.model.CadenceNotification
import com.example.data.model.CompanionMessage
import com.example.data.model.HighlightItem
import com.example.data.model.IntercessionItem
import com.example.data.model.Notification
import com.example.data.model.ReadingSession
import com.example.data.model.Routine
import com.example.data.model.RoutineItem
import com.example.data.model.Task
import com.example.data.model.UserPreferences
import com.example.data.model.toBook
import com.example.data.model.toBookItem
import com.example.data.model.toCadenceNotification
import com.example.data.model.toHighlightItem
import com.example.data.model.toRoutine
import com.example.data.model.toRoutineItem
import com.example.haptics.CadenceHaptics
import com.example.notification.CadenceNotificationManager
import com.example.reminder.SmartReadingEngine
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


enum class Screen {
    ONBOARDING,
    TODAY,
    ROUTINE,
    TASK_CREATION,
    LIBRARY,
    BOOK_DETAIL,
    READER,
    COMPANION,
    ACTIVITY,
    INSIGHTS,
    SETTINGS,
    INTERCESSIONS,
    BOOK_COMPLETION
}

class CompanionViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: CompanionRepository

    // Database-backed Flows
    val rawBooks: StateFlow<List<Book>>
    val books: StateFlow<List<BookItem>>
    val rawRoutines: StateFlow<List<Routine>>
    val routines: StateFlow<List<RoutineItem>>
    val tasks: StateFlow<List<Task>>
    val rawSessions: StateFlow<List<ReadingSession>>
    val rawNotifications: StateFlow<List<Notification>>
    val notifications: StateFlow<List<CadenceNotification>>
    val userPreferences: StateFlow<UserPreferences?>
    val intercessions: StateFlow<List<IntercessionItem>>
    val companionMessages: StateFlow<List<CompanionMessage>>

    private val _currentScreen = MutableStateFlow(Screen.TODAY)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val _screenHistory = mutableListOf<Screen>()

    private val _selectedBook = MutableStateFlow<BookItem?>(null)
    val selectedBook: StateFlow<BookItem?> = _selectedBook.asStateFlow()

    private val _bookAnnotations = MutableStateFlow<List<Annotation>>(emptyList())
    val bookAnnotations: StateFlow<List<Annotation>> = _bookAnnotations.asStateFlow()

    private val _activeAnnotationFilter = MutableStateFlow("ALL") // "ALL", "HIGHLIGHT", "BOOKMARK", "NOTE", "IDEA", "QUESTION"
    val activeAnnotationFilter: StateFlow<String> = _activeAnnotationFilter.asStateFlow()

    private val _highlights = MutableStateFlow<List<HighlightItem>>(emptyList())
    val highlights: StateFlow<List<HighlightItem>> = _highlights.asStateFlow()

    private val _onboardingStep = MutableStateFlow(1)
    val onboardingStep: StateFlow<Int> = _onboardingStep.asStateFlow()

    private val _profileName = MutableStateFlow("Quiet Reader")
    val profileName: StateFlow<String> = _profileName.asStateFlow()

    private val _profileBio = MutableStateFlow("Local Profile")
    val profileBio: StateFlow<String> = _profileBio.asStateFlow()

    private val _wakeTime = MutableStateFlow("06:00 AM")
    val wakeTime: StateFlow<String> = _wakeTime.asStateFlow()

    private val _bedTime = MutableStateFlow("10:00 PM")
    val bedTime: StateFlow<String> = _bedTime.asStateFlow()

    private val _selectedPractices = MutableStateFlow<Set<String>>(emptySet())
    val selectedPractices: StateFlow<Set<String>> = _selectedPractices.asStateFlow()

    private val _naturalRoutineText = MutableStateFlow("")
    val naturalRoutineText: StateFlow<String> = _naturalRoutineText.asStateFlow()

    // Real Reader State
    private val _readerPage = MutableStateFlow(1)
    val readerPage: StateFlow<Int> = _readerPage.asStateFlow()

    private val _readerScrollPosition = MutableStateFlow(0f)
    val readerScrollPosition: StateFlow<Float> = _readerScrollPosition.asStateFlow()

    private val _readerZoomLevel = MutableStateFlow(1.0f)
    val readerZoomLevel: StateFlow<Float> = _readerZoomLevel.asStateFlow()

    private val _readerFontSizeSp = MutableStateFlow(16)
    val readerFontSizeSp: StateFlow<Int> = _readerFontSizeSp.asStateFlow()

    private val _readerPaperTone = MutableStateFlow("Linen") // "Linen", "Soft Dim", "Night"
    val readerPaperTone: StateFlow<String> = _readerPaperTone.asStateFlow()

    private val _isBookmarked = MutableStateFlow(false)
    val isBookmarked: StateFlow<Boolean> = _isBookmarked.asStateFlow()

    private val _isShelfEmptyView = MutableStateFlow(false)
    val isShelfEmptyView: StateFlow<Boolean> = _isShelfEmptyView.asStateFlow()

    // Import status & error state
    private val _isImportingPdf = MutableStateFlow(false)
    val isImportingPdf: StateFlow<Boolean> = _isImportingPdf.asStateFlow()

    private val _importError = MutableStateFlow<String?>(null)
    val importError: StateFlow<String?> = _importError.asStateFlow()

    // Inspection & shell states: loading, empty, completion
    private val _isDemoLoading = MutableStateFlow(false)
    val isDemoLoading: StateFlow<Boolean> = _isDemoLoading.asStateFlow()

    private val _isDemoEmpty = MutableStateFlow(false)
    val isDemoEmpty: StateFlow<Boolean> = _isDemoEmpty.asStateFlow()

    private val _activeCompletionModal = MutableStateFlow<String?>(null)
    val activeCompletionModal: StateFlow<String?> = _activeCompletionModal.asStateFlow()

    private val _routineFilter = MutableStateFlow("All")
    val routineFilter: StateFlow<String> = _routineFilter.asStateFlow()

    private val _toastEvent = MutableSharedFlow<String>()
    val toastEvent: SharedFlow<String> = _toastEvent.asSharedFlow()

    // Active prayer timer state for Intercessions & Devotion
    private val _isSessionTimerActive = MutableStateFlow(false)
    val isSessionTimerActive: StateFlow<Boolean> = _isSessionTimerActive.asStateFlow()

    private val _sessionRemainingSeconds = MutableStateFlow(30 * 60) // 30 minutes
    val sessionRemainingSeconds: StateFlow<Int> = _sessionRemainingSeconds.asStateFlow()

    // Reading session tracking
    private var sessionStartTime: Long = 0
    private var sessionStartPage: Int = 1

    private val geminiAssistantEngine: GeminiAssistantEngine
    private val moshi: Moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

    init {
        val database = CompanionDatabase.getInstance(application)
        repository = CompanionRepository(database)
        geminiAssistantEngine = GeminiAssistantEngine(repository)

        // Initialize Android notification channels
        CadenceNotificationManager.createNotificationChannels(application)

        rawBooks = repository.allBooks.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        books = repository.allBooks.map { list -> list.map { it.toBookItem() } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        rawRoutines = repository.allRoutines.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        routines = repository.allRoutines.map { list -> list.map { it.toRoutineItem() } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        tasks = repository.allTasks.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        rawSessions = repository.allSessions.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        rawNotifications = repository.allNotifications.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        notifications = repository.allNotifications.map { list -> list.map { it.toCadenceNotification() } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        userPreferences = repository.userPreferences.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
        intercessions = repository.allIntercessions.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        companionMessages = repository.allCompanionMessages.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        viewModelScope.launch {
            userPreferences.collect { prefs ->
                if (prefs != null) {
                    _profileName.value = prefs.profileName
                    _profileBio.value = prefs.profileBio
                    _wakeTime.value = prefs.wakeTime
                    _bedTime.value = prefs.bedTime
                }
            }
        }

        // Evaluate smart reading reminders after database is loaded
        viewModelScope.launch {
            evaluateSmartReminders()
        }

        // Start timer tick coroutine
        viewModelScope.launch {
            while (true) {
                delay(1000)
                if (_isSessionTimerActive.value && _sessionRemainingSeconds.value > 0) {
                    _sessionRemainingSeconds.value -= 1
                    if (_sessionRemainingSeconds.value == 0) {
                        _isSessionTimerActive.value = false
                        performHaptic(CadenceHaptics.HapticType.ALARM_PULSE)
                        emitToast("Peaceful session concluded. Cadence recorded.")
                    }
                }
            }
        }
    }


    fun navigateTo(screen: Screen) {
        if (_currentScreen.value == Screen.READER && screen != Screen.READER) {
            // Persist position when leaving reader
            persistCurrentReadingPosition()
            recordReadingSessionEnd()
        }
        if (_currentScreen.value != screen) {
            _screenHistory.add(_currentScreen.value)
            _currentScreen.value = screen
        }
    }

    fun navigateBack(): Boolean {
        if (_currentScreen.value == Screen.READER) {
            persistCurrentReadingPosition()
            recordReadingSessionEnd()
        }
        if (_screenHistory.isNotEmpty()) {
            _currentScreen.value = _screenHistory.removeAt(_screenHistory.lastIndex)
            return true
        }
        if (_currentScreen.value != Screen.TODAY) {
            _currentScreen.value = Screen.TODAY
            return true
        }
        return false
    }

    fun setOnboardingStep(step: Int) {
        _onboardingStep.value = step.coerceIn(1, 5)
    }

    fun setWakeTime(time: String) {
        _wakeTime.value = time
        emitToast("Wake anchor set to $time")
    }

    fun togglePractice(practice: String) {
        val current = _selectedPractices.value.toMutableSet()
        if (current.contains(practice)) {
            current.remove(practice)
        } else {
            current.add(practice)
        }
        _selectedPractices.value = current
    }

    fun setNaturalRoutineText(text: String) {
        _naturalRoutineText.value = text
    }

    fun acceptProposedRoutine() {
        finishOnboarding()
    }

    fun finishOnboarding() {
        viewModelScope.launch {
            val practices = _selectedPractices.value
            val wake = _wakeTime.value

            // Save user preferences
            val currentPrefs = userPreferences.value ?: UserPreferences()
            repository.updateUserPreferences(currentPrefs.copy(quietHours = "22:00 - $wake"))

            // Create routines ONLY for practices explicitly selected by the user
            if (practices.contains("Bible")) {
                val r = Routine(
                    title = "Morning Scripture & Devotion",
                    type = "prayer",
                    time = wake,
                    days = "Daily",
                    duration = 30,
                    enabled = true,
                    gentleChime = true,
                    hasAlarm = true
                )
                val id = repository.insertRoutine(r)
                val nextMillis = CadenceAlarmScheduler.parseTimeStringToNextMillis(wake)
                CadenceAlarmScheduler.scheduleAlarm(
                    context = getApplication(),
                    alarmId = (id + 1000).toInt(),
                    triggerTimeMillis = nextMillis,
                    title = r.title,
                    body = "${r.title} scheduled for $wake",
                    category = "ROUTINE",
                    destinationScreen = "TODAY",
                    extraId = id,
                    isAlarm = true
                )
            }

            if (practices.contains("Reading")) {
                val r = Routine(
                    title = "Evening Reading",
                    type = "reading",
                    time = "08:00 PM",
                    days = "Daily",
                    duration = 30,
                    enabled = true,
                    gentleChime = true,
                    hasAlarm = false
                )
                repository.insertRoutine(r)
            }

            if (practices.contains("Study")) {
                val r = Routine(
                    title = "Study Focus Block",
                    type = "study",
                    time = "02:00 PM",
                    days = "Mon–Fri",
                    duration = 60,
                    enabled = true,
                    gentleChime = true,
                    hasAlarm = false
                )
                repository.insertRoutine(r)
            }

            if (practices.contains("Exercise")) {
                val r = Routine(
                    title = "Daily Movement",
                    type = "movement",
                    time = "07:00 AM",
                    days = "Daily",
                    duration = 30,
                    enabled = true,
                    gentleChime = true,
                    hasAlarm = false
                )
                repository.insertRoutine(r)
            }

            if (practices.contains("Prayer")) {
                val r = Routine(
                    title = "Stillness & Prayer",
                    type = "prayer",
                    time = "06:30 AM",
                    days = "Daily",
                    duration = 20,
                    enabled = true,
                    gentleChime = true,
                    hasAlarm = false
                )
                repository.insertRoutine(r)
            }

            if (practices.contains("Work")) {
                val r = Routine(
                    title = "Deep Work Block",
                    type = "work",
                    time = "09:00 AM",
                    days = "Mon–Fri",
                    duration = 90,
                    enabled = true,
                    gentleChime = true,
                    hasAlarm = false
                )
                repository.insertRoutine(r)
            }

            emitToast("Cadence saved")
            navigateTo(Screen.TODAY)
        }
    }

    /**
     * Select a book and load its annotations and persisted position.
     */
    fun selectBook(book: BookItem) {
        _selectedBook.value = book
        _readerPage.value = book.currentPage.coerceAtLeast(1)
        _readerScrollPosition.value = book.scrollPosition
        _readerZoomLevel.value = if (book.zoomLevel > 0f) book.zoomLevel else 1.0f

        viewModelScope.launch {
            repository.getAnnotationsForBook(book.id).collect { annList ->
                _bookAnnotations.value = annList
                _highlights.value = annList.map { it.toHighlightItem(book.title) }
            }
        }
    }

    /**
     * Opens the reader at the exact persisted reading position.
     */
    fun openReaderForBook(book: BookItem, page: Int = book.currentPage) {
        selectBook(book)
        _readerPage.value = page.coerceIn(1, book.totalPages.coerceAtLeast(1))
        _readerScrollPosition.value = book.scrollPosition
        _readerZoomLevel.value = if (book.zoomLevel > 0f) book.zoomLevel else 1.0f

        sessionStartTime = System.currentTimeMillis()
        sessionStartPage = _readerPage.value

        navigateTo(Screen.READER)
    }

    /**
     * Continue reading the most recently read book or specific book at exact saved position.
     */
    fun continueReading(book: BookItem? = null) {
        val targetBook = book ?: _selectedBook.value ?: books.value.firstOrNull()
        if (targetBook != null) {
            openReaderForBook(targetBook, targetBook.currentPage)
            emitToast("Restored ${targetBook.title} at p. ${targetBook.currentPage}")
        }
    }

    /**
     * Exact reading position updates & Room persistence
     */
    fun setReaderPage(page: Int) {
        val current = _selectedBook.value ?: return
        val newPage = page.coerceIn(1, current.totalPages.coerceAtLeast(1))
        _readerPage.value = newPage
        persistReadingPosition(current.id, newPage, _readerScrollPosition.value, _readerZoomLevel.value, current.totalPages)
    }

    fun updateScrollPosition(scroll: Float) {
        _readerScrollPosition.value = scroll.coerceIn(0f, 1f)
        val current = _selectedBook.value ?: return
        persistReadingPosition(current.id, _readerPage.value, _readerScrollPosition.value, _readerZoomLevel.value, current.totalPages)
    }

    fun updateZoomLevel(zoom: Float) {
        _readerZoomLevel.value = zoom.coerceIn(1.0f, 3.0f)
        val current = _selectedBook.value ?: return
        persistReadingPosition(current.id, _readerPage.value, _readerScrollPosition.value, _readerZoomLevel.value, current.totalPages)
    }

    private fun persistCurrentReadingPosition() {
        val current = _selectedBook.value ?: return
        persistReadingPosition(current.id, _readerPage.value, _readerScrollPosition.value, _readerZoomLevel.value, current.totalPages)
    }

    private fun persistReadingPosition(
        bookId: Long,
        page: Int,
        scrollPosition: Float,
        zoomLevel: Float,
        totalPages: Int
    ) {
        viewModelScope.launch {
            val progress = if (totalPages > 0) page.toFloat() / totalPages else 0f
            repository.updateReadingPosition(
                bookId = bookId,
                currentPage = page,
                scrollPosition = scrollPosition,
                zoomLevel = zoomLevel,
                progress = progress
            )
            // Update cached selected book
            _selectedBook.value?.let { current ->
                if (current.id == bookId) {
                    _selectedBook.value = current.copy(
                        currentPage = page,
                        scrollPosition = scrollPosition,
                        zoomLevel = zoomLevel
                    )
                }
            }
        }
    }

    private fun recordReadingSessionEnd() {
        val current = _selectedBook.value ?: return
        if (sessionStartTime > 0) {
            val endTime = System.currentTimeMillis()
            val durationSec = (endTime - sessionStartTime) / 1000
            if (durationSec > 2) { // only record meaningful sessions
                viewModelScope.launch {
                    repository.insertSession(
                        ReadingSession(
                            bookId = current.id,
                            startedAt = sessionStartTime,
                            endedAt = endTime,
                            startPage = sessionStartPage,
                            endPage = _readerPage.value,
                            duration = durationSec
                        )
                    )
                }
            }
            sessionStartTime = 0
        }
    }

    fun nextPage() {
        val book = _selectedBook.value ?: return
        if (_readerPage.value < book.totalPages) {
            setReaderPage(_readerPage.value + 1)
        } else {
            finishBook(book)
        }
    }

    fun prevPage() {
        if (_readerPage.value > 1) {
            setReaderPage(_readerPage.value - 1)
        }
    }

    fun toggleBookmark() {
        _isBookmarked.value = !_isBookmarked.value
        val book = _selectedBook.value ?: return
        val page = _readerPage.value
        viewModelScope.launch {
            if (_isBookmarked.value) {
                repository.insertAnnotation(
                    Annotation(
                        bookId = book.id,
                        page = page,
                        position = _readerScrollPosition.value,
                        type = "BOOKMARK",
                        content = "Bookmark on page $page",
                        createdAt = System.currentTimeMillis()
                    )
                )
                emitToast("Page $page bookmarked")
            } else {
                emitToast("Bookmark removed")
            }
        }
    }

    fun addAnnotation(
        type: String,
        content: String,
        position: Float = _readerScrollPosition.value,
        quoteSnippet: String = "",
        color: String = "Default"
    ) {
        val book = _selectedBook.value ?: return
        val page = _readerPage.value
        val validTypes = listOf("HIGHLIGHT", "BOOKMARK", "NOTE", "IDEA", "QUESTION")
        val finalType = if (type.uppercase() in validTypes) type.uppercase() else "NOTE"

        viewModelScope.launch {
            repository.insertAnnotation(
                Annotation(
                    bookId = book.id,
                    page = page,
                    position = position,
                    type = finalType,
                    content = content,
                    quoteSnippet = quoteSnippet,
                    color = color,
                    createdAt = System.currentTimeMillis()
                )
            )
            emitToast("Saved $finalType to page $page")
        }
    }

    fun deleteAnnotation(annotation: Annotation) {
        viewModelScope.launch {
            repository.deleteAnnotation(annotation)
            emitToast("Annotation removed")
        }
    }

    fun updateAnnotation(annotation: Annotation) {
        viewModelScope.launch {
            repository.updateAnnotation(annotation)
            emitToast("Annotation updated")
        }
    }

    fun navigateToAnnotation(annotation: Annotation) {
        val book = _selectedBook.value ?: return
        _readerPage.value = annotation.page.coerceIn(1, book.totalPages.coerceAtLeast(1))
        _readerScrollPosition.value = annotation.position.coerceIn(0f, 1f)
        persistReadingPosition(book.id, _readerPage.value, _readerScrollPosition.value, _readerZoomLevel.value, book.totalPages)
        emitToast("Jumped to page ${annotation.page} (${annotation.type})")
    }

    fun setAnnotationFilter(filter: String) {
        _activeAnnotationFilter.value = filter
    }

    fun adjustReaderFontSize(delta: Int) {
        _readerFontSizeSp.value = (_readerFontSizeSp.value + delta).coerceIn(13, 26)
    }

    fun setReaderPaperTone(tone: String) {
        _readerPaperTone.value = tone
    }

    fun finishBook(book: BookItem) {
        viewModelScope.launch {
            recordReadingSessionEnd()
            val updated = book.copy(
                currentPage = book.totalPages,
                status = "COMPLETED"
            )
            repository.updateBook(updated.toBook())
            _selectedBook.value = updated
            emitToast("${book.title} completed.")
            performHaptic(CadenceHaptics.HapticType.BOOK_COMPLETE)
            navigateTo(Screen.BOOK_COMPLETION)
        }
    }

    fun reReadBook(book: BookItem) {
        viewModelScope.launch {
            val updated = book.copy(
                currentPage = 1,
                scrollPosition = 0f,
                status = "READING"
            )
            repository.updateBook(updated.toBook())
            _selectedBook.value = updated
            emitToast("Restarting ${book.title} from page 1")
            openReaderForBook(updated, 1)
        }
    }

    fun toggleEmptyShelfView() {
        _isShelfEmptyView.value = !_isShelfEmptyView.value
        emitToast(if (_isShelfEmptyView.value) "Showing quiet empty shelf" else "Showing populated shelf")
    }

    /**
     * Real PDF Import using system document picker Uri
     */
    fun importPdfFromUri(uri: Uri) {
        viewModelScope.launch {
            _isImportingPdf.value = true
            _importError.value = null
            emitToast("Importing PDF document...")
            val result = PdfBookManager.importPdfFromUri(getApplication(), uri, repository)
            _isImportingPdf.value = false
            when (result) {
                is PdfImportResult.Success -> {
                    val bookItem = result.book.toBookItem()
                    _selectedBook.value = bookItem
                    _isShelfEmptyView.value = false
                    emitToast("Successfully imported '${bookItem.title}' (${bookItem.totalPages} pages)")
                    navigateTo(Screen.LIBRARY)
                }
                is PdfImportResult.Error -> {
                    _importError.value = result.message
                    emitToast("Import failed: ${result.message}")
                }
            }
        }
    }

    fun clearImportError() {
        _importError.value = null
    }

    fun toggleRoutineCompleted(routine: RoutineItem) {
        viewModelScope.launch {
            val newCompleted = !routine.isCompleted
            repository.setRoutineCompletedToday(routine.id, newCompleted)
            emitToast(if (newCompleted) "'${routine.title}' completed" else "'${routine.title}' unmarked")
        }
    }

    fun toggleRoutineEnabled(routine: RoutineItem) {
        viewModelScope.launch {
            val newEnabled = !routine.enabled
            repository.setRoutineEnabled(routine.id, newEnabled)
            emitToast(if (newEnabled) "'${routine.title}' enabled" else "'${routine.title}' paused")
        }
    }

    fun addRoutine(
        title: String,
        category: String,
        time: String,
        duration: Int,
        days: String,
        isRecurring: Boolean,
        gentleChime: Boolean = true,
        hasAlarm: Boolean = false
    ) {
        viewModelScope.launch {
            val r = Routine(
                title = title,
                type = category,
                time = time,
                days = days,
                duration = duration,
                enabled = true,
                gentleChime = gentleChime,
                hasAlarm = hasAlarm
            )
            repository.insertRoutine(r)
            emitToast("Added '$title' to cadence")
        }
    }

    fun updateRoutine(routine: RoutineItem) {
        viewModelScope.launch {
            repository.updateRoutine(routine.toRoutine())
            emitToast("Updated '${routine.title}'")
        }
    }

    fun deleteRoutine(routine: RoutineItem) {
        viewModelScope.launch {
            repository.deleteRoutine(routine.toRoutine())
            emitToast("Removed '${routine.title}'")
        }
    }

    // Task Management
    fun toggleTaskCompleted(task: Task) {
        viewModelScope.launch {
            val nextState = !task.completed
            repository.setTaskCompletion(task.id, nextState)
            emitToast(if (nextState) "Task '${task.title}' completed" else "Task marked pending")
        }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch {
            repository.updateTask(task)
            emitToast("Updated '${task.title}'")
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            repository.deleteTask(task)
            emitToast("Deleted '${task.title}'")
        }
    }

    fun postponeTask(task: Task, newDate: String = "Tomorrow") {
        viewModelScope.launch {
            repository.postponeTask(task.id, newDate)
            emitToast("Postponed '${task.title}' to $newDate")
        }
    }

    fun snoozeTask(task: Task, minutes: Int = 15) {
        viewModelScope.launch {
            // Compute new time
            val parts = task.time.split(":")
            val hour = parts.getOrNull(0)?.trim()?.toIntOrNull() ?: 8
            val minPart = parts.getOrNull(1) ?: "00 AM"
            val min = minPart.take(2).trim().toIntOrNull() ?: 0
            val isPm = minPart.contains("PM", ignoreCase = true)
            
            var totalMins = (if (isPm && hour != 12) hour + 12 else if (!isPm && hour == 12) 0 else hour) * 60 + min + minutes
            val newHour24 = (totalMins / 60) % 24
            val newMin = totalMins % 60
            val newIsPm = newHour24 >= 12
            val newHour12 = when {
                newHour24 == 0 -> 12
                newHour24 > 12 -> newHour24 - 12
                else -> newHour24
            }
            val formattedTime = String.format("%02d:%02d %s", newHour12, newMin, if (newIsPm) "PM" else "AM")
            repository.snoozeTask(task.id, formattedTime)
            emitToast("Snoozed '${task.title}' by ${minutes}m (now $formattedTime)")
        }
    }

    fun createTask(
        title: String,
        category: String = "reading",
        time: String = "08:00 AM",
        durationMinutes: Int = 30,
        anchor: String = "",
        days: String = "Daily",
        isRecurring: Boolean = true,
        gentleChime: Boolean = true,
        duration: Int = durationMinutes,
        recurrence: String = if (isRecurring) days else "Once",
        description: String = anchor,
        hasNotification: Boolean = true,
        hasAlarm: Boolean = false,
        date: String = "Today"
    ) {
        viewModelScope.launch {
            val task = Task(
                title = title,
                description = description.ifEmpty { anchor },
                category = category.lowercase(),
                date = date,
                time = time,
                duration = duration,
                completed = false,
                recurrence = recurrence,
                hasNotification = hasNotification,
                hasAlarm = hasAlarm
            )
            repository.insertTask(task)
            if (isRecurring) {
                val r = Routine(
                    title = title,
                    type = category.lowercase(),
                    time = time,
                    days = days,
                    duration = duration,
                    enabled = true,
                    gentleChime = gentleChime,
                    hasAlarm = hasAlarm
                )
                repository.insertRoutine(r)
            }
            emitToast("Cadence task '$title' created")
            triggerCompletionModal("TASK")
            navigateTo(Screen.ROUTINE)
        }
    }

    fun markAllNotificationsRead() {
        markNotificationsRead()
    }

    fun applyCompanionProposal(proposalTitle: String) {
        applyProposal(proposalTitle)
    }

    fun applyCompanionProposal(message: CompanionMessage) {
        applyProposal(message.proposalTitle ?: message.text)
    }

    fun toggleIntercessionPrayed(item: IntercessionItem) {
        viewModelScope.launch {
            val updated = item.copy(isPrayedToday = !item.isPrayedToday)
            repository.updateIntercession(updated)
            emitToast(if (updated.isPrayedToday) "Intercession ${item.number} prayed" else "Intercession unmarked")
        }
    }

    fun updateIntercessionNotes(item: IntercessionItem, notes: String) {
        viewModelScope.launch {
            repository.updateIntercession(item.copy(notes = notes))
            emitToast("Prayer notes preserved")
        }
    }

    fun toggleSessionTimer() {
        _isSessionTimerActive.value = !_isSessionTimerActive.value
        emitToast(if (_isSessionTimerActive.value) "Sanctuary contemplation timer active" else "Contemplation timer paused")
    }

    fun resetSessionTimer(minutes: Int = 30) {
        _sessionRemainingSeconds.value = minutes * 60
        _isSessionTimerActive.value = false
    }

    fun sendCompanionQuery(query: String) {
        if (query.isBlank()) return
        val timeNow = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())

        performHaptic(CadenceHaptics.HapticType.BUTTON_CLICK)

        viewModelScope.launch {
            val userMsg = CompanionMessage(
                text = query.trim(),
                isUser = true,
                timestamp = timeNow
            )
            repository.sendCompanionMessage(userMsg)

            // Process query through the real GeminiAssistantEngine using application database as source of truth
            val result = geminiAssistantEngine.processUserQuery(query)

            val replyMsg = CompanionMessage(
                text = result.replyText,
                isUser = false,
                timestamp = timeNow,
                proposalTitle = result.proposalTitle,
                proposalDetails = result.proposalDetails,
                planType = result.planType,
                planPayload = result.planPayload,
                planStatus = if (result.planType != null) "PENDING" else "IDLE"
            )
            repository.sendCompanionMessage(replyMsg)
            performHaptic(CadenceHaptics.HapticType.SELECTION)
        }
    }

    fun acceptProposedPlan(message: CompanionMessage) {
        viewModelScope.launch {
            val payload = message.planPayload ?: return@launch
            performHaptic(CadenceHaptics.HapticType.TASK_COMPLETE)

            when (message.planType) {
                "DAILY_PLAN" -> {
                    val adapter = moshi.adapter(ProposedDailyPlan::class.java)
                    val plan = adapter.fromJson(payload)
                    if (plan != null) {
                        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                        for (item in plan.items) {
                            val task = Task(
                                title = item.title,
                                description = "Daily rhythm anchor: ${item.category}",
                                date = todayStr,
                                time = item.time,
                                duration = item.durationMinutes,
                                category = item.category.lowercase(),
                                hasNotification = item.hasNotification,
                                hasAlarm = item.hasAlarm,
                                completed = false
                            )
                            val taskId = repository.insertTask(task)

                            if (item.hasAlarm || item.hasNotification) {
                                val nextMillis = CadenceAlarmScheduler.parseTimeStringToNextMillis(item.time)
                                CadenceAlarmScheduler.scheduleAlarm(
                                    context = getApplication(),
                                    alarmId = (taskId + 30000).toInt(),
                                    triggerTimeMillis = nextMillis,
                                    title = item.title,
                                    body = "${item.title} begins now (${item.durationMinutes} mins).",
                                    category = "ROUTINE",
                                    destinationScreen = "TODAY",
                                    extraId = taskId,
                                    isAlarm = item.hasAlarm
                                )
                            }
                        }
                        emitToast("Scheduled ${plan.items.size} rhythm anchors for today")
                        navigateTo(Screen.TODAY)
                    }
                }

                "READING_PLAN" -> {
                    val adapter = moshi.adapter(ProposedReadingPlan::class.java)
                    val plan = adapter.fromJson(payload)
                    if (plan != null) {
                        val todayCal = Calendar.getInstance()
                        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

                        for (day in plan.schedule) {
                            val taskDate = sdf.format(todayCal.time)
                            val task = Task(
                                title = "Read ${plan.bookTitle} (${day.dayLabel})",
                                description = "Read Pages ${day.startPage}–${day.endPage} (${day.pagesToRead} pages, ~${day.estimatedMinutes} min)",
                                date = taskDate,
                                time = "08:00 PM",
                                duration = day.estimatedMinutes,
                                category = "reading",
                                hasNotification = true,
                                hasAlarm = false,
                                completed = false
                            )
                            repository.insertTask(task)
                            todayCal.add(Calendar.DAY_OF_YEAR, 1)
                        }

                        // Schedule reading routine
                        val routine = Routine(
                            title = "Read ${plan.bookTitle}",
                            type = "reading",
                            time = "08:00 PM",
                            duration = 30,
                            days = "Every day",
                            enabled = true,
                            gentleChime = true,
                            hasAlarm = false
                        )
                        repository.insertRoutine(routine)

                        emitToast("Adopted ${plan.targetDays}-day reading plan for ${plan.bookTitle}")
                        navigateTo(Screen.TODAY)
                    }
                }

                "ROUTINE_PROPOSAL" -> {
                    val adapter = moshi.adapter(ProposedRoutine::class.java)
                    val routine = adapter.fromJson(payload)
                    if (routine != null) {
                        val r = Routine(
                            title = routine.title,
                            type = routine.type.lowercase(),
                            time = routine.time,
                            duration = routine.duration,
                            days = routine.days,
                            enabled = true,
                            gentleChime = routine.gentleChime,
                            hasAlarm = routine.hasAlarm
                        )
                        val id = repository.insertRoutine(r)

                        val nextMillis = CadenceAlarmScheduler.parseTimeStringToNextMillis(routine.time)
                        CadenceAlarmScheduler.scheduleAlarm(
                            context = getApplication(),
                            alarmId = (id + 1000).toInt(),
                            triggerTimeMillis = nextMillis,
                            title = routine.title,
                            body = "${routine.title} scheduled for ${routine.days} at ${routine.time}",
                            category = "ROUTINE",
                            destinationScreen = "ROUTINE",
                            extraId = id,
                            isAlarm = routine.hasAlarm
                        )

                        emitToast("Created routine '${routine.title}' (${routine.days} at ${routine.time})")
                        navigateTo(Screen.ROUTINE)
                    }
                }
            }

            // Mark message as applied
            repository.updateCompanionMessage(message.copy(proposalApplied = true, planStatus = "ACCEPTED"))
        }
    }

    fun cancelProposedPlan(message: CompanionMessage) {
        viewModelScope.launch {
            repository.updateCompanionMessage(message.copy(planStatus = "CANCELLED"))
            emitToast("Proposal dismissed")
            performHaptic(CadenceHaptics.HapticType.BUTTON_CLICK)
        }
    }

    fun evaluateSmartReminders() {
        viewModelScope.launch {
            val allBooks = rawBooks.value
            val allRoutines = rawRoutines.value
            val allSessions = rawSessions.value
            val prefs = userPreferences.value
            val existingNotifs = rawNotifications.value

            val triggers = SmartReadingEngine.evaluateTriggers(
                books = allBooks,
                routines = allRoutines,
                sessions = allSessions,
                preferences = prefs,
                existingNotifications = existingNotifs
            )

            for (trigger in triggers) {
                val notif = Notification(
                    type = trigger.type.name.lowercase(),
                    title = trigger.title,
                    body = trigger.body,
                    createdAt = System.currentTimeMillis(),
                    read = false,
                    action = trigger.destinationScreen
                )
                val notifId = repository.insertNotification(notif).toInt()

                // Trigger real Android notification
                CadenceNotificationManager.showNotification(
                    context = getApplication(),
                    notificationId = notifId,
                    category = "READING",
                    title = trigger.title,
                    body = trigger.body,
                    destinationScreen = trigger.destinationScreen,
                    extraId = trigger.bookId ?: trigger.routineId ?: 0L,
                    isAlarm = false
                )
            }
        }
    }

    fun performHaptic(type: CadenceHaptics.HapticType) {
        val isHapticsEnabled = userPreferences.value?.haptics ?: true
        CadenceHaptics.performHaptic(getApplication(), type, isHapticsEnabled)
    }

    fun setHapticsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            val current = userPreferences.value ?: UserPreferences()
            repository.updateUserPreferences(current.copy(haptics = enabled))
            emitToast(if (enabled) "Haptic pulses enabled" else "Haptics muted")
        }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            val current = userPreferences.value ?: UserPreferences()
            repository.updateUserPreferences(current.copy(notifications = enabled))
            emitToast(if (enabled) "System notifications active" else "System notifications muted")
        }
    }

    fun setQuietHours(quietHours: String) {
        viewModelScope.launch {
            val current = userPreferences.value ?: UserPreferences()
            repository.updateUserPreferences(current.copy(quietHours = quietHours))
            emitToast("Quiet hours updated to $quietHours")
        }
    }

    fun updateProfile(name: String, bio: String) {
        viewModelScope.launch {
            val current = userPreferences.value ?: UserPreferences()
            repository.updateUserPreferences(current.copy(profileName = name, profileBio = bio))
            emitToast("Profile updated successfully")
        }
    }

    fun setWakeTimePreference(time: String) {
        viewModelScope.launch {
            val current = userPreferences.value ?: UserPreferences()
            repository.updateUserPreferences(current.copy(wakeTime = time))
            emitToast("Dawn wake anchor updated to $time")
        }
    }

    fun setBedTimePreference(time: String) {
        viewModelScope.launch {
            val current = userPreferences.value ?: UserPreferences()
            repository.updateUserPreferences(current.copy(bedTime = time))
            emitToast("Evening rest boundary updated to $time")
        }
    }

    fun canScheduleExactAlarms(): Boolean {
        return CadenceAlarmScheduler.canScheduleExactAlarms(getApplication())
    }

    fun getExactAlarmSettingsIntent() = CadenceAlarmScheduler.getExactAlarmSettingsIntent(getApplication())

    fun applyProposal(proposalTitle: String) {
        viewModelScope.launch {
            performHaptic(CadenceHaptics.HapticType.BUTTON_CLICK)
            if (proposalTitle.contains("Resume", ignoreCase = true) || proposalTitle.contains("Atomic", ignoreCase = true)) {
                val atomic = books.value.firstOrNull { it.title.contains("Atomic", ignoreCase = true) }
                if (atomic != null) {
                    openReaderForBook(atomic)
                }
            } else if (proposalTitle.contains("Sanctuary", ignoreCase = true) || proposalTitle.contains("Intercession", ignoreCase = true)) {
                navigateTo(Screen.INTERCESSIONS)
            } else {
                navigateTo(Screen.TODAY)
            }
            emitToast("Proposal applied to your cadence")
        }
    }

    fun clearCompanionHistory() {
        viewModelScope.launch {
            repository.clearCompanionMessages()
            emitToast("Companion history cleared")
            performHaptic(CadenceHaptics.HapticType.BUTTON_CLICK)
        }
    }

    fun markNotificationsRead() {
        viewModelScope.launch {
            repository.markAllNotificationsAsRead()
            emitToast("All notifications acknowledged")
            performHaptic(CadenceHaptics.HapticType.BUTTON_CLICK)
        }
    }

    fun dismissNotification(id: Long) {
        viewModelScope.launch {
            repository.dismissNotification(id)
            performHaptic(CadenceHaptics.HapticType.BUTTON_CLICK)
        }
    }

    fun toggleDemoLoading() {
        _isDemoLoading.value = !_isDemoLoading.value
        emitToast(if (_isDemoLoading.value) "Simulating loading skeletons" else "Loading skeleton disabled")
    }

    fun toggleDemoEmpty() {
        _isDemoEmpty.value = !_isDemoEmpty.value
        emitToast(if (_isDemoEmpty.value) "Simulating empty states" else "Restored populated states")
    }

    fun triggerCompletionModal(type: String) {
        _activeCompletionModal.value = type
        performHaptic(CadenceHaptics.HapticType.TASK_COMPLETE)
    }

    fun dismissCompletionModal() {
        _activeCompletionModal.value = null
    }

    fun setRoutineFilter(filter: String) {
        _routineFilter.value = filter
        performHaptic(CadenceHaptics.HapticType.SELECTION)
    }

    fun emitToast(msg: String) {
        viewModelScope.launch {
            _toastEvent.emit(msg)
        }
    }

    override fun onCleared() {
        super.onCleared()
        persistCurrentReadingPosition()
        recordReadingSessionEnd()
    }
}


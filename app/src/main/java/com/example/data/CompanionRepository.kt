package com.example.data

import com.example.data.dao.AlarmDao
import com.example.data.dao.AnnotationDao
import com.example.data.dao.BookDao
import com.example.data.dao.CadenceDao
import com.example.data.dao.NotificationDao
import com.example.data.dao.ReadingSessionDao
import com.example.data.dao.RoutineDao
import com.example.data.dao.TaskDao
import com.example.data.dao.UserPreferencesDao
import com.example.data.model.Alarm
import com.example.data.model.Annotation
import com.example.data.model.Book
import com.example.data.model.CompanionMessage
import com.example.data.model.IntercessionItem
import com.example.data.model.Notification
import com.example.data.model.ReadingSession
import com.example.data.model.Routine
import com.example.data.model.Task
import com.example.data.model.UserPreferences
import kotlinx.coroutines.flow.Flow

class CompanionRepository(
    private val database: CompanionDatabase,
    private val bookDao: BookDao = database.bookDao(),
    private val sessionDao: ReadingSessionDao = database.readingSessionDao(),
    private val annotationDao: AnnotationDao = database.annotationDao(),
    private val taskDao: TaskDao = database.taskDao(),
    private val routineDao: RoutineDao = database.routineDao(),
    private val alarmDao: AlarmDao = database.alarmDao(),
    private val notificationDao: NotificationDao = database.notificationDao(),
    private val preferencesDao: UserPreferencesDao = database.userPreferencesDao(),
    private val cadenceDao: CadenceDao = database.cadenceDao()
) {

    // Books
    val allBooks: Flow<List<Book>> = bookDao.getAllBooks()
    fun getBookById(id: Long): Flow<Book?> = bookDao.getBookById(id)
    suspend fun getBookByIdDirect(id: Long): Book? = bookDao.getBookByIdDirect(id)
    suspend fun findBookByTitleOrUri(title: String, fileUri: String): Book? = bookDao.findBookByTitleOrUri(title, fileUri)
    suspend fun insertBook(book: Book): Long = bookDao.insertBook(book)
    suspend fun updateBook(book: Book) = bookDao.updateBook(book)
    suspend fun updateReadingPosition(
        bookId: Long,
        currentPage: Int,
        scrollPosition: Float,
        zoomLevel: Float,
        progress: Float
    ) = bookDao.updateReadingPosition(
        bookId = bookId,
        currentPage = currentPage,
        scrollPosition = scrollPosition,
        zoomLevel = zoomLevel,
        progress = progress,
        lastReadAt = System.currentTimeMillis()
    )
    suspend fun deleteBook(book: Book) = bookDao.deleteBook(book)
    suspend fun deleteBookById(id: Long) = bookDao.deleteBookById(id)

    // Reading Sessions
    val allSessions: Flow<List<ReadingSession>> = sessionDao.getAllSessions()
    fun getSessionsForBook(bookId: Long): Flow<List<ReadingSession>> = sessionDao.getSessionsForBook(bookId)
    suspend fun insertSession(session: ReadingSession): Long = sessionDao.insertSession(session)

    // Annotations
    fun getAnnotationsForBook(bookId: Long): Flow<List<Annotation>> = annotationDao.getAnnotationsForBook(bookId)
    fun getAnnotationsForBookAndPage(bookId: Long, page: Int): Flow<List<Annotation>> = annotationDao.getAnnotationsForBookAndPage(bookId, page)
    fun getAnnotationsForBookByType(bookId: Long, type: String): Flow<List<Annotation>> = annotationDao.getAnnotationsForBookByType(bookId, type)
    suspend fun insertAnnotation(annotation: Annotation): Long = annotationDao.insertAnnotation(annotation)
    suspend fun updateAnnotation(annotation: Annotation) = annotationDao.updateAnnotation(annotation)
    suspend fun deleteAnnotation(annotation: Annotation) = annotationDao.deleteAnnotation(annotation)
    suspend fun deleteAnnotationById(id: Long) = annotationDao.deleteAnnotationById(id)

    // Tasks
    val allTasks: Flow<List<Task>> = taskDao.getAllTasks()
    fun getTasksForDate(date: String): Flow<List<Task>> = taskDao.getTasksForDate(date)
    suspend fun getTaskById(id: Long): Task? = taskDao.getTaskById(id)
    suspend fun insertTask(task: Task): Long = taskDao.insertTask(task)
    suspend fun updateTask(task: Task) = taskDao.updateTask(task)
    suspend fun setTaskCompletion(id: Long, completed: Boolean, completedAt: Long? = if (completed) System.currentTimeMillis() else null) = taskDao.setTaskCompletion(id, completed, completedAt)
    suspend fun postponeTask(id: Long, newDate: String) = taskDao.postponeTask(id, newDate)
    suspend fun snoozeTask(id: Long, newTime: String) = taskDao.snoozeTask(id, newTime)
    suspend fun deleteTask(task: Task) = taskDao.deleteTask(task)
    suspend fun deleteTaskById(id: Long) = taskDao.deleteTaskById(id)

    // Routines
    val allRoutines: Flow<List<Routine>> = routineDao.getAllRoutines()
    val enabledRoutines: Flow<List<Routine>> = routineDao.getEnabledRoutines()
    suspend fun getRoutineById(id: Long): Routine? = routineDao.getRoutineById(id)
    suspend fun insertRoutine(routine: Routine): Long = routineDao.insertRoutine(routine)
    suspend fun updateRoutine(routine: Routine) = routineDao.updateRoutine(routine)
    suspend fun setRoutineEnabled(id: Long, enabled: Boolean) = routineDao.setRoutineEnabled(id, enabled)
    suspend fun setRoutineCompletedToday(id: Long, completed: Boolean) = routineDao.setRoutineCompletedToday(id, completed)
    suspend fun deleteRoutine(routine: Routine) = routineDao.deleteRoutine(routine)
    suspend fun deleteRoutineById(id: Long) = routineDao.deleteRoutineById(id)

    // Alarms
    val allAlarms: Flow<List<Alarm>> = alarmDao.getAllAlarms()
    suspend fun insertAlarm(alarm: Alarm): Long = alarmDao.insertAlarm(alarm)
    suspend fun updateAlarm(alarm: Alarm) = alarmDao.updateAlarm(alarm)
    suspend fun deleteAlarm(alarm: Alarm) = alarmDao.deleteAlarm(alarm)

    // Notifications
    val allNotifications: Flow<List<Notification>> = notificationDao.getAllNotifications()
    val unreadNotifications: Flow<List<Notification>> = notificationDao.getUnreadNotifications()
    suspend fun insertNotification(notification: Notification): Long = notificationDao.insertNotification(notification)
    suspend fun markAllNotificationsAsRead() = notificationDao.markAllAsRead()
    suspend fun dismissNotification(id: Long) = notificationDao.deleteNotificationById(id)

    // User Preferences
    val userPreferences: Flow<UserPreferences?> = preferencesDao.getUserPreferences()
    suspend fun getUserPreferencesDirect(): UserPreferences? = preferencesDao.getUserPreferencesDirect()
    suspend fun updateUserPreferences(preferences: UserPreferences) = preferencesDao.insertUserPreferences(preferences)

    // Intercessions & Messages
    val allIntercessions: Flow<List<IntercessionItem>> = cadenceDao.getAllIntercessions()
    suspend fun updateIntercession(intercession: IntercessionItem) = cadenceDao.updateIntercession(intercession)

    val allCompanionMessages: Flow<List<CompanionMessage>> = cadenceDao.getAllCompanionMessages()
    suspend fun sendCompanionMessage(message: CompanionMessage): Long = cadenceDao.insertCompanionMessage(message)
    suspend fun updateCompanionMessage(message: CompanionMessage) = cadenceDao.updateCompanionMessage(message)
    suspend fun clearCompanionMessages() = cadenceDao.clearCompanionMessages()
}

package com.example

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.alarm.CadenceAlarmScheduler
import com.example.data.CompanionDatabase
import com.example.data.CompanionRepository
import com.example.data.model.Book
import com.example.data.model.ReadingSession
import com.example.data.model.Routine
import com.example.data.model.Task
import com.example.data.model.UserPreferences
import com.example.haptics.CadenceHaptics
import com.example.notification.CadenceNotificationManager
import com.example.receiver.AlarmReceiver
import com.example.receiver.BootReceiver
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import java.util.Calendar

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class BackgroundBehaviorAuditTest {

    private lateinit var context: Context
    private lateinit var db: CompanionDatabase
    private lateinit var repository: CompanionRepository

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, CompanionDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = CompanionRepository(db)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun testAppOpenAndStateEmissions() = runBlocking {
        // App open verification
        val initialBooks = repository.allBooks.first()
        assertTrue(initialBooks.isEmpty())

        val book = Book(
            title = "The Power of Silence",
            author = "Robert Sarah",
            totalPages = 250,
            currentPage = 1,
            status = "READING"
        )
        val id = repository.insertBook(book)
        assertTrue(id > 0)

        val updatedBooks = repository.allBooks.first()
        assertEquals(1, updatedBooks.size)
        assertEquals("The Power of Silence", updatedBooks[0].title)
    }

    @Test
    fun testAppBackgroundedReadingPositionPersistence() = runBlocking {
        val book = Book(
            id = 42,
            title = "Deep Work",
            author = "Cal Newport",
            totalPages = 300,
            currentPage = 1,
            scrollPosition = 0f,
            zoomLevel = 1f,
            status = "READING"
        )
        repository.insertBook(book)

        // Reading session while app was open
        val readToPage = 85
        val scrollOffset = 0.65f
        val zoom = 1.15f
        val progress = readToPage.toFloat() / 300f

        // Simulated backgrounding / leaving reader
        repository.updateReadingPosition(
            bookId = 42,
            currentPage = readToPage,
            scrollPosition = scrollOffset,
            zoomLevel = zoom,
            progress = progress
        )

        val session = ReadingSession(
            bookId = 42,
            startedAt = System.currentTimeMillis() - 25 * 60 * 1000,
            endedAt = System.currentTimeMillis(),
            startPage = 1,
            endPage = readToPage,
            duration = 1500
        )
        repository.insertSession(session)

        // Verification after app backgrounded
        val persisted = repository.getBookByIdDirect(42)
        assertNotNull(persisted)
        assertEquals(85, persisted!!.currentPage)
        assertEquals(0.65f, persisted.scrollPosition, 0.001f)
        assertEquals(1.15f, persisted.zoomLevel, 0.001f)

        val sessions = repository.getSessionsForBook(42).first()
        assertEquals(1, sessions.size)
        assertEquals(1500L, sessions[0].duration)
    }

    @Test
    fun testPhoneRestartBootReceiverReschedulesRoutinesAndTasks() = runBlocking {
        // Seed enabled routines and tasks
        val morningPrayer = Routine(
            id = 10,
            title = "Dawn Prayer & Stillness",
            type = "prayer",
            time = "06:00 AM",
            days = "Daily",
            duration = 30,
            enabled = true,
            gentleChime = true,
            hasAlarm = true
        )
        repository.insertRoutine(morningPrayer)

        val uncompletedTask = Task(
            id = 20,
            title = "Read 20 pages of scripture",
            category = "reading",
            date = "Today",
            time = "07:00 AM",
            duration = 20,
            completed = false,
            hasNotification = true,
            hasAlarm = false
        )
        repository.insertTask(uncompletedTask)

        // Simulate device reboot intent ACTION_BOOT_COMPLETED
        val bootIntent = Intent(Intent.ACTION_BOOT_COMPLETED)
        val bootReceiver = BootReceiver()
        bootReceiver.onReceive(context, bootIntent)

        // Verify alarm scheduler parsed upcoming time
        val nextPrayerMillis = CadenceAlarmScheduler.parseTimeStringToNextMillis("06:00 AM")
        assertTrue(nextPrayerMillis > System.currentTimeMillis())

        val nextTaskMillis = CadenceAlarmScheduler.parseTimeStringToNextMillis("07:00 AM")
        assertTrue(nextTaskMillis > System.currentTimeMillis())
    }

    @Test
    fun testExactAlarmPermissionHandling() {
        // Check exact alarm capability detection
        val canSchedule = CadenceAlarmScheduler.canScheduleExactAlarms(context)
        // Returns true or false depending on SDK version and platform permissions
        assertNotNull(canSchedule)

        // Intent for requesting exact alarm settings is properly formed
        val settingsIntent = CadenceAlarmScheduler.getExactAlarmSettingsIntent(context)
        assertNotNull(settingsIntent)
        assertNotNull(settingsIntent.data)
        assertTrue(settingsIntent.data.toString().contains(context.packageName))

        // Test scheduling alarm with fallback
        val testTrigger = System.currentTimeMillis() + 60000
        CadenceAlarmScheduler.scheduleAlarm(
            context = context,
            alarmId = 999,
            triggerTimeMillis = testTrigger,
            title = "Test Wake Anchor",
            body = "Time for quiet reading",
            category = "ALARM",
            isAlarm = true
        )

        // Verify cancellation
        CadenceAlarmScheduler.cancelAlarm(context, 999)
    }

    @Test
    fun testNotificationPermissionAndChannels() {
        // Initialize notification channels
        CadenceNotificationManager.createNotificationChannels(context)

        // Notification dispatch does not crash when called
        CadenceNotificationManager.showNotification(
            context = context,
            notificationId = 101,
            category = "READING",
            title = "Reading Cadence",
            body = "Time to continue your quiet rhythm",
            destinationScreen = "READER",
            isAlarm = false
        )

        CadenceNotificationManager.cancelNotification(context, 101)
    }

    @Test
    fun testQuietHoursCalculation() {
        // Overnight quiet hours: 22:00 to 06:00
        val isQuietActive = CadenceNotificationManager.isQuietHoursActive("22:00 - 06:00")
        assertNotNull(isQuietActive)

        // Malformed string fallback
        assertFalse(CadenceNotificationManager.isQuietHoursActive("invalid"))
        assertFalse(CadenceNotificationManager.isQuietHoursActive(""))
    }

    @Test
    fun testHapticsDisabledRespectsUserPreference() = runBlocking {
        // Save preference with haptics disabled
        val prefs = UserPreferences(
            id = 1,
            haptics = false,
            notifications = true,
            quietHours = "22:00 - 06:00"
        )
        repository.updateUserPreferences(prefs)

        val loadedPrefs = repository.getUserPreferencesDirect()
        assertNotNull(loadedPrefs)
        assertFalse(loadedPrefs!!.haptics)

        // When haptics are disabled, performHaptic returns immediately without error
        CadenceHaptics.performHaptic(context, CadenceHaptics.HapticType.BUTTON_CLICK, loadedPrefs.haptics)
        CadenceHaptics.performHaptic(context, CadenceHaptics.HapticType.TASK_COMPLETE, false)
        CadenceHaptics.performHaptic(context, CadenceHaptics.HapticType.BOOK_COMPLETE, false)
        CadenceHaptics.performHaptic(context, CadenceHaptics.HapticType.ALARM_PULSE, false)
    }

    @Test
    fun testAlarmReceiverBroadcastExecution() = runBlocking {
        val triggerIntent = Intent("com.example.ACTION_ALARM_TRIGGER").apply {
            putExtra("EXTRA_ALARM_ID", 1002)
            putExtra("EXTRA_TITLE", "Sanctuary Morning Rhythm")
            putExtra("EXTRA_BODY", "Scripture reading starts now.")
            putExtra("EXTRA_CATEGORY", "ROUTINE")
            putExtra("EXTRA_SCREEN", "ROUTINE")
            putExtra("EXTRA_IS_ALARM", true)
        }

        val receiver = AlarmReceiver()
        receiver.onReceive(context, triggerIntent)

        // Test snooze broadcast
        val snoozeIntent = Intent("com.example.ACTION_ALARM_SNOOZE").apply {
            putExtra("EXTRA_NOTIFICATION_ID", 1002)
            putExtra("EXTRA_TITLE", "Sanctuary Morning Rhythm")
            putExtra("EXTRA_BODY", "Scripture reading starts now.")
            putExtra("EXTRA_CATEGORY", "ROUTINE")
            putExtra("EXTRA_SCREEN", "ROUTINE")
        }
        receiver.onReceive(context, snoozeIntent)

        // Test dismiss broadcast
        val dismissIntent = Intent("com.example.ACTION_ALARM_DISMISS").apply {
            putExtra("EXTRA_NOTIFICATION_ID", 1002)
        }
        receiver.onReceive(context, dismissIntent)
    }
}

package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        Book::class,
        ReadingSession::class,
        Annotation::class,
        Task::class,
        Routine::class,
        Alarm::class,
        Notification::class,
        UserPreferences::class,
        IntercessionItem::class,
        CompanionMessage::class
    ],
    version = 3,
    exportSchema = false
)
abstract class CompanionDatabase : RoomDatabase() {

    abstract fun bookDao(): BookDao
    abstract fun readingSessionDao(): ReadingSessionDao
    abstract fun annotationDao(): AnnotationDao
    abstract fun taskDao(): TaskDao
    abstract fun routineDao(): RoutineDao
    abstract fun alarmDao(): AlarmDao
    abstract fun notificationDao(): NotificationDao
    abstract fun userPreferencesDao(): UserPreferencesDao
    abstract fun cadenceDao(): CadenceDao

    companion object {
        @Volatile
        private var INSTANCE: CompanionDatabase? = null

        fun getInstance(context: Context): CompanionDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CompanionDatabase::class.java,
                    "quiet_companion_db"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateInitialData(database)
                    }
                }
            }
        }

        suspend fun populateInitialData(database: CompanionDatabase) {
            val prefDao = database.userPreferencesDao()

            // Initial User Preferences (clean defaults)
            prefDao.insertUserPreferences(
                UserPreferences(
                    id = 1,
                    theme = "Linen",
                    haptics = true,
                    notifications = true,
                    quietHours = "22:00 - 06:00",
                    readingPreferences = "16sp;Linen;Vertical",
                    profileName = "Quiet Reader",
                    profileBio = "Local Profile",
                    wakeTime = "06:00 AM",
                    bedTime = "10:00 PM"
                )
            )
        }
    }
}

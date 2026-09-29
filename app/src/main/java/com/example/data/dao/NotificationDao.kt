package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Notification
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications ORDER BY createdAt DESC")
    fun getAllNotifications(): Flow<List<Notification>>

    @Query("SELECT * FROM notifications WHERE `read` = 0 ORDER BY createdAt DESC")
    fun getUnreadNotifications(): Flow<List<Notification>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: Notification): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotifications(notifications: List<Notification>)

    @Update
    suspend fun updateNotification(notification: Notification)

    @Query("UPDATE notifications SET `read` = 1")
    suspend fun markAllAsRead()

    @Delete
    suspend fun deleteNotification(notification: Notification)

    @Query("DELETE FROM notifications WHERE id = :id")
    suspend fun deleteNotificationById(id: Long)
}

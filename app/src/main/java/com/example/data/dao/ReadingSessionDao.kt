package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.ReadingSession
import kotlinx.coroutines.flow.Flow

@Dao
interface ReadingSessionDao {

    @Query("SELECT * FROM reading_sessions ORDER BY endedAt DESC")
    fun getAllSessions(): Flow<List<ReadingSession>>

    @Query("SELECT * FROM reading_sessions WHERE bookId = :bookId ORDER BY endedAt DESC")
    fun getSessionsForBook(bookId: Long): Flow<List<ReadingSession>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: ReadingSession): Long
}

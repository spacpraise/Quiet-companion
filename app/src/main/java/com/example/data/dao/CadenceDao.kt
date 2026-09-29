package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.CompanionMessage
import com.example.data.model.IntercessionItem
import kotlinx.coroutines.flow.Flow

@Dao
interface CadenceDao {

    // Intercessions
    @Query("SELECT * FROM intercessions ORDER BY number ASC")
    fun getAllIntercessions(): Flow<List<IntercessionItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIntercessions(items: List<IntercessionItem>)

    @Update
    suspend fun updateIntercession(item: IntercessionItem)

    // Companion Messages
    @Query("SELECT * FROM companion_messages ORDER BY id ASC")
    fun getAllCompanionMessages(): Flow<List<CompanionMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompanionMessage(message: CompanionMessage): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompanionMessages(messages: List<CompanionMessage>)

    @Update
    suspend fun updateCompanionMessage(message: CompanionMessage)

    @Query("DELETE FROM companion_messages")
    suspend fun clearCompanionMessages()
}

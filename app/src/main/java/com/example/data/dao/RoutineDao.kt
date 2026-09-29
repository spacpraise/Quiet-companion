package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Routine
import kotlinx.coroutines.flow.Flow

@Dao
interface RoutineDao {
    @Query("SELECT * FROM routines ORDER BY time ASC, id ASC")
    fun getAllRoutines(): Flow<List<Routine>>

    @Query("SELECT * FROM routines WHERE enabled = 1 ORDER BY time ASC, id ASC")
    fun getEnabledRoutines(): Flow<List<Routine>>

    @Query("SELECT * FROM routines WHERE id = :id LIMIT 1")
    suspend fun getRoutineById(id: Long): Routine?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutine(routine: Routine): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutines(routines: List<Routine>)

    @Update
    suspend fun updateRoutine(routine: Routine)

    @Query("UPDATE routines SET enabled = :enabled WHERE id = :id")
    suspend fun setRoutineEnabled(id: Long, enabled: Boolean)

    @Query("UPDATE routines SET isCompletedToday = :completed WHERE id = :id")
    suspend fun setRoutineCompletedToday(id: Long, completed: Boolean)

    @Delete
    suspend fun deleteRoutine(routine: Routine)

    @Query("DELETE FROM routines WHERE id = :id")
    suspend fun deleteRoutineById(id: Long)
}

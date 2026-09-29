package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Task
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY date ASC, time ASC, id DESC")
    fun getAllTasks(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE completed = 0 ORDER BY date ASC, time ASC")
    fun getIncompleteTasks(): Flow<List<Task>>


    @Query("SELECT * FROM tasks WHERE date = :date ORDER BY time ASC, id DESC")
    fun getTasksForDate(date: String): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE id = :id LIMIT 1")
    suspend fun getTaskById(id: Long): Task?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<Task>)

    @Update
    suspend fun updateTask(task: Task)

    @Query("UPDATE tasks SET completed = :completed, completedAt = :completedAt WHERE id = :id")
    suspend fun setTaskCompletion(id: Long, completed: Boolean, completedAt: Long?)

    @Query("UPDATE tasks SET date = :newDate WHERE id = :id")
    suspend fun postponeTask(id: Long, newDate: String)

    @Query("UPDATE tasks SET time = :newTime WHERE id = :id")
    suspend fun snoozeTask(id: Long, newTime: String)

    @Delete
    suspend fun deleteTask(task: Task)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteTaskById(id: Long)
}

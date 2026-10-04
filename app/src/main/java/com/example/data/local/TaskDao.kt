package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.TaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY isCompleted ASC, priority DESC, dueTimestamp ASC")
    fun getAllTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE isCompleted = 0 ORDER BY priority DESC, dueTimestamp ASC")
    fun getPendingTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE isCompleted = 1 ORDER BY completedTimestamp DESC")
    fun getCompletedTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE isCompleted = 0 AND isPermanentNotification = 1 ORDER BY priority DESC, dueTimestamp ASC")
    fun getPermanentActiveTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE isCompleted = 0 AND isPermanentNotification = 1 ORDER BY priority DESC, dueTimestamp ASC")
    suspend fun getPermanentActiveTasksSync(): List<TaskEntity>

    @Query("SELECT * FROM tasks WHERE isCompleted = 0")
    suspend fun getAllPendingTasksSync(): List<TaskEntity>

    @Query("SELECT * FROM tasks WHERE isCompleted = 0 AND latitude IS NOT NULL AND longitude IS NOT NULL AND locationTrigger != 'NONE'")
    suspend fun getPendingLocationTasksSync(): List<TaskEntity>

    @Query("SELECT * FROM tasks WHERE isCompleted = 0 AND latitude IS NOT NULL AND longitude IS NOT NULL AND locationTrigger != 'NONE'")
    fun getPendingLocationTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getTaskById(id: Long): TaskEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity): Long

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Delete
    suspend fun deleteTask(task: TaskEntity)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteTaskById(id: Long)

    @Query("UPDATE tasks SET isCompleted = :completed, completedTimestamp = :timestamp WHERE id = :id")
    suspend fun updateTaskCompletion(id: Long, completed: Boolean, timestamp: Long?)

    @Query("UPDATE tasks SET dueTimestamp = :newDueTimestamp WHERE id = :id")
    suspend fun snoozeTask(id: Long, newDueTimestamp: Long)
}

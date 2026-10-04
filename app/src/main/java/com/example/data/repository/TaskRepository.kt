package com.example.data.repository

import android.content.Context
import com.example.data.local.TaskDao
import com.example.data.model.RepeatInterval
import com.example.data.model.TaskEntity
import com.example.notification.NotificationHelper
import com.example.notification.ReminderScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class TaskRepository(
    private val taskDao: TaskDao,
    private val notificationHelper: NotificationHelper,
    private val reminderScheduler: ReminderScheduler
) {
    val allTasks: Flow<List<TaskEntity>> = taskDao.getAllTasks()
    val pendingTasks: Flow<List<TaskEntity>> = taskDao.getPendingTasks()
    val completedTasks: Flow<List<TaskEntity>> = taskDao.getCompletedTasks()
    val permanentActiveTasks: Flow<List<TaskEntity>> = taskDao.getPermanentActiveTasks()
    val pendingLocationTasks: Flow<List<TaskEntity>> = taskDao.getPendingLocationTasks()

    suspend fun getTaskById(id: Long): TaskEntity? = taskDao.getTaskById(id)

    suspend fun insertTask(task: TaskEntity): Long {
        val id = taskDao.insertTask(task)
        val createdTask = task.copy(id = id)
        reminderScheduler.scheduleReminder(createdTask)
        refreshPermanentNotification()
        return id
    }

    suspend fun updateTask(task: TaskEntity) {
        taskDao.updateTask(task)
        if (task.isCompleted) {
            reminderScheduler.cancelReminder(task.id)
        } else {
            reminderScheduler.scheduleReminder(task)
        }
        refreshPermanentNotification()
    }

    suspend fun setTaskCompleted(id: Long, completed: Boolean) {
        val timestamp = if (completed) System.currentTimeMillis() else null
        val task = taskDao.getTaskById(id) ?: return

        if (completed && task.repeatInterval != RepeatInterval.NONE) {
            // If repeating, advance due date and keep active!
            val oneDay = 24 * 60 * 60 * 1000L
            val nextDue = when (task.repeatInterval) {
                RepeatInterval.HOURLY -> task.dueTimestamp + 60 * 60 * 1000L
                RepeatInterval.DAILY, RepeatInterval.WEEKDAYS -> task.dueTimestamp + oneDay
                RepeatInterval.WEEKLY -> task.dueTimestamp + 7 * oneDay
                RepeatInterval.MONTHLY -> task.dueTimestamp + 30 * oneDay
                RepeatInterval.NONE -> task.dueTimestamp
            }
            val updatedTask = task.copy(
                dueTimestamp = nextDue,
                isCompleted = false,
                isLocationTriggered = false
            )
            taskDao.updateTask(updatedTask)
            reminderScheduler.scheduleReminder(updatedTask)
        } else {
            taskDao.updateTaskCompletion(id, completed, timestamp)
            reminderScheduler.cancelReminder(id)
        }
        refreshPermanentNotification()
    }

    suspend fun snoozeTask(id: Long, minutes: Int = 10) {
        val newDue = System.currentTimeMillis() + (minutes * 60 * 1000L)
        taskDao.snoozeTask(id, newDue)
        val task = taskDao.getTaskById(id)
        if (task != null) {
            reminderScheduler.scheduleReminder(task.copy(dueTimestamp = newDue))
        }
        refreshPermanentNotification()
    }

    suspend fun deleteTask(task: TaskEntity) {
        reminderScheduler.cancelReminder(task.id)
        taskDao.deleteTask(task)
        refreshPermanentNotification()
    }

    suspend fun deleteTaskById(id: Long) {
        reminderScheduler.cancelReminder(id)
        taskDao.deleteTaskById(id)
        refreshPermanentNotification()
    }

    suspend fun refreshPermanentNotification() {
        val activeTasks = taskDao.getPermanentActiveTasksSync()
        notificationHelper.updatePermanentNotification(activeTasks)
    }

    suspend fun getPendingTasksSync(): List<TaskEntity> {
        return taskDao.getAllPendingTasksSync()
    }

    suspend fun getPendingLocationTasksSync(): List<TaskEntity> {
        return taskDao.getPendingLocationTasksSync()
    }
}

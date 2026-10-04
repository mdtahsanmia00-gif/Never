package com.example

import android.app.Application
import com.example.data.local.AppDatabase
import com.example.data.repository.TaskRepository
import com.example.notification.NotificationHelper
import com.example.notification.ReminderScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class NeverForgetApp : Application() {

    lateinit var database: AppDatabase
        private set

    lateinit var notificationHelper: NotificationHelper
        private set

    lateinit var reminderScheduler: ReminderScheduler
        private set

    lateinit var repository: TaskRepository
        private set

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    companion object {
        lateinit var instance: NeverForgetApp
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        database = AppDatabase.getDatabase(this)
        notificationHelper = NotificationHelper(this)
        notificationHelper.createNotificationChannels()
        reminderScheduler = ReminderScheduler(this)
        repository = TaskRepository(database.taskDao(), notificationHelper, reminderScheduler)

        // Seed initial friendly tasks if database is empty so user sees the beautiful UI right away!
        applicationScope.launch {
            seedSampleTasksIfNeeded()
            repository.refreshPermanentNotification()
        }
    }

    private suspend fun seedSampleTasksIfNeeded() {
        val existing = repository.getPendingTasksSync()
        if (existing.isEmpty()) {
            val now = System.currentTimeMillis()
            val hour = 3600 * 1000L

            repository.insertTask(
                com.example.data.model.TaskEntity(
                    title = "Daily Team Sync & Planning",
                    description = "Discuss sprint goals and project updates with design team",
                    dueTimestamp = now + (2 * hour),
                    priority = com.example.data.model.Priority.HIGH,
                    category = com.example.data.model.TaskCategory.MEETING,
                    isPermanentNotification = true,
                    repeatInterval = com.example.data.model.RepeatInterval.DAILY
                )
            )

            repository.insertTask(
                com.example.data.model.TaskEntity(
                    title = "পড়তে বসা (রাত ৯টা)",
                    description = "AI Smart Reminder: বাংলা বা ইংরেজিতে সরাসরি কথা বলে টাস্ক যোগ করুন",
                    dueTimestamp = now + (4 * hour),
                    priority = com.example.data.model.Priority.HIGH,
                    category = com.example.data.model.TaskCategory.STUDY,
                    isPermanentNotification = true,
                    repeatInterval = com.example.data.model.RepeatInterval.DAILY
                )
            )

            repository.insertTask(
                com.example.data.model.TaskEntity(
                    title = "Grocery: Buy Fresh Fruits & Milk",
                    description = "Pick up bananas, apples, and almond milk from Supermarket",
                    dueTimestamp = now + (6 * hour),
                    priority = com.example.data.model.Priority.MEDIUM,
                    category = com.example.data.model.TaskCategory.GROCERIES,
                    locationName = "City Supermarket",
                    latitude = 23.8103,
                    longitude = 90.4125,
                    locationTrigger = com.example.data.model.LocationTrigger.ON_ENTER,
                    isPermanentNotification = false
                )
            )

            repository.insertTask(
                com.example.data.model.TaskEntity(
                    title = "Gym: Core & Cardio Session",
                    description = "30 mins treadmill and abs workout",
                    dueTimestamp = now + (8 * hour),
                    priority = com.example.data.model.Priority.LOW,
                    category = com.example.data.model.TaskCategory.HEALTH,
                    locationName = "Fitness Gym Center",
                    latitude = 23.7808,
                    longitude = 90.4192,
                    locationTrigger = com.example.data.model.LocationTrigger.ON_ENTER,
                    isPermanentNotification = false
                )
            )
        }
    }
}

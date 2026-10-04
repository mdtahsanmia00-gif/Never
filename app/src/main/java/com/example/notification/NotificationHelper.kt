package com.example.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.R
import com.example.data.model.Priority
import com.example.data.model.TaskEntity
import com.example.receiver.NotificationActionReceiver

class NotificationHelper(private val context: Context) {

    companion object {
        const val CHANNEL_PERMANENT_ID = "channel_permanent_tasks"
        const val CHANNEL_ALARM_ID = "channel_alarm_reminders"
        const val CHANNEL_LOCATION_ID = "channel_location_reminders"

        const val PERMANENT_NOTIFICATION_ID = 99999
        const val BASE_ALARM_NOTIFICATION_ID = 20000
    }

    private val notificationManager: NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val permanentChannel = NotificationChannel(
                CHANNEL_PERMANENT_ID,
                "Permanent Active Reminders (স্থায়ী নোটিফিকেশন)",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Keeps important pending tasks visible until completed"
                setShowBadge(true)
            }

            val alarmChannel = NotificationChannel(
                CHANNEL_ALARM_ID,
                "Task Alarms & Missed Alerts (কাজের অ্যালার্ম)",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Plays alert when a task is due or overdue"
                enableVibration(true)
                setShowBadge(true)
            }

            val locationChannel = NotificationChannel(
                CHANNEL_LOCATION_ID,
                "Location Based Alerts (লোকেশন রিমাইন্ডার)",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminds when entering or leaving designated locations"
                enableVibration(true)
            }

            notificationManager.createNotificationChannels(
                listOf(permanentChannel, alarmChannel, locationChannel)
            )
        }
    }

    /**
     * Updates or creates the permanent (ongoing) notification in the drawer.
     * Displays current active high-priority tasks and allows marking them done right from notification!
     */
    fun updatePermanentNotification(tasks: List<TaskEntity>) {
        if (tasks.isEmpty()) {
            notificationManager.cancel(PERMANENT_NOTIFICATION_ID)
            return
        }

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            context,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val topTask = tasks.first()
        val title = if (tasks.size == 1) {
            "🔔 ${topTask.title}"
        } else {
            "🔔 Never Forget AI: ${tasks.size} Active Tasks"
        }

        val inboxStyle = NotificationCompat.InboxStyle()
        inboxStyle.setBigContentTitle("Active Tasks (কাজ মনে রাখুন)")
        tasks.take(5).forEach { task ->
            val priorityIcon = when (task.priority) {
                Priority.HIGH -> "⚡ [High] "
                Priority.MEDIUM -> "🔹 "
                Priority.LOW -> "▫️ "
            }
            inboxStyle.addLine("$priorityIcon${task.title}")
        }
        if (tasks.size > 5) {
            inboxStyle.setSummaryText("+${tasks.size - 5} more tasks")
        }

        // Action: Complete Top Task
        val completeIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = NotificationActionReceiver.ACTION_COMPLETE_TASK
            putExtra(NotificationActionReceiver.EXTRA_TASK_ID, topTask.id)
        }
        val completePendingIntent = PendingIntent.getBroadcast(
            context,
            topTask.id.toInt(),
            completeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action: Snooze Top Task 10 min
        val snoozeIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = NotificationActionReceiver.ACTION_SNOOZE_TASK
            putExtra(NotificationActionReceiver.EXTRA_TASK_ID, topTask.id)
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            context,
            (topTask.id + 1000).toInt(),
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_PERMANENT_ID)
            .setSmallIcon(R.drawable.app_icon_bell)
            .setContentTitle(title)
            .setContentText("Tap to open app • Don't forget your goals!")
            .setStyle(inboxStyle)
            .setContentIntent(openAppPendingIntent)
            .setOngoing(true) // CANNOT be easily swiped away until user finishes or dismisses!
            .setAutoCancel(false)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .addAction(0, "✅ Mark Done: ${topTask.title.take(15)}", completePendingIntent)
            .addAction(0, "⏰ Snooze 10m", snoozePendingIntent)

        try {
            NotificationManagerCompat.from(context).notify(PERMANENT_NOTIFICATION_ID, builder.build())
        } catch (_: SecurityException) {
            // Notification permission might not be granted yet
        }
    }

    /**
     * Shows a Heads-Up / Alarm Notification when a task is due or missed.
     */
    fun showTaskAlarmNotification(task: TaskEntity, isOverdue: Boolean = false) {
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            context,
            task.id.toInt(),
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val completeIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = NotificationActionReceiver.ACTION_COMPLETE_TASK
            putExtra(NotificationActionReceiver.EXTRA_TASK_ID, task.id)
        }
        val completePendingIntent = PendingIntent.getBroadcast(
            context,
            (task.id + 2000).toInt(),
            completeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val snoozeIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = NotificationActionReceiver.ACTION_SNOOZE_TASK
            putExtra(NotificationActionReceiver.EXTRA_TASK_ID, task.id)
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            context,
            (task.id + 3000).toInt(),
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val titlePrefix = if (isOverdue) "⚠️ Overdue Task Alert!" else "⏰ Reminder Alert!"
        val builder = NotificationCompat.Builder(context, CHANNEL_ALARM_ID)
            .setSmallIcon(R.drawable.app_icon_bell)
            .setContentTitle("$titlePrefix ${task.title}")
            .setContentText(if (task.description.isNotBlank()) task.description else "Time to finish this task now!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setContentIntent(openAppPendingIntent)
            .setAutoCancel(true)
            .addAction(0, "✅ Done", completePendingIntent)
            .addAction(0, "⏰ Snooze 10m", snoozePendingIntent)

        val notificationId = (BASE_ALARM_NOTIFICATION_ID + (task.id % 10000)).toInt()
        try {
            NotificationManagerCompat.from(context).notify(notificationId, builder.build())
        } catch (_: SecurityException) {
        }
    }

    /**
     * Shows notification triggered by Location arrival/departure.
     */
    fun showLocationReminderNotification(task: TaskEntity, isArrival: Boolean) {
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            context,
            (task.id + 4000).toInt(),
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val completeIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = NotificationActionReceiver.ACTION_COMPLETE_TASK
            putExtra(NotificationActionReceiver.EXTRA_TASK_ID, task.id)
        }
        val completePendingIntent = PendingIntent.getBroadcast(
            context,
            (task.id + 5000).toInt(),
            completeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val actionText = if (isArrival) "📍 Arrived at ${task.locationName}" else "🚗 Left ${task.locationName}"
        val builder = NotificationCompat.Builder(context, CHANNEL_LOCATION_ID)
            .setSmallIcon(R.drawable.app_icon_bell)
            .setContentTitle("$actionText: ${task.title}")
            .setContentText(if (task.description.isNotBlank()) task.description else "Don't forget this task while here!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(openAppPendingIntent)
            .setAutoCancel(true)
            .addAction(0, "✅ Mark Done", completePendingIntent)

        val notificationId = (BASE_ALARM_NOTIFICATION_ID + 50000 + (task.id % 10000)).toInt()
        try {
            NotificationManagerCompat.from(context).notify(notificationId, builder.build())
        } catch (_: SecurityException) {
        }
    }

    fun cancelNotification(notificationId: Int) {
        notificationManager.cancel(notificationId)
    }

    fun cancelPermanentNotification() {
        notificationManager.cancel(PERMANENT_NOTIFICATION_ID)
    }
}

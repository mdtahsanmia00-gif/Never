package com.example.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.data.model.RepeatInterval
import com.example.data.model.TaskEntity
import com.example.receiver.ReminderAlarmReceiver

class ReminderScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager

    fun scheduleReminder(task: TaskEntity) {
        if (alarmManager == null || task.isCompleted) return

        val triggerTime = task.dueTimestamp
        // If trigger time is in the past and no repeat, we don't schedule a future alarm (it's overdue)
        if (triggerTime <= System.currentTimeMillis() && task.repeatInterval == RepeatInterval.NONE) {
            return
        }

        val effectiveTriggerTime = if (triggerTime <= System.currentTimeMillis()) {
            calculateNextTrigger(triggerTime, task.repeatInterval)
        } else {
            triggerTime
        }

        val intent = Intent(context, ReminderAlarmReceiver::class.java).apply {
            putExtra(ReminderAlarmReceiver.EXTRA_TASK_ID, task.id)
            putExtra(ReminderAlarmReceiver.EXTRA_TASK_TITLE, task.title)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            task.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        effectiveTriggerTime,
                        pendingIntent
                    )
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        effectiveTriggerTime,
                        pendingIntent
                    )
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    effectiveTriggerTime,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    effectiveTriggerTime,
                    pendingIntent
                )
            }
            Log.d("ReminderScheduler", "Scheduled reminder for task '${task.title}' at $effectiveTriggerTime")
        } catch (e: SecurityException) {
            Log.e("ReminderScheduler", "Security exception scheduling alarm", e)
        }
    }

    fun cancelReminder(taskId: Long) {
        if (alarmManager == null) return
        val intent = Intent(context, ReminderAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            taskId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    private fun calculateNextTrigger(originalTime: Long, repeat: RepeatInterval): Long {
        val now = System.currentTimeMillis()
        var nextTime = originalTime
        val oneHour = 60 * 60 * 1000L
        val oneDay = 24 * oneHour
        val oneWeek = 7 * oneDay

        while (nextTime <= now) {
            nextTime += when (repeat) {
                RepeatInterval.HOURLY -> oneHour
                RepeatInterval.DAILY, RepeatInterval.WEEKDAYS -> oneDay
                RepeatInterval.WEEKLY -> oneWeek
                RepeatInterval.MONTHLY -> 30 * oneDay
                RepeatInterval.NONE -> return now + 10 * 60 * 1000L // default fallback
            }
        }
        return nextTime
    }
}

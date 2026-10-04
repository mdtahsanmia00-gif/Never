package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.NeverForgetApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotificationActionReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_COMPLETE_TASK = "com.example.ACTION_COMPLETE_TASK"
        const val ACTION_SNOOZE_TASK = "com.example.ACTION_SNOOZE_TASK"
        const val ACTION_DISMISS_PERMANENT = "com.example.ACTION_DISMISS_PERMANENT"
        const val EXTRA_TASK_ID = "extra_task_id"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getLongExtra(EXTRA_TASK_ID, -1L)
        val app = context.applicationContext as? NeverForgetApp ?: return

        when (intent.action) {
            ACTION_COMPLETE_TASK -> {
                if (taskId != -1L) {
                    CoroutineScope(Dispatchers.IO).launch {
                        app.repository.setTaskCompleted(taskId, true)
                        Log.d("NotificationActionReceiver", "Task $taskId marked complete via notification action")
                    }
                }
            }
            ACTION_SNOOZE_TASK -> {
                if (taskId != -1L) {
                    CoroutineScope(Dispatchers.IO).launch {
                        app.repository.snoozeTask(taskId, 10)
                        Log.d("NotificationActionReceiver", "Task $taskId snoozed by 10 mins")
                    }
                }
            }
            ACTION_DISMISS_PERMANENT -> {
                app.notificationHelper.cancelPermanentNotification()
            }
        }
    }
}

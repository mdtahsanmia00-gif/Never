package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.NeverForgetApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReminderAlarmReceiver : BroadcastReceiver() {

    companion object {
        const val EXTRA_TASK_ID = "extra_task_id"
        const val EXTRA_TASK_TITLE = "extra_task_title"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getLongExtra(EXTRA_TASK_ID, -1L)
        if (taskId == -1L) return

        val app = context.applicationContext as? NeverForgetApp ?: return

        CoroutineScope(Dispatchers.IO).launch {
            val task = app.repository.getTaskById(taskId) ?: return@launch
            if (!task.isCompleted) {
                app.notificationHelper.showTaskAlarmNotification(task, isOverdue = false)
                app.repository.refreshPermanentNotification()
            }
        }
    }
}

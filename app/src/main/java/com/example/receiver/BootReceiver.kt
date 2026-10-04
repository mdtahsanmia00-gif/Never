package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.NeverForgetApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val app = context.applicationContext as? NeverForgetApp ?: return
            CoroutineScope(Dispatchers.IO).launch {
                val pending = app.repository.getPendingTasksSync()
                for (task in pending) {
                    app.reminderScheduler.scheduleReminder(task)
                }
                app.repository.refreshPermanentNotification()
            }
        }
    }
}

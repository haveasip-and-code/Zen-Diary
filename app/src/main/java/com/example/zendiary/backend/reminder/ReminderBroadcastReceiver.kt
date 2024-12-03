package com.example.zendiary.backend.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.example.zendiary.utils.NotificationHelper

class ReminderBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Toast.makeText(context, "Reminder: It's time to journal!", Toast.LENGTH_LONG).show()

        triggerNotification(context)
    }

    private fun triggerNotification(context: Context) {
        val notificationBuilder = NotificationHelper.createNotification(context, "Reminder", "Time to write in your journal!")
        NotificationHelper.notify(context, 1, notificationBuilder)
    }
}
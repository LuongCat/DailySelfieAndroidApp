package com.example.btqt_nhom3.Notification

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import com.example.btqt_nhom3.Notification.NotificationHelper

class SelfieReminderReceiver : BroadcastReceiver() {
    @RequiresApi(Build.VERSION_CODES.O)
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun onReceive(context: Context, intent: Intent?) {
        if (!hasTakenSelfieToday(context)) {
            NotificationHelper.sendReminderNotification(context)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun hasTakenSelfieToday(context: Context): Boolean {
        val prefs = context.getSharedPreferences("SelfiePrefs", Context.MODE_PRIVATE)
        val lastDate = prefs.getString("lastSelfieDate", "")
        val today = java.time.LocalDate.now().toString()
        return lastDate == today
    }
}
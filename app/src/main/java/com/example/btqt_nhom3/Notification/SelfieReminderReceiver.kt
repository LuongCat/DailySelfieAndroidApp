package com.example.btqt_nhom3.Notification

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class SelfieReminderReceiver : BroadcastReceiver() {
    @RequiresApi(Build.VERSION_CODES.O)
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            val prefs = context.getSharedPreferences("reminder_pref", Context.MODE_PRIVATE)

            val savedTime = prefs.getString("reminder_time", "Chưa đặt")

            if(savedTime != "Chưa đặt") {
                AlarmHelper.setReminder(context, savedTime!!)

                val formatter = DateTimeFormatter.ofPattern("HH:mm")
                val alarmTime = LocalTime.parse(savedTime, formatter)
                val now = LocalTime.now()

                if(!hasTakenSelfieToday(context) && now.isAfter(alarmTime)) {
                    NotificationHelper.sendReminderNotification(context)
                }
            }
        }
        else
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
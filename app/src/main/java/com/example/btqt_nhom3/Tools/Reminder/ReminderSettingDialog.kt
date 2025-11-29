package com.example.btqt_nhom3.Tools.Reminder

import android.annotation.SuppressLint
import android.app.TimePickerDialog
import android.content.Context
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import com.example.btqt_nhom3.Notification.AlarmHelper
import com.example.btqt_nhom3.databinding.DialogReminderSettingsBinding
import java.util.Calendar
import androidx.core.content.edit

object ReminderSettingsDialog {

    private const val PREF_NAME = "reminder_pref"
    private const val KEY_TIME = "reminder_time"

    @SuppressLint("DefaultLocale")
    fun show(context: Context) {
        val binding = DialogReminderSettingsBinding.inflate(LayoutInflater.from(context))
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

        val savedTime = prefs.getString(KEY_TIME, "Chưa đặt")
        binding.txtTime.text = savedTime

        val dialog = AlertDialog.Builder(context)
            .setTitle("Cài đặt nhắc nhở")
            .setView(binding.root)
            .setPositiveButton("Lưu", null)
            .setNeutralButton("Xóa", null)
            .setNegativeButton("Đóng", null)
            .create()

        dialog.setOnShowListener {

            val btnSave = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            val btnClear = dialog.getButton(AlertDialog.BUTTON_NEUTRAL)
            val btnClose = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)

            btnSave.setOnClickListener {
                val time = binding.txtTime.text.toString()
                if (time != "Chưa đặt") {
                    prefs.edit { putString(KEY_TIME, time) }
                    AlarmHelper.setReminder(context, time)
                }
                dialog.dismiss()
            }

            btnClear.setOnClickListener {
                prefs.edit { remove(KEY_TIME) }
                AlarmHelper.cancelReminder(context)
                dialog.dismiss()
            }

            btnClose.setOnClickListener {
                dialog.dismiss()
            }
        }

        binding.btnPickTime.setOnClickListener {
            val now = Calendar.getInstance()
            TimePickerDialog(
                context,
                { _, hour, minute ->
                    val time = String.format("%02d:%02d", hour, minute)
                    binding.txtTime.text = time
                },
                now.get(Calendar.HOUR_OF_DAY),
                now.get(Calendar.MINUTE),
                true
            ).show()
        }

        dialog.show()
    }
}

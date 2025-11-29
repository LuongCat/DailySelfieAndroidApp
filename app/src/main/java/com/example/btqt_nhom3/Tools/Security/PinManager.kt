package com.example.btqt_nhom3.Tools.Security

import android.content.Context
import androidx.core.content.edit

object PinManager {
    private const val KEY_PIN = "user_pin"

    fun savePin(context: Context, pin: String) {
        context.getSharedPreferences("security", Context.MODE_PRIVATE)
            .edit {
                putString(KEY_PIN, pin)
            }
    }

    fun getPin(context: Context): String? {
        return context.getSharedPreferences("security", Context.MODE_PRIVATE)
            .getString(KEY_PIN, null)
    }

    fun hasPin(context: Context): Boolean {
        return getPin(context) != null
    }
}
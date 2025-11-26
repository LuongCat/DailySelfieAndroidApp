package com.example.btqt_nhom3.Tools.Security

import android.content.Context

object SecurityPrefs {
    private const val PREF = "security_settings"
    private const val KEY_LOCK_PIN = "lock_with_pin"
    private const val KEY_FINGERPRINT = "use_fingerprint"

    fun setUsePin(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .edit().putBoolean(KEY_LOCK_PIN, enabled).apply()
    }

    fun isUsePin(context: Context) =
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .getBoolean(KEY_LOCK_PIN, false)

    fun setUseFingerprint(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .edit().putBoolean(KEY_FINGERPRINT, enabled).apply()
    }

    fun isUseFingerprint(context: Context) =
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .getBoolean(KEY_FINGERPRINT, false)
}

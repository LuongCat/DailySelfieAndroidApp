package com.example.btqt_nhom3.Tools.Security

import android.content.Context
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import com.example.btqt_nhom3.databinding.DialogSecuritySettingsBinding

object SecuritySettingsDialog {
    fun show(context: Context) {
        val binding = DialogSecuritySettingsBinding.inflate(LayoutInflater.from(context))

        val dialog = AlertDialog.Builder(context)
            .setView(binding.root)
            .setNegativeButton("Đóng", null)
            .create()

        binding.cbUsePin.isChecked = SecurityPrefs.isUsePin(context)
        binding.cbUseFingerprint.isChecked = SecurityPrefs.isUseFingerprint(context)

        binding.cbUseFingerprint.isEnabled =
            if (SecurityPrefs.isUsePin(context))
                true else false
        binding.btnChangeQuestion.isEnabled =
            if (SecurityPrefs.isUsePin(context))
                true else false
        binding.btnChangePin.isEnabled =
            if (SecurityPrefs.isUsePin(context))
                true else false

        binding.cbUsePin.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                if (!PinManager.hasPin(context)) {
                    PinCreateDialog.show(
                        context,
                        onSuccess = {
                            SecurityPrefs.setUsePin(context, true)
                            binding.cbUseFingerprint.isEnabled = true
                            binding.btnChangePin.isEnabled = true
                            binding.btnChangeQuestion.isEnabled = true
                        },
                        onCancel = {
                            binding.cbUsePin.isChecked = false
                        }
                    )
                } else {
                    SecurityPrefs.setUsePin(context, true)
                    binding.cbUseFingerprint.isEnabled = true
                    binding.btnChangePin.isEnabled = true
                    binding.btnChangeQuestion.isEnabled = true
                }
            } else {
                SecurityPrefs.setUsePin(context, false)

                SecurityPrefs.setUseFingerprint(context, false)
                binding.cbUseFingerprint.isChecked = false
                binding.cbUseFingerprint.isEnabled = false

                binding.btnChangePin.isEnabled = false
                binding.btnChangeQuestion.isEnabled = false
            }
        }

        binding.cbUseFingerprint.setOnCheckedChangeListener { _, checked ->
            SecurityPrefs.setUseFingerprint(context, checked)
        }

        binding.btnChangePin.setOnClickListener {
            PinChangeDialog.show(context) { }
        }

        binding.btnChangeQuestion.setOnClickListener {
            SecurityQuestionChangeDialog.show(context) { }
        }

        dialog.show()
    }
}

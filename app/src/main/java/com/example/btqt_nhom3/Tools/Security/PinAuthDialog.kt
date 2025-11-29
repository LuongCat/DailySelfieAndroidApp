package com.example.btqt_nhom3.Tools.Security

import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.example.btqt_nhom3.databinding.DialogPinAuthBinding

object PinAuthDialog {

    fun show(
        activity: AppCompatActivity,
        onSuccess: () -> Unit,
        onFailed: () -> Unit
    ) {
        val binding = DialogPinAuthBinding.inflate(LayoutInflater.from(activity))

        val dialog = AlertDialog.Builder(activity)
            .setView(binding.root)
            .setCancelable(false)
            .create()

        val savedPin = PinManager.getPin(activity)

        binding.txtTitle.text = "Nhập mã PIN để mở khóa"
        binding.btnCancel.text = "Thoát"

        binding.btnCancel.setOnClickListener {
            dialog.dismiss()
            onFailed()
        }

        binding.btnOK.setOnClickListener {
            val pin = binding.edtPin.text.toString()
            if (pin == savedPin) {
                dialog.dismiss()
                onSuccess()
            } else {
                binding.edtPin.error = "Sai PIN"
            }
        }

        binding.btnForgotPin.setOnClickListener {
            dialog.dismiss()
            ForgotPinDialog.show(activity, onSuccess, onFailed)
        }


        if (SecurityPrefs.isUseFingerprint(activity)) {
            binding.btnFingerprint.apply {
                visibility = android.view.View.VISIBLE
                setOnClickListener {
                    showFingerprint(activity) {
                        dialog.dismiss()
                        onSuccess()
                    }
                }
            }
        } else {
            binding.btnFingerprint.visibility = android.view.View.GONE
        }

        dialog.show()
    }

    // === XÁC THỰC VÂN TAY ===
    private fun showFingerprint(
        activity: AppCompatActivity,
        onSuccess: () -> Unit
    ) {
        val executor = ContextCompat.getMainExecutor(activity)

        val prompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    onSuccess()
                }
            })

        val info = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Mở khóa bằng vân tay")
            .setDescription("Chạm cảm biến")
            .setNegativeButtonText("Hủy")
            .build()

        prompt.authenticate(info)
    }
}

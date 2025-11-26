package com.example.btqt_nhom3.Tools.Security

import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.btqt_nhom3.databinding.DialogSecurityQuestionBinding

object ForgotPinDialog {

    fun show(
        activity: AppCompatActivity,
        onSuccess: () -> Unit,
        onFailed: () -> Unit
    ) {
        val binding = DialogSecurityQuestionBinding.inflate(LayoutInflater.from(activity))

        val dialog = AlertDialog.Builder(activity)
            .setView(binding.root)
            .setCancelable(false)
            .create()

        val question = SecurityQuestionManager.getQuestion(activity)
        binding.txtQuestion.text = question

        binding.btnCancel.setOnClickListener {
            dialog.dismiss()
            onFailed()
        }

        binding.btnOK.setOnClickListener {
            val ans = binding.edtAnswer.text.toString()

            if (!SecurityQuestionManager.checkAnswer(activity, ans)) {
                binding.edtAnswer.error = "Sai câu trả lời"
                return@setOnClickListener
            }

            dialog.dismiss()

            // Mở dialog đổi PIN
            PinChangeDialog.show(activity) {
                onSuccess()
            }
        }

        dialog.show()
    }
}

package com.example.btqt_nhom3.Tools.Security

import android.content.Context
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import com.example.btqt_nhom3.databinding.DialogSecurityQuestionChangeBinding

object SecurityQuestionChangeDialog {

    fun show(context: Context, onSaved: () -> Unit) {
        val binding = DialogSecurityQuestionChangeBinding.inflate(
            LayoutInflater.from(context)
        )

        val dialog = AlertDialog.Builder(context)
            .setView(binding.root)
            .setCancelable(false)
            .create()

        // Nếu đã có câu hỏi cũ → điền sẵn để user chỉnh sửa
        val oldQuestion = SecurityQuestionManager.getQuestion(context)
        if (oldQuestion != null) binding.edtQuestion.setText(oldQuestion)

        binding.btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        binding.btnOK.setOnClickListener {
            val question = binding.edtQuestion.text.toString().trim()
            val answer = binding.edtAnswer.text.toString().trim()

            if (question.isEmpty()) {
                binding.edtQuestion.error = "Nhập câu hỏi"
                return@setOnClickListener
            }
            if (answer.isEmpty()) {
                binding.edtAnswer.error = "Nhập đáp án"
                return@setOnClickListener
            }

            SecurityQuestionManager.save(context, question, answer)
            dialog.dismiss()
            onSaved()
        }

        dialog.show()
    }
}

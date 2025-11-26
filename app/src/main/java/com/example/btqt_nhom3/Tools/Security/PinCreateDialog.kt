package com.example.btqt_nhom3.Tools.Security

import android.content.Context
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import com.example.btqt_nhom3.databinding.DialogPinCreateBinding

object PinCreateDialog {

    fun show(
        context: Context,
        onSuccess: () -> Unit,
        onCancel: (() -> Unit)? = null
    ) {
        val binding = DialogPinCreateBinding.inflate(LayoutInflater.from(context))

        val dialog = AlertDialog.Builder(context)
            .setView(binding.root)
            .setCancelable(true)
            .create()

        binding.btnCancel.setOnClickListener {
            dialog.dismiss()
            onCancel?.invoke()
        }

        binding.btnOK.setOnClickListener {
            val pin = binding.edtPin.text.toString()
            val q = binding.edtQuestion.text.toString()
            val a = binding.edtAnswer.text.toString()

            if (pin.length != 4) {
                binding.edtPin.error = "Nhập 4 số"
                return@setOnClickListener
            }

            if (q.isBlank()) {
                binding.edtQuestion.error = "Không được trống"
                return@setOnClickListener
            }

            if (a.isBlank()) {
                binding.edtAnswer.error = "Không được trống"
                return@setOnClickListener
            }

            PinManager.savePin(context, pin)
            SecurityQuestionManager.save(context, q, a)

            dialog.dismiss()
            onSuccess()
        }

        dialog.setOnCancelListener {
            onCancel?.invoke()
        }

        dialog.show()
    }
}


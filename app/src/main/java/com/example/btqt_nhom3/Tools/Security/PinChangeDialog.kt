package com.example.btqt_nhom3.Tools.Security

import android.content.Context
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import com.example.btqt_nhom3.databinding.DialogPinChangeBinding

object PinChangeDialog {

    fun show(context: Context, onSuccess: () -> Unit) {
        val binding = DialogPinChangeBinding.inflate(LayoutInflater.from(context))
        val dialog = AlertDialog.Builder(context)
            .setView(binding.root)
            .setCancelable(false)
            .create()

        var step = 0
        var newPinTemp: String? = null

        updateUI(binding, step)

        binding.btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        binding.btnOK.setOnClickListener {
            val pin = binding.edtPin.text.toString()

            if (pin.length != 4) {
                binding.edtPin.error = "Nhập 4 số"
                return@setOnClickListener
            }

            when (step) {
                0 -> {
                    newPinTemp = pin
                    step = 1
                    updateUI(binding, step)
                }

                1 -> {
                    if (pin != newPinTemp) {
                        binding.edtPin.error = "Không khớp, nhập lại"
                        return@setOnClickListener
                    }

                    // LƯU PIN
                    PinManager.savePin(context, pin)
                    dialog.dismiss()
                    onSuccess()
                }
            }
        }

        dialog.show()
    }

    private fun updateUI(binding: DialogPinChangeBinding, step: Int) {
        binding.edtPin.text.clear()

        when (step) {
            0 -> {
                binding.txtTitle.text = "Nhập mã PIN mới"
            }
            1 -> {
                binding.txtTitle.text = "Nhập lại mã PIN mới"
            }
        }
    }
}

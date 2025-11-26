package com.example.btqt_nhom3.Tools.Security

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.btqt_nhom3.MainActivity

class SplashSecurityActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Nếu không bật khóa app → vào thẳng
        if (!SecurityPrefs.isUsePin(this)) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        // Nếu bật khóa → yêu cầu xác thực
        PinAuthDialog.show(this,
            onSuccess = {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            },
            onFailed = {
                finish() // sai PIN -> thoát app
            }
        )
    }
}

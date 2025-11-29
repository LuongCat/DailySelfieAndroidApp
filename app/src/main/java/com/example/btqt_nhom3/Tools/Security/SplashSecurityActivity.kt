package com.example.btqt_nhom3.Tools.Security

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.btqt_nhom3.MainActivity

class SplashSecurityActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!SecurityPrefs.isUsePin(this)) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        PinAuthDialog.show(this,
            onSuccess = {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            },
            onFailed = {
                finish()
            }
        )
    }
}

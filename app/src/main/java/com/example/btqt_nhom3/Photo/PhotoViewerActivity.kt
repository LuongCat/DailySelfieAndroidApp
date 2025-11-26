package com.example.btqt_nhom3.Photo

import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.btqt_nhom3.R
import java.io.File
import android.content.Intent
import com.example.btqt_nhom3.MainActivity

class PhotoViewerActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_viewer)

        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        btnBack.setOnClickListener {
            if (isTaskRoot) {
                val intent = Intent(this, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            } else {
                finish()
            }
        }

        val photos = intent.getStringArrayListExtra("photos") ?: arrayListOf()
        val startIndex = intent.getIntExtra("start_index", 0)

        val pager = findViewById<ViewPager2>(R.id.photoViewPager)
        val files = photos.map { File(it) }

        pager.adapter = PhotoPagerAdapter(files)
        pager.setCurrentItem(startIndex, false)
    }
}
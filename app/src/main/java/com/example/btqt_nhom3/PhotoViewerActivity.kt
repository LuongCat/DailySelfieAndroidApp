package com.example.btqt_nhom3

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import java.io.File
import androidx.viewpager2.widget.ViewPager2
import android.widget.ImageButton

class PhotoViewerActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_viewer)

        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        btnBack.setOnClickListener { finish() }

        // Lấy ArrayList<String> trực tiếp, tránh deprecated getSerializableExtra
        val photos = intent.getStringArrayListExtra("photos") ?: arrayListOf()
        val startIndex = intent.getIntExtra("start_index", 0)

        val pager = findViewById<ViewPager2>(R.id.photoViewPager)
        val files = photos.map { File(it) }

        pager.adapter = PhotoPagerAdapter(files)
        pager.setCurrentItem(startIndex, false)
    }
}
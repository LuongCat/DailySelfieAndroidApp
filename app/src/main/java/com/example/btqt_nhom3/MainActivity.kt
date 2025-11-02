package com.example.btqt_nhom3

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: DailySelfieAdapter
    private val dailySelfies = mutableMapOf<String, List<File>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.rvSelfies)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = DailySelfieAdapter(dailySelfies)
        recyclerView.adapter = adapter

        loadSelfies()

        findViewById<FloatingActionButton>(R.id.btnTakeSelfie).setOnClickListener {
            startActivity(Intent(this, CameraActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        loadSelfies()
    }

    private fun loadSelfies() {
        val selfieDir = File(filesDir, "selfies")

        val allFiles = selfieDir.listFiles()?.toList() ?: emptyList()

        val grouped = allFiles.groupBy { file ->
            // File name format: yyyy-MM-dd_HH-mm-ss.jpg
            file.name.substring(0, 10) // lấy yyyy-MM-dd
        }

        val sortedGrouped = grouped.mapValues { entry ->
            entry.value.sortedByDescending { file ->
                val timePart = file.name.substring(11, 19) // HH-mm-ss
                timePart
            }
        }

        dailySelfies.clear()
        dailySelfies.putAll(sortedGrouped)

        adapter.notifyDataSetChanged()
    }
}

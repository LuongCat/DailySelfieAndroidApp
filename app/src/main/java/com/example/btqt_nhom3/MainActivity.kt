package com.example.btqt_nhom3

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.File
import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import com.example.btqt_nhom3.Camera.CameraActivity
import android.widget.ImageButton
import android.widget.PopupMenu
import com.example.btqt_nhom3.Notification.NotificationHelper
import com.example.btqt_nhom3.Photo.DailySelfieAdapter
import com.example.btqt_nhom3.Tools.Reminder.ReminderSettingsDialog
import com.example.btqt_nhom3.Tools.Security.PinAuthDialog
import com.example.btqt_nhom3.Tools.Security.SecurityPrefs
import com.example.btqt_nhom3.Tools.Security.SecuritySettingsDialog

class MainActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: DailySelfieAdapter
    private val dailySelfies = mutableMapOf<String, List<File>>()

    private val PERMISSION_REQUEST_CODE = 1001
    private val REQUIRED_PERMISSIONS = mutableListOf(
        Manifest.permission.CAMERA,
    ).apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.POST_NOTIFICATIONS)
        }
    }.toTypedArray()

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.rvSelfies)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = DailySelfieAdapter(dailySelfies)
        recyclerView.adapter = adapter

        requestNecessaryPermissions()

        loadSelfies()

        findViewById<FloatingActionButton>(R.id.btnTakeSelfie).setOnClickListener {
            openCamera();
        }

        val btnSettings = findViewById<ImageButton>(R.id.btnSettings)

        btnSettings.setOnClickListener { view ->
            val popup = PopupMenu(this, view)

            popup.menu.add("Cài đặt nhắc nhở")
            popup.menu.add("Tạo video Time-lapse")
            popup.menu.add("Sao lưu & Đồng bộ")
            popup.menu.add("Cài đặt bảo mật")

            popup.setOnMenuItemClickListener { item ->
                when (item.title) {
                    "Cài đặt nhắc nhở" -> {
                        ReminderSettingsDialog.show(this)
                    }
                    "Tạo video Time-lapse" -> {
                        // TODO
                    }
                    "Sao lưu & Đồng bộ" -> {
                        // TODO
                    }
                    "Cài đặt bảo mật" -> {
                        if (SecurityPrefs.isUsePin(this)) {

                            PinAuthDialog.show(
                                activity = this,
                                onSuccess = {
                                    SecuritySettingsDialog.show(this)
                                },
                                onFailed = {
                                    // người dùng thoát PIN → không mở settings
                                }
                            )

                        } else {
                            SecuritySettingsDialog.show(this)
                        }
                    }
                }
                true
            }

            popup.show()
        }


        NotificationHelper.createNotificationChannel(this);
    }

    override fun onResume() {
        super.onResume()
        loadSelfies()
    }

    private fun requestNecessaryPermissions() {
        val notGranted = REQUIRED_PERMISSIONS.filter {
            ActivityCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (notGranted.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, notGranted.toTypedArray(), PERMISSION_REQUEST_CODE)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PERMISSION_REQUEST_CODE) {
            for ((index, result) in grantResults.withIndex()) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    val perm = permissions[index]
                    Toast.makeText(this, "Permission denied: $perm", Toast.LENGTH_SHORT).show()
                }
            }
        }
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

    private fun openCamera() {
        startActivity(Intent(this, CameraActivity::class.java))
    }
}

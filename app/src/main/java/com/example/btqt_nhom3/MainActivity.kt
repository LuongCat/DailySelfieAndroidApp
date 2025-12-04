package com.example.btqt_nhom3

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.exifinterface.media.ExifInterface
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.btqt_nhom3.Camera.CameraActivity
import com.example.btqt_nhom3.Notification.NotificationHelper
import com.example.btqt_nhom3.Photo.DailySelfieAdapter
import com.example.btqt_nhom3.Photo.PhotoViewerActivity
import com.example.btqt_nhom3.Tools.Reminder.ReminderSettingsDialog
import com.example.btqt_nhom3.Tools.Security.PinAuthDialog
import com.example.btqt_nhom3.Tools.Security.SecurityPrefs
import com.example.btqt_nhom3.Tools.Security.SecuritySettingsDialog
import com.example.btqt_nhom3.Tools.Timelapse.TimelapseActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: DailySelfieAdapter

    private var dailySelfies = mutableMapOf<String, List<File>>()
    private var allPhotos: List<File> = emptyList()

    // ---- GLOBAL SELECT MODE ----
    private val selectedPhotos = mutableSetOf<File>()
    var globalSelectionMode = false

    @RequiresApi(Build.VERSION_CODES.S)
    private val REQUIRED_PERMISSIONS = mutableListOf(
        Manifest.permission.POST_NOTIFICATIONS,
        Manifest.permission.CAMERA,
        Manifest.permission.SCHEDULE_EXACT_ALARM,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.INTERNET,
        Manifest.permission.RECEIVE_BOOT_COMPLETED,
    ).apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            add(Manifest.permission.POST_NOTIFICATIONS)
    }.toTypedArray()

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.rvSelfies)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val deleteBar = findViewById<View>(R.id.deleteBar)
        val btnDelete = findViewById<ImageButton>(R.id.btnDelete)
        val btnCamera = findViewById<FloatingActionButton> (R.id.btnTakeSelfie)

        adapter = DailySelfieAdapter(
            dailySelfies,
            ::onPhotoClicked,

            { count, files ->
                selectedPhotos.clear()
                selectedPhotos.addAll(files)
                deleteBar.visibility = if (count > 0) View.VISIBLE else View.GONE
                btnCamera.visibility = if (count > 0) View.GONE else View.VISIBLE
            },

            { globalSelectionMode },

            {
                globalSelectionMode = true
            }
        )

        recyclerView.adapter = adapter

        requestPermissionsIfNeeded()
        loadSelfies()

        setupDeleteButton(btnDelete)
        setupClearSelectionButton(findViewById(R.id.btnClearSelection))
        setupTakePhotoButton()
        setupSettingsMenu()
        NotificationHelper.createNotificationChannel(this)
    }

    override fun onResume() {
        super.onResume()
        loadSelfies()
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun requestPermissionsIfNeeded() {
        val need = REQUIRED_PERMISSIONS.filter {
            ActivityCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (need.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, need.toTypedArray(), 1001)
        }
    }

    // ===========================================================
    // LOAD SELFIES
    // ===========================================================
    private fun loadSelfies() {
        val selfieDir = File(filesDir, "selfies")
        val allFiles = selfieDir.listFiles()?.filter { it.isFile } ?: emptyList()

        val grouped = allFiles.groupBy { file ->
            try {
                val exif = ExifInterface(file.absolutePath)
                val raw =
                    exif.getAttribute(ExifInterface.TAG_DATETIME_ORIGINAL)
                        ?: exif.getAttribute(ExifInterface.TAG_DATETIME)

                if (raw != null) {
                    val input = SimpleDateFormat("yyyy:MM:dd HH:mm:ss", Locale.getDefault())
                    val output = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    output.format(input.parse(raw)!!)
                } else {
                    val out = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    out.format(Date(file.lastModified()))
                }
            } catch (_: Exception) {
                val out = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                out.format(Date(file.lastModified()))
            }
        }

        dailySelfies.clear()
        dailySelfies.putAll(
            grouped.mapValues { (_, v) -> v.sortedByDescending { it.lastModified() } }
        )

        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        if (!dailySelfies.containsKey(today)) {
            dailySelfies[today] = emptyList()
        }

        allPhotos = dailySelfies.values.flatten()

        val sdfFull = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val sdfMonthDay = SimpleDateFormat("MM-dd", Locale.getDefault())

        val todayDate = Date()
        val todayFull = sdfFull.format(todayDate)
        val todayMD = sdfMonthDay.format(todayDate)

        val previousYearPhotos = grouped
            .filter { (key, _) ->
                try {
                    val md = key.substring(5)
                    md == todayMD && key != todayFull
                } catch (_: Exception) { false }
            }
            .flatMap { it.value }
            .sortedByDescending { it.lastModified() }

        dailySelfies["on_this_day"] = previousYearPhotos

        dailySelfies = LinkedHashMap<String, List<File>>().apply {
            put("on_this_day", previousYearPhotos)
            dailySelfies.forEach { (k, v) ->
                if (k != "on_this_day") put(k, v)
            }
        }

        adapter.updateData(dailySelfies)
    }

    // ===========================================================
    // DELETE
    // ===========================================================
    private fun setupDeleteButton(btn: ImageButton) {
        btn.setOnClickListener {
            if (selectedPhotos.isEmpty()) return@setOnClickListener

            AlertDialog.Builder(this)
                .setTitle("Xóa ảnh")
                .setMessage("Bạn có chắc muốn xóa ${selectedPhotos.size} ảnh?")
                .setPositiveButton("Xóa") { _, _ ->
                    selectedPhotos.forEach { f ->
                        if (f.exists()) f.delete()
                    }
                    exitSelectionMode()
                }
                .setNegativeButton("Hủy", null)
                .show()
        }
    }

    private fun setupClearSelectionButton(btn: ImageButton) {
        btn.setOnClickListener {
            exitSelectionMode()
        }
    }

    private fun exitSelectionMode() {
        selectedPhotos.clear()
        globalSelectionMode = false

        adapter.clearAllSelections()
        findViewById<View>(R.id.deleteBar).visibility = View.GONE
        findViewById<FloatingActionButton> (R.id.btnTakeSelfie).visibility = View.VISIBLE

        loadSelfies()
    }

    // ===========================================================
    // ON PHOTO CLICK
    // ===========================================================
    private fun onPhotoClicked(file: File) {
        // nếu đang chọn thì click là toggle, không mở viewer
        if (globalSelectionMode) return

        val index = allPhotos.indexOf(file)
        if (index < 0) {
            loadSelfies()
            return
        }
        val i = Intent(this, PhotoViewerActivity::class.java).apply {
            putStringArrayListExtra(
                "photos",
                ArrayList(allPhotos.map { it.absolutePath })
            )
            putExtra("start_index", index)
        }
        startActivity(i)
    }

    private fun setupTakePhotoButton() {
        findViewById<FloatingActionButton>(R.id.btnTakeSelfie).setOnClickListener {
            startActivity(Intent(this, CameraActivity::class.java))
        }
    }

    private fun setupSettingsMenu() {
        val btnSettings = findViewById<ImageButton>(R.id.btnSettings)

        btnSettings.setOnClickListener { view ->
            val popup = PopupMenu(this, view)
            popup.menu.add("Cài đặt nhắc nhở")
            popup.menu.add("Tạo video Time-lapse")
            popup.menu.add("Sao lưu và đồng bộ")
            popup.menu.add("Cài đặt bảo mật")

            popup.setOnMenuItemClickListener { item ->
                when (item.title) {
                    "Cài đặt nhắc nhở" ->
                        ReminderSettingsDialog.show(this)

                    "Tạo video Time-lapse" ->
                        startActivity(Intent(this, TimelapseActivity::class.java))

                    "Sao lưu và đồng bộ" ->
                        Toast.makeText(this,"Cooming soon...",Toast.LENGTH_SHORT).show()

                    "Cài đặt bảo mật" -> {
                        if (SecurityPrefs.isUsePin(this)) {
                            PinAuthDialog.show(
                                this,
                                onSuccess = { SecuritySettingsDialog.show(this) },
                                onFailed = {}
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
    }
}

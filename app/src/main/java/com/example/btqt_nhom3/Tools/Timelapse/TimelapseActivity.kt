package com.example.btqt_nhom3.Tools.Timelapse

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.btqt_nhom3.R
import java.text.SimpleDateFormat
import java.util.*
import java.io.File

class TimelapseActivity : AppCompatActivity() {

    private lateinit var txtStart: TextView
    private lateinit var txtEnd: TextView
    private lateinit var btnCreate: Button

    private lateinit var txtSpeedLabel: TextView
    private lateinit var seekFrames: SeekBar

    private var startDate = ""
    private var endDate = ""
    private val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    private lateinit var rv: RecyclerView
    private lateinit var adapterVideo: TimelapseAdapter

    private var imagesPerSecond: Double = 1.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timelapse)

        val toolbar = findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.timelapseToolbar)
        toolbar.setNavigationOnClickListener { finish() }

        txtStart = findViewById(R.id.txtStartDate)
        txtEnd = findViewById(R.id.txtEndDate)
        btnCreate = findViewById(R.id.btnCreateTimelapse)
        txtSpeedLabel = findViewById(R.id.txtSpeedLabel)
        seekFrames = findViewById(R.id.seekFrames)

        seekFrames.max = 10
        seekFrames.progress = 1

        seekFrames.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            @SuppressLint("SetTextI18n")
            override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                imagesPerSecond = if (progress < 1) 0.5 else progress.toDouble()
                txtSpeedLabel.text = "Tốc độ: $imagesPerSecond ảnh/giây"
            }
            override fun onStartTrackingTouch(p0: SeekBar?) {}
            override fun onStopTrackingTouch(p0: SeekBar?) {}
        })

        txtStart.setOnClickListener { pickDate(true) }
        txtEnd.setOnClickListener { pickDate(false) }

        btnCreate.setOnClickListener { createVideo() }

        rv = findViewById(R.id.rvTimelapseList)
        adapterVideo = TimelapseAdapter(
            emptyList(),
            onItemClick = { openVideo(it) },
            onDelete = { deleteVideo(it) }
        )
        rv.adapter = adapterVideo
        rv.layoutManager = LinearLayoutManager(this)

        loadTimelapseVideos()
    }

    private fun pickDate(isStart: Boolean) {
        val c = Calendar.getInstance()

        val dialog = DatePickerDialog(
            this,
            { _, y, m, d ->
                c.set(y, m, d)
                val dateStr = sdf.format(c.time)

                if (isStart) {
                    startDate = dateStr
                    txtStart.text = dateStr
                } else {
                    endDate = dateStr
                    txtEnd.text = dateStr
                }
            },
            c.get(Calendar.YEAR),
            c.get(Calendar.MONTH),
            c.get(Calendar.DAY_OF_MONTH)
        )
        dialog.show()
    }

    private fun createVideo() {
        if (startDate.isBlank() || endDate.isBlank()) {
            Toast.makeText(this, "Hãy chọn đủ ngày bắt đầu và kết thúc", Toast.LENGTH_SHORT).show()
            return
        }

        val calStart = Calendar.getInstance().apply {
            time = sdf.parse(startDate)!!
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0)
        }

        val calEnd = Calendar.getInstance().apply {
            time = sdf.parse(endDate)!!
            set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59); set(Calendar.SECOND, 59)
        }

        val start = calStart.time
        val end = calEnd.time

        if (start.after(end)) {
            Toast.makeText(this, "Ngày bắt đầu phải trước ngày kết thúc", Toast.LENGTH_SHORT).show()
            return
        }

        val images = SelfieFilter.getSelfiesBetween(this, start, end)

        if (images.isEmpty()) {
            Toast.makeText(this, "Không có ảnh trong khoảng đã chọn", Toast.LENGTH_LONG).show()
            return
        }

        val fps = 30.0
        val framesPerImage = (fps / imagesPerSecond).toInt()

        val output = TimeLapseGenerator.generate(
            this, images, start, end, framesPerImage
        )

        Toast.makeText(this, "Video đã tạo: ${output.absolutePath}", Toast.LENGTH_LONG).show()
        loadTimelapseVideos()
    }

    private fun loadTimelapseVideos() {
        val dir = File(filesDir, "selfies_timelapse")
        if (!dir.exists()) dir.mkdirs()

        val list = dir.listFiles()?.sortedByDescending { it.lastModified() } ?: emptyList()
        adapterVideo.update(list)
    }

    private fun openVideo(file: File) {
        val intent = Intent(this, VideoPlayerActivity::class.java)
        intent.putExtra("video", file.absolutePath)
        startActivity(intent)
    }

    private fun deleteVideo(file: File) {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Xóa video?")
            .setMessage("Bạn có chắc muốn xóa:\n${file.name} ?")
            .setPositiveButton("Xóa") { _, _ ->
                file.delete()
                loadTimelapseVideos()
                Toast.makeText(this, "Đã xóa", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Hủy", null)
            .show()
    }
}

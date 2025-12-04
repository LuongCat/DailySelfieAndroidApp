package com.example.btqt_nhom3.Photo

import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.btqt_nhom3.R
import java.io.File
import android.content.Intent
import android.app.AlertDialog
import android.text.InputFilter
import android.widget.EditText
import android.widget.LinearLayout
import androidx.exifinterface.media.ExifInterface
import java.text.SimpleDateFormat
import java.util.*
import com.example.btqt_nhom3.MainActivity

class PhotoViewerActivity : AppCompatActivity() {

    private lateinit var pager: ViewPager2
    private lateinit var photoAdapter: PhotoPagerAdapter
    private lateinit var photos: ArrayList<String>
    private lateinit var txtDate: TextView
    private lateinit var txtEmoji: TextView
    private lateinit var txtFeeling: TextView

    private var EditingPhotoPath: String? = null

    private val editPhotoLauncher = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {

            val newPath = result.data?.getStringExtra("edited_path") ?: return@registerForActivityResult

            val index = pager.currentItem

            photos.add(index, newPath)

            val files = photos.map { File(it) }
            photoAdapter = PhotoPagerAdapter(files)
            pager.adapter = photoAdapter

            pager.setCurrentItem(index, false)

            loadFeelingAndDate(index)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_viewer)

        txtEmoji = findViewById(R.id.txtEmoji)
        txtFeeling = findViewById(R.id.txtFeeling)
        txtDate = findViewById(R.id.txtDate)

        val btnEditFeeling = findViewById<ImageButton>(R.id.btnEditFeeling)
        val btnEditImage = findViewById<ImageButton>(R.id.btnEditImage)

        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        val btnDelete = findViewById<ImageButton>(R.id.btnDelete)

        photos = intent.getStringArrayListExtra("photos") ?: arrayListOf()
        val startIndex = intent.getIntExtra("start_index", 0)

        pager = findViewById(R.id.photoViewPager)

        val files = photos.map { File(it) }
        photoAdapter = PhotoPagerAdapter(files)
        pager.adapter = photoAdapter
        pager.setCurrentItem(startIndex, false)

        if (photos.isNotEmpty()) {
            loadFeelingAndDate(startIndex)
        }

        pager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                loadFeelingAndDate(position)
            }
        })

        btnBack.setOnClickListener {
            if (isTaskRoot) {
                val intent = Intent(this, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            } else finish()
        }

        btnEditImage.setOnClickListener {
            openEditor()
        }

        btnEditFeeling.setOnClickListener {
            openEditFeelingDialog(txtEmoji, txtFeeling)
        }

        btnDelete.setOnClickListener {
            val pos = pager.currentItem
            val path = photos[pos]

            AlertDialog.Builder(this)
                .setTitle("Xóa ảnh?")
                .setMessage("Bạn có chắc muốn xóa không?")
                .setPositiveButton("Xóa") { _, _ ->
                    if (deletePhoto(path)) {

                        photos.removeAt(pos)

                        val newFiles = photos.map { File(it) }
                        photoAdapter = PhotoPagerAdapter(newFiles)
                        pager.adapter = photoAdapter

                        if (photos.isEmpty()) {
                            finish()
                            return@setPositiveButton
                        }

                        pager.setCurrentItem(maxOf(0, pos - 1), false)
                    }
                }
                .setNegativeButton("Hủy", null)
                .show()
        }
    }

    private fun openEditor() {
        EditingPhotoPath = photos[pager.currentItem]

        val intent = Intent(this, PhotoEditorActivity::class.java)
        intent.putExtra("photo_path", EditingPhotoPath)
        editPhotoLauncher.launch(intent)
    }

    private fun loadFeelingAndDate(position: Int) {
        val path = photos[position]
        val file = File(path)

        val feelingData = FeelingManager.loadFeeling(path)
        if (feelingData == null) {
            txtEmoji.text = "🙂"
            txtFeeling.text = "Hôm nay thế nào nhỉ?"
        } else {
            txtEmoji.text = feelingData.emoji
            txtFeeling.text = feelingData.text
        }

        txtDate.text = getPhotoDate(file)
    }

    private fun getPhotoDate(file: File): String {
        return try {
            val exif = ExifInterface(file.absolutePath)
            val dateStr = exif.getAttribute(ExifInterface.TAG_DATETIME_ORIGINAL)
                ?: exif.getAttribute(ExifInterface.TAG_DATETIME)

            if (dateStr != null) {
                val input = SimpleDateFormat("yyyy:MM:dd HH:mm:ss", Locale.getDefault())
                val output = SimpleDateFormat("'Ngày' dd 'tháng' MM 'năm' yyyy", Locale.getDefault())
                val date = input.parse(dateStr)
                if (date != null) return output.format(date)
            }

            val name = file.nameWithoutExtension
            val fmtIn = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault())
            val fmtOut = SimpleDateFormat("'Ngày' dd 'tháng' MM 'năm' yyyy", Locale.getDefault())
            fmtOut.format(fmtIn.parse(name))

        } catch (_: Exception) {
            "Không rõ ngày"
        }
    }

    private fun openEditFeelingDialog(txtEmoji: TextView, txtFeeling: TextView) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_feeling, null)

        val emojiRow = dialogView.findViewById<LinearLayout>(R.id.emojiRow)
        val edtFeeling = dialogView.findViewById<EditText>(R.id.edtFeeling)
        edtFeeling.filters = arrayOf(InputFilter.LengthFilter(360))

        edtFeeling.setText(txtFeeling.text.toString())

        val emojiList = listOf("😀","😊","😁","🤣","😍","❤️","💔","😮","😘","😎","🤗","😢","😭","😡","😴","🤔","😇","🥲","😜")
        var selectedEmoji = txtEmoji.text.toString()

        emojiList.forEach { emo ->
            val tv = TextView(this).apply {
                text = emo
                textSize = 28f
                setPadding(24, 16, 24, 16)
                setOnClickListener {
                    selectedEmoji = emo
                    highlightEmoji(emojiRow, this)
                }
            }
            emojiRow.addView(tv)
        }

        highlightEmoji(emojiRow, findEmojiView(emojiRow, selectedEmoji))

        AlertDialog.Builder(this)
            .setTitle("Chọn cảm xúc")
            .setView(dialogView)
            .setPositiveButton("Lưu") { _, _ ->
                txtEmoji.text = selectedEmoji
                txtFeeling.text = edtFeeling.text.toString()

                val currentPos = pager.currentItem
                if (currentPos in photos.indices) {
                    val path = photos[currentPos]
                    FeelingManager.saveFeeling(path, selectedEmoji, edtFeeling.text.toString())
                }
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun highlightEmoji(row: LinearLayout, selectedView: TextView?) {
        for (i in 0 until row.childCount) {
            val child = row.getChildAt(i) as TextView
            child.setBackgroundColor(0x00000000)
        }
        selectedView?.setBackgroundColor(0x330000FF)
    }

    private fun findEmojiView(row: LinearLayout, emoji: String): TextView? {
        for (i in 0 until row.childCount) {
            val tv = row.getChildAt(i) as TextView
            if (tv.text.toString() == emoji) return tv
        }
        return null
    }

    fun deletePhoto(path: String): Boolean {
        return try {
            val file = File(path)
            file.exists() && file.delete()
        } catch (_: Exception) {
            false
        }
    }
}

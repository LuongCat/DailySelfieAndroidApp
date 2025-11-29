package com.example.btqt_nhom3.Photo

import android.content.Intent
import android.graphics.*
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.exifinterface.media.ExifInterface
import com.canhub.cropper.CropImageView
import com.example.btqt_nhom3.R
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class PhotoEditorActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_PHOTO_PATH = "photo_path"
        const val RESULT_EXTRA_EDITED_PATH = "edited_path"
    }

    private lateinit var cropView: CropImageView
    private lateinit var btnCropToggle: ImageButton
    private lateinit var btnApplyCrop: ImageButton
    private lateinit var btnRotate: ImageButton
    private lateinit var btnFilters: ImageButton
    private lateinit var btnSave: ImageButton
    private lateinit var btnCancel: ImageButton

    private var originalPath: String? = null
    private var originalBitmap: Bitmap? = null
    private var workingBitmap: Bitmap? = null

    private var rotationDegrees = 0
    private var cropMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_editor)

        cropView = findViewById(R.id.cropImageView)
        btnCropToggle = findViewById(R.id.btnCropToggle)
        btnApplyCrop = findViewById(R.id.btnApplyCrop)
        btnRotate = findViewById(R.id.btnRotate)
        btnFilters = findViewById(R.id.btnFilters)
        btnSave = findViewById(R.id.btnSave)
        btnCancel = findViewById(R.id.btnCancel)

        originalPath = intent.getStringExtra(EXTRA_PHOTO_PATH)
        if (originalPath == null) {
            Toast.makeText(this, "Không có ảnh để chỉnh.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        originalBitmap = BitmapFactory.decodeFile(originalPath)
        if (originalBitmap == null) {
            Toast.makeText(this, "Không thể mở ảnh.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        workingBitmap = originalBitmap?.copy(Bitmap.Config.ARGB_8888, true)
        cropView.setImageBitmap(workingBitmap)

        cropView.setFixedAspectRatio(false)
        cropView.guidelines = CropImageView.Guidelines.OFF

        btnCropToggle.setOnClickListener {
            cropMode = !cropMode
            updateUIForCropMode()

            cropView.guidelines = if (cropMode)
                CropImageView.Guidelines.ON
            else
                CropImageView.Guidelines.OFF
        }

        btnApplyCrop.setOnClickListener {
            if (!cropMode) return@setOnClickListener

            val cropped = cropView.croppedImage
            if (cropped != null) {
                workingBitmap?.recycle()
                workingBitmap = cropped.copy(Bitmap.Config.ARGB_8888, true)
                rotationDegrees = 0

                cropView.setImageBitmap(workingBitmap)

                cropMode = false
                cropView.guidelines = CropImageView.Guidelines.OFF

                updateUIForCropMode()
            } else {
                Toast.makeText(this, "Chọn vùng crop trước.", Toast.LENGTH_SHORT).show()
            }
        }

        btnRotate.setOnClickListener {
            rotationDegrees = (rotationDegrees + 90) % 360
            workingBitmap = workingBitmap?.let { rotateBitmap(it, 90f) }
            workingBitmap?.let { cropView.setImageBitmap(it) }
        }

        btnFilters.setOnClickListener { showFilterChooser() }

        btnSave.setOnClickListener { saveEditedAndReturn() }

        btnCancel.setOnClickListener {
            if (cropMode) {
                cropMode = false
                cropView.guidelines = CropImageView.Guidelines.OFF
                updateUIForCropMode()
            } else {
                setResult(RESULT_CANCELED)
                finish()
            }
        }
    }

    /* =============================================================
       ==========   ẨN/HIỆN NÚT THEO CHẾ ĐỘ CROP   =================
       ============================================================= */
    private fun updateUIForCropMode() {
        if (cropMode) {
            btnCropToggle.visibility = View.GONE
            btnRotate.visibility = View.GONE
            btnFilters.visibility = View.GONE
            btnSave.visibility = View.GONE

            btnApplyCrop.visibility = View.VISIBLE
            btnCancel.visibility = View.VISIBLE
        } else {
            btnCropToggle.visibility = View.VISIBLE
            btnRotate.visibility = View.VISIBLE
            btnFilters.visibility = View.VISIBLE
            btnSave.visibility = View.VISIBLE

            btnApplyCrop.visibility = View.GONE
        }
    }

    /* =============================================================
       ======================== FILTERS =============================
       ============================================================= */

    private fun showFilterChooser() {
        val items = arrayOf("None", "Grayscale", "Sepia", "Invert", "High Contrast")

        AlertDialog.Builder(this)
            .setTitle("Chọn filter")
            .setItems(items) { _, which ->
                when (which) {
                    0 -> workingBitmap = originalBitmap?.copy(Bitmap.Config.ARGB_8888, true)
                    1 -> applyColorMatrixFilter(ColorMatrix().apply { setSaturation(0f) })
                    2 -> applySepia()
                    3 -> applyInvert()
                    4 -> applyHighContrast()
                }

                if (rotationDegrees != 0) {
                    workingBitmap = workingBitmap?.let { rotateBitmap(it, rotationDegrees.toFloat()) }
                    rotationDegrees = 0
                }

                workingBitmap?.let { cropView.setImageBitmap(it) }
            }.show()
    }

    private fun applyColorMatrixFilter(matrix: ColorMatrix) {
        val src = originalBitmap ?: return
        val bmp = src.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(bmp)
        val paint = Paint().apply { colorFilter = ColorMatrixColorFilter(matrix) }
        canvas.drawBitmap(bmp, 0f, 0f, paint)
        workingBitmap = bmp
    }

    private fun applySepia() {
        val m = ColorMatrix()
        m.set(
            floatArrayOf(
                0.393f, 0.769f, 0.189f, 0f, 0f,
                0.349f, 0.686f, 0.168f, 0f, 0f,
                0.272f, 0.534f, 0.131f, 0f, 0f,
                0f, 0f, 0f, 1f, 0f
            )
        )
        applyColorMatrixFilter(m)
    }

    private fun applyInvert() {
        val m = ColorMatrix(
            floatArrayOf(
                -1f, 0f, 0f, 0f, 255f,
                0f, -1f, 0f, 0f, 255f,
                0f, 0f, -1f, 0f, 255f,
                0f, 0f, 0f, 1f, 0f
            )
        )
        applyColorMatrixFilter(m)
    }

    private fun applyHighContrast() {
        val contrast = 1.4f
        val translate = (-0.5f * contrast + 0.5f) * 255f

        val m = ColorMatrix(
            floatArrayOf(
                contrast, 0f, 0f, 0f, translate,
                0f, contrast, 0f, 0f, translate,
                0f, 0f, contrast, 0f, translate,
                0f, 0f, 0f, 1f, 0f
            )
        )
        applyColorMatrixFilter(m)
    }

    /* =============================================================
       ========================== SAVE ==============================
       ============================================================= */

    private fun saveEditedAndReturn() {
        val bmp = workingBitmap ?: run {
            Toast.makeText(this, "Không có dữ liệu để lưu.", Toast.LENGTH_SHORT).show()
            return
        }

        val dir = File(filesDir, "selfies")
        if (!dir.exists()) dir.mkdirs()

        val originalFile = originalPath?.let { File(it) }
        val originalName = originalFile?.nameWithoutExtension ?: "photo"
        val timestamp = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(Date())
        val newName = "${originalName}_edit_$timestamp.jpg"
        val newFile = File(dir, newName)

        try {
            FileOutputStream(newFile).use { out ->
                bmp.compress(Bitmap.CompressFormat.JPEG, 95, out)
            }

            // Copy EXIF
            originalFile?.let { srcFile ->
                try {
                    val srcExif = ExifInterface(srcFile.absolutePath)
                    val dstExif = ExifInterface(newFile.absolutePath)

                    val tags = arrayOf(
                        ExifInterface.TAG_APERTURE_VALUE,
                        ExifInterface.TAG_F_NUMBER,
                        ExifInterface.TAG_EXPOSURE_TIME,
                        ExifInterface.TAG_ISO_SPEED_RATINGS,
                        ExifInterface.TAG_DATETIME,
                        ExifInterface.TAG_DATETIME_ORIGINAL,
                        ExifInterface.TAG_DATETIME_DIGITIZED,
                        ExifInterface.TAG_FLASH,
                        ExifInterface.TAG_FOCAL_LENGTH,
                        ExifInterface.TAG_GPS_LATITUDE,
                        ExifInterface.TAG_GPS_LONGITUDE,
                        ExifInterface.TAG_GPS_ALTITUDE,
                        ExifInterface.TAG_MAKE,
                        ExifInterface.TAG_MODEL,
                        ExifInterface.TAG_ORIENTATION,
                        ExifInterface.TAG_WHITE_BALANCE
                    )

                    for (tag in tags) {
                        srcExif.getAttribute(tag)?.let { value ->
                            dstExif.setAttribute(tag, value)
                        }
                    }
                    dstExif.saveAttributes()
                } catch (_: Exception) {}
            }

            val resultIntent = Intent().apply {
                putExtra(RESULT_EXTRA_EDITED_PATH, newFile.absolutePath)
            }
            setResult(RESULT_OK, resultIntent)
            finish()

        } catch (e: Exception) {
            Toast.makeText(this, "Lỗi lưu ảnh: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun rotateBitmap(src: Bitmap, degrees: Float): Bitmap {
        val matrix = Matrix().apply { postRotate(degrees) }
        return Bitmap.createBitmap(src, 0, 0, src.width, src.height, matrix, true)
    }
}

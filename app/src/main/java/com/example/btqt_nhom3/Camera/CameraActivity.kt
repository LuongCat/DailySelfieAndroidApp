package com.example.btqt_nhom3.Camera

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.btqt_nhom3.Photo.FeelingManager
import com.example.btqt_nhom3.Photo.PhotoViewerActivity
import com.example.btqt_nhom3.R
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.Executors
import com.google.android.material.imageview.ShapeableImageView
import coil.load
import androidx.exifinterface.media.ExifInterface


class CameraActivity : AppCompatActivity() {

    private lateinit var previewView: PreviewView
    private lateinit var btnCapture: ImageButton
    private lateinit var btnSwitch: ImageButton

    private var imageCapture: ImageCapture? = null
    private var cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
    private val backgroundExecutor = Executors.newSingleThreadExecutor()

    companion object {
        private const val CAMERA_PERMISSION_CODE = 100
        private const val TAG = "CameraActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        previewView = findViewById(R.id.previewView)
        btnCapture = findViewById(R.id.btnCapture)
        btnSwitch = findViewById(R.id.btnSwitch)

        if (!hasPermission()) requestPermission() else startCamera()

        btnCapture.setOnClickListener { takePhoto() }

        btnSwitch.setOnClickListener {
            cameraSelector =
                if (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA)
                    CameraSelector.DEFAULT_FRONT_CAMERA
                else
                    CameraSelector.DEFAULT_BACK_CAMERA

            startCamera()
        }

        // Thumbnail
        val btnThumbnail = findViewById<ShapeableImageView>(R.id.btnThumbnail)
        loadLatestThumbnail()

        btnThumbnail.setOnClickListener {
            openLatestPhoto()
        }
    }

    private fun hasPermission() =
        ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.CAMERA),
            CAMERA_PERMISSION_CODE
        )
    }

    override fun onRequestPermissionsResult(requestCode: Int, p: Array<out String>, g: IntArray) {
        super.onRequestPermissionsResult(requestCode, p, g)
        if (requestCode == CAMERA_PERMISSION_CODE && hasPermission()) startCamera()
        else finish()
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().apply {
                setSurfaceProvider(previewView.surfaceProvider)
            }

            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()

            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                this,
                cameraSelector,
                preview,
                imageCapture
            )

        }, ContextCompat.getMainExecutor(this))
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return

        val dir = File(filesDir, "selfies")
        if (!dir.exists()) dir.mkdirs()

        val filename =
            SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(Date()) + ".jpg"
        val photoFile = File(dir, filename)

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {

                @RequiresApi(Build.VERSION_CODES.O)
                override fun onImageSaved(res: ImageCapture.OutputFileResults) {

                    playFlashAnimation()

                    backgroundExecutor.execute {
                        processImageAfterCapture(photoFile)
                        runOnUiThread { loadLatestThumbnail() }
                    }
                }

                override fun onError(e: ImageCaptureException) {
                    Toast.makeText(this@CameraActivity, e.message, Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    private fun playFlashAnimation() {
        val flash = View(this)
        flash.setBackgroundColor(0xFFFFFFFF.toInt())
        flash.alpha = 0f

        addContentView(
            flash,
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )

        flash.animate()
            .alpha(0.8f)
            .setDuration(60)
            .withEndAction {
                flash.animate()
                    .alpha(0f)
                    .setDuration(90)
                    .withEndAction {
                        (flash.parent as ViewGroup).removeView(flash)
                    }
            }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun processImageAfterCapture(photoFile: File) {
        try {
            val isFront = cameraSelector == CameraSelector.DEFAULT_FRONT_CAMERA

            val originalExif = ExifInterface(photoFile.absolutePath)

            if (isFront) {
                val bitmap = BitmapFactory.decodeFile(photoFile.absolutePath)
                val matrix = Matrix().apply { preScale(-1f, 1f) }
                val flipped = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)

                FileOutputStream(photoFile).use {
                    flipped.compress(Bitmap.CompressFormat.JPEG, 95, it)
                }
            }

            val exif = ExifInterface(photoFile.absolutePath)

            val tagsToCopy = listOf(
                ExifInterface.TAG_APERTURE_VALUE,
                ExifInterface.TAG_DATETIME,
                ExifInterface.TAG_EXPOSURE_TIME,
                ExifInterface.TAG_FLASH,
                ExifInterface.TAG_FOCAL_LENGTH,
                ExifInterface.TAG_GPS_LATITUDE,
                ExifInterface.TAG_GPS_LONGITUDE,
                ExifInterface.TAG_MAKE,
                ExifInterface.TAG_MODEL,
                ExifInterface.TAG_WHITE_BALANCE
            )
            for (tag in tagsToCopy) {
                originalExif.getAttribute(tag)?.let { value ->
                    exif.setAttribute(tag, value)
                }
            }

            exif.saveAttributes()

            // =====================================
            // SET NGÀY GIẢ Ở ĐÂY NÈÈÈÈÈÈÈÈÈÈÈÈÈÈÈÈÈÈÈÈÈÈÈÈÈÈÈÈÈÈÈÈÈÈÈÈÈÈÈÈÈÈÈÈÈÈÈÈÈÈÈÈÈÈÈ
            // =====================================
            //setFakeDate(photoFile, 2024, 12, 25)

            FeelingManager.saveFeeling(photoFile.absolutePath, "🙂", "Hôm nay thế nào nhỉ?")

        } catch (e: Exception) {
            Log.e("CameraActivity", "Error process image", e)
        }
    }

    // ---------------------------
    // Thumbnail
    // ---------------------------

    private fun loadLatestThumbnail() {
        val thumb = findViewById<ShapeableImageView>(R.id.btnThumbnail)

        val folder = File(filesDir, "selfies")
        if (!folder.exists()) {
            thumb.setImageResource(android.R.drawable.ic_menu_gallery)
            return
        }

        val latestFile = folder.listFiles()
            ?.filter { it.isFile }?.maxByOrNull { it.lastModified() }

        if (latestFile != null) {
            thumb.load(latestFile) {
                crossfade(true)
                placeholder(android.R.drawable.ic_menu_gallery)
                error(android.R.drawable.ic_delete)
            }
        } else {
            thumb.setImageResource(android.R.drawable.ic_menu_gallery)
        }
    }


    // ---------------------------
    // Open Photo Viewer (all photos)
    // ---------------------------

    private fun openLatestPhoto() {
        val dir = File(filesDir, "selfies")
        val files = dir.listFiles()?.sortedByDescending { it.lastModified() } ?: return
        if (files.isEmpty()) return

        val photos = ArrayList(files.map { it.absolutePath })
        val latest = files.first().absolutePath
        val startIndex = photos.indexOf(latest)

        val intent = Intent(this, PhotoViewerActivity::class.java)
        intent.putStringArrayListExtra("photos", photos)
        intent.putExtra("start_index", startIndex)
        startActivity(intent)
    }

    // ---------------------------
    // Test
    // ---------------------------

    private fun setFakeDate(photoFile: File, year: Int, month: Int, day: Int) {
        try {
            val exif = ExifInterface(photoFile.absolutePath)

            val fakeDate = "%04d:%02d:%02d 12:00:00".format(year, month, day)

            exif.setAttribute(ExifInterface.TAG_DATETIME, fakeDate)
            exif.setAttribute(ExifInterface.TAG_DATETIME_ORIGINAL, fakeDate)
            exif.setAttribute(ExifInterface.TAG_DATETIME_DIGITIZED, fakeDate)
            exif.saveAttributes()

            val sdf = SimpleDateFormat("yyyy:MM:dd HH:mm:ss", Locale.getDefault())
            val date = sdf.parse(fakeDate)
            if (date != null) {
                photoFile.setLastModified(date.time)
            }

            Log.d("FAKE_DATE", "Set ngày giả: $fakeDate")

        } catch (e: Exception) {
            Log.e("FAKE_DATE", "Lỗi set ngày giả", e)
        }
    }
}

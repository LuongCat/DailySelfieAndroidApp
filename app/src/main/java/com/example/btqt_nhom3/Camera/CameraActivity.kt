package com.example.btqt_nhom3.Camera

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.Build
import android.os.Bundle
import android.util.Log
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
import com.example.btqt_nhom3.Photo.PhotoViewerActivity
import com.example.btqt_nhom3.R
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.io.FileOutputStream
import androidx.core.content.edit

class CameraActivity : AppCompatActivity() {

    private lateinit var previewView: PreviewView
    private lateinit var btnCapture: ImageButton
    private lateinit var btnSwitch: ImageButton

    private var imageCapture: ImageCapture? = null
    private var cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

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

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_CODE
            )
        } else {
            startCamera()
        }

        btnCapture.setOnClickListener { takePhoto() }

        btnSwitch.setOnClickListener {
            cameraSelector = if (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA)
                CameraSelector.DEFAULT_FRONT_CAMERA
            else CameraSelector.DEFAULT_BACK_CAMERA
            startCamera()
        }
    }

    private fun allPermissionsGranted() = ContextCompat.checkSelfPermission(
        this, Manifest.permission.CAMERA
    ) == PackageManager.PERMISSION_GRANTED

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.Companion.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            imageCapture = ImageCapture.Builder().build()

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture
                )
            } catch (e: Exception) {
                Log.e(TAG, "Use case binding failed", e)
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return

        val selfiesDir = File(filesDir, "selfies")
        if (!selfiesDir.exists()) selfiesDir.mkdirs()

        val timestamp = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault()).format(Date())
        val photoFile = File(selfiesDir, "$timestamp.jpg")

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                @RequiresApi(Build.VERSION_CODES.O)
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {

                    // ==== LẬT ẢNH SELFIE (CHỈ ÁP DỤNG CHO CAMERA TRƯỚC) ====
                    if (cameraSelector == CameraSelector.DEFAULT_FRONT_CAMERA) {
                        try {
                            val bitmap = BitmapFactory.decodeFile(photoFile.absolutePath)

                            // ma trận lật ngang
                            val matrix = Matrix().apply {
                                preScale(-1f, 1f)
                            }

                            val flipped = Bitmap.createBitmap(
                                bitmap,
                                0, 0,
                                bitmap.width,
                                bitmap.height,
                                matrix,
                                true
                            )

                            // ghi ảnh đã lật lại vào file cũ
                            FileOutputStream(photoFile).use { out ->
                                flipped.compress(Bitmap.CompressFormat.JPEG, 100, out)
                            }

                            val today = java.time.LocalDate.now().toString()
                            val prefs = this@CameraActivity.getSharedPreferences("SelfiePrefs", Context.MODE_PRIVATE)
                            prefs.edit { putString("lastSelfieDate", today) }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }

                    // === CHUYỂN ẢNH QUA PHOTOVIEWER ===
                    val photoList = arrayListOf(photoFile.absolutePath)

                    val intent = Intent(this@CameraActivity, PhotoViewerActivity::class.java)
                    intent.putStringArrayListExtra("photos", photoList)
                    intent.putExtra("start_index", 0)
                    startActivity(intent)

                    finish()
                }

                override fun onError(exception: ImageCaptureException) {
                    Toast.makeText(
                        this@CameraActivity,
                        "Photo capture failed: ${exception.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.e(TAG, "Photo capture failed", exception)
                }
            }
        )
    }
}
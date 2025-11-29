package com.example.btqt_nhom3.Photo

import androidx.exifinterface.media.ExifInterface
import android.util.Base64
import android.util.Log

data class FeelingData(val emoji: String, val text: String)

object FeelingManager {
    fun saveFeeling(path: String, emoji: String, text: String) {
        val full = "$emoji|$text"
        saveFeelingToJpeg(path, full)    // hàm EXIF bạn đã có
    }

    fun loadFeeling(path: String): FeelingData? {
        val raw = loadFeelingFromJpeg(path) ?: return null
        return if (raw.contains("|")) {
            val parts = raw.split("|", limit = 2)
            FeelingData(parts[0], parts[1])
        } else null
    }

    fun saveFeelingToJpeg(imagePath: String, feeling: String) {
        Log.e("Feeling-SAVE", "Before save: $feeling")

        val exif = ExifInterface(imagePath)

        val utf8Bytes = feeling.toByteArray(Charsets.UTF_8)
        val value = "charset=\"UTF-8\" " + Base64.encodeToString(utf8Bytes, Base64.NO_WRAP)

        exif.setAttribute(ExifInterface.TAG_USER_COMMENT, value)
        exif.saveAttributes()

        val exifCheck = ExifInterface(imagePath)
        val saved = exifCheck.getAttribute(ExifInterface.TAG_USER_COMMENT)

        Log.e("Feeling-SAVE", "After save (raw EXIF): $saved")
    }


    fun loadFeelingFromJpeg(imagePath: String): String? {
        val exif = ExifInterface(imagePath)
        val raw = exif.getAttribute(ExifInterface.TAG_USER_COMMENT) ?: return null

        return try {
            if (raw.startsWith("charset=\"UTF-8\"")) {
                val base64Part = raw.substringAfter("UTF-8\" ").trim()
                val decoded = Base64.decode(base64Part, Base64.NO_WRAP)
                String(decoded, Charsets.UTF_8)
            } else {
                raw        // fallback
            }
        } catch (e: Exception) {
            null
        }
    }
}
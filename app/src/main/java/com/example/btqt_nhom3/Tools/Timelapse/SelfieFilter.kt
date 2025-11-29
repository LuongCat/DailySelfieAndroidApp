package com.example.btqt_nhom3.Tools.Timelapse

import android.content.Context
import androidx.exifinterface.media.ExifInterface
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

object SelfieFilter {

    fun getSelfiesBetween(context: Context, start: Date, end: Date): List<String> {

        val dir = File(context.filesDir, "selfies")
        if (!dir.exists()) return emptyList()

        val inputFmt = SimpleDateFormat("yyyy:MM:dd HH:mm:ss", Locale.getDefault())

        val result = mutableListOf<String>()

        dir.listFiles()?.forEach { file ->
            if (!file.isFile) return@forEach

            val exif = ExifInterface(file.absolutePath)
            val rawDate =
                exif.getAttribute(ExifInterface.TAG_DATETIME_ORIGINAL)
                    ?: exif.getAttribute(ExifInterface.TAG_DATETIME)

            val date =
                if (rawDate != null) {
                    inputFmt.parse(rawDate)
                } else {
                    Date(file.lastModified())
                }

            if (date != null && !date.before(start) && !date.after(end)) {
                result.add(file.absolutePath)
            }
        }

        return result.sorted()
    }
}

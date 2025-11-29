//package com.example.btqt_nhom3.Tools.BackupAndSync
//
//import android.util.Log
//import okhttp3.MediaType.Companion.toMediaType
//import okhttp3.MultipartBody
//import okhttp3.OkHttpClient
//import okhttp3.Request
//import okhttp3.RequestBody.Companion.asRequestBody
//import java.io.File
//
//object DriveBackup {
//
//    /**
//     * Upload 1 file ảnh lên Google Drive
//     * @param accessToken token từ AuthorizationClient
//     * @param file ảnh local
//     */
//    suspend fun uploadImage(accessToken: String, file: File): Boolean {
//        try {
//            val client = OkHttpClient()
//
//            val mediaType = "image/jpeg".toMediaType()
//            val body = MultipartBody.Builder()
//                .setType(MultipartBody.FORM)
//                .addFormDataPart(
//                    "metadata", null,
//                    MultipartBody.Part.create(
//                        """
//                        {
//                          "name": "${file.name}",
//                          "mimeType": "image/jpeg"
//                        }
//                        """.trimIndent().toByteArray()
//                            .toRequestBody("application/json; charset=utf-8".toMediaType())
//                    )
//                )
//                .addFormDataPart(
//                    "file",
//                    file.name,
//                    file.asRequestBody(mediaType)
//                )
//                .build()
//
//            val request = Request.Builder()
//                .url("https://www.googleapis.com/upload/drive/v3/files?uploadType=multipart")
//                .addHeader("Authorization", "Bearer $accessToken")
//                .post(body)
//                .build()
//
//            val response = client.newCall(request).execute()
//
//            return response.isSuccessful
//
//        } catch (e: Exception) {
//            Log.e("DriveBackup", "Upload failed", e)
//            return false
//        }
//    }
//}

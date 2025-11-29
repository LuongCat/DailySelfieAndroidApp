//package com.example.btqt_nhom3.Tools.BackupAndSync
//
//import android.app.Activity
//import androidx.credentials.CredentialManager
//import androidx.credentials.GetCredentialRequest
//
//import com.google.android.libraries.identity.googleid.GetGoogleIdOption
//import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
//
//import com.google.android.gms.auth.api.identity.AuthorizationRequest
//import com.google.android.gms.auth.api.identity.Identity
//import com.google.android.gms.common.api.Scope
//
//import kotlinx.coroutines.tasks.await
//
//object DriveSignIn {
//
//    private const val WEB_CLIENT_ID = "YOUR_WEB_CLIENT_ID"
//
//    /**
//     * Step 1 – Sign in Google account (Credential Manager)
//     * Trả về Google ID Token
//     */
//    suspend fun signIn(activity: Activity): String {
//        val googleOption = GetGoogleIdOption.Builder()
//            .setFilterByAuthorizedAccounts(false)      // Cho phép chọn nhiều tài khoản
//            .setServerClientId(WEB_CLIENT_ID)          // Web client ID chuẩn (OAuth 2.0)
//            .build()
//
//        val request = GetCredentialRequest.Builder()
//            .addCredentialOption(googleOption)
//            .build()
//
//        val credentialManager = CredentialManager.create(activity)
//
//        val result = credentialManager.getCredential(
//            activity,
//            request
//        )
//
//        val credential = result.credential as GoogleIdTokenCredential
//        return credential.idToken      // → Token bạn gửi lên backend hoặc để lưu user
//    }
//
//    /**
//     * Step 2 – Lấy Access Token ngắn hạn để gọi Google Drive REST API
//     */
//    suspend fun getDriveAccessToken(activity: Activity): String {
//
//        val request = AuthorizationRequest.builder()
//            .setRequestedScopes(listOf(Scope("https://www.googleapis.com/auth/drive.file")))
//            .build()
//
//        val authorizationClient = Identity.getAuthorizationClient(activity)
//
//        val result = authorizationClient
//            .authorize(request)
//            .await()
//
//        return result.accessToken
//            ?: throw Exception("Failed to obtain Drive access token.")
//    }
//}

/*
 * Copyright (C) 2026 marcinlowercase
 * ...
 */
package marcinlowercase.a.core.manager

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.IntentSenderRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.AuthorizationResult
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.Scope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.api.client.http.ByteArrayContent
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import marcinlowercase.a.R
import java.io.ByteArrayOutputStream

class DriveSyncManager(private val context: Context) {

    private val credentialManager = CredentialManager.create(context)
    private val authClient = Identity.getAuthorizationClient(context)
    private val prefs = context.getSharedPreferences("DriveAuthPrefs", Context.MODE_PRIVATE)

    private val driveScope = Scope(DriveScopes.DRIVE_APPDATA)
    private val syncFileName = "browser_sync_data.json"

    // ==========================================
    // 1. CREDENTIAL MANAGER SIGN IN
    // ==========================================
    suspend fun signInWithGoogle(activity: Activity): String? {
        return withContext(Dispatchers.Main) {
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(context.getString(R.string.default_web_client_id))
                .setAutoSelectEnabled(true)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            try {
                val result = credentialManager.getCredential(
                    request = request,
                    context = activity
                )
                val credential = result.credential
                if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    val email = googleIdTokenCredential.id
                    saveEmail(email)
                    email
                } else {
                    null
                }
            } catch (e: GetCredentialException) {
                android.util.Log.e("DriveSync", "Credential Manager sign-in failed", e)
                null
            }
        }
    }

    // ==========================================
    // 2. GOOGLE DRIVE AUTHORIZATION (OAuth Token)
    // ==========================================
    fun getDriveAccessToken(
        onResolutionRequired: (IntentSenderRequest) -> Unit,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val authRequest = AuthorizationRequest.builder()
            .setRequestedScopes(listOf(driveScope))
            .build()

        authClient.authorize(authRequest)
            .addOnSuccessListener { result ->
                if (result.hasResolution()) {
                    val pendingIntent = result.pendingIntent
                    if (pendingIntent != null) {
                        onResolutionRequired(IntentSenderRequest.Builder(pendingIntent.intentSender).build())
                    } else {
                        onFailure(Exception("PendingIntent is null"))
                    }
                } else {
                    val token = result.accessToken
                    if (token != null) {
                        saveAccessToken(token)
                        onSuccess(token)
                    } else {
                        onFailure(Exception("Access token is null"))
                    }
                }
            }
            .addOnFailureListener { e -> onFailure(e) }
    }

    suspend fun handleDriveAuthResult(data: Intent?): String? {
        return withContext(Dispatchers.IO) {
            try {
                val result: AuthorizationResult = authClient.getAuthorizationResultFromIntent(data)
                val token = result.accessToken ?: return@withContext null
                saveAccessToken(token)
                token
            } catch (e: Exception) {
                android.util.Log.e("DriveSync", "Failed to parse authorization result", e)
                null
            }
        }
    }

    // ==========================================
    // 3. GOOGLE DRIVE FILE CRUD OPERATIONS
    // ==========================================
    private fun getDriveService(accessToken: String): Drive {
        return Drive.Builder(
            NetHttpTransport(),
            GsonFactory.getDefaultInstance()
        ) { request ->
            request.headers.authorization = "Bearer $accessToken"
        }.setApplicationName("browser").build()
    }

    suspend fun uploadToDrive(accessToken: String, jsonString: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val service = getDriveService(accessToken)
            val fileList = service.files().list()
                .setSpaces("appDataFolder")
                .setQ("name = '$syncFileName' and trashed = false")
                .setFields("files(id, name)")
                .execute()

            val existingFileId = fileList.files?.firstOrNull()?.id
            val mediaContent = ByteArrayContent.fromString("application/json", jsonString)

            if (existingFileId != null) {
                service.files().update(existingFileId, null, mediaContent).execute()
            } else {
                val fileMetadata = com.google.api.services.drive.model.File().apply {
                    name = syncFileName
                    parents = listOf("appDataFolder")
                }
                service.files().create(fileMetadata, mediaContent).execute()
            }
            true
        } catch (e: Exception) {
            android.util.Log.e("DriveSync", "Failed to upload to Drive", e)
            false
        }
    }

    suspend fun downloadFromDrive(accessToken: String): String? = withContext(Dispatchers.IO) {
        try {
            val service = getDriveService(accessToken)
            val fileList = service.files().list()
                .setSpaces("appDataFolder")
                .setQ("name = '$syncFileName' and trashed = false")
                .setFields("files(id, name)")
                .execute()

            val fileId = fileList.files?.firstOrNull()?.id ?: return@withContext null
            val outputStream = ByteArrayOutputStream()
            service.files().get(fileId).executeMediaAndDownloadTo(outputStream)
            outputStream.toString("UTF-8")
        } catch (e: Exception) {
            android.util.Log.e("DriveSync", "Failed to download from Drive", e)
            null
        }
    }

    suspend fun deleteFromDrive(accessToken: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val service = getDriveService(accessToken)
            val fileList = service.files().list()
                .setSpaces("appDataFolder")
                .setQ("name = '$syncFileName' and trashed = false")
                .setFields("files(id, name)")
                .execute()

            val fileId = fileList.files?.firstOrNull()?.id ?: return@withContext true
            service.files().delete(fileId).execute()
            true
        } catch (e: Exception) {
            android.util.Log.e("DriveSync", "Failed to delete file from Drive", e)
            false
        }
    }

    // ==========================================
    // 4. STORAGE & STATE
    // ==========================================
    fun getSavedAccessToken(): String? = prefs.getString("access_token", null)
    fun getSavedEmail(): String = prefs.getString("user_email", "") ?: ""

    fun saveAccessToken(token: String) = prefs.edit().putString("access_token", token).apply()
    fun saveEmail(email: String) = prefs.edit().putString("user_email", email).apply()

    fun signOut(onComplete: () -> Unit) {
        prefs.edit().clear().apply()
        onComplete()
    }
}
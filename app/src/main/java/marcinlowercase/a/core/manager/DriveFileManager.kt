package marcinlowercase.a.core.manager

import com.google.api.client.http.ByteArrayContent
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

class DriveFileManager(private val syncManager: DriveSyncManager) {

    private fun getDriveService(accessToken: String): Drive {
        return Drive.Builder(
            NetHttpTransport(),
            GsonFactory.getDefaultInstance()
        ) { request ->
            request.headers.authorization = "Bearer $accessToken"
        }.setApplicationName("oo1_browser").build()
    }

    // Ensures: Google Drive -> oo1_studio -> {appId}
    private suspend fun getOrCreateAppFolderId(service: Drive, appId: String): String = withContext(Dispatchers.IO) {
        val rootFolderName = "oo1_studio"

        // 1. Find or create root 'oo1_studio' folder
        val rootQuery = service.files().list()
            .setQ("name = '$rootFolderName' and mimeType = 'application/vnd.google-apps.folder' and trashed = false and 'root' in parents")
            .setFields("files(id)")
            .execute()

        val rootFolderId = rootQuery.files?.firstOrNull()?.id ?: run {
            val folderMetadata = File().apply {
                name = rootFolderName
                mimeType = "application/vnd.google-apps.folder"
                parents = listOf("root")
            }
            service.files().create(folderMetadata).setFields("id").execute().id
        }

        // 2. Find or create subfolder for this specific appId
        val appQuery = service.files().list()
            .setQ("name = '$appId' and mimeType = 'application/vnd.google-apps.folder' and trashed = false and '$rootFolderId' in parents")
            .setFields("files(id)")
            .execute()

        appQuery.files?.firstOrNull()?.id ?: run {
            val appFolderMetadata = File().apply {
                name = appId
                mimeType = "application/vnd.google-apps.folder"
                parents = listOf(rootFolderId)
            }
            service.files().create(appFolderMetadata).setFields("id").execute().id
        }
    }

    // ==========================================
    // PUBLIC API FOR WEB APPS
    // ==========================================

    suspend fun saveText(
        accessToken: String,
        appId: String,
        fileName: String,
        content: String,
        mimeType: String = "application/json"
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val service = getDriveService(accessToken)
            val folderId = getOrCreateAppFolderId(service, appId)

            // Check if file already exists in the app folder
            val existing = service.files().list()
                .setQ("name = '$fileName' and '$folderId' in parents and trashed = false")
                .setFields("files(id)")
                .execute()
                .files?.firstOrNull()

            val mediaContent = ByteArrayContent.fromString(mimeType, content)

            if (existing != null) {
                // Overwrite existing file
                service.files().update(existing.id, null, mediaContent).execute()
            } else {
                // Create new file
                val metadata = File().apply {
                    name = fileName
                    parents = listOf(folderId)
                }
                service.files().create(metadata, mediaContent).execute()
            }
            true
        } catch (e: Exception) {
            android.util.Log.e("DriveFile", "Failed to save file: $fileName", e)
            false
        }
    }

    suspend fun readText(
        accessToken: String,
        appId: String,
        fileName: String
    ): String? = withContext(Dispatchers.IO) {
        try {
            val service = getDriveService(accessToken)
            val folderId = getOrCreateAppFolderId(service, appId)

            val file = service.files().list()
                .setQ("name = '$fileName' and '$folderId' in parents and trashed = false")
                .setFields("files(id)")
                .execute()
                .files?.firstOrNull() ?: return@withContext null

            val outputStream = ByteArrayOutputStream()
            service.files().get(file.id).executeMediaAndDownloadTo(outputStream)
            outputStream.toString("UTF-8")
        } catch (e: Exception) {
            android.util.Log.e("DriveFile", "Failed to read file: $fileName", e)
            null
        }
    }
}
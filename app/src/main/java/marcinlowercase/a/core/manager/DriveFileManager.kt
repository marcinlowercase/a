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

    /* in-memory cache to prevent race-condition folder duplicates */
    private val folderIdCache = java.util.concurrent.ConcurrentHashMap<String, String>()

    /* read-only search: returns folder id or null if missing */
    private suspend fun findAppFolderId(service: Drive, appId: String): String? = withContext(Dispatchers.IO) {
        folderIdCache[appId]?.let { return@withContext it }

        val rootFolderName = "the_browser_of_oo1_studio"

        val rootFolderId = folderIdCache["_root_"] ?: service.files().list()
            .setQ("name = '$rootFolderName' and mimeType = 'application/vnd.google-apps.folder' and trashed = false and 'root' in parents")
            .setFields("files(id)")
            .execute()
            .files?.firstOrNull()?.id

        if (rootFolderId == null) return@withContext null
        folderIdCache["_root_"] = rootFolderId

        val appFolders = service.files().list()
            .setQ("name = '$appId' and mimeType = 'application/vnd.google-apps.folder' and trashed = false and '$rootFolderId' in parents")
            .setFields("files(id)")
            .execute()
            .files

        val primaryFolderId = appFolders?.firstOrNull()?.id
        if (primaryFolderId != null) {
            folderIdCache[appId] = primaryFolderId

            /* purge any accidental duplicate folders created earlier */
            if (appFolders.size > 1) {
                for (i in 1 until appFolders.size) {
                    try { service.files().delete(appFolders[i].id).execute() } catch (_: Exception) {}
                }
            }
        }
        primaryFolderId
    }

    /* write helper: guarantees only one folder per app */
    private suspend fun getOrCreateAppFolderId(service: Drive, appId: String): String = withContext(Dispatchers.IO) {
        val existing = findAppFolderId(service, appId)
        if (existing != null) return@withContext existing

        val rootFolderName = "the_browser_of_oo1_studio"
        val rootFolderId = folderIdCache["_root_"] ?: run {
            val foundRoot = service.files().list()
                .setQ("name = '$rootFolderName' and mimeType = 'application/vnd.google-apps.folder' and trashed = false and 'root' in parents")
                .setFields("files(id)")
                .execute()
                .files?.firstOrNull()?.id

            val finalRoot = foundRoot ?: run {
                val folderMetadata = File().apply {
                    name = rootFolderName
                    mimeType = "application/vnd.google-apps.folder"
                    parents = listOf("root")
                }
                service.files().create(folderMetadata).setFields("id").execute().id
            }
            folderIdCache["_root_"] = finalRoot
            finalRoot
        }

        val appFolderMetadata = File().apply {
            name = appId
            mimeType = "application/vnd.google-apps.folder"
            parents = listOf(rootFolderId)
        }
        val createdId = service.files().create(appFolderMetadata).setFields("id").execute().id
        folderIdCache[appId] = createdId
        createdId
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

            /* fetch all matching files to deduplicate and update */
            val existingFiles = service.files().list()
                .setQ("name = '$fileName' and '$folderId' in parents and trashed = false")
                .setFields("files(id)")
                .execute()
                .files

            val mediaContent = ByteArrayContent.fromString(mimeType, content)

            if (!existingFiles.isNullOrEmpty()) {
                val primaryFile = existingFiles.first()
                service.files().update(primaryFile.id, null, mediaContent).execute()

                /* purge any duplicate files created by previous runs */
                if (existingFiles.size > 1) {
                    for (i in 1 until existingFiles.size) {
                        try { service.files().delete(existingFiles[i].id).execute() } catch (_: Exception) {}
                    }
                }
            } else {
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
            val folderId = findAppFolderId(service, appId) ?: return@withContext null

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

    suspend fun listFiles(
        accessToken: String,
        appId: String
    ): List<Map<String, Any>> = withContext(Dispatchers.IO) {
        try {
            val service = getDriveService(accessToken)
            val folderId = findAppFolderId(service, appId) ?: return@withContext emptyList()

            val fileList = service.files().list()
                .setQ("'$folderId' in parents and trashed = false")
                .setFields("files(id, name, size, modifiedTime, mimeType)")
                .execute()

            fileList.files?.map { file ->
                mapOf(
                    "id" to (file.id ?: ""),
                    "name" to (file.name ?: ""),
                    "size" to (file.size ?: 0L),
                    "modifiedTime" to (file.modifiedTime?.value ?: 0L),
                    "mimeType" to (file.mimeType ?: "")
                )
            } ?: emptyList()
        } catch (e: Exception) {
            android.util.Log.e("DriveFile", "Failed to list files for $appId", e)
            emptyList()
        }
    }

    suspend fun deleteFile(
        accessToken: String,
        appId: String,
        fileName: String
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val service = getDriveService(accessToken)
            val folderId = findAppFolderId(service, appId) ?: return@withContext false

            val file = service.files().list()
                .setQ("name = '$fileName' and '$folderId' in parents and trashed = false")
                .setFields("files(id)")
                .execute()
                .files?.firstOrNull() ?: return@withContext false

            service.files().delete(file.id).execute()
            true
        } catch (e: Exception) {
            android.util.Log.e("DriveFile", "Failed to delete file: $fileName", e)
            false
        }
    }
}
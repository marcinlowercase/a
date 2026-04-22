package marcinlowercase.a.core.function

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Base64
import android.util.Log

fun saveBase64ToStorage(
    context: Context,
    filename: String,
    base64Data: String,
    mimeType: String,
    folderType: String
): Boolean {
    return try {
        // Strip out the URI prefix if the website accidentally included it (e.g., "data:image/png;base64,...")
        val cleanBase64 = if (base64Data.contains(",")) {
            base64Data.substringAfter(",")
        } else {
            base64Data
        }
        val bytes = Base64.decode(cleanBase64, Base64.DEFAULT)

        val resolver = context.contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
            put(MediaStore.MediaColumns.MIME_TYPE, mimeType)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val isImage = folderType.equals("PICTURES", ignoreCase = true) || folderType.equals("DCIM", ignoreCase = true)
                val envFolder = if (isImage) Environment.DIRECTORY_PICTURES else Environment.DIRECTORY_DOWNLOADS

                put(MediaStore.MediaColumns.RELATIVE_PATH, envFolder)
                put(MediaStore.MediaColumns.IS_PENDING, 1) // Lock file while writing
            }
        }

        val isImage = folderType.equals("PICTURES", ignoreCase = true) || folderType.equals("DCIM", ignoreCase = true)
        val collection = if (isImage) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            } else {
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            } else {
                // Pre-Android 10 fallback for downloads requires WRITE_EXTERNAL_STORAGE permission
                MediaStore.Files.getContentUri("external")
            }
        }

        val uri = resolver.insert(collection, contentValues)
        if (uri != null) {
            resolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(bytes)
                outputStream.flush()
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.clear()
                contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0) // Unlock file
                resolver.update(uri, contentValues, null, null)
            }
            true
        } else {
            false
        }
    } catch (e: Exception) {
        Log.e("GeckoExt", "Failed to save file to storage", e)
        false
    }
}
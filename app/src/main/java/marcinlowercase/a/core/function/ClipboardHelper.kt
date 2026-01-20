package marcinlowercase.a.core.function

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Base64
import android.widget.Toast

import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.URL

suspend fun copyImageToClipboard(context: Context, imageUrl: String) {
    withContext(Dispatchers.IO) {
        try {
            val imagesDir = File(context.cacheDir, "images")
            if (!imagesDir.exists()) imagesDir.mkdirs()

            // Note: We try to guess extension, but PNG is safe for clipboard usually
            val tempFile = File(imagesDir, "clipboard_temp.png")

            if (imageUrl.startsWith("data:")) {
                val base64Data = imageUrl.substringAfter(",")
                val decodedBytes = Base64.decode(base64Data, Base64.DEFAULT)
                FileOutputStream(tempFile).use { it.write(decodedBytes) }
            } else {
                // Handle HTTP download with User-Agent to prevent 403 Forbidden
                val urlObj = URL(imageUrl)
                val connection = urlObj.openConnection() as java.net.HttpURLConnection
                connection.requestMethod = "GET"
                connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 10; Mobile; rv:109.0) Gecko/109.0 Firefox/110.0")
                connection.connect()

                if (connection.responseCode != 200) {
                    throw Exception("HTTP Error ${connection.responseCode}")
                }

                connection.inputStream.use { input ->
                    FileOutputStream(tempFile).use { output ->
                        input.copyTo(output)
                    }
                }
            }

            val authority = "${context.packageName}.fileprovider"
            val contentUri = FileProvider.getUriForFile(context, authority, tempFile)

            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newUri(context.contentResolver, "Image", contentUri)
            clipboard.setPrimaryClip(clip)

            withContext(Dispatchers.Main) {
//                Toast.makeText(context, "Image copied!", Toast.LENGTH_SHORT).show()
            }

        } catch (_: Exception) {
            withContext(Dispatchers.Main) {
                // Show the actual error message
//                Toast.makeText(context, "Failed: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }
}
//
//
//
//suspend fun downloadImageToContentUri(context: Context, imageUrl: String): Uri? {
//    return withContext(Dispatchers.IO) {
//        try {
//            // 1. Setup Cache Dir
//            val imagesDir = File(context.cacheDir, "images")
//            if (!imagesDir.exists()) imagesDir.mkdirs()
//
//            // 2. Create File (We overwrite 'share_temp.png' to save space)
//            val tempFile = File(imagesDir, "share_temp.png")
//
//            // 3. Download
//            if (imageUrl.startsWith("data:")) {
//                val base64Data = imageUrl.substringAfter(",")
//                val decodedBytes = Base64.decode(base64Data, Base64.DEFAULT)
//                FileOutputStream(tempFile).use { it.write(decodedBytes) }
//            } else {
//                val urlObj = URL(imageUrl)
//                val connection = urlObj.openConnection() as java.net.HttpURLConnection
//                // Fake User-Agent to avoid 403 Forbidden
//                connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Android 14; Mobile; rv:120.0) Gecko/120.0 Firefox/120.0")
//                connection.connect()
//
//                if (connection.responseCode != 200) throw Exception("HTTP ${connection.responseCode}")
//
//                connection.inputStream.use { input ->
//                    FileOutputStream(tempFile).use { output ->
//                        input.copyTo(output)
//                    }
//                }
//            }
//
//            // 4. Return the URI
//            // Ensure this authority matches your AndroidManifest.xml
//            val authority = "${context.packageName}.fileprovider"
//            FileProvider.getUriForFile(context, authority, tempFile)
//
//        } catch (e: Exception) {
//            e.printStackTrace()
//            null
//        }
//    }
//}
//
//
//suspend fun shareImage(context: Context, imageUrl: String) {
//    // 1. Download the image
//    val uri = downloadImageToContentUri(context, imageUrl)
//
//    if (uri != null) {
//        withContext(Dispatchers.Main) {
//            try {
//                // 2. Build the Share Intent
//                val intent = Intent(Intent.ACTION_SEND).apply {
//                    type = "image/png" // We saved it as .png, so be specific
//                    putExtra(Intent.EXTRA_STREAM, uri)
//
//                    // CRITICAL for Android 10+: Attach ClipData
//                    // Without this, the receiving app might not get read permissions
//                    clipData = ClipData.newRawUri("Image", uri)
//
//                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
//                }
//
//                // 3. Create Chooser
//                val chooserIntent = Intent.createChooser(intent, "Share Image")
//
//                // 4. Handle Non-Activity Contexts
//                // If 'context' is not an Activity, we must add this flag or it will crash
//                if (context !is Activity) {
//                    chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//                }
//
//                context.startActivity(chooserIntent)
//
//            } catch (e: Exception) {
//                e.printStackTrace()
//                Toast.makeText(context, "Error launching share: ${e.message}", Toast.LENGTH_SHORT).show()
//            }
//        }
//    } else {
//        withContext(Dispatchers.Main) {
//            Toast.makeText(context, "Failed to download image for sharing", Toast.LENGTH_SHORT).show()
//        }
//    }
//}
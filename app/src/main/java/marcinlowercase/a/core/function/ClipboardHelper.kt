package marcinlowercase.a.core.function

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.util.Base64

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

        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                // Show the actual error message
//                Toast.makeText(context, "Failed: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }
}
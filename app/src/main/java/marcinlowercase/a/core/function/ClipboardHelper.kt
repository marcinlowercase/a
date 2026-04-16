/*
 * Copyright (C) 2026 marcinlowercase
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; version 2 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
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
            }

        } catch (_: Exception) {
            withContext(Dispatchers.Main) {}
        }
    }
}
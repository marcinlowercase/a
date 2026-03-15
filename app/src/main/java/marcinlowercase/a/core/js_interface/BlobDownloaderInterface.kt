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
package marcinlowercase.a.core.js_interface

import android.webkit.JavascriptInterface

class BlobDownloaderInterface(
    // It now takes a callback function in its constructor
    private val onBlobDataReceived: (base64Data: String, filename: String, mimeType: String) -> Unit
) {
    @Suppress("unused")
    @JavascriptInterface
    fun downloadBase64File(base64Data: String, filename: String, mimeType: String) {
        // Instead of saving the file, it just calls the callback with the data.
        onBlobDataReceived(base64Data, filename, mimeType)
    }
}
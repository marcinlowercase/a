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
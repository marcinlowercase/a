package marcinlowercase.a.core.js_interface

import android.util.Log
import android.webkit.JavascriptInterface

class FaviconJavascriptInterface(
    private val onFaviconUrlFound: (String) -> Unit
) {
    @Suppress("unused")
    @JavascriptInterface
    fun passFaviconUrl(absoluteIconUrl: String?) {
        Log.e("Favicon",  "Entered Interface")

        Log.d("Favicon", "passFaviconUrl called")
        if (absoluteIconUrl != null) {
            Log.d("Favicon", "Received absolute icon URL from JS: $absoluteIconUrl")
            onFaviconUrlFound(absoluteIconUrl)
        } else {
            // Here you can decide on a fallback. Maybe do nothing and let Coil show a placeholder.
            // Or you can try the /favicon.ico, but it's less reliable.
            Log.d("Favicon", "No icon URL found in page HTML.")
        }
    }
}
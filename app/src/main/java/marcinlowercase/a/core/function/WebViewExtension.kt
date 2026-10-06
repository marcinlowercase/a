package marcinlowercase.a.core.function

import android.content.Context
import android.net.Uri
import org.mozilla.geckoview.GeckoSession

fun webViewLoad(session: GeckoSession?, url: String, context: Context? = null) {
    if (session == null) return

    // Zero-disk in-memory load for local content:// streams from Files app
    if (url.startsWith("content://") && context != null) {
        try {
            val contentUri = Uri.parse(url)
            val bytes = context.contentResolver.openInputStream(contentUri)?.use { it.readBytes() }
            if (bytes != null) {
                session.load(
                    GeckoSession.Loader()
                        .data(bytes, "text/html")
                        .flags(GeckoSession.LOAD_FLAGS_NONE)
                )
                return
            }
        } catch (_: Exception) {
            // Fall back to standard URI load if stream fails
        }
    }

    session.load(
        GeckoSession.Loader()
            .uri(url)
            .flags(GeckoSession.LOAD_FLAGS_NONE)
    )
}
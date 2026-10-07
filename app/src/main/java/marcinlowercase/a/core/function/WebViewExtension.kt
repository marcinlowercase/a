package marcinlowercase.a.core.function

import android.content.Context
import android.net.Uri
import org.mozilla.geckoview.GeckoSession

fun webViewLoad(session: GeckoSession?, url: String, context: Context? = null) {
    if (session == null) return

    val trimmed = url.trim()

    // 1. Raw HTML Code Detection: Load in-memory without disk or network
    val isRawHtml = trimmed.startsWith("<!DOCTYPE", ignoreCase = true) ||
            trimmed.startsWith("<html", ignoreCase = true) ||
            (trimmed.startsWith("<") && trimmed.contains("</html>", ignoreCase = true))

    if (isRawHtml) {
        session.load(
            GeckoSession.Loader()
                .data(trimmed.toByteArray(Charsets.UTF_8), "text/html") // Encodes safely without '#' truncation
                .flags(GeckoSession.LOAD_FLAGS_NONE)
        )
        return
    }

    // 2. Android content:// streams from Files app
    if (trimmed.startsWith("content://") && context != null) {
        try {
            val contentUri = Uri.parse(trimmed)
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
            // Fallback to URI
        }
    }

    // 3. Standard HTTP/HTTPS/about URLs
    session.load(
        GeckoSession.Loader()
            .uri(trimmed)
            .flags(GeckoSession.LOAD_FLAGS_NONE)
    )
}
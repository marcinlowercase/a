package marcinlowercase.a.core.function

import android.content.Context
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import marcinlowercase.a.core.manager.DriveFileManager
import marcinlowercase.a.core.manager.DriveSyncManager
import org.mozilla.geckoview.GeckoSession
import java.io.File

fun webViewLoad(session: GeckoSession?, url: String, context: Context? = null) {
    Log.i("webViewLoad", "url: $url")
    if (session == null) return

    val trimmed = url.trim()

    // -------------------------------------------------------------
    // 1. VIRTUAL LOCAL SCHEME: local://<app_id>
    // -------------------------------------------------------------
    if (trimmed.startsWith("local://", ignoreCase = true) && context != null) {
        val appId = trimmed.removePrefix("local://").trim('/').replace("/", "_")
        val appsDir = File(context.filesDir, "apps")
        val localFile = File(appsDir, "$appId.html")

        // FAST PATH: Device internal storage cache hit (sub-5ms)
        if (localFile.exists()) {
            val htmlBytes = localFile.readBytes()
            session.load(
                GeckoSession.Loader()
                    .data(htmlBytes, "text/html")
                    .flags(GeckoSession.LOAD_FLAGS_NONE)
            )
            return
        }

        // FALLBACK: Query Google Drive via DriveFileManager (Network Path)
        MainScope().launch {
            val driveSyncManager = DriveSyncManager(context)
            val driveFileManager = DriveFileManager(driveSyncManager)
            val token = withContext(Dispatchers.IO) { driveSyncManager.getFreshAccessToken() }

            if (!token.isNullOrBlank()) {
                val cloudCode = withContext(Dispatchers.IO) {
                    driveFileManager.readText(token, appId, "index.html")
                }

                if (!cloudCode.isNullOrBlank() && !cloudCode.startsWith("ERROR")) {
                    // Cache to on-device storage for future zero-latency boots
                    withContext(Dispatchers.IO) {
                        if (!appsDir.exists()) appsDir.mkdirs()
                        localFile.writeText(cloudCode)
                    }

                    // Render in-memory
                    session.load(
                        GeckoSession.Loader()
                            .data(cloudCode.toByteArray(Charsets.UTF_8), "text/html")
                            .flags(GeckoSession.LOAD_FLAGS_NONE)
                    )
                    return@launch
                }
            }

            // NOT FOUND IN LOCAL DISK OR CLOUD
            val errorHtml = """
                <!DOCTYPE html>
                <html>
                <head>
                  <meta name="viewport" content="width=device-width,initial-scale=1">
                  <style>
                    body { background: #09090b; color: #f4f4f5; font-family: system-ui; display: flex; flex-direction: column; align-items: center; justify-content: center; height: 100vh; margin: 0; text-align: center; padding: 24px; box-sizing: border-box; }
                    h2 { color: #ef4444; margin: 0 0 8px; }
                    p { color: #a1a1aa; font-size: 14px; margin: 0; }
                  </style>
                </head>
                <body>
                  <h2>App Not Found</h2>
                  <p>Could not locate "$appId" locally or in your Google Drive.</p>
                </body>
                </html>
            """.trimIndent()

            session.load(
                GeckoSession.Loader()
                    .data(errorHtml.toByteArray(Charsets.UTF_8), "text/html")
                    .flags(GeckoSession.LOAD_FLAGS_NONE)
            )
        }
        return
    }

    // -------------------------------------------------------------
    // 2. Raw HTML Strings
    // -------------------------------------------------------------
    val isRawHtml = trimmed.startsWith("<!DOCTYPE", ignoreCase = true) ||
            trimmed.startsWith("<html", ignoreCase = true) ||
            (trimmed.startsWith("<") && trimmed.contains("</html>", ignoreCase = true))

    if (isRawHtml) {
        session.load(
            GeckoSession.Loader()
                .data(trimmed.toByteArray(Charsets.UTF_8), "text/html")
                .flags(GeckoSession.LOAD_FLAGS_NONE)
        )
        return
    }

    // -------------------------------------------------------------
    // 3. Android content:// streams
    // -------------------------------------------------------------
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
        } catch (_: Exception) {}
    }

    // -------------------------------------------------------------
    // 4. Standard HTTP/HTTPS URIs
    // -------------------------------------------------------------
    session.load(
        GeckoSession.Loader()
            .uri(trimmed)
            .flags(GeckoSession.LOAD_FLAGS_NONE)
    )
}
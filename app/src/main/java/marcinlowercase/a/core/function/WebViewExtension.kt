package marcinlowercase.a.core.function

import android.content.Context
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import marcinlowercase.a.CustomApplication
import marcinlowercase.a.core.manager.DriveFileManager
import marcinlowercase.a.core.manager.DriveSyncManager
import marcinlowercase.a.core.server.LocalAppServer
import org.mozilla.geckoview.GeckoSession
import java.io.File

fun webViewLoad(session: GeckoSession?, url: String, context: Context? = null) {
    Log.i("webViewLoad", "url: $url")
    if (session == null) return

    val trimmed = url.trim()
    val server = (context?.applicationContext as? CustomApplication)?.localAppServer

    // -------------------------------------------------------------
    // 1. RAW HTML STRING: Store in draft buffer and route to localhost
    // -------------------------------------------------------------
    val isRawHtml = trimmed.startsWith("<!DOCTYPE", ignoreCase = true) ||
            trimmed.startsWith("<html", ignoreCase = true) ||
            (trimmed.startsWith("<") && trimmed.contains("</html>", ignoreCase = true))

    if (isRawHtml && server != null) {
        server.currentDraftHtml = trimmed
        val internalUrl = LocalAppServer.toInternalUrl("local://draft/index.html")
        session.load(
            GeckoSession.Loader()
                .uri(internalUrl)
                .flags(GeckoSession.LOAD_FLAGS_NONE)
        )
        return
    }

    // -------------------------------------------------------------
    // 2. ANDROID content:// STREAMS: Read into draft buffer and route to localhost
    // -------------------------------------------------------------
    if (trimmed.startsWith("content://", ignoreCase = true) && context != null && server != null) {
        try {
            val contentUri = Uri.parse(trimmed)
            val htmlContent = context.contentResolver.openInputStream(contentUri)?.use {
                it.bufferedReader().readText()
            }
            if (!htmlContent.isNullOrBlank()) {
                server.currentDraftHtml = htmlContent
                val internalUrl = LocalAppServer.toInternalUrl("local://draft/index.html")
                session.load(
                    GeckoSession.Loader()
                        .uri(internalUrl)
                        .flags(GeckoSession.LOAD_FLAGS_NONE)
                )
                return
            }
        } catch (e: Exception) {
            Log.e("webViewLoad", "Failed to stream content URI", e)
        }
    }

    // -------------------------------------------------------------
    // 3. VIRTUAL LOCAL SCHEME: local://...
    // -------------------------------------------------------------
    if (trimmed.startsWith(LocalAppServer.VIRTUAL_SCHEME, ignoreCase = true) && context != null && server != null) {
        val virtualUrl = if (trimmed.endsWith("/index.html") || trimmed.endsWith("/index.htm")) {
            trimmed
        } else {
            "${trimmed.trimEnd('/')}/index.html"
        }

        // A. Draft Route
        if (virtualUrl.startsWith("local://draft/", ignoreCase = true)) {
            val internalUrl = LocalAppServer.toInternalUrl(virtualUrl)
            session.load(
                GeckoSession.Loader()
                    .uri(internalUrl)
                    .flags(GeckoSession.LOAD_FLAGS_NONE)
            )
            return
        }

        // B. Installed App Route: local://apps/<app_id>/index.html
        val appId = virtualUrl.removePrefix("local://apps/")
            .removeSuffix("/index.html")
            .removeSuffix("/index.htm")
            .trim('/')
            .replace("/", "_")

        val appsDir = File(context.filesDir, "apps")
        val localFile = File(appsDir, "$appId.html")
        val internalUrl = LocalAppServer.toInternalUrl(virtualUrl)

        // Fast path: cached on disk
        if (localFile.exists()) {
            session.load(
                GeckoSession.Loader()
                    .uri(internalUrl)
                    .flags(GeckoSession.LOAD_FLAGS_NONE)
            )
            return
        }

        // Fallback: fetch from Google Drive if missing (e.g. Device B)
        MainScope().launch {
            val driveSyncManager = DriveSyncManager(context)
            val driveFileManager = DriveFileManager(driveSyncManager)
            val token = withContext(Dispatchers.IO) { driveSyncManager.getFreshAccessToken() }

            if (!token.isNullOrBlank()) {
                val cloudCode = withContext(Dispatchers.IO) {
                    driveFileManager.readText(token, appId, "index.html")
                }

                if (!cloudCode.isNullOrBlank() && !cloudCode.startsWith("ERROR")) {
                    withContext(Dispatchers.IO) {
                        if (!appsDir.exists()) appsDir.mkdirs()
                        localFile.writeText(cloudCode)
                    }

                    session.load(
                        GeckoSession.Loader()
                            .uri(internalUrl)
                            .flags(GeckoSession.LOAD_FLAGS_NONE)
                    )
                    return@launch
                }
            }

            // Not found locally or in Drive: show 404
            session.load(
                GeckoSession.Loader()
                    .uri(internalUrl)
                    .flags(GeckoSession.LOAD_FLAGS_NONE)
            )
        }
        return
    }

    // -------------------------------------------------------------
    // 4. Standard Remote Web URIs (http/https/about)
    // -------------------------------------------------------------
    session.load(
        GeckoSession.Loader()
            .uri(trimmed)
            .flags(GeckoSession.LOAD_FLAGS_NONE)
    )
}
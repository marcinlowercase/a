package marcinlowercase.a.core.server

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket

class LocalAppServer(private val context: Context) {

    private var serverSocket: ServerSocket? = null
    private var serverJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO)

    // In-memory buffer for unpinned draft code
    @Volatile
    var currentDraftHtml: String = ""

    companion object {
        const val PORT = 12321
        private const val LOCALHOST_PREFIX = "http://127.0.0.1:$PORT/"
        const val VIRTUAL_SCHEME = "local://"

        /**
         * Translates virtual "local://" URLs to internal loopback "http://127.0.0.1:12321/".
         * Used by webViewLoad before passing the URL to GeckoView.
         */
        fun toInternalUrl(url: String): String {
            val trimmed = url.trim()
            return if (trimmed.startsWith(VIRTUAL_SCHEME, ignoreCase = true)) {
                val path = trimmed.removePrefix(VIRTUAL_SCHEME).removePrefix("/")
                LOCALHOST_PREFIX + path
            } else {
                trimmed
            }
        }

        /**
         * Translates internal loopback "http://127.0.0.1:12321/" back to virtual "local://".
         * Used by onLocationChangeFun, tabs, and clipboard.
         */
        fun toVirtualUrl(url: String): String {
            val trimmed = url.trim()
            return if (trimmed.startsWith(LOCALHOST_PREFIX, ignoreCase = true)) {
                val path = trimmed.removePrefix(LOCALHOST_PREFIX)
                VIRTUAL_SCHEME + path
            } else {
                trimmed
            }
        }

        /**
         * Formats URL for clean address bar display:
         * "local://draft/index.html" -> "local://draft"
         * "local://apps/inventory_checker/index.html" -> "local://apps/inventory_checker"
         */
        fun formatForDisplay(url: String): String {
            val virtual = toVirtualUrl(url)
            return if (virtual.startsWith(VIRTUAL_SCHEME, ignoreCase = true)) {
                virtual.removeSuffix("/index.html")
                    .removeSuffix("/index.htm")
                    .removeSuffix("/")
                    .lowercase()
            } else {
                virtual
            }
        }
    }

    val baseUrl: String
        get() = "http://127.0.0.1:$PORT"

    fun start() {
        if (serverSocket != null && !serverSocket!!.isClosed) return

        serverJob = scope.launch {
            try {
                serverSocket = ServerSocket().apply {
                    reuseAddress = true
                    bind(InetSocketAddress("127.0.0.1", PORT), 50)
                }
                Log.i("LocalAppServer", "Loopback server bound on $baseUrl")

                while (isActive && !serverSocket!!.isClosed) {
                    val clientSocket = serverSocket!!.accept()
                    launch { handleClient(clientSocket) }
                }
            } catch (e: Exception) {
                if (isActive) Log.e("LocalAppServer", "Server exception", e)
            }
        }
    }

    fun stop() {
        try {
            serverJob?.cancel()
            serverSocket?.close()
            serverSocket = null
            Log.i("LocalAppServer", "Server stopped")
        } catch (_: Exception) {}
    }

    private fun handleClient(socket: Socket) {
        try {
            socket.use { s ->
                val reader = BufferedReader(InputStreamReader(s.getInputStream()))
                val requestLine = reader.readLine() ?: return
                val parts = requestLine.split(" ")
                if (parts.size < 2 || parts[0] != "GET") {
                    send404(s.getOutputStream())
                    return
                }

                val rawPath = parts[1].substringBefore("?")
                val output = s.getOutputStream()

                when {
                    // 1. DRAFT ROUTE: /draft/index.html
                    rawPath == "/draft/index.html" || rawPath == "/draft" || rawPath == "/draft/" -> {
                        sendResponse(output, "text/html; charset=utf-8", currentDraftHtml.toByteArray(Charsets.UTF_8))
                    }

                    // 2. PINNED APP CODE: /apps/<app_id>/index.html
                    rawPath.startsWith("/apps/") && (rawPath.endsWith("/index.html") || rawPath.endsWith("/index.htm")) -> {
                        val appId = rawPath.removePrefix("/apps/")
                            .removeSuffix("/index.html")
                            .removeSuffix("/index.htm")
                            .trim('/')

                        val appsDir = File(context.filesDir, "apps")
                        val file = File(appsDir, "$appId.html")
                        val nestedFile = File(File(appsDir, appId), "index.html")

                        val fileToServe = if (file.exists()) file else if (nestedFile.exists()) nestedFile else null
                        if (fileToServe != null) {
                            sendResponse(output, "text/html; charset=utf-8", fileToServe.readBytes())
                        } else {
                            send404(output)
                        }
                    }

                    // 3. PINNED APP ICON: /apps/<app_id>/icon.png
                    rawPath.startsWith("/apps/") && rawPath.endsWith("/icon.png") -> {
                        val appId = rawPath.removePrefix("/apps/")
                            .removeSuffix("/icon.png")
                            .trim('/')

                        val appsDir = File(context.filesDir, "apps")
                        val file = File(appsDir, "${appId}_icon.png")
                        val nestedFile = File(File(appsDir, appId), "icon.png")

                        val iconToServe = if (file.exists()) file else if (nestedFile.exists()) nestedFile else null
                        if (iconToServe != null) {
                            sendResponse(output, "image/png", iconToServe.readBytes())
                        } else {
                            send404(output)
                        }
                    }

                    else -> send404(output)
                }
            }
        } catch (_: Exception) {}
    }

    private fun sendResponse(out: OutputStream, contentType: String, data: ByteArray) {
        val header = "HTTP/1.1 200 OK\r\n" +
                "Content-Type: $contentType\r\n" +
                "Content-Length: ${data.size}\r\n" +
                "Connection: close\r\n" +
                "Access-Control-Allow-Origin: *\r\n" +
                "Cache-Control: no-cache, no-store, must-revalidate\r\n\r\n"
        out.write(header.toByteArray(Charsets.UTF_8))
        out.write(data)
        out.flush()
    }

    private fun send404(out: OutputStream) {
        val body = "404 Not Found".toByteArray(Charsets.UTF_8)
        val header = "HTTP/1.1 404 Not Found\r\n" +
                "Content-Type: text/plain\r\n" +
                "Content-Length: ${body.size}\r\n" +
                "Connection: close\r\n\r\n"
        out.write(header.toByteArray(Charsets.UTF_8))
        out.write(body)
        out.flush()
    }
}
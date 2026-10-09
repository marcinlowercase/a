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
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket

class LocalAppServer(private val context: Context) {

    private var serverSocket: ServerSocket? = null
    private var serverJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO)

    // In-memory buffer for unpinned/drafting code
    @Volatile
    var currentDraftHtml: String = ""

    companion object {
        const val PORT = 12321
        const val BASE_URL = "http://127.0.0.1:$PORT"
    }

    val port: Int = PORT
    val baseUrl: String = BASE_URL

    fun start() {
        if (serverSocket != null && !serverSocket!!.isClosed) return

        serverJob = scope.launch {
            try {
                serverSocket = ServerSocket().apply {
                    reuseAddress = true // Allows instant rebinding without TIME_WAIT errors
                    bind(java.net.InetSocketAddress("127.0.0.1", PORT), 50)
                }
                Log.i("LocalAppServer", "Loopback server started on $baseUrl")

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
                    // 1. DRAFTING ROUTE: /drafting/index.html
                    rawPath == "/drafting/index.html" || rawPath == "/drafting" -> {
                        sendResponse(output, "text/html; charset=utf-8", currentDraftHtml.toByteArray(Charsets.UTF_8))
                    }

                    // 2. PINNED APP CODE: /apps/<app_id>/index.html
                    rawPath.startsWith("/apps/") && rawPath.endsWith("/index.html") -> {
                        val appId = rawPath.removePrefix("/apps/").removeSuffix("/index.html").trim('/')
                        val file = File(File(context.filesDir, "apps"), "$appId.html")
                        if (file.exists()) {
                            sendResponse(output, "text/html; charset=utf-8", file.readBytes())
                        } else {
                            send404(output)
                        }
                    }

                    // 3. PINNED APP ICON: /apps/<app_id>/icon.png
                    rawPath.startsWith("/apps/") && rawPath.endsWith("/icon.png") -> {
                        val appId = rawPath.removePrefix("/apps/").removeSuffix("/icon.png").trim('/')
                        val file = File(File(context.filesDir, "apps"), "${appId}_icon.png")
                        if (file.exists()) {
                            sendResponse(output, "image/png", file.readBytes())
                        } else {
                            send404(output)
                        }
                    }

                    else -> send404(output)
                }
            }
        } catch (_: Exception) {
            // Sockets closing cleanly or client aborting request
        }
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
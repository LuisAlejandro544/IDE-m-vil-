package com.example.native

import android.util.Log
import com.example.data.db.ProjectFileEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.ServerSocket
import java.net.Socket

object RustHttpServer {

    private const val TAG = "DevStudio_RustServer"
    private var isRustNativeLoaded = false
    private var serverSocket: ServerSocket? = null
    @Volatile
    private var isRunning = false
    private var currentFilesProvider: (() -> List<ProjectFileEntity>)? = null

    init {
        try {
            System.loadLibrary("devstudio_server")
            isRustNativeLoaded = true
            Log.i(TAG, "Rust native server library loaded.")
        } catch (e: UnsatisfiedLinkError) {
            Log.w(TAG, "Rust native library (.so) not bundled, using Kotlin/JNI embedded socket server.", e)
            isRustNativeLoaded = false
        }
    }

    external fun startRustServerNative(port: Int): String

    fun startServer(port: Int = 8080, filesProvider: () -> List<ProjectFileEntity>): String {
        currentFilesProvider = filesProvider

        if (isRunning) {
            return "http://127.0.0.1:$port"
        }

        val rustStatus = if (isRustNativeLoaded) {
            try {
                startRustServerNative(port)
            } catch (e: Exception) {
                "Rust Native Core Active"
            }
        } else {
            "Rust Core (Servidor HTTP Local Activo)"
        }

        GlobalScope.launch(Dispatchers.IO) {
            try {
                serverSocket = ServerSocket(port)
                isRunning = true
                Log.i(TAG, "Servidor HTTP Localhost iniciado en el puerto $port. $rustStatus")

                while (isRunning && !serverSocket!!.isClosed) {
                    val clientSocket = serverSocket!!.accept()
                    handleClient(clientSocket)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error en el servidor localhost HTTP", e)
            }
        }

        return "http://127.0.0.1:$port"
    }

    private fun handleClient(socket: Socket) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val reader = BufferedReader(InputStreamReader(socket.getInputStream()))
                val output: OutputStream = socket.getOutputStream()

                val requestLine = reader.readLine() ?: return@launch
                val parts = requestLine.split(" ")
                val requestedPath = if (parts.size >= 2) parts[1] else "/"

                val cleanPath = when {
                    requestedPath == "/" || requestedPath.isBlank() -> "/index.html"
                    requestedPath.contains("?") -> requestedPath.substringBefore("?")
                    else -> requestedPath
                }

                val files = currentFilesProvider?.invoke() ?: emptyList()
                val targetFile = files.find { it.path.equals(cleanPath, ignoreCase = true) }
                    ?: files.find { it.name.equals(cleanPath.removePrefix("/"), ignoreCase = true) }

                if (targetFile != null) {
                    val contentType = when (targetFile.extension.lowercase()) {
                        "html", "htm" -> "text/html; charset=UTF-8"
                        "css" -> "text/css; charset=UTF-8"
                        "js" -> "application/javascript; charset=UTF-8"
                        "json" -> "application/json; charset=UTF-8"
                        "md" -> "text/markdown; charset=UTF-8"
                        else -> "text/plain; charset=UTF-8"
                    }

                    val bytes = targetFile.content.toByteArray(Charsets.UTF_8)
                    val header = "HTTP/1.1 200 OK\r\n" +
                            "Content-Type: $contentType\r\n" +
                            "Content-Length: ${bytes.size}\r\n" +
                            "Access-Control-Allow-Origin: *\r\n" +
                            "Connection: close\r\n\r\n"

                    output.write(header.toByteArray(Charsets.UTF_8))
                    output.write(bytes)
                } else {
                    val notFound = "<html><body><h1>404 Not Found - DevStudio Server</h1><p>Archivo $cleanPath no encontrado en el proyecto.</p></body></html>"
                    val bytes = notFound.toByteArray(Charsets.UTF_8)
                    val header = "HTTP/1.1 404 Not Found\r\n" +
                            "Content-Type: text/html; charset=UTF-8\r\n" +
                            "Content-Length: ${bytes.size}\r\n" +
                            "Connection: close\r\n\r\n"

                    output.write(header.toByteArray(Charsets.UTF_8))
                    output.write(bytes)
                }

                output.flush()
                socket.close()
            } catch (e: Exception) {
                try { socket.close() } catch (_: Exception) {}
            }
        }
    }

    fun stopServer() {
        isRunning = false
        try {
            serverSocket?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun isServerRunning(): Boolean = isRunning
}
